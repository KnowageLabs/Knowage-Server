package it.eng.spagobi.analiticalmodel.document.util;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LikeExpression;
import org.hibernate.criterion.MatchMode;

public class EscapedLikeRestrictions {
	private EscapedLikeRestrictions() {}

	public static Criterion likeEscaped(String propertyName, String value, MatchMode matchMode) {
		return likeEscaped(propertyName, value, matchMode, false);
	}

	public static Criterion ilikeEscaped(String propertyName, String value, MatchMode matchMode) {
		return likeEscaped(propertyName, value, matchMode, true);
	}

	private static Criterion likeEscaped(String propertyName, String value, MatchMode matchMode, boolean ignoreCase) {
		return new LikeExpression(propertyName, escape(value), matchMode, '!', ignoreCase) {/*a trick to call protected constructor*/};
	}

	private static String escape(String value) {
		return value
				.replace("!", "!!")
				.replace("%", "!%")
				.replace("_", "!_");
	}
}
