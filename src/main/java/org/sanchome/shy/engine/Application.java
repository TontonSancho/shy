package org.sanchome.shy.engine;

import org.sanchome.shy.engine.entity.BlenderTree;
import org.sanchome.shy.engine.entity.Crate;
import org.sanchome.shy.engine.player.LocalPlayer;
import org.sanchome.shy.engine.world.HelloWorld;
import org.sanchome.shy.engine.world.IWorld;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.util.SkyFactory;

public class Application extends SimpleApplication {

	private BulletAppState bulletAppState;
	private static IWorld world;
	LocalPlayer localPlayer;

	static public IWorld getCurrentWorld() {
		return world;
	}
	
	@Override
	public void simpleInitApp() {

		// Set azerty keyboard
		inputManager.deleteMapping("FLYCAM_Forward");
		inputManager.deleteMapping("FLYCAM_Lower");
		inputManager.deleteMapping("FLYCAM_StrafeLeft");
		inputManager.deleteMapping("FLYCAM_Rise");
		inputManager.deleteMapping("FLYCAM_StrafeRight");
		inputManager.addMapping("FLYCAM_Forward", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("FLYCAM_StrafeLeft", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("FLYCAM_StrafeRight", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(flyCam, new String[] { "FLYCAM_Forward", "FLYCAM_Lower", "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", "Jump" });

		// Speed up the cam a bit
		flyCam.setMoveSpeed(50);

		/** Set up Physics */
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		// Instanciate the chossen terrain
		world = new HelloWorld();
		world.init(assetManager, cam, rootNode, bulletAppState);

		// Local player
		localPlayer = new LocalPlayer();
		localPlayer.init(assetManager, cam, rootNode, bulletAppState);
		inputManager.addListener(localPlayer, "FLYCAM_Forward", "FLYCAM_Lower", "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", "Jump");

		// Crates
		for (int i = 0; i < 0; i++) {
			Crate crate = new Crate();
			crate.init(assetManager, cam, rootNode, bulletAppState);
		}
		
		// Trees
		for (int i = 0; i < 2; i++) {
			BlenderTree tree = new BlenderTree();
			tree.init(assetManager, cam, rootNode, bulletAppState);
		}
		
		// Light
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(0.5f));
		rootNode.addLight(al);
		
		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(-0.7f, -1.0f, -1.0f).normalizeLocal());
		rootNode.addLight(sun);
		
		// Sky
		Spatial sky = SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false);
		rootNode.attachChild(sky);
		
		// Shadow
		
		rootNode.setShadowMode(ShadowMode.Off);
		PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 4);
		pssmRenderer.setDirection(sun.getDirection());
		pssmRenderer.setShadowIntensity(0.5f);
		//pssmRenderer.setShadowZExtend(100.0f);
		viewPort.addProcessor(pssmRenderer);
		
	}

	@Override
	public void simpleUpdate(float tpf) {
		localPlayer.simpleUpdate(tpf);
	}
}
