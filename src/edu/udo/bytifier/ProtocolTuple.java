package edu.udo.bytifier;

/**<p>
 * A simple container class connecting {@link ClassProtocol ClassProtocol's} and
 * one of their supported {@link Class classes}. Instances of {@link ProtocolTuple}
 * are used by a {@link Bytifier} to define a serialization protocol.
 * 
 * @see Bytifier#Bytifier(java.util.Collection)
 * @see Bytifier#Bytifier(ProtocolTuple...)
 * 
 * @author Nic Starzi
 */
public class ProtocolTuple {
	
	/** The class that is supported by the {@link #proto protocol} */
	public final Class<?> cls;
	/** A protocol that is compatible with the given {@link #cls class} */
	public final ClassProtocol proto;
	
	/**<p>
	 * Constructs a new ProtocolTuple for the given class and protocol. This
	 * constructor does not check whether the protocol and class are compatible.
	 * <p>
	 * {@code Null} arguments are not allowed and will result in an
	 * {@link IllegalArgumentException} being thrown.
	 * 
	 * @param protocolClass		a class that is supported by {@code protocol}
	 * @param protocol			a protocol that is compatible with {@code protocolClass}
	 */
	public ProtocolTuple(Class<?> protocolClass, ClassProtocol protocol) {
		if (protocolClass == null) {
			throw new IllegalArgumentException("protocolClass == null");
		}
		if (protocol == null) {
			throw new IllegalArgumentException("protocol == null");
		}
		cls = protocolClass;
		proto = protocol;
	}
	
	/**<p>
	 * The class that is supported by the {@link #getProtocol() protcol}.
	 * <p>
	 * Always returns the class that was initially passed to the constructor
	 * of this object. The value never changes over the life-time of this
	 * object.
	 * 
	 * @return	the protocol that is compatible with the target class
	 */
	public Class<?> getTargetClass() {
		return cls;
	}
	
	/**<p>
	 * The {@link ClassProtocol} that is compatible to the
	 * {@link #getTargetClass() targeted class}.
	 * <p>
	 * Always returns the protocol that was initially passed to the constructor
	 * of this object. The value never changes over the life-time of this
	 * object.
	 * 
	 * @return	the class that is supported by the protocol
	 */
	public ClassProtocol getProtocol() {
		return proto;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(cls.getName());
		sb.append("-[");
		sb.append(proto);
		sb.append("]-{");
		sb.append(proto.getIdentificationNumber());
		sb.append("}");
		return sb.toString();
	}
}