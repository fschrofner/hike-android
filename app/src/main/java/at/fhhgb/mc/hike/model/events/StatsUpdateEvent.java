package at.fhhgb.mc.hike.model.events;

import at.fhhgb.mc.hike.model.database.HikeStats;

/**
 * @author Florian Schrofner
 */

public class StatsUpdateEvent {
    private HikeStats mHikeStats;

    public StatsUpdateEvent(HikeStats hikeStats) {
        mHikeStats = hikeStats;
    }

    public HikeStats getHikeStats() {
        return mHikeStats;
    }
}
