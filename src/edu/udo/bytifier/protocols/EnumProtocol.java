package edu.udo.bytifier.protocols;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class EnumProtocol<T extends Enum<T>> implements ClassProtocol {
	
	private final T[] values;
	
	public EnumProtocol(Class<T> enumCls) {
		values = enumCls.getEnumConstants();
	}
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object input) {
		@SuppressWarnings("unchecked")
		T enumConst = (T) input;
		data.writeIntForSize(values.length, enumConst.ordinal());
	}
	
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		// intentionally left blank
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		int ordinal = data.readIntForSize(values.length);
		return values[ordinal];
	}
	
	@Override
	public int getIdentificationNumber() {
		int prime = 37;
		int magNum = getClass().getName().hashCode();
		if (values.length == 0) {
			return magNum;
		}
		magNum += prime * values.length;
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) values[0].getClass();
		magNum += prime * cls.getName().hashCode();
		return magNum;
	}
}