package org.sanchome.shy.engine;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Application extends SimpleApplication {
	@Override
	public void simpleInitApp() {
		Box b = new Box(Vector3f.ZERO, 1, 1, 1); // create cube shape at the origin
		Geometry geom = new Geometry("Box", b); // create cube geometry from the shape
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple material
		mat.setColor("Color", ColorRGBA.Blue); // set color of material to blue
		geom.setMaterial(mat); // set the cube's material
		rootNode.attachChild(geom); // make the cube appear in the scene
	}
}
