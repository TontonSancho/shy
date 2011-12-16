package org.sanchome.shy.engine.player;

import org.sanchome.shy.engine.ApplicationClient;
import org.sanchome.shy.engine.CollisionGroup;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
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

public class LocalPlayer implements IPlayer, ActionListener, AnalogListener {

	private Node rootNode;
	private Camera camera;
	private Node playerNode;
	private CharacterControl player;
	private Geometry mark;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;
	private static final float SPEED_NORMAL = 1.0f;
	private static final float SPEED_RUN = 2.0f;
	private float playerSpeed = SPEED_NORMAL;
	
	
	private Node intermediateNode;
	private Node intermediateFootNode;
	private Node footNode;
	private RigidBodyControl footBody;
	
	private float footRadianX            = 0.0f;
	private float footRadianZ            = 0.0f;
	private Vector3f footOffsetPosition  = new Vector3f(0.0f, -4.5f, 1.0f);

	private float footRadius = 0.4f;
	private float footLength = 0.60f;
	private float footWeight = 1000000.0f;
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.camera = camera;
		this.rootNode = rootNode;

		playerNode = new Node("LocalPlayer:Node");
		
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
		player = new CharacterControl(capsuleShape, 0.0f);
		player.setCollisionGroup(CollisionGroup.PLAYER_CAPSULE);
		player.setCollideWithGroups(CollisionGroup.PLAYER_CAPSULE_COLLISION_MASK);
		player.setJumpSpeed(30);
		player.setFallSpeed(40);
		player.setGravity(60);
		
		playerNode.addControl(player);
		
		rootNode.attachChild(playerNode);
		
		bulletAppState.getPhysicsSpace().add(player);
		
		player.setPhysicsLocation(new Vector3f(0.0f, ApplicationClient.getCurrentWorld().getHeightAt(0.0f, 0.0f, 3.1f), 0.0f));
		
		intermediateNode = new Node(playerNode.getName()+":Intermediate:Node");
		Matrix3f rot = Matrix3f.IDENTITY;
		rot.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
		intermediateNode.setLocalRotation(rot);
		playerNode.attachChild(intermediateNode);
		
		intermediateFootNode = new Node(playerNode.getName()+":IntermediateFoot:Node");
		intermediateNode.attachChild(intermediateFootNode);
		
		
		footNode = new Node(playerNode.getName()+":Foot:Node");
		intermediateFootNode.attachChild(footNode);
		CapsuleCollisionShape footShape = new CapsuleCollisionShape(footRadius, footLength, 0);
		footBody = new LocalPlayerFootControl(bulletAppState, footShape, footWeight);
		footBody.setCollisionGroup(CollisionGroup.PLAYER_FOOT);
		footBody.setCollideWithGroups(CollisionGroup.PLAYER_FOOT_COLLISION_MASK);
		footBody.setKinematic(true);
		footNode.addControl(footBody);
		bulletAppState.getPhysicsSpace().add(footBody);
		
		footNode.setLocalTranslation(footOffsetPosition);
		
		// A Shoot Mark
		Sphere sphere = new Sphere(30, 30, 0.2f);
		mark = new Geometry("BOOM!", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
		
	}
	
	private Matrix3f hipOrientation = Matrix3f.IDENTITY.clone();
	private Matrix3f rotX = Matrix3f.IDENTITY.clone();
	private Matrix3f rotZ = Matrix3f.IDENTITY.clone();
	private PhysicsRigidBody theThingToShoot = null;
	private Vector3f lookAtOffset = null;

