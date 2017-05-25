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
import com.google.android.gms.maps.model.MarkerOptions;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.common.executors.CallerThreadExecutor;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import java.net.URL;
import android.net.Uri;
import java.net.MalformedURLException;
import java.io.IOException;

import javax.annotation.Nullable;

import  android.util.Log;

public class AirMapOverlay extends AirMapFeature {

    private GroundOverlayOptions overlayOptions;
    private GroundOverlay overlay;

    private List<LatLng> coordinates;
    private LatLngBounds boundsLatLng;
    // private int strokeColor;
    // private int fillColor;
    // private float strokeWidth;
    // private boolean geodesic;
    private BitmapDescriptor imgBitmapDescriptor;
    private boolean visible;
    private float transparency;
    private float zIndex;
    private float markerHue = 0.0f;

    private Bitmap imgBitmap;
    private DataSource<CloseableReference<CloseableImage>> dataSource;

    private final ControllerListener<ImageInfo> mLogoControllerListener =
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

    private final BaseDataSubscriber<CloseableReference<CloseableImage>> subscriber = new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                      @Override
                      protected void onNewResultImpl(
                          DataSource<CloseableReference<CloseableImage>> data) {
                        CloseableReference<CloseableImage> imageReference = null;
                        // if (!data.isFinished()) {
                        //   // if we are not interested in the intermediate images,
                        //   // we can just return here.
                        //   return;
                        // }
                        try {
                            Log.d("TRY", "SUBSCRIBE");
                            // keep the closeable reference
                            imageReference = data.getResult();
                            // do something with the result
                            if(imageReference != null) {
                                 Log.d("REF", "Not null");
                                CloseableImage image = imageReference.get();
                                if (image != null && image instanceof CloseableStaticBitmap) {
                                    Log.d("IMAGE", "Not null");
                                    CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
                                    Bitmap bitmap = closeableStaticBitmap.getUnderlyingBitmap();
                                    if (bitmap != null) {

                                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                        imgBitmap = bitmap;
                                        imgBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                                        Log.d("BMPDESC", ""+bitmap.getDensity());
                                        if(overlay != null) {
                                            overlay.setImage(imgBitmapDescriptor);
                                        }
                                    }
                                }
                            }
                        } finally {
                                dataSource.close();
                                if (imageReference != null) {
                                    CloseableReference.closeSafely(imageReference);
                                }
                        }
                        Log.d("UPDATE", "Calling");
                        // update();
                      }

                      @Override
                      protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> data) {
                       
                        // handle failure
                      }
                    };

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
                        this.coordinates.get(1)
                    );

        if(overlay != null) {
            overlay.setPositionFromBounds(
                new LatLngBounds(
                    this.coordinates.get(0),
                    this.coordinates.get(1)
                    )
                );
        }
    }

    public void setImage(String uri) {
        Log.d("SETIMG", "void setImg");
        if (uri == null) {
            imgBitmapDescriptor = null;
        } else if (uri.startsWith("http://") || uri.startsWith("https://") ||
                uri.startsWith("file://")) {
            Log.d("STARTREQ", "void setImg");
            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(uri))
                    .build();
                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
                dataSource.subscribe(subscriber, 
                    CallerThreadExecutor.getInstance());
                // try {
                //    dataSource.subscribe(new BaseBitmapDataSubscriber() {
                //        @Override
                //        public void onNewResultImpl(@Nullable Bitmap bitmap) {
                //            if (bitmap == null) {
                //             Log.d("Merda CRISTO", "Nope, niente bmp");
                //                return;
                //            }

                //         bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                //         imgBitmap = bitmap;
                //         imgBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                //         Log.d("Merda Gesu", "si bmp");
                //            // The bitmap provided to this method is only guaranteed to be around
                //            // for the lifespan of this method. The image pipeline frees the
                //            // bitmap's memory after this method has completed.
                //            //
                //            // This is fine when passing the bitmap to a system process as
                //            // Android automatically creates a copy.
                //            //
                //            // If you need to keep the bitmap around, look into using a
                //            // BaseDataSubscriber instead of a BaseBitmapDataSubscriber.
                //        }

                //        @Override
                //        public void onFailureImpl(DataSource dataSource) {
                //            Log.d("Merda CRISTO", "failure");
                //        }
                //    }, CallerThreadExecutor.getInstance());
                // } finally {
                //    if (dataSource != null) {
                //        dataSource.close();
                //        update();
                //    }
                // }
                // URL url = new URL(uri); 
                // Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream()); 
                // this.image = BitmapDescriptorFactory.fromBitmap(bmp);
                // if (overlay != null) {
                //     overlay.setImage(image);
                // }
            
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

    private BitmapDescriptor getImage() {
        // Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        //     Bitmap bmp = Bitmap.createBitmap(400, 400, conf); // this creates a MUTABLE bitmap
        //     bmp.eraseColor(Color.RED);
        //     return BitmapDescriptorFactory.fromBitmap(bmp);
        // Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        //     int[] colors = new int[20000];
        //     for (int i = 0; i < 20000; i++) {
        //         colors[i] = 0;
        //     }
        // Bitmap bmp = Bitmap.createBitmap(colors, 100, 200, conf); // this creates a MUTABLE bitmap
        // return BitmapDescriptorFactory.fromBitmap(bmp);
        if (imgBitmapDescriptor != null) {
            // use local image as a marker
            Log.d("NEMO", "not null");
            return imgBitmapDescriptor;
        } else {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = Bitmap.createBitmap(400, 400, conf); // this creates a MUTABLE bitmap
            bmp.eraseColor(Color.RED);
            return BitmapDescriptorFactory.fromBitmap(bmp);
            // render the default marker pin
            // return BitmapDescriptorFactory.defaultMarker(this.markerHue);
        }
    }

    public void update() {
        if (overlay == null) {
            return;
        }
        Log.d("UPDATE", "update");
        Log.d("POSITION", overlay.getPosition().toString());
        // overlay.setPositionFromBounds(boundsLatLng);
        overlay.setImage(getImage());
    }

    private GroundOverlayOptions createGroundOverlayOptions() {
        GroundOverlayOptions options = new GroundOverlayOptions();
        options.positionFromBounds(boundsLatLng);
        options.image(getImage());
        options.visible(true);
        options.transparency(0f);
        options.zIndex(1.0f);
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