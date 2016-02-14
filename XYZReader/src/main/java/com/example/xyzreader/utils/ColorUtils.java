package com.example.xyzreader.utils;

import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

/**
 * Created by alexanderkulikovskiy on 15.02.16.
 */
public class ColorUtils {
    public static @Nullable
    Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }
}
