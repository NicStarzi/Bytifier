package edu.udo.bytifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecodeStreamData {
	
	protected final byte[] bytes = new byte[8];
	protected final InputStream in;
	protected final Object[] refMap;
	protected final int clsSize;
	protected final int protoID;
	protected int lastRefIdx;
	
	public DecodeStreamData(byte[] byteArr) throws IOException {
		this(byteArr, true);
	}
	
	public DecodeStreamData(byte[] byteArr, boolean readDefaults) throws IOException {
		in = new ByteArrayInputStream(byteArr);
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
	
	public int getProtocolIdentificationNumber() {
		return protoID;
	}
	
	public boolean hasMoreData() throws IOException {
		return in.available() > 0;
//		return pos < bytes.length;
	}
	
	public int getRemainingBytes() throws IOException {
		return in.available();
//		return bytes.length - pos;
	}
	
	protected byte[] getBytes() throws IOException {
		byte[] bytes = new byte[in.available()];
		in.read(bytes, 0, bytes.length);
		return bytes;
//		return Arrays.copyOfRange(bytes, pos, bytes.length);
	}
	
	public void setObjectReference(Object object) {
		refMap[lastRefIdx++] = object;
	}
	
	public Object readObjectReference() throws IOException {
		int refIdx = readReference();
		return refMap[refIdx];
	}
	
	public int readClassIndex() throws IOException {
		switch (clsSize) {
		case 1: return readInt1();
		case 2: return readInt2();
		case 3: return readInt3();
		case 4: return readInt4();
		default: throw new IllegalStateException("clsSize="+clsSize);
		}
	}
	
	protected int readReference() throws IOException {
		return readIntForSize(lastRefIdx);
	}
	
	public int readIntForSize(int maxValue) throws IOException {
		int byteCount = DecodeData.calculateByteCountFor(maxValue);
		switch (byteCount) {
		case 1: return readInt1();
		case 2: return readInt2();
		case 3: return readInt3();
		case 4: return readInt4();
		default: throw new IllegalStateException("byteCount="+byteCount);
		}
	}
	
	public void readBytes(byte[] out) throws IOException {
		readBytes(out, 0, out.length);
	}
	
	public void readBytes(byte[] out, int offset, int length) throws IOException {
		int count = in.read(out, offset, length);
		if (count == -1) {
			throw new IOException("End of Stream");
		}
		
	}
	
	public boolean readBoolean() throws IOException {
		return readInt1() == DecodeData.BOOLEAN_TRUE;
	}
	
	public byte readInt1() throws IOException {
		return (byte) in.read();
	}
	
	public short readInt2() throws IOException {
		in.read(bytes, 0, 2);
		int pos = 0;
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		return (short) (b1 | b2 << 8);
	}
	
	public int readInt3() throws IOException {
		in.read(bytes, 0, 3);
		int pos = 0;
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		int b3 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16);
	}
	
	public int readInt4() throws IOException {
		in.read(bytes, 0, 4);
		int pos = 0;
		int b1 = bytes[pos++] & 0xFF;
		int b2 = bytes[pos++] & 0xFF;
		int b3 = bytes[pos++] & 0xFF;
		int b4 = bytes[pos++] & 0xFF;
		return (b1 | b2 << 8 | b3 << 16 | b4 << 24);
	}
	
	public long readInt8() throws IOException {
		in.read(bytes, 0, 8);
		int pos = 0;
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
	
	public float readFloat4() throws IOException {
		int bits = readInt4();
		return Float.intBitsToFloat(bits);
	}
	
	public double readFloat8() throws IOException {
		long bits = readInt8();
		return Double.longBitsToDouble(bits);
	}
	
	public ChunkType readChunkType() throws IOException {
		int idx = readInt1();
		if (idx < 0 || idx >= ChunkType.COUNT) {
			return ChunkType.ILLEGAL;
		}
		return ChunkType.ALL.get(idx);
	}
	
	public String readJavaIdentifier() throws IOException {
		int strLen = readInt2();
		char[] strChars = new char[strLen];
		for (int i = 0; i < strLen; i++) {
			strChars[i] = (char) readInt2();
		}
		String result = new String(strChars);
		return result;
	}
}