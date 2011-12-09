package org.sanchome.shy.engine.player;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;

public class LocalPlayerFootControl extends RigidBodyControl implements PhysicsCollisionListener {
	
	public LocalPlayerFootControl(BulletAppState bulletAppState, CapsuleCollisionShape footShape, float footWeight) {
		super(footShape, footWeight);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
	}

	public void collision(PhysicsCollisionEvent collisionEvent) {
		if ("Player:Node".equals(collisionEvent.getNodeA().getName()) || "Player:Node".equals(collisionEvent.getNodeB().getName()))
		{
			System.out.println("Collision between:" + collisionEvent.getNodeA());
			System.out.println("              and:" + collisionEvent.getNodeB());
		}
	}
}
