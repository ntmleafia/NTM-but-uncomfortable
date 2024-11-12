package com.hbm.blocks.leafia.pwr;

import com.hbm.inventory.leafia.inventoryutils.LeafiaPacket;
import com.hbm.items.ModItems;
import com.llib.exceptions.messages.TextWarningLeafia;
import com.llib.math.MathLeafia;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.tileentity.leafia.pwr.PWRBase;
import com.hbm.tileentity.leafia.pwr.PWRData;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class PWRDiagnosis {
	public static final Set<PWRDiagnosis> ongoing = new HashSet<>();
	private static boolean cleanupInProgress = false; // idk if this is necessary but I hate crashes so much so have this anyway
	public static void cleanup() {
		if (cleanupInProgress) return;
		cleanupInProgress = true;
		Set<PWRDiagnosis> removalQueue = new HashSet<>();
		for (PWRDiagnosis task : ongoing) {
			if (task.checkTimeElapsed() >= 10_000 || task.closure)
				removalQueue.add(task);
		}
		for (PWRDiagnosis diagnosis : removalQueue) {
			diagnosis.destroy();
		}
		cleanupInProgress = false;
	}
	public double cr;
	public double cg;
	public double cb;

	public int lastConfirmed;
	public final Set<BlockPos> activePos = new HashSet<>();
	public final Set<BlockPos> blockPos = new HashSet<>();
	public final Set<BlockPos> corePos = new HashSet<>();
	public final Set<BlockPos> potentialPos = new HashSet<>();
	public final Set<BlockPos> fuelPositions = new HashSet<>();
	boolean closure = false;
	World world = null;
	/*
	Creates PWRDiagnosis instance, and automatically adds to ongoing Set
	 */
	public PWRDiagnosis(World world) {
		confirmLife();
		this.world = world;
		ongoing.add(this);
		cr = world.rand.nextDouble();
		cg = world.rand.nextDouble();
		cb = world.rand.nextDouble();
	}
	PWRCore getPWRBlock(BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof PWRCore)
			return (PWRCore)block;
		return null;
	}
	PWRBase getPWREntity(BlockPos pos) {
		PWRCore pwr = getPWRBlock(pos);
		if (pwr != null)
			return pwr.getPWR(world,pos);
		return null;
	}
	void debugSpawnParticle(BlockPos pos) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "vanillaExt");
		data.setString("mode", "reddust");
		data.setDouble("mX", cr);
		data.setDouble("mY", cg);
		data.setDouble("mZ", cb);
		for (EntityPlayer player : world.playerEntities) {
			if (player.getHeldItem(EnumHand.OFF_HAND).getItem() == ModItems.wand_d)
				LeafiaPacket._sendToClient(new AuxParticlePacketNT(data,pos.getX()+0.5,pos.getY()+0.1,pos.getZ()+0.5),player);
		}
	}
	public void explorePosition(BlockPos pos) {
		if (closure) return;
		activePos.add(pos);
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos neighbor = pos.add(facing.getFrontOffsetX(),facing.getFrontOffsetY(),facing.getFrontOffsetZ());
			if (world.getBlockState(neighbor).getBlock() instanceof PWRCore) {
				addPosition(neighbor);
			}
		}
		activePos.remove(pos);
		//for (EntityPlayer player : world.playerEntities) {
		//	player.sendMessage(new TextComponentString(""+activePos.size()));
		//}
		if (activePos.size() <= 0) {
			close();
		}
	}
	public void addPosition(BlockPos pos) {
		if (closure) return;
		if (!blockPos.contains(pos)) {
			blockPos.add(pos);
			confirmLife();
			PWRCore pwr = getPWRBlock(pos);
			debugSpawnParticle(pos.up());
			if (pwr != null) {
				// if this block isn't under control of topping blocks,
				if (pwr.tileEntityShouldCreate(world,pos)) {
					// then allow it to be assigned as core
					TileEntity entity = world.getTileEntity(pos);
					if (entity != null) {
						if (entity instanceof ITickable) // only assign tickable entities as a core
							potentialPos.add(pos);
					}
					if (pwr instanceof MachinePWRElement)
						fuelPositions.add(pos);
				}
			}
			PWRBase entity = getPWREntity(pos);
			if (entity != null) {
				// if this block is the core of the affected assembly..
				if (entity.getData() != null) {
					// check if it's in right place
					if (potentialPos.contains(pos))
						corePos.add(pos); // mark it so one of valid cores are chosen when joining assemblies
					else
						entity.setData(null); // destroy it if it's on the wrong place
					debugSpawnParticle(pos.up(2));
				}
			}
			try {
				explorePosition(pos);
			} catch (StackOverflowError error) {
				closure = true;
				for (EntityPlayer player : world.playerEntities) {
					if (player.getHeldItem(EnumHand.OFF_HAND).getItem() == ModItems.wand_d)
						player.sendMessage(new TextWarningLeafia("STACK OVERFLOW! PWR too gigantic!"));
				}
			}
		}
	}
	void close() {
		if (closure) return;
		closure = true;
		BlockPos outCorePos = null;
		if (corePos.size() >= 2) { // if theres multiple cores
			// pick random...
			Object[] array = corePos.toArray();
			outCorePos = (BlockPos)(array[world.rand.nextInt(array.length)]);
			// and then remove the rest
			for (BlockPos pos : corePos) {
				if (!pos.equals(outCorePos)) {
					PWRBase entity = getPWREntity(pos);
					if (entity != null)
						entity.setData(null);
				}
			}
		} else if (corePos.size() <= 0) { // if theres not a single available core
			// pick random member...
			Object[] array = potentialPos.toArray();
			if (array.length > 0) {
				// and book it as the new core
				outCorePos = (BlockPos)(array[world.rand.nextInt(array.length)]);
			}
		} else // if theres exactly one, keep it
			outCorePos = (BlockPos)corePos.toArray()[0];

		// iterate over all members (blocks)
		for (BlockPos pos : blockPos) {
			boolean shouldHaveCoreCoords = false;
			PWRCore pwr = getPWRBlock(pos);
			// check if it should have a tile entity
			if (pwr != null) {
				shouldHaveCoreCoords = pwr.tileEntityShouldCreate(world,pos);
			}
			PWRBase entity = getPWREntity(pos);
			if (entity != null) // give coordinates of the core for each valid blocks
				entity.setCore(shouldHaveCoreCoords ? outCorePos : null);
		}
		// if position for core is booked,
		if (outCorePos != null) {
			debugSpawnParticle(outCorePos.up(3));
			PWRBase entity = getPWREntity(outCorePos);
			if (entity != null) {
				if (entity.getData() == null) { // and if the target block doesn't have a core yet
					entity.setData(new PWRData((TileEntity)entity)); // assign it
				}
				entity.getData().members = blockPos;
			}
		}
		for (EntityPlayer player : world.playerEntities) {
			if (player.getHeldItem(EnumHand.OFF_HAND).getItem() == ModItems.wand_d)
				player.sendMessage(new TextComponentString("PWR Diagnosis complete, core position "+((outCorePos == null) ? "removed" : "set")));
		}
	}
	public void confirmLife() {
		lastConfirmed = MathLeafia.getTime32s();
	}
	public int checkTimeElapsed() {
		return MathLeafia.getTimeDifference32s(MathLeafia.getTime32s(),lastConfirmed);
	}
	public void destroy() {
		if (ongoing.contains(this))
			ongoing.remove(this);
		if (world == null) return;
		if (world.playerEntities == null) return;
		for (EntityPlayer player : world.playerEntities) {
			if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == ModItems.wand_d)
				player.sendMessage(new TextComponentString("PWR Diagnosis instance removed"));
		}
	}
}
