package edu.udo.bytifier.tests;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.EncodeData;
import edu.udo.bytifier.ProtocolBuilder;
import edu.udo.bytifier.ValueType;

public class PersonTest {
	
	public static class Person {
		@ValueType String name;
		int age;
		double salary;
		EmployeeType type;
		public Person() {
			this(null, 0);
		}
		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public int getAge() {
			return age;
		}
		public void setSalary(double salary) {
			this.salary = salary;
		}
		public double getSalary() {
			return salary;
		}
		public void setType(EmployeeType type) {
			this.type = type;
		}
		public EmployeeType getType() {
			return type;
		}
		@Override
		public String toString() {
			return name+"["+age+"]="+salary;
		}
	}
	public static enum EmployeeType {
		PERMANENT,
		TEMPORARY,
		;
	}
	public static class Box {
		int i = 42;
		double d = 4.2;
		boolean b = true;
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("i=");
			sb.append(i);
			sb.append("; d=");
			sb.append(d);
			sb.append("; b=");
			sb.append(b);
			return sb.toString();
		}
	}
	
	@Test
	void runTest() {
		ProtocolBuilder protoBuilder = new ProtocolBuilder();
		protoBuilder.setStringEncodingCharset(StandardCharsets.UTF_8);
		protoBuilder.defineEnum(EmployeeType.class);
		protoBuilder.defineViaReflection(Box.class);
		protoBuilder.defineForClass(Person.class)
			.addConstructorString(Person::getName)
			.addConstructorInt(Person::getAge)
//			.addFieldString(Person::getName, Person::setName)
//			.addFieldInt(Person::getAge, Person::setAge)
			.addFieldDouble(Person::getSalary, Person::setSalary)
			.addFieldReferencedObject(Person::getType, Person::setType)
			.useFieldHashing(false)
			;
		
		Bytifier bytifier = protoBuilder.build();
		
		EncodeData encoder = new EncodeData(bytifier, 256);
		byte[] bytes = PersonTest.writePersons(bytifier, encoder);
		
		System.out.println();
		System.out.println("byteCount="+bytes.length);
//		System.out.println("bufferCount="+(1 + (encoder.byteBufList == null ? 0 : encoder.byteBufList.size())));
		System.out.println("bytes="+Arrays.toString(bytes));
		System.out.println();
		
		PersonTest.readPersons(bytifier, bytes);
	}
	
	static byte[] writePersons(Bytifier bytifier, EncodeData data) {
		System.out.println("Test.writePersons()");
		Person alice = new Person("Alice", 21);
		alice.salary = 4.2;
		
		Person bob = new Person("Bob", 19);
		bob.salary = Math.PI;
		
		Box box = new Box();
		System.out.println("PRE:box="+box);
		box.i = 96;
		box.d = 9.6;
		box.b = false;
		System.out.println("POST:box="+box);
		
		Person[] persons = {alice, bob, null, alice};
		Object[] out = {persons, box, persons};
		return bytifier.encode(out);
	}
	
	static void readPersons(Bytifier bytifier, byte[] bytes) {
		System.out.println("Test.readPersons()");
		Object result = bytifier.decode(bytes);
		System.out.println("result="+result);
		
		Object[] arr = (Object[]) result;
		System.out.println("arr="+Arrays.toString(arr));
		
		Person[] persons = (Person[]) arr[0];
		System.out.println("persons="+Arrays.toString(persons));
		
		Box box = (Box) arr[1];
		System.out.println("box="+box);
	}
	
}