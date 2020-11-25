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
package it.eng.knowage.engines.dossier.template.parameter;

import com.fasterxml.jackson.annotation.JsonSetter;

public class Parameter {

	private String type;
	private String urlName;
	private String dossierUrlName;
	private String value;
	private String urlNameDescription;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrlName() {
		return urlName;
	}

	@JsonSetter("url_name")
	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}

	public String getDossierUrlName() {
		return dossierUrlName;
	}

	@JsonSetter("dossier_url_name")
	public void setDossierUrlName(String dossierUrlName) {
		this.dossierUrlName = dossierUrlName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {

		return "type:" + this.type + " ,urlName:" + this.urlName + " ,dossierUrlName:" + this.dossierUrlName;
	}

	public String getUrlNameDescription() {
		return urlNameDescription;
	}

	@JsonSetter("url_name_description")
	public void setUrlNameDescription(String urlNameDescription) {
		this.urlNameDescription = urlNameDescription;
	}

}
