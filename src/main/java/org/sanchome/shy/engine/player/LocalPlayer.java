package org.sanchome.shy.engine.player;

import org.sanchome.shy.engine.ApplicationClient;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

public class LocalPlayer implements IPlayer {

	private Node rootNode;
	private Camera camera;
	private Node playerNode;
	private CharacterControl player;
	private Geometry mark;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;
	
	private Node hipNode;
	private RigidBodyControl hipBody;
	private Node footNode;
	
	
	private Vector3f hipOffsetPosition  = new Vector3f(10.0f, 0.0f, 1.0f);
	private Vector3f footOffsetPosition = new Vector3f();

	private float hipHalfSize = 0.2f;
	private float legPartRadius = 0.1f;
	private float legPartLength = 0.5f;
	private float legPartWeight = 7.0f;
	private float footRadius = 0.15f;
	private float footLength = 0.25f;
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.camera = camera;
		this.rootNode = rootNode;

		playerNode = new Node("Player-Node");
		
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
		player = new CharacterControl(capsuleShape, 3.0f);
		player.setCollisionGroup(0);
		player.setJumpSpeed(20);
		player.setFallSpeed(30);
		player.setGravity(30);
		player.setPhysicsLocation(new Vector3f(0.0f, ApplicationClient.getCurrentWorld().getHeightAt(0.0f, 0.0f, 3.1f), 0.0f));
		playerNode.addControl(player);
		rootNode.attachChild(playerNode);
		
		bulletAppState.getPhysicsSpace().add(player);
		
		// Hanche (fixe)
		hipNode = new Node("Dummy-Node");
		BoxCollisionShape dummyShape = new BoxCollisionShape(new Vector3f(hipHalfSize, hipHalfSize, hipHalfSize));
		hipBody = new RigidBodyControl(dummyShape, 0.0f);
		hipBody.setCollisionGroup(0);
		hipBody.setKinematic(true);
		hipNode.addControl(hipBody);
		playerNode.attachChild(hipNode);
		hipNode.setLocalTranslation(hipOffsetPosition);
		//hipBody.setPhysicsLocation(hipOffsetPosition/*player.getPhysicsLocation().add(hipOffsetPosition)*/);
		bulletAppState.getPhysicsSpace().add(hipBody);

		// Top Leg (rotule)
		Node legTopNode = new Node("LegTop-Node");
		CapsuleCollisionShape legTopShape = new CapsuleCollisionShape(legPartRadius, legPartLength, 0);
		RigidBodyControl legTopBody = new RigidBodyControl(legTopShape, legPartWeight);
		legTopBody.setCollisionGroup(0);
		legTopNode.addControl(legTopBody);
		playerNode.attachChild(legTopNode);
		legTopNode.setLocalTranslation(hipNode.getLocalTranslation().add(new Vector3f(0.0f, -hipHalfSize, 0.0f)));
		//legTopBody.setPhysicsLocation(hipBody.getPhysicsLocation().add(new Vector3f(0.0f, -hipHalfSize, 0.0f)));
		//hipNode.attachChild(legTopNode);
		//legTopNode.setLocalTranslation(new Vector3f(0.0f, -hipHalfSize, 0.0f));
		bulletAppState.getPhysicsSpace().add(legTopBody);
		
		ConeJoint legTopJoint = new ConeJoint(hipBody, legTopBody, Vector3f.UNIT_Y.negate().multLocal(hipHalfSize), Vector3f.UNIT_X.mult(legPartLength/2.0f));
		legTopJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(legTopJoint);
		
		// Bottom Leg (rotule)
		Node legBottomNode = new Node("LegBottom-Node");
		CapsuleCollisionShape legBottomShape = new CapsuleCollisionShape(legPartRadius, legPartLength, 0);
		RigidBodyControl legBottomBody = new RigidBodyControl(legBottomShape, legPartWeight);
		legBottomBody.setCollisionGroup(0);
		legBottomNode.addControl(legBottomBody);
		playerNode.attachChild(legBottomNode);
		legBottomNode.setLocalTranslation(legTopNode.getLocalTranslation().add(new Vector3f(0.0f, -legPartLength, 0.0f)));
		//legBottomBody.setPhysicsLocation(legTopBody.getPhysicsLocation().add(new Vector3f(0.0f, -legPartLength, 0.0f)));
		//legTopNode.attachChild(legBottomNode);
		//legBottomNode.setLocalTranslation(new Vector3f(0.0f, -legPartLength, 0.0f));
		bulletAppState.getPhysicsSpace().add(legBottomBody);
		
