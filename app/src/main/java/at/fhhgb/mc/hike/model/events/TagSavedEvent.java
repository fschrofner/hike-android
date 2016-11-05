package at.fhhgb.mc.hike.model.events;

import at.fhhgb.mc.hike.model.database.HikeTag;

/**
 * @author Florian Schrofner
 */

public class TagSavedEvent {
    private HikeTag mHikeTag;

    public TagSavedEvent(HikeTag hikeTag) {
        mHikeTag = hikeTag;
    }

    public HikeTag getHikeTag() {
        return mHikeTag;
    }
}
