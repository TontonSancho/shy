package org.sanchome.shy.engine.player;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.controls.ActionListener;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public interface IPlayer extends ActionListener {
	void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState);
	void simpleUpdate(float tpf);
}
