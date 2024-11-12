package com.hbm.tileentity.leafia.pwr;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface PWRBase {
    void setCore(@Nullable BlockPos pos);
    void setData(@Nullable PWRData data);
    PWRData getData();
}