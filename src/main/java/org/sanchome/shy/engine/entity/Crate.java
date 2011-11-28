package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.Application;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
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
	
	private static final Box BOX;
	private static Material wall_mat;
	
	static {
		BOX = new Box(Vector3f.ZERO, 1.0f, 1.0f, 1.0f);
		BOX.scaleTextureCoordinates(new Vector2f(1.0f, 1.0f));
	}
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		if (wall_mat == null) {
			wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		    TextureKey key = new TextureKey("textures/crate.jpg");
		    key.setGenerateMips(true);
		    Texture tex = assetManager.loadTexture(key);
		    wall_mat.setTexture("ColorMap", tex);
		}
		
		Geometry brick_geo = new Geometry("brick", BOX);
		brick_geo.setMaterial(wall_mat);
		rootNode.attachChild(brick_geo);
	    /** Position the brick geometry  */
		
		float initialPositionX = (float)(500.0*Math.random())-250.0f;
		float initialPositionZ = (float)(500.0*Math.random())-250.0f;
		
	    brick_geo.setLocalTranslation(
	    		new Vector3f(
	    				initialPositionX,
	    				Application.getCurrentWorld().getHeightAt(initialPositionX, initialPositionZ, 1.0f),
	    				initialPositionZ
	    		)
	    );
	    /** Make brick physical with a mass > 0.0f. */
	    RigidBodyControl brick_phy = new RigidBodyControl(20f);
	    /** Add physical brick to physics space. */
	    brick_geo.addControl(brick_phy);
	    brick_geo.setUserData("RigidBodyControl", brick_phy);
	    bulletAppState.getPhysicsSpace().add(brick_phy);
	    
	    brick_geo.setShadowMode(ShadowMode.Cast);
	}

}