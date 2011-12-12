package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.ApplicationClient;
import org.sanchome.shy.engine.CollisionGroup;
import org.sanchome.shy.engine.UserSettings;
import org.sanchome.shy.engine.UserSettings.ShadowDetails;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LodControl;

public class BlenderTree implements IEntity {
	
	private static int TREE_ORDER = 0;
	
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Node myLocalNode;
	private Spatial model;
	private RigidBodyControl model_phy;
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
		
		myLocalNode = new Node("Tree:"+TREE_ORDER++);
		rootNode.attachChild(myLocalNode);
		
		// Geom part
		float massOffset = 2.0f;

		float initialScale     = (float)(8.0f*Math.random())+4.0f;

		Vector3f initialPosition = ApplicationClient.getCurrentWorld().getRandomPosition( -1.0f - massOffset*initialScale );
		myLocalNode.setLocalTranslation(initialPosition);
		
//		Vector3f worldSize = ApplicationClient.getCurrentWorld().getWorldMax().subtractLocal(ApplicationClient.getCurrentWorld().getWorldMin()).multLocal(0.95f);
//		
//		float initialPositionX = (float)(worldSize.x*Math.random())-(worldSize.x/2f);
//		float initialPositionZ = (float)(worldSize.z*Math.random())-(worldSize.z/2f);
//		
//		myLocalNode.setLocalTranslation(
//	    		new Vector3f(
//	    				initialPositionX,
//	    				ApplicationClient.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, -1.0f) - massOffset*initialScale,
//	    				initialPositionZ
//	    		)
//	    );
		
		model = assetManager.loadModel("models/blender/Tree.mesh.xml" );
		Node node = (Node)model;
		Geometry geom = (Geometry) node.getChild("Tree-geom-1");
		System.out.println("model:"+geom);
		
		myLocalNode.attachChild(model);

		geom.setLocalTranslation(0.0f, massOffset, 0.0f);
		
		Matrix3f rot = Matrix3f.IDENTITY.clone();
		rot.fromStartEndVectors(Vector3f.UNIT_Y, ApplicationClient.getCurrentWorld().getNormalAt(initialPosition.x, initialPosition.z) );
		
		model.setLocalRotation(rot);
		
		model.setLocalScale(initialScale);
		
		model.setShadowMode(UserSettings.SHADOW_DETAIL == ShadowDetails.FULL ? ShadowMode.CastAndReceive : ShadowMode.Cast);
		
		// Physic part
		BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(0.2f*initialScale, 1.0f*initialScale, 0.2f*initialScale));
		SphereCollisionShape scs = new SphereCollisionShape(1.625f*initialScale);
		CompoundCollisionShape ccs = new CompoundCollisionShape();
		ccs.addChildShape(bcs, new Vector3f(-0.0f*initialScale, (massOffset + 1.15f)*initialScale, 0.1f*initialScale));
		ccs.addChildShape(scs, new Vector3f(0.0f*initialScale, (massOffset + 3.75f)*initialScale, 0.0f*initialScale));
		
		model_phy = new RigidBodyControl(ccs, 0.0f /*100.0f*initialScale*/);
		model_phy.setCollisionGroup(CollisionGroup.TREES);
		model_phy.setCollideWithGroups(CollisionGroup.TREES_COLLISION_MASK);
		model.setUserData("RigidBodyControl", model_phy);
		model.addControl(model_phy);
		
		bulletAppState.getPhysicsSpace().add(model_phy);
		
		
		model_phy.setFriction(100.0f);
		
		//model_phy.setEnabled(false);
		
		
		
	    /*
		Vector3f terrainNormal = ApplicationClient.getCurrentWorld().getNormalAt(initialPositionX, initialPositionZ);
		Matrix3f rotationMatrix = new Matrix3f();
		rotationMatrix.fromStartEndVectors(Vector3f.UNIT_Y, terrainNormal);
		Quaternion treeOrientation = Quaternion.IDENTITY;
		treeOrientation.fromRotationMatrix(rotationMatrix);
		model_phy.setPhysicsRotation(treeOrientation);
		*/
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
