package com.trawlingtracker;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.game.chatbox.ChatboxTextMenuInput;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.Text;

/**
 * Soup rolls once per Sailing XP drop from the player's own trawling net, never from a crewmate's.
 * The client is only told the new XP total, not what earned it, so attempts are picked out by the
 * size of each drop (see {@link CountedXpDrops}).
 * <p>
 * Rare fish roll once per successful catch by the player or a crewmate. Every catch posts a chat
 * message naming the fish. The shoal nearest the boat that yields that fish decides which rare fish,
 * and at what rates, the catch rolled for.
 * <p>
 * Angler's paint rolls once per successful catch by the player only.
 */
@PluginDescriptor(
	name = "Trawling Tracker",
	description = "Tracks your deep sea trawling luck: Soup, rare fish and angler's paint",
	tags = {"sailing", "trawling", "pet", "soup", "rare fish", "marlin", "paint", "fishing", "drop rate"}
)
public class TrawlingTrackerPlugin extends Plugin
{
	private static final String ATTEMPTS_KEY = "attempts";
	private static final String CATCHES_KEY_PREFIX = "catches_";
	private static final String PAINT_KEY = "paintCatches";
	private static final String PAINT_MIXED_KEY = "paintCatches_MIXED";
	private static final String CREW_XP_MESSAGE = "You gain some experience by watching your crew work";
	private static final String COUNTED_DROPS_KEY = "countedXpDrops";
	private static final String HORIZONS_LURE_KEY = "horizonsLure";

	/**
	 * How long after the last catch Sailing XP drops still count as trawling.
	 */
	private static final Duration TRAWLING_WINDOW = Duration.ofSeconds(60);

	/**
	 * Replaced by {@link #COUNTED_DROPS_KEY}, and migrated on startup.
	 */
	private static final String OLD_MIN_XP_KEY = "minXp";
	private static final String OLD_MAX_XP_KEY = "maxXp";
	private static final int OLD_MIN_XP_DEFAULT = 15;
	private static final int OLD_MAX_XP_DEFAULT = 30;

	/**
	 * Settings that show each count and set it when edited.
	 */
	private static final String SOUP_COUNT_KEY = "countSoup";
	private static final String PAINT_COUNT_KEY = "countPaint";
	private static final Map<RareFish, String> FISH_COUNT_KEYS = new EnumMap<>(RareFish.class);

	static
	{
		FISH_COUNT_KEYS.put(RareFish.GIANT_BLUE_KRILL, "countGiantBlueKrill");
		FISH_COUNT_KEYS.put(RareFish.GOLDEN_HADDOCK, "countGoldenHaddock");
		FISH_COUNT_KEYS.put(RareFish.ORANGEFIN, "countOrangefin");
		FISH_COUNT_KEYS.put(RareFish.HUGE_HALIBUT, "countHugeHalibut");
		FISH_COUNT_KEYS.put(RareFish.PURPLEFIN, "countPurplefin");
		FISH_COUNT_KEYS.put(RareFish.SWIFT_MARLIN, "countSwiftMarlin");
	}

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TrawlingTrackerOverlay overlay;

	@Inject
	private TrawlingTrackerConfig config;

	@Getter
	private TrawlAttemptCounter soup;

	@Getter
	private RareFishCounter rareFish;

	@Getter
	private PaintCounter paint;

	private CountedXpDrops countedDrops;

	/**
	 * Any Sailing XP drop, crewmates' included: while trawling one lands every few ticks.
	 */
	@Getter
	private Instant lastSailingXpDrop;

	/**
	 * Shoal clickboxes in the scene, including ones inside a moving shoal's own world view.
	 */
	private final Map<GameObject, Shoal> shoals = new HashMap<>();

	/**
	 * Sailing XP gained this tick. It's judged at the end of the tick, once any crew XP message
	 * for the same tick has come in.
	 */
	private int pendingXp;
	private boolean crewXpThisTick;

	/**
	 * What the plugin last wrote into each count setting, so its own writes aren't mistaken for
	 * the player setting a count.
	 */
	private final Map<String, Long> shownCounts = new ConcurrentHashMap<>();

