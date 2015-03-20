/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.hotlink.rememberme.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubObjects;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class SbiRememberMe extends SbiHibernateModel  {

	//	Fields
	private Integer id;
	private String name;
	private String description;
	private String userName;
	private SbiObjects sbiObject;
	private SbiSubObjects sbiSubObject;
	private String parameters;
	
    // Constructors

	/**
     * default constructor.
     */
    public SbiRememberMe() {
    }
    
    /**
     * constructor with id.
     * 
     * @param id the id
     */
    public SbiRememberMe(Integer id) {
        this.id = id;
    }

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the sbi object.
	 * 
	 * @return the sbi object
	 */
	public SbiObjects getSbiObject() {
		return sbiObject;
	}

	/**
	 * Sets the sbi object.
	 * 
	 * @param sbiObject the new sbi object
	 */
	public void setSbiObject(SbiObjects sbiObject) {
		this.sbiObject = sbiObject;
	}

	/**
	 * Gets the parameters.
	 * 
	 * @return the parameters
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters.
	 * 
	 * @param parameters the new parameters
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the sbi sub object.
	 * 
	 * @return the sbi sub object
	 */
	public SbiSubObjects getSbiSubObject() {
		return sbiSubObject;
	}

	/**
	 * Sets the sbi sub object.
	 * 
	 * @param sbiSubObject the new sbi sub object
	 */
	public void setSbiSubObject(SbiSubObjects sbiSubObject) {
		this.sbiSubObject = sbiSubObject;
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
    
}
