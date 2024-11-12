package com.llib.math;

import com.llib.math.base.RangeBase;

public class RangeDouble extends RangeBase {
    double min;
    double max;
    public RangeDouble(double min,double max) {
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
    public float lerp(float t) { return (float)(min + (max - min) * t); } // not very preferable to use..

    @Override
    public double doubleMin() {
        return this.min;
    }
    @Override
    public double doubleMax() {
        return this.max;
    }
}
