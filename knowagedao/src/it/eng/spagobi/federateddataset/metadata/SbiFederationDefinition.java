/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.federateddataset.metadata;

import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

public class SbiFederationDefinition extends SbiHibernateModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7110733029167618379L;
	//fields
	private int federation_id;
	private String label;
	private String name;
	private String description;
	private String relationships;
	private Set<SbiDataSet> sourceDatasets;
	private Boolean degenerated; //true if the federation is degenerated.. When a user creates a derived dataset the system creates a federation that links the original dataste and the derived one
	
	//constructors
	public String getRelationships() {
		return relationships;
	}

	public SbiFederationDefinition(int federation_id, String label, String name,
			String description, String relationships) {
		super();
		this.federation_id = federation_id;
		this.label = label;
		this.name = name;
		this.description = description;
		this.relationships = relationships;
	}

	public void setRelationships(String relationships) {
		this.relationships = relationships;
	}

	public SbiFederationDefinition(int federation_id) {
		
		this.federation_id = federation_id;
	}

	public int getFederation_id() {
		return federation_id;
	}

	public SbiFederationDefinition() {
		
	}

	public int federation_id() {
		return federation_id;
	}

	public void setFederation_id(int federation_id) {
		this.federation_id = federation_id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<SbiDataSet> getSourceDatasets() {
		return sourceDatasets;
	}

	public void setSourceDatasets(Set<SbiDataSet> sourceDatasets) {
		this.sourceDatasets = sourceDatasets;
	}

	public boolean isDegenerated() {
		if(degenerated == null){
			return false;
		}
		return degenerated;
	}

	public void setDegenerated(Boolean degenerated) {
		this.degenerated = degenerated;
	}
	
	
	
	
}
