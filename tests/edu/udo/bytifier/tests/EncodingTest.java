package edu.udo.bytifier.tests;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

class EncodingTest {
	
	static class TestEncoder extends EncodeData {
		public TestEncoder() {
			super(new Bytifier(), false);
		}
		int getPos() {
			return pos;
		}
		byte[] getBuf() {
			return byteBuf;
		}
	}
	
	@Test
	void testWriteIntForSize() {
		testWriteIntForSize(0, 0);
		testWriteIntForSize(42, 1);
		testWriteIntForSize(512, 2);
		testWriteIntForSize(128000, 3);
		testWriteIntForSize(Integer.MAX_VALUE / 2, 4);
		testWriteIntForSize(Integer.MAX_VALUE, 4);
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DecodeData.calculateByteCountFor(-1);
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			DecodeData.calculateByteCountFor(Integer.MIN_VALUE);
		});
	}
	
	void testWriteIntForSize(int value, int expectedBytes) {
		int maxValue;
		if (expectedBytes == Integer.BYTES) {
			maxValue = Integer.MAX_VALUE;
		} else {
			maxValue = (1 << (8 * expectedBytes)) - 1;
		}
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeIntForSize(maxValue, value);
		Assertions.assertEquals(pos + expectedBytes, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBytes(false), false);
		int decoded = decoder.readIntForSize(maxValue);
		Assertions.assertTrue(decoded >= 0);
		Assertions.assertEquals(value, decoded);
	}
	
	@Test
	void testWriteBytes() {
		byte[] empty = new byte[0];
		byte[] fewBytes = new byte[] {-42, +42, -24, +24, 0, -1, +1, Byte.MAX_VALUE, Byte.MIN_VALUE};
		byte[] manyBytes = new byte[128];
		manyBytes[0] = 0;
		manyBytes[1] = 1;
		for (int i = 2; i < manyBytes.length; i++) {
			manyBytes[i] = (byte) (manyBytes[i - 1] + manyBytes[i - 2]);
		}
		
		testWriteBytes(empty, 0, 0);
		testWriteBytes(fewBytes, 0, 0);
		testWriteBytes(manyBytes, 0, 0);
		
		testWriteBytes(fewBytes, 0, fewBytes.length);
		testWriteBytes(manyBytes, 0, manyBytes.length);
		
		testWriteBytes(fewBytes, 0, fewBytes.length / 2);
		testWriteBytes(manyBytes, 0, manyBytes.length / 2);
		
		testWriteBytes(fewBytes, fewBytes.length / 2, fewBytes.length / 2);
		testWriteBytes(manyBytes, manyBytes.length / 2, manyBytes.length / 2);
	}
	
	void testWriteBytes(byte[] arr, int offset, int length) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeBytes(arr, offset, length);
		Assertions.assertEquals(pos + length, encoder.getPos());
		
		byte[] expected = Arrays.copyOfRange(arr, offset, offset + length);
		byte[] written = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, written);
	}
	
	@Test
	void testWriteBoolean() {
		testWriteBoolean(true);
		testWriteBoolean(false);
	}
	
	void testWriteBoolean(boolean value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeBoolean(value);
		Assertions.assertEquals(pos + 1, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readBoolean());
	}
	
	@Test
	void testWriteInt1() {
		testWriteInt1((byte) 0);
		testWriteInt1((byte) 1);
		testWriteInt1((byte) -1);
		testWriteInt1((byte) 42);
		testWriteInt1((byte) -42);
		testWriteInt1((byte) 192);
		testWriteInt1(Byte.MIN_VALUE);
		testWriteInt1((byte) (Byte.MIN_VALUE+1));
		testWriteInt1((byte) (Byte.MIN_VALUE-1));
		testWriteInt1(Byte.MAX_VALUE);
		testWriteInt1((byte) (Byte.MAX_VALUE-1));
		testWriteInt1((byte) (Byte.MAX_VALUE+1));
	}
	
	void testWriteInt1(byte value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeInt1(value);
		Assertions.assertEquals(pos + Byte.BYTES, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readInt1());
	}
	
	@Test
	void testWriteInt2() {
		testWriteInt2((short) 0);
		testWriteInt2((short) 1);
		testWriteInt2((short) -1);
		testWriteInt2((short) 42);
		testWriteInt2((short) -42);
		testWriteInt2(Short.MIN_VALUE);
		testWriteInt2((short) (Short.MIN_VALUE+1));
		testWriteInt2((short) (Short.MIN_VALUE-1));
		testWriteInt2(Short.MAX_VALUE);
		testWriteInt2((short) (Short.MAX_VALUE-1));
		testWriteInt2((short) (Short.MAX_VALUE+1));
	}
	
	void testWriteInt2(short value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeInt2(value);
		Assertions.assertEquals(pos + Short.BYTES, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readInt2());
	}
	
	@Test
	void testWriteInt3() {
		testWriteInt3(0);
		testWriteInt3(1);
		testWriteInt3(-1);
		testWriteInt3(42);
		testWriteInt3(-42);
		testWriteInt3(Integer.MIN_VALUE);
		testWriteInt3(Integer.MIN_VALUE+1);
		testWriteInt3(Integer.MIN_VALUE-1);
		testWriteInt3(Integer.MAX_VALUE);
		testWriteInt3(Integer.MAX_VALUE-1);
		testWriteInt3(Integer.MAX_VALUE+1);
	}
	
	void testWriteInt3(int value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeInt3(value);
		Assertions.assertEquals(pos + 3, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readInt3());
	}
	
	@Test
	void testWriteInt4() {
		testWriteInt4(0);
		testWriteInt4(1);
		testWriteInt4(-1);
		testWriteInt4(42);
		testWriteInt4(-42);
		testWriteInt4(Integer.MIN_VALUE);
		testWriteInt4(Integer.MIN_VALUE+1);
		testWriteInt4(Integer.MIN_VALUE-1);
		testWriteInt4(Integer.MAX_VALUE);
		testWriteInt4(Integer.MAX_VALUE-1);
		testWriteInt4(Integer.MAX_VALUE+1);
	}
	
	void testWriteInt4(int value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeInt4(value);
		Assertions.assertEquals(pos + Integer.BYTES, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readInt4());
	}
	
	@Test
	void testWriteInt8() {
		testWriteInt8(0);
		testWriteInt8(1);
		testWriteInt8(-1);
		testWriteInt8(42);
		testWriteInt8(-42);
		testWriteInt8(Integer.MIN_VALUE);
		testWriteInt8(Integer.MAX_VALUE);
		testWriteInt8(Long.MIN_VALUE);
		testWriteInt8(Long.MIN_VALUE+1);
		testWriteInt8(Long.MIN_VALUE-1);
		testWriteInt8(Long.MAX_VALUE);
		testWriteInt8(Long.MAX_VALUE-1);
		testWriteInt8(Long.MAX_VALUE+1);
	}
	
	void testWriteInt8(long value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeInt8(value);
		Assertions.assertEquals(pos + Long.BYTES, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readInt8());
	}
	
	@Test
	void testWriteFloat4() {
		testWriteFloat4(0f);
		testWriteFloat4(+0f);
		testWriteFloat4(-0f);
		testWriteFloat4(+1f);
		testWriteFloat4(-1f);
		testWriteFloat4((float)Math.PI);
		testWriteFloat4(-(float)Math.PI);
		testWriteFloat4((float)Math.E);
		testWriteFloat4(-(float)Math.E);
		testWriteFloat4(Float.MIN_VALUE);
		testWriteFloat4(-Float.MIN_VALUE);
		testWriteFloat4(Float.MIN_NORMAL);
		testWriteFloat4(-Float.MIN_NORMAL);
		testWriteFloat4(Float.MAX_VALUE);
		testWriteFloat4(-Float.MAX_VALUE);
		testWriteFloat4(Float.POSITIVE_INFINITY);
		testWriteFloat4(Float.NEGATIVE_INFINITY);
		testWriteFloat4(Float.NaN);
	}
	
	void testWriteFloat4(float value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeFloat4(value);
		Assertions.assertEquals(pos + Float.BYTES, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readFloat4());
	}
	
	@Test
	void testWriteFloat8() {
		testWriteFloat8(0f);
		testWriteFloat8(+0f);
		testWriteFloat8(-0f);
		testWriteFloat8(+1f);
		testWriteFloat8(-1f);
		testWriteFloat8(Math.PI);
		testWriteFloat8(-Math.PI);
		testWriteFloat8(Math.E);
		testWriteFloat8(-Math.E);
		testWriteFloat8(Double.MIN_VALUE);
		testWriteFloat8(-Double.MIN_VALUE);
		testWriteFloat8(Double.MIN_NORMAL);
		testWriteFloat8(-Double.MIN_NORMAL);
		testWriteFloat8(Double.MAX_VALUE);
		testWriteFloat8(-Double.MAX_VALUE);
		testWriteFloat8(Double.POSITIVE_INFINITY);
		testWriteFloat8(Double.NEGATIVE_INFINITY);
		testWriteFloat8(Double.NaN);
	}
	
	void testWriteFloat8(double value) {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		encoder.writeFloat8(value);
		Assertions.assertEquals(pos + Double.BYTES, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(value, decoder.readFloat8());
	}
	
	@Test
	void testWriteManyData() {
		TestEncoder encoder = new TestEncoder();
		int pos = encoder.getPos();
		
		encoder.writeBoolean(true);		pos += 1;
		encoder.writeBoolean(false);	pos += 1;
		encoder.writeInt1(42);			pos += 1;
		encoder.writeInt2(42);			pos += 2;
		encoder.writeInt3(42);			pos += 3;
		encoder.writeInt4(42);			pos += 4;
		encoder.writeInt8(42);			pos += 8;
		encoder.writeFloat4(4.2f);		pos += 4;
		encoder.writeFloat8(4.2);		pos += 8;
		
		Assertions.assertEquals(pos, encoder.getPos());
		
		DecodeData decoder = new DecodeData(encoder.getBuf(), false);
		Assertions.assertEquals(true, decoder.readBoolean());
		Assertions.assertEquals(false, decoder.readBoolean());
		Assertions.assertEquals(42, decoder.readInt1());
		Assertions.assertEquals(42, decoder.readInt2());
		Assertions.assertEquals(42, decoder.readInt3());
		Assertions.assertEquals(42, decoder.readInt4());
		Assertions.assertEquals(42, decoder.readInt8());
		Assertions.assertEquals(4.2f, decoder.readFloat4());
		Assertions.assertEquals(4.2, decoder.readFloat8());
	}
	
}
