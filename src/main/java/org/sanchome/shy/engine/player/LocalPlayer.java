package org.sanchome.shy.engine.player;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class LocalPlayer implements IPlayer {

	private Camera camera;
	private CharacterControl player;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;

	public void init(AssetManager assetManager, Camera camera, Node rootNode, BulletAppState bulletAppState) {
		this.camera = camera;
		
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
		player = new CharacterControl(capsuleShape, 0.05f);
		player.setJumpSpeed(20);
		player.setFallSpeed(30);
		player.setGravity(30);
		player.setPhysicsLocation(new Vector3f(0, 10, 0));
		
	    bulletAppState.getPhysicsSpace().add(player);
	}
	
	public void simpleUpdate(float tpf) {
		Vector3f camDir = camera.getDirection().clone().multLocal(0.6f);
		Vector3f camLeft = camera.getLeft().clone().multLocal(0.4f);
		walkDirection.set(0, 0, 0);
		if (left)  { walkDirection.addLocal(camLeft); }
		if (right) { walkDirection.addLocal(camLeft.negate()); }
		if (up)    { walkDirection.addLocal(camDir); }
		if (down)  { walkDirection.addLocal(camDir.negate()); }
		player.setWalkDirection(walkDirection);
		camera.setLocation(player.getPhysicsLocation());
	}
	
	public void onAction(String binding, boolean value, float tpf) {
		
		if (binding.equals("FLYCAM_StrafeLeft")) {
			if (value) { left = true; } else { left = false; }
		} else if (binding.equals("FLYCAM_StrafeRight")) {
			if (value) { right = true; } else { right = false; }
		} else if (binding.equals("FLYCAM_Forward")) {
			if (value) { up = true; } else { up = false; }
		} else if (binding.equals("FLYCAM_Lower")) {
			if (value) { down = true; } else { down = false; }
		} else if (binding.equals("Jump")) {
			player.jump();
		}
	}
}
