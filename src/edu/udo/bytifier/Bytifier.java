package edu.udo.bytifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.udo.bytifier.protocols.ProtocolUtil;
import edu.udo.bytifier.protocols.UnknownClassProtocol;

public class Bytifier {
	
	protected final List<ProtocolTuple> protocols;
	protected final int protocolID;
	protected UnknownObjectTypeReaction unknownReaction = UnknownObjectTypeReaction.WRITE_AND_WARNING;
	protected UnknownClassProtocol ucp = new UnknownClassProtocol();
	
	public Bytifier() {
		this(Collections.emptyList());
	}
	
	public Bytifier(ProtocolTuple ... protocols) {
		this(Arrays.asList(protocols));
	}
	
	public Bytifier(Collection<ProtocolTuple> protocols) {
		this.protocols = Collections.unmodifiableList(new ArrayList<>(protocols));
		protocolID = calculateProtocolIdentificationNumber();
	}
	
	protected int calculateProtocolIdentificationNumber() {
		int magNum = 0;
		int prime = 37;
		for (ProtocolTuple tuple : protocols) {
			Class<?> cls = tuple.cls;
			magNum += prime * cls.getName().hashCode();
			
			ClassProtocol proto = tuple.proto;
			magNum += prime * proto.getIdentificationNumber();
		}
		return magNum;
	}
	
	public List<ProtocolTuple> getProtocols() {
		return protocols;
	}
	
