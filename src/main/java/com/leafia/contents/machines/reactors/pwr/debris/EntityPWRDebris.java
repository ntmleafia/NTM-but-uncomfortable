package com.leafia.contents.machines.reactors.pwr.debris;

import com.hbm.blocks.machine.pile.BlockGraphite;
import com.hbm.blocks.machine.pile.BlockGraphiteDrilledBase;
import com.hbm.entity.projectile.EntityDebrisBase;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.saveddata.RadiationSavedData;
import com.leafia.contents.machines.reactors.pwr.blocks.components.channel.MachinePWRChannel;
import com.leafia.contents.machines.reactors.pwr.blocks.components.channel.MachinePWRConductor;
import com.leafia.contents.machines.reactors.pwr.blocks.components.control.MachinePWRControl;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static com.leafia.contents.machines.reactors.pwr.debris.EntityPWRDebris.DebrisType.*;

public class EntityPWRDebris extends EntityDebrisBase {
	public static final DataParameter<String> BLOCK_RSC = EntityDataManager.createKey(EntityPWRDebris.class, DataSerializers.STRING);
	public static final DataParameter<Integer> BLOCK_META = EntityDataManager.createKey(EntityPWRDebris.class, DataSerializers.VARINT);

	public EntityPWRDebris(World world){
		super(world);
	}

	public EntityPWRDebris(World world,double x,double y,double z,IBlockState state){
		super(world, x, y, z);
		DebrisType type = DebrisType.BLANK;
		Block block = state.getBlock();
		this.getDataManager().set(BLOCK_RSC,block.getRegistryName().toString());
		this.getDataManager().set(BLOCK_META,block.getMetaFromState(state));
		if (block instanceof MachinePWRControl)
			type = (world.rand.nextInt(3) == 0) ? CONTROL_FRAME : CONTROL_ROD;
		else if (block instanceof MachinePWRChannel || block instanceof MachinePWRConductor)
			type = (world.rand.nextInt(3) == 0) ? CHANNEL_3X : CHANNEL_1X;
		else if (block instanceof BlockGraphite || block instanceof BlockGraphiteDrilledBase)
			type = GRAPHITE;
		else if (state.getMaterial().equals(Material.IRON)) {
			if (world.rand.nextInt(3) == 0)
				type = BLANK;
			else {
				if (block.getSoundType().equals(SoundType.STONE))
					type = CONCRETE;
				else
					type = SHRAPNEL;
			}
		} else
			type = CONCRETE;
		this.setType(type);
	}

	@Override
	public void onUpdate() {
		if (!world.isRemote) {
			/*
			if (this.getType() == DebrisType.ELEMENT) {
				BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
				RadiationSavedData.incrementRad(world, pos, 20, 150);
			}*/
		}
		super.onUpdate();
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand){
		if(!world.isRemote && !isDead) {
			switch(this.getType()){
				case CONTROL_FRAME:
					if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal,3)))
						this.setDead();
					break;
				case CONTROL_ROD:
					if(player.inventory.addItemStackToInventory((world.rand.nextInt(20) == 0 ? new ItemStack(ModItems.nugget_pb209) : new ItemStack(ModItems.nugget_lead,4))))
						this.setDead();
					break;
				case GRAPHITE:
					if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_graphite)))
						this.setDead();
					break;
				case CHANNEL_1X:
					if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.plate_steel)))
						this.setDead();
				case CHANNEL_3X:
					if(player.inventory.addItemStackToInventory(new ItemStack(ModItems.hull_tube_steel,2)))
						this.setDead();
				default:
					Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.getDataManager().get(BLOCK_RSC)));
					if (block == null) break;
					ComparableStack stack = new ComparableStack(ItemBlock.getItemFromBlock(block));
					if (ShredderRecipes.shredderRecipes.containsKey(stack)) {
						ItemStack out = ShredderRecipes.shredderRecipes.get(stack);
						if (out != null) {
							if(player.inventory.addItemStackToInventory(out))
								this.setDead();
						}
					}
			}
			player.inventoryContainer.detectAndSendChanges();
		}

		return false;
	}
	@Override
	public void setSize() {
		switch(this.getType()){
			case BLANK: this.setSize(0.5F, 0.5F); break;
			case CONCRETE: this.setSize(0.75F, 0.5F); break;
			case GRAPHITE: this.setSize(0.25F, 0.25F); break;
			case SHRAPNEL: this.setSize(0.5F, 0.5F); break;

			default: this.setSize(1F, 1F);
		}
	}
	@Override
	public int getLifetime(){
		switch(this.getType()){
			default: return 10*60*20;
		}
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(BLOCK_RSC, "minecraft:tnt");
		this.getDataManager().register(BLOCK_META, 0);
	}
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.getDataManager().set(BLOCK_RSC, nbt.getString("block_rsc"));
		this.getDataManager().set(BLOCK_META, nbt.getInteger("block_meta"));
	}
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setString("block_rsc", this.getDataManager().get(BLOCK_RSC));
		nbt.setInteger("block_meta", this.getDataManager().get(BLOCK_META));
	}

	public void setType(DebrisType type){
		this.getDataManager().set(TYPE_ID, type.ordinal());
	}

	public DebrisType getType(){
		return DebrisType.values()[Math.abs(this.getDataManager().get(TYPE_ID)) % DebrisType.values().length];
	}

	public static enum DebrisType {
		BLANK,
		CONCRETE(4),
		SHRAPNEL,
		GRAPHITE,
		CONTROL_FRAME(2),
		CONTROL_ROD,
		CHANNEL_3X,
		CHANNEL_1X;
		final double weight;
		DebrisType() {
			this.weight = 1;
		}
		DebrisType(double weight) {
			this.weight = weight;
		}
	}
}