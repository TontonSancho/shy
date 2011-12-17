package org.sanchome.shy.engine.entity;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public abstract class AEntity {

	private static int ENTITY_ORDER = 0;
	
	protected int order;
	protected Node myNode;
	
	public AEntity() {
		order = ENTITY_ORDER++;
	}
	
	final public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		myNode = new Node(getEntityName()+":Node");
		rootNode.attachChild(myNode);
		initForClient(assetManager, myNode);
		initForServer(assetManager, myNode, bulletAppState);
	}

	public abstract String getEntityName();
	public abstract void initForClient(AssetManager assetManager, Node myNode);
	public abstract void initForServer(AssetManager assetManager, Node myNode, BulletAppState bulletAppState);
	public abstract void setPosition(Vector3f position);
	final public void setPosition(float x, float y, float z) { setPosition(new Vector3f(x, y, z)); };
	public abstract void setYRotation(float yRadian);
	
}
