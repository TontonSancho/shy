package org.sanchome.shy.engine;

import org.sanchome.shy.engine.entity.BlenderTree;
import org.sanchome.shy.engine.entity.Crate;
import org.sanchome.shy.engine.player.LocalPlayer;
import org.sanchome.shy.engine.world.HelloWorld;
import org.sanchome.shy.engine.world.IWorld;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.BulletAppState.ThreadingType;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Caps;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.util.SkyFactory;

public class Application extends SimpleApplication {

	private BulletAppState bulletAppState;
	private static IWorld world;
	private Node mobilesNode;
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
		inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(flyCam, new String[] { "Forward", "Backward", "Left", "Right", "Jump" });

		// Speed up the cam a bit
		flyCam.setMoveSpeed(50);

		/** Set up Physics */
		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);
		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		// Instanciate the chossen terrain
		world = new HelloWorld();
		world.init(assetManager, cam, rootNode, bulletAppState);

		mobilesNode = new Node("mobilesNode");
		rootNode.attachChild(mobilesNode);

		// Local player
		localPlayer = new LocalPlayer();
		localPlayer.init(assetManager, cam, mobilesNode, bulletAppState);
		inputManager.addListener(localPlayer, "Forward", "Backward", "Left", "Right", "Jump", "Shoot");

		// Crates
		for (int i = 0; i < 30; i++) {
			Crate crate = new Crate();
			crate.init(assetManager, cam, mobilesNode, bulletAppState);
		}

		// Trees
		for (int i = 0; i < 30; i++) {
			BlenderTree tree = new BlenderTree();
			tree.init(assetManager, cam, mobilesNode, bulletAppState);
		}

		// Light
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(0.5f));
		rootNode.addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(-0.7f, -1.0f, -1.0f).normalizeLocal());
		rootNode.addLight(sun);

		// Sky, some graphic cards cannot run float textures
		Spatial sky;
		if (renderer.getCaps().contains(Caps.FloatTexture)) {
			sky = SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false);
		} else {
			// TODO : find a better sky..
			sky = SkyFactory.createSky(assetManager, "textures/alternateSky.jpg", true);
		}
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
