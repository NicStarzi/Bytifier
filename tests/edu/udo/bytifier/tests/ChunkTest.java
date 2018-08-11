package edu.udo.bytifier.tests;

import java.lang.reflect.Array;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.Bytifier.ProtocolTuple;
import edu.udo.bytifier.ChunkType;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;
import edu.udo.bytifier.StandardClassProtocols;

class ChunkTest {
	
	static class EmptyClass{}
	static class ClassWithPrimitiveAttributes {
		int i = 42;
		float f = 4.2f;
		long l = 21L;
		double d = 2.1;
	}
	static class ValueTypeArrayWrapper {
		Object[] arr;
	}
	
	Bytifier bytifier;
	EncodeData encoder;
	
	@BeforeEach
	void createBytifier() {
		ProtocolTuple tupObj = new ProtocolTuple(Object.class, StandardClassProtocols.OBJECT_PROTOCOL);
		ProtocolTuple tupEmpty = new ProtocolTuple(EmptyClass.class, new ClassProtocol() {
			@Override
			public void write(Bytifier bytifier, EncodeData data, Object input) {}
			@Override
			public void read(Bytifier bytifier, DecodeData data, Object object) {}
			@Override
			public Object create(Bytifier bytifier, DecodeData data) {
				return new EmptyClass();
			}
			@Override
			public int getMagicNumber() {return 1;}
		});
		ProtocolTuple tupAttr = new ProtocolTuple(ClassWithPrimitiveAttributes.class, new ClassProtocol() {
			@Override
			public void write(Bytifier bytifier, EncodeData data, Object input) {
				ClassWithPrimitiveAttributes c = (ClassWithPrimitiveAttributes) input;
				data.writeInt4(c.i);
				data.writeFloat4(c.f);
				data.writeInt8(c.l);
				data.writeFloat8(c.d);
			}
			@Override
			public void read(Bytifier bytifier, DecodeData data, Object object) {
				ClassWithPrimitiveAttributes c = (ClassWithPrimitiveAttributes) object;
				c.i = data.readInt4();
				c.f = data.readFloat4();
				c.l = data.readInt8();
				c.d = data.readFloat8();
			}
			@Override
			public Object create(Bytifier bytifier, DecodeData data) {
				return new ClassWithPrimitiveAttributes();
			}
			@Override
			public int getMagicNumber() {return 1;}
		});
		ProtocolTuple tupValTypeArr = new ProtocolTuple(ValueTypeArrayWrapper.class, new ClassProtocol() {
			@Override
			public void write(Bytifier bytifier, EncodeData data, Object input) {
				ValueTypeArrayWrapper c = (ValueTypeArrayWrapper) input;
				int clsIdx = data.getProtocolIndexFor(c.arr.getClass().getComponentType());
				data.writeClassIndex(clsIdx);
				data.writeInt3(c.arr.length);
				for (Object obj : c.arr) {
					bytifier.writeChunk(data, obj, true);
				}
			}
			@Override
			public void read(Bytifier bytifier, DecodeData data, Object object) {
				ValueTypeArrayWrapper c = (ValueTypeArrayWrapper) object;
				int clsIdx = data.readClassIndex();
				Class<?> arrCls = bytifier.getProtocols().get(clsIdx).cls;
				int len = data.readInt3();
				c.arr = (Object[]) Array.newInstance(arrCls, len);
				for (int i = 0; i < len; i++) {
					c.arr[i] = bytifier.readChunk(data);
				}
			}
			@Override
			public Object create(Bytifier bytifier, DecodeData data) {
				return new ValueTypeArrayWrapper();
			}
			@Override
			public int getMagicNumber() {return 1;}
		});
		
		bytifier = new Bytifier(tupObj, tupEmpty, tupAttr, tupValTypeArr);
		encoder = new EncodeData(bytifier, false);
	}
	
