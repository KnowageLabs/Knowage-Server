package it.eng.knowage.api.dossier.utils;

import java.util.List;

/**
 * @deprecated An entire class for a string join?
 */
@Deprecated
public class StringUtilities {

	public static String join(List<String> list, String delim) {

		StringBuilder sb = new StringBuilder();

		String loopDelim = "";

		for (String s : list) {

			sb.append(loopDelim);
			sb.append(s);

			loopDelim = delim;
		}

		return sb.toString();
	}

	private StringUtilities() {

	}
}
