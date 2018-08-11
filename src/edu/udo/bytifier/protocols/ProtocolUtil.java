package edu.udo.bytifier.protocols;

public class ProtocolUtil {
	
	public static Class<?> getArrayElementType(Object arr) {
		Class<?> arrCls = arr.getClass();
		Class<?> elemCls = arrCls.getComponentType();
		if (elemCls == null) {
			return null;
		}
		while (elemCls != null) {
			arrCls = elemCls;
			elemCls = arrCls.getComponentType();
		}
		return arrCls;
	}
	
	public static int getArrayDimension(Object arr) {
		Class<?> arrCls = arr.getClass().getComponentType();
		int count = 0;
		while (arrCls != null) {
			count++;
			arrCls = arrCls.getComponentType();
		}
		return count;
	}
	
}