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
package it.eng.spagobi.engines.dossier.bo;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class DossierPresentation implements Serializable{

	static private Logger logger = Logger.getLogger(DossierPresentation.class);
	
	private Integer id;
	private Long workflowProcessId;
	private Integer biobjectId;
	private Integer binId;
	private String name;
	private byte[] content;
	private Integer prog;
	private Date creationDate;
	private Boolean approved;
	
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
	 * Gets the biobject id.
	 * 
	 * @return the biobject id
	 */
	public Integer getBiobjectId() {
		return biobjectId;
	}
	
	/**
	 * Sets the biobject id.
	 * 
	 * @param biobjectId the new biobject id
	 */
	public void setBiobjectId(Integer biobjectId) {
		this.biobjectId = biobjectId;
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
	 * Gets the content.
	 * 
	 * @return the content
	 * 
	 * @throws EMFUserError the EMF user error
	 * @throws EMFInternalError the EMF internal error
	 */
	public byte[] getContent() throws EMFUserError, EMFInternalError {
		if (content == null) {
			if (binId != null) {
				// reads from database
				try {
					content = DAOFactory.getBinContentDAO().getBinContent(binId);
				} catch (EMFUserError e) {
					logger.error("Error while recovering content of presentation with id = [" + id + "], binary content id = [" + binId + "], " +
							"name = [" + name + "] of biobject with id = [" + biobjectId + "]" + e);
					throw e;
				} catch (EMFInternalError e) {
					logger.error("Error while recovering content of presentation with id = [" + id + "], binary content id = [" + binId + "], " +
							"name = [" + name + "] of biobject with id = [" + biobjectId + "]" + e);
					throw e;
				}
			} else {
				logger.warn("Both content field of this istance and binary identifier are null. Cannot load content from database.");
			}
		}
		return content;
	}
	
	/**
	 * Sets the content.
	 * 
	 * @param content the new content
	 */
	public void setContent(byte[] content) {
		this.content = content;
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
	 * Gets the approved.
	 * 
	 * @return the approved
	 */
	public Boolean getApproved() {
		return approved;
	}
	
	/**
	 * Sets the approved.
	 * 
	 * @param approved the new approved
	 */
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
	
	/**
	 * Gets the bin id.
	 * 
	 * @return the bin id
	 */
	public Integer getBinId() {
		return binId;
	}
	
	/**
	 * Sets the bin id.
	 * 
	 * @param binId the new bin id
	 */
	public void setBinId(Integer binId) {
		this.binId = binId;
	}
	
	/**
	 * Gets the workflow process id.
	 * 
	 * @return the workflow process id
	 */
	public Long getWorkflowProcessId() {
		return workflowProcessId;
	}
	
	/**
	 * Sets the workflow process id.
	 * 
	 * @param workflowProcessId the new workflow process id
	 */
	public void setWorkflowProcessId(Long workflowProcessId) {
		this.workflowProcessId = workflowProcessId;
	}
	
}
