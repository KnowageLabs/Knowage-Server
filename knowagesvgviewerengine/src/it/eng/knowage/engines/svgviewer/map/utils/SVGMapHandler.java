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
