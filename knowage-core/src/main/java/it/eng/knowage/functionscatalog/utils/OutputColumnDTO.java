/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.functionscatalog.utils;

import com.google.gson.Gson;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

public class OutputColumnDTO {

	private String name;
	private String fieldType;
	private String type;

	@ExtendedAlphanumeric
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ExtendedAlphanumeric
	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	@ExtendedAlphanumeric
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static OutputColumnDTO valueOf(String json) {
		return new Gson().fromJson(json, OutputColumnDTO.class);
	}
}
