package edu.udo.bytifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class EncodeData {
	
	public static final int MIN_BUFFER_SIZE = 16;
	
	protected final Map<Object, Integer> refMap = new IdentityHashMap<>();
	protected final Map<Class<?>, Integer> protocolIdxMap = new HashMap<>();
	protected final byte[] primitiveBuf = new byte[8];
	protected final int clsSize;
	protected final byte[] byteBuf;
	protected List<byte[]> byteBufList;
	protected int pos;
	
	public EncodeData(Bytifier bytifier) {
		this(bytifier, true);
	}
	
	public EncodeData(Bytifier bytifier, boolean writeDefaults) {
		this(bytifier, 1024 * 4, writeDefaults);
	}
	
	public EncodeData(Bytifier bytifier, int bufferSize) {
		this(bytifier, bufferSize, true);
	}
	
	public EncodeData(Bytifier bytifier, int bufferSize, boolean writeDefaults) {
		if (bufferSize < MIN_BUFFER_SIZE) {
			bufferSize = MIN_BUFFER_SIZE;
		}
		byteBuf = new byte[bufferSize];
		int clsCount = bytifier.protocols.size();
		clsSize = DecodeData.calculateByteCountFor(clsCount);
		
		for (int i = 0; i < clsCount; i++) {
			protocolIdxMap.put(bytifier.protocols.get(i).cls, Integer.valueOf(i));
		}
		
		if (writeDefaults) {
			writeInt4(bytifier.magicNum);
			writeInt1(clsSize);
			writeInt4(0);// place holder for reference count. Written to at a later time.
		}
	}
	
	protected void printDebug() {
		byte[] bla = getBytes();
		System.out.println("pos="+pos+"; bytes="+Arrays.toString(bla)+"; buffered="+(byteBufList == null ? 0 : byteBufList.size()));
	}
	
	protected void pushCurrentByteBuffer() {
//		System.out.println("EncodeData.pushCurrentByteBuffer="+Arrays.toString(byteBuf));
		if (byteBufList == null) {
			byteBufList = new ArrayList<>();
		}
		byteBufList.add(Arrays.copyOf(byteBuf, byteBuf.length));
		pos = 0;
	}
	
	public int getProtocolIndexFor(Class<?> clazz) {
		return protocolIdxMap.getOrDefault(clazz, -1);
	}
	
	public int getProtocolIndexFor(Object object) {
		return protocolIdxMap.getOrDefault(object.getClass(), -1);
	}
	
	public int getReferenceIndexFor(Object object) {
		return refMap.getOrDefault(object, -1);
	}
	
	public byte[] getBytes(boolean writeRefCount) {
		// size of each buffer
		int bufSize = byteBuf.length;
		// byteBufList can be null if we only needed a single buffer
		List<byte[]> bufList = byteBufList == null ? Collections.emptyList() : byteBufList;
		// the length of the returned byte array
		// each buffer in bufList is added in full, the last buffer byteBuf is filled up to pos
		int totalByteCount = bufList.size() * bufSize + pos;
		// the returned byte buffer
		byte[] out = new byte[totalByteCount];
		int outPos = 0;
		// write buffers from bufList to out in sequential order
		for (byte[] buf : bufList) {
			System.arraycopy(buf, 0, out, outPos, bufSize);
			outPos += bufSize;
		}
		// write the byteBuf to out up to the position pos
		System.arraycopy(byteBuf, 0, out, outPos, pos);
		if (writeRefCount) {
			// write the current reference count to out
			// Always written to a constant location
			EncodeData.writeInt4(out, 4 + 1, refMap.size());
		}
		return out;
	}
	
	public byte[] getBytes() {
		return getBytes(true);
	}
	
	public void writeIntForSize(int maxValue, int value) {
		int byteCount = DecodeData.calculateByteCountFor(maxValue);
		switch (byteCount) {
		case 1: writeInt1(value);break;
		case 2: writeInt2(value);break;
		case 3: writeInt3(value);break;
		case 4: writeInt4(value);break;
		default: throw new IllegalStateException("byteCount="+byteCount);
		}
	}
	
	public void writeClassIndex(int value) {
		switch (clsSize) {
		case 1: writeInt1(value);break;
		case 2: writeInt2(value);break;
		case 3: writeInt3(value);break;
		case 4: writeInt4(value);break;
		default: throw new IllegalStateException("clsSize="+clsSize);
		}
	}
	
	public void writeNewReferenceIndex(Object object) {
		int refIdx = refMap.size();
		refMap.put(object, Integer.valueOf(refIdx));
	}
	
	public void writeOldReferenceIndex(int value) {
		writeIntForSize(refMap.size(), value);
	}
	
	public void writeBytes(byte[] in) {
		writeBytes(in, 0, in.length);
	}
	
	public void writeBytes(byte[] in, int offset, int length) {
		while (length > 0) {
			int remaining = byteBuf.length - pos;
			if (remaining >= length) {
				System.arraycopy(in, offset, byteBuf, pos, length);
				pos += length;
			} else {
				System.arraycopy(in, offset, byteBuf, pos, remaining);
				offset += remaining;
				pushCurrentByteBuffer();
			}
			length -= remaining;
		}
	}
	
	protected void writePrimitives(int length) {
		writeBytes(primitiveBuf, 0, length);
	}
	
	public void writeBoolean(boolean value) {
		if (value) {
			writeInt1(DecodeData.BOOLEAN_TRUE);
		} else {
			writeInt1(~DecodeData.BOOLEAN_TRUE);
		}
	}
	
	public void writeInt1(int value) {
		primitiveBuf[0] = (byte) (value & 0xFF);
		writePrimitives(1);
	}
	
	public void writeInt2(int value) {
		primitiveBuf[0] = (byte) ((value >> 0) & 0xFF);
		primitiveBuf[1] = (byte) ((value >> 8) & 0xFF);
		writePrimitives(2);
	}
	
	public void writeInt3(int value) {
		primitiveBuf[0] = (byte) ((value >> 0) & 0xFF);
		primitiveBuf[1] = (byte) ((value >> 8) & 0xFF);
		primitiveBuf[2] = (byte) ((value >> 16) & 0xFF);
		writePrimitives(3);
	}
	
	public void writeInt4(int value) {
		primitiveBuf[0] = (byte) ((value >> 0) & 0xFF);
		primitiveBuf[1] = (byte) ((value >> 8) & 0xFF);
		primitiveBuf[2] = (byte) ((value >> 16) & 0xFF);
		primitiveBuf[3] = (byte) ((value >> 24) & 0xFF);
		writePrimitives(4);
	}
	
	public static void writeInt4(byte[] arr, int pos, int value) {
		arr[pos++] = (byte) ((value >> 0) & 0xFF);
		arr[pos++] = (byte) ((value >> 8) & 0xFF);
		arr[pos++] = (byte) ((value >> 16) & 0xFF);
		arr[pos++] = (byte) ((value >> 24) & 0xFF);
	}
	
	public void writeInt8(long value) {
		primitiveBuf[0] = (byte) ((value >> 0) & 0xFF);
		primitiveBuf[1] = (byte) ((value >> 8) & 0xFF);
		primitiveBuf[2] = (byte) ((value >> 16) & 0xFF);
		primitiveBuf[3] = (byte) ((value >> 24) & 0xFF);
		primitiveBuf[4] = (byte) ((value >> 32) & 0xFF);
		primitiveBuf[5] = (byte) ((value >> 40) & 0xFF);
		primitiveBuf[6] = (byte) ((value >> 48) & 0xFF);
		primitiveBuf[7] = (byte) ((value >> 56) & 0xFF);
		writePrimitives(8);
	}
	
	public void writeFloat4(float value) {
		int bits = Float.floatToRawIntBits(value);
		writeInt4(bits);
	}
	
	public void writeFloat8(double value) {
		long bits = Double.doubleToRawLongBits(value);
		writeInt8(bits);
	}
	
	public void writeChunkType(ChunkType value) {
		writeInt1(value.ordinal());
	}
}