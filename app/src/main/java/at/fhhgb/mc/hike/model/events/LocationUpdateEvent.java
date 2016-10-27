package at.fhhgb.mc.hike.model.events;

import android.location.Location;

/**
 * @author Florian Schrofner
 */

public class LocationUpdateEvent {
    Location mLocation;

    public LocationUpdateEvent(Location location) {
        mLocation = location;
    }

    public Location getLocation() {
        return mLocation;
    }
}
