package org.sanchome.shy.engine.world;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public interface IWorld {
	void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState);
}
