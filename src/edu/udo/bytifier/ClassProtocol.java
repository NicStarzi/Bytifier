package edu.udo.bytifier;

/**<p>
 * A ClassProtocol defines how a class is supposed to be serialized and
 * deserialized. Each class protocol should only be used for a single class.
 * <p>
 * To identify a class protocol across clients a 32-bit
 * {@link #getIdentificationNumber() identification number} is used. This
 * number should be considered a constant for any instance of CLassProtocol.
 * For ClassProtocols with parameterized behavior the identification number
 * should change with the configuration.
 * 
 * @author Nic Starzi
 */
public interface ClassProtocol {
	
	/**<p>
	 * Serializes {@code input} and writes the bytes into {@link EncodeData data}.
	 * <p>
	 * The {@link ChunkType} and other overhead data must not be written by this
	 * method. Only the actual data of the input is written here.
	 * <p>
	 * The {@link Bytifier#writeChunk(EncodeData, Object, boolean)} method can be
	 * used to write data of referenced objects to {@link EncodeData data}.
	 * 
	 * @param bytifier	the instance of {@link Bytifier} doing the serialization
	 * @param data		a buffer where the byte data is written to
	 * @param input		the object to be serialized
	 * @see Bytifier#writeChunk(EncodeData, Object, boolean)
	 */
	public void write(Bytifier bytifier, EncodeData data, Object input);
	
	/**<p>
	 * Reads as many bytes as needed from {@link DecodeData data} to instantiate
	 * an object and return it. The returned object is of a type that is compatible
	 * with the {@link #read(Bytifier, DecodeData, Object)} and
	 * {@link #write(Bytifier, EncodeData, Object)} methods.
	 * <p>
	 * A cyclic reference can not be deserialized by only using the
	 * {@link #create(Bytifier, DecodeData)} methods of one or more Class Protocols.
	 * In order to correctly deserialize cyclic references, the create method should
	 * only do minimal work necessary to instantiate an object and any further state
	 * should be set by the {@link #read(Bytifier, DecodeData, Object)} method.
	 * <p>
	 * The {@link #read(Bytifier, DecodeData, Object)} method will be called after
	 * this method has returned with the returned object reference being passed as
	 * the third argument to the read-method.
	 * 
	 * @param bytifier	the instance of {@link Bytifier} doing the deserialization
	 * @param data		a buffer where byte data can be read from
	 * @return			a reference to a newly constructed object compatible with the read- and write-methods
	 * @see #read(Bytifier, DecodeData, Object)
	 */
	public Object create(Bytifier bytifier, DecodeData data);
	
	/**<p>
	 * Reads bytes from {@link DecodeData data} and sets the state of {@code object}.
	 * <p>
	 * This method is called after the {@link #create(Bytifier, DecodeData)} method
	 * has been used to instantiate the object that is being deserialized. At the time
	 * this method is called the object has been correctly indexed by the given
	 * {@link Bytifier} and cyclic reference graphs can correctly be deserialized.
	 * <p>
	 * The {@link Bytifier#readChunk(DecodeData)} method can be used to delegate
	 * deserialization of referenced objects to the default deserialization process.
	 * 
	 * @param bytifier	the instance of {@link Bytifier} doing the deserialization
	 * @param data		a buffer where byte data can be read from
	 * @param object	the deserialized object; previously instantiated by the {@link #create(Bytifier, DecodeData)} method
	 * @see #create(Bytifier, DecodeData)
	 * @see Bytifier#readChunk(DecodeData)
	 */
	public void read(Bytifier bytifier, DecodeData data, Object object);
	
	/**<p>
	 * A constant number used to identify this protocol across instances. This number is
	 * part of the calculation used to determine the
	 * {@link Bytifier#getProtocolIdentificationNumber() protocol identification number}.
	 * <p>
	 * To perform its purpose of detecting mismatches within a protocol, this number should
	 * be chosen with great care. A hash code over identifying features of this ClassProtocol
	 * is a recommended implementation for this method.
	 * 
	 * @return		used as part of the {@link Bytifier#getProtocolIdentificationNumber()} calculation
	 */
	public default int getIdentificationNumber() {
		return getClass().getName().hashCode();
	}
	
}