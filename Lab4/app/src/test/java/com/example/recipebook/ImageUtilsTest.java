package com.example.recipebook;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ImageUtilsTest {
    @Test
    public void sampleSizeIsOneForSmallImage() {
        assertEquals(1, ImageUtils.calculateInSampleSize(800, 600, 1024, 1024));
    }

    @Test
    public void sampleSizeReducesLargeImageBeforeDecode() {
        assertEquals(4, ImageUtils.calculateInSampleSize(4032, 3024, 1024, 1024));
    }

    @Test
    public void sampleSizeHandlesTallImage() {
        assertEquals(4, ImageUtils.calculateInSampleSize(1200, 4000, 1024, 1024));
    }
}
