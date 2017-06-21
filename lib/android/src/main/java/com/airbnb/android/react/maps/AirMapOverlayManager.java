package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class AirMapOverlayManager extends ViewGroupManager<AirMapOverlay> {
    private final DisplayMetrics metrics;

    public AirMapOverlayManager(ReactApplicationContext reactContext) {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            metrics = new DisplayMetrics();
            ((WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay()
                    .getRealMetrics(metrics);
        } else {
            metrics = reactContext.getResources().getDisplayMetrics();
        }
    }

    @Override
    public String getName() {
        return "AIRMapOverlay";
    }

    @Override
    public AirMapOverlay createViewInstance(ThemedReactContext context) {
        return new AirMapOverlay(context);
    }

    @ReactProp(name = "bounds")
    public void setCoordinates(AirMapOverlay view, ReadableArray bounds) {
        view.setCoordinates(bounds);
    }

    @ReactProp(name = "image")
    public void setImage(AirMapOverlay view, String uri) {
        view.setImage(uri);
    }

    @ReactProp(name = "visible", defaultBoolean = true)
    public void setVisible(AirMapOverlay view, boolean visible) {
        view.setVisible(visible);
    }

    @ReactProp(name = "transparency", defaultFloat = 0f)
    public void setTransparency(AirMapOverlay view, float transparency) {
        view.setTransparency(transparency);
    }

    @ReactProp(name = "zIndex", defaultFloat = 1.0f)
    public void setZIndex(AirMapOverlay view, float zIndex) {
        view.setZIndex(zIndex);
    }

    @ReactProp(name = "rotation", defaultFloat = 0f)
    public void setBearing(AirMapOverlay view, float bearing) {
        view.setBearing(bearing);
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
            "onPress", MapBuilder.of("registrationName", "onPress")
        );
    }
}