package edu.udo.bytifier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import edu.udo.bytifier.Bytifier.ProtocolTuple;

public class ProtocolBuilder {
	
	protected final Map<Class<?>, Supplier<ClassProtocol>> clsMap = new HashMap<>();
	
	{
		setStringCharset(StandardCharsets.UTF_8);
		clsMap.put(Object.class, () -> StandardClassProtocols.OBJECT_PROTOCOL);
	}
	
	public boolean hasClassDefinition(Class<?> cls) {
		return clsMap.containsKey(cls);
	}
	
	public void setStringCharset(Charset charset) {
		clsMap.put(String.class, () -> new StandardClassProtocols.StringProtocol(charset));
	}
	
	public <T extends Enum<T>> void defineEnum(Class<T> enumClass) {
		clsMap.put(enumClass, () -> new StandardClassProtocols.EnumProtocol<>(enumClass));
	}
	
	public void defineViaReflection(Class<?> cls) {
		clsMap.put(cls, () -> ReflectionClassProtocol.allFieldsOf(cls));
	}
	
	public void defineViaReflectionWithToFields(Class<?> cls, String ... fieldNames) {
		clsMap.put(cls, () -> ReflectionClassProtocol.withFields(cls, fieldNames));
	}
	
	public void defineViaReflectionWithoutFields(Class<?> cls, String ... fieldNames) {
		clsMap.put(cls, () -> ReflectionClassProtocol.withoutFields(cls, fieldNames));
	}
	
	public <T> PerClassBuilder<T> defineForClass(Class<T> cls) {
		if (hasClassDefinition(cls)) {
			throw new IllegalStateException("hasClassDefinition("+cls.getName()+") == true");
		}
		PerClassBuilder<T> builder = new PerClassBuilder<>(cls);
		clsMap.put(cls, builder::build);
		return builder;
	}
	
	public static final List<ProtocolTuple> PRIMITIVE_TYPE_ARRAY_PROTOCOLS =
			Collections.unmodifiableList(Arrays.asList(new ProtocolTuple[]
			{
				new ProtocolTuple(byte[].class, StandardClassProtocols.BYTE_ARRAY_PROTOCOL),
				new ProtocolTuple(short[].class, StandardClassProtocols.SHORT_ARRAY_PROTOCOL),
				new ProtocolTuple(int[].class, StandardClassProtocols.INT_ARRAY_PROTOCOL),
				new ProtocolTuple(long[].class, StandardClassProtocols.LONG_ARRAY_PROTOCOL),
				new ProtocolTuple(float[].class, StandardClassProtocols.FLOAT_ARRAY_PROTOCOL),
				new ProtocolTuple(double[].class, StandardClassProtocols.DOUBLE_ARRAY_PROTOCOL),
				new ProtocolTuple(boolean[].class, StandardClassProtocols.BOOL_ARRAY_PROTOCOL),
				new ProtocolTuple(char[].class, StandardClassProtocols.CHAR_ARRAY_PROTOCOL),
			}));
	
	public Bytifier build() {
		int protocolCount = clsMap.size() + PRIMITIVE_TYPE_ARRAY_PROTOCOLS.size();
		List<ProtocolTuple> protocols = new ArrayList<>(protocolCount);
		// add protocols for primitive arrays
		for (ProtocolTuple tup : PRIMITIVE_TYPE_ARRAY_PROTOCOLS) {
			protocols.add(tup);
		}
		// add user defined protocols
		for (Entry<Class<?>, Supplier<ClassProtocol>> entry : clsMap.entrySet()) {
			Class<?> cls = entry.getKey();
			ClassProtocol prot = entry.getValue().get();
			protocols.add(new ProtocolTuple(cls, prot));
		}
		return new Bytifier(protocols);
	}
	
}