	public void simpleUpdate(float tpf) {
		
		Vector3f camDir = camera.getDirection().clone().multLocal(0.3f*playerSpeed);
		Vector3f camLeft = camera.getLeft().clone().multLocal(0.2f*playerSpeed);
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
		// Suppress Y move component
		walkDirection.set(walkDirection.getX(), 0.0f, walkDirection.getZ());
		// Update player position and orientation
		player.setWalkDirection(walkDirection);
		camera.setLocation(player.getPhysicsLocation());
		
		// Adjust the hip orientation to follow the camera
		hipOrientation.fromStartEndVectors(Vector3f.UNIT_X, camera.getDirection().clone().multLocal(1.0f, 0.0f, 1.0f).normalizeLocal());
		intermediateNode.setLocalRotation(hipOrientation);
		
		if (theThingToShoot != null) {
			// Look at the targeted point
			camera.lookAt(theThingToShoot.getPhysicsLocation().add(lookAtOffset), Vector3f.UNIT_Y);
			
			updateFootOrientation();
		}
		
	}
	
	private void updateFootOrientation() {
		// Adjust the foot orientation to follow mouse control
		rotZ.fromAngleNormalAxis(footRadianZ, Vector3f.UNIT_Z);
		rotX.fromAngleNormalAxis(footRadianX, Vector3f.UNIT_X);
		
		rotZ.multLocal(rotX);
		intermediateFootNode.setLocalRotation(rotZ);
		
		//Relax
		footRadianX = footRadianX / 1.1f;
		footRadianZ = footRadianZ / 1.1f;
	}
	
	public void onAnalog(String name, float value, float tpf) {
		if (theThingToShoot != null) {
			// Update foot orientation accumulators
			if ("FootControl_Left".equals(name)) {
				footRadianX += value;
			} else if ("FootControl_Right".equals(name)) {
				//System.out.println("right"+value);
				footRadianX -= value;
			} else if ("FootControl_Up".equals(name)) {
				//System.out.println("up"+value);
				footRadianZ += value;
			} else if ("FootControl_Down".equals(name)) {
				//System.out.println("down"+value);
				footRadianZ -= value;
			}
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
		} else if ("Run".equals(binding)) {
			playerSpeed = isPressed ? SPEED_RUN : SPEED_NORMAL;
		} else if ("Jump".equals(binding)) {
			player.jump();
		} else if (("Shoot").equals(binding)) {
			if (!isPressed) {
				CollisionResults results = new CollisionResults();
				Ray ray = new Ray(camera.getLocation(), camera.getDirection());
				rootNode.collideWith(ray, results);
				if (results.size() > 0) {
					CollisionResult closest = results.getClosestCollision();
					PhysicsRigidBody rbc = closest.getGeometry().getParent().getUserData("PhysicsRigidBody");
					if (rbc==null)
						rbc = closest.getGeometry().getUserData("PhysicsRigidBody");
					if(rbc!=null) {
						System.out.println("rbc       :"+rbc);
						//if(!rbc..isEnabled()) rbc.setEnabled(true);
						rbc.activate();
						rbc.applyImpulse(camera.getDirection().mult(1000.0f), camera.getLocation().subtract(rbc.getPhysicsLocation()));
					}
					mark.setLocalTranslation(closest.getContactPoint());
					rootNode.attachChild(mark);
					
				} else {
					rootNode.detachChild(mark);
				}
			}
		} else if (("FootControl_Lock").equals(binding)) {
			if (isPressed) {
				System.out.println("FootControl_Lockz");
				CollisionResults results = new CollisionResults();
				Ray ray = new Ray(camera.getLocation(), camera.getDirection());
				rootNode.collideWith(ray, results);
				if (results.size() > 0) {
					CollisionResult closest = results.getClosestCollision();
					PhysicsRigidBody rbc = closest.getGeometry().getParent().getUserData("PhysicsRigidBody");
					if (rbc==null)
						rbc = closest.getGeometry().getUserData("PhysicsRigidBody");
					if(rbc!=null) {
						theThingToShoot = rbc;
						lookAtOffset = closest.getContactPoint().subtract(rbc.getPhysicsLocation());
					}
					mark.setLocalTranslation(closest.getContactPoint());
					rootNode.attachChild(mark);
				}
			} else {
				// The player releases the lock
				theThingToShoot = null;
				rootNode.detachChild(mark);
				footRadianX = 0.0f;
				footRadianZ = 0.0f;
				updateFootOrientation();
			}
		}
	}
}
