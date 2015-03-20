/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.udp.bo;

/**
* @author Antonella Giachino (antonella.giachino@eng.it)
*/

import java.io.Serializable;
import java.util.Date;

public class Udp implements Serializable {
	
	private Integer udpId=null;
    private String label=null;
    private String name=null;
    private String description=null;
    private Integer dataTypeId=null;
    private Integer familyId=null;
	private Boolean multivalue=null;

	private String dataTypeValeCd = null;
	
	/**
	 * @return the udpId
	 */
	public Integer getUdpId() {
		return udpId;
	}
	/**
	 * @param udpId the udpId to set
	 */
	public void setUdpId(Integer udpId) {
		this.udpId = udpId;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the dataTypeId
	 */
	public Integer getDataTypeId() {
		return dataTypeId;
	}
	/**
	 * @param dataTypeId the dataTypeId to set
	 */
	public void setDataTypeId(Integer dataTypeId) {
		this.dataTypeId = dataTypeId;
	}
	/**
	 * @return the familyId
	 */
	public Integer getFamilyId() {
		return familyId;
	}
	/**
	 * @param familyId the familyId to set
	 */
	public void setFamilyId(Integer familyId) {
		this.familyId = familyId;
	}
	/**
	 * @return the multivalue
	 */
	public Boolean getMultivalue() {
		return multivalue;
	}
	/**
	 * @param multivalue the multivalue to set
	 */
	public void setMultivalue(Boolean multivalue) {
		this.multivalue = multivalue;
	}
	public String getDataTypeValeCd() {
		return dataTypeValeCd;
	}
	public void setDataTypeValeCd(String dataTypeValeCd) {
		this.dataTypeValeCd = dataTypeValeCd;
	}
	
	
	
}
