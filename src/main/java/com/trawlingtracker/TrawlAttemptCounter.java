package com.trawlingtracker;

import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Tracks Sailing XP drops and counts the ones judged to be the player's trawling attempts.
 */
class TrawlAttemptCounter
{
	/**
	 * A gap this long between attempts starts a new trip for the per-hour rate.
	 */
	static final Duration TRIP_GAP = Duration.ofMinutes(5);

	private static final int NO_BASELINE = -1;

	private int lastXp = NO_BASELINE;

	/**
	 * All-time attempts on this account. Saved to config by the plugin.
	 */
	@Getter
	@Setter
	private long attempts;

	@Getter
	private Instant lastAttempt;

	private Instant tripStart;
	private int tripAttempts;

	/**
	 * Sets the XP total that the next update is compared against, without producing a drop.
	 */
	void setBaseline(int xp)
	{
		lastXp = xp;
	}

	/**
	 * Forgets the last XP total, so the next update only sets a new baseline. Use this when the
	 * client is about to resend stats (e.g. a new login), which must not count as a drop.
	 */
	void clearBaseline()
	{
		lastXp = NO_BASELINE;
	}

	/**
	 * @return how much XP this update added, or 0 if it was a baseline or no increase
	 */
	int onXp(int xp)
	{
		int previous = lastXp;
		lastXp = xp;
		if (previous == NO_BASELINE)
		{
			return 0;
		}
		return Math.max(0, xp - previous);
	}

	void recordAttempt(Instant now)
	{
		attempts++;
		if (lastAttempt == null || Duration.between(lastAttempt, now).compareTo(TRIP_GAP) > 0)
		{
			tripStart = now;
			tripAttempts = 0;
		}
		tripAttempts++;
		lastAttempt = now;
	}

	/**
	 * Zeroes everything except the baseline, so the next attempt still counts.
	 */
	void reset()
	{
		attempts = 0;
		lastAttempt = null;
		tripStart = null;
		tripAttempts = 0;
	}

	/**
	 * Attempts per hour this trip, or -1 until there are two attempts to measure between. The
	 * first attempt only starts the clock, so it isn't counted in the rate; this keeps the rate
	 * honest early in a trip and lets it decay while no attempts are coming in.
	 */
	int attemptsPerHour(Instant now)
	{
		if (tripAttempts < 2)
		{
			return -1;
		}

		long elapsedMs = Duration.between(tripStart, now).toMillis();
		if (elapsedMs <= 0)
		{
			return -1;
		}
		return (int) ((tripAttempts - 1) * 3_600_000L / elapsedMs);
	}
}
