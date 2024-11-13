package com.leafia.contents.machines.reactors.pwr.blocks.components;

import com.leafia.contents.machines.reactors.pwr.PWRData;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface PWRComponentEntity {
    void setCoreLink(@Nullable BlockPos pos);
    void assignCore(@Nullable PWRData data);
    PWRData getCore();
    default void onDiagnosis() {};
}