	@Test
	void testEncodeDecode() {
		EmptyClass ec1 = new EmptyClass();
		EmptyClass ec2 = new EmptyClass();
		
		Object[] arr1 = {ec2, ec1, null, ec1};
		
		Object[][] outerArr = {
			arr1,
			new ClassWithPrimitiveAttributes[] {new ClassWithPrimitiveAttributes()},
			new EmptyClass[] {ec1, null, ec2},
			arr1,
			{},
		};
		
		byte[] encoded = bytifier.encode(outerArr);
		Object decoded = bytifier.decode(encoded);
		
		Assertions.assertNotNull(decoded);
		Assertions.assertEquals(Object[][].class, decoded.getClass());
		
		Object[][] result = (Object[][]) decoded;
		int len = outerArr.length;
		Assertions.assertEquals(len, result.length);
		for (int i = 0; i < len; i++) {
			Object[] expInner = outerArr[i];
			Object[] actInner = result[i];
			
			int lenInner = expInner.length;
			Assertions.assertEquals(lenInner, actInner.length);
			Assertions.assertEquals(expInner.getClass(), actInner.getClass());
			for (int j = 0; j < lenInner; j++) {
				Object exp = expInner[j];
				Object act = actInner[j];
				
				if (exp == null) {
					Assertions.assertSame(exp, act);
				} else {
					Assertions.assertEquals(exp.getClass(), act.getClass());
				}
			}
		}
	}
	
	@Test
	void testReadChunkNull() {
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(0);// number of references.
		// chunk for null pointer
		encoder.writeChunkType(ChunkType.NULL);
		// we write the reference count by hand
		byte[] bytes = encoder.getBytes(false);
		Object result = bytifier.decode(bytes);
		Assertions.assertNull(result);
	}
	
	@Test
	void testReadNewObjRef() {
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(1);// number of references.
		// chunk for new instance of EmptyClass
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		// we write the reference count by hand
		byte[] bytes = encoder.getBytes(false);
		Object result = bytifier.decode(bytes);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(EmptyClass.class, result.getClass());
	}
	
	@Test
	void testReadValueType() {
		int arrLen = 3;
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(1);// number of references. One instance of ValueTypeArrayWrapper.
		// chunk for ValueTypeArrayWrapper instance
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(ValueTypeArrayWrapper.class));
		// index for Object array within wrapper
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt3(arrLen);// array length
		
		// chunks for EmptyClass instances/references
		for (int i = 0; i < arrLen; i++) {
			encoder.writeChunkType(ChunkType.VALUE_OBJ_TYPE);
			encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		}
		
		// we write the reference count by hand
		byte[] bytes = encoder.getBytes(false);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(ValueTypeArrayWrapper.class, result.getClass());
		
		ValueTypeArrayWrapper wrapper = (ValueTypeArrayWrapper) result;
		Assertions.assertNotNull(wrapper.arr);
		Assertions.assertEquals(Object[].class, wrapper.arr.getClass());
		Assertions.assertEquals(arrLen, wrapper.arr.length);
		
