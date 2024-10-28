package com.hbm.main.leafia;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class LeafiaShakecam {
    public static NoiseGeneratorPerlin noise;
    public static float blurSum = 0;
    public static float bloomSum = 0;
    public static final Set<shakeInstance> instances = new HashSet<>();
    public static void localTick() {
        int len = instances.size();
        for (int i = 0; i < len;) {
            shakeInstance instance = (shakeInstance)(instances.toArray()[i]);
            if (!instance.tick()) {
                instances.remove(instance);
                len--;
            } else i++;
        }
    }
    public static void shakeCam() {
        double sumBlur = 0;
        double sumBloom = 0;
        for (shakeInstance instance : instances) {
            double dist = 0;
            if (instance.position != null) {
                Vec3d pos = Minecraft.getMinecraft().player.getPositionVector();
                dist = Math.sqrt(instance.position.distanceSqToCenter(pos.x,pos.y,pos.z));
            }
            double n = instance.render(dist);
            sumBlur += (Math.pow(n/16,2)*0.005);
            sumBloom += (Math.pow(n/16/5,6)*0.25);
        }
        blurSum = (float)sumBlur;
        bloomSum = (float)sumBloom;
    }
    public static void _addShake(@Nullable BlockPos pos,shakeInstance instance) {
        if (pos != null)
            instance.position = pos;
        instances.add(instance);
    }
    public static class shakeInstance {
        public BlockPos position = null;
        public float range = 25;
        public float intensity = 4;
        public float curve = 2;
        public float speed = 4;
        public float duration = 5;
        public LeafiaEase.Ease ease = LeafiaEase.Ease.EXPO;
        public LeafiaEase.Direction direction = LeafiaEase.Direction.O;
        public LeafiaEase easeInstance;
        shakeInstance(@Nullable Float duration, @Nullable LeafiaEase.Ease ease, @Nullable LeafiaEase.Direction direction) {
            if (duration != null)
                this.duration = duration;
            if (ease != null)
                this.ease = ease;
            if (direction != null)
                this.direction = direction;
            easeInstance = new LeafiaEase(this.ease,this.direction);
        }
        public shakeInstance configure(@Nullable Float range, @Nullable Float intensity, @Nullable Float curve, @Nullable Float speed) {
            if (range != null)
                this.range = range;
            if (intensity != null)
                this.intensity = intensity;
            if (curve != null)
                this.curve = curve;
            if (speed != null)
                this.speed = speed;
            return this;
        }
        public shakeInstance setRange(float range) {
            this.range = range;
            return this;
        }
        public shakeInstance setIntensity(float intensity) {
            this.intensity = intensity;
            return this;
        }
        public shakeInstance setCurve(float curve) {
            this.curve = curve;
            return this;
        }
        public shakeInstance setSpeed(float speed) {
            this.speed = speed;
            return this;
        }
        public shakeInstance loadPreset(Preset preset) {
            this.range = preset.range;
            this.intensity = preset.intensity;
            this.curve = preset.curve;
            this.speed = preset.speed;
            return this;
        }
        public double getTimeMultiplier(double time) {
            if (easeInstance == null) return 1;
            easeInstance.updateEase(ease,direction);
            return 1-easeInstance.get(time/duration,0,1,true);
        }
        public double getCalculatedIntensity(double distance) {
            return intensity*Math.pow(MathHelper.clamp(1-distance/range,0,1),curve);
        }
        public boolean tick() {
            return false;
        }
        public double render(double distance) { return 0; }
    }
    public enum Preset {
        RUPTURE(25,8,2,8),
        EXPLOSION(62.5f,12,3,8),
        QUAKE(350,4,3,4);
        public float range;
        public float intensity;
        public float curve;
        public float speed;
        Preset(float range,float intensity,float curve,float speed) {
            this.range = range;
            this.intensity = intensity;
            this.curve = curve;
            this.speed = speed;
        }
    }
    public static class shakeSimple extends shakeInstance {
        int ticks = 0;
        float x = 0;
        float y = 0;
        Random rand;
        public shakeSimple(@Nullable Float duration, @Nullable LeafiaEase.Ease ease, @Nullable LeafiaEase.Direction direction) {
            super(duration, ease, direction);
            rand = new Random();
        }
        @Override
        public boolean tick() {
            ticks++;
            x = rand.nextFloat()-0.5f;
            y = rand.nextFloat()-0.5f;
            return !(ticks/20d >= duration);
        }
        @Override
        public double render(double distance) {
            GL11.glTranslated(
                    0.04*x*getCalculatedIntensity(distance)*getTimeMultiplier(ticks/20d),
                    0.04*y*getCalculatedIntensity(distance)*getTimeMultiplier(ticks/20d),
                    0
            );
            return getCalculatedIntensity(distance)*getTimeMultiplier(ticks/20d)*4;
        }
    }
    public static class shakeSmooth extends shakeInstance {
        long lastMil = 0;
        double timer = 0;

        public shakeSmooth(@Nullable Float duration, @Nullable LeafiaEase.Ease ease, @Nullable LeafiaEase.Direction direction) {
            super(duration, ease, direction);
            lastMil = Math.floorMod(System.currentTimeMillis(),10000);
        }
        @Override
        public boolean tick() {
            return !(timer >= duration);
        }
        @Override
        public double render(double distance) {
            long curMil = Math.floorMod(System.currentTimeMillis(),10000);
            timer = timer + Math.floorMod(curMil-lastMil,10000)/1e+3;
            lastMil = curMil;
            GL11.glRotated(noise.getValue(timer*speed,0)*getCalculatedIntensity(distance)*getTimeMultiplier(timer),1,0,0);
            GL11.glRotated(noise.getValue(0,timer*speed)*getCalculatedIntensity(distance)*getTimeMultiplier(timer),0,1,0);
            GL11.glRotated(noise.getValue(timer*speed,timer*speed)*getCalculatedIntensity(distance)*getTimeMultiplier(timer),0,0,1);
            return getCalculatedIntensity(distance)*getTimeMultiplier(timer)*speed;
        }
    }
}
