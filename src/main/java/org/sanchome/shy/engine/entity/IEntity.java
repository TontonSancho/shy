package org.sanchome.shy.engine.entity;

import org.sanchome.shy.engine.IInitable;

public interface IEntity extends IInitable {

	void detach();
	
	boolean isStabilized();

	void enableStabilization();

	void restoreNormalPhysics();

}
