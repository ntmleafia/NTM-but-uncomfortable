package com.llib.math;

public class MathLeafia {
    public static short getTime32s() {
        return (short)(Math.floorMod(System.currentTimeMillis(),32_767));
    }
    public static short getTimeDifference32s(int t1,int t2) {
        return (short)(Math.floorMod(t2-t1,32_767));
    }
}
