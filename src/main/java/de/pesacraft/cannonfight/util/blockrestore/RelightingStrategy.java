package de.pesacraft.cannonfight.util.blockrestore;

/* This code is a modified version, original taken from
 * https://github.com/desht/dhutils/blob/master/Lib/src/main/java/me/desht/dhutils/block/MassBlockUpdate.java
 *  
 * Thanks to desht for creating this!
 */
public enum RelightingStrategy {
	/**
	 * Do not do any relighting calculations at all.  If any block
	 * lighting properties (i.e. light emission or light blocking)
	 * change, this may result in incorrect lighting of the changed
	 * blocks.  This strategy should be used if you are certain
	 * that no lighting properties are being changed, or if your
	 * plugin will handle relighting itself.
	 */
	NEVER,

	/**
	 * Immediately relight any blocks whose lighting properties
	 * have changed.  For very large changes (on the order of tens
	 * of thousands or more), this may result in some server lag.
	 */
	IMMEDIATE,

	/**
	 * Carry out relighting over the next several ticks, to
	 * minimise the risk of server lag.  Note that this carries
	 * a non-trivial server-side memory cost, as updated block
	 * locations need to be temporarily stored pending lighting
	 * updates.
	 */
	DEFERRED,

    /**
     * Immediately notify the client which blocks have changed.
     * Recalculate relighting in the background like DEFERRED mode.
     */
    HYBRID,
}