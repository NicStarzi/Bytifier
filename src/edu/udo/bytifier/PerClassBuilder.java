package edu.udo.bytifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import edu.udo.bytifier.protocols.StringProtocol;

public class PerClassBuilder<CLS_T> {
	
	protected final List<WriteAction> writeActions = new ArrayList<>();
	protected final List<ReadAction> readActions = new ArrayList<>();
	protected final List<ConstructorData> constrData = new ArrayList<>();
	protected final Class<CLS_T> cls;
	protected final ProtocolBuilder bldr;
	protected boolean useFieldHash = false;
	protected int fieldHash = 0;
	protected boolean computeIdfNum = true;
	protected int idfNum = 0;
	
	public PerClassBuilder(ProtocolBuilder builder, Class<CLS_T> clazz) {
		bldr = builder;
		cls = clazz;
	}
	
	public ProtocolBuilder endDefinition() {
		return bldr;
	}
	
	protected int computeMagicNumber() {
		return calculateFieldHash(cls);
	}
	
	protected int calculateFieldHash(Class<?> classToTest) {
		final int prime = 31;
		int hash = 1;
		Class<?> curCls = classToTest;
		while (curCls != Object.class) {
			for (Field field : curCls.getDeclaredFields()) {
				String fieldName = field.getName();
				Class<?> fieldType = field.getType();
				String fieldTypeName = fieldType.getName();
				hash = hash * prime + fieldName.hashCode();
				hash = hash * prime + fieldTypeName.hashCode();
			}
			curCls = curCls.getSuperclass();
		}
		return hash;
	}
	
	public PerClassBuilder<CLS_T> setProtocolIdentificationNumber(int value) {
		idfNum = value;
		setAutoComputeProtocolIdentificationNumber(false);
		return this;
	}
	
	public PerClassBuilder<CLS_T> setAutoComputeProtocolIdentificationNumber(boolean value) {
		computeIdfNum = value;
		return this;
	}
	
	public PerClassBuilder<CLS_T> useFieldHashing(boolean value) {
		useFieldHash = value;
		fieldHash = useFieldHash ? calculateFieldHash(cls) : 0;
		return this;
	}
	
	public PerClassBuilder<CLS_T> addConstructorString(Function<CLS_T, String> getter) {
		constrData.add(new ConstructorData(String.class,
				(bytifier, data, input) ->
				{
					@SuppressWarnings("unchecked")
					CLS_T obj = (CLS_T) input;
					String val = getter.apply(obj);
					StringProtocol.STRING_UTF8_PROTOCOL.write(bytifier, data, val);
				},
				(bytifier, data) -> {
					String val = (String) StringProtocol.STRING_UTF8_PROTOCOL.create(bytifier, data);
					StringProtocol.STRING_UTF8_PROTOCOL.read(bytifier, data, val);
					return val;
				}));
		return this;
	}
	
	public PerClassBuilder<CLS_T> addConstructorInt(ToIntFunction<CLS_T> getter) {
		constrData.add(new ConstructorData(Integer.TYPE,
				(bytifier, data, input) ->
				{
					@SuppressWarnings("unchecked")
					CLS_T obj = (CLS_T) input;
					int val = getter.applyAsInt(obj);
					data.writeInt4(val);
				},
				(bytifier, data) -> data.readInt4()));
		return this;
	}
	
	public PerClassBuilder<CLS_T> addConstructorDouble(ToDoubleFunction<CLS_T> getter) {
		constrData.add(new ConstructorData(Double.TYPE,
				(bytifier, data, input) ->
				{
					@SuppressWarnings("unchecked")
					CLS_T obj = (CLS_T) input;
					double val = getter.applyAsDouble(obj);
					data.writeFloat8(val);
				},
				(bytifier, data) -> data.readFloat8()));
		return this;
	}
	
	public PerClassBuilder<CLS_T> addConstructorBool(Predicate<CLS_T> getter) {
		constrData.add(new ConstructorData(Boolean.TYPE,
				(bytifier, data, input) ->
				{
					@SuppressWarnings("unchecked")
					CLS_T obj = (CLS_T) input;
					boolean val = getter.test(obj);
					data.writeBoolean(val);
				},
				(bytifier, data) -> data.readBoolean()));
		return this;
	}
	
