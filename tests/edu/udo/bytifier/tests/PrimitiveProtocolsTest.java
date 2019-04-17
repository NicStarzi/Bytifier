package edu.udo.bytifier.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ProtocolBuilder;

class PrimitiveProtocolsTest {
	
	Bytifier bytifier;
	
	@BeforeEach
	void setup() {
		bytifier = new ProtocolBuilder().build();
	}
	
	@Test
	void testByteArray() {
		byte[] arr = {0, 1, -1,
				Byte.MAX_VALUE / 2, Byte.MIN_VALUE / 2,
				Byte.MAX_VALUE - 1, Byte.MIN_VALUE + 1,
				Byte.MAX_VALUE, Byte.MIN_VALUE};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		byte[] resArr = (byte[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testShortArray() {
		short[] arr = {0, 1, -1,
				Short.MAX_VALUE / 2, Short.MIN_VALUE / 2,
				Short.MAX_VALUE - 1, Short.MIN_VALUE + 1,
				Short.MAX_VALUE, Short.MIN_VALUE};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		short[] resArr = (short[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testIntArray() {
		int[] arr = {0, 1, -1,
				Integer.MAX_VALUE / 2, Integer.MIN_VALUE / 2,
				Integer.MAX_VALUE - 1, Integer.MIN_VALUE + 1,
				Integer.MAX_VALUE, Integer.MIN_VALUE};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		int[] resArr = (int[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testLongArray() {
		long[] arr = {0, 1, -1,
				Long.MAX_VALUE / 2, Long.MIN_VALUE / 2,
				Long.MAX_VALUE - 1, Long.MIN_VALUE + 1,
				Long.MAX_VALUE, Long.MIN_VALUE};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		long[] resArr = (long[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testFloatArray() {
		float[] arr = {+0f, -0f,
				Float.MIN_VALUE, -Float.MIN_VALUE,
				1f, Math.nextAfter(1f, +1), Math.nextAfter(Math.nextAfter(1f, +1), +1),
				Float.MAX_VALUE / 2, -Float.MAX_VALUE / 2,
				Float.MAX_VALUE - 1, -Float.MAX_VALUE + 1,
				Float.MAX_VALUE, Float.MIN_VALUE,
				Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
				Float.NaN};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		float[] resArr = (float[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testDoubleArray() {
		double[] arr = {+0.0, -0.0,
				Double.MIN_VALUE, -Double.MIN_VALUE,
				1.0, Math.nextAfter(1.0, +1), Math.nextAfter(Math.nextAfter(1.0, +1), +1),
				Double.MAX_VALUE / 2, -Double.MAX_VALUE / 2,
				Double.MAX_VALUE - 1, -Double.MAX_VALUE + 1,
				Double.MAX_VALUE, Double.MIN_VALUE,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.NaN};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		double[] resArr = (double[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testBoolArray() {
		boolean[] arr = {Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE};
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		boolean[] resArr = (boolean[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
	@Test
	void testCharArray() {
		String abc = "abcdefghijklmnopqrstuvwxyz";
		String capABC = abc.toUpperCase();
		String numbers = "0123456789";
		String symbols = "+-*/%$!&^~.,:;_#?()";
		String manyChars = abc + capABC + numbers + symbols;
		char[] arr = manyChars.toCharArray();
		
		byte[] bytes = bytifier.encode(arr);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(arr.getClass(), result.getClass());
		char[] resArr = (char[]) result;
		Assertions.assertArrayEquals(arr, resArr);
	}
	
}