		for (int i = 0; i < arrLen; i++) {
			Assertions.assertNotNull(wrapper.arr[i]);
			Assertions.assertEquals(EmptyClass.class, wrapper.arr[i].getClass());
			
			for (int j = i + 1; j < arrLen; j++) {
				Assertions.assertNotSame(wrapper.arr[j], wrapper.arr[i]);
			}
		}
	}
	
	@Test
	void testReadGenericArray() {
		/*
		 * We write a two-dimensional object array containing three one-dimensional object arrays
		 * and a null reference to the byte stream and attempt to decode it.
		 */
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(5);// number of references. (outer array, filled array, empty array, 2 elements of filled array}
		// chunk for generic array with element type object
		encoder.writeChunkType(ChunkType.GENERIC_ARRAY);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt1(2);// dimension of outer array (2 dimensional)
		encoder.writeInt3(4);// length of outer array {arr1, emptyArr, null, arr1}
		// chunk for first (arr1) generic array with element type object
		encoder.writeChunkType(ChunkType.GENERIC_ARRAY);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt1(1);// dimension of array (1 dimensional)
		encoder.writeInt3(3);// length of array
		// chunk for a new instance of EmptyClass (element of arr1)
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		// chunk for null pointer (element of arr1)
		encoder.writeChunkType(ChunkType.NULL);
		// chunk for a new instance of Object (element of arr1)
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		// chunk for second (empty) generic array with element type object
		encoder.writeChunkType(ChunkType.GENERIC_ARRAY);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt1(1);// dimension of array (1 dimensional)
		encoder.writeInt3(0);// length of array
		// chunk for null pointer
		encoder.writeChunkType(ChunkType.NULL);
		// chunk for reference to arr1
		encoder.writeChunkType(ChunkType.READ_OBJ_REF);
		encoder.writeClassIndex(1);// see table
		
		// we write the reference count by hand
		byte[] bytes = encoder.getBytes(false);
		Object result = bytifier.decode(bytes);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(Object[][].class, result.getClass());
		
		Object[][] outerArr = (Object[][]) result;
		Assertions.assertEquals(4, outerArr.length);
		
		Assertions.assertNotNull(outerArr[0]);
		Assertions.assertEquals(Object[].class, outerArr[0].getClass());
		
		// arr1
		Object[] arr1 = outerArr[0];
		Assertions.assertEquals(3, arr1.length);
		
		Assertions.assertNotNull(arr1[0]);
		Assertions.assertEquals(EmptyClass.class, arr1[0].getClass());
		
		Assertions.assertNull(arr1[1]);
		
		Assertions.assertNotNull(arr1[2]);
		Assertions.assertEquals(Object.class, arr1[2].getClass());
		
		// empty arr
		Assertions.assertNotNull(outerArr[1]);
		Assertions.assertEquals(Object[].class, outerArr[1].getClass());
		
		Object[] emptyArr = outerArr[1];
		Assertions.assertEquals(0, emptyArr.length);
		
		// null ref
		Assertions.assertNull(outerArr[2]);
		
		// ref to arr1
		Assertions.assertNotNull(outerArr[3]);
		Assertions.assertSame(arr1, outerArr[3]);
	}
	
	@Test
	void testClassProtocolIndices() {
		testClassProtocolIndex(Object.class, 0);
		testClassProtocolIndex(EmptyClass.class, 1);
		testClassProtocolIndex(ClassWithPrimitiveAttributes.class, 2);
	}
	
	private void testClassProtocolIndex(Class<?> cls, int expectedIndex) {
		int idx = encoder.getProtocolIndexFor(cls);
		Assertions.assertTrue(idx >= 0 && idx < bytifier.getProtocols().size());
		Class<?> clsFromBytifier = bytifier.getProtocols().get(idx).cls;
		Assertions.assertEquals(cls, clsFromBytifier);
		Assertions.assertEquals(expectedIndex, idx);
	}
	
	@Test
	void testEncode() {
		EmptyClass c1 = new EmptyClass();
		ClassWithPrimitiveAttributes c2 = new ClassWithPrimitiveAttributes();
		Object[] arr = {c1, c2};
		byte[] result = bytifier.encode(arr);
		
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(3);// number of references. {arr, c1, c2}
		// chunk for generic object array
		encoder.writeChunkType(ChunkType.GENERIC_ARRAY);
		encoder.writeInt1(0);// class index for Object.class
		encoder.writeInt1(1);// write dimension of array (1 dimensional)
		encoder.writeInt3(2);// write array length
		// chunk for c1
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeInt1(1);// class index for EmptyClass.class
		// chunk for c2
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeInt1(2);// class index for EmptyClass.class
		encoder.writeInt4(c2.i);
		encoder.writeFloat4(c2.f);
		encoder.writeInt8(c2.l);
		encoder.writeFloat8(c2.d);
		
		byte[] expected = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, result);
	}
	
	@Test
	void testWriteChunkNull() {
		byte[] result = bytifier.encode(null);
		
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(0);// number of references.
		// chunk for null pointer
		encoder.writeChunkType(ChunkType.NULL);
		
		byte[] expected = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, result);
	}
	
	@Test
	void testWriteNewReference() {
		byte[] result = bytifier.encode(new EmptyClass());
		
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(1);// number of references.
		// chunk for EmptyClass instance
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		// the encoder and the bytifier must have synchronized protocol lists for this to work!
		encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		// the empty class does not contain any data, so here is nothing more to add
		
		byte[] expected = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, result);
	}
	
	@Test
	void testWriteGenericArray() {
		/*
		 * This test can only work if the writeChunkNull and writeNewReference
		 * tests have been successful as well!
		 */
		Object[] arr = {null, new Object(), null};
		byte[] result = bytifier.encode(arr);
		
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(2);// number of references. The array and the object inside the array.
		// chunk for generic array
		encoder.writeChunkType(ChunkType.GENERIC_ARRAY);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt1(1);// dimension of array (1 dimensional)
		encoder.writeInt3(arr.length);// array length
		
		// chunk for first null reference
		encoder.writeChunkType(ChunkType.NULL);
		
		// chunk for the object
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		
		// chunk for second null reference
		encoder.writeChunkType(ChunkType.NULL);
		
		byte[] expected = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, result);
	}
	
	@Test
	void testWriteOldReference() {
		/*
		 * This test can only work if the writeGenericArray test has been successful!
		 */
		EmptyClass ec1 = new EmptyClass();
		EmptyClass ec2 = new EmptyClass();
		Object[] arr = {ec1, ec2, ec1, ec2, ec1};
		byte[] result = bytifier.encode(arr);
		/*
		 * Reference Table:
		 * Index		Object
		 * 0			Object[] arr
		 * 1			EmptyClass ec1
		 * 2			EmptyClass ec2
		 */
		
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(3);// number of references. The array and two instances of EmptyClass.
		// chunk for generic array
		encoder.writeChunkType(ChunkType.GENERIC_ARRAY);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt1(1);// dimension of array (1 dimensional)
		encoder.writeInt3(arr.length);// array length
		
		// chunk for first EmptyClass instance
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		
		// chunk for second EmptyClass instance
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		
		// chunk for reference to first EmptyClassInstance
		encoder.writeChunkType(ChunkType.READ_OBJ_REF);
		encoder.writeClassIndex(1);// see table
		
		// chunk for reference to second EmptyClassInstance
		encoder.writeChunkType(ChunkType.READ_OBJ_REF);
		encoder.writeClassIndex(2);// see table
		
		// chunk for reference to first EmptyClassInstance
		encoder.writeChunkType(ChunkType.READ_OBJ_REF);
		encoder.writeClassIndex(1);// see table
		
		byte[] expected = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, result);
	}
	
	@Test
	void testWriteValueType() {
		/*
		 * This test can only work if the writeGenericArray test has been successful!
		 */
		ValueTypeArrayWrapper wrapper = new ValueTypeArrayWrapper();
		EmptyClass ec1 = new EmptyClass();
		EmptyClass ec2 = new EmptyClass();
		wrapper.arr = new Object[] {ec1, ec2, ec1, ec2, ec1};
		byte[] result = bytifier.encode(wrapper);
		
		int magicNum = bytifier.getMagicNumber();
		encoder.writeInt4(magicNum);//magic number
		encoder.writeInt1(1);// number of bytes to encode class index
		encoder.writeInt4(1);// number of references. Only the instance of ValueTypeArrayWrapper.
		// The array and the two instances of EmptyClass should not get references.
		// chunk for ValueTypeArrayWrapper instance
		encoder.writeChunkType(ChunkType.NEW_OBJ_REF);
		encoder.writeClassIndex(encoder.getProtocolIndexFor(ValueTypeArrayWrapper.class));
		// index for Object array within wrapper
		encoder.writeClassIndex(encoder.getProtocolIndexFor(Object.class));
		encoder.writeInt3(wrapper.arr.length);// array length
		
		// chunks for EmptyClass instances/references
		for (int i = 0; i < wrapper.arr.length; i++) {
			encoder.writeChunkType(ChunkType.VALUE_OBJ_TYPE);
			encoder.writeClassIndex(encoder.getProtocolIndexFor(EmptyClass.class));
		}
		
		byte[] expected = encoder.getBytes(false);
		Assertions.assertArrayEquals(expected, result);
	}
	
}
