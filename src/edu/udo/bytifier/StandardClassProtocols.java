package edu.udo.bytifier;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class StandardClassProtocols {
	
	private StandardClassProtocols() {}
	
	public static final ClassProtocol OBJECT_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object input) {}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {return new Object();}
	};
	public static final StringProtocol STRING_UTF8_PROTOCOL = new StringProtocol(StandardCharsets.UTF_8);
	public static class StringProtocol implements ClassProtocol {
		protected final Charset charset;
		public StringProtocol(Charset charset) {
			this.charset = charset;
		}
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object input) {
			String str = (String) input;
			int length = str.length();
			data.writeInt3(length);
			
			byte[] bytes = str.getBytes(charset);
			data.writeBytes(bytes);
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			byte[] bytes = new byte[length];
			data.readBytes(bytes);
			return new String(bytes, charset);
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {}
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}
	public static class EnumProtocol<T extends Enum<T>> implements ClassProtocol {
		private final T[] values;
		public EnumProtocol(Class<T> enumCls) {
			values = enumCls.getEnumConstants();
		}
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object input) {
			@SuppressWarnings("unchecked")
			T enumConst = (T) input;
			data.writeIntForSize(values.length, enumConst.ordinal());
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			// intentionally left blank
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int ordinal = data.readIntForSize(values.length);
			return values[ordinal];
		}
	}
	public static class ArrayListProtocol implements ClassProtocol {
		@SuppressWarnings("rawtypes")
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			ArrayList list = (ArrayList) object;
			int length = list.size();
			data.writeInt2(length);
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
			int length = data.readInt2();
			ArrayList list = (ArrayList) object;
			list.ensureCapacity(length);
			for (int i = 0; i < length; i++) {
				list.add(bytifier.readChunk(data));
			}
		}
	}
	public static class CollectionProtocol<T> implements ClassProtocol {
		protected final Supplier<Collection<T>> constructor;
		public CollectionProtocol(Supplier<Collection<T>> constructor) {
			this.constructor = constructor;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			Collection<T> col = (Collection<T>) object;
			int length = col.size();
			data.writeInt2(length);
			for (T elem : col) {
				bytifier.writeChunk(data, elem, false);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			return constructor.get();
		}
		@SuppressWarnings("unchecked")
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			int length = data.readInt2();
			Collection<T> list = (Collection<T>) object;
			for (int i = 0; i < length; i++) {
				list.add((T) bytifier.readChunk(data));
			}
		}
	}
	public static class ArrayProtocol<T> implements ClassProtocol {
		protected final Class<T> elemType;
		public ArrayProtocol(Class<T> elementType) {
			elemType = elementType;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			T[] arr = (T[]) object;
			data.writeInt2(arr.length);
			for (int i = 0; i < arr.length; i++) {
				bytifier.writeChunk(data, arr[i], false);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt2();
			return Array.newInstance(elemType, length);
		}
		@SuppressWarnings("unchecked")
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			T[] arr = (T[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = (T) bytifier.readChunk(data);
			}
		}
	}
	public static ClassProtocol BOOL_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			boolean[] arr = (boolean[]) object;
			data.writeInt2(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeBoolean(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt2();
			return new boolean[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			boolean[] arr = (boolean[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readBoolean();
			}
		}
	};
	public static ClassProtocol INT_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			int[] arr = (int[]) object;
			data.writeInt2(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeInt4(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt2();
			return new int[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			int[] arr = (int[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readInt4();
			}
		}
	};
	
}