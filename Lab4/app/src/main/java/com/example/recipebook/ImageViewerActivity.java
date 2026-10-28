package com.example.recipebook;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class ImageViewerActivity extends Activity {
    public static final String EXTRA_IMAGE_PATH = "image_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ZoomImageView imageView = findViewById(R.id.zoomImageView);
        String path = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (path == null || !new File(path).isFile()) {
            Toast.makeText(this, "Фотография не найдена", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        imageView.post(imageView::resetZoom);
    }
}
