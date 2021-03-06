package edu.udo.bytifier.protocols;

import java.util.Collection;
import java.util.function.Supplier;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class CollectionProtocol implements ClassProtocol {
	
	protected final Supplier<? extends Collection<?>> constructor;
	
	public CollectionProtocol(Supplier<? extends Collection<?>> constructor) {
		this.constructor = constructor;
	}
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object object) {
		Collection<?> collection = (Collection<?>) object;
		int length = collection.size();
		data.writeInt4(length);
		for (Object elem : collection) {
			bytifier.writeChunk(data, elem, false);
		}
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		return constructor.get();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		int length = data.readInt4();
		Collection collection = (Collection) object;
		for (int i = 0; i < length; i++) {
			collection.add(bytifier.readChunk(data));
		}
	}
}