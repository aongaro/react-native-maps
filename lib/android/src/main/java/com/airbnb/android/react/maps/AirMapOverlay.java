package com.airbnb.android.react.maps;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.Color;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import javax.annotation.Nullable;

public class AirMapOverlay extends AirMapFeature {

    private GroundOverlayOptions overlayOptions;
    private GroundOverlay overlay;
    private List<LatLng> coordinates;
    private LatLngBounds boundsLatLng;
    
    private boolean visible;
    private float transparency;
    private float zIndex;
    private float bearing;

    private Bitmap imgBitmap;
    private BitmapDescriptor imgBitmapDescriptor;
    private final DraweeHolder<?> imageHolder;
    private DataSource<CloseableReference<CloseableImage>> dataSource;

    private final ControllerListener<ImageInfo> controllerListener =
            new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(
                        String id,
                        @Nullable final ImageInfo imageInfo,
                        @Nullable Animatable animatable) {
                    CloseableReference<CloseableImage> imageReference = null;
                    try {
                        imageReference = dataSource.getResult();
                        if (imageReference != null) {
                            CloseableImage image = imageReference.get();
                            if (image != null && image instanceof CloseableStaticBitmap) {
                                CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
                                Bitmap bitmap = closeableStaticBitmap.getUnderlyingBitmap();
                                if (bitmap != null) {
                                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                    imgBitmap = bitmap;
                                    imgBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                                }
                            }
                        }
                    } finally {
                        dataSource.close();
                        if (imageReference != null) {
                            CloseableReference.closeSafely(imageReference);
                        }
                    }
                    update();
                }
            };

    public AirMapOverlay(Context context) {
        super(context);
        imageHolder = DraweeHolder.create(createDraweeHierarchy(), context);
        imageHolder.onAttach();
    }

    private GenericDraweeHierarchy createDraweeHierarchy() {
        return new GenericDraweeHierarchyBuilder(getResources())
                .setFadeDuration(0)
                .build();
    }

    //coordinates in form [SW, NW] (SW = [latSW, lngSW] NE = [latNE, lngNE])
    public void setCoordinates(ReadableArray coordinates) {
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableArray coordinate = coordinates.getArray(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble(0), coordinate.getDouble(1)));
        }

        this.boundsLatLng = new LatLngBounds(
                        this.coordinates.get(0),
                        this.coordinates.get(1)
                    );
        if (overlay != null) {
            overlay.setPositionFromBounds(boundsLatLng);
        }
    }

    public void setImage(String uri) {
        if (uri == null) {
            imgBitmapDescriptor = null;
        } else if (uri.startsWith("http://") || uri.startsWith("https://") ||
                uri.startsWith("file://")) {
            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(uri))
                    .build();
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setControllerListener(controllerListener)
                .setOldController(imageHolder.getController())
                .build();

            imageHolder.setController(controller);
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

    public void setBearing(float bearing) {
        this.bearing = bearing;
        if (overlay != null) {
            overlay.setBearing(bearing);
        }
    }

    public GroundOverlayOptions getGroundOverlayOptions() {
        if (overlayOptions == null) {
            overlayOptions = createGroundOverlayOptions();
        }
        return overlayOptions;
    }

    private BitmapDescriptor getImage() {
        if (imgBitmapDescriptor != null) {
            return imgBitmapDescriptor;
        } else {
            //default transparent bitmap
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(400, 400, conf);
            return BitmapDescriptorFactory.fromBitmap(bmp);
        }
    }

    public void update() {
        if (overlay == null) {
            return;
        }
        overlay.setImage(getImage());
    }

    private GroundOverlayOptions createGroundOverlayOptions() {
        GroundOverlayOptions options = new GroundOverlayOptions();
        options.positionFromBounds(boundsLatLng);
        options.image(getImage());
        options.visible(visible);
        options.transparency(transparency);
        options.zIndex(zIndex);
        options.bearing(bearing);
        return options;
    }

    @Override
    public Object getFeature() {
        return overlay;
    }

    @Override
    public void addToMap(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(boundsLatLng.getCenter()));
        overlay = map.addGroundOverlay(getGroundOverlayOptions());
        overlay.setClickable(true);
    }

    @Override
    public void removeFromMap(GoogleMap map) {
        overlay.remove();
    }
}