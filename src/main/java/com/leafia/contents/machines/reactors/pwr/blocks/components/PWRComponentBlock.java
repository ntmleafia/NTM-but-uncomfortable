package com.leafia.contents.machines.reactors.pwr.blocks.components;

import com.leafia.contents.machines.reactors.pwr.PWRDiagnosis;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PWRComponentBlock {
    boolean tileEntityShouldCreate(World world,BlockPos pos);
    default PWRComponentEntity getPWR(World world,BlockPos pos) {
        if (!tileEntityShouldCreate(world,pos)) return null;
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null) {
            if (entity instanceof PWRComponentEntity) {
                return (PWRComponentEntity)entity;
            }
        }
        return null;
    };
    default TileEntity getEntity(World world,BlockPos pos) {
        if (!tileEntityShouldCreate(world,pos)) return null;
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null) {
            if (entity instanceof PWRComponentEntity) {
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
