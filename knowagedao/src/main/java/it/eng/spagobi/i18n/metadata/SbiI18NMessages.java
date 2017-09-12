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

	private SbiI18NMessagesId id;
	private String message;

	public SbiI18NMessages() {
	}

	public SbiI18NMessages(SbiI18NMessagesId id) {
		this.id = id;
	}

	public SbiI18NMessages(SbiI18NMessagesId id, String message) {
		this.id = id;
		this.message = message;
	}

	public SbiI18NMessagesId getId() {
		return this.id;
	}

	public void setId(SbiI18NMessagesId id) {
		this.id = id;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
