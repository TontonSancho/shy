package org.sanchome.shy.engine.entity;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.FixedConeCollisionShape;
import com.jme3.bullet.control.GhostControl;

public class SheepSmallVisionControl extends GhostControl implements PhysicsCollisionListener {

	private Sheep sheep;
	
	public SheepSmallVisionControl(Sheep sheep, FixedConeCollisionShape visionShape, BulletAppState bulletAppState) {
		super(visionShape);
		this.sheep = sheep;
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
	}

	public void collision(PhysicsCollisionEvent event) {
		System.out.println("Sheep:"+sheep+" Vision collide:"+event);
		
	}

}
