package at.fhhgb.mc.hike.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.esotericsoftware.kryo.serializers.FieldSerializer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

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
import at.fhhgb.mc.hike.service.LocationService;
import at.fhhgb.mc.hike.ui.activity.TagActivity;
import at.flosch.logwrap.Log;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class MapFragment extends GlobalFragment {
    final static int ZOOM_LEVEL_HIKING = 18;

    @BindView(R.id.mapview)
    MapView mMapView;

    @BindView(R.id.start_tracking_button)
    Button mStartButton;

    @BindView(R.id.stop_tracking_button)
    Button mStopButton;

    @BindView(R.id.add_tag_button)
    Button mAddTagButton;

    MyLocationNewOverlay mLocationOverlay;

    long mHikeUniqueId = Long.MIN_VALUE;
    MapController mMapController;
    ArrayList<GeoPoint> mPath;

    private final String TAG = MapFragment.class.getSimpleName();

    public static MapFragment newInstance(){
        return new MapFragment();
    }

    private void loadPathFromDatabase(){
        try {

            HikeRoute hikeRoute = Database.getHikeRouteFromDatabase(mHikeUniqueId);
            mPath = hikeRoute.getPathAsGeoPoints();

            redrawPath();
            //showUserMarker(mPath.get(mPath.size() - 1));

            mMapController.setZoom(ZOOM_LEVEL_HIKING);
            mMapController.setCenter(mPath.get(mPath.size() - 1));


            if(mLocationOverlay != null){
                mMapView.getOverlays().add(mLocationOverlay);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        showLocation();

        if(LocationService.ongoingHikeId() != null){
            Log.d(TAG, "resuming ongoing hike");
            mHikeUniqueId = LocationService.ongoingHikeId();
            setupForOngoingHike();
        } else {
            Log.d(TAG, "new hike");
            setupForNewHike();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on create view called");
        setView(R.layout.fragment_map, inflater, container);
        setupMap();
        return mRootView;
    }

    private void setupForNewHike(){
        Log.d(TAG, "setup for new hike");
        mHikeUniqueId = Long.MIN_VALUE;
        mPath = new ArrayList<>();
        showStartButton();
        mMapView.invalidate();
    }

    private void showStartButton(){
        mStartButton.setVisibility(View.VISIBLE);
        mStopButton.setVisibility(View.GONE);
        mAddTagButton.setVisibility(View.GONE);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearMap();
                showStopButton();
                mHikeUniqueId = Helper.generateUniqueId();
                EventBus.getDefault().post(new StartHikeTrackingEvent(mHikeUniqueId));
            }
        });

    }

    private void showStopButton(){
        mStartButton.setVisibility(View.GONE);
        mStopButton.setVisibility(View.VISIBLE);
        mAddTagButton.setVisibility(View.VISIBLE);

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupForNewHike();
                EventBus.getDefault().post(new StopHikeTrackingEvent());
            }
        });
        mAddTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TagActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clearMap(){
        mMapView.getOverlays().clear();
        mMapView.invalidate();
    }

    private void setupForOngoingHike(){
        Log.d(TAG, "setup for existing hike");
        clearMap();
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

    private void showLocation(){
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()),mMapView);
        mLocationOverlay.enableMyLocation();
        mMapView.getOverlays().add(this.mLocationOverlay);
        //TODO: zoom in?
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
        mMapController.setZoom(ZOOM_LEVEL_HIKING);
        mMapController.setCenter(geoPoint);
        mPath.add(geoPoint);

        //TODO: don't recreate the overlay all the time, just update it
        redrawPath();
        //showUserMarker(geoPoint);
        if(mLocationOverlay != null){
            mMapView.getOverlays().add(mLocationOverlay);
        }

        mMapView.invalidate();
    }

    private void showUserMarker(GeoPoint geoPoint){
        Marker userMarker = new Marker(mMapView);
        userMarker.setPosition(geoPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMapView.getOverlays().add(userMarker);
    }

    private void redrawPath(){
        if(mPath.size() > 1){
            Polyline polyline = new Polyline();
            polyline.setPoints(mPath);
            polyline.setColor(Color.BLACK);
            mMapView.getOverlays().add(polyline);
        }
    }
}
