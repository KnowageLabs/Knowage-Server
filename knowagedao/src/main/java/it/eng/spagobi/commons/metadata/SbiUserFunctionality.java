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

import java.util.Set;


public class SbiUserFunctionality  extends SbiHibernateModel {


     private int id;
     private String name=null;
     private String description=null;
     private Set roleType;
     private SbiProductType productType;
     
    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * Sets the id.
     * 
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description.
     * 
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the role type.
     * 
     * @return the role type
     */
    public Set getRoleType() {
        return roleType;
    }
    
    /**
     * Sets the role type.
     * 
     * @param roleType the new role type
     */
    public void setRoleType(Set roleType) {
        this.roleType = roleType;
    }

	public SbiProductType getProductType() {
		return productType;
	}

	public void setProductType(SbiProductType productType) {
		this.productType = productType;
	}

    




}