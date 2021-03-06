package org.sanchome.shy.engine;


public class UserSettings {
	
	public static int SHEEP_NUMBER = 1;
	
	public static int TREE_NUMBER =  2;
	
	public static int CRATE_NUMBER = 0;

	public static int FENCE_NUMBER = 10;
	
	public static boolean TERRAIN_LOD_EXAGGERATION = true;
	
	public static enum ShadowMode {
		NONE,
		BASIC,
		PSSM
	}
	
	public static ShadowMode SHADOW_MODE = ShadowMode.BASIC;
	
	/**
	 *  512, 1024, 2048, ...
	 */
	public static int SHADOW_MODE_DEFINITION = 1204;
	
	public static enum ShadowDetails {
		SIMPLE,
		FULL
	}
	
	public static ShadowDetails SHADOW_DETAIL = ShadowDetails.FULL;

	
}
