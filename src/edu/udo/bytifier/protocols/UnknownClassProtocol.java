package edu.udo.bytifier.protocols;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;
import edu.udo.bytifier.ValueType;

public class UnknownClassProtocol implements ClassProtocol {
	
	private interface ReadFromField {
		void readFieldData(Bytifier bytifier, DecodeData data, Object obj, Field field) throws IllegalAccessException;
	}
	
	private interface WriteToField {
		void writeFieldData(Bytifier bytifier, EncodeData data, Object obj, Field field) throws IllegalAccessException;
	}
	
	private final Map<Class<?>, WriteToField> writeDataMap = new HashMap<>();
	private final Map<Class<?>, ReadFromField> readDataMap = new HashMap<>();
	private final WriteToField writeRef = (btf, data, obj, field) -> btf.writeChunk(data, field.get(obj), false);
	private final WriteToField writeValType = (btf, data, obj, field) -> btf.writeChunk(data, field.get(obj), true);
	private final ReadFromField defaultRead = (btf, data, obj, field) -> field.set(obj, btf.readChunk(data));
	{
		writeDataMap.put(Byte.TYPE, (btf, data, obj, field) -> data.writeInt1(field.getByte(obj)));
		writeDataMap.put(Short.TYPE, (btf, data, obj, field) -> data.writeInt2(field.getShort(obj)));
		writeDataMap.put(Integer.TYPE, (btf, data, obj, field) -> data.writeInt4(field.getInt(obj)));
		writeDataMap.put(Long.TYPE, (btf, data, obj, field) -> data.writeInt8(field.getLong(obj)));
		writeDataMap.put(Float.TYPE, (btf, data, obj, field) -> data.writeFloat4(field.getFloat(obj)));
		writeDataMap.put(Double.TYPE, (btf, data, obj, field) -> data.writeFloat8(field.getDouble(obj)));
		writeDataMap.put(Boolean.TYPE, (btf, data, obj, field) -> data.writeBoolean(field.getBoolean(obj)));
		writeDataMap.put(Character.TYPE, (btf, data, obj, field) -> data.writeInt2(field.getChar(obj)));
		
		readDataMap.put(Byte.TYPE, (btf, data, obj, field) -> field.setByte(obj, data.readInt1()));
		readDataMap.put(Short.TYPE, (btf, data, obj, field) -> field.setShort(obj, data.readInt2()));
		readDataMap.put(Integer.TYPE, (btf, data, obj, field) -> field.setInt(obj, data.readInt4()));
		readDataMap.put(Long.TYPE, (btf, data, obj, field) -> field.setLong(obj, data.readInt8()));
		readDataMap.put(Float.TYPE, (btf, data, obj, field) -> field.setFloat(obj, data.readFloat4()));
		readDataMap.put(Double.TYPE, (btf, data, obj, field) -> field.setDouble(obj, data.readFloat8()));
		readDataMap.put(Boolean.TYPE, (btf, data, obj, field) -> field.setBoolean(obj, data.readBoolean()));
		readDataMap.put(Character.TYPE, (btf, data, obj, field) -> field.setChar(obj, (char) data.readInt2()));
	}
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object input) {
		try {
			Class<?> cls = input.getClass();
			data.writeJavaIdentifier(cls.getName());
			
			Field[] fieldArr = cls.getDeclaredFields();
			List<Field> fields =
					Arrays.asList(fieldArr)
					.stream()
					.filter(field -> !field.isSynthetic()
							&& !Modifier.isTransient(field.getModifiers())
							&& !Modifier.isStatic(field.getModifiers())
							&& !Modifier.isFinal(field.getModifiers()))
					.collect(Collectors.toList())
			;
			data.writeInt2(fields.size());
			
			AccessibleObject.setAccessible(fieldArr, true);
			for (Field field : fields) {
				data.writeJavaIdentifier(field.getName());
				boolean valueType = field.getAnnotation(ValueType.class) != null
						||field.getType().getAnnotation(ValueType.class) != null;
				writeFieldData(bytifier, data, input, field, valueType);
			}
			AccessibleObject.setAccessible(fieldArr, false);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		try {
			Class<?> cls = object.getClass();
			
			int fieldCount = data.readInt2();
			for (int i = 0; i < fieldCount; i++) {
				String fieldName = data.readJavaIdentifier();
				try {
					Field field = cls.getDeclaredField(fieldName);
					field.setAccessible(true);
					readFieldData(bytifier, data, object, field);
					field.setAccessible(false);
				} catch (NoSuchFieldException e) {
					continue;
				}
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		String clsName = data.readJavaIdentifier();
		try {
			Class<?> objCls = Class.forName(clsName);
			Constructor<?> constr = objCls.getDeclaredConstructor();
			boolean wasAccessible = constr.isAccessible();
			constr.setAccessible(true);
			Object obj = constr.newInstance();
			constr.setAccessible(wasAccessible);
			return obj;
		} catch (ClassNotFoundException
				| InstantiationException
				| IllegalAccessException
				| NoSuchMethodException
				| SecurityException
				| InvocationTargetException e)
		{
			return new IllegalArgumentException(e);
		}
	}
	
	@Override
	public int getIdentificationNumber() {
		throw new UnsupportedOperationException("Unknown Class Protocol must not be part of a Bytifier Protocol");
	}
	
	private void writeFieldData(
			Bytifier bytifier,
			EncodeData data,
			Object obj,
			Field field,
			boolean valueType)
			
			throws IllegalAccessException
	{
		Class<?> fieldType = field.getType();
		WriteToField usedDefaultWrite = valueType ? writeValType : writeRef;
		writeDataMap.getOrDefault(fieldType, usedDefaultWrite).writeFieldData(bytifier, data, obj, field);
	}
	
	private void readFieldData(Bytifier bytifier, DecodeData data, Object obj, Field field) throws IllegalAccessException {
		Class<?> fieldType = field.getType();
		readDataMap.getOrDefault(fieldType, defaultRead).readFieldData(bytifier, data, obj, field);
	}
}