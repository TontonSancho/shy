package org.sanchome.shy.engine.player;

import org.sanchome.shy.engine.IInitable;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.controls.ActionListener;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public interface IPlayer extends IInitable, ActionListener {
	void simpleUpdate(float tpf);
}
