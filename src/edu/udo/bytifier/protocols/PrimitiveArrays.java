package edu.udo.bytifier.protocols;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class PrimitiveArrays {
	
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
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
		public int getIdentificationNumber() {
			return 0xFEED00AA;
		}
	};
	
}