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

package it.eng.spagobi.services.security.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class PythonWidget {

	@JsonProperty("user")
	private String userId;
	@JsonProperty("document")
	private String documentId;
	@JsonProperty("widget")
	private String widgetId;
	@JsonProperty("script")
	private String script;

	public PythonWidget() {

	}

	public String getUserId() {
		return userId;
	}

	@JsonSetter("user")
	public void setuserId(String userId) {
		this.userId = userId;
	}

	public String getDocumentId() {
		return documentId;
	}

	@JsonSetter("document")
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getWidgetId() {
		return widgetId;
	}

	@JsonSetter("widget")
	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getScript() {
		return script;
	}

	@JsonSetter("script")
	public void setScript(String script) {
		this.script = script;
	}

}
