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
package it.eng.spagobi.engines.whatif.calculatedmember;

public class Argument {

	private String expected_value;
	private String argument_description;

	public String getExpected_value() {
		return expected_value;
	}

	public void setExpected_value(String expected_value) {
		this.expected_value = expected_value;
	}

	public String getArgument_description() {
		return argument_description.trim();
	}

	public void setArgument_description(String argument_description) {
		this.argument_description = argument_description;
	}

}
