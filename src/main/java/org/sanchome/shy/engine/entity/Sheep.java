package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.ApplicationClient;
import org.sanchome.shy.engine.CollisionGroup;
import org.sanchome.shy.engine.UserSettings;
import org.sanchome.shy.engine.UserSettings.ShadowDetails;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class Sheep implements IEntity, IUpdatable, AnimEventListener {
	
	private static int SHEEP_ORDER = 0;
	
	private static final float WHEEL_RADIUS;
	private static final float WHEEL_Y_OFFSET;
	private static final float WHEEL_SUSPENSION_LENGTH;
	
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Node myLocalNode;
	private Spatial model;
	private Geometry model_geo;
	
	private VehicleControl model_phy;
	
	private AnimControl playerControl;
	private AnimChannel channel_nothing;
	
	static {
		WHEEL_RADIUS       = 0.3f;
		WHEEL_Y_OFFSET     = 0.0f;
		WHEEL_SUSPENSION_LENGTH = 2.0f;
	}
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
		
		myLocalNode = new Node("Sheep:"+SHEEP_ORDER++);
		rootNode.attachChild(myLocalNode);
		
		Vector3f initialPosition = ApplicationClient.getCurrentWorld().getRandomPosition(WHEEL_Y_OFFSET+WHEEL_RADIUS+WHEEL_SUSPENSION_LENGTH);
		myLocalNode.setLocalTranslation(initialPosition);
		
		model = assetManager.loadModel("models/blender/Sheep.mesh.xml" );
		Node node = (Node)model;
		model_geo = (Geometry) node.getChild("Sheep-geom-1");
		
		Matrix3f adjustOrientationX = Matrix3f.IDENTITY.clone();
		adjustOrientationX.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
		
		Matrix3f adjustOrientationZ = Matrix3f.IDENTITY.clone();
		adjustOrientationZ.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Z);
		model_geo.setLocalRotation(adjustOrientationX.mult(adjustOrientationZ));
		model_geo.setLocalScale(0.8f);
		model_geo.setLocalTranslation(new Vector3f(-0.3f, -0.1f, 0.1f));
		System.out.println("model:"+model_geo);
		
		playerControl = model.getControl(AnimControl.class);
		channel_nothing = playerControl.createChannel();
		playerControl.addListener(this);
		
		myLocalNode.attachChild(model);
		
		
		Matrix3f rot = Matrix3f.IDENTITY.clone();
		rot.fromStartEndVectors(Vector3f.UNIT_Y, ApplicationClient.getCurrentWorld().getNormalAt(initialPosition.x, initialPosition.z) );
		
		model.setLocalRotation(rot);
		
		CapsuleCollisionShape bcs = new CapsuleCollisionShape(1.5f, 2.5f, 0);
		
		//Try vehicule bodyControl
		float WHEEL_SIDE_OFFSET = 2.0f;//1.5f;
		float WHEEL_FRONT_OFFSET = 2.0f;//2.0f;
		model_phy = new VehicleControl(bcs, 50.0f);
		model_phy.setSuspensionCompression(0.0f);
		model_phy.addWheel(new Vector3f(WHEEL_FRONT_OFFSET,  -WHEEL_Y_OFFSET, WHEEL_SIDE_OFFSET),  Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, true);
		model_phy.addWheel(new Vector3f(WHEEL_FRONT_OFFSET,  -WHEEL_Y_OFFSET, -WHEEL_SIDE_OFFSET), Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, true);
		model_phy.addWheel(new Vector3f(-WHEEL_FRONT_OFFSET, -WHEEL_Y_OFFSET, WHEEL_SIDE_OFFSET),  Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, false);
		model_phy.addWheel(new Vector3f(-WHEEL_FRONT_OFFSET, -WHEEL_Y_OFFSET, -WHEEL_SIDE_OFFSET), Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, false);
		model.addControl(model_phy);
		//model_phy = new RigidBodyControl(bcs, 50.0f);
		model_phy.setCollisionGroup(CollisionGroup.SHEEP_BODY);
		model_phy.setCollideWithGroups(CollisionGroup.SHEEP_BODY_COLLISION_MASK);
		//model.addControl(model_phy);
		
		
		model.setUserData("PhysicsRigidBody", model_phy);
		
		
		model_phy.setPhysicsLocation(
			new Vector3f(
					initialPosition.x,
					initialPosition.y + 0.6f,
					initialPosition.z
			)
		);
		
		model.setShadowMode(UserSettings.SHADOW_DETAIL == ShadowDetails.FULL ? ShadowMode.CastAndReceive : ShadowMode.Cast);
		

		bulletAppState.getPhysicsSpace().add(model_phy);
		
		model_phy.setFriction(0.32f);
		
		motorEnabled = true;
		doNothing();
	}
	
	private boolean motorEnabled = false;
	
	public void update(float tpf) {
		if (motorEnabled) {
			//DSP Control
			float velocity = model_phy.getLinearVelocity().length();
			if(velocity < 5.0f) {
				model_phy.accelerate(100.0f);
				model_phy.brake(0.0f);
			} else {
				model_phy.accelerate(0.0f);
				model_phy.brake(1.0f);
			}
		} else {
			model_phy.brake(1.0f);
		}
	}
	
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// TODO Auto-generated method stub
		
	}
	
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		double rnd = Math.random();
		if ("nothing".equals(animName)) {
			if (rnd < 0.6)
				return; //reloop
			else if (0.6 <= rnd && rnd < 0.7)
				walk();
			else if (0.7 <= rnd && rnd < 0.8)
				bew();
			else if (0.8 <= rnd && rnd < 0.95)
				feed();
			else if (0.95 <= rnd)
				dance();
		} else if ("walk".equals(animName)) {
			if (rnd<0.8)
				return; //reloop
			else
				doNothing();
		} else if ("bew".equals(animName)) {
			if (rnd<0.2)
				return; //reloop
			else
				doNothing();
		} else if ("feed".equals(animName)) {
			if (rnd<0.5)
				return; //reloop
			else
				doNothing();
		} else if ("runaway".equals(animName)) {
			
		} else if ("dance".equals(animName)) {
			if (rnd<0.5)
				return; //reloop
			else
				doNothing();
		}
	}
	
	private void doNothing() {
		channel_nothing.setAnim("nothing", 0.8f);
		stopMotor();
	}
	
	private void walk() {
		channel_nothing.setAnim("walk", 0.8f);
		float radius = FastMath.HALF_PI / 2.0f;
		model_phy.steer(((float)Math.random()*radius)-(radius/2.0f));
		startMotor();
	}
	
	private void bew() {
		channel_nothing.setAnim("bew",0.8f);
		stopMotor();
	}
	
	private void feed() {
		channel_nothing.setAnim("feed",0.8f);
		stopMotor();
	}
	
	private void dance() {
		channel_nothing.setAnim("dance",0.8f);
		stopMotor();
	}
	
	private void startMotor() {
		model_phy.brake(0.0f);
		motorEnabled=true;
	}
	
	private void stopMotor() {
		model_phy.steer(0.0f);
		model_phy.accelerate(0.0f);
		motorEnabled=false;
	}
	
	public boolean isStabilized() {
		return model_phy.isEnabled() && !model_phy.isActive();
	}
	
	public void enableStabilization() {
		if (!model_phy.isEnabled()) {
			model_phy.setEnabled(true);
			model_phy.activate();
		}
	}
	
	public void detach() {
		rootNode.detachChild(model);
		bulletAppState.getPhysicsSpace().remove(model_phy);
	}
	public void restoreNormalPhysics() {
		model_phy.setAngularSleepingThreshold(1.0f);
		model_phy.setLinearSleepingThreshold(0.8f);
	}

}
