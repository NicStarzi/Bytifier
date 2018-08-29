package edu.udo.bytifier.protocols;

import java.util.ArrayList;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class ArrayListProtocol implements ClassProtocol {
	
	@SuppressWarnings("rawtypes")
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object object) {
		ArrayList list = (ArrayList) object;
		int length = list.size();
		data.writeInt4(length);
		for (int i = 0; i < length; i++) {
			bytifier.writeChunk(data, list.get(i), false);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		return new ArrayList();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		int length = data.readInt4();
		ArrayList list = (ArrayList) object;
		list.ensureCapacity(length);
		for (int i = 0; i < length; i++) {
			list.add(bytifier.readChunk(data));
		}
	}
}