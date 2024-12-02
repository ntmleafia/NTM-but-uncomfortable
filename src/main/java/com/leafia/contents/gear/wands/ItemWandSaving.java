package com.leafia.contents.gear.wands;

import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import static net.minecraft.util.EnumActionResult.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWandSaving extends Item {
	@SideOnly(Side.CLIENT) public boolean activated = false;
	public ItemWandSaving(String s) {
		this.setUnlocalizedName(s);
		this.setRegistryName(s);

		ModItems.ALL_ITEMS.add(this);
	}
	@Override
	public void addInformation(ItemStack stack,@Nullable World worldIn,List<String> tooltip,ITooltipFlag flagIn) {
		tooltip.add(I18nUtil.resolveKey("desc.creative"));
		tooltip.add(I18nUtil.resolveKey("desc.savingwand.1"));
		tooltip.add(I18nUtil.resolveKey("desc.savingwand.2"));
		tooltip.add(I18nUtil.resolveKey("desc.savingwand.3"));
		tooltip.add(I18nUtil.resolveKey("desc.savingwand.4"));
		tooltip.add(I18nUtil.resolveKey("desc.savingwand.5"));
	}
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player,World world,BlockPos pos,EnumFacing side,float hitX,float hitY,float hitZ,EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null) {
			if (nbt.hasKey("selectionX0") && nbt.hasKey("selectionX1")) {
				int x0 = nbt.getInteger("selectionX0");
				int y0 = nbt.getInteger("selectionY0");
				int z0 = nbt.getInteger("selectionZ0");
				int x1 = nbt.getInteger("selectionX1");
				int y1 = nbt.getInteger("selectionY1");
				int z1 = nbt.getInteger("selectionZ1");
				
				return SUCCESS;
			}
		}
		return PASS;
	}
}