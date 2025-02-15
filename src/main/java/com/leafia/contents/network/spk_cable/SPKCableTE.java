package com.leafia.contents.network.spk_cable;

import com.hbm.interfaces.ILaserable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class SPKCableTE extends TileEntity implements ILaserable {
	public static class EffectLink {
		BlockPos link = null;
		boolean emit = false;
		final EnumFacing direction;
		final SPKCableTE self;
		EffectLink(SPKCableTE self,int i) {
			this.self = self;
			direction = EnumFacing.values()[i];
		}
		public void reset() {
			link = null;
			emit = false;
		}
		public void setLink(SPKCableTE other) {
			reset();
			link = other.getPos();
			EffectLink lnk = other.links[direction.getOpposite().ordinal()];
			if (lnk.link == null || !lnk.link.equals(self.getPos())) {
				lnk.reset();
				lnk.link = self.getPos();
				emit = true;
			}
		}
	}
	public EffectLink[] links = new EffectLink[EnumFacing.values().length];
	public SPKCableTE() {
		for (int i = 0; i < EnumFacing.values().length; i++)
			links[i] = new EffectLink(this,i);
	}
	@Override
	public void addEnergy(long energy,EnumFacing dir) {

	}
}
