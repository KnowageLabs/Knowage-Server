/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.metadata;

import java.util.HashSet;
import java.util.Set;



public class SbiTenant  extends SbiHibernateModel {

    // Fields    

	private Integer id;
	private String name;
	private String theme;
	private Set sbiOrganizationEngines = new HashSet(0);
	private Set sbiOrganizationDatasources = new HashSet(0);
	
    // Constructors



	/**
     * default constructor.
     */
    public SbiTenant() {
    }
    
    /**
     * constructor with id.
     * 
     * @param valueId the value id
     */
    public SbiTenant(Integer id) {
        this.id = id;
    }
   

    // Property accessors
    
    
    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
	public Set getSbiOrganizationEngines() {
		return this.sbiOrganizationEngines;
	}

	public void setSbiOrganizationEngines(Set sbiOrganizationEngines) {
		this.sbiOrganizationEngines = sbiOrganizationEngines;
	}

	public Set getSbiOrganizationDatasources() {
		return this.sbiOrganizationDatasources;
	}

	public void setSbiOrganizationDatasources(Set sbiOrganizationDatasources) {
		this.sbiOrganizationDatasources = sbiOrganizationDatasources;
	}
}