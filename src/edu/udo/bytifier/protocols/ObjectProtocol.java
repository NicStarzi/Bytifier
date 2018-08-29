package edu.udo.bytifier.protocols;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class ObjectProtocol implements ClassProtocol {
	
	public static final ObjectProtocol INSTANCE = new ObjectProtocol();
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object input) {}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		return new Object();
	}
	
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {}
	
	@Override
	public int getIdentificationNumber() {
		return 0xCAFEBABE;
	}
	
}