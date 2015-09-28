/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.federation;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.Set;

public class FederationDefinition {

	
	private int federation_id;
	
	private String name;
	
	private String label;
	
	private String description;
	
	private String relationships;
	
	private Set<IDataSet> sourceDatasets;

	public int getFederation_id() {
		return federation_id;
	}

	public void setFederation_id(int federation_id) {
		this.federation_id = federation_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRelationships() {
		return relationships;
	}

	public void setRelationships(String relationships) {
		this.relationships = relationships;
	}

	public Set<IDataSet> getSourceDatasets() {
		return sourceDatasets;
	}

	public void setSourceDatasets(Set<IDataSet> sourceDatasets) {
		this.sourceDatasets = sourceDatasets;
	}
	
	
	
}
