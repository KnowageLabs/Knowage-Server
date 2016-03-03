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
package it.eng.spagobi.commons.metadata;

import java.util.HashSet;
import java.util.Set;



public class SbiTenant  extends SbiHibernateModel {

    // Fields    

	private Integer id;
	private String name;
	private String theme;
	private Set sbiOrganizationDatasources = new HashSet(0);
	private Set sbiOrganizationProductType = new HashSet(0);
	
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

	public Set getSbiOrganizationDatasources() {
		return this.sbiOrganizationDatasources;
	}

	public void setSbiOrganizationDatasources(Set sbiOrganizationDatasources) {
		this.sbiOrganizationDatasources = sbiOrganizationDatasources;
	}

	public Set getSbiOrganizationProductType() {
		return sbiOrganizationProductType;
	}

	public void setSbiOrganizationProductType(Set sbiOrganizationProductType) {
		this.sbiOrganizationProductType = sbiOrganizationProductType;
	}
}