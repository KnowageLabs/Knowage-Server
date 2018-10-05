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
package it.eng.spagobi.i18n.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiI18NMessages extends SbiHibernateModel implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer languageCd;
	private String label;
	private String message;

	public SbiI18NMessages() {
	}

	public SbiI18NMessages(Integer id, Integer languageCd, String label, String message) {
		super();
		this.id = id;
		this.languageCd = languageCd;
		this.label = label;
		this.message = message;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getLanguageCd() {
		return languageCd;
	}

	public void setLanguageCd(Integer languageCd) {
		this.languageCd = languageCd;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
