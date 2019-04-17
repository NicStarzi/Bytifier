package edu.udo.bytifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.udo.bytifier.protocols.UnknownClassProtocol;

/**<p>
 * A chunk is a continuous block of data corresponding either to a java object and its
 * members or a null reference. Each chunk is prefixed by its type, a single byte indicating
 * how the chunk is to be interpreted. The {@link Bytifier} defines a method to read and
 * write chunk types.
 * <p>
 * This type is an implementation detail of the bytifier framework. It is not necessary
 * for a user of the framework to ever use this type.
 * 
 * @author Nic Starzi
 */
public enum ChunkType {
	
	/**
	 * This chunk type is never written out and is never supposed to be read. Any chunk type
	 * indices outside of the valid range will automatically be mapped to this chunk type.
	 */
	ILLEGAL			("Illegal"),
	/**
	 * Represents a null reference. The chunk contains no further data.
	 */
	NULL			("Null Pointer"),
	/**
	 * Represents an object written by value. Any time a value type object is read a new
	 * instance should be created. When value type objects are written they do not write a
	 * {@link EncodeData#writeNewReferenceIndex(Object) reference index}.
	 */
	VALUE_OBJ_TYPE	("Value Type Object"),
	/**
	 * Represents the first occurrence of a plain old java object. The first occurrence
	 * of a referenced object is the only time its members are written. Any further
	 * occurrences will only write an index into the reference map. When a new object
	 * reference is read the object is instantiated and written to the reference map.
	 */
	NEW_OBJ_REF		("New Object"),
	/**
	 * Represents a reference to an object which was already written at some point in the
	 * past. This chunk contains only an index into the reference map where the object
	 * can be obtained from.
	 */
	READ_OBJ_REF	("Reference to known Object"),
	/**
	 * Represents any standard java array for an element type which is known to the protocol.
	 * Arrays define their dimension and length followed by their element data. Arrays of a
	 * larger dimension than 1 have other arrays or null pointers as their element data.
	 */
	GENERIC_ARRAY	("Array Default Protocol"),
	/**
	 * Represents the first occurrence to an object of a type that is unknown to the protocol.
	 * The first occurrence of a referenced object is the only time its members are written. Any
	 * further occurrences will only write an index into the reference map.<p>
	 * The {@link UnknownClassProtocol} should be used to read and write the chunk data.
	 */
	UNKNOWN_OBJ		("Object of unknown type"),
	/**
	 * Represents an enum literal for an enum type that is unknown to the protocol. The chunk
	 * contains the full class name for the enum type and the {@link Enum#ordinal() ordinal number}
	 * of the literal. The ordinal number is written with a
	 * {@link DecodeData#calculateByteCountFor(int) dynamic size}.
	 */
	UNKNOWN_ENUM	("Enum literal of unknown type"),
	;
	/** An  {@link Collections#unmodifiableList(List) unmodifiable list} containing all literals of this {@link Enum}.*/
	public static final List<ChunkType> ALL
		= Collections.unmodifiableList(Arrays.asList(ChunkType.values()));
	/** The number of {@link #values() enum literals} in this {@link Enum} */
	public static final int COUNT = ALL.size();
	
	/** The same as the {@link #ordinal()} of this enum literal.*/
	public final int IDX = ordinal();
	/** A human readable name */
	private final String properName;
	
	private ChunkType(String humanReadableName) {
		properName = humanReadableName;
	}
	
	/**
	 * <p>Returns a human readable name for this literal.</p>
	 * @return	a human readable name. Never returns {@code null}.
	 */
	public String getProperName() {
		return properName;
	}
	
}