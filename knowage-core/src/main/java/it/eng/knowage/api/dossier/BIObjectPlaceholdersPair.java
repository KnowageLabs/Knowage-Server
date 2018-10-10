/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier;

import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

import java.util.List;

public class BIObjectPlaceholdersPair {

	BIObject biObject;
	List<PlaceHolder> placeholders;

	public BIObject getBiObject() {
		return biObject;
	}

	public void setBiObject(BIObject biObject) {
		this.biObject = biObject;
	}

	public List<PlaceHolder> getPlaceholders() {
		return placeholders;
	}

	public void setPlaceholders(List<PlaceHolder> placeholders) {
		this.placeholders = placeholders;
	}

}
