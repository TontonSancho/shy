package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.Application;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class BlenderTree implements IEntity {
	
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Spatial model;
	private RigidBodyControl model_phy;
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
		
		
		float massOffset = 2.0f;
		
		model = assetManager.loadModel("models/blender/Tree.mesh.xml" );
		Node node = (Node)model;
		Geometry geom = (Geometry) node.getChild("Tree-geom-1");
		System.out.println("model:"+geom);
		
		rootNode.attachChild(model);
		
		float initialPositionX = (float)(500.0*Math.random())-250.0f;
		float initialPositionZ = (float)(500.0*Math.random())-250.0f;
		float initialScale     = (float)(8.0f*Math.random())+4.0f;
		
		geom.setLocalTranslation(0.0f, massOffset, 0.0f);


		model.setLocalTranslation(
	    		new Vector3f(
	    				initialPositionX,
	    				Application.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, 1.0f) - massOffset*initialScale,
	    				initialPositionZ
	    		)
	    );
		model.setLocalScale(initialScale);
		
		
		
		BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(0.2f*initialScale, 1.0f*initialScale, 0.2f*initialScale));
		SphereCollisionShape scs = new SphereCollisionShape(1.625f*initialScale);
		CompoundCollisionShape ccs = new CompoundCollisionShape();
		ccs.addChildShape(bcs, new Vector3f(-0.0f*initialScale, (massOffset + 1.15f)*initialScale, 0.1f*initialScale));
		ccs.addChildShape(scs, new Vector3f(0.0f*initialScale, (massOffset + 3.75f)*initialScale, 0.0f*initialScale));
		
		model_phy = new RigidBodyControl(ccs, 100.0f*initialScale);
		model.addControl(model_phy);
		model.setUserData("RigidBodyControl", model_phy);

		model_phy.setAngularSleepingThreshold(10000.0f);
		model_phy.setLinearSleepingThreshold(10000.0f);
		model_phy.setCcdMotionThreshold(10000.0f);
		
		bulletAppState.getPhysicsSpace().add(model_phy);
		//model_phy.setFriction(50.0f);
		model_phy.setEnabled(false);
		
		model.setShadowMode(ShadowMode.Cast);
		
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