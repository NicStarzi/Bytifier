package edu.udo.bytifier.protocols;

import java.util.Collection;
import java.util.function.Supplier;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class CollectionProtocol<T> implements ClassProtocol {
	
	protected final Supplier<Collection<T>> constructor;
	
	public CollectionProtocol(Supplier<Collection<T>> constructor) {
		this.constructor = constructor;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object object) {
		Collection<T> col = (Collection<T>) object;
		int length = col.size();
		data.writeInt4(length);
		for (T elem : col) {
			bytifier.writeChunk(data, elem, false);
		}
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		return constructor.get();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		int length = data.readInt4();
		Collection<T> list = (Collection<T>) object;
		for (int i = 0; i < length; i++) {
			list.add((T) bytifier.readChunk(data));
		}
	}
}