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
	private CharacterControl player;
	private Geometry mark;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;
	
	private Node dummyNode;
	private RigidBodyControl dummyBody;
	private Vector3f dummyOffsetPosition = new Vector3f(-5.0f, 0.0f, -5.0f);

	private float hanche;
	private float legPartRadius = 0.1f;
	private float legPartLength = 0.5f;
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.camera = camera;
		this.rootNode = rootNode;

		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
		player = new CharacterControl(capsuleShape, 3.0f);
		player.setJumpSpeed(20);
		player.setFallSpeed(30);
		player.setGravity(30);
		player.setPhysicsLocation(new Vector3f(0.0f, ApplicationClient.getCurrentWorld().getHeightAt(0.0f, 0.0f, 3.1f), 0.0f));
				
		bulletAppState.getPhysicsSpace().add(player);

		// Hanche (fixe)
		dummyNode = new Node("Dummy-Node");
		BoxCollisionShape dummyShape = new BoxCollisionShape(new Vector3f(0.2f, 0.2f, 0.2f));
		dummyBody = new RigidBodyControl(dummyShape, 0.0f);
		dummyBody.setKinematic(true);
		dummyNode.addControl(dummyBody);
		rootNode.attachChild(dummyNode);
		//mark2.addControl(dummyBody);
		//rootNode.attachChild(mark2);
		dummyBody.setPhysicsLocation(player.getPhysicsLocation().add(dummyOffsetPosition));
		bulletAppState.getPhysicsSpace().add(dummyBody);

		// Top Leg (rotule)
		Node legTopNode = new Node("LegTop-Node");
		CapsuleCollisionShape legTopShape = new CapsuleCollisionShape(0.1f, 0.5f, 0);
		RigidBodyControl legTopBody = new RigidBodyControl(legTopShape, 7.0f);
		legTopNode.addControl(legTopBody);
		rootNode.attachChild(legTopNode);
		legTopBody.setPhysicsLocation(dummyBody.getPhysicsLocation().add(new Vector3f(0.0f, -0.2f, 0.0f)));
		bulletAppState.getPhysicsSpace().add(legTopBody);
		
		ConeJoint legTopJoint = new ConeJoint(dummyBody, legTopBody, Vector3f.UNIT_Y.negate().multLocal(0.2f), Vector3f.UNIT_X.mult(0.25f));
		legTopJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(legTopJoint);
		
		// Bottom Leg (rotule)
		Node legBottomNode = new Node("LegBottom-Node");
		CapsuleCollisionShape legBottomShape = new CapsuleCollisionShape(0.1f, 0.5f, 0);
		RigidBodyControl legBottomBody = new RigidBodyControl(legBottomShape, 7.0f);
		legBottomNode.addControl(legBottomBody);
		rootNode.attachChild(legBottomNode);
		legBottomBody.setPhysicsLocation(legTopBody.getPhysicsLocation().add(new Vector3f(0.0f, -0.4f, 0.0f)));
		bulletAppState.getPhysicsSpace().add(legBottomBody);
		
		ConeJoint legBottomJoint = new ConeJoint(legTopBody, legBottomBody, Vector3f.UNIT_X.negate().multLocal(0.25f), Vector3f.UNIT_X.mult(0.25f));
		legBottomJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(legBottomJoint);

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
		//dummyBody.setPhysicsLocation(player.getPhysicsLocation().add(dummyOffsetPosition));
		dummyNode.setLocalTranslation(player.getPhysicsLocation().add(dummyOffsetPosition));
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
