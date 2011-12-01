package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.Application;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class Sheep implements IEntity, IUpdatable {

	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Geometry model_geo;
	
	private static final Box BOX;
	private static Material wall_mat;
	
	private RigidBodyControl model_phy;
	private RigidBodyControl model_phy_frontWheel;
	private RigidBodyControl model_phy_rearWheel;
	private HingeJoint frontJoint;
	private HingeJoint rearJoint;
	
	static {
		BOX = new Box(Vector3f.ZERO, 1.0f, 1.0f, 1.0f);
		BOX.scaleTextureCoordinates(new Vector2f(1.0f, 1.0f));
	}
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
		
		if (wall_mat == null) {
			wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		    TextureKey key = new TextureKey("textures/whool.jpg");
		    key.setGenerateMips(true);
		    Texture tex = assetManager.loadTexture(key);
		    wall_mat.setTexture("ColorMap", tex);
		}
		
		model_geo = new Geometry("sheep", BOX);
		
		model_geo.setMaterial(wall_mat);
		rootNode.attachChild(model_geo);
		
		float initialPositionX = -20.0f;//(float)(500.0*Math.random())-250.0f;
		float initialPositionZ = -5.0f;//(float)(500.0*Math.random())-250.0f;
		
		model_geo.setLocalTranslation(
				new Vector3f(
						initialPositionX,
						Application.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, 1.0f),
						initialPositionZ
				)
		);
		
		
		BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(2.0f, 1.0f, 1.4f));
		model_phy = new RigidBodyControl(bcs, 10.0f);
		model_geo.addControl(model_phy);
		model_geo.setUserData("RigidBodyControl", model_phy);
		bulletAppState.getPhysicsSpace().add(model_phy);
		model_phy.setFriction(50.0f);
		
		Node frontWheelNode = new Node("frontWheelNode");
		CapsuleCollisionShape frontWheel = new CapsuleCollisionShape(0.3f, 2.8f, 2);
		model_phy_frontWheel = new RigidBodyControl(frontWheel, .1f);
		frontWheelNode.addControl(model_phy_frontWheel);
		rootNode.attachChild(frontWheelNode);
		bulletAppState.getPhysicsSpace().add(model_phy_frontWheel);
		model_phy_frontWheel.setFriction(50.0f);
		
		Node rearWheelNode = new Node("rearWheelNode");
		CapsuleCollisionShape rearWheel  = new CapsuleCollisionShape(0.3f, 2.8f, 2);
		model_phy_rearWheel = new RigidBodyControl(rearWheel, .1f);
		rearWheelNode.addControl(model_phy_rearWheel);
		rootNode.attachChild(rearWheelNode);
		bulletAppState.getPhysicsSpace().add(model_phy_rearWheel);
		model_phy_rearWheel.setFriction(50.0f);
		
		frontJoint = new HingeJoint(model_phy, model_phy_frontWheel, new Vector3f(1.1f, -1.4f, 0.0f), Vector3f.ZERO, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
		frontJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(frontJoint);
		rearJoint =  new HingeJoint(model_phy, model_phy_rearWheel, new Vector3f(-1.7f, -1.4f, 0.0f), Vector3f.ZERO, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
		bulletAppState.getPhysicsSpace().add(rearJoint);
		rearJoint.setCollisionBetweenLinkedBodys(false);
		
		model_geo.setShadowMode(ShadowMode.Cast);
	}
	
	public void update(float tpf) {
		//model_phy_frontWheel.setAngularVelocity(Vector3f.ZERO);
		//model_phy_rearWheel.setAngularVelocity(Vector3f.ZERO);
		//rearJoint.
		
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
		rootNode.detachChild(model_geo);
		bulletAppState.getPhysicsSpace().remove(model_phy);
	}
	public void restoreNormalPhysics() {
		model_phy.setAngularSleepingThreshold(1.0f);
		model_phy.setLinearSleepingThreshold(0.8f);
	}

}
