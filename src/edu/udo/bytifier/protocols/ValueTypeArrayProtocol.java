package edu.udo.bytifier.protocols;

import java.lang.reflect.Array;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class ValueTypeArrayProtocol<T> implements ClassProtocol {
	
	protected final Class<T> elemType;
	protected final boolean valType;
	
	public ValueTypeArrayProtocol(Class<T> elementType, boolean isValueType) {
		elemType = elementType;
		valType = isValueType;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object object) {
		T[] arr = (T[]) object;
		data.writeInt3(arr.length);
		for (int i = 0; i < arr.length; i++) {
			bytifier.writeChunk(data, arr[i], valType);
		}
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		int length = data.readInt3();
		return Array.newInstance(elemType, length);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		T[] arr = (T[]) object;
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (T) bytifier.readChunk(data);
		}
	}
}