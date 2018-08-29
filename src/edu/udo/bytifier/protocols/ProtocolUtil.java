package edu.udo.bytifier.protocols;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

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
	
	public static void iterateNestedArrayBreadthFirst(Object arr, Consumer<Object> action) {
		ProtocolUtil.iterateNestedArrayBreadthFirst(arr, action, true);
	}
	
	public static void iterateNestedArrayBreadthFirst(Object arr, Consumer<Object> action, boolean includeArrays) {
		if (!arr.getClass().isArray()) {
			throw new IllegalArgumentException("arr.getClass() == "+arr.getClass());
		}
		
		Deque<Object> stack = new ArrayDeque<>();
		stack.add(arr);
		if (includeArrays) {
			action.accept(arr);
		}
		
		while (stack.size() > 0) {
			Object current = stack.removeFirst();
//			System.out.println("current="+current);
			int len = Array.getLength(current);
//			System.out.println("len="+len);
			for (int i = 0; i < len; i++) {
				Object elem = Array.get(current, i);
				if (elem != null && elem.getClass().isArray()) {
//					System.out.println("elem="+Arrays.toString((Object[])elem));
					stack.addLast(elem);
					if (includeArrays) {
						action.accept(elem);
					}
				} else {
//					System.out.println("elem="+elem.getClass().getSimpleName());
					action.accept(elem);
				}
			}
		}
	}
	
}