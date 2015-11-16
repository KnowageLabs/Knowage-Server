/*
CKANClient-J - Data Catalogue Software client in Java
Copyright (C) 2013 Newcastle University
Copyright (C) 2012 Open Knowledge Foundation

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.ckan.resource.impl;

import it.eng.spagobi.tools.dataset.ckan.resource.CKANResource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an extra metadata field in a dataset or group
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>, Ross Jones <ross.jones@okfn.org>
 * @version 1.8
 * @since 2013-22-02
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Extra extends CKANResource {
	private String key;
	private String value;

	public Extra() {
	}

	public Extra(String k, String v) {
		key = k;
		value = v;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String k) {
		key = k;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String v) {
		value = v;
	}

	@Override
	public String toString() {
		return "<Extra:" + this.getKey() + "=" + this.getValue() + ">";
	}
}