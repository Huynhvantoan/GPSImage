package com.toandev.gpsimage.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.Videoio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipInputStream;

public class MyUtils {
    public static boolean isCameraUsebyApp() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextEntry(HashMap.java:854)
	at java.util.HashMap$KeyIterator.next(HashMap.java:885)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:286)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:173)
*/
        /*
        r0 = 0;
        r0 = android.hardware.Camera.open();	 Catch:{ RuntimeException -> 0x000c, all -> 0x0014 }
        if (r0 == 0) goto L_0x000a;
    L_0x0007:
        r0.release();
    L_0x000a:
        r2 = 0;
    L_0x000b:
        return r2;
    L_0x000c:
        r1 = move-exception;
        r2 = 1;
        if (r0 == 0) goto L_0x000b;
    L_0x0010:
        r0.release();
        goto L_0x000b;
    L_0x0014:
        r2 = move-exception;
        if (r0 == 0) goto L_0x001a;
    L_0x0017:
        r0.release();
    L_0x001a:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.hson.gpsimage.MyUtils.isCameraUsebyApp():boolean");
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(src).openConnection();
            connection.setDoInput(true);
            connection.connect();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (IOException e) {
            return null;
        }
    }

    public static Bitmap getBitmapFromFile(File file) {
        Bitmap myBitmap = null;
        if (file.exists()) {
            myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (myBitmap == null) {
                Log.i("hson", "decode file null - " + file.getAbsolutePath());
            }
        }
        return myBitmap;
    }

    public static Bitmap combineImage(Bitmap bmp1, Bitmap bmp2) {
        Log.i("hson", bmp1 == null ? "null" : "not null");
        Log.i("hson", bmp2 == null ? "null" : "not null");
        Bitmap resultImage = Bitmap.createBitmap(bmp1.getWidth() + bmp2.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(resultImage);
        canvas.drawBitmap(bmp1, 0.0f, 0.0f, null);
        canvas.drawBitmap(bmp2, (float) bmp1.getWidth(), 0.0f, null);
        return resultImage;
    }

    @SuppressLint("WrongConstant")
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @SuppressLint("WrongConstant")
    public static boolean isLocationEnabled(Context context) {
         LocationManager manager = (LocationManager) context.getSystemService("location");
        return manager.isProviderEnabled("gps") || manager.isProviderEnabled("network");
    }

    public static void saveBitmap(Bitmap bmp, String filename) {
        Exception e;
        Throwable th;
        FileOutputStream out = null;
        try {
            FileOutputStream out2 = new FileOutputStream(filename);
            try {
                bmp.compress(CompressFormat.JPEG, 100, out2);
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        out = out2;
                        return;
                    }
                }
                out = out2;
            } catch (Exception e3) {
                e = e3;
                out = out2;
                try {
                    e.printStackTrace();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    try {
                        throw th;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                    out.close();
                }
                try {
                    throw th;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static boolean copyExifData(File sourceFile, File destFile, List<TagInfo> excludedFields, double longitude, double latitude) {
        ImageReadException exception;
        Throwable th;
        ImageWriteException exception2;
        IOException exception3;
        File tempFile = null;
        OutputStream tempStream = null;
        try {
            File tempFile2 = new File(destFile.getAbsolutePath() + ".tmp");
            try {
                TiffOutputSet sourceSet = getSanselanOutputSet(sourceFile, 73);
                TiffOutputSet destSet = getSanselanOutputSet(destFile, sourceSet.byteOrder);
                if (sourceSet.byteOrder != destSet.byteOrder) {
                    if (tempStream != null) {
                        try {
                            tempStream.close();
                        } catch (IOException e) {
                        }
                    }
                    if (tempFile2 != null && tempFile2.exists()) {
                        tempFile2.delete();
                    }
                    tempFile = tempFile2;
                    return false;
                }
                destSet.getOrCreateExifDirectory();
                List<?> sourceDirectories = sourceSet.getDirectories();
                for (int i = 0; i < sourceDirectories.size(); i++) {
                    TiffOutputDirectory sourceDirectory = (TiffOutputDirectory) sourceDirectories.get(i);
                    TiffOutputDirectory destinationDirectory = getOrCreateExifDirectory(destSet, sourceDirectory);
                    if (destinationDirectory != null) {
                        List<?> sourceFields = sourceDirectory.getFields();
                        for (int j = 0; j < sourceFields.size(); j++) {
                            TiffOutputField sourceField = (TiffOutputField) sourceFields.get(j);
                            if (excludedFields != null) {
                                if (excludedFields.contains(sourceField.tagInfo)) {
                                    destinationDirectory.removeField(sourceField.tagInfo);
                                }
                            }
                            destinationDirectory.removeField(sourceField.tagInfo);
                            destinationDirectory.add(sourceField);
                        }
                    }
                }
                destSet.setGPSInDegrees(longitude, latitude);
                OutputStream tempStream2 = new BufferedOutputStream(new FileOutputStream(tempFile2));
                try {
                    new ExifRewriter().updateExifMetadataLossless(destFile, tempStream2, destSet);
                    tempStream2.close();
                    if (destFile.delete()) {
                        tempFile2.renameTo(destFile);
                    }
                    if (tempStream2 != null) {
                        try {
                            tempStream2.close();
                        } catch (IOException e2) {
                        }
                    }
                    if (tempFile2 != null && tempFile2.exists()) {
                        tempFile2.delete();
                    }
                    tempStream = tempStream2;
                    tempFile = tempFile2;
                    return true;
                } catch (ImageReadException e3) {
                    exception = e3;
                    tempStream = tempStream2;
                    tempFile = tempFile2;
                    try {
                        exception.printStackTrace();
                        if (tempStream != null) {
                            try {
                                tempStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        tempFile.delete();
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        if (tempStream != null) {
                            try {
                                tempStream.close();
                            } catch (IOException e5) {
                            }
                        }
                        tempFile.delete();
                        throw th;
                    }
                } catch (ImageWriteException e6) {
                    exception2 = e6;
                    tempStream = tempStream2;
                    tempFile = tempFile2;
                    exception2.printStackTrace();
                    if (tempStream != null) {
                        try {
                            tempStream.close();
                        } catch (IOException e7) {
                        }
                    }
                    tempFile.delete();
                    return false;
                } catch (IOException e8) {
                    exception3 = e8;
                    tempStream = tempStream2;
                    tempFile = tempFile2;
                    exception3.printStackTrace();
                    if (tempStream != null) {
                        try {
                            tempStream.close();
                        } catch (IOException e9) {
                        }
                    }
                    tempFile.delete();
                    return false;
                } catch (Throwable th3) {
                    th = th3;
                    tempStream = tempStream2;
                    tempFile = tempFile2;
                    if (tempStream != null) {
                        tempStream.close();
                    }
                    tempFile.delete();
                    throw th;
                }
            } catch (ImageReadException e10) {
                exception = e10;
                tempFile = tempFile2;
                exception.printStackTrace();
                if (tempStream != null) {
                    tempStream.close();
                }
                tempFile.delete();
                return false;
            } catch (ImageWriteException e11) {
                exception2 = e11;
                tempFile = tempFile2;
                exception2.printStackTrace();
                if (tempStream != null) {
                    tempStream.close();
                }
                tempFile.delete();
                return false;
            } catch (IOException e12) {
                exception3 = e12;
                tempFile = tempFile2;
                exception3.printStackTrace();
                if (tempStream != null) {
                    tempStream.close();
                }
                tempFile.delete();
                return false;
            } catch (Throwable th4) {
                th = th4;
                tempFile = tempFile2;
                if (tempStream != null) {
                    tempStream.close();
                }
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
                try {
                    throw th;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        } catch (IOException e15) {
            exception3 = e15;
            exception3.printStackTrace();
            if (tempStream != null) {
                try {
                    tempStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return false;
        }
        return false;
    }

    private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, int defaultByteOrder) throws IOException, ImageReadException, ImageWriteException {
        TiffImageMetadata exif = null;
        TiffOutputSet outputSet = null;
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) Sanselan.getMetadata(jpegImageFile);
        if (jpegMetadata != null) {
            exif = jpegMetadata.getExif();
            if (exif != null) {
                outputSet = exif.getOutputSet();
            }
        }
        if (outputSet == null) {
            if (exif != null) {
                defaultByteOrder = exif.contents.header.byteOrder;
            }
            outputSet = new TiffOutputSet(defaultByteOrder);
        }
        return outputSet;
    }

    private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet, TiffOutputDirectory outputDirectory) {
        TiffOutputDirectory result = outputSet.findDirectory(outputDirectory.type);
        if (result != null) {
            return result;
        }
        result = new TiffOutputDirectory(outputDirectory.type);
        try {
            outputSet.addDirectory(result);
            return result;
        } catch (ImageWriteException e) {
            return null;
        }
    }

    public static int getImageOrientation(String path) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exif.getAttributeInt("Orientation", 0);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case Videoio.VIDEOWRITER_PROP_FRAMEBYTES /*2*/:
                matrix.setScale(-1.0f, 1.0f);
                break;
            case Videoio.CV_CAP_PROP_FRAME_WIDTH /*3*/:
                matrix.setRotate(180.0f);
                break;
            case Videoio.CV_CAP_PROP_FRAME_HEIGHT /*4*/:
                matrix.setRotate(180.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case Videoio.CAP_PVAPI_PIXELFORMAT_RGB24 /*5*/:
                matrix.setRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case Videoio.CAP_PVAPI_PIXELFORMAT_BGR24 /*6*/:
                matrix.setRotate(90.0f);
                break;
            case Videoio.CAP_PVAPI_PIXELFORMAT_RGBA32 /*7*/:
                matrix.setRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case Videoio.CAP_PVAPI_PIXELFORMAT_BGRA32 /*8*/:
                matrix.setRotate(-90.0f);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isImage(String fileName) {
        for (String extension : new String[]{".jpg", ".png", ".gif", ".jpeg"}) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static int getEntrySize(Context context, String fileName) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(context.getAssets().open(fileName)));
        int entryCount = 0;
        while (zis.getNextEntry() != null) {
            try {
                entryCount++;
            } finally {
                zis.close();
            }
        }
        return entryCount;
    }

    public static Bitmap captureView(View view) {
        Bitmap image = Bitmap.createBitmap(Videoio.CAP_QT, Videoio.CV_CAP_ANDROID, Config.ARGB_8888);
        view.draw(new Canvas(image));
        return image;
    }

    public static Bitmap convertToBitmap(View layout) {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        return layout.getDrawingCache();
    }

    public static Bitmap getMap(double lat, double lon, int zoom, int width, int height, float rotate, String key) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format("http://maps.google.com/maps/api/staticmap?markers=%f,%f&zoom=%d&size=%dx%d&scale=2&key=%s", new Object[]{Double.valueOf(lat), Double.valueOf(lon), Integer.valueOf(zoom), Integer.valueOf(width), Integer.valueOf(height), key})).openConnection();
        connection.setDoInput(true);
        connection.connect();
        return BitmapFactory.decodeStream(connection.getInputStream());
    }

    public Bitmap BITMAP_RESIZER(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);
        float ratioX = ((float) newWidth) / ((float) bitmap.getWidth());
        float ratioY = ((float) newHeight) / ((float) bitmap.getHeight());
        float middleX = ((float) newWidth) / 2.0f;
        float middleY = ((float) newHeight) / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - ((float) (bitmap.getWidth() / 2)), middleY - ((float) (bitmap.getHeight() / 2)), new Paint(2));
        return scaledBitmap;
    }

    public static Bitmap resizeBitMapImage1(String filePath, int targetWidth, int targetHeight) {
        Bitmap bitMapImage = null;
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            double sampleSize = 0.0d;
            Boolean scaleByHeight = Boolean.valueOf(Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth - targetWidth));
            if ((options.outHeight * options.outWidth) * 2 >= 1638) {
                sampleSize = (double) ((int) Math.pow(2.0d, Math.floor(Math.log(scaleByHeight.booleanValue() ? (double) (options.outHeight / targetHeight) : (double) (options.outWidth / targetWidth)) / Math.log(2.0d))));
            }
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[Imgproc.COLOR_BGR2YUV_IYUV];
            while (true) {
                try {
                    options.inSampleSize = (int) sampleSize;
                    bitMapImage = BitmapFactory.decodeFile(filePath, options);
                    break;
                } catch (Exception e) {
                    sampleSize *= 2.0d;
                }
            }
        } catch (Exception e2) {
        }
        return bitMapImage;
    }

    public static Bitmap resize(String filePath) {
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, out);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static String convertToDegree(double number) {
        String firstNumber="100";
        String secondNumber="400";
        double secondTemp = (number - ((double) getDoubleBeforePoint(number))) * 60.0d;
        int thirdNumber = getDoubleBeforePoint((secondTemp - ((double) getDoubleBeforePoint(secondTemp))) * 60.0d);
        return String.format("%d.%d.%d", new Object[]{Integer.valueOf(firstNumber), Integer.valueOf(secondNumber), Integer.valueOf(thirdNumber)});
    }

    public static int getDoubleBeforePoint(double number) {
        return Integer.parseInt(String.valueOf(number).split("\\.")[0]);
    }

    public static Bitmap drawTextToBitmap(String mText, int width) {
        int paddedWidth = Math.round(((float) width) * 0.99f);
        try {
            Paint paint = new Paint(1);
            paint.setColor(Color.rgb(LoaderCallbackInterface.INIT_FAILED, LoaderCallbackInterface.INIT_FAILED, 0));
            paint.setTextAlign(Align.LEFT);
            float textSize = 57.0f;
            Rect bounds = new Rect();
            paint.setTextSize(57.0f);
            paint.getTextBounds(mText, 0, mText.length(), bounds);
            if (bounds.width() > paddedWidth) {
                do {
                    textSize -= 1.0f;
                    paint.setTextSize(textSize);
                    paint.getTextBounds(mText, 0, mText.length(), bounds);
                } while (bounds.width() > paddedWidth);
            } else if (bounds.width() < paddedWidth) {
                do {
                    textSize += 1.0f;
                    paint.setTextSize(textSize);
                    paint.getTextBounds(mText, 0, mText.length(), bounds);
                } while (bounds.width() < paddedWidth);
                paint.setTextSize(textSize - 1.0f);
                paint.getTextBounds(mText, 0, mText.length(), bounds);
            }
            Bitmap bitmap = Bitmap.createBitmap(width, Math.round(((float) bounds.height()) * 1.15f), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(mText, (float) 0, (float) ((int) (((float) (canvas.getHeight() / 2)) - ((paint.descent() + paint.ascent()) / 2.0f))), paint);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap drawTextToBitmap(String text, float size, int width) {
        try {
            text = text.trim();
            if (text == null || text.isEmpty()) {
                return Bitmap.createBitmap(10, 10, Config.ARGB_8888);
            }
            Paint paint = new Paint(1);
            paint.setColor(Color.rgb(LoaderCallbackInterface.INIT_FAILED, LoaderCallbackInterface.INIT_FAILED, 0));
            paint.setTextAlign(Align.LEFT);
            float textSize = size;
            Rect bounds = new Rect();
            paint.setTextSize(textSize);
            paint.getTextBounds(text, 0, text.length(), bounds);
            while (bounds.width() > width) {
                textSize -= 1.0f;
                paint.setTextSize(textSize);
                paint.getTextBounds(text, 0, text.length(), bounds);
            }
            Bitmap bitmap = Bitmap.createBitmap(width, Math.round(((float) bounds.height()) * 1.5f), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(text, (float) ((canvas.getWidth() - bounds.width()) / 2), (float) ((int) (((float) (canvas.getHeight() / 2)) - ((paint.descent() + paint.ascent()) / 2.0f))), paint);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static void showToast(final Context context, final CharSequence text, final int duration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(context.getApplicationContext(), text, duration).show();
            }
        });
    }

    public static String getImageTime(String filePath) {
        try {
            String time = new ExifInterface(filePath).getAttribute("DateTime");
            return new SimpleDateFormat("HH:mm:ss").format(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(time));
        } catch (Exception e) {
            return "00:00:00";
        }
    }

    public static String getImageDate(String filePath) {
        try {
            String date = new ExifInterface(filePath).getAttribute("DateTime");
            return new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(date));
        } catch (Exception e) {
            return "00-00-00";
        }
    }

    public static Bitmap getLargeBitmapFromFile(String newestImagePath, int width, int height) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(newestImagePath, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(newestImagePath, options);
    }

    public static Bitmap getLargeBitmapFromFile(Context context, int imageId, int width, int height) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), imageId, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), imageId, options);
    }

    private static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @SuppressLint("StaticFieldLeak")
    public static void setImage(final ImageView imageview, final String newestImagePath, String defaultImagePath) {
        if (new File(newestImagePath).exists()) {
            new AsyncTask<Void, Void, Bitmap>() {
                int height;
                int width;

                protected void onPreExecute() {
                    super.onPreExecute();
                    this.width = imageview.getMeasuredWidth();
                    this.height = imageview.getMeasuredHeight();
                }

                protected Bitmap doInBackground(Void... params) {
                    return MyUtils.getLargeBitmapFromFile(newestImagePath, this.width, this.height);
                }

                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    imageview.setImageBitmap(bitmap);
                }
            }.execute(new Void[0]);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public static void setImage(final ImageView imageview, final Context context, final int imageId, String defaultImagePath) {
        new AsyncTask<Void, Void, Bitmap>() {
            int height;
            int width;

            protected void onPreExecute() {
                super.onPreExecute();
                this.width = imageview.getMeasuredWidth();
                this.height = imageview.getMeasuredHeight();
            }

            protected Bitmap doInBackground(Void... params) {
                return MyUtils.getLargeBitmapFromFile(context, imageId, this.width, this.height);
            }

            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                imageview.setImageBitmap(bitmap);
            }
        }.execute(new Void[0]);
    }
}
