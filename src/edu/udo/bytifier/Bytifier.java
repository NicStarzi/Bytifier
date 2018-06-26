package edu.udo.bytifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Bytifier {
	
	public static final byte CHUNK_TYPE_NULL = 0;
	public static final byte CHUNK_TYPE_OBJ_BY_VALUE = 1;
	public static final byte CHUNK_TYPE_NEW_OBJ_REF = 2;
	public static final byte CHUNK_TYPE_READ_OBJ_REF = 3;
	
	public static class ProtocolTuple {
		public final Class<?> cls;
		public final ClassProtocol proto;
		public ProtocolTuple(Class<?> protocolClass, ClassProtocol protocol) {
			cls = protocolClass;
			proto = protocol;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(cls.getName());
			sb.append("-[");
			sb.append(proto);
			sb.append("]-{");
			sb.append(proto.hashCode());
			sb.append("}");
			return sb.toString();
		}
	}
	protected final List<ProtocolTuple> protocols;
	protected final int magicNum;
	
	public Bytifier() {
		this(Collections.emptyList());
	}
	
	public Bytifier(ProtocolTuple ... protocols) {
		this(Arrays.asList(protocols));
	}
	
	public Bytifier(Collection<ProtocolTuple> protocols) {
		this.protocols = Collections.unmodifiableList(new ArrayList<>(protocols));
		magicNum = this.protocols.hashCode();
		System.out.println("Bytifier.protocols="+protocols);
	}
	
	public Object decode(byte[] bytes) {
		DecodeData data = new DecodeData(bytes);
		if (magicNum != data.getMagicNumber()) {
			System.err.println("magicNum="+magicNum+"; readMagicNumber="+data.getMagicNumber());
		}
		Object result = readChunk(data);
		if (data.hasMoreData()) {
			System.err.println("hasMoreData=true; remainingBytes="+data.getRemainingBytes());
		}
		return result;
	}
	
	protected Object readChunk(DecodeData data) {
		ChunkType chunkType = data.readChunkType();
		switch (chunkType) {
		case NULL:
			return null;
		case NEW_OBJ_REF:
			return readNewObjRef(data);
		case READ_OBJ_REF:
			return data.readObjectReference();
		case VALUE_OBJ_TYPE:
			return readValueType(data);
		case GENERIC_ARRAY:
			return readGenericArray(data);
		case ILLEGAL:
		default:
			throw new IllegalArgumentException("chunkType="+chunkType);
		}
	}
	
	protected Object readNewObjRef(DecodeData data) {
		int objClsIdx = data.readClassIndex();
		
		ClassProtocol protocol = protocols.get(objClsIdx).proto;
		Object obj = protocol.create(this, data);
		data.setObjectReference(obj);
		protocol.read(this, data, obj);
		
		return obj;
	}
	
	protected Object readValueType(DecodeData data) {
		int objClsIdx = data.readClassIndex();
		ClassProtocol protocol = protocols.get(objClsIdx).proto;
		Object obj = protocol.create(this, data);
		protocol.read(this, data, obj);
		return obj;
	}
	
	protected Object readGenericArray(DecodeData data) {
		int elemClsIdx = data.readClassIndex();
		Class<?> elemCls = protocols.get(elemClsIdx).cls;
		
		int len = data.readInt3();
		Object[] arr = (Object[]) Array.newInstance(elemCls, len);
		data.setObjectReference(arr);
		
		for (int i = 0; i < len; i++) {
			arr[i] = readChunk(data);
		}
		return arr;
	}
	
	public byte[] encode(Object objectGraph) {
		EncodeData data = new EncodeData(this);
		writeChunk(data, objectGraph, false);
		return data.getBytes();
	}
	
	protected void writeChunk(EncodeData data, Object object, boolean isValueType) {
		System.out.println("Bytifier.writeChunk="+object);
		if (object == null) {
			System.out.println("writeChunkType(NULL)");
			data.writeChunkType(ChunkType.NULL);
		} else {
			int referenceIdx = data.getReferenceIndexFor(object);
			if (referenceIdx >= 0) {
				System.out.println("writeOldReference("+referenceIdx+")");
				writeOldReference(data, referenceIdx);
			} else {
				int protocolIdx = data.getProtocolIndexFor(object);
				if (protocolIdx < 0) {
					Class<?> arrayType = object.getClass().getComponentType();
					protocolIdx = data.getProtocolIndexFor(arrayType);
					if (arrayType != null && protocolIdx >= 0) {
						System.out.println("writeGenericArray("+arrayType.getSimpleName()+")");
						writeGenericArray(data, protocolIdx, object);
					} else {
						System.out.println("writeChunkType(ILLEGAL)");
						data.writeChunkType(ChunkType.ILLEGAL);
					}
				} else if (isValueType) {
					System.out.println("writeValueType()");
					writeValueType(data, protocolIdx, object);
				} else {
					System.out.println("writeNewReference("+object+")");
					writeNewReference(data, protocolIdx, object);
				}
			}
		}
	}
	
	protected void writeValueType(EncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.VALUE_OBJ_TYPE);
		data.writeClassIndex(protocolIdx);
		protocols.get(protocolIdx).proto.write(this, data, object);
	}
	
	protected void writeGenericArray(EncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.GENERIC_ARRAY);
		data.writeNewReferenceIndex(object);
		data.writeClassIndex(protocolIdx);
		
		Object[] arr = (Object[]) object;
		int len = arr.length;
		data.writeInt3(len);
		
		for (Object elem : arr) {
			writeChunk(data, elem, false);
		}
	}
	
	protected void writeNewReference(EncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.NEW_OBJ_REF);
		data.writeNewReferenceIndex(object);
		data.writeClassIndex(protocolIdx);
		protocols.get(protocolIdx).proto.write(this, data, object);
	}
	
	protected void writeOldReference(EncodeData data, int referenceIdx) {
		data.writeChunkType(ChunkType.READ_OBJ_REF);
		data.writeOldReferenceIndex(referenceIdx);
	}
}