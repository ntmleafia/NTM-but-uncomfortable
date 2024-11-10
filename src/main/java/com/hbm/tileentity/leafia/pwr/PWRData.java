package com.hbm.tileentity.leafia.pwr;

import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.HeatRecipes;
import com.hbm.tileentity.machine.TileEntityMachineReactorLarge;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class PWRData implements ITickable, IFluidHandler, ITankPacketAcceptor {
    public BlockPos corePos;
    public FluidTank[] tanks;
    public Fluid[] tankTypes;
    public String coolantName = ModForgeFluids.coolant.getName();
    public int compression = 0;
    public double heat = 20;

    public PWRData() {
        tanks = new FluidTank[] {
                new FluidTank(128_000),
                new FluidTank(128_000),
                new FluidTank(16_000),

                new FluidTank(512_000),
                new FluidTank(256_000)
        };
        tankTypes = new Fluid[] {
                ModForgeFluids.coolant,
                ModForgeFluids.hotcoolant,
                ModForgeFluids.malcoolant,

                FluidRegistry.WATER,
                ModForgeFluids.steam
        };
    }
    public void readFromNBT(NBTTagCompound nbt) {
        nbt = nbt.getCompoundTag("data");
        if(nbt.hasKey("compression"))
            compression = nbt.getInteger("compression");
        if(compression == 0){
            tankTypes[4] = ModForgeFluids.steam;
        } else if(compression == 1){
            tankTypes[4] = ModForgeFluids.hotsteam;
        } else if(compression == 2){
            tankTypes[4] = ModForgeFluids.superhotsteam;
        }
        tankTypes[0] = ModForgeFluids.coolant;
        tankTypes[1] = ModForgeFluids.hotcoolant;
        tankTypes[2] = ModForgeFluids.malcoolant;
        if (nbt.hasKey("coolantName")) {
            Fluid coolant = FluidRegistry.getFluid(coolantName);
            if (coolant != null) {
                Fluid hot = HeatRecipes.getBoilFluid(coolant);
                if (hot != null) {
                    tankTypes[0] = coolant;
                    tankTypes[1] = hot;
                    Fluid hotter = hot;
                    while (true) {
                        Fluid hottest = HeatRecipes.getBoilFluid(hotter);
                        if (hottest == null) break;
                        else {
                            hotter = hottest;
                            if (hottest.isGaseous())
                                tankTypes[2] = hottest;
                        }
                    }
                }
            }
        }
        if (nbt.hasKey("heat"))
            heat = nbt.getDouble("heat");
        if(nbt.hasKey("tanks"))
            FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
    }
    public NBTTagCompound writeToNBT(NBTTagCompound mainCompound) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("heat", heat);
        nbt.setInteger("compression", compression);
        nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));

        mainCompound.setTag("data",nbt);
        return mainCompound;
    }

    @Override
    public void recievePacket(NBTTagCompound[] tags) {
        if(tags.length != 5){
            return;
        } else {
            for (int i = 0; i < 5; i++) {
                tanks[i].readFromNBT(tags[i]);
            }
        }
    }

    @Override
    public void update() {

    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0], tanks[2].getTankProperties()[0]};
    }
    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if(resource == null){
            return 0;
        } else if(resource.getFluid() == tankTypes[0]){
            return tanks[0].fill(resource, doFill);
        } else if(resource.getFluid() == tankTypes[1]){
            return tanks[1].fill(resource, doFill);
        } else {
            return 0;
        }
    }
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if(resource != null && resource.getFluid() == tankTypes[1]) {
            return tanks[1].drain(resource.amount,doDrain);
        } else if(resource != null && resource.getFluid() == tankTypes[4]){
            return tanks[4].drain(resource.amount, doDrain);
        } else {
            return null;
        }
    }
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return tanks[1].drain(maxDrain, doDrain);
    }
}
