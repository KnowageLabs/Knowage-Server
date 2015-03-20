/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
package it.eng.qbe.script.groovy;

import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.commons.lang.StringEscapeUtils;

public class GroovyScriptAPI {

	public String getCrossNavigationLink(String... args) {
		Assert.assertNotNull(args, "Missing input arguments");
		Assert.assertTrue(args.length >= 2, "Input arguments aren't enough: you have to specify the cross navigation text and target document's label at least.");
		String text = args[0];
		String label = args[1];
		String parameters = args.length >= 3 ? args[2] : "";
		String subobject = args.length >= 4 ? args[3] : null;
		String newTitle = args.length >= 5 ? args[4] : null;
		String target = args.length >= 6 ? args[5] : null;
		StringBuffer result = new StringBuffer("");
		result.append("<a href='#' onclick=\"javascript:sendMessage({");
		result.append("'windowName' : this.name");
		result.append(", 'label' : '" + StringEscapeUtils.escapeJavaScript(label) + "'");
		result.append(", 'parameters' : '" + StringEscapeUtils.escapeJavaScript(parameters) + "'");
		if(subobject != null) result.append(", 'subobject' : '" + StringEscapeUtils.escapeJavaScript(subobject) + "'");
		if(newTitle != null) result.append(", 'title' : '" + StringEscapeUtils.escapeJavaScript(newTitle) + "'");
		if(target != null) result.append(", 'target' : '" + StringEscapeUtils.escapeJavaScript(target) + "'");
		result.append("}, 'crossnavigation');\"");
		result.append(">" + StringEscapeUtils.escapeHtml(text) + "</a>");
		return result.toString();
	}
	
	public String getImage(String imageUrl) {
		StringBuffer result = new StringBuffer("");
		result.append("<img src='" + StringEscapeUtils.escapeHtml(imageUrl) + "'></img>");
		return result.toString();
	}
	
	public String getLink(String url, String text) {
		StringBuffer result = new StringBuffer("");
		result.append("<a href='" + StringEscapeUtils.escapeHtml(url) + "'>" +StringEscapeUtils.escapeHtml(text)  + "</a>");
		return result.toString();
	}
	
}
