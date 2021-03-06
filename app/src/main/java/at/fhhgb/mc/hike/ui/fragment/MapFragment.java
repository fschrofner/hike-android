package at.fhhgb.mc.hike.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import at.fhhgb.mc.hike.BuildConfig;
import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.app.Database;
import at.fhhgb.mc.hike.app.Helper;
import at.fhhgb.mc.hike.model.database.DatabaseException;
import at.fhhgb.mc.hike.model.database.HikeRoute;
import at.fhhgb.mc.hike.model.database.HikeStats;
import at.fhhgb.mc.hike.model.database.HikeTag;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.fhhgb.mc.hike.model.events.StartHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.StatsUpdateEvent;
import at.fhhgb.mc.hike.model.events.StopHikeTrackingEvent;
import at.fhhgb.mc.hike.model.events.TagSavedEvent;
import at.fhhgb.mc.hike.service.LocationService;
import at.fhhgb.mc.hike.ui.activity.TagActivity;
import at.flosch.logwrap.Log;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class MapFragment extends GlobalFragment {
    final static int REQUEST_CODE_CREATE_TAG = 2134;
    final static int ZOOM_LEVEL_HIKING = 18;
    final static int MAX_POINTS = 150;
    final static int PATH_COLOR = Color.BLACK;

    @BindView(R.id.mapview)
    MapView mMapView;

    @BindView(R.id.start_tracking_button)
    Button mStartButton;

    @BindView(R.id.stop_tracking_button)
    Button mStopButton;

    @BindView(R.id.add_tag_button)
    Button mAddTagButton;

    @BindView(R.id.hike_time)
    Chronometer mHikeTime;

    @BindView(R.id.hike_distance)
    TextView mHikeDistance;

    @BindView(R.id.hike_elevation)
    TextView mHikeElevation;

    @BindView(R.id.hike_stats_holder)
    LinearLayout mHikeStats;

    MyLocationNewOverlay mLocationOverlay;

    long mHikeUniqueId = Long.MIN_VALUE;
    MapController mMapController;
    ArrayList<GeoPoint> mPath;
    ArrayList<HikeTag> mTags;

    private Thread updateThread = null;
    Polyline pathOverlay = null;

    private final String TAG = MapFragment.class.getSimpleName();

    public static MapFragment newInstance(){
        return new MapFragment();
    }

    private void loadDataFromDatabase(){
        try {

            HikeRoute hikeRoute = Database.getHikeRouteFromDatabase(mHikeUniqueId);
            mPath = hikeRoute.getPathAsGeoPoints();

            //TODO: load
            mTags = hikeRoute.getTags();

            redrawEverything(mMapView, PATH_COLOR);

            mMapController.setZoom(ZOOM_LEVEL_HIKING);
            mMapController.setCenter(mPath.get(mPath.size() - 1));

            showDistance(hikeRoute.getStats().getTotalDistanceMeters());
            showElevation(hikeRoute.getStats().getPositiveElevationChangeMeters());

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        showLocation();

        if(LocationService.ongoingHike()){
            Log.d(TAG, "resuming ongoing hike");
            mHikeUniqueId = LocationService.ongoingHikeId();
            setupForOngoingHike();
        } else {
            Log.d(TAG, "new hike");
            setupForNewHike();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHikeTime.stop();
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
        mTags = new ArrayList<>();
        showStartButton();
        hideHikeStats();
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
                showHikeStats();
                getGlobalActivity().changeMenu(R.menu.hike_menu);
                mHikeUniqueId = Helper.generateUniqueId();
                mHikeTime.setBase(SystemClock.elapsedRealtime());
                mHikeTime.start();
                EventBus.getDefault().post(new StartHikeTrackingEvent(mHikeUniqueId));
            }
        });

    }

    private void showStopButton(){
        mStartButton.setVisibility(View.GONE);
        mStopButton.setVisibility(View.VISIBLE);
        //mAddTagButton.setVisibility(View.VISIBLE);

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupForNewHike();
                getGlobalActivity().changeMenu(R.menu.empty_menu);
                EventBus.getDefault().post(new StopHikeTrackingEvent());
                mHikeTime.stop();
            }
        });
    }

    private void showHikeStats(){
        mHikeStats.setVisibility(View.VISIBLE);
    }

    private void hideHikeStats(){
        mHikeStats.setVisibility(View.GONE);
    }

    private void clearMap(){
        mMapView.getOverlays().clear();
        mMapView.invalidate();
    }

    private void setupForOngoingHike(){
        Log.d(TAG, "setup for existing hike");
        clearMap();
        loadDataFromDatabase();
        showHikeStats();

        if(LocationService.startTime() != null){
            long elapsedTime = System.currentTimeMillis() - LocationService.startTime();
            mHikeTime.setBase(SystemClock.elapsedRealtime() - elapsedTime);
        }

        mHikeTime.start();
        showStopButton();
    }

    private void setupMap(){
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(ZOOM_LEVEL_HIKING);
        mMapView.setMaxZoomLevel(20);
        mMapView.setMinZoomLevel(3);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setMultiTouchControls(true);
//        mMapView.setMapListener(new MapListener() {
//            @Override
//            public boolean onScroll(ScrollEvent event) {
//                redrawEverything(mMapView, PATH_COLOR);
//                return false;
//            }
//
//            @Override
//            public boolean onZoom(ZoomEvent event) {
//                redrawEverything(mMapView, PATH_COLOR);
//                return false;
//            }
//        });
    }

    private void showLocation(){
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()),mMapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();
        mMapView.getOverlays().add(this.mLocationOverlay);

        mLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mMapController.animateTo(mLocationOverlay.getMyLocation());
            }
        });
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
        mPath.add(geoPoint);

        //TODO: don't recreate the overlay all the time, just update it
        redrawEverything(mMapView, PATH_COLOR);
    }

    @Subscribe
    public void onStatsUpdated(StatsUpdateEvent event){
        HikeStats stats = event.getHikeStats();
        showDistance(stats.getTotalDistanceMeters());
        showElevation(stats.getPositiveElevationChangeMeters());
    }

    private void showDistance(long meters){
        //TODO: format correctly
        mHikeDistance.setText(meters + " m");
    }

    private void showElevation(long meters){
        //TODO: format correctly
        mHikeElevation.setText(meters + " m");
    }

    private void showUserMarker(GeoPoint geoPoint){
        Marker userMarker = new Marker(mMapView);
        userMarker.setPosition(geoPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mMapView.getOverlays().add(userMarker);
    }

    private void showUserLocationIfEnabled(){
        if(mLocationOverlay != null){
            mMapView.getOverlays().add(mLocationOverlay);
        }
    }

    private void showTags(){
        //Log.d(TAG, "showing tags, size: " + mTags.size());
        for(HikeTag tag : mTags){
            GeoPoint geoPoint = new GeoPoint(tag.getLatitude(), tag.getLongitude());
            if(checkIfVisible(geoPoint)){
                Marker tagMarker = new Marker(mMapView);
                tagMarker.setPosition(geoPoint);

                @DrawableRes int tagIcon = R.drawable.header;

                //set icon according to type
                switch(tag.getTagType()){
                    case Title:
                        tagIcon = R.drawable.header;
                        tagMarker.setTitle(tag.getTitle());
                        break;
                    case Text:
                        tagIcon = R.drawable.paragraph;
                        tagMarker.setTitle(tag.getDescription());
                        break;
                    case Image:
                        tagIcon = R.drawable.image;
                        //TODO: load image into bubble
                        //tagMarker.setImage(ResourcesCompat.getDrawable(getResources(), tag.getPhoto(), null));
                        break;
                    case Poi:
                        tagIcon = R.drawable.poi;
                        tagMarker.setTitle(tag.getTitle());
                        break;
                }

                tagMarker.setIcon(ResourcesCompat.getDrawable(getResources(), tagIcon, null));
                tagMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mMapView.getOverlays().add(tagMarker);
            }
        }
    }

    public void redrawEverything(final MapView osmMap, final int color){
        if(updateThread == null || !updateThread.isAlive()){
            redraw(osmMap, color);
        }
    }

    private void redraw(final MapView osmMap, final int color){
        updateThread = new Thread(new Runnable() {
            public void run() {
                final ArrayList<GeoPoint> zoomPoints = new ArrayList<>(mPath);

                //Remove any points that are offscreen
                removeHiddenPoints(zoomPoints);

                //If there's still too many then thin the array
                if(zoomPoints.size() > MAX_POINTS){
                    int stepSize = (int) zoomPoints.size()/MAX_POINTS;
                    int count = 1;
                    for (Iterator<GeoPoint> iterator = zoomPoints.iterator(); iterator.hasNext();) {
                        iterator.next();

                        if(count != stepSize){
                            iterator.remove();
                        }else{
                            count = 0;
                        }

                        count++;
                    }
                }

                //Update the map on the event thread
                osmMap.post(new Runnable() {
                    public void run() {
                        //ideally the Polyline construction would happen in the thread but that causes glitches while the event thread
                        //waits for redraw:
                        osmMap.getOverlays().remove(pathOverlay);
                        pathOverlay = new Polyline();
                        pathOverlay.setPoints(zoomPoints);
                        pathOverlay.setColor(color);
                        osmMap.getOverlays().add(pathOverlay);

                        showUserLocationIfEnabled();
                        showTags();

                        osmMap.invalidate();
                    }
                });
            }
        });
        updateThread.start();
    }

    private void removeHiddenPoints(ArrayList<GeoPoint> zoomPoints) {

        for (Iterator<GeoPoint> iterator = zoomPoints.iterator(); iterator.hasNext(); ) {
            GeoPoint point = iterator.next();

            if (!checkIfVisible(point)) {
                iterator.remove();
            }
        }
    }

    private boolean checkIfVisible(GeoPoint point){
        BoundingBox bounds = mMapView.getBoundingBox();

        boolean inLongitude = point.getLatitude() < bounds.getLatNorth() && point.getLatitude() > bounds.getLatSouth();
        boolean inLatitude = point.getLongitude() > bounds.getLonWest() && point.getLongitude() < bounds.getLonEast();

        return inLongitude && inLatitude;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_add){
            Intent intent = new Intent(getContext(), TagActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CREATE_TAG);
            return true;
        } else if(item.getItemId() == R.id.menu_share){
            if(LocationService.ongoingHike()){
                String key = LocationService.ongoingHikeFirebaseId();
                String url = getString(R.string.sharing_url) + key;
                Helper.showShareIntent(getContext(), url);
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "received activity result");
        if(requestCode == REQUEST_CODE_CREATE_TAG && resultCode == TagActivity.RESULT_CODE_TAG_CREATED){
            //TODO: add marker and save tag in database
            Log.d(TAG, "a tag was created");
            Serializable ser = data.getSerializableExtra(TagActivity.EXTRA_CREATED_TAG);
            HikeTag tag = (HikeTag)ser;

            //setting the correct location
            GeoPoint lastGeopoint = mPath.get(mPath.size()-1);
            tag.setLatitude(lastGeopoint.getLatitude());
            tag.setLongitude(lastGeopoint.getLongitude());

            //add to list of tags to display
            mTags.add(tag);
            Log.d(TAG, "number of tags saved: " + mTags.size());

            //sending tag to service, so that it can be saved
            EventBus.getDefault().post(new TagSavedEvent(tag));

            redrawEverything(mMapView, PATH_COLOR);
        } else if(requestCode == REQUEST_CODE_CREATE_TAG){
            Log.d(TAG, "error when creating tag!");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
