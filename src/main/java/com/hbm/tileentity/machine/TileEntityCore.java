package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.MachineFieldDisturber;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModFluidProperties;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.machine.ItemCatalyst;
import com.hbm.items.special.ItemAMSCore;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;
import com.leafia.contents.effects.folkvangr.visual.EntityCloudFleijaRainbow;
import com.leafia.dev.LeafiaDebug;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import com.leafia.passive.LeafiaPassiveLocal;
import com.llib.math.LeafiaColor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class TileEntityCore extends TileEntityMachineBase implements ITickable, LeafiaPacketReceiver {
    public enum Cores {
        ams_core_sing(HBMSoundHandler.dfc_vs, (intended, distance) ->
                Math.pow(MathHelper.clamp(1 - (distance - 3) / 15, 0, 1), 1.5)),
        ams_core_wormhole(HBMSoundHandler.dfc_tw, (intended, distance) ->
                Math.pow(MathHelper.clamp(1 - (distance - 3) / 40, 0, 1), 2)),
        ams_core_eyeofharmony(HBMSoundHandler.dfc_eoh, (intended, distance) ->
                Math.pow(MathHelper.clamp(1 - (distance - 3) / 150, 0, 1), 3));
        public final SoundEvent sfx;
        public final BiFunction<Float, Double, Double> attentuationFunction;

        Cores(SoundEvent sfx, BiFunction<Float, Double, Double> attentuationFunction) {
            this.sfx = sfx;
            this.attentuationFunction = attentuationFunction;
        }
    }

    public boolean hasCore = false;
    public int field;
    public int heat;
    public int color;
    public FluidTank[] tanks;
    public int overload = 0;

    public enum packetKeys {
        TEMP, STABILIZATION, MAXIMUM,
        CONTAINED, EXPELLING, POTENTIAL,

        TANK_A, TANK_B,
        EXPEL_TICK, COLOR, CORE_TYPE,

        PLAY_SOUND, JAMMER;

        public int key;

        packetKeys() {
            this.key = this.ordinal();
        }
    }

    public double temperature = 0;
    public double stabilization = 0;
    public double containedEnergy = 0;
    public double expellingEnergy = 0;
    public double potentialRelease = 0;

    public double internalEnergy = 0;
    public double[] expelTicks = new double[20];
    public double energyMod = 1;
    public double bonus = 0;
    public final List<TileEntityCoreReceiver> absorbers = new ArrayList<>();
    boolean destroyed = false;
    double explosionIn = -1;
    public long explosionClock = 0;
    public BlockPos jammerPos = null;
    public static double failsafeLevel = 250000000;

    public double incomingSpk = 0;
    public double expellingSpk = 0;

    public double getStabilizationDiv() {
        return 1 + Math.sqrt(stabilization * 10);
    }

    public double getStabilizationDivAlt() {
        return Math.pow(stabilization / 3, 2) * 3 + 1;
    }

    public double getEnergyBase(double corePower) {
        double value = (Math.pow(Math.max(temperature / meltingPoint, 0) / 666 + 1, 0.666) - 1) * 666;
        // ^^ fuck this shit :D

        value = (Math.pow(Math.tanh(temperature / 666), 2) * 0.5
                + Math.pow(Math.max(0, Math.atan((temperature - 1300) / 666)), 2)) * 2.5
                + Math.pow(temperature / 5000, 3);

        value = Math.pow(value / Math.max(corePower, 0.001)/* anti-division by zero */, 0.666) * corePower;
        if (temperature > meltingPoint)
            value = value + (temperature - meltingPoint) / 100;
        return value;
    }

    public double getEnergyCurved(double energy) {
        return Math.pow(energy / 200, 2.25) * 200;
    }

    public int meltingPoint = 2250;
    public int ticks = 0;

    public TileEntityCore() {
        super(3);
        tanks = new FluidTank[2];
        tanks[0] = new FluidTank(128000);
        tanks[1] = new FluidTank(128000);
    }

    @Override
    public String getName() {
        return "container.dfcCore";
    }


    public double client_maxDial = 0.95;
    public Cores client_type = null;
    AudioWrapper client_sfx = null;
    boolean sfxPlaying = false;
    AudioWrapper meltdownSFX = null;

    @Override
    public void invalidate() {
        if (sfxPlaying && client_sfx != null) {
            client_sfx.stopSound();
            client_sfx = null;
            sfxPlaying = false;
        }
        if (meltdownSFX != null) {
            meltdownSFX.stopSound();
            meltdownSFX = null;
        }
        MinecraftServer server = world.getMinecraftServer();
        if (server != null && !world.isRemote) {
            if (!server.isDedicatedServer())
                LeafiaPassiveLocal.trackingCores.remove(this);
        }
        super.invalidate();
    }

    @Override
    public void validate() {
        super.validate();
        MinecraftServer server = world.getMinecraftServer();
        if (server != null && !world.isRemote) {
            if (!server.isDedicatedServer())
                LeafiaPassiveLocal.trackingCores.add(this);
        }
    }

    @Override
    public void update() {
        if (destroyed) return;
        if (!world.isRemote) {
			/*
			if(heat > 0 && heat >= field) {
				
				int fill = tanks[0].getFluidAmount() + tanks[1].getFluidAmount();
				int max = tanks[0].getCapacity() + tanks[1].getCapacity();
				int mod = heat * 10;
				
				int size = Math.max(Math.min(fill * mod / max, 1000), 50);
				
				//System.out.println(fill + " * " + mod + " / " + max + " = " + size);

	    		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 100000.0F, 1.0F);

				EntityNukeExplosionMK3 exp = new EntityNukeExplosionMK3(world);
				exp.posX = pos.getX();
				exp.posY = pos.getY();
				exp.posZ = pos.getZ();
				exp.destructionRange = size;
				exp.speed = 25;
				exp.coefficient = 1.0F;
				exp.waste = false;
				if(overload >= 60 && !EntityNukeExplosionMK3.isJammed(this.world, exp)){
					world.spawnEntity(exp);
		    		
		    		EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(world, size);
		    		cloud.posX = pos.getX();
		    		cloud.posY = pos.getY();
		    		cloud.posZ = pos.getZ();
		    		world.spawnEntity(cloud);
		    	}
		    	overload++;
			} else {
				if(overload > 0) overload = 0;
			}*/
            ItemStack catalystA = inventory.getStackInSlot(0);
            ItemStack catalystB = inventory.getStackInSlot(2);
            NBTTagCompound tagA = null;
            NBTTagCompound tagB = null;
            double damageA = 100;
            double damageB = 100;
            if (catalystA.getItem() instanceof ItemCatalyst && catalystB.getItem() instanceof ItemCatalyst) {
                LeafiaColor col = new LeafiaColor(calcAvgHex(
                        ((ItemCatalyst) catalystA.getItem()).getColor(),
                        ((ItemCatalyst) catalystB.getItem()).getColor()
                ));
                tagA = catalystA.getTagCompound();
                tagB = catalystB.getTagCompound();
                if (tagA == null) {
                    tagA = new NBTTagCompound();
                    catalystA.setTagCompound(tagA);
                }
                if (tagB == null) {
                    tagB = new NBTTagCompound();
                    catalystB.setTagCompound(tagB);
                }
                damageA = tagA.getDouble("damage");
                damageB = tagB.getDouble("damage");
                color = col.lerp(new LeafiaColor(
                        world.rand.nextFloat(), world.rand.nextFloat(), world.rand.nextFloat()
                ), Math.pow(Math.max(damageA / 100, damageB / 100), 2)).toInARGB();
                hasCore = true;
            } else {
                color = 0;
                hasCore = false;
            }
            expellingSpk = 0;

            if (inventory.getStackInSlot(1).getItem() instanceof ItemAMSCore && tanks[0].getFluid() != null && tanks[1].getFluid() != null) {
                if (tagA != null && tagB != null) {
                    meltingPoint = Math.min(1500000, Math.min(ItemCatalyst.getMelting(catalystA), ItemCatalyst.getMelting(catalystB)));

                    double corePower = getCorePower();
                    double coreHeatMod = getCoreHeat();
                    double coreInefficiency = getCoreFuel();
                    //1 SPK = 5,000HE
                    long catalystPower = ItemCatalyst.getPowerAbs(catalystA) + ItemCatalyst.getPowerAbs(catalystB);
                    float catalystPowerMod = ItemCatalyst.getPowerMod(catalystA) * ItemCatalyst.getPowerMod(catalystB);
                    float catalystHeatMod = ItemCatalyst.getHeatMod(catalystA) * ItemCatalyst.getHeatMod(catalystB);
                    float catalystFuelMod = ItemCatalyst.getFuelMod(catalystA) * ItemCatalyst.getFuelMod(catalystB);
                    double catalystPowerSPK = catalystPower / 5000d;

                    ticks++;
                    //LeafiaDebug.debugLog(world,"incomingSpk: "+incomingSpk);

                    double desiredBonus = Math.sqrt(incomingSpk);
                    bonus += (desiredBonus - bonus) * 0.01;

                    containedEnergy += (Math.pow(Math.pow(incomingSpk, 0.666) + 1, 0.666) - 1) * 6.666 / 3;
                    double multiplier = (1 + world.rand.nextGaussian() * 0.75 / getStabilizationDivAlt())
                            * (1 + Math.sin(ticks * Math.PI) * 0.75 / getStabilizationDivAlt());

                    double tempChange = Math.pow(Math.pow(containedEnergy,
                            MathHelper.clampedLerp(0.44, 0.666, Math.pow(1 / getStabilizationDivAlt(), 0.666))) / 100, 0.666) * 100 * 7
                            * Math.max(1 - Math.pow(temperature / meltingPoint, 2) / 1.25 * Math.max(1 - Math.pow(containedEnergy / 10000, 1), 0.1), 0.5) //Math.pow(internalEnergy/corePower,6.666/getStabilizationDiv()/getStabilizationDiv())*corePower //(Math.pow(internalEnergy/(meltingPoint/2d),6.666/getStabilizationDiv()/getStabilizationDiv())*(meltingPoint/2d))
                            / 20 * coreHeatMod * multiplier;
                    tempChange = tempChange - Math.signum(tempChange) * Math.max(Math.abs(tempChange) - 10, 0) / 5;
                    temperature += tempChange;
                    temperature *= 0.99 * Math.pow(1 / getStabilizationDiv(), 0.0025);

                    Fluid f1 = tanks[0].getFluid().getFluid();
                    Fluid f2 = tanks[1].getFluid().getFluid();

                    //tanks[0].drain(demand, true);
                    //tanks[1].drain(demand, true);

                    double fuelPower = ModFluidProperties.getDFCEfficiency(f1) * ModFluidProperties.getDFCEfficiency(f2);

                    containedEnergy += Math.pow(temperature, 0.666) * getEnergyBase(corePower) * corePower / 65 //Math.pow(temperature*getEnergyBase()/corePower,0.5)*corePower
                            / 20 / Math.pow(getStabilizationDiv(), 2) * energyMod * multiplier * fuelPower;

                    if (Double.isNaN(temperature)) temperature = failsafeLevel; // fuck you
                    if (Double.isNaN(containedEnergy)) containedEnergy = failsafeLevel; // fuck you
                    if (Double.isNaN(tempChange)) tempChange = 0; // fuck you
                    if (temperature < -100) temperature = failsafeLevel; // for real fuck you piece of dipshti
                    temperature = Math.min(failsafeLevel, temperature); // for technical reasons there has to be a limit
                    containedEnergy = Math.min(failsafeLevel, containedEnergy); // for technical reasons there has to be a limit

                    double taxes = Math.max(0, containedEnergy - (temperature / meltingPoint) * 1000);
                    //containedEnergy -= taxes/14;
                    //containedEnergy = getEnergyCurved(internalEnergy)*energyMod;
                    {
                        double sin = Math.sin(ticks / 20d * Math.PI);
                        double abs = Math.abs(sin);
                        double sign = Math.signum(sin);
                        double wave = (1 - Math.pow(abs, 0.5)) * (1 + Math.sin(ticks / 20d * Math.PI * 22) * 0.7);//sign*Math.pow(abs,0.5);
                        double craziness = Math.pow(Math.abs(Math.sin(ticks / 20d * Math.PI * 0.666)), 6.666) * 25 / getStabilizationDiv() / getStabilizationDivAlt();
                        craziness *= Math.max(0, Math.pow(containedEnergy / 10000, 4) - 0.2);
                        //double waveScaled = wave/2/getStabilizationDiv()+0.5;
                        potentialRelease = getEnergyBase(corePower) / 200 * energyMod + wave * containedEnergy / 2000 / getStabilizationDivAlt() + craziness; //waveScaled * containedEnergy/200;
                        potentialRelease = Math.abs(potentialRelease); // overflows are expected
                    }
                    double energyBefore = containedEnergy;
                    if (world.rand.nextInt(101) >= 50 / getStabilizationDivAlt())
                        containedEnergy = Math.max(containedEnergy - (containedEnergy * potentialRelease / 20 + taxes / 14) * absorbers.size(), 0);
                    //containedEnergy = getEnergyCurved(internalEnergy)*energyMod;
                    double expelBonus = Math.min(bonus * bonus, bonus * bonus * potentialRelease / 20 * absorbers.size());
                    bonus -= Math.sqrt(expelBonus);

                    double expelling = energyBefore - containedEnergy + expelBonus;
                    for (TileEntityCoreReceiver absorber : absorbers) {
                        absorber.joules += (long) (expelling * 333_333) / absorbers.size();
                    }
                    expellingSpk = expelling;
                    expelTicks[Math.floorMod(ticks, 20)] = expelling;

                    double timeToMeltdown = 10;
                    double timeToRegen = 30;
                    tagA.setDouble("damage", MathHelper.clamp(damageA
                                    + (temperature >= ItemCatalyst.getMelting(catalystA) ? 5 / timeToMeltdown : -5 / timeToRegen),
                            0, 100
                    ));
                    tagB.setDouble("damage", MathHelper.clamp(damageB
                                    + (temperature >= ItemCatalyst.getMelting(catalystB) ? 5 / timeToMeltdown : -5 / timeToRegen),
                            0, 100
                    ));
                }

                if (damageA >= 100 || damageB >= 100) {
                    EntityNukeExplosionMK3 exp = null;
                    if (jammerPos != null) {
                        if (!(world.getBlockState(jammerPos).getBlock() instanceof MachineFieldDisturber))
                            jammerPos = null;
                    }
                    if (explosionIn < 10 || jammerPos == null) { // stand by
                        exp = new EntityNukeExplosionMK3(world);
                        exp.posX = pos.getX();
                        exp.posY = pos.getY();
                        exp.posZ = pos.getZ();
                        exp.destructionRange = 20;
                        exp.speed = 25;
                        exp.coefficient = 1.0F;
                        exp.waste = false;
                    }
                    if (jammerPos == null) {
                        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 100000.0F, 1.0F);

                        if (!EntityNukeExplosionMK3.isJammed(this.world, exp)) {
                            destroyed = true;
                            world.spawnEntity(exp);
                            EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(world, exp.destructionRange);
                            cloud.posX = pos.getX();
                            cloud.posY = pos.getY();
                            cloud.posZ = pos.getZ();
                            world.spawnEntity(cloud);
                        } else {
                            jammerPos = EntityNukeExplosionMK3.lastDetectedJammer;
                            if (explosionIn < 0) {
                                explosionIn = 120;
                                explosionClock = System.currentTimeMillis();
                                LeafiaPacket._start(this)
                                        .__write(packetKeys.PLAY_SOUND.key, 0)
                                        .__sendToAll();
                            }
                        }
                    }
                    if (jammerPos != null) {
                        boolean tick = true;
                        MinecraftServer server = world.getMinecraftServer();
                        if (server != null) {
                            LeafiaDebug.debugLog(world, "isSinglePlayer: " + server.isSinglePlayer());
                            LeafiaDebug.debugLog(world, "isServerInOnlineMode: " + server.isServerInOnlineMode());
                            LeafiaDebug.debugLog(world, "isDedicatedServer: " + server.isDedicatedServer());
                            LeafiaDebug.debugLog(world, TextFormatting.GOLD + "Time Left: " + explosionIn);
                            if (!server.isDedicatedServer())
                                tick = !Minecraft.getMinecraft().isGamePaused();
                        }
                        if (tick) {
                            long time = System.currentTimeMillis();
                            explosionIn = Math.max(explosionIn - (time - explosionClock) / 1000d, 0);
                            explosionClock = time;
                            if (explosionIn <= 0 && exp != null) {
                                world.playSound(null, pos, HBMSoundHandler.dfc_explode, SoundCategory.BLOCKS, 100, 1);
                                destroyed = true;
                                world.spawnEntity(exp);
                                EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(world, exp.destructionRange);
                                cloud.posX = pos.getX();
                                cloud.posY = pos.getY();
                                cloud.posZ = pos.getZ();
                                world.spawnEntity(cloud);
                            }
                        }
                    }
                } else {
                    if (explosionIn >= 0) {
                        jammerPos = null;
                        explosionIn = -1;
                        LeafiaPacket._start(this)
                                .__write(packetKeys.PLAY_SOUND.key, 1)
                                .__sendToAll();
                    }
                }
            }

            expellingEnergy = 0;
            for (double energy : expelTicks)
                expellingEnergy += energy;
            incomingSpk = 0;
            energyMod = 1;
            absorbers.clear();

            if (temperature > 100) {
                vaporization();
            }
			/*
			NBTTagCompound data = new NBTTagCompound();
			data.setString("tank0", tanks[0].getFluid() == null ? "HBM_EMPTY" : tanks[0].getFluid().getFluid().getName());
			data.setString("tank1", tanks[1].getFluid() == null ? "HBM_EMPTY" : tanks[1].getFluid().getFluid().getName());
			data.setInteger("fill0", tanks[0].getFluidAmount());
			data.setInteger("fill1", tanks[1].getFluidAmount());
			data.setInteger("field", field);
			data.setInteger("heat", heat);
			data.setInteger("color", color);
			data.setBoolean("hasCore", hasCore);
			networkPack(data, 250);
			*/
            //PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, tanks), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 10));
            NBTTagCompound fluidA = new NBTTagCompound();
            NBTTagCompound fluidB = new NBTTagCompound();
            tanks[0].writeToNBT(fluidA);
            tanks[1].writeToNBT(fluidB);
            Integer coreId = null;
            try {
                coreId = Cores.valueOf(inventory.getStackInSlot(1).getItem().getRegistryName().getPath()).ordinal();
            } catch (IllegalArgumentException | NullPointerException ignored) {
            } // fuck you im lazy
            LeafiaPacket._start(this)
                    .__write(packetKeys.TANK_A.key, fluidA)
                    .__write(packetKeys.TANK_B.key, fluidB)

                    .__write(packetKeys.TEMP.key, temperature)
                    .__write(packetKeys.STABILIZATION.key, stabilization)
                    .__write(packetKeys.CONTAINED.key, containedEnergy + bonus * bonus)
                    .__write(packetKeys.EXPELLING.key, expellingEnergy)
                    .__write(packetKeys.POTENTIAL.key, potentialRelease)

                    .__write(packetKeys.EXPEL_TICK.key, expellingSpk)
                    .__write(packetKeys.MAXIMUM.key, meltingPoint)

                    .__write(packetKeys.COLOR.key, color)
                    .__write(packetKeys.CORE_TYPE.key, coreId)

                    .__write(packetKeys.JAMMER.key, jammerPos)

                    .__sendToAffectedClients();

            heat = 0;
            stabilization = 0;
            this.markDirty();
        } else {
            ticks++;
            client_maxDial = world.rand.nextDouble() * 0.08 + 0.9;

            if (client_sfx != null) {
                if (temperature >= 100 && !sfxPlaying) {
                    sfxPlaying = true;
                    client_sfx.startSound();
                } else if (temperature < 100 && sfxPlaying) {
                    sfxPlaying = false;
                    client_sfx.stopSound();
                }
            }
            //TODO: sick particle effects
        }
    }
	/*
	@Override
	public void networkUnpack(NBTTagCompound data) {
		String s0 = data.getString("tank0");
		String s1 = data.getString("tank1");
		if("HBM_EMPTY".equals(s0)){
			tanks[0].setFluid(null);
		} else {
			tanks[0].setFluid(new FluidStack(FluidRegistry.getFluid(s0), data.getInteger("fill0")));
		}
		if("HBM_EMPTY".equals(s1)){
			tanks[1].setFluid(null);
		} else {
			tanks[1].setFluid(new FluidStack(FluidRegistry.getFluid(s1), data.getInteger("fill1")));
		}
		field = data.getInteger("field");
		heat = data.getInteger("heat");
		color = data.getInteger("color");
		hasCore = data.getBoolean("hasCore");
	}*/

    private void vaporization() {

        double scale = (int) Math.log(temperature / 50 + 1) * 1.25 / 4 + 0.5;

        int range = (int) (scale * 4);
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - range + 0.5, pos.getY() - range + 0.5, pos.getZ() - range + 0.5, pos.getX() + range + 0.5, pos.getY() + range + 0.5, pos.getZ() + range + 0.5));

        for (Entity e : list) {
            boolean isPlayer = e instanceof EntityPlayer;
            if (!(isPlayer && ArmorUtil.checkForHazmat((EntityPlayer) e))) {
                if (!(Library.isObstructed(world, pos.getX() + 0.5, pos.getY() + 0.5 + 6, pos.getZ() + 0.5, e.posX, e.posY + e.getEyeHeight(), e.posZ))) {
                    if (!isPlayer || (isPlayer && !((EntityPlayer) e).capabilities.isCreativeMode))
                        e.attackEntityFrom(ModDamageSource.ams, (int) (this.temperature / 100));
                    e.setFire(3);
                }
            }
            if (isPlayer) {
                AdvancementManager.grantAchievement(((EntityPlayer) e), AdvancementManager.progress_dfc);
            }
        }

        List<Entity> list2 = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - scale + 0.5, pos.getY() - scale + 0.5, pos.getZ() - scale + 0.5, pos.getX() + scale + 0.5, pos.getY() + scale + 0.5, pos.getZ() + scale + 0.5));

        for (Entity e : list2) {
            boolean isPlayer = e instanceof EntityPlayer;
            if (!(isPlayer && ArmorUtil.checkForHaz2((EntityPlayer) e))) {
                if (!isPlayer || (isPlayer && !((EntityPlayer) e).capabilities.isCreativeMode))
                    e.attackEntityFrom(ModDamageSource.amsCore, (int) (this.temperature / 10));
                e.setFire(3);
            }
        }
    }

    public int getFieldScaled(int i) {
        return (field * i) / 100;
    }

    public int getHeatScaled(int i) {
        return (heat * i) / 100;
    }

    public boolean isReady() {

        if (getCorePower() == 0)
            return false;

        if (color == 0)
            return false;

        if (tanks[0].getFluid() == null || tanks[1].getFluid() == null)
            return false;

        if (ModFluidProperties.getDFCEfficiency(tanks[0].getFluid().getFluid()) <= 0 || ModFluidProperties.getDFCEfficiency(tanks[1].getFluid().getFluid()) <= 0)
            return false;

        return true;
    }

    //100 emitter watt = 10000 joules = 1 heat = 10mB burned
    public long burn(long joules) {

        //check if a reaction can take place
        if (!isReady())
            return joules;

        int demand = (int) Math.sqrt((double) joules);

        long powerAbs = ItemCatalyst.getPowerAbs(inventory.getStackInSlot(0)) + ItemCatalyst.getPowerAbs(inventory.getStackInSlot(2));
        float powerMod = ItemCatalyst.getPowerMod(inventory.getStackInSlot(0)) * ItemCatalyst.getPowerMod(inventory.getStackInSlot(2));
        float heatMod = ItemCatalyst.getHeatMod(inventory.getStackInSlot(0)) * ItemCatalyst.getHeatMod(inventory.getStackInSlot(2));
        float fuelMod = ItemCatalyst.getFuelMod(inventory.getStackInSlot(0)) * ItemCatalyst.getFuelMod(inventory.getStackInSlot(2));

        demand = (int) (getCoreFuel() * demand * fuelMod);

        //check if the reaction has enough valid fuel
        if (tanks[0].getFluidAmount() < demand || tanks[1].getFluidAmount() < demand)
            return joules;

        heat += (int) (getCoreHeat() * heatMod * Math.ceil((double) joules / 10000D));

        Fluid f1 = tanks[0].getFluid().getFluid();
        Fluid f2 = tanks[1].getFluid().getFluid();

        tanks[0].drain(demand, true);
        tanks[1].drain(demand, true);

        long powerOutput = (long) Math.max(0, (powerMod * joules * getCorePower() * ModFluidProperties.getDFCEfficiency(f1) * ModFluidProperties.getDFCEfficiency(f2)) + powerAbs);
        if (powerOutput > 0 && heat == 0)
            heat = 1;
        return powerOutput;
    }

    //TODO: move stats to the AMSCORE class
    //Alcater: ok did that
    public int getCorePower() {
        return ItemAMSCore.getPowerBase(inventory.getStackInSlot(1));
    }

    public float getCoreHeat() {
        return ItemAMSCore.getHeatBase(inventory.getStackInSlot(1));
    }

    public float getCoreFuel() {
        return ItemAMSCore.getFuelBase(inventory.getStackInSlot(1));
    }

    private int calcAvgHex(int h1, int h2) {

        int r1 = ((h1 & 0xFF0000) >> 16);
        int g1 = ((h1 & 0x00FF00) >> 8);
        int b1 = ((h1 & 0x0000FF) >> 0);

        int r2 = ((h2 & 0xFF0000) >> 16);
        int g2 = ((h2 & 0x00FF00) >> 8);
        int b2 = ((h2 & 0x0000FF) >> 0);

        int r = (((r1 + r2) / 2) << 16);
        int g = (((g1 + g2) / 2) << 8);
        int b = (((b1 + b2) / 2) << 0);

        return r | g | b;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("tanks"))
            FFUtils.deserializeTankArray(compound.getTagList("tanks", 10), tanks);
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("tanks", FFUtils.serializeTankArray(tanks));
        return super.writeToNBT(compound);
    }

    @Override
    public String getPacketIdentifier() {
        return "dfcore";
    }

    @Override
    public void onReceivePacketLocal(byte key, Object value) {
        for (packetKeys pkt : packetKeys.values()) {
            if (key == pkt.key) {
                switch (pkt) {
                    case TEMP:
                        temperature = (double) value;
                        break;
                    case STABILIZATION:
                        stabilization = (double) value;
                        break;
                    case MAXIMUM:
                        meltingPoint = (int) value;
                        break;
                    case CONTAINED:
                        containedEnergy = (double) value;
                        break;
                    case EXPELLING:
                        expellingEnergy = (double) value;
                        break;
                    case POTENTIAL:
                        potentialRelease = (double) value;
                        break;
                    case TANK_A:
                        tanks[0].readFromNBT((NBTTagCompound) value);
                        break;
                    case TANK_B:
                        tanks[1].readFromNBT((NBTTagCompound) value);
                        break;
                    case EXPEL_TICK:
                        expellingSpk = (double) value;
                        break;
                    case COLOR:
                        color = (int) value;
                        break;
                    case JAMMER:
                        jammerPos = (BlockPos) value;
                        break;
                    case CORE_TYPE:
                        Cores lastCore = client_type;
                        Integer id = (Integer) value;
                        if (id == null) client_type = null;
                        else client_type = Cores.values()[id];
                        if (client_type != lastCore) {
                            if (client_sfx != null) {
                                client_sfx.stopSound();
                                client_sfx = null;
                                sfxPlaying = false;
                            }
                            if (client_type != null) {
                                client_sfx = MainRegistry.proxy.getLoopedSound(
                                        client_type.sfx, SoundCategory.BLOCKS,
                                        pos.getX(), pos.getY(), pos.getZ(),
                                        1, 1
                                ).setCustomAttentuation(client_type.attentuationFunction);
                            }
                        }
                        break;
                    case PLAY_SOUND:
                        if (meltdownSFX != null) meltdownSFX.stopSound();
                        if (value == null) break;
                        int type = (int) value;
                        SoundEvent soundToPlay;
                        float pitch = 1;
                        if (type == 0) {
                            soundToPlay = HBMSoundHandler.dfc_meltdown;
                        } else if (type == 1) {
                            soundToPlay = SoundEvents.BLOCK_FIRE_EXTINGUISH;
                            pitch = 0.8f;
                        } else break;
                        meltdownSFX = MainRegistry.proxy.getLoopedSound(
                                        soundToPlay,
                                        SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(),
                                        1, pitch
                                ).setCustomAttentuation((intended, distance) -> Math.pow(MathHelper.clamp(1 - (distance - 50) / 500, 0, 1), 6.66))
                                .setLooped(false);
                        meltdownSFX.startSound();
                        break;
                }
            }
        }
    }

    @Override
    public void onReceivePacketServer(byte key, Object value, EntityPlayer plr) {

    }

    @Override
    public void onPlayerValidate(EntityPlayer plr) {

    }
}
