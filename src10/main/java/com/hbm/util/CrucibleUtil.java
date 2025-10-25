package com.hbm.util;

import java.util.List;

import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.lib.ForgeDirection;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.block.Block;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CrucibleUtil {
	
	/**
	 * Standard pouring, casting a hitscan straight down at the given coordinates with the given range. Returns the leftover material, just like ICrucibleAcceptor's pour.
	 * The method directly modifies the original stack, so be careful and make a copy beforehand if you don't want that.
	 * Pass an empty Vec3 instance in order to get the impact position of the stream.
	 */
	public static MaterialStack pourSingleStack(World world, double x, double y, double z, double range, boolean safe, MaterialStack stack, int quanta, Vec3 impactPosHolder) {

		RayTraceResult[] mopHolder = new RayTraceResult[1];
		ICrucibleAcceptor acc = getPouringTarget(world, new Vec3d(x, y, z), new Vec3d(x, y - range, z), mopHolder);
		RayTraceResult mop = mopHolder[0];
		
		if(acc == null) {
			spill(mop, safe, stack, quanta, impactPosHolder);
			return stack;
		}
		
		MaterialStack ret = tryPourStack(world, acc, mop, stack, impactPosHolder);
		
		if(ret != null) {
			return ret;
		}

		spill(mop, safe, stack, quanta, impactPosHolder);
		return stack;
	}
	
	/**
	 * Standard pouring, casting a hitscan straight down at the given coordinates with the given range. Returns the materialStack that has been removed.
	 * The method doesn't make copies of the MaterialStacks in the list, so the materials being subtracted or outright removed will apply to the original list.
	 * Pass an empty Vec3 instance in order to get the impact position of the stream.
	 */
	public static MaterialStack pourFullStack(World world, double x, double y, double z, double range, boolean safe, List<MaterialStack> stacks, int quanta, Vec3 impactPosHolder) {
		
		if(stacks.isEmpty()) return null;
		
		RayTraceResult[] mopHolder = new RayTraceResult[1];
		ICrucibleAcceptor acc = getPouringTarget(world, new Vec3d(x, y, z), new Vec3d(x, y - range, z), mopHolder);
		RayTraceResult mop = mopHolder[0];
		
		if(acc == null) {
			return spill(mop, safe, stacks, quanta, impactPosHolder);
		}
		
		for(MaterialStack stack : stacks) {
			
			int amountToPour = Math.min(stack.amount, quanta);
			MaterialStack toPour = new MaterialStack(stack.material, amountToPour);
			MaterialStack left = tryPourStack(world, acc, mop, toPour, impactPosHolder);
			
			if(left != null) {
				stack.amount -= (amountToPour - left.amount);
				return new MaterialStack(stack.material, stack.amount - left.amount);
			}
		}
		
		return spill(mop, safe, stacks, quanta, impactPosHolder);
	}
	
	/**
	 * Tries to pour the stack onto the supplied crucible acceptor instance. Also features our friend the Vec3 dummy, which will be filled with the stream's impact position.
	 * Returns whatever is left of the stack when successful or null when unsuccessful (potential spillage).
	 */
	public static MaterialStack tryPourStack(World world, ICrucibleAcceptor acc, RayTraceResult mop, MaterialStack stack, Vec3 impactPosHolder) {
		Vec3d hit = mop.hitVec;
		
		if(stack.material.smeltable != SmeltingBehavior.SMELTABLE) {
			return null;
		}
		
		if(acc.canAcceptPartialPour(world, mop.getBlockPos(), hit.x, hit.y, hit.z, ForgeDirection.getOrientation(mop.sideHit), stack)) {
			MaterialStack left = acc.pour(world, mop.getBlockPos(), hit.x, hit.y, hit.z, ForgeDirection.getOrientation(mop.sideHit), stack);
			if(left == null) {
				left = new MaterialStack(stack.material, 0);
			}
			
			impactPosHolder.xCoord = hit.x;
			impactPosHolder.yCoord = hit.y;
			impactPosHolder.zCoord = hit.z;
			
			return left;
		}
		
		return null;
	}
	
	/**
	 * Uses hitscan to find the target of the pour, from start (the top) to end (the bottom). Pass a single cell MOP array to get the reference of the MOP for later use.
	 * Now we're thinking with reference types.
	 */
	public static ICrucibleAcceptor getPouringTarget(World world, Vec3d start, Vec3d end, RayTraceResult[] mopHolder) {
		
		RayTraceResult mop = world.rayTraceBlocks(start, end, true, true, true);
		
		if(mopHolder != null) {
			mopHolder[0] = mop;
		}
		
		if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
			return null;
		}
		
		Block b = world.getBlockState(mop.getBlockPos()).getBlock();
		
		if(!(b instanceof ICrucibleAcceptor)) {
			return null;
		}
		
		return (ICrucibleAcceptor) b;
	}
	
	/**
	 * Regular spillage routine but accepts a stack list instead of a stack. simply uses the first available stack from the list. Assumes list is not empty.
	 */
	public static MaterialStack spill(RayTraceResult mop, boolean safe, List<MaterialStack> stacks, int quanta, Vec3 impactPos) {
		//simply use the first available material
		MaterialStack top = stacks.get(0);
		MaterialStack ret = spill(mop, safe, top, quanta, impactPos);
		//remove all stacks with no content
		stacks.removeIf(o -> o.amount <= 0);
		
		return ret;
	}
	
	/**
	 * The routine used for then there is no valid crucible acceptor found. Will NOP with safe mode on. Returns the MaterialStack that was lost.
	 */
	public static MaterialStack spill(RayTraceResult mop, boolean safe, MaterialStack stack, int quanta, Vec3 impactPos) {
		
		//do nothing if safe mode is on
		if(safe) {
			return null;
		}
		
		MaterialStack toWaste = new MaterialStack(stack.material, Math.min(stack.amount, quanta));
		stack.amount -= toWaste.amount;
		
		//if there is a vec3 reference, set the impact coordinates
		if(impactPos != null && mop != null) {
			impactPos.xCoord = mop.hitVec.x;
			impactPos.yCoord = mop.hitVec.y;
			impactPos.zCoord = mop.hitVec.z;
		}
		
		return toWaste;
	}
}
