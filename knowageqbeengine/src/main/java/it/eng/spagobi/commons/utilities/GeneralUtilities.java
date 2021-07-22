/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.commons.utilities;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;

public class GeneralUtilities {
	public static String getScriptFromLocale(SessionContainer permSess) {
		String toReturn = "";
		String script = (String) permSess.getAttribute(SpagoBIConstants.AF_SCRIPT);

		if (StringUtils.isNotBlank(script)) {
			toReturn = script + "-";
		}

		return toReturn;

	}

	public static String getAngularPropertiesFileName(Locale locale, String separator) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String script = locale.getScript();

		StringBuilder sb = new StringBuilder();
		sb.append(language);
		sb.append(separator);
		if (StringUtils.isNotBlank(script)) {
			sb.append(script);
			sb.append(separator);
		}
		sb.append(country);

		return sb.toString();
	}
}
