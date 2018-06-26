package edu.udo.bytifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ChunkType {
	
	ILLEGAL,
	NULL,
	VALUE_OBJ_TYPE,
	NEW_OBJ_REF,
	READ_OBJ_REF,
	GENERIC_ARRAY,
	;
	public static final List<ChunkType> ALL
		= Collections.unmodifiableList(Arrays.asList(ChunkType.values()));
	public static final int COUNT = ALL.size();
	
	public final int ID = ordinal();
	
}