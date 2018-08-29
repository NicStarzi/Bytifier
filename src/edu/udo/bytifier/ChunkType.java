package edu.udo.bytifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ChunkType {
	
	ILLEGAL			("Illegal"),
	NULL			("Null Pointer"),
	VALUE_OBJ_TYPE	("Value Type Object"),
	NEW_OBJ_REF		("New Object"),
	READ_OBJ_REF	("Reference to known Object"),
	GENERIC_ARRAY	("Array Default Protocol"),
	UNKNOWN_OBJ		("Object of unknown type"),
	;
	public static final List<ChunkType> ALL
		= Collections.unmodifiableList(Arrays.asList(ChunkType.values()));
	public static final int COUNT = ALL.size();
	
	public final int IDX = ordinal();
	private final String properName;
	
	private ChunkType(String humanReadableName) {
		properName = humanReadableName;
	}
	
	public String getProperName() {
		return properName;
	}
	
}