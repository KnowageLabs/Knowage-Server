/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

import it.eng.qbe.model.structure.IModelField;

import java.util.List;

/**
 * The Class Filter.
 *
 * @author Andrea Gioia
 */
public class Filter {

	private IModelField field = null;

	private List<String> values;

	public Filter(IModelField field, List<String> values) {
		super();
		this.field = field;
		this.values = values;
	}

	public IModelField getField() {
		return field;
	}

	public void setField(IModelField field) {
		this.field = field;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
