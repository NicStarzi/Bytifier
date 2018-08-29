package edu.udo.bytifier.protocols;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class ReflectionClassProtocol implements ClassProtocol {
	
	protected static final FieldWriter DEFAULT_FIELD_WRITER =
			(bytifier, data, input, field) -> bytifier.writeChunk(data, field.get(input), false);
	protected static final Map<Class<?>, FieldWriter> FIELD_TYPE_WRITE_ACTIONS = new HashMap<>();
	static {
		FIELD_TYPE_WRITE_ACTIONS.put(Character.TYPE,
				(bytifier, data, input, field) -> data.writeInt2(field.getChar(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Byte.TYPE,
				(bytifier, data, input, field) -> data.writeInt1(field.getByte(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Short.TYPE,
				(bytifier, data, input, field) -> data.writeInt2(field.getShort(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Integer.TYPE,
				(bytifier, data, input, field) -> data.writeInt4(field.getInt(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Long.TYPE,
				(bytifier, data, input, field) -> data.writeInt8(field.getLong(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Float.TYPE,
				(bytifier, data, input, field) -> data.writeFloat4(field.getFloat(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Double.TYPE,
				(bytifier, data, input, field) -> data.writeFloat8(field.getDouble(input)));
		FIELD_TYPE_WRITE_ACTIONS.put(Boolean.TYPE,
				(bytifier, data, input, field) -> data.writeBoolean(field.getBoolean(input)));
	}
	protected static final FieldReader DEFAULT_FIELD_READER =
			(bytifier, data, object, field) -> field.set(object, bytifier.readChunk(data));
	protected static final Map<Class<?>, FieldReader> FIELD_TYPE_READ_ACTIONS = new HashMap<>();
	static {
		FIELD_TYPE_READ_ACTIONS.put(Character.TYPE,
				(bytifier, data, object, field) -> field.setChar(object, (char) data.readInt2()));
		FIELD_TYPE_READ_ACTIONS.put(Byte.TYPE,
				(bytifier, data, object, field) -> field.setByte(object, data.readInt1()));
		FIELD_TYPE_READ_ACTIONS.put(Short.TYPE,
				(bytifier, data, object, field) -> field.setShort(object, data.readInt2()));
		FIELD_TYPE_READ_ACTIONS.put(Integer.TYPE,
				(bytifier, data, object, field) -> field.setInt(object, data.readInt4()));
		FIELD_TYPE_READ_ACTIONS.put(Long.TYPE,
				(bytifier, data, object, field) -> field.setLong(object, data.readInt8()));
		FIELD_TYPE_READ_ACTIONS.put(Float.TYPE,
				(bytifier, data, object, field) -> field.setFloat(object, data.readFloat4()));
		FIELD_TYPE_READ_ACTIONS.put(Double.TYPE,
				(bytifier, data, object, field) -> field.setDouble(object, data.readFloat8()));
		FIELD_TYPE_READ_ACTIONS.put(Boolean.TYPE,
				(bytifier, data, object, field) -> field.setBoolean(object, data.readBoolean()));
	}
	
	public static ReflectionClassProtocol allFieldsOf(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		ReflectionClassProtocol.forEachField(clazz, field -> {
			if (!field.isSynthetic()) {
				fields.add(field);
			}
		});
		return new ReflectionClassProtocol(clazz, fields);
	}
	
	public static ReflectionClassProtocol withoutFields(Class<?> clazz, String ... fieldNames) {
		List<Field> fields = new ArrayList<>();
		ReflectionClassProtocol.forEachField(clazz, field -> {
			if (field.isSynthetic()) {
				return;
			}
			boolean valid = true;
			for (String name : fieldNames) {
				if (field.getName().equals(name)) {
					valid = false;
					break;
				}
			}
			if (valid) {
				fields.add(field);
			}
		});
		return new ReflectionClassProtocol(clazz, fields);
	}
	
	public static ReflectionClassProtocol withFields(Class<?> clazz, String ... fieldNames) {
		List<Field> fields = new ArrayList<>();
		ReflectionClassProtocol.forEachField(clazz, field -> {
			if (field.isSynthetic()) {
				return;
			}
			for (String name : fieldNames) {
				if (field.getName().equals(name)) {
					fields.add(field);
				}
			}
		});
		if (fields.size() < fieldNames.length) {
			List<String> unknownNames = new ArrayList<>(fieldNames.length - fields.size());
			for (String name : fieldNames) {
				boolean nameIsKnown = false;
				for (Field field : fields) {
					if (field.getName().equals(name)) {
						nameIsKnown = true;
						break;
					}
				}
				if (!nameIsKnown) {
					unknownNames.add(name);
				}
			}
			throw new IllegalArgumentException("Unknown field names := "+unknownNames);
		}
		return new ReflectionClassProtocol(clazz, fields);
	}
	
	protected final List<Field> fields;
	protected final Class<?> cls;
	
	protected ReflectionClassProtocol(Class<?> clazz, Collection<Field> selectedFields) {
		cls = clazz;
		fields = new ArrayList<>(selectedFields);
		fields.forEach(field -> field.setAccessible(true));
	}
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object input) {
		fields.forEach(field -> {
			try {
				Class<?> type = field.getType();
				FieldWriter writer = FIELD_TYPE_WRITE_ACTIONS.getOrDefault(type, DEFAULT_FIELD_WRITER);
				writer.write(bytifier, data, input, field);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		try {
			return cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {
		fields.forEach(field -> {
			try {
				Class<?> type = field.getType();
				FieldReader reader = FIELD_TYPE_READ_ACTIONS.getOrDefault(type, DEFAULT_FIELD_READER);
				reader.read(bytifier, data, object, field);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@Override
	public int getIdentificationNumber() {
		final int prime = 31;
		final int[] result = {cls.getName().hashCode()};
		fields.forEach(field -> {
			int hashCls = field.getDeclaringClass().getName().hashCode();
			int hashName = field.getName().hashCode();
			int hashType = field.getType().hashCode();
			result[0] += prime * hashCls + prime * hashName + prime * hashType;
		});
		return result[0];
	}
	
	public static interface FieldWriter {
		public void write(Bytifier bytifier, EncodeData data, Object input, Field field) throws Exception;
	}
	
	public static interface FieldReader {
		public void read(Bytifier bytifier, DecodeData data, Object object, Field field) throws Exception;
	}
	
	public static void forEachField(Class<?> clazz, Consumer<Field> action) {
		Class<?> curCls = clazz;
		while (curCls != Object.class) {
			for (Field field : curCls.getDeclaredFields()) {
				action.accept(field);
			}
			curCls = curCls.getSuperclass();
		}
	}
	
}