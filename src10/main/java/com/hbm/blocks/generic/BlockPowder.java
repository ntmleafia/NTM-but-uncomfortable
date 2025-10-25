package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.util.ContaminationUtil;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPowder extends Block {
	
	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 6);

	public BlockPowder(Material mat, SoundType soundType, String s) {
		super(mat);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setSoundType(soundType);
		this.setHarvestLevel("shovel", 0);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return new AxisAlignedBB(0, 0, 0, 1, 0.125, 1);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean canEntitySpawn(IBlockState state, Entity entityIn){
		return ContaminationUtil.isRadImmune(entityIn);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		if(this == ModBlocks.fallout){
			return ModItems.fallout;
		}
		return null;
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		IBlockState state = world.getBlockState(pos.down());
		Block block = state.getBlock();
		return block != Blocks.ICE && block != Blocks.PACKED_ICE && (block.isLeaves(state, world, pos.down()) || (block == this && (state.getValue(META) & 7) == 7 || state.isOpaqueCube() && state.getMaterial().blocksMovement()));
	}
	
	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity){
		if(!world.isRemote && entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).addPotionEffect(new PotionEffect(HbmPotion.radiation, 2 * 60 * 20, 14));
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos){
		if(!this.canPlaceBlockAt(world, pos)) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos){
		return true;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{META});
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta);
	}
}