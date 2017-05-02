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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;

/**
 * The Class SVGMapMerger.
 *
 */
public class SVGMapMerger {

	/**
	 * Merge map.
	 *
	 * @param srcMap
	 *            the src map
	 * @param dstMap
	 *            the dst map
	 * @param srcId
	 *            the src id
	 * @param dstId
	 *            the dst id
	 */
	public static void mergeMap(SVGDocument srcMap, SVGDocument dstMap, String srcId, String dstId) {
		SVGElement srcMapRoot;
		Element srcElement;
		Element dstElement;

		srcMapRoot = srcMap.getRootElement();
		srcElement = (srcId == null ? srcMapRoot : srcMap.getElementById(srcId));

		dstElement = dstMap.getElementById(dstId);

		NodeList nodeList = srcElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Node importedNode = dstMap.importNode(node, true);
			dstElement.appendChild(importedNode);
		}
	}
}
