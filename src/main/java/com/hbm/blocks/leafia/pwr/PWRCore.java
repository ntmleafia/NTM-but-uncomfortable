package com.hbm.blocks.leafia.pwr;

import com.hbm.tileentity.leafia.pwr.PWRBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PWRCore {
    boolean tileEntityShouldCreate(World world,BlockPos pos);
    default PWRBase getPWR(World world,BlockPos pos) {
        if (!tileEntityShouldCreate(world,pos)) return null;
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null) {
            if (entity instanceof PWRBase) {
                return (PWRBase)entity;
            }
        }
        return null;
    };
    default TileEntity getEntity(World world,BlockPos pos) {
        if (!tileEntityShouldCreate(world,pos)) return null;
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null) {
            if (entity instanceof PWRBase) {
                return entity;
            }
        }
        return null;
    };
    default void beginDiagnosis(World world,BlockPos pos) {
        if (world.isRemote) return;
        PWRDiagnosis.cleanup();
        PWRDiagnosis diagnosis = new PWRDiagnosis(world);
        diagnosis.addPosition(pos);
    }
}
