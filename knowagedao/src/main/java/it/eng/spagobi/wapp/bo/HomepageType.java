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
package it.eng.spagobi.wapp.bo;

import java.util.Arrays;

public enum HomepageType {

	DOCUMENT("document"),
	IMAGE("image"),
	STATIC("static"),
	DYNAMIC("dynamic");

	private final String value;

	HomepageType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static HomepageType fromValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Homepage type cannot be null");
		}
		return Arrays.stream(values())
				.filter(type -> type.value.equalsIgnoreCase(value.trim()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unsupported homepage type [" + value + "]"));
	}

}
