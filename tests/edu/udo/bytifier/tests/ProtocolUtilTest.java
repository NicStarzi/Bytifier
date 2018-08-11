package edu.udo.bytifier.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.udo.bytifier.protocols.ProtocolUtil;

class ProtocolUtilTest {
	
	@Test
	void testArrayDimension() {
		Assertions.assertThrows(NullPointerException.class, () -> testArrayDimension(0, null));
		testArrayDimension(1, new Object[0]);
		testArrayDimension(2, new Object[0][0]);
		testArrayDimension(3, new Object[0][0][0]);
		testArrayDimension(0, new Object());
		testArrayDimension(1, new int[2]);
		testArrayDimension(2, new int[2][4]);
		testArrayDimension(3, new int[2][4][8]);
		testArrayDimension(2, new Object[0][]);
	}
	
	private void testArrayDimension(int expectedDimension, Object arr) {
		int calculatedDimension = ProtocolUtil.getArrayDimension(arr);
		Assertions.assertEquals(expectedDimension, calculatedDimension);
	}
	
	@Test
	void testArrayElementType() {
		Assertions.assertThrows(NullPointerException.class, () -> testArrayElementType(null, null));
		testArrayElementType(Object.class, new Object[0]);
		testArrayElementType(String.class, new String[0][0]);
		testArrayElementType(Double.class, new Double[0][0][0]);
		testArrayElementType(null, new Object());
		testArrayElementType(Integer.TYPE, new int[2]);
		testArrayElementType(Integer.TYPE, new int[2][4]);
		testArrayElementType(Float.TYPE, new float[2][4][8]);
		testArrayElementType(Object.class, new Object[0][]);
	}
	
	private void testArrayElementType(Class<?> expectedType, Object arr) {
		Class<?> calculatedType = ProtocolUtil.getArrayElementType(arr);
		Assertions.assertEquals(expectedType, calculatedType);
	}
	
}