package org.sanchome.shy.engine.network.message;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class EntityState {

	public String   entityId;
	public Vector3f position;
	public Vector3f orientation;

	public EntityState() {}

	public EntityState(String entityId, Vector3f position, Vector3f orientation) {
		this.entityId = entityId;
		this.position = position;
		this.orientation = orientation;
	}
}
