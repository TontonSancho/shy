package org.sanchome.shy.engine.entity;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class AEntity implements IEntity {

	private static int ENTITY_ORDER = 0;
	
	protected int order;
	
	public AEntity() {
		order = ENTITY_ORDER++;
	}
	
	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		// TODO Auto-generated method stub

	}

	public void detach() {
		// TODO Auto-generated method stub

	}

	public boolean isStabilized() {
		// TODO Auto-generated method stub
		return false;
	}

	public void enableStabilization() {
		// TODO Auto-generated method stub

	}

	public void restoreNormalPhysics() {
		// TODO Auto-generated method stub

	}

}
