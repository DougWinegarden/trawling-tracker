# Trawling Tracker

A RuneLite plugin that tracks your deep sea trawling luck: attempts at the Soup pet
(1/360,000), catches toward every rare fish the shoal you're fishing can give, and your own
catches toward angler's paint (1/18,000, or 1/16,200 at mixed shoals).

```
 Trawling                 <- red "NOT trawling" if no Sailing XP in 6 seconds
 Soup          1/360,000
 Attempts:        41,212
 Attempts/hr:      1,998
 Drop rate:       0.114x  <- drops you'd expect by now
 Chance by now:   10.82%  <- chance you'd have had one by now
 Vibrant shoal            <- only at a mixed shoal
 Swift marlin    1/6,000  <- your usual fish stays on top
 Catches:          2,310
 Drop rate:       0.385x
 Chance by now:   31.96%
 Purplefin       1/6,000  <- extra fish, gone once you're back at a regular shoal
 Catches:            140
 Drop rate:       0.023x
 Chance by now:    2.31%
 Angler's paint 1/16,200  <- 1/18,000 at regular shoals
 Your catches:     1,420
 Drop rate:       0.085x
 Chance by now:    8.16%
```

**Compact view** in the settings trims that to two lines each, the name and count together:

```
 Trawling
 Soup attempts:          41,212
 Chance by now:          10.82%
 Vibrant shoal
 Swift marlin catches:    2,310
 Chance by now:          31.96%
 Angler's paint catches:  1,420
 Chance by now:           8.16%
```

The settings page has a **How it works** section (click to open) explaining everything below.
You can hide Soup, angler's paint, all rare fish, or each rare fish separately; counting carries
on either way. Counts are saved per account. Right-click the overlay for **Reset Soup**,
**Reset angler's paint** or **Reset rare fish** (pick which of the fish on screen).

Kept count by hand before installing? Open **Your counts** in the settings while logged in and
type your totals in. It shows every count on your account as of when you opened the settings, and
changing one sets it (a chat message confirms). Typed-in catches count at the regular shoal's rate.

## Soup: your attempts

Soup rolls once per Sailing XP drop from **your** trawling net; your crewmates' drops never roll it.
RuneLite only sees your Sailing XP total, so the plugin goes by the size of each drop, matching
sizes exactly because the sizes in between are the crew's. With a cotton net:

| Drop | Who | Counted? |
| --- | --- | --- |
| 15 | You, at the right depth | yes |
| 7-8 | You, at the wrong depth (7.5 XP) | yes |
| 25-26 | You and a crewmate on the same tick (15 + 10.5) | yes, once |
| 18 | You at the wrong depth plus a crewmate (7.5 + 10.5) | yes, once |
| 10-11 | A crewmate on their own (10.5 XP) | no |
| 21 | Two crewmates on the same tick | no |
| larger | Some other Sailing activity | no |

The list lives in **Counted XP drops** in the settings. Drops only count while you're trawling,
which means any catch, yours or the crew's, in the last minute; Sailing XP from anything else is
ignored, as is a drop that comes with "You gain some experience by watching your crew work"
(crewmates trimming the sails).

The game keeps XP to one decimal place and shows it rounded down, which is why 7.5 arrives as 7 or
8 while a whole 15 always arrives as 15.

These sizes assume you have no **Keg of Horizon's Lure** aboard: its 2.5% Sailing XP bonus changes
every size, so 15 becomes 15.3 and arrives as 15 or 16. Switching **Horizon's Lure aboard** on or
off fills the list in with that keg's sizes (`7, 8, 15, 16, 18, 19, 26`), which you can then edit.
A **Keg of Trawler's Trust** is fine either way, since its extra fish don't change XP.

With another net or different crew Deckhandiness you'll see other sizes: turn on **Show counting in
chat**, read yours off, and list them.

## Rare fish: everyone's catches

Every successful catch, by you or a crewmate, rolls for the shoal's rare fish. Each catch posts a
chat message ("You catch 3 marlin!", "Jolly Jim catches 2 marlin!"). The shoal nearest your boat
that gives that fish decides which rare fish it rolled for:

| Shoal | Rare fish (chance per catch) |
| --- | --- |
| Giant krill | giant blue krill 1/2,000 |
| Haddock | golden haddock 1/2,000 |
| Yellowfin | orangefin 1/4,000 |
| Halibut | huge halibut 1/4,000 |
| Bluefin | purplefin 1/6,000 |
| Marlin | swift marlin 1/6,000 |
| Shimmering (from krill or haddock) | giant blue krill 9/20,000, golden haddock 1/2,500, orangefin 1/10,000, huge halibut 1/20,000 |
| Glistening (from yellowfin or halibut) | orangefin 9/40,000, huge halibut 1/5,000, purplefin 1/20,000, swift marlin 1/40,000 |
| Vibrant (from bluefin or marlin) | purplefin 1/6,000, swift marlin 1/6,000 |

A regular shoal has a 10% chance to turn into its mixed shoal for two stops. Catches made while
your net is full don't post a catch message, so they aren't counted.

## Angler's paint: your catches

Angler's paint rolls on every successful catch you make with your own trawling net: 1/18,000 at
regular shoals, 1/16,200 at mixed ones. Crewmates' catches never roll it, so only your own "You
catch ..." messages count. Rod fishing at a shoal doesn't roll it either, but its catch messages
look the same, so rod catches would be counted.

## The stats

- **Drop rate:** drops you'd expect by now, adding up each roll's chance. 1.000x means you've
  rolled as many times as the rate's denominator.
- **Chance by now:** `1 - (1 - p)^n` for n rolls at chance p. Rolls at different rates (a rare
  fish caught at both regular and mixed shoals) multiply their `(1 - p)^n` together first.
  63.2% at 1x, 86.5% at 2x, 95.0% at 3x.

Turn on **Show counting in chat** to see every XP drop and catch, and what it counted toward.

## Installing

In RuneLite, open the **Plugin Hub** (the plug icon in the Configuration panel), search for
**Trawling Tracker** and click **Install**.

## Development

To run the plugin from source in a development RuneLite client, run `./gradlew run`, or on Windows
double-click `run-client.bat`. To log in with a Jagex account, do the one-time setup in
[RuneLite's guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) first.

Rates and mechanics come from the OSRS wiki pages for
[Deep sea trawling](https://oldschool.runescape.wiki/w/Deep_sea_trawling),
[Soup](https://oldschool.runescape.wiki/w/Soup) and
[Angler's paint](https://oldschool.runescape.wiki/w/Angler%27s_paint).
