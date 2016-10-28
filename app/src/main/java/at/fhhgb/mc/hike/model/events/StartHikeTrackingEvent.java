package at.fhhgb.mc.hike.model.events;

/**
 * @author Florian Schrofner
 */

public class StartHikeTrackingEvent {
    long mUniqueId;

    public StartHikeTrackingEvent(long uniqueId) {
        mUniqueId = uniqueId;
    }

    public long getUniqueId() {
        return mUniqueId;
    }
}
