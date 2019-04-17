package edu.udo.bytifier;

public interface IEncodeData {
	
	public default void writeIntForSize(int maxValue, int value) {
		int byteCount = DecodeData.calculateByteCountFor(maxValue);
		switch (byteCount) {
		case 1: writeInt1(value);break;
		case 2: writeInt2(value);break;
		case 3: writeInt3(value);break;
		case 4: writeInt4(value);break;
		default: throw new IllegalStateException("byteCount == "+byteCount);
		}
	}
	
	public void writeReferenceIndexOf(Object object);
	
	public default void writeBytes(byte[] in) {
		writeBytes(in, 0, in.length);
	}
	
	public void writeBytes(byte[] in, int offset, int length);
	
	public void writeBoolean(boolean value);
	
	public void writeInt1(int value);
	
	public void writeInt2(int value);
	
	public void writeInt3(int value);
	
	public void writeInt4(int value);
	
	public void writeInt8(long value);
	
	public void writeFloat4(float value);
	
	public void writeFloat8(double value);
	
	public void writeChunkType(ChunkType value);
	
	public void writeJavaIdentifier(String str);
	
}