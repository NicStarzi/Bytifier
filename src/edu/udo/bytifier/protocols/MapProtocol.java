package edu.udo.bytifier.protocols;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class MapProtocol implements ClassProtocol {
	
	private final Supplier<?> cnstrctr;
	
	public <MT extends Map<?, ?>> MapProtocol(Supplier<MT> constructor) {
		cnstrctr = constructor;
	}
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object object) {
		Map<?, ?> map = (Map<?, ?>) object;
		int length = map.size();
		data.writeInt4(length);
		for (Entry<?, ?> entry : map.entrySet()) {
			bytifier.writeChunk(data, entry.getKey(), false);
			bytifier.writeChunk(data, entry.getValue(), false);
		}
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		return cnstrctr.get();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		int length = data.readInt4();
		Map map = (Map) object;
		for (int i = 0; i < length; i++) {
			Object key = bytifier.readChunk(data);
			Object val = bytifier.readChunk(data);
			map.put(key, val);
		}
	}
}