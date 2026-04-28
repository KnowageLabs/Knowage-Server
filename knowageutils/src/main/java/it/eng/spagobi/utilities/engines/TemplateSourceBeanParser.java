/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.utilities.engines;

import java.io.StringReader;

import org.xml.sax.InputSource;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

public final class TemplateSourceBeanParser {

	private TemplateSourceBeanParser() {
	}

	public static SourceBean parse(String templateContent) throws SourceBeanException {
		String sanitizedTemplateContent = sanitizeTemplateContent(templateContent);
		if (sanitizedTemplateContent == null) {
			return null;
		}
		if (sanitizedTemplateContent.isEmpty()) {
			throw new SourceBeanException("xmlSourceBean non valido");
		}
		return SourceBean.fromXMLStream(new InputSource(new StringReader(sanitizedTemplateContent)));
	}

	private static String sanitizeTemplateContent(String templateContent) {
		String sanitizedTemplateContent = removeUtf8Bom(templateContent);
		return sanitizedTemplateContent != null ? sanitizedTemplateContent.trim() : null;
	}

	private static String removeUtf8Bom(String templateContent) {
		if (templateContent != null && !templateContent.isEmpty() && templateContent.charAt(0) == '\uFEFF') {
			return templateContent.substring(1);
		}
		return templateContent;
	}
}
