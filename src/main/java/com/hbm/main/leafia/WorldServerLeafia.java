package com.hbm.main.leafia;

import com.hbm.entity.effect.EntityNukeTorex;
import net.minecraft.entity.Entity;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

public class WorldServerLeafia {
    public static void saveAllChunks(WorldServer worldServer) throws MinecraftException {
        System.out.println("#Leaf: saveAllChunks called");
        if (worldServer.weatherEffects != null) {
            System.out.println("#Leaf: Iterating over weather effects");
            System.out.println("#Leaf: Length: "+worldServer.weatherEffects.size());
            for (Entity entity : worldServer.weatherEffects) {
                if (entity instanceof EntityNukeTorex) {
                    System.out.println("#Leaf: Found torex");
                    if (!((EntityNukeTorex) entity).calculationFinished)
                        throw new MinecraftException("Ongoing nuclear explosion detected, sabotaging world saving");
                }
            }
        }
    }
}
