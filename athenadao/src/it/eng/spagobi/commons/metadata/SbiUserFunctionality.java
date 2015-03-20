/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.metadata;

import java.util.Set;


public class SbiUserFunctionality  extends SbiHibernateModel {


     private int id;
     private String name=null;
     private String description=null;
     private Set roleType;
     
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
    




}