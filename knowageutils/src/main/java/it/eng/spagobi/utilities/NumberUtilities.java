package it.eng.spagobi.utilities;

import java.math.BigDecimal;

public final class NumberUtilities {

	public static boolean isNumber(Class clazz) {
		return Number.class.isAssignableFrom(clazz) || BigDecimal.class.isAssignableFrom(clazz);
	}

	public static boolean isFloatingPoint(Class clazz) {
		if (clazz == Double.class || clazz == Float.class || clazz == BigDecimal.class) {
			return true;
		}
		return false;
	}

}
