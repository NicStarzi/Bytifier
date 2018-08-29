package edu.udo.bytifier.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.Bytifier.ProtocolTuple;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;
import edu.udo.bytifier.debug.DebugBytifier;
import edu.udo.bytifier.protocols.ObjectProtocol;

class PayloadTest {
	
	Bytifier bytifier;
	EncodeData encoder;
	int writtenLinkedClassCount;
	
	@BeforeEach
	void createBytifier() {
		ProtocolTuple tupObj = new ProtocolTuple(Object.class, ObjectProtocol.INSTANCE);
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
			public int getIdentificationNumber() {return 1;}
		});
		ProtocolTuple tupLinked = new ProtocolTuple(LinkedClass.class, new ClassProtocol() {
			@Override
			public void write(Bytifier bytifier, EncodeData data, Object input) {
				writtenLinkedClassCount++;
				LinkedClass lc = (LinkedClass) input;
				bytifier.writeChunk(data, lc.next, true);
			}
			@Override
			public void read(Bytifier bytifier, DecodeData data, Object object) {
				LinkedClass lc = (LinkedClass) object;
				LinkedClass link = (LinkedClass) bytifier.readChunk(data);
				lc.next = link;
			}
			@Override
			public Object create(Bytifier bytifier, DecodeData data) {
				return new LinkedClass();
			}
			@Override
			public int getIdentificationNumber() {return 2;}
		});
		
		bytifier = new Bytifier(tupObj, tupEmpty, tupLinked);
		encoder = new EncodeData(bytifier, false);
	}
	
	@Test
	void testCyclicGenericArray() {
		Object[] arr = new Object[3];
		arr[0] = new EmptyClass();
		arr[1] = null;
		arr[2] = arr;
		
		byte[] encoded = bytifier.encode(arr);
//		Object decoded = bytifier.decode(encoded);
		Object decoded = new DebugBytifier(bytifier).decode(encoded);
		
		Assertions.assertNotNull(decoded);
		Assertions.assertEquals(arr.getClass(), decoded.getClass());
		
		Object[] result = (Object[]) decoded;
		Assertions.assertNotNull(result[0]);
		Assertions.assertEquals(arr[0].getClass(), result[0].getClass());
		
		Assertions.assertNull(result[1]);
		
		Assertions.assertNotNull(result[2]);
		Assertions.assertEquals(arr[2].getClass(), result[2].getClass());
		
		Assertions.assertSame(result, result[2]);
	}
	
	@Test
	void testStackDepth() {
		LinkedClass lc = new LinkedClass();
		try {
			addLink(lc);
		} catch (StackOverflowError e) {
			int stackDepth = 0;
			LinkedClass cur = lc;
			while (cur != null) {
				stackDepth++;
				cur = cur.next;
			}
			System.out.println("Stack Depth: "+stackDepth);
		}
		
		try {
			bytifier.encode(lc);
		} catch (StackOverflowError e) {
			System.out.println("number of LinkedClass objects written: "+writtenLinkedClassCount);
		}
	}
	
	private void addLink(LinkedClass lc) {
		lc.next = new LinkedClass();
		addLink(lc.next);
	}
}