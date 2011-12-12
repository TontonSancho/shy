package org.sanchome.shy.engine.world;

import org.sanchome.shy.engine.IInitable;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public interface IWorld extends IInitable {
	
	Vector3f getWorldMin();
	Vector3f getWorldMax();
	
	float getHeightAt(Vector2f queryXZ);
	float getHeightAt(Vector2f queryXZ, float yOffset);
	float getHeightAt(float queryX, float queryZ);
	float getHeightAt(float queryX, float queryZ, float yOffset);
	
	Vector3f getNormalAt(Vector2f queryXZ);
	Vector3f getNormalAt(float queryX, float queryZ);
	
	Vector3f getRandomPosition();
	Vector3f getRandomPosition(float yOffset);
}
