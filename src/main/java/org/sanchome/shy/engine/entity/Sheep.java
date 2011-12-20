package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.ApplicationClient;
import org.sanchome.shy.engine.CollisionGroup;
import org.sanchome.shy.engine.UserSettings;
import org.sanchome.shy.engine.UserSettings.ShadowDetails;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.ConeCollisionShape;
import com.jme3.bullet.collision.shapes.FixedConeCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class Sheep implements IEntity, IUpdatable, AnimEventListener {
	
	public static enum State {
		DO_NOTHING,
		WALK,
		BEW,
		FEED,
		RUNAWAY,
		DANCE,
		FLY,
		RUN
	};
	
	private static int SHEEP_ORDER = 0;
	
	private static final float WHEEL_RADIUS;
	private static final float WHEEL_Y_OFFSET;
	private static final float WHEEL_SUSPENSION_LENGTH;
	
	private State state;
	private static final float WALK_MAX_VELOCITY     = 5.0f;
	private static final float RUN_MAX_VELOCITY      = 10.0f;
	private static final float RUNAWAY_MAX_VELOCITY  = 30.0f;
	private float max_velocity_regulator = WALK_MAX_VELOCITY;
	private boolean motorEnabled = false;
	
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Node myLocalNode;
	private Spatial model;
	private Geometry model_geo;
	
	private float mass_offset_y = 1.0f;
	private VehicleControl model_phy;
	
	private AnimControl animControl;
	private AnimChannel animChannel;
	
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
		
		Node secondNode = new Node(myLocalNode.getName()+":Second:Node");
		myLocalNode.attachChild(secondNode);
		
		Node thirdNode = new Node(secondNode.getName()+":Third:Node");
		secondNode.attachChild(thirdNode);
		
		model = assetManager.loadModel("models/blender/Sheep.mesh.xml" );
		thirdNode.attachChild(model);
		Node node = (Node)model;
		model_geo = (Geometry) node.getChild("Sheep-geom-1");
		
		Matrix3f adjustOrientationX = Matrix3f.IDENTITY.clone();
		adjustOrientationX.fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
		
		Matrix3f adjustOrientationZ = Matrix3f.IDENTITY.clone();
		adjustOrientationZ.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Z);
		thirdNode.setLocalRotation(adjustOrientationX.mult(adjustOrientationZ));
		thirdNode.setLocalScale(0.8f);
		thirdNode.setLocalTranslation(new Vector3f(-0.3f, -0.1f + mass_offset_y, 0.1f));
		
		animControl = model.getControl(AnimControl.class);
		animChannel = animControl.createChannel();
		animControl.addListener(this);
		
		myLocalNode.attachChild(secondNode);
		
		
		Matrix3f rot = Matrix3f.IDENTITY.clone();
		rot.fromStartEndVectors(Vector3f.UNIT_Y, ApplicationClient.getCurrentWorld().getNormalAt(initialPosition.x, initialPosition.z) );
		
		secondNode.setLocalRotation(rot);
		
		CapsuleCollisionShape bcs = new CapsuleCollisionShape(1.5f, 2.5f, 0);
		
		CompoundCollisionShape ccs  = new CompoundCollisionShape();
		ccs.addChildShape(bcs, new Vector3f(0.0f, mass_offset_y, 0.0f));
		
		//Try vehicule bodyControl
		float WHEEL_SIDE_OFFSET = 2.0f;//1.5f;
		float WHEEL_FRONT_OFFSET = 2.0f;//2.0f;
		model_phy = new VehicleControl(ccs, 100.0f);
		model_phy.setSuspensionCompression(0.0f);
		model_phy.addWheel(new Vector3f(WHEEL_FRONT_OFFSET,  -WHEEL_Y_OFFSET + mass_offset_y, WHEEL_SIDE_OFFSET),  Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, true);
		model_phy.addWheel(new Vector3f(WHEEL_FRONT_OFFSET,  -WHEEL_Y_OFFSET + mass_offset_y, -WHEEL_SIDE_OFFSET), Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, true);
		model_phy.addWheel(new Vector3f(-WHEEL_FRONT_OFFSET, -WHEEL_Y_OFFSET + mass_offset_y, WHEEL_SIDE_OFFSET),  Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, false);
		model_phy.addWheel(new Vector3f(-WHEEL_FRONT_OFFSET, -WHEEL_Y_OFFSET + mass_offset_y, -WHEEL_SIDE_OFFSET), Vector3f.UNIT_Y.negate(),  Vector3f.UNIT_Z, WHEEL_SUSPENSION_LENGTH, WHEEL_RADIUS, false);
		secondNode.addControl(model_phy);
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
		
		state = State.DO_NOTHING;
		doNothing();
		
		// Sheep Vision
		
		float visionCodeHeight = 30.0f;
		FixedConeCollisionShape visionShape = new FixedConeCollisionShape(visionCodeHeight*0.8f, visionCodeHeight, 1);
		GhostControl visionControl = new GhostControl(visionShape);
		Node visionNode = new Node(myLocalNode.getName()+":Vision:Node");
		visionControl.setCollisionGroup(0x00000000);
		visionControl.setCollideWithGroups(0x00000000);
		visionControl.setSpatial(visionNode);
		visionNode.addControl(visionControl);
		Quaternion visionRotation = Quaternion.IDENTITY.clone().fromAngleNormalAxis(FastMath.PI+0.7f, Vector3f.UNIT_X);
		//Quaternion rot2 = Quaternion.IDENTITY.clone().fromAngleNormalAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Z);
		visionNode.setLocalRotation(visionRotation);
		visionNode.setLocalTranslation(-0.0f, visionCodeHeight/2.0f, 12.0f);
		
		/* FOR TEST PURPOSE ONLY */
		Sphere sphere = new Sphere(30, 30, 1.0f);
		Geometry mark = new Geometry("Vision", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Blue);
		mark.setMaterial(mark_mat);
		//visionNode.attachChild(mark);
		
		bulletAppState.getPhysicsSpace().add(visionControl);
		
		Bone headBone = animControl.getSkeleton().getBone("tete");
		Node headNode = headBone.getAttachmentsNode();
		thirdNode.attachChild(headNode);
		headNode.attachChild(visionNode);
		
		// Smaller vision cone
		FixedConeCollisionShape vision2Shape = new FixedConeCollisionShape(1.0f, visionCodeHeight, 1);
		SheepSmallVisionControl vision2Control = new SheepSmallVisionControl(this, model_phy, vision2Shape, bulletAppState);
		vision2Control.setCollisionGroup(0x00000000);
		vision2Control.setCollideWithGroups(0x00000000 | CollisionGroup.CRATES | CollisionGroup.FENCES | CollisionGroup.TREES | CollisionGroup.PLAYER_CAPSULE | CollisionGroup.SHEEP_BODY);
		vision2Control.setSpatial(visionNode);
		visionNode.addControl(vision2Control);
		
		bulletAppState.getPhysicsSpace().add(vision2Control);
	}
	
	public void update(float tpf) {
		if (motorEnabled) {
			//DSP Control
			float velocity = model_phy.getLinearVelocity().length();
			animChannel.setSpeed(velocity/7.5f);
			if(velocity < max_velocity_regulator) {
				model_phy.accelerate(150.0f);
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
		if (State.DO_NOTHING == state) {
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
			run();
		} else if (State.WALK == state) {
			if (rnd<0.8)
				return; //reloop
			else
				doNothing();
		} else if (State.BEW == state) {
			if (rnd<0.2)
				return; //reloop
			else
				doNothing();
		} else if (State.FEED == state) {
			if (rnd<0.5)
				return; //reloop
			else
				doNothing();
		} else if (State.RUNAWAY == state) {
			
		} else if (State.DANCE == state) {
			if (rnd<0.5)
				return; //reloop
			else
				doNothing();
		} else if (State.RUN == state) {
			if (rnd<0.5) model_phy.steer(0.0f);
			if (rnd<1.0)
				return; //reloop
			else // Actually, never stop
				doNothing();
		}
	}
	
	private void doNothing() {
		state = State.DO_NOTHING;
		// Set anim
		animChannel.setAnim("nothing", 0.8f);
		animChannel.setSpeed(1.0f);
		// Stop
		stopMotor();
	}
	
	private void walk() {
		state = State.WALK;
		// Set anim
		animChannel.setAnim("walk", 0.8f);
		animChannel.setSpeed(1.0f);
		// Set direction
		//float radius = FastMath.HALF_PI / 2.0f;
		//model_phy.steer(((float)Math.random()*radius)-(radius/2.0f));
		// Set speed regulation
		max_velocity_regulator = WALK_MAX_VELOCITY;
		// Go
		startMotor();
	}
	
	private void bew() {
		state = State.BEW;
		// Set anim
		animChannel.setAnim("bew",0.8f);
		animChannel.setSpeed(1.0f);
		stopMotor();
	}
	
	private void feed() {
		state = State.FEED;
		// Set anim
		animChannel.setAnim("feed",0.8f);
		animChannel.setSpeed(1.0f);
		stopMotor();
	}
	
	private void dance() {
		state = State.DANCE;
		// Set anim
		animChannel.setAnim("dance",0.8f);
		animChannel.setSpeed(1.0f);
		stopMotor();
	}
	
	private void run() {
		state = State.RUN;
		// Set anim
		animChannel.setAnim("walk", 0.8f);
		animChannel.setSpeed(1.0f);
		// Set speed regulation
		max_velocity_regulator = RUN_MAX_VELOCITY;
		// Go
		startMotor();
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
	
	@Override
	public String toString() {
		return myLocalNode.getName();
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
