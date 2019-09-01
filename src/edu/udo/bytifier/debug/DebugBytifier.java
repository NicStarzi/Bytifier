package edu.udo.bytifier.debug;

import java.lang.reflect.Array;
import java.util.Collection;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ChunkType;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;
import edu.udo.bytifier.ProtocolTuple;
import edu.udo.bytifier.UnknownObjectTypeReaction;
import edu.udo.bytifier.protocols.ProtocolUtil;

public class DebugBytifier extends Bytifier {
	
	protected String mostRecentReport;
	
	public DebugBytifier() {
		super();
	}
	
	public DebugBytifier(ProtocolTuple ... protocols) {
		super(protocols);
	}
	
	public DebugBytifier(Collection<ProtocolTuple> protocols) {
		super(protocols);
	}
	
	public DebugBytifier(Bytifier bytifier) {
		super(bytifier.getProtocols());
	}
	
	public String getMostRecentReport() {
		return mostRecentReport;
	}
	
	@Override
	public Object decode(byte[] data) {
		DebugDecodeData decoder = new DebugDecodeData(data);
		Object decodedObj = readChunkAndReport(decoder);
		mostRecentReport = decoder.getReportString();
		System.out.println(mostRecentReport);
		return decodedObj;
	}
	
	@Override
	public Object readChunk(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readChunkAndReport((DebugDecodeData) data);
		} else {
			return super.readChunk(data);
		}
	}
	
	@Override
	public Object readGenericArray(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readGenericArrayAndReport((DebugDecodeData) data);
		} else {
			return super.readGenericArray(data);
		}
	}
	
	@Override
	public Object readUnknownObject(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readUnknownObjectAndReport((DebugDecodeData) data);
		} else {
			return super.readUnknownObject(data);
		}
	}
	
	@Override
	protected Object readUnknownEnum(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readUnknownEnumAndReport((DebugDecodeData) data);
		} else {
			return super.readUnknownEnum(data);
		}
	}
	
	@Override
	public Object readNewObjRef(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readNewObjRefAndReport((DebugDecodeData) data);
		} else {
			return super.readNewObjRef(data);
		}
	}
	
	@Override
	public Object readOldObjRef(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readOldObjRefAndReport((DebugDecodeData) data);
		} else {
			return super.readOldObjRef(data);
		}
	}
	
	@Override
	public Object readValueType(DecodeData data) {
		if (data instanceof DebugDecodeData) {
			return readValueTypeAndReport((DebugDecodeData) data);
		} else {
			return super.readValueType(data);
		}
	}
	
	public Object readChunkAndReport(DebugDecodeData data) {
		data.incrementDepth();
		
		ChunkType chunkType = data.readChunkType();
		data.writeLine("Chunk Type: ", chunkType.getProperName());
		
		Object result;
		switch (chunkType) {
		case NULL:
			data.decrementDepth();
			return null;// nothing needs to be done
		case NEW_OBJ_REF:
			result = readNewObjRef(data);
			break;
		case READ_OBJ_REF:
			result = readOldObjRef(data);
			break;
		case VALUE_OBJ_TYPE:
			result = readValueType(data);
			break;
		case GENERIC_ARRAY:
			result = readGenericArray(data);
			break;
		case UNKNOWN_OBJ:
			return readUnknownObject(data);
		case UNKNOWN_ENUM:
			return readUnknownEnum(data);
		case ILLEGAL:
		default:
			throw new IllegalStateException("chunk type '"+chunkType+"' is unknown.");
		}
		data.decrementDepth();
		return result;
	}
	
	public Object readGenericArrayAndReport(DebugDecodeData data) {
		int elemClsIdx = data.readClassIndex();
		Class<?> elemCls = getClassForIndex(elemClsIdx);
		data.writeLine("Array element class index: ", Integer.toString(elemClsIdx), "; Array element class: ", elemCls.getName());
		
		int dim = data.readInt1();
		data.writeLine("Array dimension: ", Integer.toString(dim));
		for (int i = 1; i < dim; i++) {
			elemCls = Array.newInstance(elemCls, 0).getClass();
		}

		int len = data.readInt3();
		data.writeLine("Array length: ", Integer.toString(len));
		Object arr = Array.newInstance(elemCls, len);
		data.pushObjectReference(arr);

		for (int i = 0; i < len; i++) {
			Object elem = readChunk(data);
			Array.set(arr, i, elem);
		}
		return arr;
	}
	
	public Object readUnknownObjectAndReport(DebugDecodeData data) {
		try {
			int mark = data.getCurrentReadPosition();
			
			Object obj = ucp.create(this, data);
			data.writeLine("Object of non protocolled type: ", obj);
			data.pushObjectReference(obj);
			ucp.read(this, data, obj);
			
			int bytes = data.getBytesReadSince(mark);
			data.writeLine("Object byte size: ", Integer.toString(bytes));
			return obj;
		} catch (Exception e) {
			data.writeLine("Error while deserialising unknown object. ", e.getClass().getName(), ": ", e.getMessage());
			return null;
		}
	}
	
	public Object readUnknownEnumAndReport(DebugDecodeData data) {
		String clsName = data.readJavaIdentifier();
		data.writeLine("Enum class name: ", clsName);
		try {
			Class<?> enumCls = Class.forName(clsName);
			data.writeLine("Enum class object: ", enumCls, "; is enum? ", enumCls.isEnum());
			if (!enumCls.isEnum()) {
				throw new IllegalArgumentException(
						"Expected enum type name; found = "+clsName);
			}
			Object[] values = enumCls.getEnumConstants();
			
			int enumIdx = data.readIntForSize(values.length);
			data.writeLine("Enum literal index: ", enumIdx);
			Object enumLit = values[enumIdx];
			data.writeLine("Enum literal: ", enumLit);
			return enumLit;
		} catch (ClassNotFoundException e) {
			data.writeLine("Expected enum type; name = ", clsName);
			return null;
		}
	}
	
	public Object readNewObjRefAndReport(DebugDecodeData data) {
		int objClsIdx = data.readClassIndex();
		Class<?> objCls = getClassForIndex(objClsIdx);
		data.writeLine("Object class index: ", Integer.toString(objClsIdx), "; object class: ", objCls.getName());
		
		int mark = data.getCurrentReadPosition();
		
		ClassProtocol protocol = getProtocolForIndex(objClsIdx);
		Object obj = protocol.create(this, data);
		data.pushObjectReference(obj);
		protocol.read(this, data, obj);
		
		int bytes = data.getBytesReadSince(mark);
		data.writeLine("Object byte size: ", Integer.toString(bytes));
		return obj;
	}
	
	public Object readOldObjRefAndReport(DebugDecodeData data) {
		int refSize = data.getNextObjectReferenceSizeInBytes();
		int refIdx = data.getNextObjectReference();
		data.writeLine("Referenced object index: ", Integer.toString(refIdx));
		data.writeLine("Size of reference in bytes: ", Integer.toString(refSize));
		Object refObj = data.readObjectReference();
		data.writeLine("Referenced object: ", refObj);
		return refObj;
	}
	
	public Object readValueTypeAndReport(DebugDecodeData data) {
		int objClsIdx = data.readClassIndex();
		Class<?> objCls = getClassForIndex(objClsIdx);
		data.writeLine("Value class index: ", Integer.toString(objClsIdx), "; Value class: ", objCls.getName());
		
		int mark = data.getCurrentReadPosition();
		
		ClassProtocol protocol = getProtocolForIndex(objClsIdx);
		Object obj = protocol.create(this, data);
		protocol.read(this, data, obj);
		
		data.writeLine("Value byte size: ", data.getBytesReadSince(mark));
		return obj;
	}
	
	@Override
	public byte[] encode(Object objectGraph) {
		DebugEncodeData data = new DebugEncodeData(this);
		writeChunkAndReport(data, objectGraph, false);
		return data.getBytes();
	}
	
	@Override
	public void writeChunk(EncodeData data, Object object, boolean isValueType) {
		if (data instanceof DebugEncodeData) {
			writeChunkAndReport((DebugEncodeData) data, object, isValueType);
		} else {
			super.writeChunk(data, object, isValueType);
		}
	}
	
	@Override
	public void writeValueType(EncodeData data, int protocolIdx, Object object) {
		if (data instanceof DebugEncodeData) {
			writeValueTypeAndReport((DebugEncodeData) data, protocolIdx, object);
		} else {
			super.writeValueType(data, protocolIdx, object);
		}
	}
	
	@Override
	public void writeGenericArray(EncodeData data, int protocolIdx, Object object) {
		if (data instanceof DebugEncodeData) {
			writeGenericArrayAndReport((DebugEncodeData) data, protocolIdx, object);
		} else {
			super.writeGenericArray(data, protocolIdx, object);
		}
	}
	
	@Override
	public void writeNewReference(EncodeData data, int protocolIdx, Object object) {
		if (data instanceof DebugEncodeData) {
			writeNewReferenceAndReport((DebugEncodeData) data, protocolIdx, object);
		} else {
			super.writeNewReference(data, protocolIdx, object);
		}
	}
	
	@Override
	public void writeOldReference(EncodeData data, int referenceIdx) {
		if (data instanceof DebugEncodeData) {
			writeOldReferenceAndReport((DebugEncodeData) data, referenceIdx);
		} else {
			super.writeOldReference(data, referenceIdx);
		}
	}
	
	@Override
	protected void beforeUnknownObjectWrite(EncodeData data, Object object) {
		if (data instanceof DebugEncodeData) {
			beforeUnknownObjectWriteAndReport((DebugEncodeData) data, object);
		} else {
			super.beforeUnknownObjectWrite(data, object);
		}
	}
	
	@Override
	protected void writeUnknownObject(EncodeData data, Object object) {
		if (data instanceof DebugEncodeData) {
			writeUnknownObjectAndReport((DebugEncodeData) data, object);
		} else {
			super.writeUnknownObject(data, object);
		}
	}
	
	@Override
	protected void writeUnknownEnum(EncodeData data, Object object) {
		if (data instanceof DebugEncodeData) {
			writeUnknownEnumAndReport((DebugEncodeData) data, object);
		} else {
			super.writeUnknownEnum(data, object);
		}
	}
	
	public void writeChunkAndReport(DebugEncodeData data, Object object, boolean isValueType) {
		if (object == null) {
			data.writeLine("Chunk Type: ", ChunkType.NULL.getProperName());
			data.writeChunkType(ChunkType.NULL);
		} else {
			int referenceIdx = data.getReferenceIndexFor(object);
			if (referenceIdx >= 0) {
				data.writeLine("Chunk Type: ", ChunkType.READ_OBJ_REF.getProperName());
				writeOldReference(data, referenceIdx);
			} else {
				int protocolIdx = data.getProtocolIndexFor(object);
				if (protocolIdx < 0) {
					Class<?> arrayType = ProtocolUtil.getArrayElementType(object);
					protocolIdx = data.getProtocolIndexFor(arrayType);
					if (arrayType != null && protocolIdx >= 0) {
						data.writeLine("Chunk Type: ", ChunkType.GENERIC_ARRAY.getProperName());
						writeGenericArray(data, protocolIdx, object);
					} else {
						if (object.getClass().isEnum()) {
							data.writeLine("Chunk Type: ", ChunkType.UNKNOWN_ENUM.getProperName());
						} else {
							data.writeLine("Chunk Type: ", ChunkType.UNKNOWN_OBJ.getProperName());
						}
						beforeUnknownObjectWrite(data, object);
					}
				} else if (isValueType) {
					data.writeLine("Chunk Type: ", ChunkType.VALUE_OBJ_TYPE.getProperName());
					writeValueType(data, protocolIdx, object);
				} else {
					data.writeLine("Chunk Type: ", ChunkType.NEW_OBJ_REF.getProperName());
					writeNewReference(data, protocolIdx, object);
				}
			}
		}
	}
	
	protected void beforeUnknownObjectWriteAndReport(DebugEncodeData data, Object object) {
		data.writeLine("Unknown object class: ", object.getClass().getName(), "; unknown object toString: ", object);
		switch (unknownReaction) {
		case THROW_EXCEPTION:
			data.writeLine("Reaction: Exception");
			throw new IllegalArgumentException(
					"The type of the following object is not part of the protocol: "
						+object.getClass().getName()+": "+object);
		case WRITE_AND_WARNING:
			data.writeLine("Reaction: Warning");
			System.err.println(
					"The type of the following object is not part of the protocol: "
						+object.getClass().getName()+": "+object);
			//$FALL-THROUGH$ - we want to also write when we emit a warning
		case WRITE:
			if (object.getClass().isEnum()) {
				data.writeLine("Reaction: Write Enum");
				writeUnknownEnum(data, object);
			} else {
				data.writeLine("Reaction: Write Unknown Object");
				writeUnknownObject(data, object);
			}
			break;
		case WRITE_NULL:
			data.writeLine("Reaction: Write Null");
			data.writeChunkType(ChunkType.NULL);
			break;
		default:
			data.writeLine("Reaction: Unknown Reaction");
			throw new IllegalStateException(
					"The selected "
					+UnknownObjectTypeReaction.class.getSimpleName()
					+" is not supported: "
					+unknownReaction);
		}
	}
	
	public void writeUnknownObjectAndReport(DebugEncodeData data, Object object) {
//		data.writeChunkType(ChunkType.UNKNOWN_OBJ);
//		data.writeNewReferenceIndex(object);
//
//		ucp.write(this, data, object);
	}
	
	public void writeUnknownEnumAndReport(DebugEncodeData data, Object object) {
//		data.writeChunkType(ChunkType.UNKNOWN_ENUM);
//
//		Class<?> cls = object.getClass();
//		Object[] values = cls.getEnumConstants();
//
//		Enum<?> literal = (Enum<?>) object;
//		int ordinal = literal.ordinal();
//
//		data.writeJavaIdentifier(cls.getName());
//		data.writeIntForSize(values.length, ordinal);
	}
	
	public void writeValueTypeAndReport(DebugEncodeData data, int protocolIdx, Object object) {
		data.writeChunkType(ChunkType.VALUE_OBJ_TYPE);
		
		data.writeLine("Value class index: ", Integer.toString(protocolIdx), "; Value class: ", object.getClass().getName());
		data.writeClassIndex(protocolIdx);
		
		int mark = data.getCurrentWritePosition();
		getProtocolForIndex(protocolIdx).write(this, data, object);
		
		data.writeLine("Value byte size: ", data.getBytesWrittenSince(mark));
	}
	
	public void writeGenericArrayAndReport(DebugEncodeData data, int protocolIdx, Object object) {
//		data.writeChunkType(ChunkType.GENERIC_ARRAY);
//		data.writeNewReferenceIndex(object);
//		data.writeClassIndex(protocolIdx);
//		// write the dimension of the generic array as 1 byte (max = 256)
//		int dim = ProtocolUtil.getArrayDimension(object);
//		data.writeInt1(dim);
//		// write the length of the generic array with 3 bytes (max = 2^48)
//		int len = Array.getLength(object);
//		data.writeInt3(len);
//
//		for (int i = 0; i < len; i++) {
//			Object elem = Array.get(object, i);
//			writeChunk(data, elem, false);
//		}
	}
	
	public void writeNewReferenceAndReport(DebugEncodeData data, int protocolIdx, Object object) {
//		data.writeChunkType(ChunkType.NEW_OBJ_REF);
//		data.writeNewReferenceIndex(object);
//		data.writeClassIndex(protocolIdx);
//		getProtocolForIndex(protocolIdx).write(this, data, object);
	}
	
	public void writeOldReferenceAndReport(DebugEncodeData data, int referenceIdx) {
		
//		data.writeChunkType(ChunkType.READ_OBJ_REF);
//		data.writeOldReferenceIndex(referenceIdx);
	}
	
}