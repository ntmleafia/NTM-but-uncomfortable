package com.leafia.contents.gear.utility;

import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemForgeFluidIdentifier;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.conductor.TileEntityFFDuctBaseMk2;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemFuzzyIdentifier extends ItemForgeFluidIdentifier {
	public static final ModelResourceLocation fuzzyModel = new ModelResourceLocation(RefStrings.MODID + ":fuzzy_identifier", "inventory");
	public ItemFuzzyIdentifier(String s) {
		super(s);
	}
	@Override
	public void getSubItems(CreativeTabs tab,NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			items.add(new ItemStack(this));
		}
	}
	public static ItemStack getStackFromFluid(Fluid f){
		ItemStack stack = new ItemStack(ModItems.fuzzy_identifier, 1, 0);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("fluidtype", f.getName());
		stack.setTagCompound(tag);
		return stack;
	}
	public void addInformation(ItemStack stack,World worldIn,List<String> list,ITooltipFlag flagIn) {
		if (!(stack.getItem() instanceof ItemForgeFluidIdentifier))
			return;
		Fluid f = null;
		if (stack.hasTagCompound()) {
			f = FluidRegistry.getFluid(stack.getTagCompound().getString("fluidtype"));
		}
		list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.leafia.fuzzy.howto"));
		list.add("");
		if (f != null) {
			list.add(I18nUtil.resolveKey("desc.leafia.fuzzy.set"));
			list.add("   " + f.getLocalizedName(new FluidStack(f,1000)));
		} else
			list.add(TextFormatting.RED+I18nUtil.resolveKey("desc.leafia.fuzzy.unset"));
	}
	@Override
	public EnumActionResult onItemUse(EntityPlayer player,World worldIn,BlockPos pos,EnumHand hand,EnumFacing facing,float hitX,float hitY,float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		TileEntityFFDuctBaseMk2 duct = null;
		if(te != null && te instanceof TileEntityFFDuctBaseMk2){
			duct = (TileEntityFFDuctBaseMk2) te;
		}
		if(duct != null){
			if(player.isSneaking()){
				if(null != duct.getType()){
					if (!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemFuzzyIdentifier) {
						ItemStack stack = player.getHeldItem(hand);
						NBTTagCompound nbt = stack.getTagCompound();
						if (nbt == null) nbt = new NBTTagCompound();
						nbt.setString("fluidtype",duct.getType().getName());
						stack.setTagCompound(nbt);
						if (!worldIn.isRemote)
							worldIn.playSound(null,player.getPosition(),HBMSoundHandler.techBleep,SoundCategory.PLAYERS,1,1);
						else
							Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("item.fuzzy_identifier.message",duct.getType().getLocalizedName(new FluidStack(duct.getType(),1000))).setStyle(new Style().setColor(TextFormatting.YELLOW)));
					}
				}
			}else{
				if(getType(player.getHeldItem(hand)) != duct.getType()){
					spreadType(worldIn, pos, getType(player.getHeldItem(hand)), duct.getType(), 256);
				}
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}
	@Override
	public ModelResourceLocation getResourceLocation() {
		return fuzzyModel;
	}
}