		ConeJoint legBottomJoint = new ConeJoint(legTopBody, legBottomBody, Vector3f.UNIT_X.negate().multLocal(legPartLength/2.0f), Vector3f.UNIT_X.mult(legPartLength/2.0f));
		legBottomJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(legBottomJoint);
		
		// Bottom Leg (rotule)
		footNode = new Node("Foot-Node");
		CapsuleCollisionShape footShape = new CapsuleCollisionShape(footRadius, footLength, 0);
		RigidBodyControl footBody = new RigidBodyControl(footShape, 0.0f);
		footBody.setCollisionGroup(0);
		footBody.setKinematic(true);
		footNode.addControl(footBody);
		playerNode.attachChild(footNode);
		footNode.setLocalTranslation(legBottomNode.getLocalTranslation().add(new Vector3f(footLength/2.0f, -legPartLength, 0.0f)));
		//footBody.setPhysicsLocation(legBottomBody.getPhysicsLocation().add(new Vector3f(footLength/2.0f, -legPartLength, 0.0f)));
		//legBottomNode.attachChild(footNode);
		//footNode.setLocalTranslation(new Vector3f(footLength/2.0f, -legPartLength, 0.0f));
		bulletAppState.getPhysicsSpace().add(footBody);
		
		ConeJoint footJoint = new ConeJoint(legBottomBody, footBody, Vector3f.UNIT_X.negate().multLocal(legPartLength/2.0f), Vector3f.UNIT_X.negate().multLocal(footLength/2.0f));
		footJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(footJoint);

		footOffsetPosition = footBody.getPhysicsLocation().subtract(hipBody.getPhysicsLocation());
		
		
		// A Shoot Mark
		Sphere sphere = new Sphere(30, 30, 0.2f);
		mark = new Geometry("BOOM!", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
		
		
	}

	public void simpleUpdate(float tpf) {
		Vector3f camDir = camera.getDirection().clone().multLocal(0.6f);
		Vector3f camLeft = camera.getLeft().clone().multLocal(0.4f);
		walkDirection.set(0, 0, 0);
		if (left) {
			walkDirection.addLocal(camLeft);
		}
		if (right) {
			walkDirection.addLocal(camLeft.negate());
		}
		if (up) {
			walkDirection.addLocal(camDir);
		}
		if (down) {
			walkDirection.addLocal(camDir.negate());
		}
		player.setWalkDirection(walkDirection);
		camera.setLocation(player.getPhysicsLocation());
		playerNode.lookAt(walkDirection, Vector3f.UNIT_Y);
		//dummyBody.setPhysicsLocation(player.getPhysicsLocation().add(dummyOffsetPosition));
		//hipNode.setLocalTranslation(player.getPhysicsLocation().add(hipOffsetPosition));
		//footNode.setLocalTranslation(player.getPhysicsLocation().add(hipOffsetPosition).addLocal(footOffsetPosition));
	}

	public void onAction(String binding, boolean isPressed, float tpf) {

		if ("Left".equals(binding)) {
			if (isPressed) {
				left = true;
			} else {
				left = false;
			}
		} else if ("Right".equals(binding)) {
			if (isPressed) {
				right = true;
			} else {
				right = false;
			}
		} else if ("Forward".equals(binding)) {
			if (isPressed) {
				up = true;
			} else {
				up = false;
			}
		} else if ("Backward".equals(binding)) {
			if (isPressed) {
				down = true;
			} else {
				down = false;
			}
		} else if ("Jump".equals(binding)) {
			player.jump();
		} else if (("Shoot").equals(binding)) {
			if (!isPressed) {
				CollisionResults results = new CollisionResults();
				Ray ray = new Ray(camera.getLocation(), camera.getDirection());
				rootNode.collideWith(ray, results);
				if (results.size() > 0) {
					CollisionResult closest = results.getClosestCollision();
					RigidBodyControl rbc = closest.getGeometry().getParent().getUserData("RigidBodyControl");
					if (rbc==null)
						rbc = closest.getGeometry().getUserData("RigidBodyControl");
					if(rbc!=null) {
						System.out.println("rbc       :"+rbc);
						if(!rbc.isEnabled()) rbc.setEnabled(true);
						rbc.activate();
						rbc.applyImpulse(camera.getDirection().mult(1000.0f), camera.getLocation().subtract(rbc.getPhysicsLocation()));
					}
					mark.setLocalTranslation(closest.getContactPoint());
					rootNode.attachChild(mark);
					
				} else {
					rootNode.detachChild(mark);
				}
			}
		}
	}
}
