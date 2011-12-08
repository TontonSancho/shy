package org.sanchome.shy.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sanchome.shy.engine.entity.BlenderTree;
import org.sanchome.shy.engine.entity.Crate;
import org.sanchome.shy.engine.entity.IEntity;
import org.sanchome.shy.engine.entity.IUpdatable;
import org.sanchome.shy.engine.entity.Sheep;
import org.sanchome.shy.engine.player.LocalPlayer;
import org.sanchome.shy.engine.world.HelloWorld;
import org.sanchome.shy.engine.world.IWorld;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.BulletAppState.ThreadingType;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.util.SkyFactory;

public class ApplicationClient extends SimpleApplication {

	private static final Logger logger = Logger.getLogger(ApplicationClient.class.getName());


	private BulletAppState bulletAppState;
	private static IWorld world;
	private Node mobilesNode;
	LocalPlayer localPlayer;
	Map<IEntity, Integer> entitiesToStabilizePhysicaly = new HashMap<IEntity, Integer>();
	List<IUpdatable> uptatableEntities = new ArrayList<IUpdatable>();

	static public IWorld getCurrentWorld() {
		return world;
	}
	
	@Override
	public void simpleInitApp() {
		logger.info("Starting shy client ...");

		// Set azerty keyboard
		for(String builtinMapping :
			new String[]{
				"FLYCAM_RotateDrag",
				"FLYCAM_Forward",
				"FLYCAM_Backward",
				"FLYCAM_Rise",
				"FLYCAM_Lower",
				"FLYCAM_StrafeLeft",
				"FLYCAM_StrafeRight"
			}) {
			inputManager.deleteMapping(builtinMapping);
									
		}
		
		inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("FootControl", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(flyCam, new String[] { "Forward", "Backward", "Left", "Right", "Jump" });
		
		// Speed up the cam a bit
		flyCam.setMoveSpeed(50);

		/** Set up Physics */
		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(ThreadingType.PARALLEL);
		bulletAppState.setBroadphaseType(BroadphaseType.SIMPLE);
		bulletAppState.setWorldMin(new Vector3f(-256.0f, -200.0f, -256.0f));
		bulletAppState.setWorldMax(new Vector3f(256.0f, 200.0f, 256.0f));
		stateManager.attach(bulletAppState);
		bulletAppState.getPhysicsSpace().setMaxSubSteps(10);
		bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		// Instanciate the chossen terrain
		world = new HelloWorld();
		world.init(assetManager, cam, rootNode, bulletAppState);
		
		mobilesNode = new Node("mobilesNode");
		rootNode.attachChild(mobilesNode);

		// Local player
		localPlayer = new LocalPlayer();
		localPlayer.init(assetManager, cam, mobilesNode, bulletAppState);
		inputManager.addListener(localPlayer, "Forward", "Backward", "Left", "Right", "Jump", "Shoot", "FootControl");

		// Crates
		for (int i = 0; i < 1; i++) {
			Crate crate = new Crate();
			crate.init(assetManager, cam, mobilesNode, bulletAppState);
			//entitiesToStabilizePhysicaly.put(crate, 0);
		}
		
		// Trees
		for (int i = 0; i < 50; i++) {
			BlenderTree tree = new BlenderTree();
			tree.init(assetManager, cam, mobilesNode, bulletAppState);
			//entitiesToStabilizePhysicaly.put(tree, 0);
		}
		
		
		// Sheeps
		for (int i = 0; i < 1; i++) {
			Sheep sheep = new Sheep();
			sheep.init(assetManager, cam, mobilesNode, bulletAppState);
			//entitiesToStabilizePhysicaly.put(sheep, 0);
			uptatableEntities.add(sheep);
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
		// Physic/Dynamic stabilization
		// trick to load large amount of physic objects
		if (!entitiesToStabilizePhysicaly.isEmpty()) {
			//IEntity entityToStabilize = null;
			
			boolean atLeastOneStabilization = false;
			int i=0;
			for(IEntity entityToStabilize : entitiesToStabilizePhysicaly.keySet()) {
				if (entitiesToStabilizePhysicaly.get(entityToStabilize) > 100) {
					// Pop it
					entitiesToStabilizePhysicaly.remove(entityToStabilize);
					entityToStabilize.detach();
					atLeastOneStabilization = true;
					System.out.println("------ Your are too long to stabilize:"+i);
					break;
				}
					
				if (!entityToStabilize.isStabilized()) {
					System.out.println("Stabilization i:"+i+" on:"+entitiesToStabilizePhysicaly.size());
					entityToStabilize.enableStabilization();
					entitiesToStabilizePhysicaly.put(entityToStabilize, entitiesToStabilizePhysicaly.get(entityToStabilize)+1);
					atLeastOneStabilization=true;
					break;
				}
				else {
					entitiesToStabilizePhysicaly.put(entityToStabilize, 0);
					System.out.println("Stabilized i:"+i+" on:"+entitiesToStabilizePhysicaly.size());
				}
				i++;
			}
			if(!atLeastOneStabilization) {
				for(IEntity entityToStabilize : entitiesToStabilizePhysicaly.keySet())
					entityToStabilize.restoreNormalPhysics();
				entitiesToStabilizePhysicaly.clear();
				logger.info("Stabilization done.");
			}
		}
		
		for(IUpdatable updatable : uptatableEntities)
			updatable.update(tpf);
		
		localPlayer.simpleUpdate(tpf);
	}
}
