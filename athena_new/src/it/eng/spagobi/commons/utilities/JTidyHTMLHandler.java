/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import java.io.InputStream;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

public class JTidyHTMLHandler {

	public String getContent(InputStream is)
			throws Exception {
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		org.w3c.dom.Document root = tidy.parseDOM(is, null);
		Element rawDoc = root.getDocumentElement();
		String title = getTitle(rawDoc);
		String body = getBody(rawDoc);

		if ((body != null) && (!body.equals(""))) {
			return body;
		}
		return null;
	}

	/**
	 * Gets the title text of the HTML document.
	 * 
	 * @rawDoc the DOM Element to extract title Node from
	 * @return the title text
	 */
	protected String getTitle(Element rawDoc) {
		if (rawDoc == null) {
			return null;
		}

		String title = "";

		NodeList children = rawDoc.getElementsByTagName("title");
		if (children.getLength() > 0) {
			Element titleElement = ((Element) children.item(0));
			Text text = (Text) titleElement.getFirstChild();
			if (text != null) {
				title = text.getData();
			}
		}
		return title;
	}

	/**
	 * Gets the body text of the HTML document.
	 * 
	 * @rawDoc the DOM Element to extract body Node from
	 * @return the body text
	 */
	protected String getBody(Element rawDoc) {
		if (rawDoc == null) {
			return null;
		}

		String body = "";
		NodeList children = rawDoc.getElementsByTagName("body");
		if (children.getLength() > 0) {
			body = getText(children.item(0));
		}
		return body;
	}

	/**
	 * Extracts text from the DOM node.
	 * 
	 * @param node
	 *            a DOM node
	 * @return the text value of the node
	 */
	protected String getText(Node node) {
		NodeList children = node.getChildNodes();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			switch (child.getNodeType()) {
			case Node.ELEMENT_NODE:
				sb.append(getText(child));
				sb.append(" ");
				break;
			case Node.TEXT_NODE:
				sb.append(((Text) child).getData());
				break;
			}
		}
		return sb.toString();
	}

}
