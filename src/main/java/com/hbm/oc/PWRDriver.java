package com.hbm.oc;

import com.leafia.contents.machines.reactors.pwr.PWRData;
import com.leafia.contents.machines.reactors.pwr.blocks.components.control.TileEntityPWRControl;
import com.leafia.contents.machines.reactors.pwr.blocks.components.element.TileEntityPWRElement;
import com.leafia.contents.machines.reactors.pwr.blocks.components.terminal.TileEntityPWRTerminal;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class PWRDriver extends DriverSidedTileEntity {
    @Override
    public Class<?> getTileEntityClass() {
        return TileEntityPWRTerminal.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos blockPos, EnumFacing enumFacing) {
        TileEntity entity = world.getTileEntity(blockPos);

        if (entity instanceof TileEntityPWRTerminal) {
            TileEntityPWRTerminal te = (TileEntityPWRTerminal)entity;
            return new PWREnvironment(world, te.getPos());
        }

        return null;
    }

    public static class PWREnvironment extends HBMDriver<TileEntityPWRTerminal> {

        public PWREnvironment(World w, BlockPos pos) {
            super(TileEntityPWRTerminal.class, w, pos);
        }

        @Override
        public String preferredName() {
            return "pwr";
        }

        @Override
        public int priority() {
            return 1000;
        }

        @Callback(doc = "function():table -- Get the control rods of the reactor")
        public Object[] getControlRods() {
            PWRData core = getTileEntity().getLinkedCore();
            if (core == null) return new Object[]{ null };
            HashMap<String, Object> result = new HashMap<String, Object>(core.controls.size() + 1)
            {{
                put("master", core.masterControl);
            }};
            for(BlockPos pos : core.controls)
            {
                TileEntity control = world.getTileEntity(pos);
                if(control instanceof TileEntityPWRControl)
                {
                    TileEntityPWRControl c = (TileEntityPWRControl) control;
                    result.put(c.name, new HashMap<String, Object>(){{
                        put("world_pos", c.getPos());
                        put("target_pos", c.targetPosition);
                        put("pos", c.position);
                        put("height", c.height);
                    }});
                }
            }
            return new Object[]{ result };
        }

        @Callback(doc = "function():table -- Get the fuel rods of the reactor")
        public Object[] getFuelRods()
        {
            PWRData core = getTileEntity().getLinkedCore();
            if (core == null) return new Object[]{ null };
            HashMap<Integer, Object> result = new HashMap<>(core.fuels.size());
            int i = 1;
            for(BlockPos pos : core.fuels)
            {
                TileEntity fuel = world.getTileEntity(pos);
                if(fuel instanceof TileEntityPWRElement)
                {
                    TileEntityPWRElement f = (TileEntityPWRElement) fuel;
                    result.put(i, new HashMap<String, Object>(){{
                        put("world_pos", f.getPos());
                        put("scale", f.channelScale);
                        put("exchange_scale", f.exchangerScale);
                    }});
                    i++;
                }
            }
            return new Object[]{ result };
        }
    }
}
