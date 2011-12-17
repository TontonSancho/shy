package org.sanchome.shy.engine.entity.tool;

import org.sanchome.shy.engine.entity.Fence;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class FenceFactory {
	
	private AssetManager assetManager;
	private Camera camera;
	private Node rootNode;
	private BulletAppState bulletAppState;
	
	private FenceFactory() {};
	
	private FenceFactory(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.assetManager = assetManager;
		this.camera = camera;
		this.rootNode = rootNode;
		this.bulletAppState = bulletAppState;
	}
	
	public static FenceFactory getPencil(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		return new FenceFactory(assetManager, camera, rootNode, bulletAppState);
	}
	
	public void drawLine(float startX, float startZ, float endX, float endZ) {
		Vector2f start = new Vector2f(startX, startZ);
		Vector2f end   = new Vector2f(endX, endZ);
		
		float distance = start.distance(end);
		
		float fenceLength = 2.9f*2.0f;
		float fenceNumber = distance / fenceLength;
		// Floors fenceStep to compute again the new
		fenceNumber = FastMath.floor(fenceNumber);
		// Compute the fenceStep now
		float fenceStep = distance / fenceNumber;
		
		Vector2f vStep = end.subtract(start).normalizeLocal().multLocal(fenceStep);
		
		for(Vector2f currentPosition = start.clone(); currentPosition.distance(end) > fenceLength ; currentPosition.addLocal(vStep)) {
			// Create a fence at currentPosition
			Fence f = new Fence();
			f.init(assetManager, camera, rootNode, bulletAppState);
			f.setPosition(currentPosition.x, 0.0f, currentPosition.y);
			// Where we go for the next step
			f.setYRotation(vStep.getAngle());
		}
		
	}
}
