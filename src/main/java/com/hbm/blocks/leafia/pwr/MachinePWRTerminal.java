package com.hbm.blocks.leafia.pwr;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.leafia.MachineTooltip;
import com.hbm.blocks.machine.BlockMachineBase;
import com.hbm.tileentity.leafia.pwr.TileEntityPWRElement;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.List;

public class MachinePWRTerminal extends BlockMachineBase implements ITooltipProvider {
    public MachinePWRTerminal() {
        super(Material.IRON,-1,"reactor_hatch");
        this.setUnlocalizedName("pwr_terminal");
    }
    @Override
    public void addInformation(ItemStack stack,@Nullable World player,List<String> tooltip,ITooltipFlag advanced) {
        MachineTooltip.addMultiblock(tooltip);
        MachineTooltip.addModular(tooltip);
        addStandardInfo(tooltip);
        super.addInformation(stack,player,tooltip,advanced);
    }
    @Override
    public TileEntity createNewTileEntity(World worldIn,int meta) {
        return null;
    }

    @Override
    protected boolean rotatable() {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL; // grrrrwl
    }
}
