package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.ApplicationClient;
import org.sanchome.shy.engine.CollisionGroup;
import org.sanchome.shy.engine.UserSettings;
import org.sanchome.shy.engine.UserSettings.ShadowDetails;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
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
import com.jme3.texture.Texture;

public class Sheep implements IEntity, IUpdatable {
	
	private static int SHEEP_ORDER = 0;
	private static final Box BOX;
	private static Material wall_mat;
	
	private static final Vector3f WHEEL_SIZE;
	private static final float    WHEEL_Z_OFFSET = 1.1f;
	private static final Vector3f FRONT_WHEEL_OFFSET;
	private static final Vector3f REAR_WHEEL_OFFSET;
	
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Node myLocalNode;
	private Spatial model;
	private Geometry model_geo;
	
	private RigidBodyControl model_phy;
	private RigidBodyControl model_phy_frontWheel;
	private RigidBodyControl model_phy_rearWheel;
	private HingeJoint frontJoint;
	private HingeJoint rearJoint;
	
	static {
		BOX = new Box(Vector3f.ZERO, 1.0f, 1.0f, 1.0f);
		BOX.scaleTextureCoordinates(new Vector2f(1.0f, 1.0f));
		
		WHEEL_SIZE = new Vector3f(0.6f, 2.8f, 1.4f);
		FRONT_WHEEL_OFFSET = new Vector3f(1.1f, -WHEEL_Z_OFFSET, 0.0f);
		REAR_WHEEL_OFFSET  = new Vector3f(-1.7f, -WHEEL_Z_OFFSET, 0.0f);
	}
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
		
		myLocalNode = new Node("Sheep:"+SHEEP_ORDER++);
		rootNode.attachChild(myLocalNode);
		
		Vector3f initialPosition = ApplicationClient.getCurrentWorld().getRandomPosition(WHEEL_Z_OFFSET);
		myLocalNode.setLocalTranslation(initialPosition);

//		Vector3f worldSize = ApplicationClient.getCurrentWorld().getWorldMax().subtractLocal(ApplicationClient.getCurrentWorld().getWorldMin()).multLocal(0.95f);
//		float initialPositionX = (float)(worldSize.x*Math.random())-(worldSize.x/2f);
//		float initialPositionZ = (float)(worldSize.z*Math.random())-(worldSize.z/2f);
		
//		myLocalNode.setLocalTranslation(
//				new Vector3f(
//						initialPositionX,
//						ApplicationClient.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, 1.0f),
//						initialPositionZ
//				)
//		);
		
