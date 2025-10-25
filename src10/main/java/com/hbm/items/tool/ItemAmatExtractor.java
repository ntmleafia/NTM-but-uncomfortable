package com.hbm.items.tool;

import com.hbm.blocks.bomb.BlockCrashedBomb;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.items.special.ItemCell;
import com.hbm.items.special.ItemCustomLore;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAmatExtractor extends ItemCustomLore {

	public ItemAmatExtractor(String s){
		super(s);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        IBlockState state = world.getBlockState(pos);
        int type = state.getBlock().getMetaFromState(state);
        if(type==0 && state.getBlock() instanceof BlockCrashedBomb) {
			if(!world.isRemote && ItemCell.hasEmptyCell(player)) {
				
				float chance = world.rand.nextFloat();
				
				if(chance < 0.01) {
					((BlockCrashedBomb) world.getBlockState(pos).getBlock()).explode(world, pos);
				} else if(chance <= 0.3) {
					ItemCell.consumeEmptyCell(player);
	
					if(!player.inventory.addItemStackToInventory(ItemCell.getFullCell(ModForgeFluids.BALEFIRE))) {
						player.dropItem(ItemCell.getFullCell(ModForgeFluids.BALEFIRE), false);
					}
				} else {
					ItemCell.consumeEmptyCell(player);
	
					if(!player.inventory.addItemStackToInventory(ItemCell.getFullCell(ModForgeFluids.AMAT))) {
						player.dropItem(ItemCell.getFullCell(ModForgeFluids.AMAT), false);
					}
				}
				
				player.inventoryContainer.detectAndSendChanges();
				ContaminationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.CREATIVE, 50.0F);
			}
			
			player.swingArm(hand);
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
	}
}