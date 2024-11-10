package com.hbm.blocks.leafia.pwr;

import com.hbm.tileentity.leafia.pwr.PWRData;
import net.minecraft.tileentity.TileEntity;

public interface PWRCore {
    PWRData getCore();
    TileEntity getEntity();
}
