package com.llib.math;

import com.llib.math.base.RangeBase;

import java.util.Iterator;

public class RangeInt extends RangeBase implements Iterable<Integer> {
    int min;
    int max;
    public RangeInt(int min,int max) {
        this.min = Math.min(min,max);
        this.max = Math.max(min,max);
    }
    public boolean isInRange(int x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public boolean isInRange(long x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public boolean isInRange(float x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public boolean isInRange(double x) {
        if (x > max) return false;
        if (x < min) return false;
        return true;
    }
    public double lerp(double t) {
        return min + (max - min) * t;
    }
    public float lerp(float t) {
        return min + (max - min) * t;
    }

    @Override
    public double doubleMin() {
        return this.min;
    }
    @Override
    public double doubleMax() {
        return this.max;
    }
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int cursor = min;
            @Override
            public boolean hasNext() {
                return cursor <= max;
            }
            @Override
            public Integer next() {
                return cursor++;
            }
        };
    }
}
