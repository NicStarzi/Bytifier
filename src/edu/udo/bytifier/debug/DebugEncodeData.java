package edu.udo.bytifier.debug;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.EncodeData;

public class DebugEncodeData extends EncodeData {
	
	protected StringBuilder sb = new StringBuilder(2048);
	protected int depth = 0;
	protected int refCountWritePosInSB;
	
	public DebugEncodeData(Bytifier bytifier) {
		super(bytifier);
		
		
		String protoID = Integer.toHexString(bytifier.getProtocolIdentificationNumber());
		String clsSizeStr = Integer.toString(clsSize);
		writeLine("Protocol identification number: ", protoID);
		writeLine("Byte size of class references: ", clsSizeStr);
		writeLine("Expected number of referenced objects: ");
		refCountWritePosInSB = sb.length();
//		String refCountStr = Integer.toString(refMap.length);
//		sb.replace(refCountWritePosInSB, refCountWritePosInSB, refCountStr);
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
	
	public int getCurrentWritePosition() {
		return pos;
	}
	
	public int getBytesWrittenSince(int prevReadPos) {
		return pos - prevReadPos;
	}
	
	public String getReportString() {
		String result = sb.toString();
		return result;
	}
	
}