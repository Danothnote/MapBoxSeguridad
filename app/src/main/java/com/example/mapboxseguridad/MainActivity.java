package com.example.mapboxseguridad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener, PermissionsListener {

    private static final String SEGURIDAD_SOURCE_ID = "seguridad-id";
    private static final String HEATMAP_LAYER_ID = "seguridad-heat";
    private static final String HEATMAP_SOURCE_ID = "seguridad-source-id";
    private static final String LAYER_ID ="layer-id";

    private MapboxMap mapboxMap;
    private MapView mapView;

    private PermissionsManager permissionsManager;

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    private boolean estilo;
    private boolean mostrar;
    private boolean heat;
    private boolean rutas;
    private boolean activado = false;

    FloatingActionMenu actionMenu;

    private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
    private static final String TEAL_COLOR = "#23D2BE";
    private static final float POLYLINE_WIDTH = 5;
    private DirectionsRoute optimizedRoute;
    private MapboxOptimization optimizedClient;
    private List<Point> stops = new ArrayList<>();
    private Point origin;
    private LatLng point;
    private boolean colocadop = false;
    private boolean colocadom = false;
    private boolean colocadot = false;
    public static SQLiteHelper sqLiteHelper;

    private Marker featureMarker;
    private int conteo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mostrar = true;
        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);

        actionMenu = (FloatingActionMenu)findViewById(R.id.fab_menu);
        actionMenu.setClosedOnTouchOutside(true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.addImages) {
            Intent activityAddres =new Intent(MainActivity.this, AddAddress.class);
            startActivity(activityAddres);
            Toast.makeText(this, R.string.addImage, Toast.LENGTH_SHORT).show();
            return true;
        }else if (id == R.id.menlistImage){

            Intent activityAddres =new Intent(MainActivity.this, ImagesList.class);
            startActivity(activityAddres);
            Toast.makeText(this, R.string.btnListImages, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Danothnote/MapBoxSeguridad"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_panecillo) {
            point = new LatLng(-0.2310036, -78.519666);
            CameraPosition position = new CameraPosition.Builder()
                    .target(point) // Sets the new camera position
                    .zoom(15) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder
            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);
            if (activado) {
                if (colocadop == false){
                    if (alreadyTwelveMarkersOnMap()) {
                        Toast.makeText(MainActivity.this, R.string.only_twelve_stops_allowed, Toast.LENGTH_LONG).show();
                    } else {
                        Style style = mapboxMap.getStyle();
                        if (style != null) {
                            addDestinationMarker(style, point);
                            addPointToStopsList(point);
                            getOptimizedRoute(style, stops);
                            colocadop = true;
                        }
                    }
                }
            }
        } else if (id == R.id.nav_mitaddelmundo) {
            point = new LatLng(-0.0102496, -78.4464668);
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(-0.0102496, -78.4464668)) // Sets the new camera position
                    .zoom(15) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder
            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);
            if (activado) {
                if (colocadom == false){
                    if (alreadyTwelveMarkersOnMap()) {
                        Toast.makeText(MainActivity.this, R.string.only_twelve_stops_allowed, Toast.LENGTH_LONG).show();
                    } else {
                        Style style = mapboxMap.getStyle();
                        if (style != null) {
                            addDestinationMarker(style, point);
                            addPointToStopsList(point);
                            getOptimizedRoute(style, stops);
                            colocadom = true;
                        }
                    }
                }
            }
        } else if (id == R.id.nav_teleferico) {
            point = new LatLng(-0.1923033, -78.5193715);
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(-0.1923033, -78.5193715)) // Sets the new camera position
                    .zoom(15) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder
            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);
            if (activado) {
                if (colocadot == false){
                    if (alreadyTwelveMarkersOnMap()) {
                        Toast.makeText(MainActivity.this, R.string.only_twelve_stops_allowed, Toast.LENGTH_LONG).show();
                    } else {
                        Style style = mapboxMap.getStyle();
                        if (style != null) {
                            addDestinationMarker(style, point);
                            addPointToStopsList(point);
                            getOptimizedRoute(style, stops);
                            colocadot = true;
                        }
                    }
                }
            }
        } else if (id == R.id.nav_tema) {
            if (estilo) {
                estilo = false;
                cambiarTema(estilo);
            } else {
                estilo = true;
                cambiarTema(estilo);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setMaxZoomPreference(18.9);

        cambiarTema(false);
    }

    private void cambiarTema(boolean estilo) {
        if (estilo) {
            rutas = true;
            mostrar = true;
            mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/danohealer/cjxrm9kn36lns1cqeups6qc3m"),
                    new Style.OnStyleLoaded() {

                        @Override
                        public void onStyleLoaded(@NonNull final Style style) {
                            style.addSource(new GeoJsonSource(SEGURIDAD_SOURCE_ID, loadGeoJsonFromAsset("seguridad.geojson")));
                            style.addSource(new GeoJsonSource("source-id", loadGeoJsonFromAsset("poligono.geojson")));
                            style.addLayerBelow(new FillLayer(LAYER_ID, "source-id").withProperties(fillColor(Color.argb(80,49,187,242))), "settlement-label");
                            style.addSource(new GeoJsonSource("optimized-route-source-id"));
                            findViewById(R.id.check_rutas).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (rutas) {
                                        rutas = false;
                                        activado = true;
                                        initMarkerIconSymbolLayer(style);
                                        initOptimizedRouteLineLayer(style);
                                        if (stops.isEmpty()) {
                                            stops.add(origin);
                                        }
                                        Toast.makeText(MainActivity.this, R.string.rutas_activadas, Toast.LENGTH_SHORT).show();
                                        mapboxMap.addOnMapClickListener(MainActivity.this);
                                        mapboxMap.addOnMapLongClickListener(MainActivity.this);
                                    } else {
                                        rutas = true;
                                        activado = false;
                                        Toast.makeText(MainActivity.this, R.string.rutas_desactivadas, Toast.LENGTH_SHORT).show();
                                        stops.clear();
                                        if (mapboxMap != null) {
                                            Style style = mapboxMap.getStyle();
                                            if (style != null) {
                                                resetDestinationMarkers(style);
                                                removeOptimizedRoute(style);
                                                removeMarkerIconSymbolLayer(style);
                                            }
                                        }
                                        mapboxMap.removeOnMapClickListener(MainActivity.this);
                                        mapboxMap.removeOnMapLongClickListener(MainActivity.this);
                                    }
                                }
                            });

                            findViewById(R.id.check_poligono).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mostrar == true) {
                                        mostrar = false;
                                        style.getLayer(LAYER_ID).setProperties(fillColor(Color.argb(0,49,187,242)));
                                    } else {
                                        mostrar = true;
                                        style.getLayer(LAYER_ID).setProperties(fillColor(Color.argb(80,49,187,242)));
                                    }
                                }
                            });
                            enableLocationComponent(style);
                            addHeatmapLayer(style);
                        }
                    });
        } else {
            rutas = true;
            mostrar = true;
            mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/danohealer/cjxbcwi6s4eob1cpw6lupzig3"),
                    new Style.OnStyleLoaded() {

                        @Override
                        public void onStyleLoaded(@NonNull final Style style) {
                            style.addSource(new GeoJsonSource(SEGURIDAD_SOURCE_ID, loadGeoJsonFromAsset("seguridad.geojson")));
                            style.addSource(new GeoJsonSource("source-id", loadGeoJsonFromAsset("poligono.geojson")));
                            style.addLayerBelow(new FillLayer(LAYER_ID, "source-id").withProperties(fillColor(Color.argb(80,49,187,242))), "settlement-label");
                            style.addSource(new GeoJsonSource("optimized-route-source-id"));
                            findViewById(R.id.check_rutas).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (rutas) {
                                        rutas = false;
                                        activado = true;
                                        initMarkerIconSymbolLayer(style);
                                        initOptimizedRouteLineLayer(style);
                                        if (stops.isEmpty()) {
                                            stops.add(origin);
                                        }
                                        Toast.makeText(MainActivity.this, R.string.rutas_activadas, Toast.LENGTH_SHORT).show();
                                        mapboxMap.addOnMapClickListener(MainActivity.this);
                                        mapboxMap.addOnMapLongClickListener(MainActivity.this);
                                    } else {
                                        rutas = true;
                                        activado = false;
                                        Toast.makeText(MainActivity.this, R.string.rutas_desactivadas, Toast.LENGTH_SHORT).show();
                                        stops.clear();
                                        if (mapboxMap != null) {
                                            Style style = mapboxMap.getStyle();
                                            if (style != null) {
                                                resetDestinationMarkers(style);
                                                removeOptimizedRoute(style);
                                                removeMarkerIconSymbolLayer(style);
                                            }
                                        }
                                        mapboxMap.removeOnMapClickListener(MainActivity.this);
                                        mapboxMap.removeOnMapLongClickListener(MainActivity.this);
                                    }
                                }
                            });

                            findViewById(R.id.check_poligono).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mostrar == true) {
                                        mostrar = false;
                                        style.getLayer(LAYER_ID).setProperties(fillColor(Color.argb(0,49,187,242)));
                                    } else {
                                        mostrar = true;
                                        style.getLayer(LAYER_ID).setProperties(fillColor(Color.argb(80,49,187,242)));
                                    }
                                }
                            });
                            enableLocationComponent(style);
                            addHeatmapLayer(style);
                        }
                    });
        }
    }

    private String loadGeoJsonFromAsset(String filename) {
        try {
// Load GeoJSON file
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (Exception exception) {
            Timber.e("Exception loading GeoJSON: %s", exception.toString());
            exception.printStackTrace();
            return null;
        }
    }

    private void addHeatmapLayer(@NonNull Style loadedMapStyle) {
        final HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, SEGURIDAD_SOURCE_ID);
        layer.setMaxZoom(19);
        layer.setSourceLayer(HEATMAP_SOURCE_ID);
        layer.setProperties(
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0.01), rgba(0, 0, 0, 0),
                                literal(0.1), rgba(0, 2, 114, .1),
                                literal(0.2), rgba(0, 6, 219, .15),
                                literal(0.3), rgba(0, 74, 255, .2),
                                literal(0.4), rgba(0, 202, 255, .25),
                                literal(0.5), rgba(73, 255, 154, .3),
                                literal(0.6), rgba(171, 255, 59, .35),
                                literal(0.7), rgba(255, 197, 3, .4),
                                literal(0.8), rgba(255, 82, 1, 0.6),
                                literal(0.9), rgba(196, 0, 1, 0.6),
                                literal(0.95), rgba(121, 0, 0, 0.6)
                        )
                ),
                heatmapIntensity(2f),
                heatmapRadius(30f),
                heatmapOpacity(0.6f)
        );
        loadedMapStyle.addLayerAbove(layer, "referencia");
        heat=true;
        findViewById(R.id.check_heat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (heat) {
                    heat = false;
                    layer.setProperties(
                            heatmapColor(
                                    interpolate(
                                            linear(), heatmapDensity(),
                                            literal(0.01), rgba(0, 0, 0, 0),
                                            literal(0.1), rgba(0, 2, 114, 0),
                                            literal(0.2), rgba(0, 6, 219, 0),
                                            literal(0.3), rgba(0, 74, 255, 0),
                                            literal(0.4), rgba(0, 202, 255, 0),
                                            literal(0.5), rgba(73, 255, 154, 0),
                                            literal(0.6), rgba(171, 255, 59, 0),
                                            literal(0.7), rgba(255, 197, 3, 0),
                                            literal(0.8), rgba(255, 82, 1, 0),
                                            literal(0.9), rgba(196, 0, 1, 0),
                                            literal(0.95), rgba(121, 0, 0, 0)
                                    )
                            ),
                            heatmapIntensity(2f),
                            heatmapRadius(30f),
                            heatmapOpacity(0f)
                    );
                } else {
                    heat = true;
                    layer.setProperties(
                            heatmapColor(
                                    interpolate(
                                            linear(), heatmapDensity(),
                                            literal(0.01), rgba(0, 0, 0, 0),
                                            literal(0.1), rgba(0, 2, 114, .1),
                                            literal(0.2), rgba(0, 6, 219, .15),
                                            literal(0.3), rgba(0, 74, 255, .2),
                                            literal(0.4), rgba(0, 202, 255, .25),
                                            literal(0.5), rgba(73, 255, 154, .3),
                                            literal(0.6), rgba(171, 255, 59, .35),
                                            literal(0.7), rgba(255, 197, 3, .4),
                                            literal(0.8), rgba(255, 82, 1, 0.6),
                                            literal(0.9), rgba(196, 0, 1, 0.6),
                                            literal(0.95), rgba(121, 0, 0, 0.6)
                                    )
                            ),
                            heatmapIntensity(2f),
                            heatmapRadius(30f),
                            heatmapOpacity(0.6f)
                    );
                }
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        MainActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                    buttonLocation(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude());
                    origin = Point.fromLngLat(result.getLastLocation().getLongitude(), result.getLastLocation().getLatitude());
                    getOrigin(origin);
                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Point getOrigin(Point origin){
        if (stops.isEmpty()) {
            stops.add(origin);
        }
        return origin;
    }

    public void buttonLocation(final double lat, final double lng) {
        FloatingActionButton floatingActionButton = findViewById(R.id.myLocationButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(lat, lng)) // Sets the new camera position
                        .zoom(15) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 500);
            }
        });
    }

    private void initMarkerIconSymbolLayer(@NonNull Style loadedMapStyle) {
// Add the marker image to map
        loadedMapStyle.addImage("icon-image", BitmapFactory.decodeResource(
                this.getResources(), R.drawable.mapbox_marker_icon_default));
// Add the source to the map
        loadedMapStyle.addLayer(new SymbolLayer("icon-layer-id", ICON_GEOJSON_SOURCE_ID).withProperties(
                iconImage("icon-image"),
                iconSize(1f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -4f})
        ));
    }

    private void removeMarkerIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.removeImage("icon-image");
        loadedMapStyle.removeLayer("icon-layer-id");
        loadedMapStyle.removeLayer("optimized-route-layer-id");
    }

    private void initOptimizedRouteLineLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayerBelow(new LineLayer("optimized-route-layer-id", "optimized-route-source-id")
                .withProperties(
                        lineColor(Color.parseColor(TEAL_COLOR)),
                        lineWidth(POLYLINE_WIDTH)
                ), "icon-layer-id");
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
// Optimization API is limited to 12 coordinate sets
        if (alreadyTwelveMarkersOnMap()) {
            Toast.makeText(MainActivity.this, R.string.only_twelve_stops_allowed, Toast.LENGTH_LONG).show();
        } else {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                addDestinationMarker(style, point);
                addPointToStopsList(point);
                getOptimizedRoute(style, stops);
            }
        }
        return true;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        stops.clear();
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                resetDestinationMarkers(style);
                removeOptimizedRoute(style);
                if (stops.isEmpty()) {
                    stops.add(origin);
                }
                return true;
            }
        }
        return false;
    }

    private void resetDestinationMarkers(@NonNull Style style) {
        colocadop = false;
        colocadom = false;
        colocadot = false;
        mapboxMap.removeAnnotations();
        conteo = 0;
    }

    private void removeOptimizedRoute(@NonNull Style style) {
        GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
        if (optimizedLineSource != null) {
            optimizedLineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
        }
    }

    private boolean alreadyTwelveMarkersOnMap() {
        return stops.size() == 12;
    }

    private void addDestinationMarker(@NonNull Style style, LatLng point) {
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel);
        conteo++;
        if (features.size() > 0) {
            Feature feature = features.get(0);
            String property;
            StringBuilder stringBuilder = new StringBuilder();
            if (feature.properties() != null) {
                if (feature.properties().get("name") != null) {
                    stringBuilder.append(String.format("%s", feature.properties().get("name")));
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                if (feature.properties().get("type") != null) {
                    stringBuilder.append(String.format("%s", feature.properties().get("type")));
                    stringBuilder.append(System.getProperty("line.separator"));
                } else if (feature.properties().get("class") != null) {
                    stringBuilder.append(String.format("%s", feature.properties().get("class")));
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                featureMarker = mapboxMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(Integer.toString(conteo))
                        .snippet(stringBuilder.toString())
                );

            } else {
                property = getString(R.string.query_feature_marker_snippet);
                featureMarker = mapboxMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(Integer.toString(conteo))
                        .snippet(property)
                );
            }
        } else {
            featureMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(Integer.toString(conteo))
                    .snippet(getString(R.string.query_feature_marker_snippet))
            );
        }
        mapboxMap.selectMarker(featureMarker);
    }

    private void addPointToStopsList(LatLng point) {
        stops.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
    }

    private void getOptimizedRoute(@NonNull final Style style, List<Point> coordinates) {
        optimizedClient = MapboxOptimization.builder()
                .source("first")
                .destination("last")
                .coordinates(coordinates)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .roundTrip(false)
                .accessToken(Mapbox.getAccessToken())
                .build();

        optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
            @Override
            public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
                if (!response.isSuccessful()) {
                    Timber.d( getString(R.string.no_success));
                    Toast.makeText(MainActivity.this, R.string.no_success, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (response.body().trips().isEmpty()) {
                        Timber.d("%s size = %s", getString(R.string.successful_but_no_routes), response.body().trips().size());

                        Toast.makeText(MainActivity.this, R.string.successful_but_no_routes,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

// Get most optimized route from API response
                optimizedRoute = response.body().trips().get(0);
                drawOptimizedRoute(style, optimizedRoute);
            }

            @Override
            public void onFailure(Call<OptimizationResponse> call, Throwable throwable) {
                Timber.d("Error: %s", throwable.getMessage());
            }
        });
    }

    private void drawOptimizedRoute(@NonNull Style style, DirectionsRoute route) {
        GeoJsonSource optimizedLineSource = style.getSourceAs("optimized-route-source-id");
        if (optimizedLineSource != null) {
            optimizedLineSource.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
                    LineString.fromPolyline(route.geometry(), PRECISION_6))));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationEngine != null) {
            initLocationEngine();
        }

        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (optimizedClient != null) {
            optimizedClient.cancelCall();
        }
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
// Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
