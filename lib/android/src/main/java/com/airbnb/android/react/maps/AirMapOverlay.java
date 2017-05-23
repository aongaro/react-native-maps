package com.airbnb.android.react.maps;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;


import java.util.ArrayList;
import java.util.List;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

public class AirMapOverlay extends AirMapFeature {

    private GroundOverlayOptions overlayOptions;
    private GroundOverlay overlay;

    private List<LatLng> coordinates;
    private LatLngBounds boundsLatLng;
    // private int strokeColor;
    // private int fillColor;
    // private float strokeWidth;
    // private boolean geodesic;
    private BitmapDescriptor image;
    private boolean visible;
    private float transparency;
    private float zIndex;
    private float markerHue = 0.0f;

    public AirMapOverlay(Context context) {
        super(context);
    }
    //coordinates in form [[lat, lng], [lat, lng]] se, nw
    public void setCoordinates(ReadableArray coordinates) {
        // it's kind of a bummer that we can't run map() or anything on the ReadableArray
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableArray coordinate = coordinates.getArray(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble(0), coordinate.getDouble(1)));
        }

        this.boundsLatLng = new LatLngBounds(
                        this.coordinates.get(0),
                        this.coordinates.get(0)
                    );

        if (overlay != null) {
            overlay.setPositionFromBounds(
                new LatLngBounds(
                    this.coordinates.get(0),
                    this.coordinates.get(0)
                    )
                );
        }
    }

    public void setImage(String uri) {
        if (uri == null) {
            this.image = null;
        } else if (uri.startsWith("http://") || uri.startsWith("https://") ||
                uri.startsWith("file://")) {
            try {
                URL url = new URL(uri); 
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream()); 
                this.image = BitmapDescriptorFactory.fromBitmap(bmp);
                if (overlay != null) {
                    overlay.setImage(image);
                }
            } catch (MalformedURLException e) {
                System.err.println("MalformedURLException: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }
            
        } 
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (overlay != null) {
            overlay.setVisible(visible);
        }
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
        if (overlay != null) {
            overlay.setZIndex(transparency);
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (overlay != null) {
            overlay.setZIndex(zIndex);
        }
    }

    public GroundOverlayOptions getGroundOverlayOptions() {
        if (overlayOptions == null) {
            overlayOptions = createGroundOverlayOptions();
        }
        return overlayOptions;
    }

    private BitmapDescriptor getIcon() {
        if (image != null) {
            // use local image as a marker
            return image;
        } else {
            // render the default marker pin
            return BitmapDescriptorFactory.defaultMarker(this.markerHue);
        }
    }

    private GroundOverlayOptions createGroundOverlayOptions() {
        GroundOverlayOptions options = new GroundOverlayOptions();
        options.positionFromBounds(boundsLatLng);
        options.image(image);
        options.visible(visible);
        options.transparency(transparency);
        options.zIndex(zIndex);
        return options;
    }

    @Override
    public Object getFeature() {
        return overlay;
    }

    @Override
    public void addToMap(GoogleMap map) {
        overlay = map.addGroundOverlay(getGroundOverlayOptions());
        overlay.setClickable(true);
    }

    @Override
    public void removeFromMap(GoogleMap map) {
        overlay.remove();
    }
}