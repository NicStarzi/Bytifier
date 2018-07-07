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
		@Override
		public int getMagicNumber() {return 0xFEED000C;}
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
		public int getMagicNumber() {
			return getClass().getName().hashCode() + 31 * charset.name().hashCode();
		}
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
		@Override
		public int getMagicNumber() {
			int prime = 37;
			int magNum = getClass().getName().hashCode();
			if (values.length == 0) {
				return magNum;
			}
			magNum += prime * values.length;
			@SuppressWarnings("unchecked")
			Class<T> cls = (Class<T>) values[0].getClass();
			magNum += prime * cls.getName().hashCode();
			return magNum;
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
	public static ClassProtocol BYTE_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			byte[] arr = (byte[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeInt1(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new byte[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			byte[] arr = (byte[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readInt1();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED000A;
		}
	};
	public static ClassProtocol SHORT_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			short[] arr = (short[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeInt2(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new short[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			short[] arr = (short[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readInt2();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED000B;
		}
	};
	public static ClassProtocol INT_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			int[] arr = (int[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeInt4(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new int[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			int[] arr = (int[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readInt4();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED000C;
		}
	};
	public static ClassProtocol LONG_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			long[] arr = (long[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeInt8(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new long[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			long[] arr = (long[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readInt8();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED000D;
		}
	};
	public static ClassProtocol FLOAT_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			float[] arr = (float[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeFloat4(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new float[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			float[] arr = (float[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readFloat4();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED000E;
		}
	};
	public static ClassProtocol DOUBLE_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			double[] arr = (double[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeFloat8(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new double[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			double[] arr = (double[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readFloat8();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED000F;
		}
	};
	public static ClassProtocol BOOL_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			boolean[] arr = (boolean[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeBoolean(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new boolean[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			boolean[] arr = (boolean[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = data.readBoolean();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED00A0;
		}
	};
	public static ClassProtocol CHAR_ARRAY_PROTOCOL = new ClassProtocol() {
		@Override
		public void write(Bytifier bytifier, EncodeData data, Object object) {
			char[] arr = (char[]) object;
			data.writeInt3(arr.length);
			for (int i = 0; i < arr.length; i++) {
				data.writeInt2(arr[i]);
			}
		}
		@Override
		public Object create(Bytifier bytifier, DecodeData data) {
			int length = data.readInt3();
			return new char[length];
		}
		@Override
		public void read(Bytifier bytifier, DecodeData data, Object object) {
			char[] arr = (char[]) object;
			for (int i = 0; i < arr.length; i++) {
				arr[i] = (char) data.readInt2();
			}
		}
		@Override
		public int getMagicNumber() {
			return 0xFEED00AA;
		}
	};
	
}