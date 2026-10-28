package com.example.recipebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.UUID;

public final class ImageUtils {
    public static final int MAX_DIMENSION = 1024;
    public static final int JPEG_QUALITY = 80;

    private ImageUtils() { }

    public static String compressAndStore(Context context, Uri sourceUri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = context.getContentResolver().openInputStream(sourceUri)) {
            if (input == null) throw new IOException("Не удалось открыть выбранное изображение");
            BitmapFactory.decodeStream(input, null, bounds);
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("Выбранный файл не является поддерживаемым изображением");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight,
                MAX_DIMENSION, MAX_DIMENSION);
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap decoded;
        try (InputStream input = context.getContentResolver().openInputStream(sourceUri)) {
            if (input == null) throw new IOException("Не удалось повторно открыть изображение");
            decoded = BitmapFactory.decodeStream(input, null, options);
        }
        if (decoded == null) throw new IOException("Не удалось декодировать изображение");

        Bitmap scaled = decoded;
        int longestSide = Math.max(decoded.getWidth(), decoded.getHeight());
        if (longestSide > MAX_DIMENSION) {
            float factor = (float) MAX_DIMENSION / longestSide;
            int targetWidth = Math.max(1, Math.round(decoded.getWidth() * factor));
            int targetHeight = Math.max(1, Math.round(decoded.getHeight() * factor));
            scaled = Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true);
            if (scaled != decoded) decoded.recycle();
        }

        File imageDir = new File(context.getFilesDir(), "images");
        if (!imageDir.exists() && !imageDir.mkdirs()) {
            if (scaled != null && !scaled.isRecycled()) scaled.recycle();
            throw new IOException("Не удалось создать папку изображений");
        }
        File output = new File(imageDir, "recipe_" + UUID.randomUUID() + ".jpg");
        try (FileOutputStream stream = new FileOutputStream(output)) {
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                throw new IOException("Не удалось сохранить сжатое изображение");
            }
            stream.flush();
        } catch (IOException ex) {
            output.delete();
            throw ex;
        } finally {
            if (scaled != null && !scaled.isRecycled()) scaled.recycle();
        }
        return output.getAbsolutePath();
    }

    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int sample = 1;
        if (width > reqWidth || height > reqHeight) {
            int limit = Math.max(reqWidth, reqHeight);
            while (Math.max(width / sample, height / sample) > limit) {
                sample *= 2;
            }
        }
        return Math.max(1, sample);
    }

    public static void deleteStoredImage(String path) {
        if (path == null || path.trim().isEmpty()) return;
        File file = new File(path);
        if (file.isFile()) {
            file.delete();
        }
    }
}
