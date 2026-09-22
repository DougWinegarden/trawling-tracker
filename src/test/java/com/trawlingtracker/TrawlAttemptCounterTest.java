package com.trawlingtracker;

import static org.junit.Assert.assertEquals;
import java.time.Instant;
import org.junit.Test;

public class TrawlAttemptCounterTest
{
	private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

	private static Instant seconds(double s)
	{
		return T0.plusMillis((long) (s * 1000));
	}

	@Test
	public void firstUpdateOnlySetsBaseline()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		assertEquals(0, counter.onXp(50_000));
		assertEquals(15, counter.onXp(50_015));
	}

	@Test
	public void sameXpIsNoDrop()
	{
		// StatChanged also fires for boosted level changes and on re-login with unchanged XP
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.onXp(50_000);
		assertEquals(0, counter.onXp(50_000));
	}

	@Test
	public void clearedBaselineSwallowsLoginResend()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.onXp(50_000);
		counter.clearBaseline();
		// Log into an account with much more Sailing XP
		assertEquals(0, counter.onXp(900_000));
		assertEquals(15, counter.onXp(900_015));
	}

	@Test
	public void explicitBaselineMakesNextUpdateADrop()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.setBaseline(50_000);
		assertEquals(15, counter.onXp(50_015));
	}

	@Test
	public void resetKeepsBaseline()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.onXp(50_000);
		counter.setAttempts(1_234);
		counter.recordAttempt(T0);
		counter.reset();
		assertEquals(0, counter.getAttempts());
		assertEquals(15, counter.onXp(50_015));
	}

	@Test
	public void attemptsAddToSavedTotal()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.setAttempts(100_000);
		counter.recordAttempt(T0);
		counter.recordAttempt(seconds(1.8));
		assertEquals(100_002, counter.getAttempts());
	}

	@Test
	public void attemptsPerHour()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.recordAttempt(T0);
		assertEquals(-1, counter.attemptsPerHour(T0));

		// One roll every 3 ticks (1.8s) is 2,000/hr
		counter.recordAttempt(seconds(1.8));
		counter.recordAttempt(seconds(3.6));
		assertEquals(2_000, counter.attemptsPerHour(seconds(3.6)));

		// Decays while idle
		assertEquals(1_000, counter.attemptsPerHour(seconds(7.2)));
	}

	@Test
	public void longGapStartsNewTrip()
	{
		TrawlAttemptCounter counter = new TrawlAttemptCounter();
		counter.recordAttempt(T0);
		counter.recordAttempt(seconds(1.8));

		Instant later = seconds(3_600);
		counter.recordAttempt(later);
		assertEquals(-1, counter.attemptsPerHour(later));
		counter.recordAttempt(later.plusMillis(1_800));
		assertEquals(2_000, counter.attemptsPerHour(later.plusMillis(1_800)));
		assertEquals(4, counter.getAttempts());
	}
}
