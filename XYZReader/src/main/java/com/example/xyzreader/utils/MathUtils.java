package com.example.xyzreader.utils;

/**
 * Borrowed from github.com/romannurik/muzei
 *
 * Created by alexander on 15.02.16.
 */
public class MathUtils {
    public static float constrain(float min, float max, float v) {
        return Math.max(min, Math.min(max, v));
    }
}
