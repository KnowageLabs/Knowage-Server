/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.metadata;

import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;


public class SbiObjTemplates  extends SbiHibernateModel {


	private Integer objTempId;
     private SbiObjects sbiObject;
     private SbiBinContents sbiBinContents;
     private String name;
     private Integer prog;
     private Date creationDate;
     private Boolean active;
     private String dimension=null;     
     private String creationUser=null;
     
	/**
	 * Gets the dimension.
	 * 
	 * @return the dimension
	 */
	public String getDimension() {
        return dimension;
    }

    /**
     * Sets the dimension.
     * 
     * @param dimension the new dimension
     */
    public void setDimension(String dimension) {
        this.dimension = dimension;
    }




	/**
	 * Gets the creation user.
	 * 
	 * @return the creation user
	 */
	public String getCreationUser() {
        return creationUser;
    }

    /**
     * Sets the creation user.
     * 
     * @param creationUser the new creation user
     */
    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

	/**
	 * Gets the obj temp id.
	 * 
	 * @return the obj temp id
	 */
	public Integer getObjTempId() {
		return objTempId;
	}
	
	/**
	 * Sets the obj temp id.
	 * 
	 * @param objTempId the new obj temp id
	 */
	public void setObjTempId(Integer objTempId) {
		this.objTempId = objTempId;
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
	 * Gets the sbi bin contents.
	 * 
	 * @return the sbi bin contents
	 */
	public SbiBinContents getSbiBinContents() {
		return sbiBinContents;
	}
	
	/**
	 * Sets the sbi bin contents.
	 * 
	 * @param sbiBinContents the new sbi bin contents
	 */
	public void setSbiBinContents(SbiBinContents sbiBinContents) {
		this.sbiBinContents = sbiBinContents;
	}
	
	/**
	 * Gets the prog.
	 * 
	 * @return the prog
	 */
	public Integer getProg() {
		return prog;
	}
	
	/**
	 * Sets the prog.
	 * 
	 * @param prog the new prog
	 */
	public void setProg(Integer prog) {
		this.prog = prog;
	}
	
	/**
	 * Gets the creation date.
	 * 
	 * @return the creation date
	 */
	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * Sets the creation date.
	 * 
	 * @param creationDate the new creation date
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Gets the active.
	 * 
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Sets the active.
	 * 
	 * @param active the new active
	 */
	public void setActive(Boolean active) {
		this.active = active;
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

}