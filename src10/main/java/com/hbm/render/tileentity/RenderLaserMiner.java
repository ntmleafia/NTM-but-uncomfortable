package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.MathHelper;

public class RenderLaserMiner extends TileEntitySpecialRenderer<TileEntityMachineMiningLaser> {

	@Override
	public boolean isGlobalRenderer(TileEntityMachineMiningLaser te) {
		return true;
	}
	
	@Override
	public void render(TileEntityMachineMiningLaser te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y - 1, z + 0.5);

        double tx = x;
		double ty = 0;
		double tz = z;
		if(((TileEntityMachineMiningLaser)te).beam) {
			tx = (((TileEntityMachineMiningLaser)te).targetX - ((TileEntityMachineMiningLaser)te).lastTargetX) * partialTicks + ((TileEntityMachineMiningLaser)te).lastTargetX;
			ty = (((TileEntityMachineMiningLaser)te).targetY - ((TileEntityMachineMiningLaser)te).lastTargetY) * partialTicks + ((TileEntityMachineMiningLaser)te).lastTargetY;
			tz = (((TileEntityMachineMiningLaser)te).targetZ - ((TileEntityMachineMiningLaser)te).lastTargetZ) * partialTicks + ((TileEntityMachineMiningLaser)te).lastTargetZ;
		}
		double vx = tx - ((TileEntityMachineMiningLaser)te).getPos().getX();
		double vy = ty - ((TileEntityMachineMiningLaser)te).getPos().getY() + 3;
		double vz = tz - ((TileEntityMachineMiningLaser)te).getPos().getZ();

		Vec3 nVec = Vec3.createVectorHelper(vx, vy, vz);
		nVec = nVec.normalize();

		double d = 1.5D;
		nVec.xCoord *= d;
		nVec.yCoord *= d;
		nVec.zCoord *= d;

		Vec3 vec = Vec3.createVectorHelper(vx - nVec.xCoord, vy - nVec.yCoord, vz - nVec.zCoord);

		double length = vec.length();
		double yaw = Math.toDegrees(Math.atan2(vec.xCoord, vec.zCoord));
		double sqrt = MathHelper.sqrt(vec.xCoord * vec.xCoord + vec.zCoord * vec.zCoord);
		double pitch = Math.toDegrees(Math.atan2(vec.yCoord, sqrt));
		//turns out using tan(vec.yCoord, length) was inaccurate,
		//the emitter wouldn't match the laser perfectly when pointing down

		bindTexture(ResourceManager.mining_laser_base_tex);
		ResourceManager.mining_laser.renderPart("Base");

		//GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GL11.glPushMatrix();
		GL11.glRotated(yaw, 0, 1, 0);
		bindTexture(ResourceManager.mining_laser_pivot_tex);
		ResourceManager.mining_laser.renderPart("Pivot");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotated(yaw, 0, 1, 0);
		GL11.glTranslated(0, -1, 0);
		GL11.glRotated(pitch + 90, -1, 0, 0);
		GL11.glTranslated(0, 1, 0);
		bindTexture(ResourceManager.mining_laser_laser_tex);
		ResourceManager.mining_laser.renderPart("Laser");
		GL11.glPopMatrix();
		//GlStateManager.shadeModel(GL11.GL_FLAT);

		if(((TileEntityMachineMiningLaser)te).beam) {
			length = vec.length();
			GL11.glTranslated(nVec.xCoord, nVec.yCoord - 1, nVec.zCoord);
			int range = (int)Math.ceil(length * 0.5);
			BeamPronter.prontBeam(vec, EnumWaveType.STRAIGHT, EnumBeamType.SOLID, 0xa00000, 0xFFFFFF, 0, 1, 0, 3, 0.09F);
		}

		GL11.glPopMatrix();
	}
}
