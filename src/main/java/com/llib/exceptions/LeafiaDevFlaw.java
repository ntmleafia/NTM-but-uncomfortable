package com.llib.exceptions;

public class LeafiaDevFlaw extends RuntimeException {
    public LeafiaDevFlaw(String s) {
        super("🌿"+s);
    }
}
