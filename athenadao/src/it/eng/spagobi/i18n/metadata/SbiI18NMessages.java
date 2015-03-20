/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
