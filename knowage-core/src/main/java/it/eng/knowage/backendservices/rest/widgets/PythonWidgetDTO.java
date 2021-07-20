/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.backendservices.rest.widgets;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;

public class PythonWidgetDTO {

	private String environmentLabel;
	private String datasetLabel;
	private String outputVariable;
	private String script;
	private Map<String, Object> drivers;
	private String parameters;
	private String aggregations;
	private String selections;
	private String documentId;
	private String widgetId;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@ExtendedAlphanumeric
	public String getEnvironmentLabel() {
		return environmentLabel;
	}

	public void setEnvironmentLabel(String environmentLabel) {
		this.environmentLabel = environmentLabel;
	}

	@ExtendedAlphanumeric
	public String getDatasetLabel() {
		return datasetLabel;
	}

	public void setDatasetLabel(String datasetLabel) {
		this.datasetLabel = datasetLabel;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@ExtendedAlphanumeric
	public String getOutputVariable() {
		return outputVariable;
	}

	public void setOutputVariable(String outputVariable) {
		this.outputVariable = outputVariable;
	}

	@Xss
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Map<String, Object> getDrivers() {
		return drivers;
	}

	public void setDrivers(Map<String, Object> drivers) {
		this.drivers = drivers;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getAggregations() {
		return aggregations;
	}

	public void setAggregations(String aggregations) {
		this.aggregations = aggregations;
	}

	public String getSelections() {
		return selections;
	}

	public void setSelections(String selections) {
		this.selections = selections;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	@ExtendedAlphanumeric
	public String getWidgetId() {
		return widgetId;
	}

	@ExtendedAlphanumeric
	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

}
