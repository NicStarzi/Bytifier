package edu.udo.bytifier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
	
	public void defineViaReflectionLimitedToFields(Class<?> cls, String ... fieldNames) {
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