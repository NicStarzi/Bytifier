# Bytifier
A java serialization library.

To serialize java objects an instance of edu.udo.bytifier.Bytifier is needed. It is recommended to construct a Bytifier instance via the edu.udo.bytifier.ProtocolBuilder. Use the factory methods of the ProtocolBuilder to define a Bytifier protocol.

The following java code is an example of how to use the ProtocolBuilder to construct a Bytifier instance:

```java
Bytifier theBytifier = new ProtocolBuilder()
			.defineForClass(Contact.class)
				.addFieldString(Contact::getFirstName, Contact::setFirstName)
				.addFieldString(Contact::getLastName, Contact::setLastName)
				.addFieldString(Contact::getAdress, Contact::setAdress)
				.addFieldString(Contact::getHomeNumber, Contact::setHomeNumber)
				.addFieldString(Contact::getMobileNumber, Contact::setMobileNumber)
				.endDefinition()
			.build();
```

The constructed Bytifier can now be used to efficiently serialize and deserialize instances of Contact and arrays of Contact. The following code shows how to serialize an array of contacts to a byte-array and to deserialize the byte-array back to the original array of contacts.

```java
Contact[] contacts = {
	new Contact("Bart", "Simpson", "1234 Fake Street", "", ""),
	//...
};

byte[] serializedContacts = theBytifier.encode(contacts);

Contact[] deserializedContacts = (Contact[]) theBytifier.decode(serializedContacts);
```

The Bytifier can serialize arbitrary object graphs of arbitrary object types. Those types that have been defined via the ProtocolBuilder will be serialized more efficiently than those that are unknown to the Bytifier. The Bytifier can properly serialize recursive object graphs, null references and enum literals. 