package edu.udo.bytifier;

import java.util.Arrays;

public class DecodeData {
	
	public static final byte BOOLEAN_TRUE = 0b01010101;
	public static final int MAX_INT_1 = (1 << (8 * 1));
	public static final int MAX_INT_2 = (1 << (8 * 2));
	public static final int MAX_INT_3 = (1 << (8 * 3));
	public static final int MAX_INT_4 = Integer.MAX_VALUE;
	
	protected final byte[] bytes;
	protected final Object[] refMap;
	protected final int clsSize;
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
			clsSize = readInt1();
			
			int refCount = readInt4();
			if (refCount == 0) {
				refMap = null;
			} else {
				refMap = new Object[refCount];
			}
		} else {
			protoID = 0;
			clsSize = 0;
			refMap = null;
		}
	}
	
	protected void printDebug() {
		byte[] bytes = getBytes();
		System.out.println("pos="+pos+"; bytes="+Arrays.toString(bytes));
	}
	
	public static int calculateByteCountFor(int value) {
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
	
	public int getProtocolIdentificationNumber() {
		return protoID;
	}
	
	public boolean hasMoreData() {
		return pos < bytes.length;
	}
	
	public int getRemainingBytes() {
		return bytes.length - pos;
	}
	
	protected byte[] getBytes() {
		return Arrays.copyOfRange(bytes, pos, bytes.length);
	}
	
	public void setObjectReference(Object object) {
		refMap[lastRefIdx++] = object;
	}
	
	public Object readObjectReference() {
		int refIdx = readReference();
		return refMap[refIdx];
	}
	
	public int readClassIndex() {
		switch (clsSize) {
		case 1: return readInt1();
		case 2: return readInt2();
		case 3: return readInt3();
		case 4: return readInt4();
		default: throw new IllegalStateException("clsSize="+clsSize);
		}
	}
	
	protected int readReference() {
		return readIntForSize(lastRefIdx);
	}
	
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
	
	public void readBytes(byte[] out) {
		readBytes(out, 0, out.length);
	}
	
	public void readBytes(byte[] out, int offset, int length) {
		System.arraycopy(bytes, pos, out, offset, length);
		pos += length;
	}
	
	public boolean readBoolean() {
		return bytes[pos++] == BOOLEAN_TRUE;
	}
	
	public byte readInt1() {
		return bytes[pos++];
	}
	
	public short readInt2() {
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		return (short) (b1 | b2 << 8);
	}
	
	public int readInt3() {
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		int b3 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16);
	}
	
	public int readInt4() {
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		int b3 = bytes[pos++] & 0xFF;
		int b4 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16 | b4 << 24);
	}
	
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
	
	public float readFloat4() {
		int bits = readInt4();
		return Float.intBitsToFloat(bits);
	}
	
	public double readFloat8() {
		long bits = readInt8();
		return Double.longBitsToDouble(bits);
	}
	
	public ChunkType readChunkType() {
		int idx = readInt1();
		if (idx < 0 || idx >= ChunkType.COUNT) {
			return ChunkType.ILLEGAL;
		}
		return ChunkType.ALL.get(idx);
	}
	
}