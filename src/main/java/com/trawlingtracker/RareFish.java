package com.trawlingtracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum RareFish
{
	GIANT_BLUE_KRILL("Giant blue krill"),
	GOLDEN_HADDOCK("Golden haddock"),
	ORANGEFIN("Orangefin"),
	HUGE_HALIBUT("Huge halibut"),
	PURPLEFIN("Purplefin"),
	SWIFT_MARLIN("Swift marlin");

	private final String displayName;
}
