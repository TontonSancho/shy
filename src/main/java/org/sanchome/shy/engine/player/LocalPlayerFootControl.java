package org.sanchome.shy.engine.player;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class LocalPlayerFootControl extends RigidBodyControl implements PhysicsCollisionListener {
	
	private long lastTimetout;
	
	public LocalPlayerFootControl(BulletAppState bulletAppState, CapsuleCollisionShape footShape, float footWeight) {
		super(footShape, footWeight);
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
	}

	public void collision(PhysicsCollisionEvent collisionEvent) {
		Spatial toShoot = null;
		Vector3f localImpact = null;
		if (this.getUserObject() == collisionEvent.getNodeA() && collisionEvent.getNodeB().getName().startsWith("Sheep")) {
			toShoot = collisionEvent.getNodeB();
			localImpact = collisionEvent.getLocalPointB();
		}
		else if (this.getUserObject() == collisionEvent.getNodeA() && collisionEvent.getNodeA().getName().startsWith("Sheep")) {
			toShoot = collisionEvent.getNodeA();
			localImpact = collisionEvent.getLocalPointA();
		}
		if (toShoot!=null) {
			if (lastTimetout + 1000L > System.currentTimeMillis()) return;
			System.out.println("Shoot on:"+toShoot);
			RigidBodyControl rbc = toShoot.getUserData("RigidBodyControl");

			if (rbc==null) return;
			
			//if (this.getLinearVelocity().getY() < 1.0f) return;
			
			lastTimetout = System.currentTimeMillis();
			
			System.out.println("--------         getAppliedImpulse:"+collisionEvent.getAppliedImpulse());
			System.out.println("-------- getAppliedImpulseLateral1:"+collisionEvent.getAppliedImpulseLateral1());
			System.out.println("-------- getAppliedImpulseLateral2:"+collisionEvent.getAppliedImpulseLateral2());
			System.out.println("--------       getCombinedFriction:"+collisionEvent.getCombinedFriction());
			System.out.println("--------    getCombinedRestitution:"+collisionEvent.getCombinedRestitution());
			System.out.println("--------              getDistance1:"+collisionEvent.getDistance1());
			System.out.println("--------                 getIndex0:"+collisionEvent.getIndex0());
			System.out.println("--------                 getIndex1:"+collisionEvent.getIndex1());
			System.out.println("--------               getLifeTime:"+collisionEvent.getLifeTime());
			System.out.println("--------               localImpact:"+localImpact);
			System.out.println("--------            getLocalPointA:"+collisionEvent.getLocalPointA());
			System.out.println("--------            getLocalPointB:"+collisionEvent.getLocalPointB());
			System.out.println("--------         getLinearVelocity:"+this.getLinearVelocity());
			
			
			
			rbc.applyImpulse(
					new Vector3f(
							this.getLinearVelocity().x,
							500.0f,
							this.getLinearVelocity().z)
					.normalizeLocal()
					.multLocal(2000.0f)
				, localImpact);
			
		}
	}
}
