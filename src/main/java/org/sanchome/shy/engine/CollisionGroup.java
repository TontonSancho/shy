package org.sanchome.shy.engine;

public class CollisionGroup {
	
	public static final int TERRAIN        = 0x00000001;
	public static final int TREES          = 0x00000002;
	public static final int CRATES         = 0x00000004;
	public static final int SHEEP_BODY     = 0x00000008;
	public static final int SHEEP_WHEELS   = 0x00000010;
	public static final int PLAYER_CAPSULE = 0x00000020;
	public static final int PLAYER_FOOT    = 0x00000040;
	public static final int FENCES         = 0x00000080;
	
	// See root README
	// for more information about this composition ...
	public static final int TERRAIN_COLLISION_MASK        = TREES | CRATES | SHEEP_BODY | SHEEP_WHEELS | PLAYER_CAPSULE | FENCES;
	public static final int TREES_COLLISION_MASK          = TERRAIN | CRATES | SHEEP_BODY | PLAYER_CAPSULE;
	public static final int CRATES_COLLISION_MASK         = TERRAIN | TREES | CRATES | SHEEP_BODY | PLAYER_CAPSULE | PLAYER_FOOT | FENCES;
	public static final int SHEEP_BODY_COLLISION_MASK     = TERRAIN | TREES | CRATES | SHEEP_BODY | PLAYER_CAPSULE | PLAYER_FOOT | FENCES;
	public static final int SHEEP_WHEELS_COLLISION_MASK   = TERRAIN;
	public static final int PLAYER_CAPSULE_COLLISION_MASK = TERRAIN | TREES | CRATES | SHEEP_BODY | FENCES;
	public static final int PLAYER_FOOT_COLLISION_MASK    = CRATES | SHEEP_BODY;
	public static final int FENCES_COLLISION_MASK         = TERRAIN | CRATES | SHEEP_BODY | PLAYER_CAPSULE;
	
	
	
	//
	// Not yet used
	public static final int COLLISION_GROUP_09 = 0x00000100;
	public static final int COLLISION_GROUP_10 = 0x00000200;
	public static final int COLLISION_GROUP_11 = 0x00000400;
	public static final int COLLISION_GROUP_12 = 0x00000800;
	public static final int COLLISION_GROUP_13 = 0x00001000;
	public static final int COLLISION_GROUP_14 = 0x00002000;
	public static final int COLLISION_GROUP_15 = 0x00004000;
	public static final int COLLISION_GROUP_16 = 0x00008000;
	public static final int COLLISION_GROUP_17 = 0x00010000;
	public static final int COLLISION_GROUP_18 = 0x00020000;
	public static final int COLLISION_GROUP_19 = 0x00040000;
	public static final int COLLISION_GROUP_20 = 0x00080000;
	public static final int COLLISION_GROUP_21 = 0x00100000;
	public static final int COLLISION_GROUP_22 = 0x00200000;
	public static final int COLLISION_GROUP_23 = 0x00400000;
	public static final int COLLISION_GROUP_24 = 0x00800000;
	public static final int COLLISION_GROUP_25 = 0x01000000;
	public static final int COLLISION_GROUP_26 = 0x02000000;
	public static final int COLLISION_GROUP_27 = 0x04000000;
	public static final int COLLISION_GROUP_28 = 0x08000000;
	public static final int COLLISION_GROUP_29 = 0x10000000;
	public static final int COLLISION_GROUP_30 = 0x20000000;
	public static final int COLLISION_GROUP_31 = 0x40000000;
	public static final int COLLISION_GROUP_32 = 0x80000000;
    
}
