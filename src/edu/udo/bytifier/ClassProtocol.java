package edu.udo.bytifier;

public interface ClassProtocol {
	
	public void write(Bytifier bytifier, EncodeData data, Object input);
	
	public Object create(Bytifier bytifier, DecodeData data);
	
	public void read(Bytifier bytifier, DecodeData data, Object object);
	
	public default int getMagicNumber() {
		return getClass().getName().hashCode();
	}
	
}