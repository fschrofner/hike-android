package at.fhhgb.mc.hike.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import at.fhhgb.mc.hike.BuildConfig;
import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.model.events.LocationUpdateEvent;
import at.flosch.logwrap.Log;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class MapFragment extends GlobalFragment {
    final static int ZOOM_LEVEL_HIKING = 18;

    @BindView(R.id.mapview)
    MapView mMapView;

    MapController mMapController;

    ArrayList<GeoPoint> mPath;

    private final String TAG = MapFragment.class.getSimpleName();

    public static MapFragment newInstance(){
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setView(R.layout.fragment_map, inflater, container);
        setupMap();
        return mRootView;
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
        mPath = new ArrayList<>();
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
