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
package it.eng.knowage.engines.svgviewer.map.utils;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Class SVGMapHandler.
 *
 * @author Andrea Gioia
 */
public class SVGMapHandler {

	/**
	 * Adds the attributes.
	 *
	 * @param e
	 *            the e
	 * @param attributes
	 *            the attributes
	 */
	public static void addAttributes(Element e, Map attributes) {
		Iterator it = attributes.keySet().iterator();
		while (it.hasNext()) {
			String attributeName = (String) it.next();
			String attributeValue = (String) attributes.get(attributeName);
			e.setAttribute("attrib:" + attributeName, attributeValue);
		}
	}
}
