/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j;

import java.io.Serializable;
import java.util.List;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * @class WriteBackEditConfig The configuration of the writeback..
 */
public class WriteBackEditConfig implements Serializable {

	private static final long serialVersionUID = 7446808818499425470L;

	private String editCubeName;

	private List<String> editableMeasures;

	private Integer initalVersion;

	public String getEditCubeName() {
		return editCubeName;
	}

	public void setEditCubeName(String editCubeName) {
		this.editCubeName = editCubeName;
	}

	public List<String> getEditableMeasures() {
		return editableMeasures;
	}

	public void setEditableMeasures(List<String> editableMeasures) {
		this.editableMeasures = editableMeasures;
	}

	public Integer getInitialVersion() {
		if (initalVersion == null) {
			return 0;
		}
		return initalVersion;
	}

	public void setInitialVersion(Integer defaultVersion) {
		this.initalVersion = defaultVersion;
	}

}
