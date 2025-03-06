package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.render.RenderSparks;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityCore;
import com.hbm.tileentity.machine.TileEntityCore.Cores;
import com.llib.math.LeafiaColor;
import com.llib.math.MathLeafia;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderCore extends TileEntitySpecialRenderer<TileEntityCore> {

    @Override
    public boolean isGlobalRenderer(TileEntityCore te) {
        return true;
    }

    @Override
    public void render(TileEntityCore core, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (core.temperature < 100) {
            renderStandby(core, x, y, z);
        } else {

            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            //GL11.glRotatef(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            //GL11.glRotatef(Minecraft.getMinecraft().getRenderManager().playerViewX - 90, 1.0F, 0.0F, 0.0F);
            GL11.glTranslated(-0.5, -0.5, -0.5);

            renderOrb(core, 0, 0, 0);
            GL11.glPopMatrix();
        }
        if (core.jammerPos != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            LeafiaColor colorBlast = new LeafiaColor(1, 0.5, 0);
            LeafiaColor colorJammer = new LeafiaColor(0, 0.75, 1);
            Vec3 vec = new Vec3(core.jammerPos.subtract(core.getPos()));
            BeamPronter.prontBeam(
                    vec,
                    EnumWaveType.RANDOM, EnumBeamType.SOLID,
                    colorBlast.toInARGB(), colorBlast.toInARGB(),
                    (int) (core.getWorld().getTotalWorldTime() % 1000),
                    (int) vec.length(), 0.5f, 1, 0.2f
            );
            BeamPronter.prontBeam(
                    vec,
                    EnumWaveType.RANDOM, EnumBeamType.SOLID,
                    colorJammer.toInARGB(), colorJammer.toInARGB(),
                    (int) ((core.getWorld().getTotalWorldTime() + 500) % 1000),
                    (int) vec.length(), 0.5f, 1, 0.2f
            );
            GL11.glPopMatrix();
        }
    }

    public void renderStandby(TileEntityCore core, double x, double y, double z) {

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableTexture2D();

        GL11.glScalef(0.25F, 0.25F, 0.25F);
        float brightness = (float) Math.pow(core.temperature / 100d, 1.5);
        GlStateManager.color(0.1F + brightness * .9f, 0.1F + brightness * .9f, 0.1F + brightness * .9f);
        ResourceManager.sphere_uv.renderAll();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GL11.glScalef(1.25F, 1.25F, 1.25F);
        GlStateManager.color(0.1F + brightness * .9f, 0.2F + (((float) core.temperature / 100f) * 0.25f + brightness * 0.75f) * .8f, 0.4F + ((float) core.temperature / 100f) * .6f);
        ResourceManager.sphere_uv.renderAll();
        GlStateManager.disableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        if (core.getWorld().rand.nextInt(50) == 0) {
            for (int i = 0; i < 3; i++) {
                RenderSparks.renderSpark((int) System.currentTimeMillis() / 100 + i * 10000, 0, 0, 0, 1.5F, 5, 10, 0x00FFFF, 0xFFFFFF);
                RenderSparks.renderSpark((int) System.currentTimeMillis() / 50 + i * 10000, 0, 0, 0, 3F, 5, 10, 0x00FFFF, 0xFFFFFF);
            }
        }
        GlStateManager.color(1F, 1F, 1F);
        GL11.glPopMatrix();
    }

    public void renderOrb(TileEntityCore core, double x, double y, double z) {

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        GL11.glPushMatrix();

        int color = core.color;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8 & 255) / 255F;
        float b = (color & 255) / 255F;
        GlStateManager.color(r, g, b, 1.0F);

        int tot = core.tanks[0].getCapacity() + core.tanks[1].getCapacity();
        int fill = core.tanks[0].getFluidAmount() + core.tanks[1].getFluidAmount();

        float scale = (float) Math.log(core.temperature / 50 + 1) * ((float) fill / (float) tot) + 0.5F;
        GL11.glScalef(scale, scale, scale);

        GlStateManager.enableCull();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        GL11.glScalef(0.5F, 0.5F, 0.5F);
        ResourceManager.sphere_ruv.renderAll();
        GL11.glScalef(2F, 2F, 2F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);

        for (int i = 6; i <= 10; i++) {

            GL11.glPushMatrix();
            GL11.glScalef(i * 0.1F, i * 0.1F, i * 0.1F);
            ResourceManager.sphere_ruv.renderAll();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        if (core.client_type == Cores.ams_core_sing) {
            GL11.glPushMatrix();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(0.11f, 0.11f, 0.11f);
            GL11.glScalef(-scale * 1.15f, -scale * 1.15f, -scale * 1.15f);
            ResourceManager.sphere_ruv.renderAll();
            GL11.glPopMatrix();

            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(0.529f, 0.722f, 1, 0.5f);
            double tr = MathHelper.positiveModulo(System.currentTimeMillis() / 1000d, 3000) / 2 * Math.PI;
            double add = 0.4; // 0.6
            GL11.glRotated(tr / 3 * (180 / Math.PI), 0, 1, 2);
            for (int d = 0; d < 16; d++) {
                GL11.glPushMatrix();
                double t = tr + add * d;
                GL11.glRotated(-MathLeafia.smoothLinear(Math.abs(MathHelper.positiveModulo(t / 3 / Math.PI, 2) - 1), 0.5) * 180 * 3, 0, 0, 1);
                GL11.glRotated(Math.sin(t / 3) * 135, 0, 1, 0);
                GL11.glTranslated(0, 0, -scale * 1.4 / 2);
                GL11.glScalef(-0.25f, -0.25f, -0.25f);
                ResourceManager.sphere_ruv.renderAll();
                GL11.glPopMatrix();
            }
        }
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GL11.glPopMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableCull();
        GL11.glPopMatrix();
    }
}
