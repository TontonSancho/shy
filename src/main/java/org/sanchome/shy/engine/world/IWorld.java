package org.sanchome.shy.engine.world;

import org.sanchome.shy.engine.IInitable;

import com.jme3.math.Vector2f;

public interface IWorld extends IInitable {
	
	float getHeightAt(Vector2f queryXZ);
	float getHeightAt(Vector2f queryXZ, float yOffset);
	float getHeightAt(float queryX, float queryZ);
	float getHeightAt(float queryX, float queryZ, float yOffset);
	
}
