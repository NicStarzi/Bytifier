package edu.udo.bytifier.protocols;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ClassProtocol;
import edu.udo.bytifier.DecodeData;
import edu.udo.bytifier.EncodeData;

public class StringProtocol implements ClassProtocol {
	
	public static final StringProtocol STRING_UTF8_PROTOCOL = new StringProtocol(StandardCharsets.UTF_8);
	
	protected final Charset charset;
	
	public StringProtocol(Charset charset) {
		this.charset = charset;
	}
	
	@Override
	public void write(Bytifier bytifier, EncodeData data, Object input) {
		String str = (String) input;
		int length = str.length();
		data.writeInt3(length);
		
		byte[] bytes = str.getBytes(charset);
		data.writeBytes(bytes);
	}
	
	@Override
	public Object create(Bytifier bytifier, DecodeData data) {
		int length = data.readInt3();
		byte[] bytes = new byte[length];
		data.readBytes(bytes);
		return new String(bytes, charset);
	}
	
	@Override
	public void read(Bytifier bytifier, DecodeData data, Object object) {}
	
	@Override
	public int getIdentificationNumber() {
		return getClass().getName().hashCode() + 31 * charset.name().hashCode();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}