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
package it.eng.spagobi.tools.crossnavigation.bo;

public class SimpleNavigation {

	private Integer id;
	private String name;
	private String fromDoc;
	private String toDoc;

	public SimpleNavigation() {
	}

	public SimpleNavigation(Integer id, String name, String fromDoc, String toDoc) {
		this.id = id;
		this.name = name;
		this.fromDoc = fromDoc;
		this.toDoc = toDoc;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the fromDoc
	 */
	public String getFromDoc() {
		return fromDoc;
	}

	/**
	 * @param fromDoc
	 *            the fromDoc to set
	 */
	public void setFromDoc(String fromDoc) {
		this.fromDoc = fromDoc;
	}

	/**
	 * @return the toDoc
	 */
	public String getToDoc() {
		return toDoc;
	}

	/**
	 * @param toDoc
	 *            the toDoc to set
	 */
	public void setToDoc(String toDoc) {
		this.toDoc = toDoc;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

}
