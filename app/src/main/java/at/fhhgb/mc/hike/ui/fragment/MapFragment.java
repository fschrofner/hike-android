package at.fhhgb.mc.hike.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import at.fhhgb.mc.hike.BuildConfig;
import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.app.Database;
import at.fhhgb.mc.hike.app.Helper;
import at.fhhgb.mc.hike.model.database.DatabaseException;
import at.fhhgb.mc.hike.model.database.HikeRoute;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.fhhgb.mc.hike.model.events.StartHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.StopHikeTrackingEvent;
import at.flosch.logwrap.Log;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class MapFragment extends GlobalFragment {
    final static int ZOOM_LEVEL_HIKING = 18;
    final static String HIKE_UNIQUE_ID = "hike.unique.id";

    @BindView(R.id.mapview)
    MapView mMapView;

    @BindView(R.id.start_tracking_button)
    Button mStartButton;

    @BindView(R.id.stop_tracking_button)
    Button mStopButton;

    long mHikeUniqueId = Long.MIN_VALUE;
    MapController mMapController;
    ArrayList<GeoPoint> mPath;

    private final String TAG = MapFragment.class.getSimpleName();

    public static MapFragment newInstance(){
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Log.d(TAG, "no saved instance state");
        } else {
            Log.d(TAG, "saved instance state available");
        }

        if(savedInstanceState != null && savedInstanceState.containsKey(HIKE_UNIQUE_ID)){
            Log.d(TAG, "recreated map fragment, retrieved unique id");
            mHikeUniqueId = savedInstanceState.getLong(HIKE_UNIQUE_ID);
        }
    }

    private void loadPathFromDatabase(){
        try {
            HikeRoute hikeRoute = Database.getHikeRouteFromDatabase(mHikeUniqueId);
            mPath = hikeRoute.getPathAsGeoPoints();

            if(mPath.size() > 1){
                Polyline polyline = new Polyline();
                polyline.setPoints(mPath);
                polyline.setColor(Color.BLACK);
                mMapView.getOverlays().add(polyline);
            }

            mMapView.invalidate();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "saving instance state");
        super.onSaveInstanceState(outState);
        outState.putLong(HIKE_UNIQUE_ID, mHikeUniqueId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setView(R.layout.fragment_map, inflater, container);
        setupMap();

        if(mHikeUniqueId == Long.MIN_VALUE){
            setupForNewHike();
        } else {
            setupForExistingHike();
        }

        return mRootView;
    }

    private void setupForNewHike(){
        Log.d(TAG, "setup for new hike");
        mHikeUniqueId = Helper.generateUniqueId();
        mPath = new ArrayList<>();
        showStartButton();
    }

    private void showStartButton(){
        mStartButton.setVisibility(View.VISIBLE);
        mStopButton.setVisibility(View.GONE);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearMap();
                showStopButton();
                EventBus.getDefault().post(new StartHikeTrackingEvent(mHikeUniqueId));
            }
        });
    }

    private void showStopButton(){
        mStartButton.setVisibility(View.GONE);
        mStopButton.setVisibility(View.VISIBLE);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupForNewHike();
                EventBus.getDefault().post(new StopHikeTrackingEvent());
            }
        });
    }

    private void clearMap(){
        mMapView.getOverlays().clear();
        mMapView.invalidate();
    }

    private void setupForExistingHike(){
        Log.d(TAG, "setup for existing hike");
        loadPathFromDatabase();
        showStopButton();
    }

    private void setupMap(){
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        mMapController = (MapController) mMapView.getController();
        mMapView.setMaxZoomLevel(20);
        mMapView.setMinZoomLevel(3);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setMultiTouchControls(true);
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
        mMapView.getOverlays().clear();

        Log.d(TAG, "received location update from service");
        GeoPoint geoPoint = new GeoPoint(event.getLocation());

        //TODO: don't always zoom to position (should be disabled when user tries to move view)
        mMapController.setCenter(geoPoint);
        mMapController.setZoom(ZOOM_LEVEL_HIKING);
        mPath.add(geoPoint);

        if(mPath.size() > 1){
            Polyline polyline = new Polyline();
            polyline.setPoints(mPath);
            polyline.setColor(Color.BLACK);

            //TODO: don't recreate the overlay all the time, just update it
            mMapView.getOverlays().add(polyline);
        }

        Marker userMarker = new Marker(mMapView);
        userMarker.setPosition(geoPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMapView.getOverlays().add(userMarker);

        mMapView.invalidate();
    }
}
