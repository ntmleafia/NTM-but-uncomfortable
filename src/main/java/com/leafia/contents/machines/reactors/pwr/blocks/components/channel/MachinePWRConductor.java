package com.leafia.contents.machines.reactors.pwr.blocks.components.channel;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import com.leafia.contents.machines.reactors.pwr.blocks.components.PWRComponentBlock;
import com.leafia.dev.MachineTooltip;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class MachinePWRConductor extends BlockBase implements ITooltipProvider, PWRComponentBlock {
    public MachinePWRConductor() {
        super(Material.IRON,"reactor_conductor");
        this.setUnlocalizedName("pwr_conductor");
        this.setSoundType(ModBlocks.PWR.soundTypePWRTube);
    }
    @Override
    public boolean shouldRenderOnGUI() {
        return true;
    }
    @Override
    public void addInformation(ItemStack stack,@Nullable World player,List<String> tooltip,ITooltipFlag advanced) {
        MachineTooltip.addMultiblock(tooltip);
        MachineTooltip.addModular(tooltip);
        MachineTooltip.addBoiler(tooltip);
        addStandardInfo(tooltip);
        MachineTooltip.addUpdate(tooltip,"tile.reactor_conductor.name","tile.reactor_computer.name");
        super.addInformation(stack,player,tooltip,advanced);
    }

    @Override
    public boolean tileEntityShouldCreate(World world,BlockPos pos) {
        return false;
    }
}
