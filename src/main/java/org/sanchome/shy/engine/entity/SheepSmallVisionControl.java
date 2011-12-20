package org.sanchome.shy.engine.entity;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.FixedConeCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SheepSmallVisionControl extends GhostControl implements PhysicsCollisionListener {

	private Sheep sheep;
	private VehicleControl vehicleControl;
	
	public SheepSmallVisionControl(Sheep sheep, VehicleControl vehicleControl, FixedConeCollisionShape visionShape, BulletAppState bulletAppState) {
		super(visionShape);
		this.sheep = sheep;
		this.vehicleControl = vehicleControl;
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
	}

	public void collision(PhysicsCollisionEvent collisionEvent) {
		if (this != collisionEvent.getObjectA() && this != collisionEvent.getObjectB() || (vehicleControl == collisionEvent.getObjectA() || vehicleControl == collisionEvent.getObjectB())) return;
		Spatial theThingToEsquive;
		if (this == collisionEvent.getObjectA())
			theThingToEsquive = collisionEvent.getNodeB();
		else
			theThingToEsquive = collisionEvent.getNodeA();
		
		Vector3f velocyDir = vehicleControl.getLinearVelocity().setY(0.0f).normalizeLocal();
		Vector2f velocyDir2D = new Vector2f(velocyDir.x, velocyDir.z);
		Vector3f theThingDir = theThingToEsquive.getWorldTranslation().subtract(vehicleControl.getPhysicsLocation()).setY(0.0f).normalizeLocal();
		Vector2f theThingDir2D = new Vector2f(theThingDir.x, theThingDir.z);
		float alpha = velocyDir2D.angleBetween(theThingDir2D);
		
		float steerAlpha = FastMath.clamp(FastMath.QUARTER_PI / alpha, -FastMath.HALF_PI / 4.0f, FastMath.HALF_PI / 4.0f);
		vehicleControl.steer(steerAlpha);
		System.out.println("Sheep vision: "+sheep);
		System.out.println("   \\---         alpha: "+alpha);
		System.out.println("   \\---    steerAlpha: "+steerAlpha);
		System.out.println("   \\--- getUserObject: "+this.getUserObject());
		System.out.println("   \\---    getObjectA: "+collisionEvent.getObjectA());
		System.out.println("   \\---    getObjectB: "+collisionEvent.getObjectB());
		System.out.println("   \\---      getNodeA: "+collisionEvent.getNodeA());
		System.out.println("   \\---      getNodeB: "+collisionEvent.getNodeB());
		System.out.println("   \\---      woirld A:"+collisionEvent.getNodeA().getWorldTranslation());
		System.out.println("   \\---      woirld B:"+collisionEvent.getNodeB().getWorldTranslation());
	}

}
