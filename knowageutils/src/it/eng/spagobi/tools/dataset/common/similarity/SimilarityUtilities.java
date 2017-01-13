package it.eng.spagobi.tools.dataset.common.similarity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SimilarityUtilities {

	private final static int DEFAULT_DECIMALS = 4;

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static double round(double value) {
		return round(value, DEFAULT_DECIMALS);
	}

}
