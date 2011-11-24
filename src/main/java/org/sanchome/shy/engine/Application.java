package org.sanchome.shy.engine;

import org.sanchome.shy.engine.player.LocalPlayer;
import org.sanchome.shy.engine.world.HelloWorld;
import org.sanchome.shy.engine.world.IWorld;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Application extends SimpleApplication {
	
	private BulletAppState bulletAppState;
	IWorld world;
	LocalPlayer localPlayer;
	
	@Override
	public void simpleInitApp() {
		// Set azerty keyboard
		inputManager.deleteMapping("FLYCAM_Forward");
		inputManager.deleteMapping("FLYCAM_Lower");
		inputManager.deleteMapping("FLYCAM_StrafeLeft");
		inputManager.deleteMapping("FLYCAM_Rise");
		inputManager.deleteMapping("FLYCAM_StrafeRight");
		inputManager.addMapping("FLYCAM_Forward", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("FLYCAM_StrafeLeft", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("FLYCAM_StrafeRight", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(flyCam, new String[] {"FLYCAM_Forward", "FLYCAM_Lower", "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", "Jump"});
		
		// Speed up the cam a bit
		flyCam.setMoveSpeed(50);
		
		/** Set up Physics */
	    bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);
	    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
		
		// Instanciate the chossen terrain
		world = new HelloWorld();
		world.init(assetManager, cam, rootNode, bulletAppState);
		
		// Make a box
		Box b = new Box(Vector3f.ZERO, 1, 1, 1); // create cube shape at the origin
		Geometry geom = new Geometry("Box", b); // create cube geometry from the shape
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple material
		mat.setColor("Color", ColorRGBA.Blue); // set color of material to blue
		geom.setMaterial(mat); // set the cube's material
		rootNode.attachChild(geom); // make the cube appear in the scene
		
		// Local player
		localPlayer = new LocalPlayer();
		localPlayer.init(assetManager, cam, rootNode, bulletAppState);
		inputManager.addListener(localPlayer, "FLYCAM_Forward", "FLYCAM_Lower", "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", "Jump");
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		localPlayer.simpleUpdate(tpf);
	}
}
