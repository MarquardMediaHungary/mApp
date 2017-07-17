package com.onceapps.m.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.onceapps.core.util.Broadcast;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.ViewUtils;
import com.onceapps.m.R;
import com.onceapps.m.ui.PageRequest;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by balage on 18/04/16.
 * App related util functions
 */
@SuppressWarnings("unused")
public class Utils {
    private static final String CLIENT_VERSION_HEADER = "API-ClientVersion";
    public static final String WEBVIEW_APP_BASE_URL = "http://www.marquardmedia.hu"; //file:///android_asset/";
    public static final String WEBVIEW_MIME_TYPE = "text/html";
    public static final String WEBVIEW_ENCODING = "UTF-8";

    public static final String FB_APP_PACKAGE_NAME = "com.facebook.katana";

    public static final String CLIENT_VERSION_SUPPORTED = "supported";
    public static final String CLIENT_VERSION_UNSUPPORTED = "unsupported";
    public static final String CLIENT_VERSION_DEPRECATED = "deprecated";

    public static boolean isClientVersionSupported(Map<String, List<String>> headers) {
        if (headers == null || !headers.containsKey(CLIENT_VERSION_HEADER)) return true;
        String clientVersionHeader = headers.get(CLIENT_VERSION_HEADER).get(0);
        boolean supported;
        if (clientVersionHeader == null) {
            supported = true;
            Logger.warn("client version unknown: null");
        } else {
            if (CLIENT_VERSION_SUPPORTED.equalsIgnoreCase(clientVersionHeader)) {
                supported = true;
            } else if (CLIENT_VERSION_UNSUPPORTED.equalsIgnoreCase(clientVersionHeader)) {
                supported = false;
                Logger.warn("client version unsupported: %s", clientVersionHeader);
                Broadcast.postEvent(new PageRequest(PageRequest.Page.ClientVersionUnsupported));
            } else if (CLIENT_VERSION_DEPRECATED.equalsIgnoreCase(clientVersionHeader)) {
                supported = false;
                Logger.warn("client version deprecated: %s", clientVersionHeader);
                Broadcast.postEvent(new PageRequest(PageRequest.Page.ClientVersionDeprecated));
            } else {
                supported = true;
                Logger.warn("client version unknown: %s", clientVersionHeader);
            }
        }
        return supported;
    }

    public static List<File> unTar(final InputStream tarInputStream, final File outputDir) throws IOException, ArchiveException {

        final List<File> extractedFiles = new LinkedList<>();
        final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, tarInputStream);
        TarArchiveEntry entry;
        while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
            final File outputFile = new File(outputDir, entry.getName());
            if (entry.isDirectory()) {
                Logger.info(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
                if (!outputFile.exists()) {
                    Logger.info(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                    }
                }
            } else {
                long startTime = System.currentTimeMillis();
                final OutputStream outputFileStream = new FileOutputStream(outputFile);
                IOUtils.copy(debInputStream, outputFileStream);
                long finishTime = System.currentTimeMillis();
                Logger.debug(String.format(Locale.getDefault(), "Created output file %s in %d ms", outputFile.getAbsolutePath(), (finishTime - startTime)));
                IOUtils.closeQuietly(outputFileStream);
            }
            extractedFiles.add(outputFile);
        }
        IOUtils.closeQuietly(debInputStream);

        return extractedFiles;
    }

    public static void loadImageResized(final SimpleDraweeView draweeView, final String url) {
        if (!TextUtils.isEmpty(url)) {

            final ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {

                @Override
                public void onFailure(String id, Throwable throwable) {
                    Logger.warn(throwable, "Error loading %s", id);
                }
            };

            draweeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Uri uri = Uri.parse(url);
                    int width = draweeView.getMeasuredWidth();
                    int height = draweeView.getMeasuredHeight();
                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setResizeOptions(new ResizeOptions(width, height))
                            .build();
                    PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                            .setOldController(draweeView.getController())
                            .setImageRequest(request)
                            .setControllerListener(controllerListener)
                            .build();
                    draweeView.setController(controller);
                    draweeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            Logger.debug("can't load image because url is empty");
        }
    }

    public static void loadImageResized(final SimpleDraweeView draweeView, final String url, int width, int height) {
        if (!TextUtils.isEmpty(url)) {

            final ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {

                @Override
                public void onFailure(String id, Throwable throwable) {
                    Logger.warn(throwable, "Error loading %s", id);
                }
            };

            Uri uri = Uri.parse(url);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .setControllerListener(controllerListener)
                    .build();
            draweeView.setController(controller);
        } else {
            Logger.debug("can't load image because url is empty");
        }
    }

    public static void blur(@NonNull Context context, Bitmap bg, View view) {

        if(bg != null && ViewUtils.nextBlur(context, bg, 25)) {
            view.setBackground(new BitmapDrawable(context.getResources(), bg));
        }
        else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.mGrey14));
        }
    }


    public static String generateUserAgentString(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("http.agent")).append(" M_App/");
        try {
            sb.append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (Exception e) {
            Logger.error("error getting app version");
            sb.append("?.?");
        }
        return sb.toString();
    }

    public static float getFloatValue(Context  context, int id) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(id, typedValue, true);
        return typedValue.getFloat();
    }
}
