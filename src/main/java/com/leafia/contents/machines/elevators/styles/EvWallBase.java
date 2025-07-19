package com.leafia.contents.machines.elevators.styles;

import com.leafia.contents.machines.elevators.EntityElevator.HitSrf;
import com.llib.math.FiaMatrix;

import java.util.ArrayList;
import java.util.List;

public class EvWallBase {
	public final int rotation;
	public EvWallBase(int rotation) {
		this.rotation = rotation;
	}
	public List<HitSrf> getHitSurfaces() {
		List<HitSrf> surfaces = new ArrayList<>();
		surfaces.add(new HitSrf(new FiaMatrix().translate(0,0,-19/16d),-15/16d,0,15/16d,36/16d,4d/16d).setType(0));
		return surfaces;
	}
}
