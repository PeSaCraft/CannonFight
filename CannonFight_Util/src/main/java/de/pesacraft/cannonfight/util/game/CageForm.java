package de.pesacraft.cannonfight.util.game;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

public enum CageForm {
	@SuppressWarnings("unchecked")
	PLAYER(new Triple[]{new ImmutableTriple<Integer, Integer, Integer>(0, -1, 0),
						new ImmutableTriple<Integer, Integer, Integer>(1, 0, 0),
						new ImmutableTriple<Integer, Integer, Integer>(0, 0, 1),
						new ImmutableTriple<Integer, Integer, Integer>(-1, 0, 0),
						new ImmutableTriple<Integer, Integer, Integer>(0, 0, -1),
						new ImmutableTriple<Integer, Integer, Integer>(1, 1, 0),
						new ImmutableTriple<Integer, Integer, Integer>(0, 1, 1),
						new ImmutableTriple<Integer, Integer, Integer>(-1, 1, 0),
						new ImmutableTriple<Integer, Integer, Integer>(0, 1, -1),
						new ImmutableTriple<Integer, Integer, Integer>(0, 2, 0)});
	
	private final Triple<Integer, Integer, Integer>[] offsets;
	
	private CageForm(Triple<Integer, Integer, Integer>[] offsets) {
		this.offsets = offsets;
	}
	
	public Triple<Integer, Integer, Integer>[] getOffsets() {
		return offsets;
	}
}
