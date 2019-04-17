package edu.udo.bytifier;

import java.lang.reflect.Array;
import java.util.Arrays;

public class DecodeData {
	/*
- Now supports serialization and deserialization of arbitrary enum types. A new chunk type UNKNOWN_ENUM has been introduced for this purpose.
- ProtocolTuple class has been moved to its own file. It is no longer an inner class of Bytifier.
- Bytifier: previously public read methods have been made protected.
- Bytifier: Error messages have been improved.
- ChunkType: Added java doc.
- ClassProtocol: Added java doc.
- DecodeData: Added a readJavaIdentifier() method to the public API.
- DecodeData: Added java doc.
- DecodeData: Improved code readability.
- DecodeData: Renamed several methods to give them more descriptive names.
- EncodeData: Added a writeJavaIdentifier(String) method to the public API.
- EncodeData: Refactored the code and split into interface and implementation.
- PerClassBuilder: Refactored the code.
- PerClassBuilder: Improved error messages.
- ProtocolBuilder: Refactored the code.
- ProtocolBuilder: Added more convenience methods. Now serialization of Collections and Maps can more easily be defined.
- ArrayListProtocol, CollectionProtocol: Removed unnecessary generics and @SuppressWarnings annotations.
- MapProtocol: Added a default ClassProtocol implementation for arbitrary Maps.
- ReflectionClassProtocol, UnknownClassProtocol, @ValueType: Added the @ValueType annotation to denote classes or attributes as value types. When using the ReflectionClassProtocol or the UnknownClassProtocol the annotation will be usd to determine whether to write an object by reference or by value.
- ReflectionClassProtocol, UnknownClassProtocol: Refactored the code.
- Test-Cases: Added more tests and improved existing test-cases.
	 */
	public static final byte BOOLEAN_TRUE = 0b01010101;
	public static final int MAX_INT_1 = (1 << (8 * 1));
	public static final int MAX_INT_2 = (1 << (8 * 2));
	public static final int MAX_INT_3 = (1 << (8 * 3));
	public static final int MAX_INT_4 = Integer.MAX_VALUE;
	
	protected final byte[] bytes;
	protected final Object[] refMap;
	protected final int clsIdxBSize;
	protected final int protoID;
	protected int pos;
	protected int lastRefIdx;
	
	public DecodeData(byte[] byteArr) {
		this(byteArr, true);
	}
	
	public DecodeData(byte[] byteArr, boolean readDefaults) {
		bytes = byteArr;
		pos = 0;
		if (readDefaults) {
			protoID = readInt4();
			clsIdxBSize = readInt1();
			
			int refCount = readInt4();
			if (refCount == 0) {
				refMap = null;
			} else {
				refMap = new Object[refCount];
			}
		} else {
			protoID = 0;
			clsIdxBSize = 0;
			refMap = null;
		}
	}
	
	protected void printDebug() {
		byte[] bytes = copyRemainingBytes();
		System.out.println("pos="+pos+"; bytes="+Arrays.toString(bytes));
	}
	
