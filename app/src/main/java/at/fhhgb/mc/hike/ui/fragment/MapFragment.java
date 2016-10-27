package at.fhhgb.mc.hike.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.flosch.logwrap.Log;

/**
 * @author Florian Schrofner
 */

public class MapFragment extends GlobalFragment {
    private final String TAG = MapFragment.class.getSimpleName();

    public static MapFragment newInstance(){
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setView(R.layout.fragment_map, inflater, container);
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onLocationUpdated(LocationUpdateEvent event){
        Log.d(TAG, "received location update from service");
    }
}
