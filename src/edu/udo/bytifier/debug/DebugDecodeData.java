package edu.udo.bytifier.debug;

import edu.udo.bytifier.DecodeData;

public class DebugDecodeData extends DecodeData {
	
	protected StringBuilder sb = new StringBuilder(2048);
	protected int depth = 0;
	
	public DebugDecodeData(byte[] byteArr) {
		super(byteArr);
		
		String protoID = Integer.toHexString(getProtocolIdentificationNumber());
		String clsSizeStr = Integer.toString(clsIdxBSize);
		String refCountStr = Integer.toString(refMap.length);
		writeLine("Protocol identification number: ", protoID);
		writeLine("Byte size of class references: ", clsSizeStr);
		writeLine("Expected number of referenced objects: ", refCountStr);
	}
	
	@Override
	public void pushObjectReference(Object object) {
		int refIdx = lastRefIdx;
		super.pushObjectReference(object);
		writeLine("Object reference index: ", Integer.toString(refIdx));
	}
	
	public int getNextObjectReferenceSizeInBytes() {
		return DecodeData.calculateByteCountFor(lastRefIdx);
	}
	
	public int getNextObjectReference() {
		// it is important to calculate byteCount BEFORE reading the reference
		// as reading it may change the size of future reference indices.
		int byteCount = getNextObjectReferenceSizeInBytes();
		int result = readReference();
		pos -= byteCount;
		return result;
	}
	
	public void incrementDepth() {
		writeLine(">>>");
		depth++;
	}
	
	public void decrementDepth() {
		depth--;
		writeLine("<<<");
	}
	
	public void indent() {
		for (int i = 0; i < depth; i++) {
			sb.append('\t');
		}
	}
	
	public void writeLine(Object ... args) {
		indent();
		for (Object obj : args) {
			sb.append(obj);
		}
		sb.append('\n');
	}
	
	public int getCurrentReadPosition() {
		return pos;
	}
	
	public int getBytesReadSince(int prevReadPos) {
		return pos - prevReadPos;
	}
	
	public String getReportString() {
		String result = sb.toString();
		sb = null;
		return result;
	}
	
}