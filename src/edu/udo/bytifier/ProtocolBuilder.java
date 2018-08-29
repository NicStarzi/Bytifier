package edu.udo.bytifier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import edu.udo.bytifier.Bytifier.ProtocolTuple;
import edu.udo.bytifier.protocols.EnumProtocol;
import edu.udo.bytifier.protocols.ObjectProtocol;
import edu.udo.bytifier.protocols.PrimitiveArrays;
import edu.udo.bytifier.protocols.ProtocolUtil;
import edu.udo.bytifier.protocols.ReflectionClassProtocol;
import edu.udo.bytifier.protocols.StringProtocol;
import edu.udo.bytifier.protocols.ValueTypeArrayProtocol;

public class ProtocolBuilder {
	
//	public static final List<ProtocolTuple> PRIMITIVE_TYPE_ARRAY_PROTOCOLS =
//		Collections.unmodifiableList(Arrays.asList(new ProtocolTuple[]
//		{
//			new ProtocolTuple(byte[].class, PrimitiveArrays.BYTE_ARRAY_PROTOCOL),
//			new ProtocolTuple(short[].class, PrimitiveArrays.SHORT_ARRAY_PROTOCOL),
//			new ProtocolTuple(int[].class, PrimitiveArrays.INT_ARRAY_PROTOCOL),
//			new ProtocolTuple(long[].class, PrimitiveArrays.LONG_ARRAY_PROTOCOL),
//			new ProtocolTuple(float[].class, PrimitiveArrays.FLOAT_ARRAY_PROTOCOL),
//			new ProtocolTuple(double[].class, PrimitiveArrays.DOUBLE_ARRAY_PROTOCOL),
//			new ProtocolTuple(boolean[].class, PrimitiveArrays.BOOL_ARRAY_PROTOCOL),
//			new ProtocolTuple(char[].class, PrimitiveArrays.CHAR_ARRAY_PROTOCOL),
//		}));
	public static final Map<Class<?>, ClassProtocol> PRIMITIVE_ARRAY_PROTOCOL_MAP = new HashMap<>();
	static {
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(byte[].class,		PrimitiveArrays.BYTE_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(short[].class,		PrimitiveArrays.SHORT_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(int[].class,		PrimitiveArrays.INT_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(long[].class,		PrimitiveArrays.LONG_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(float[].class,		PrimitiveArrays.FLOAT_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(double[].class,	PrimitiveArrays.DOUBLE_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(boolean[].class,	PrimitiveArrays.BOOL_ARRAY_PROTOCOL);
		PRIMITIVE_ARRAY_PROTOCOL_MAP.put(char[].class,		PrimitiveArrays.CHAR_ARRAY_PROTOCOL);
	}
	
	protected final Map<Class<?>, Supplier<ClassProtocol>> clsMap = new HashMap<>();
	
	{// initialize protocol with defaults
		isObjectClassIncluded(true);
		setStringEncodingCharset(StandardCharsets.UTF_8);
		setSupportedPrimitiveTypeArrays(PRIMITIVE_ARRAY_PROTOCOL_MAP.keySet());
	}
	
	public boolean hasClassDefinition(Class<?> cls) {
		return clsMap.containsKey(cls);
	}
	
	public ProtocolBuilder isObjectClassIncluded(boolean value) {
		if (value) {
			clsMap.put(Object.class, () -> ObjectProtocol.INSTANCE);
		} else {
			clsMap.remove(Object.class);
		}
		return this;
	}
	
	public ProtocolBuilder setStringEncodingCharset(Charset charset) {
		clsMap.put(String.class, () -> new StringProtocol(charset));
		return this;
	}
	
	public ProtocolBuilder setSupportedPrimitiveTypeArrays(Class<?> ... primitiveTypeArrayClasses) {
		return setSupportedPrimitiveTypeArrays(Arrays.asList(primitiveTypeArrayClasses));
	}
	
	public ProtocolBuilder setSupportedPrimitiveTypeArrays(Collection<Class<?>> primitiveTypeArrayClasses) {
		for (Class<?> arrayType : primitiveTypeArrayClasses) {
			if (!arrayType.getComponentType().isPrimitive()) {
				throw new IllegalArgumentException(arrayType.getName()+" is not a primitive array type.");
			}
			ClassProtocol protocol = PRIMITIVE_ARRAY_PROTOCOL_MAP.get(arrayType);
			if (protocol != null) {
				defineForClass(arrayType, protocol);
			} else {
				throw new IllegalArgumentException(arrayType.getName()+" is not supported.");
			}
		}
		return this;
	}
	
	public ProtocolBuilder addPrimitiveTypeArrays(Class<?> ... primitiveTypeArrayClasses) {
		return addPrimitiveTypeArrays(Arrays.asList(primitiveTypeArrayClasses));
	}
	
	public ProtocolBuilder addPrimitiveTypeArrays(Collection<Class<?>> primitiveTypeArrayClasses) {
		for (Class<?> arrayType : primitiveTypeArrayClasses) {
			if (!arrayType.getComponentType().isPrimitive()) {
				throw new IllegalArgumentException(arrayType.getName()+" is not a primitive array type.");
			}
			ClassProtocol protocol = PRIMITIVE_ARRAY_PROTOCOL_MAP.get(arrayType);
		 	if (protocol != null) {
				defineForClass(arrayType, protocol);
			} else {
				throw new IllegalArgumentException(arrayType.getName()+" is not supported.");
			}
		}
		return this;
	}
	
	public ProtocolBuilder removePrimitiveTypeArrays(Class<?> ... primitiveTypeArrayClasses) {
		return removePrimitiveTypeArrays(Arrays.asList(primitiveTypeArrayClasses));
	}
	
	public ProtocolBuilder removePrimitiveTypeArrays(Collection<Class<?>> primitiveTypeArrayClasses) {
		for (Class<?> arrayType : primitiveTypeArrayClasses) {
			if (!arrayType.getComponentType().isPrimitive()) {
				throw new IllegalArgumentException(arrayType.getName()+" is not a primitive array type.");
			}
			clsMap.remove(arrayType);
		}
		return this;
	}
	
	public ProtocolBuilder defineValueTypeArray(Class<?> arrayType, boolean isValueType) {
		Class<?> elementType = ProtocolUtil.getArrayElementType(arrayType);
		clsMap.put(arrayType, () -> new ValueTypeArrayProtocol<>(elementType, isValueType));
		return this;
	}
	
	public <T extends Enum<T>> ProtocolBuilder defineEnum(Class<T> enumClass) {
		clsMap.put(enumClass, () -> new EnumProtocol<>(enumClass));
		return this;
	}
	
	public ProtocolBuilder defineViaReflection(Class<?> cls) {
		clsMap.put(cls, () -> ReflectionClassProtocol.allFieldsOf(cls));
		return this;
	}
	
	public ProtocolBuilder defineViaReflectionOnlySelectedFields(Class<?> cls, String ... fieldNames) {
		clsMap.put(cls, () -> ReflectionClassProtocol.withFields(cls, fieldNames));
		return this;
	}
	
	public ProtocolBuilder defineViaReflectionWithoutSelectedFields(Class<?> cls, String ... fieldNames) {
		clsMap.put(cls, () -> ReflectionClassProtocol.withoutFields(cls, fieldNames));
		return this;
	}
	
	public ProtocolBuilder defineForClass(Class<?> cls, ClassProtocol protocol) {
		if (hasClassDefinition(cls)) {
			throw new IllegalStateException("hasClassDefinition("+cls.getName()+") == true");
		}
		clsMap.put(cls, () -> protocol);
		return this;
	}
	
	public <T> PerClassBuilder<T> defineForClass(Class<T> cls) {
		if (hasClassDefinition(cls)) {
			throw new IllegalStateException("hasClassDefinition("+cls.getName()+") == true");
		}
		PerClassBuilder<T> builder = new PerClassBuilder<>(this, cls);
		clsMap.put(cls, builder::build);
		return builder;
	}
	
	public Bytifier build() {
		List<ProtocolTuple> protocols = new ArrayList<>(clsMap.size());
		for (Entry<Class<?>, Supplier<ClassProtocol>> entry : clsMap.entrySet()) {
			Class<?> cls = entry.getKey();
			ClassProtocol prot = entry.getValue().get();
			protocols.add(new ProtocolTuple(cls, prot));
		}
		return new Bytifier(protocols);
	}
	
}