	@Override
	protected void startUp()
	{
		soup = new TrawlAttemptCounter();
		rareFish = new RareFishCounter();
		paint = new PaintCounter();
		lastSailingXpDrop = null;
		pendingXp = 0;
		crewXpThisTick = false;
		shoals.clear();
		shownCounts.clear();
		migrateXpRange();
		refreshCountedDrops();
		HowItWorks.BY_KEY.forEach((key, text) -> configManager.setConfiguration(TrawlingTrackerConfig.GROUP, key, text));
		overlayManager.add(overlay);

		clientThread.invoke(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}

			loadCounts();

			// Turned on mid-session: no StatChanged is coming to set the baseline, so take the
			// current total now or the next real drop would be swallowed as the baseline.
			// Zero means stats haven't loaded yet, and the login StatChanged will set it.
			int xp = client.getSkillExperience(Skill.SAILING);
			if (xp > 0)
			{
				soup.setBaseline(xp);
			}
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		shoals.clear();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.SAILING)
		{
			return;
		}

		int drop = soup.onXp(event.getXp());
		if (drop > 0)
		{
			lastSailingXpDrop = Instant.now();
			pendingXp += drop;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int drop = pendingXp;
		boolean fromCrew = crewXpThisTick;
		pendingXp = 0;
		crewXpThisTick = false;
		if (drop == 0)
		{
			return;
		}

		Instant now = Instant.now();
		boolean trawling = isTrawling(now);
		boolean attempt = !fromCrew && trawling && countedDrops.counts(drop);
		if (attempt)
		{
			soup.recordAttempt(now);
			saveSoup();
		}

		if (config.showXpDropsInChat())
		{
			chat("Sailing XP drop: " + drop + (attempt ? " (Soup attempt)"
				: fromCrew ? " (crew XP, not counted)"
				: !trawling ? " (not trawling, not counted)"
				: " (not counted)"));
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		// Crewmates' catches are filtered game messages, which the client still receives as SPAM
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		if (message.startsWith(CREW_XP_MESSAGE))
		{
			crewXpThisTick = true;
			return;
		}

		CatchMessage caught = CatchMessage.parse(message);
		Shoal regular = caught == null ? null : Shoal.regularFromCatch(caught.getCaught());
		if (regular == null)
		{
			return;
		}

		Instant now = Instant.now();
		Shoal shoal = shoalBeingFished(regular);
		rareFish.recordCatch(shoal, now);

		List<String> countedToward = new ArrayList<>();
		for (Shoal.Roll roll : shoal.getRolls())
		{
			RareFish fish = roll.getFish();
			saveCatches(fish);
			countedToward.add(fish.getDisplayName().toLowerCase());
		}

		if (caught.isByPlayer())
		{
			paint.recordCatch(shoal, now);
			savePaint();
			countedToward.add("angler's paint");
		}

		if (config.showXpDropsInChat())
		{
			chat("Catch at " + shoal.getDisplayName().toLowerCase() + " shoal counted toward "
				+ String.join(", ", countedToward));
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		Shoal shoal = Shoal.fromObjectId(object.getId());
		if (shoal != null)
		{
			shoals.put(object, shoal);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		shoals.remove(event.getGameObject());
	}

	@Subscribe
	public void onWorldViewUnloaded(WorldViewUnloaded event)
	{
		int id = event.getWorldView().getId();
		shoals.keySet().removeIf(object -> object.getWorldView() == null || object.getWorldView().getId() == id);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
				// Stats are resent on the next login, possibly for a different account.
				soup.clearBaseline();
				shoals.clear();
				break;
			case LOADING:
				// The main scene is being rebuilt and its objects will spawn again
				shoals.keySet().removeIf(object -> object.getWorldView() == null || object.getWorldView().isTopLevel());
				break;
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		loadCounts();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!TrawlingTrackerConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		String key = event.getKey();
		if (HORIZONS_LURE_KEY.equals(key))
		{
			// Fill the list in, so the player can see the sizes being counted and edit them after
			String sizes = config.horizonsLure() ? CountedXpDrops.WITH_HORIZONS_LURE : CountedXpDrops.DEFAULT;
			configManager.setConfiguration(TrawlingTrackerConfig.GROUP, COUNTED_DROPS_KEY, sizes);
			refreshCountedDrops();
			clientThread.invoke(() ->
			{
				if (client.getGameState() == GameState.LOGGED_IN)
				{
					chat("Counting Sailing XP drops of " + sizes + ".");
				}
			});
			return;
		}

		if (COUNTED_DROPS_KEY.equals(key))
		{
			refreshCountedDrops();
			return;
		}

		// The How it works text boxes can be typed in, so put the text back
		String text = HowItWorks.BY_KEY.get(key);
		if (text != null && !text.equals(event.getNewValue()))
		{
			configManager.setConfiguration(TrawlingTrackerConfig.GROUP, key, text);
			return;
		}

		if (isCountKey(key) && event.getNewValue() != null)
		{
			long value;
			try
			{
				value = Long.parseLong(event.getNewValue());
			}
			catch (NumberFormatException e)
			{
				return;
			}

			Long shown = shownCounts.get(key);
			if (shown == null || shown != value)
			{
				// Edits arrive on the settings panel's thread; the counts belong to the client thread
				clientThread.invoke(() -> setCount(key, value));
			}
		}
	}

	/**
	 * Sets a count from its setting, after the player edits it.
	 */
	private void setCount(String key, long value)
	{
		if (configManager.getRSProfileKey() == null)
		{
			// Not logged into an account to set it on; the setting is put right on the next login
			return;
		}

		String name;
		if (key.equals(SOUP_COUNT_KEY))
		{
			if (soup.getAttempts() == value)
			{
				return;
			}
			soup.setAttempts(value);
			saveSoup();
			name = "Soup attempts";
		}
		else if (key.equals(PAINT_COUNT_KEY))
		{
			if (paint.getTotalCatches() == value)
			{
				return;
			}
			paint.setTotalCatches(value);
			savePaint();
			name = "Angler's paint catches";
		}
		else
		{
			RareFish fish = fishForCountKey(key);
			if (rareFish.getTotalCatches(fish) == value)
			{
				return;
			}
			rareFish.setTotalCatches(fish, value);
			saveCatches(fish);
			name = fish.getDisplayName() + " catches";
		}

		chat(name + " set to " + QuantityFormatter.formatNumber(value) + ".");
	}

	private void refreshCountedDrops()
	{
		countedDrops = CountedXpDrops.parse(config.countedXpDrops());
	}

	/**
	 * Whether a net is being worked at a shoal, so that Sailing XP from anything else isn't taken
	 * for a trawling attempt. A catch says so plainly, and between you and your crew they come
	 * many times a minute, so the last one being recent is enough to go on.
	 */
	private boolean isTrawling(Instant now)
	{
		Instant lastCatch = rareFish.getLastCatch();
		return lastCatch != null && Duration.between(lastCatch, now).compareTo(TRAWLING_WINDOW) <= 0;
	}

	/**
	 * The min/max XP range was replaced by a list of counted drop sizes. A range the player had
	 * changed is written out as the list of the sizes it allowed, so their setting isn't lost.
	 */
	private void migrateXpRange()
	{
		Integer min = configManager.getConfiguration(TrawlingTrackerConfig.GROUP, OLD_MIN_XP_KEY, int.class);
		Integer max = configManager.getConfiguration(TrawlingTrackerConfig.GROUP, OLD_MAX_XP_KEY, int.class);
		if (min == null && max == null)
		{
			return;
		}

		int from = min == null ? OLD_MIN_XP_DEFAULT : min;
		int to = max == null ? OLD_MAX_XP_DEFAULT : max;
		boolean changedByPlayer = from != OLD_MIN_XP_DEFAULT || to != OLD_MAX_XP_DEFAULT;
		boolean listStillDefault = CountedXpDrops.DEFAULT.equals(
			configManager.getConfiguration(TrawlingTrackerConfig.GROUP, COUNTED_DROPS_KEY));
		if (changedByPlayer && listStillDefault && from <= to && to - from < 64)
		{
			StringBuilder sizes = new StringBuilder();
			for (int size = from; size <= to; size++)
			{
				sizes.append(sizes.length() == 0 ? "" : ", ").append(size);
			}
			configManager.setConfiguration(TrawlingTrackerConfig.GROUP, COUNTED_DROPS_KEY, sizes.toString());
		}

		configManager.unsetConfiguration(TrawlingTrackerConfig.GROUP, OLD_MIN_XP_KEY);
		configManager.unsetConfiguration(TrawlingTrackerConfig.GROUP, OLD_MAX_XP_KEY);
	}

	private static boolean isCountKey(String key)
	{
		return key.equals(SOUP_COUNT_KEY) || key.equals(PAINT_COUNT_KEY) || fishForCountKey(key) != null;
	}

	private static RareFish fishForCountKey(String key)
	{
		for (Map.Entry<RareFish, String> entry : FISH_COUNT_KEYS.entrySet())
		{
			if (entry.getValue().equals(key))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	boolean isShown(RareFish fish)
	{
		if (!config.showRareFish())
		{
			return false;
		}

		switch (fish)
		{
			case GIANT_BLUE_KRILL:
				return config.showGiantBlueKrill();
			case GOLDEN_HADDOCK:
				return config.showGoldenHaddock();
			case ORANGEFIN:
				return config.showOrangefin();
			case HUGE_HALIBUT:
				return config.showHugeHalibut();
			case PURPLEFIN:
				return config.showPurplefin();
			case SWIFT_MARLIN:
				return config.showSwiftMarlin();
			default:
				return true;
		}
	}

	/**
	 * The rare fish the overlay shows for the shoal being fished.
	 */
	List<RareFish> getShownFish()
	{
		List<RareFish> shown = new ArrayList<>(rareFish.getCurrentFish());
		shown.removeIf(fish -> !isShown(fish));
		return shown;
	}

	/**
	 * Asks in the chatbox before wiping the count, since it can represent hundreds of hours.
	 */
	void confirmResetSoup()
	{
		chatboxPanelManager.openTextMenuInput("Reset your Soup attempts ("
				+ QuantityFormatter.formatNumber(soup.getAttempts()) + ") to 0?")
			.option("Yes, reset it", () ->
			{
				soup.reset();
				saveSoup();
			})
			.option("No", () -> {})
			.build();
	}

	void confirmResetPaint()
	{
		chatboxPanelManager.openTextMenuInput("Reset your angler's paint catches ("
				+ QuantityFormatter.formatNumber(paint.getTotalCatches()) + ") to 0?")
			.option("Yes, reset it", () ->
			{
				paint.reset();
				savePaint();
			})
			.option("No", () -> {})
			.build();
	}

	/**
	 * Offers the rare fish the overlay is showing; picking one resets it at every shoal type.
	 */
	void chooseRareFishToReset()
	{
		List<RareFish> shown = getShownFish();
		if (shown.isEmpty())
		{
			chat("No rare fish on the overlay to reset.");
			return;
		}

		ChatboxTextMenuInput menu = chatboxPanelManager.openTextMenuInput("Reset which rare fish count to 0?");
		for (RareFish fish : shown)
		{
			menu.option(fish.getDisplayName() + " (" + QuantityFormatter.formatNumber(rareFish.getTotalCatches(fish)) + ")",
				() -> resetRareFish(fish));
		}
		menu.option("Cancel", () -> {});
		menu.build();
	}

	private void resetRareFish(RareFish fish)
	{
		rareFish.reset(fish);
		saveCatches(fish);
	}

	/**
	 * The nearest shoal to the boat that yields the caught fish. Falls back to the fish's own
	 * regular shoal if none is in view.
	 */
	private Shoal shoalBeingFished(Shoal regular)
	{
		Player player = client.getLocalPlayer();
		LocalPoint boat = player == null ? null : toMainWorld(player.getLocalLocation());
		if (boat == null)
		{
			return regular;
		}

		Shoal nearest = regular;
		int nearestDistance = Integer.MAX_VALUE;
		for (Map.Entry<GameObject, Shoal> entry : shoals.entrySet())
		{
			if (!entry.getValue().yields(regular))
			{
				continue;
			}

			LocalPoint location = toMainWorld(entry.getKey().getLocalLocation());
			int distance = location == null ? Integer.MAX_VALUE : boat.distanceTo(location);
			if (distance < nearestDistance)
			{
				nearest = entry.getValue();
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	/**
	 * Moves a point inside a boat's or shoal's own world view onto the main map, so points in
	 * different world views can be compared.
	 */
	private LocalPoint toMainWorld(LocalPoint point)
	{
		if (point == null)
		{
			return null;
		}

		WorldView top = client.getTopLevelWorldView();
		if (point.getWorldView() == top.getId())
		{
			return point;
		}

		WorldEntity entity = top.worldEntities().byIndex(point.getWorldView());
		return entity == null ? null : entity.transformToMainWorld(point);
	}

	private void loadCounts()
	{
		soup.reset();
		soup.setAttempts(load(ATTEMPTS_KEY));

		rareFish = new RareFishCounter();
		for (Shoal shoal : Shoal.values())
		{
			for (Shoal.Roll roll : shoal.getRolls())
			{
				rareFish.setCatches(roll.getFish(), shoal, load(catchesKey(roll.getFish(), shoal)));
			}
		}

		paint = new PaintCounter();
		paint.setRegularCatches(load(PAINT_KEY));
		paint.setMixedCatches(load(PAINT_MIXED_KEY));

		showCount(SOUP_COUNT_KEY, soup.getAttempts());
		showCount(PAINT_COUNT_KEY, paint.getTotalCatches());
		FISH_COUNT_KEYS.forEach((fish, key) -> showCount(key, rareFish.getTotalCatches(fish)));
	}

	private void saveSoup()
	{
		configManager.setRSProfileConfiguration(TrawlingTrackerConfig.GROUP, ATTEMPTS_KEY, soup.getAttempts());
		showCount(SOUP_COUNT_KEY, soup.getAttempts());
	}

	private void savePaint()
	{
		configManager.setRSProfileConfiguration(TrawlingTrackerConfig.GROUP, PAINT_KEY, paint.getRegularCatches());
		configManager.setRSProfileConfiguration(TrawlingTrackerConfig.GROUP, PAINT_MIXED_KEY, paint.getMixedCatches());
		showCount(PAINT_COUNT_KEY, paint.getTotalCatches());
	}

	private void saveCatches(RareFish fish)
	{
		for (Shoal shoal : Shoal.values())
		{
			if (shoal.rollFor(fish) != null)
			{
				configManager.setRSProfileConfiguration(TrawlingTrackerConfig.GROUP, catchesKey(fish, shoal),
					rareFish.getCatches(fish, shoal));
			}
		}
		showCount(FISH_COUNT_KEYS.get(fish), rareFish.getTotalCatches(fish));
	}

	/**
	 * Writes a count into its setting, so the settings page shows it.
	 */
	private void showCount(String key, long count)
	{
		int shown = (int) Math.min(count, Integer.MAX_VALUE);
		shownCounts.put(key, (long) shown);
		configManager.setConfiguration(TrawlingTrackerConfig.GROUP, key, shown);
	}

	private long load(String key)
	{
		Long saved = configManager.getRSProfileConfiguration(TrawlingTrackerConfig.GROUP, key, long.class);
		return saved == null ? 0 : saved;
	}

	private static String catchesKey(RareFish fish, Shoal shoal)
	{
		// Regular shoals keep the key used before mixed shoals were counted separately
		return CATCHES_KEY_PREFIX + fish.name() + (shoal.isMixed() ? "_" + shoal.name() : "");
	}

	private void chat(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	@Provides
	TrawlingTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TrawlingTrackerConfig.class);
	}
}
