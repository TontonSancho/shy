package org.sanchome.shy.engine.world;

import java.util.ArrayList;
import java.util.List;

import org.sanchome.shy.engine.CollisionGroup;
import org.sanchome.shy.engine.UserSettings;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.geomipmap.lodcalc.LodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class HelloWorld implements IWorld {

	private TerrainQuad terrain;
	private Material mat_terrain;
	private RigidBodyControl landscape;
	
	private int squareSize = 512; 

	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
		
		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
		mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
		
		/** 1.2) Add GRASS texture into the red layer (Tex1). */
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);
		
		/** 1.3) Add DIRT texture into the green layer (Tex2) */
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		mat_terrain.setFloat("Tex2Scale", 32f);
		
		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
		Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);
		
		/** 2. Create the height map */
		AbstractHeightMap heightmap = null;
		//Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
		//heightmap = new ImageBasedHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
		//heightmap.load();
		try {
			heightmap = new HillHeightMap(squareSize+1 /*513*/, 1000, 50, 100, (byte) 3);
			heightmap.setHeightScale(0.1f);
			//heightmap.
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		
		/**
		 * 3. We have prepared material and heightmap.
		 * Now we create the actual terrain: 
		 * 3.1) Create a TerrainQuad and name it "my terrain".
		 * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
		 * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
		 * 3.4) As LOD step scale we supply Vector3f(1,1,1). 3.5) We supply the heightmap itself.
		 */
		int patchSize = 65;
		terrain = new TerrainQuad("HelloWorld:Terrain", patchSize, squareSize+1 /*513*/, heightmap.getScaledHeightMap());
		
		/**
		 * 4. We give the terrain its material, position & scale it, and attach
		 * it.
		 */
		terrain.setMaterial(mat_terrain);
		terrain.setLocalTranslation(0, -100, 0);
		//terrain.setLocalScale(1f, 0.1f, 1f);
		rootNode.attachChild(terrain);
		
	    terrain.setShadowMode(ShadowMode.Receive);
		
		/** 5. The LOD (level of detail) depends on were the camera is: */
		List<Camera> cameras = new ArrayList<Camera>();
		cameras.add(camera);
		TerrainLodControl control = new TerrainLodControl(terrain, cameras);
		if (UserSettings.TERRAIN_LOD_EXAGGERATION) {
			LodCalculator lodCalculator = new DistanceLodCalculator(patchSize, 0.8f);
			control.setLodCalculator(lodCalculator);
		}
		terrain.addControl(control);
		
		CollisionShape terrainShape = CollisionShapeFactory.createMeshShape((Node) terrain);
		landscape = new RigidBodyControl(terrainShape, 0);
		landscape.setCollisionGroup(CollisionGroup.TERRAIN);
		landscape.setCollideWithGroups(CollisionGroup.TERRAIN_COLLISION_MASK);
		terrain.addControl(landscape);
		
		bulletAppState.getPhysicsSpace().add(terrain);
	}
	
	public float getHeightAt(Vector2f queryXZ) {
		float localT = terrain.getLocalTranslation().y;
		return localT + terrain.getHeight(queryXZ);
	}
	
	public float getHeightAt(Vector2f queryXZ, float yOffset) {
		return yOffset + getHeightAt(queryXZ);
	}
	
	public float getHeightAt(float queryX, float queryZ) {
		return getHeightAt(new Vector2f(queryX, queryZ));
	}
	
	public float getHeightAt(float queryX, float queryZ, float yOffset) {
		return getHeightAt(new Vector2f(queryX, queryZ), yOffset);
	}
	
	public Vector3f getNormalAt(Vector2f queryXZ) {
		return terrain.getNormal(queryXZ);
	}
	
	public Vector3f getNormalAt(float queryX, float queryZ) {
		return getNormalAt(new Vector2f(queryX, queryZ));
	}
	
	public Vector3f getWorldMin() {
		return new Vector3f(-squareSize/2, -100.0f, -squareSize/2);
	}
	
	public Vector3f getWorldMax() {
		return new Vector3f(squareSize/2, +100.0f, squareSize/2);
	}
	
	public Vector3f getRandomPosition() {
		return getRandomPosition(0.0f);
	}
	
	public Vector3f getRandomPosition(float yOffset) {
		float initialPositionX = (float)(squareSize*Math.random())-squareSize/2;
		float initialPositionZ = (float)(squareSize*Math.random())-squareSize/2;
		float initialPositionY = getHeightAt(initialPositionX, initialPositionZ, yOffset);
		return new Vector3f(initialPositionX, initialPositionY, initialPositionZ);
	}

}