	/**<p>
	 * Calculates and returns how many bytes are needed to store the given int value.
	 * <p>
	 * The value is regarded as an unsigned int. Negative arguments are illegal and
	 * will result in an {@link IllegalArgumentException}.
	 * <p>
	 * This method will return:
	 * <ul>
	 * <li>1 for 0 <= value < {@value #MAX_INT_1}
	 * <li>2 for {@value #MAX_INT_1} <= value < {@value #MAX_INT_2}
	 * <li>3 for {@value #MAX_INT_2} <= value < {@value #MAX_INT_3}
	 * <li>4 for {@value #MAX_INT_3} <= value < {@value #MAX_INT_4}
	 * </ul>
	 * 
	 * @param value		the maximum non-negative unsigned int value that should
	 * 					be possible to store in the returned number of bytes
	 * @return			the number of bytes necessary to store values between
	 * 					0 and the given argument as unsigned integers
	 */
	public static int calculateByteCountFor(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("Only non-negative inputs are supported. value == "+value);
		}
		if (value < MAX_INT_1) {
			return 1;
		}
		if (value < MAX_INT_2) {
			return 2;
		}
		if (value < MAX_INT_3) {
			return 3;
		}
		return 4;
	}
	
	/**<p>
	 * Returns the protocol identification number that was written to the backing data.
	 * <p>
	 * If the two parameter constructor {@link #DecodeData(byte[], boolean)} was used
	 * and {@code false} was passed as the second argument, the protocol identification
	 * number has not been read and will default to zero.
	 * <p>
	 * Calling this method does not advance the read position.
	 * 
	 * @return	the protocol identification number read from the backing data
	 */
	public int getProtocolIdentificationNumber() {
		return protoID;
	}
	
	/**<p>
	 * Returns the number of bytes necessary to read a single class index from the
	 * backing data. This number is written to the header of the backing data and
	 * reflects the number of classes that are part of the protocol.
	 * <p>
	 * If the two parameter constructor {@link #DecodeData(byte[], boolean)} was used
	 * and {@code false} was passed as the second argument, the class index byte count
	 * has not been read and will default to zero.
	 * <p>
	 * Calling this method does not advance the read position.
	 * 
	 * @return	the number of bytes needed to read a class index from the backing data
	 * @see #readClassIndex()
	 */
	public int getClassIndexByteSize() {
		return clsIdxBSize;
	}
	
	/**<p>
	 * Returns {@code true} if the current read position as not reached the end of the
	 * backing data yet.
	 * <p>
	 * Returns {@code false} otherwise.
	 * 
	 * @return	{@code true} if more data can be read. Otherwise {@code false}.
	 * @see #getRemainingByteCount()
	 * @see #copyRemainingBytes()
	 */
	public boolean hasMoreData() {
		return pos < bytes.length;
	}
	
	/**<p>
	 * Returns the number of bytes that are remaining to be read in the backing data.
	 * <p>
	 * The returned value is the difference between the total size of the backing data
	 * and the current read position. The returned value is never negative.
	 * 
	 * @return	The number of bytes remaining to be read
	 * @see #hasMoreData()
	 * @see #copyRemainingBytes()
	 */
	public int getRemainingByteCount() {
		return bytes.length - pos;
	}
	
	/**<p>
	 * Copies all unread bytes from the backing data to a new array and returns it.
	 * The returned array has a {@link Array#getLength(Object) length} equal to
	 * {@link #getRemainingByteCount()}.
	 * 
	 * @return	A copy of all the bytes that have not been read yet
	 * @see #hasMoreData()
	 * @see #getRemainingByteCount()
	 */
	public byte[] copyRemainingBytes() {
		return Arrays.copyOfRange(bytes, pos, bytes.length);
	}
	
	public void pushObjectReference(Object object) {
		refMap[lastRefIdx++] = object;
	}
	
	/**<p>
	 * TODO Reads and returns the index of an object reference. The returned value is
	 * always a non-negative number. This method does not guarantee that the returned
	 * value is a valid reference index.
	 * <p>
	 * The read position will be advanced by a number of bytes which depends on the
	 * current value of {@link #lastRefIdx}.
	 * 
	 * @return an always non-negative java int read from the backing data
	 */
	public Object readObjectReference() {
		int refIdx = readReference();
		return refMap[refIdx];
	}
	
	/**<p>
	 * Calculates {@link #getClassIndexByteSize() how many bytes are needed to store a class index},
	 * reads that many bytes and returns them as an int. The returned value will never be a
	 * negative numbers.
	 * <p>
	 * The read position will be advanced by as many bytes as where needed to store a
	 * class index. The size of a class index depend on the number of classes that are
	 * part of the protocol.
	 * 
	 * @return	always a non-negative java int read from the backing data.
	 * 			The returned value can be used as an index to a class in the protocol.
	 * @see #getClassIndexByteSize()
	 */
	public int readClassIndex() {
		switch (clsIdxBSize) {
		case 1: return readInt1();
		case 2: return readInt2();
		case 3: return readInt3();
		case 4: return readInt4();
		default: throw new IllegalStateException("clsSize="+clsIdxBSize);
		}
	}
	
	/**<p>
	 * Reads and returns the index of an object reference. The returned value is
	 * always a non-negative number. This method does not guarantee that the returned
	 * value is a valid reference index.
	 * <p>
	 * The read position will be advanced by a number of bytes which depends on the
	 * current value of {@link #lastRefIdx}.
	 * 
	 * @return an always non-negative java int read from the backing data
	 */
	protected int readReference() {
		return readIntForSize(lastRefIdx);
	}
	
	/**<p>
	 * Calculates how many bytes are needed to store unsigned integers between 0 and
	 * {@code maxValue}, reads that many bytes and returns them as an int. The returned
	 * value will never be a negative numbers.
	 * <p>
	 * The read position will be advanced by as many bytes as where necessary to
	 * store unsigned integers up to {@code maxValue}.
	 * 
	 * @return an always non-negative java int read from the backing data
	 */
	public int readIntForSize(int maxValue) {
		int byteCount = DecodeData.calculateByteCountFor(maxValue);
		switch (byteCount) {
		case 1: return readInt1();
		case 2: return readInt2();
		case 3: return readInt3();
		case 4: return readInt4();
		default: throw new IllegalStateException("byteCount="+byteCount);
		}
	}
	
	/**<p>
	 * Reads as many bytes as the {@link Array#getLength(Object) length} of
	 * {@code out} starting from the current read position and writes them to
	 * {@code out}.
	 * <p>
	 * The read position will be advanced by as many bytes as the
	 * {@link Array#getLength(Object) length of out}.
	 */
	public void readBytes(byte[] out) {
		readBytes(out, 0, out.length);
	}
	
	/**<p>
	 * Reads {@code length} many bytes starting from the current read position
	 * and writes them to {@code out} starting with the first read byte written
	 * to {@code offset}.
	 * <p>
	 * The method {@link System#arraycopy(Object, int, Object, int, int)} is
	 * used for the transfer of bytes. An {@link IndexOutOfBoundsException} is
	 * thrown accordingly for illegal values of {@code offset} or {@code length}.
	 * <p>
	 * The read position will be advanced by {@code length}
	 * many bytes.
	 */
	public void readBytes(byte[] out, int offset, int length) {
		System.arraycopy(bytes, pos, out, offset, length);
		pos += length;
	}
	
	/**
	 * Reads a single boolean value from the backing byte array
	 * and returns it. The read position will be advanced by one byte.
	 * 
	 * @return a java boolean read from the backing data
	 */
	public boolean readBoolean() {
		return bytes[pos++] == BOOLEAN_TRUE;
	}
	
	/**
	 * Reads a single 8bit integer number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java byte read from the backing data
	 */
	public byte readInt1() {
		return bytes[pos++];
	}
	
	/**
	 * Reads a single 16bit integer number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java short read from the backing data
	 */
	public short readInt2() {
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		return (short) (b1 | b2 << 8);
	}
	
	/**
	 * Reads a single 24bit integer number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java int read from the backing data; the highest 8 bits of the returned int will always be zeroes
	 */
	public int readInt3() {
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		int b3 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16);
	}
	
	/**
	 * Reads a single 32bit integer number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java int read from the backing data
	 */
	public int readInt4() {
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		int b3 = bytes[pos++] & 0xFF;
		int b4 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16 | b4 << 24);
	}
	
	/**
	 * Reads a single 64bit integer number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java long read from the backing data
	 */
	public long readInt8() {
		long b1 = bytes[pos++] & 0xFF;
		long b2 = bytes[pos++] & 0xFF;
		long b3 = bytes[pos++] & 0xFF;
		long b4 = bytes[pos++] & 0xFF;
		long b5 = bytes[pos++] & 0xFF;
		long b6 = bytes[pos++] & 0xFF;
		long b7 = bytes[pos++] & 0xFF;
		long b8 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16 | b4 << 24
				| b5 << 32 | b6 << 40 | b7 << 48 | b8 << 56);
	}
	
	/**
	 * Reads a single 32bit floating point number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java float read from the backing data
	 */
	public float readFloat4() {
		int bits = readInt4();
		return Float.intBitsToFloat(bits);
	}
	
	/**
	 * Reads a single 64bit floating point number from the backing byte array
	 * and returns it. The read position will be advanced accordingly.
	 * 
	 * @return a java double read from the backing data
	 */
	public double readFloat8() {
		long bits = readInt8();
		return Double.longBitsToDouble(bits);
	}
	
	/**
	 * Reads and returns a {@link ChunkType}. This method will never return a
	 * {@code null} reference. Unexpected data will result in the
	 * {@link ChunkType#ILLEGAL illegal chunk type} being returned. The read
	 * position will be advanced by one byte.
	 * 
	 * @return a non-null ChunkType literal; {@link ChunkType#ILLEGAL} for unexpected data
	 */
	public ChunkType readChunkType() {
		int idx = readInt1();
		if (idx < 0 || idx >= ChunkType.COUNT) {
			return ChunkType.ILLEGAL;
		}
		return ChunkType.ALL.get(idx);
	}
	
	/**<p>
	 * Reads and returns a newly constructed {@link String} from the backing
	 * byte array. The {@link String#length() string length} is encoded as a
	 * 16bit integer. Each character of the string is also encoded using 16bit.
	 * <p>
	 * The exact number of bytes by which the read position will be advanced
	 * is dynamic and depends on the length of the returned string. It will be
	 * no less than two bytes plus two bytes for each character of the string.
	 * <p>
	 * This method will never return a {@code null} reference but it may return
	 * an {@link String#isEmpty() empty string}.
	 * 
	 * @return a non-null String
	 */
	public String readJavaIdentifier() {
		int strLen = readInt2();
		if (strLen == 0) {
			return "";
		}
		char[] strChars = new char[strLen];
		for (int i = 0; i < strLen; i++) {
			strChars[i] = (char) readInt2();
		}
		String result = new String(strChars);
		return result;
	}
}