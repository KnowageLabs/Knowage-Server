/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.udp.bo;

import java.io.Serializable;
import java.util.Date;

/**
* @author Antonella Giachino (antonella.giachino@eng.it)
*/

public class UdpValue implements Serializable {
	
    private Integer udpValueId=null;
    private Integer udpId=null;
    private Integer referenceId=null;
    private String label=null;
    private String name=null;
	private String value=null;
	private String family=null;
	private Integer prog=null;
	private Date beginTs=null;
	private Date endTs=null;
	// memorize from UDB the type label, needed ffor editors creation
	private String typeLabel = null;
	
	/**
	 * @return the udpValueId
	 */
	public Integer getUdpValueId() {
		return udpValueId;
	}
	/**
	 * @param udpValueId the udpValueId to set
	 */
	public void setUdpValueId(Integer udpValueId) {
		this.udpValueId = udpValueId;
	}
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
	 * @return the referenceId
	 */
	public Integer getReferenceId() {
		return referenceId;
	}
	/**
	 * @param referenceId the referenceId to set
	 */
	public void setReferenceId(Integer referenceId) {
		this.referenceId = referenceId;
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}
	/**
	 * @param family the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}
	/**
	 * @return the prog
	 */
	public Integer getProg() {
		return prog;
	}
	/**
	 * @param prog the prog to set
	 */
	public void setProg(Integer prog) {
		this.prog = prog;
	}
	/**
	 * @return the beginTs
	 */
	public Date getBeginTs() {
		return beginTs;
	}
	/**
	 * @param beginTs the beginTs to set
	 */
	public void setBeginTs(Date beginTs) {
		this.beginTs = beginTs;
	}
	/**
	 * @return the endTs
	 */
	public Date getEndTs() {
		return endTs;
	}
	/**
	 * @param endTs the endTs to set
	 */
	public void setEndTs(Date endTs) {
		this.endTs = endTs;
	}
	public String getTypeLabel() {
		return typeLabel;
	}
	public void setTypeLabel(String typeLabel) {
		this.typeLabel = typeLabel;
	}
	
	
}