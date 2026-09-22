package com.trawlingtracker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.QuantityFormatter;

class TrawlingTrackerOverlay extends OverlayPanel
{
	private static final String TARGET = "Trawling tracker";

	/**
	 * Some net rolls every 2-5 ticks while trawling, so a longer gap means you've stopped.
	 */
	private static final Duration TRAWLING_TIMEOUT = Duration.ofSeconds(6);

	private static final Color HEADER_COLOR = Color.ORANGE;
	private static final Color MIXED_SHOAL_COLOR = Color.CYAN;

	/**
	 * Space kept between a line's left and right text.
	 */
	private static final int COLUMN_GAP = 10;

	private final TrawlingTrackerPlugin plugin;
	private final TrawlingTrackerConfig config;

	/**
	 * Measures this frame's lines, so the panel can be made wide enough for the longest.
	 */
	private FontMetrics metrics;
	private int widestLine;

	@Inject
	TrawlingTrackerOverlay(TrawlingTrackerPlugin plugin, TrawlingTrackerConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
		this.config = config;
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OverlayManager.OPTION_CONFIGURE, TARGET);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Reset Soup", TARGET, e -> plugin.confirmResetSoup());
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Reset rare fish", TARGET, e -> plugin.chooseRareFishToReset());
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Reset angler's paint", TARGET, e -> plugin.confirmResetPaint());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		metrics = graphics.getFontMetrics();
		widestLine = 0;
		Instant now = Instant.now();
		TrawlAttemptCounter soup = plugin.getSoup();
		RareFishCounter rareFish = plugin.getRareFish();
		PaintCounter paint = plugin.getPaint();
		List<RareFish> shownFish = plugin.getShownFish();

		boolean showSoup = config.showSoup() && isRecent(soup.getLastAttempt(), now, TrawlAttemptCounter.TRIP_GAP);
		boolean showRareFish = !shownFish.isEmpty() && isRecent(rareFish.getLastCatch(), now, TrawlAttemptCounter.TRIP_GAP);
		boolean showPaint = config.showAnglersPaint() && rareFish.getCurrent() != null
			&& isRecent(paint.getLastCatch(), now, TrawlAttemptCounter.TRIP_GAP);
		if (!showSoup && !showRareFish && !showPaint)
		{
			return null;
		}

		boolean trawling = isRecent(plugin.getLastSailingXpDrop(), now, TRAWLING_TIMEOUT);
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(trawling ? "Trawling" : "NOT trawling")
			.color(trawling ? Color.GREEN : Color.RED)
			.build());

		if (showSoup)
		{
			int oneIn = config.dropRate();
			long attempts = soup.getAttempts();
			addHeader("Soup", "1/" + QuantityFormatter.formatNumber(oneIn));
			addLine("Attempts:", QuantityFormatter.formatNumber(attempts));
			int perHour = soup.attemptsPerHour(now);
			if (perHour >= 0)
			{
				addLine("Attempts/hr:", QuantityFormatter.formatNumber(perHour));
			}
			addOdds(new DropOdds().add(attempts, 1.0 / oneIn));
		}

		if (showRareFish)
		{
			Shoal shoal = rareFish.getCurrent();
			if (shoal.isMixed())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(shoal.getDisplayName() + " shoal")
					.leftColor(MIXED_SHOAL_COLOR)
					.build());
			}

			for (RareFish fish : shownFish)
			{
				addHeader(fish.getDisplayName(), shoal.rollFor(fish).rateText());
				addLine("Catches:", QuantityFormatter.formatNumber(rareFish.getTotalCatches(fish)));
				addOdds(rareFish.getOdds(fish));
			}
		}

		if (showPaint)
		{
			addHeader("Angler's paint", "1/" + QuantityFormatter.formatNumber(PaintCounter.oneIn(rareFish.getCurrent())));
			addLine("Your catches:", QuantityFormatter.formatNumber(paint.getTotalCatches()));
			addOdds(paint.getOdds());
		}

		// Only used until the overlay is resized by hand, which then sets the width instead
		int width = widestLine + 2 * ComponentConstants.STANDARD_BORDER;
		panelComponent.setPreferredSize(new Dimension(Math.max(ComponentConstants.STANDARD_WIDTH, width), 0));
		return super.render(graphics);
	}

	private void measure(String left, String right)
	{
		widestLine = Math.max(widestLine, metrics.stringWidth(left) + COLUMN_GAP + metrics.stringWidth(right));
	}

	private void addHeader(String name, String rate)
	{
		measure(name, rate);
		panelComponent.getChildren().add(LineComponent.builder()
			.left(name)
			.leftColor(HEADER_COLOR)
			.right(rate)
			.rightColor(HEADER_COLOR)
			.build());
	}

	private void addLine(String left, String right)
	{
		measure(left, right);
		panelComponent.getChildren().add(LineComponent.builder()
			.left(left)
			.right(right)
			.build());
	}

	private void addOdds(DropOdds odds)
	{
		addLine("Drop rate:", String.format("%.3fx", odds.timesDropRate()));
		addLine("Chance by now:", String.format("%.2f%%", 100 * odds.chanceByNow()));
	}

	private static boolean isRecent(Instant time, Instant now, Duration within)
	{
		return time != null && Duration.between(time, now).compareTo(within) <= 0;
	}
}
