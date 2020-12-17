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
package it.eng.spagobi.analiticalmodel.execution.service.v2.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomDataDTO {

	@JsonProperty("templateContent")
	private Map<String, Object> templateContent;
//	@JsonProperty("smartFilter")
//	private SmartFilterDTO smartFilter;
//	@JsonProperty("query")
//	private String query;
	@JsonProperty("modelName")
	private String modelName;

	public CustomDataDTO() {

	}

	public CustomDataDTO(Map<String, Object> templateContent, String modelName) {
		this.templateContent = templateContent;
//		this.smartFilter = smartFilter;
//		this.query = query;
		this.modelName = modelName;
	}

	public Map<String, Object> getTemplateContent() {
		return templateContent;
	}

	public void setTemplateContent(Map<String, Object> templateContent) {
		this.templateContent = templateContent;
	}

//	public SmartFilterDTO getSmartFilterPostObject() {
//		return smartFilter;
//	}
//
//	public void setSmartFilterPostObject(SmartFilterDTO smartFilterPostObject) {
//		this.smartFilter = smartFilterPostObject;
//	}
//
//	public String getQuery() {
//		return query;
//	}
//
//	public void setQuery(String query) {
//		this.query = query;
//	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

}
