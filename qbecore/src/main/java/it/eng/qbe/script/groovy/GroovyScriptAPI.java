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
package it.eng.qbe.script.groovy;

import org.apache.commons.text.StringEscapeUtils;

import it.eng.spagobi.utilities.assertion.Assert;


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
		result.append(", 'label' : '" + StringEscapeUtils.escapeEcmaScript(label) + "'");
		result.append(", 'parameters' : '" + StringEscapeUtils.escapeEcmaScript(parameters) + "'");
		if(subobject != null) result.append(", 'subobject' : '" + StringEscapeUtils.escapeEcmaScript(subobject) + "'");
		if(newTitle != null) result.append(", 'title' : '" + StringEscapeUtils.escapeEcmaScript(newTitle) + "'");
		if(target != null) result.append(", 'target' : '" + StringEscapeUtils.escapeEcmaScript(target) + "'");
		result.append("}, 'crossnavigation');\"");
		result.append(">" + StringEscapeUtils.escapeHtml4(text) + "</a>");
		return result.toString();
	}
	
	public String getImage(String imageUrl) {
		StringBuffer result = new StringBuffer("");
		result.append("<img src='" + StringEscapeUtils.escapeHtml4(imageUrl) + "'></img>");
		return result.toString();
	}
	
	public String getLink(String url, String text) {
		StringBuffer result = new StringBuffer("");
		result.append("<a href='" + StringEscapeUtils.escapeHtml4(url) + "'>" +StringEscapeUtils.escapeHtml4(text)  + "</a>");
		return result.toString();
	}
	
}