	public void setReactionToUnknownObjectTypes(UnknownObjectTypeReaction value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be 'null'");
		}
		unknownReaction = value;
		if (unknownReaction == UnknownObjectTypeReaction.WRITE
				|| unknownReaction == UnknownObjectTypeReaction.WRITE_AND_WARNING)
		{
			if (ucp == null) {
				ucp = new UnknownClassProtocol();
			}
		} else {
			ucp = null;
		}
	}
	
	public UnknownObjectTypeReaction getReactionToUnknownObjectTypes() {
		return unknownReaction;
	}
	
	public Class<?> getClassForIndex(int classIndex) {
		if (classIndex < 0 || classIndex >= protocols.size()) {
			return null;
		}
		return protocols.get(classIndex).cls;
	}
	
	public ClassProtocol getProtocolForIndex(int classIndex) {
		if (classIndex < 0 || classIndex >= protocols.size()) {
			return null;
		}
		return protocols.get(classIndex).proto;
	}
	
	public int getProtocolIdentificationNumber() {
		return protocolID;
	}
	
	public Object decode(byte[] bytes) {
		DecodeData data = new DecodeData(bytes);
		if (protocolID != data.getProtocolIdentificationNumber()) {
			System.err.println("local Protocol Identification Number = "+protocolID
					+"; remote Protocol Identification Number = "+data.getProtocolIdentificationNumber());
		}
		Object result = readChunk(data);
		if (data.hasMoreData()) {
			System.err.println("There is unread data at the end of the input; remaining bytes="+data.getRemainingByteCount());
		}
		return result;
	}
	
	public Object readChunk(DecodeData data) {
		ChunkType chunkType = data.readChunkType();
		switch (chunkType) {
		case NULL:
			return null;
		case NEW_OBJ_REF:
			return readNewObjRef(data);
		case READ_OBJ_REF:
			return readOldObjRef(data);
		case VALUE_OBJ_TYPE:
			return readValueType(data);
		case GENERIC_ARRAY:
			return readGenericArray(data);
		case UNKNOWN_OBJ:
			return readUnknownObject(data);
		case UNKNOWN_ENUM:
			return readUnknownEnum(data);
		case ILLEGAL:
		default:
			throw new IllegalArgumentException("chunkType="+chunkType);
		}
	}
	
	protected Object readNewObjRef(DecodeData data) {
		int objClsIdx = data.readClassIndex();
		
		ClassProtocol protocol = getProtocolForIndex(objClsIdx);
		Object obj = protocol.create(this, data);
		data.pushObjectReference(obj);
		protocol.read(this, data, obj);
		
		return obj;
	}
	
	protected Object readOldObjRef(DecodeData data) {
		return data.readObjectReference();
	}
	
	protected Object readValueType(DecodeData data) {
		int objClsIdx = data.readClassIndex();
		ClassProtocol protocol = getProtocolForIndex(objClsIdx);
		Object obj = protocol.create(this, data);
		protocol.read(this, data, obj);
		return obj;
	}
	
	protected Object readGenericArray(DecodeData data) {
		int elemClsIdx = data.readClassIndex();
		Class<?> elemCls = getClassForIndex(elemClsIdx);
		int dim = data.readInt1();
		for (int i = 1; i < dim; i++) {
			elemCls = Array.newInstance(elemCls, 0).getClass();
		}
		
		int len = data.readInt3();
		Object arr = Array.newInstance(elemCls, len);
		data.pushObjectReference(arr);
		
		for (int i = 0; i < len; i++) {
			Object elem = readChunk(data);
			Array.set(arr, i, elem);
		}
		return arr;
	}
	
	protected Object readUnknownObject(DecodeData data) {
		Object obj = ucp.create(this, data);
		data.pushObjectReference(obj);
		ucp.read(this, data, obj);
		return obj;
	}
	
	protected Object readUnknownEnum(DecodeData data) {
		String clsName = data.readJavaIdentifier();
		try {
			Class<?> enumCls = Class.forName(clsName);
			if (!enumCls.isEnum()) {
				throw new IllegalArgumentException(
						"Expected enum type name; found = "+clsName);
			}
			Object[] values = enumCls.getEnumConstants();
			
			int enumIdx = data.readIntForSize(values.length);
			return values[enumIdx];
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Expected enum type; name = "+clsName);
		}
	}
	
	public byte[] encode(Object objectGraph) {
		EncodeData data = new EncodeData(this);
		writeChunk(data, objectGraph, false);
		return data.getBytes();
	}
	
	public void writeChunk(EncodeData data, Object object, boolean isValueType) {
		if (object == null) {
			data.writeChunkType(ChunkType.NULL);
			return;
		}
		if (isValueType) {
			writeValueType(data, data.getProtocolIndexFor(object), object);
			return;
		}
		int referenceIdx = data.getReferenceIndexFor(object);
		if (referenceIdx >= 0) {
			writeOldReference(data, referenceIdx);
			return;
		}
		int protocolIdx = data.getProtocolIndexFor(object);
		if (protocolIdx < 0) {
			Class<?> arrayType = ProtocolUtil.getArrayElementType(object);
			protocolIdx = data.getProtocolIndexFor(arrayType);
			if (arrayType != null && protocolIdx >= 0) {
				writeGenericArray(data, protocolIdx, object);
			} else {
				beforeUnknownObjectWrite(data, object);
			}
			return;
		}
		writeNewReference(data, protocolIdx, object);
	}
	
	protected void writeValueType(EncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.VALUE_OBJ_TYPE);
		data.writeClassIndex(protocolIdx);
		getProtocolForIndex(protocolIdx).write(this, data, object);
	}
	
	protected void writeGenericArray(EncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.GENERIC_ARRAY);
		data.writeNewReferenceIndex(object);
		data.writeClassIndex(protocolIdx);
		// write the dimension of the generic array as 1 byte (max = 256)
		int dim = ProtocolUtil.getArrayDimension(object);
		data.writeInt1(dim);
		// write the length of the generic array with 3 bytes (max = 2^48)
		int len = Array.getLength(object);
		data.writeInt3(len);
		
		for (int i = 0; i < len; i++) {
			Object elem = Array.get(object, i);
			writeChunk(data, elem, false);
		}
	}
	
	protected void writeNewReference(EncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.NEW_OBJ_REF);
		data.writeNewReferenceIndex(object);
		data.writeClassIndex(protocolIdx);
		getProtocolForIndex(protocolIdx).write(this, data, object);
	}
	
	protected void writeOldReference(EncodeData data, int referenceIdx) {
		data.writeChunkType(ChunkType.READ_OBJ_REF);
		data.writeOldReferenceIndex(referenceIdx);
	}
	
	protected void writeUnknownObject(EncodeData data, Object object) {
		data.writeChunkType(ChunkType.UNKNOWN_OBJ);
		data.writeNewReferenceIndex(object);
		
		ucp.write(this, data, object);
	}
	
	protected void writeUnknownEnum(EncodeData data, Object object) {
		data.writeChunkType(ChunkType.UNKNOWN_ENUM);
		
		Class<?> cls = object.getClass();
		Object[] values = cls.getEnumConstants();
		
		Enum<?> literal = (Enum<?>) object;
		int ordinal = literal.ordinal();
		
		data.writeJavaIdentifier(cls.getName());
		data.writeIntForSize(values.length, ordinal);
	}
	
	protected void beforeUnknownObjectWrite(EncodeData data, Object object) {
		switch (unknownReaction) {
		case EXCEPTION:
			throw new IllegalArgumentException(
					"The type of the following object is not part of the protocol: "
						+object.getClass().getName()+": "+object);
		case WRITE_AND_WARNING:
			System.err.println(
					"The type of the following object is not part of the protocol: "
						+object.getClass().getName()+": "+object);
			//$FALL-THROUGH$
		case WRITE:
			if (object.getClass().isEnum()) {
				writeUnknownEnum(data, object);
			} else {
				writeUnknownObject(data, object);
			}
			break;
		case WRITE_NULL:
			data.writeChunkType(ChunkType.NULL);
			break;
		default:
			throw new IllegalStateException(
					"The selected "
					+UnknownObjectTypeReaction.class.getSimpleName()
					+" is not supported: "
					+unknownReaction);
		}
	}
}