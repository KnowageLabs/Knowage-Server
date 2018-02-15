/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.knowage.engine.cockpit.api.export.excel;

public class ExcelSheet {
	private String label;
	private String csv;

	public ExcelSheet(String label, String csv) {
		setLabel(label);
		this.csv = csv;
	}

	private void setLabel(String label) {
		String labelWithoutQuotes = label.trim().replaceAll("\"", "").replaceAll("'", "");
		if (labelWithoutQuotes.length() > 31) {
			labelWithoutQuotes = labelWithoutQuotes.substring(0, 31);
		}
		this.label = labelWithoutQuotes;
	}

	public String getLabel() {
		return label;
	}

	public String getCsv() {
		return csv;
	}

}