	public PerClassBuilder<CLS_T> addFieldString(Function<CLS_T, String> getter, BiConsumer<CLS_T, String> setter) {
		writeActions.add(
				(bytifier, data, input) ->
		{
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) input;
			String val = getter.apply(obj);
			StringProtocol.STRING_UTF8_PROTOCOL.write(bytifier, data, val);
		});
		readActions.add(
				(bytifier, data, object) ->
		{
			String val = (String) StringProtocol.STRING_UTF8_PROTOCOL.create(bytifier, data);
			StringProtocol.STRING_UTF8_PROTOCOL.read(bytifier, data, val);
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) object;
			setter.accept(obj, val);
		});
		return this;
	}
	
	public PerClassBuilder<CLS_T> addFieldInt(ToIntFunction<CLS_T> getter, BiConsumer<CLS_T, Integer> setter) {
		writeActions.add(
				(bytifier, data, input) ->
		{
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) input;
			int val = getter.applyAsInt(obj);
			data.writeInt4(val);
		});
		readActions.add(
				(bytifier, data, object) ->
		{
			int val = data.readInt4();
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) object;
			setter.accept(obj, val);
		});
		return this;
	}
	
	public PerClassBuilder<CLS_T> addFieldDouble(ToDoubleFunction<CLS_T> getter, BiConsumer<CLS_T, Double> setter) {
		writeActions.add(
				(bytifier, data, input) ->
		{
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) input;
			double val = getter.applyAsDouble(obj);
			data.writeFloat8(val);
		});
		readActions.add(
				(bytifier, data, object) ->
		{
			double val = data.readFloat8();
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) object;
			setter.accept(obj, val);
		});
		return this;
	}
	
	public PerClassBuilder<CLS_T> addFieldBool(Predicate<CLS_T> getter, BiConsumer<CLS_T, Boolean> setter) {
		writeActions.add(
				(bytifier, data, input) ->
		{
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) input;
			boolean val = getter.test(obj);
			data.writeBoolean(val);
		});
		readActions.add(
				(bytifier, data, object) ->
		{
			boolean val = data.readBoolean();
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) object;
			setter.accept(obj, val);
		});
		return this;
	}
	
	public <REF_T> PerClassBuilder<CLS_T> addFieldValueObject(Function<CLS_T, REF_T> getter, BiConsumer<CLS_T, REF_T> setter) {
		writeActions.add(
				(bytifier, data, input) ->
		{
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) input;
			REF_T val = getter.apply(obj);
			bytifier.writeChunk(data, val, true);
		});
		readActions.add(
				(bytifier, data, object) ->
		{
			@SuppressWarnings("unchecked")
			REF_T val = (REF_T) bytifier.readChunk(data);
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) object;
			setter.accept(obj, val);
		});
		return this;
	}
	
	public <REF_T> PerClassBuilder<CLS_T> addFieldReferencedObject(Function<CLS_T, REF_T> getter, BiConsumer<CLS_T, REF_T> setter) {
		writeActions.add(
				(bytifier, data, input) ->
		{
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) input;
			REF_T val = getter.apply(obj);
			bytifier.writeChunk(data, val, false);
		});
		readActions.add(
				(bytifier, data, object) ->
		{
			@SuppressWarnings("unchecked")
			REF_T val = (REF_T) bytifier.readChunk(data);
			@SuppressWarnings("unchecked")
			CLS_T obj = (CLS_T) object;
			setter.accept(obj, val);
		});
		return this;
	}
	
	public ClassProtocol build() {
		try {
			final int constrParamCount = constrData.size();
			final Class<?>[] constrParamTypes = new Class[constrParamCount];
			int idx = 0;
			for (ConstructorData data : constrData) {
				constrParamTypes[idx++] = data.type;
			}
			final Constructor<CLS_T> constructor = cls.getDeclaredConstructor(constrParamTypes);
			constructor.setAccessible(true);
			int fieldHash = this.fieldHash;
			if (computeIdfNum) {
				idfNum = computeMagicNumber();
			}
			final int magNum = idfNum;
			
			return new ClassProtocol() {
				@Override
				public void write(Bytifier bytifier, EncodeData data, Object input) {
					if (useFieldHash) {
						data.writeInt4(fieldHash);
					}
					for (ConstructorData constrData : constrData) {
						constrData.writer.write(bytifier, data, input);
					}
					for (WriteAction writer : writeActions) {
						writer.write(bytifier, data, input);
					}
				}
				@Override
				public void read(Bytifier bytifier, DecodeData data, Object object) {
					for (ReadAction reader : readActions) {
						reader.read(bytifier, data, object);
					}
				}
				@Override
				public Object create(Bytifier bytifier, DecodeData data) {
					if (useFieldHash) {
						int readFieldHash = data.readInt4();
						if (fieldHash != readFieldHash) {
							throw new IllegalArgumentException("expectedFieldHash="+fieldHash+"; readFieldHash="+readFieldHash);
						}
					}
					try {
						Object[] params = new Object[constrParamCount];
						for (int i = 0; i < constrParamCount; i++) {
							params[i] = constrData.get(i).reader.read(bytifier, data);
						}
						return constructor.newInstance(params);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				@Override
				public int getIdentificationNumber() {
					return magNum;
				}
			};
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected class ConstructorData {
		protected final Class<?> type;
		protected final WriteAction writer;
		protected final ConstructorReadAction reader;
		public ConstructorData(Class<?> paramType, WriteAction writeFunc, ConstructorReadAction readFunc) {
			super();
			type = paramType;
			writer = writeFunc;
			reader = readFunc;
		}
	}
	
	public static interface WriteAction {
		public void write(Bytifier bytifier, EncodeData data, Object input);
	}
	
	public static interface ReadAction {
		public void read(Bytifier bytifier, DecodeData data, Object object);
	}
	
	public static interface ConstructorReadAction {
		public Object read(Bytifier bytifier, DecodeData data);
	}
	
}