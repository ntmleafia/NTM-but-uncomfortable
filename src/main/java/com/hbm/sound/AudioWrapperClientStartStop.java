package com.hbm.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class AudioWrapperClientStartStop extends AudioWrapperClient {

	public SoundEvent start;
	public SoundEvent stop;
	public World world;
	public SoundCategory cat;
	public float ssVol;
	public float x, y, z;
	
	public AudioWrapperClientStartStop(World world, SoundEvent source, SoundEvent start, SoundEvent stop, float vol, SoundCategory cat){
		super(source, cat);
		if(sound != null){
			sound.setVolume(vol);
			sound.setAttenuation(ISound.AttenuationType.LINEAR);
		}
		this.ssVol = vol;
		this.world = world;
		this.start = start;
		this.stop = stop;
	}
	
	@Override
	public void updatePosition(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
		super.updatePosition(x, y, z);
	}
	
	@Override
	public void startSound(){
		super.startSound();
		if(start != null){
			float stupidpitch = 1;
			try {
				stupidpitch = getPitch(); // fuck you
			} catch (NullPointerException ignored) {}
			world.playSound(x, y, z, start, cat, ssVol, stupidpitch, false);
		}
	}
	
	@Override
	public void stopSound(){
		if(stop != null){
			float stupidpitch = 1;
			try {
				stupidpitch = getPitch(); // fuck you
			} catch (NullPointerException ignored) {}
			world.playSound(x, y, z, stop, cat, ssVol, stupidpitch, false);
		}
		super.stopSound();
	}
	
	@Override
	public float getVolume(){
		return ssVol;
	}

}
