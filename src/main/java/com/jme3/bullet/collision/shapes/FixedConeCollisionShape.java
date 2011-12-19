package com.jme3.bullet.collision.shapes;

public class FixedConeCollisionShape extends ConeCollisionShape {

	public FixedConeCollisionShape(float radius, float height, int axis) {
		this.radius = radius;
		this.height = height;
		this.axis = axis;
		createShape();
	}
	
}
