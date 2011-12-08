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
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
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
	
	private Node intermediateNode;
	private Node hipNode;
	private RigidBodyControl hipBody;
	private Node intermediateFootNode;
	private Node footNode;
	private RigidBodyControl footBody;
	
	
	private Vector3f hipOffsetPosition   = new Vector3f(3.0f, -2.0f, 1.0f);
	private Vector3f footOffsetPosition  = new Vector3f(0.1f,0.1f,0.1f);
	private Vector3f footControlPosition = new Vector3f();

	private float hipHalfSize = 0.2f;
	private float legPartRadius = 0.1f;
	private float legPartLength = 0.7f;
	private float legPartWeight = 20.0f;
	private float footRadius = 0.2f;
	private float footLength = 0.40f;
	private float footWeight = 1000.0f;
	
	private boolean enablePhysic = false;
	
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
		//player.setPhysicsLocation(new Vector3f(0.0f, ApplicationClient.getCurrentWorld().getHeightAt(0.0f, 0.0f, 3.1f), 0.0f));
		playerNode.addControl(player);
		//playerNode.setLocalTranslation(new Vector3f(0.0f, ApplicationClient.getCurrentWorld().getHeightAt(0.0f, 0.0f, 3.1f), 0.0f));
		rootNode.attachChild(playerNode);
		
		bulletAppState.getPhysicsSpace().add(player);
		
		if (enablePhysic) {
		
		intermediateNode = new Node("Intermediate-Node");
		Matrix3f rot = Matrix3f.IDENTITY;
		rot.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
		intermediateNode.setLocalRotation(rot);
		playerNode.attachChild(intermediateNode);
		
		// Hanche (fixe)
		hipNode = new Node("Dummy-Node");
		BoxCollisionShape dummyShape = new BoxCollisionShape(new Vector3f(hipHalfSize, hipHalfSize, hipHalfSize));
		hipBody = new RigidBodyControl(dummyShape, 0.0f);
		hipBody.setCollisionGroup(0);
		hipBody.setKinematic(true);
		hipNode.addControl(hipBody);
		intermediateNode.attachChild(hipNode);
		hipNode.setLocalTranslation(hipOffsetPosition);
		//hipBody.setPhysicsLocation(hipOffsetPosition/*player.getPhysicsLocation().add(hipOffsetPosition)*/);
		bulletAppState.getPhysicsSpace().add(hipBody);

		// Top Leg (rotule)
		Node legTopNode = new Node("LegTop-Node");
		CapsuleCollisionShape legTopShape = new CapsuleCollisionShape(legPartRadius, legPartLength, 0);
		RigidBodyControl legTopBody = new RigidBodyControl(legTopShape, legPartWeight);
		legTopBody.setCollisionGroup(0);
		legTopNode.addControl(legTopBody);
		intermediateNode.attachChild(legTopNode);
		legTopNode.setLocalTranslation(hipNode.getLocalTranslation().add(new Vector3f(0.0f, -hipHalfSize, 0.0f)));
		//legTopBody.setPhysicsLocation(hipBody.getPhysicsLocation().add(new Vector3f(0.0f, -hipHalfSize, 0.0f)));
		//hipNode.attachChild(legTopNode);
		//legTopNode.setLocalTranslation(new Vector3f(0.0f, -hipHalfSize, 0.0f));
		bulletAppState.getPhysicsSpace().add(legTopBody);
		
		ConeJoint legTopJoint = new ConeJoint(hipBody, legTopBody, Vector3f.UNIT_Y.negate().multLocal(hipHalfSize), Vector3f.UNIT_X.mult(legPartLength/2.0f));
		legTopJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(legTopJoint);
		legTopJoint.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, 0.0f);
		
		
		// Bottom Leg (rotule)
		Node legBottomNode = new Node("LegBottom-Node");
		CapsuleCollisionShape legBottomShape = new CapsuleCollisionShape(legPartRadius, legPartLength, 0);
		RigidBodyControl legBottomBody = new RigidBodyControl(legBottomShape, legPartWeight);
		legBottomBody.setCollisionGroup(0);
		legBottomNode.addControl(legBottomBody);
		intermediateNode.attachChild(legBottomNode);
		legBottomNode.setLocalTranslation(legTopNode.getLocalTranslation().add(new Vector3f(0.0f, -legPartLength -legPartRadius, 0.0f)));
		//legBottomBody.setPhysicsLocation(legTopBody.getPhysicsLocation().add(new Vector3f(0.0f, -legPartLength, 0.0f)));
		//legTopNode.attachChild(legBottomNode);
		//legBottomNode.setLocalTranslation(new Vector3f(0.0f, -legPartLength, 0.0f));
		bulletAppState.getPhysicsSpace().add(legBottomBody);
		
		ConeJoint legBottomJoint = new ConeJoint(legTopBody, legBottomBody, Vector3f.UNIT_X.negate().multLocal(legPartLength/2.0f), Vector3f.UNIT_X.mult(legPartLength/2.0f));
		legBottomJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(legBottomJoint);
		legTopJoint.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, 0.0f);
		
		
		intermediateFootNode = new Node("Intermediate Foot Node");
		intermediateNode.attachChild(intermediateFootNode);
		
		// Foot (rotule)
		footNode = new Node("Foot-Node");
		CapsuleCollisionShape footShape = new CapsuleCollisionShape(footRadius, footLength, 0);
		footBody = new RigidBodyControl(footShape, footWeight);
		footBody.setCollisionGroup(0);
		footBody.setKinematic(true);
		footNode.addControl(footBody);
		intermediateFootNode.attachChild(footNode);
		footNode.setLocalTranslation(legBottomNode.getLocalTranslation().add(new Vector3f(footLength/2.0f, -legPartLength -footRadius, 0.0f)));
		//footBody.setPhysicsLocation(legBottomBody.getPhysicsLocation().add(new Vector3f(footLength/2.0f, -legPartLength, 0.0f)));
		//legBottomNode.attachChild(footNode);
		//footNode.setLocalTranslation(new Vector3f(footLength/2.0f, -legPartLength, 0.0f));
		bulletAppState.getPhysicsSpace().add(footBody);
		
		ConeJoint footJoint = new ConeJoint(legBottomBody, footBody, Vector3f.UNIT_X.negate().multLocal(legPartLength/2.0f), Vector3f.UNIT_X.negate().multLocal(footLength/2.0f));
		footJoint.setCollisionBetweenLinkedBodys(false);
		bulletAppState.getPhysicsSpace().add(footJoint);
		legTopJoint.setLimit(FastMath.QUARTER_PI, FastMath.QUARTER_PI, 0.0f);
		
		footOffsetPosition = hipOffsetPosition.add(0.0f, -hipHalfSize - legPartLength*2.0f - footRadius, 0.0f); //footNode.getLocalTranslation();
		System.out.println("Computing foot offset:"+footOffsetPosition );
		//footNode.setLocalTranslation(footOffsetPosition);
		
		}
		
		// A Shoot Mark
		Sphere sphere = new Sphere(30, 30, 0.2f);
		mark = new Geometry("BOOM!", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
		
		
	}

	private float cumulTpf = 0.0f;
	private boolean footControlEnabled = false;

	public void simpleUpdate(float tpf) {
		if (footControlEnabled) {
			footControlPosition.setX(0.8f*FastMath.sin(-cumulTpf*8.0f));
			cumulTpf += tpf;
		}
		
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
		walkDirection.set(walkDirection.getX(), 0.0f, walkDirection.getZ());
		player.setWalkDirection(walkDirection);
		camera.setLocation(player.getPhysicsLocation());
		
		if (enablePhysic) {
		
		Matrix3f rot = Matrix3f.IDENTITY;
		rot.fromStartEndVectors(Vector3f.UNIT_X, camera.getDirection().clone().multLocal(1.0f, 0.0f, 1.0f).normalizeLocal());
		//rot.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
		intermediateNode.setLocalRotation(rot);
		//footBody.setPhysicsLocation(footOffsetPosition);
		//System.out.println("footOffsetPosition:"+footOffsetPosition+" footControlPosition:"+footControlPosition);
		/*
		Vector3f strangeOffset = new Vector3f(footOffsetPosition.x, footOffsetPosition.y, footOffsetPosition.z);
		footNode.setLocalTranslation(strangeOffset.add(footControlPosition));
		*/
		Matrix3f footRotation = Matrix3f.IDENTITY;
		footRotation.fromAngleNormalAxis(FastMath.HALF_PI*FastMath.sin(-cumulTpf*1.0f), Vector3f.UNIT_Z);
		
		intermediateFootNode.setLocalRotation(footRotation);
		//footNode.setLocalTranslation(footOffsetPosition.add(footControlPosition));
		
		//System.out.println(camera.getDirection());
		//intermediateNode.setLocalRotation(camera.getRotation());
		//dummyBody.setPhysicsLocation(player.getPhysicsLocation().add(dummyOffsetPosition));
		//hipNode.setLocalTranslation(player.getPhysicsLocation().add(hipOffsetPosition));
		//footNode.setLocalTranslation(player.getPhysicsLocation().add(hipOffsetPosition).addLocal(footOffsetPosition));
		
		}
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
		} else if (("FootControl").equals(binding)) {
			footControlEnabled = isPressed;
			if (!isPressed) {
				footControlPosition.zero();
				cumulTpf = 0.0f;
			}
		}
	}
}
