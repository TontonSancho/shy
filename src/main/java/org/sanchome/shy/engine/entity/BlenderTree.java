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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class BlenderTree implements IEntity {

	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		// TODO Auto-generated method stub
		Spatial model = assetManager.loadModel("models/blender/Tree.mesh.xml" );
		rootNode.attachChild(model);
		
		float initialPositionX = (float)(500.0*Math.random())-250.0f;
		float initialPositionZ = (float)(500.0*Math.random())-250.0f;
		float initialScale     = (float)(8.0f*Math.random())+4.0f;
		
		
		model.setLocalTranslation(
	    		new Vector3f(
	    				initialPositionX,
	    				Application.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, 1.0f),
	    				initialPositionZ
	    		)
	    );
		
		model.setLocalScale(initialScale);
		
		BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(0.2f*initialScale, 1.1f*initialScale, 0.2f*initialScale));
		SphereCollisionShape scs = new SphereCollisionShape(1.625f*initialScale);
		CompoundCollisionShape ccs = new CompoundCollisionShape();
		ccs.addChildShape(bcs, new Vector3f(-0.1f*initialScale, 1.25f*initialScale, 0.0f));
		ccs.addChildShape(scs, new Vector3f(0.0f, 3.75f*initialScale, 0.0f));
		
		RigidBodyControl model_phy = new RigidBodyControl(ccs, 20f*initialScale);
		model_phy.setFriction(20.0f);
		/*
		model_phy.setFriction(50.0f);
		*/
		/*
		model_phy.getObjectId().setMassProps(
				200.0f, new javax.vecmath.Vector3f(0.0f, 10.0f, 0.0f)
			);
		*/
		model.addControl(model_phy);
	    bulletAppState.getPhysicsSpace().add(model_phy);
	    
	    model.setShadowMode(ShadowMode.Cast);
		 
	}

}
