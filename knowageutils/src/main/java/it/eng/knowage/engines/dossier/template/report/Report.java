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
package it.eng.knowage.engines.dossier.template.report;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;

public class Report {
	String label;
	List<PlaceHolder> placeholders;
	List<Parameter> parameters;
	String imageName;
	String sheet;
	String sheetHeight;
	String sheetWidth;
	String deviceScaleFactor;

	public Report() {
		placeholders = new ArrayList<>();
		parameters = new ArrayList<>();
	}

	public List<PlaceHolder> getPlaceholders() {
		return placeholders;
	}

	@JsonSetter("PLACEHOLDER")
	public void setPlaceholders(List<PlaceHolder> placeholders) {
		this.placeholders = placeholders;
	}

	public String getLabel() {
		return label;
	}

	@JsonSetter("label")
	public void setLabel(String label) {
		this.label = label;
	}

	public String getImageName() {
		return imageName;
	}

	@JsonSetter("imageName")
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getSheet() {
		return sheet;
	}

	@JsonSetter("sheet")
	public void setSheet(String sheet) {
		this.sheet = sheet;
	}

	public String getSheetHeight() {
		return sheetHeight;
	}

	@JsonSetter("sheetHeight")
	public void setSheetHeight(String sheetHeight) {
		this.sheetHeight = sheetHeight;
	}

	public String getSheetWidth() {
		return sheetWidth;
	}

	@JsonSetter("sheetWidth")
	public void setSheetWidth(String sheetWidth) {
		this.sheetWidth = sheetWidth;
	}

	public String getDeviceScaleFactor() {
		return deviceScaleFactor;
	}

	@JsonSetter("deviceScaleFactor")
	public void setDeviceScaleFactor(String deviceScaleFactor) {
		this.deviceScaleFactor = deviceScaleFactor;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	@JsonSetter("PARAMETER")
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	@JsonIgnore
	public List<Parameter> getDinamicParams() {
		List<Parameter> dinamicParams = new ArrayList<>();

		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			if (parameter.getType().equals("dynamic")) {
				dinamicParams.add(parameter);
			}
		}
		return dinamicParams;
	}

}
