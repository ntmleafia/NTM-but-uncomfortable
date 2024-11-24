package com.llib.exceptions;

public class LeafiaDevFlaw extends RuntimeException {
    public LeafiaDevFlaw(String s) {
        super("\uE05E\u8AFA"+s);
    }
}
