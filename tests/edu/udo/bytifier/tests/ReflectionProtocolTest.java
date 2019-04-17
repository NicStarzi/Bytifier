package edu.udo.bytifier.tests;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.EncodeData;
import edu.udo.bytifier.ProtocolBuilder;
import edu.udo.bytifier.ValueType;

public class ReflectionProtocolTest {
	
	public static class Complex {
		private Complex c1;
		private @ValueType Complex c2;
		private Simple s1, s2;
		public void setC1(Complex c) {
			c1 = c;
		}
		public void setC2(Complex c) {
			c2 = c;
		}
		public void setS1(Simple s1) {
			this.s1 = s1;
		}
		public void setS2(Simple s2) {
			this.s2 = s2;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Complex [identity=");
			builder.append(System.identityHashCode(this));
			builder.append(", c1=");
			builder.append(c1);
//			builder.append(c1 == null ? "null" : System.identityHashCode(c1));
			builder.append(", c2=");
			builder.append(c2 == null ? "null" : System.identityHashCode(c2));
			builder.append(", s1=");
			builder.append(s1);
			builder.append(", s2=");
			builder.append(s2);
			builder.append("]");
			return builder.toString();
		}
	}
	public static class Simple {
		private int x;
		private double y;
		private String z;
		public Simple() {
		}
		public Simple(int x, double y, String z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		public int getX() {
			return x;
		}
		public double getY() {
			return y;
		}
		public String getZ() {
			return z;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Simple [identity=");
			builder.append(System.identityHashCode(this));
			builder.append(", x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append(", z=");
			builder.append(z);
			builder.append("]");
			return builder.toString();
		}
	}
	
	@Test
	void runTest() {
		ProtocolBuilder protoBuilder = new ProtocolBuilder();
		protoBuilder.setStringEncodingCharset(StandardCharsets.UTF_8);
		protoBuilder.defineViaReflection(Simple.class);
		protoBuilder.defineViaReflection(Complex.class);
		
		Bytifier bytifier = protoBuilder.build();
		
		EncodeData encoder = new EncodeData(bytifier, 256);
		byte[] bytes = ReflectionProtocolTest.writeObjects(bytifier, encoder);
		
		System.out.println();
		System.out.println("byteCount="+bytes.length);
		System.out.println("bytes="+Arrays.toString(bytes));
		System.out.println();
		
		ReflectionProtocolTest.readObjects(bytifier, bytes);
	}
	
	static byte[] writeObjects(Bytifier bytifier, EncodeData data) {
		System.out.println("ReflectionProtocolTest.writeObjects()");
		
		Simple s1 = new Simple(42, 21.0, "84");
		Simple s2 = new Simple(1, 2.0, "3");
		Complex c1 = new Complex();
		c1.setS1(s1);
		c1.setS2(s2);
		Complex c2 = new Complex();
		c2.setC1(c1);
		c1.setC2(c2);
		
		Object[] out = {s1, s2, c1, c2};
		return bytifier.encode(out);
	}
	
	static void readObjects(Bytifier bytifier, byte[] bytes) {
		System.out.println("ReflectionProtocolTest.readObjects()");
		Object result = bytifier.decode(bytes);
		System.out.println("result="+result);
		
		Object[] arr = (Object[]) result;
		System.out.println("arr="+Arrays.toString(arr));
		
		System.out.println("s1="+(arr[0]));
		System.out.println("s2="+(arr[1]));
		System.out.println("c1="+(arr[2]));
		System.out.println("c2="+(arr[3]));
		System.out.println("cloneOf(c2)="+((Complex) arr[2]).c2);
	}
	
}