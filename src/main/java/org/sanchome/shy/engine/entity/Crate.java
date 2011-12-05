package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.ApplicationClient;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

public class Crate implements IEntity {
	
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private Geometry model_geo;
	
	private static final Box BOX;
	private static Material wall_mat;
	
	private RigidBodyControl model_phy;
	
	static {
		BOX = new Box(Vector3f.ZERO, 1.0f, 1.0f, 1.0f);
		BOX.scaleTextureCoordinates(new Vector2f(1.0f, 1.0f));
	}
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
		
		if (wall_mat == null) {
			wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		    TextureKey key = new TextureKey("textures/crate.jpg");
		    key.setGenerateMips(true);
		    Texture tex = assetManager.loadTexture(key);
		    wall_mat.setTexture("ColorMap", tex);
		}
		
		model_geo = new Geometry("brick", BOX);
		model_geo.setMaterial(wall_mat);
		rootNode.attachChild(model_geo);
	    /** Position the brick geometry  */
		
		float initialPositionX = (float)(500.0*Math.random())-250.0f;
		float initialPositionZ = (float)(500.0*Math.random())-250.0f;
		float initialScale     = (float)(2.0*Math.random())+1.0f;
		
	    model_geo.setLocalTranslation(
	    		new Vector3f(
	    				initialPositionX,
	    				ApplicationClient.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, 1.1f*initialScale),
	    				initialPositionZ
	    		)
	    );
	    
	    model_geo.setLocalScale(initialScale);
	    
	    /** Make brick physical with a mass > 0.0f. */
	    BoxCollisionShape bcs = new BoxCollisionShape(new Vector3f(1.0f*initialScale,1.0f*initialScale,1.0f*initialScale));
	    model_phy = new RigidBodyControl(bcs, 50.0f*initialScale);
	    /** Add physical brick to physics space. */
	    model_geo.addControl(model_phy);
	    model_geo.setUserData("RigidBodyControl", model_phy);
	    
		model_phy.setAngularSleepingThreshold(100.0f);
		model_phy.setLinearSleepingThreshold(100.0f);
		model_phy.setCcdMotionThreshold(100.0f);
	    
	    bulletAppState.getPhysicsSpace().add(model_phy);

		model_phy.setEnabled(false);
	    model_phy.setFriction(50.0f);

	    
	    model_geo.setShadowMode(ShadowMode.Cast);
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