		model = assetManager.loadModel("models/blender/Sheep.mesh.xml" );
		Node node = (Node)model;
		model_geo = (Geometry) node.getChild("Sheep-geom-1");
		Matrix3f adjustOrientationX = Matrix3f.IDENTITY.clone();
		adjustOrientationX.fromAngleAxis(FastMath.PI, Vector3f.UNIT_X);
		Matrix3f adjustOrientationY = Matrix3f.IDENTITY.clone();
		adjustOrientationY.fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y);
		model_geo.setLocalRotation(adjustOrientationX.mult(adjustOrientationY));
		model_geo.setLocalScale(0.8f);
		model_geo.setLocalTranslation(new Vector3f(-0.3f, -1.7f, -0.2f));
		System.out.println("model:"+model_geo);
		
		/*
		if (wall_mat == null) {
			wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		    TextureKey key = new TextureKey("textures/whool.jpg");
		    key.setGenerateMips(true);
		    Texture tex = assetManager.loadTexture(key);
		    wall_mat.setTexture("ColorMap", tex);
		}
		*/
		/* TODO: Clean
		model_geo = new Geometry(myLocalNode.getName()+":Geometry", BOX);
		*/
		/*
		model_geo.setMaterial(wall_mat);
		*/
		myLocalNode.attachChild(model);
		
		Matrix3f rot = Matrix3f.IDENTITY.clone();
		rot.fromStartEndVectors(Vector3f.UNIT_Y, ApplicationClient.getCurrentWorld().getNormalAt(initialPosition.x, initialPosition.z) );
		
		model.setLocalRotation(rot);
		
		//BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(2.0f, 1.0f, 1.4f));
		CapsuleCollisionShape bcs = new CapsuleCollisionShape(1.5f, 2.5f, 0);
		model_phy = new RigidBodyControl(bcs, 50.0f);
		model_phy.setCollisionGroup(CollisionGroup.SHEEP_BODY);
		model_phy.setCollideWithGroups(CollisionGroup.SHEEP_BODY_COLLISION_MASK);
		
		model.addControl(model_phy);
		model.setUserData("RigidBodyControl", model_phy);
		//bulletAppState.getPhysicsSpace().add(model_phy);
		//model_phy.setFriction(5.0f);
		
		model_phy.setPhysicsLocation(
			new Vector3f(
					initialPosition.x,
					initialPosition.y + 0.6f,
					initialPosition.z
			)
		);
		
		Node frontWheelNode = new Node(myLocalNode.getName()+":Node:FrontWheel");
		CylinderCollisionShape frontWheel = new CylinderCollisionShape(WHEEL_SIZE, 2);
		model_phy_frontWheel = new RigidBodyControl(frontWheel, .50f);
		model_phy_frontWheel.setCollisionGroup(CollisionGroup.SHEEP_WHEELS);
		model_phy_frontWheel.setCollideWithGroups(CollisionGroup.SHEEP_WHEELS_COLLISION_MASK);
		
		frontWheelNode.addControl(model_phy_frontWheel);
		rootNode.attachChild(frontWheelNode);
		//bulletAppState.getPhysicsSpace().add(model_phy_frontWheel);
		//model_phy_frontWheel.setFriction(50.0f);
		
		model_phy_frontWheel.setPhysicsLocation(
			model_phy.getPhysicsLocation().add(FRONT_WHEEL_OFFSET)
		);
		
		Node rearWheelNode = new Node(myLocalNode.getName()+":Node:RearWheel");
		CylinderCollisionShape rearWheel  = new CylinderCollisionShape(WHEEL_SIZE, 2);
		model_phy_rearWheel = new RigidBodyControl(rearWheel, .50f);
		model_phy_rearWheel.setCollisionGroup(CollisionGroup.SHEEP_WHEELS);
		model_phy_rearWheel.setCollideWithGroups(CollisionGroup.SHEEP_WHEELS_COLLISION_MASK);
		
		rearWheelNode.addControl(model_phy_rearWheel);
		rootNode.attachChild(rearWheelNode);
		//bulletAppState.getPhysicsSpace().add(model_phy_rearWheel);
		//model_phy_rearWheel.setFriction(50.0f);
		
		model_phy_rearWheel.setPhysicsLocation(
			model_phy.getPhysicsLocation().add(REAR_WHEEL_OFFSET)
		);
		
		frontJoint = new HingeJoint(model_phy, model_phy_frontWheel, FRONT_WHEEL_OFFSET, Vector3f.ZERO, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
		frontJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(frontJoint);
		
		rearJoint =  new HingeJoint(model_phy, model_phy_rearWheel, REAR_WHEEL_OFFSET, Vector3f.ZERO, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
		rearJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(rearJoint);
		
		
		model.setShadowMode(UserSettings.SHADOW_DETAIL == ShadowDetails.FULL ? ShadowMode.CastAndReceive : ShadowMode.Cast);
		

		bulletAppState.getPhysicsSpace().add(model_phy);
		bulletAppState.getPhysicsSpace().add(model_phy_frontWheel);
		bulletAppState.getPhysicsSpace().add(model_phy_rearWheel);

		model_phy.setFriction(5.0f);
		model_phy_frontWheel.setFriction(5.0f);
		model_phy_rearWheel.setFriction(5.0f);
		
		// For debug purpose
		/*
		model_phy.setKinematic(true);
		model_phy_frontWheel.setKinematic(true);
		model_phy_rearWheel.setKinematic(true);
		*/
		
	}
	
	private boolean motorEnabled = false;
	
	public void update(float tpf) {
		//model_phy_frontWheel.setAngularVelocity(Vector3f.ZERO);
		//model_phy_rearWheel.setAngularVelocity(Vector3f.ZERO);
		if (Math.random()>0.999) {
			motorEnabled = !motorEnabled;
		}
		if (motorEnabled) {
			frontJoint.enableMotor(true, 10.0f, 0.1f);
			rearJoint.enableMotor(true, 10.0f, 0.1f);
		} else {
			frontJoint.enableMotor(false, 10.0f, 0.2f);
			rearJoint.enableMotor(false, 10.0f, 0.2f);
			model_phy_frontWheel.setAngularVelocity(Vector3f.ZERO);
			model_phy_rearWheel.setAngularVelocity(Vector3f.ZERO);
		}
		
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
