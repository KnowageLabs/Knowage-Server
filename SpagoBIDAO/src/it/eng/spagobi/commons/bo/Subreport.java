/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import java.io.Serializable;

/**
 * @author Gioia
 *
 */
public class Subreport implements Serializable{
	private Integer  master_rpt_id;
	private Integer  sub_rpt_id;
	
	/**
	 * Instantiates a new subreport.
	 */
	public Subreport() {}
	
	/**
	 * Instantiates a new subreport.
	 * 
	 * @param master_rpt_id the master_rpt_id
	 * @param sub_rpt_id the sub_rpt_id
	 */
	public Subreport(Integer master_rpt_id, Integer sub_rpt_id) {
		this.master_rpt_id = master_rpt_id;
		this.sub_rpt_id = sub_rpt_id;
	}
	
	/**
	 * Gets the master_rpt_id.
	 * 
	 * @return the master_rpt_id
	 */
	public Integer getMaster_rpt_id() {
		return master_rpt_id;
	}
	
	/**
	 * Sets the master_rpt_id.
	 * 
	 * @param master_rpt_id the new master_rpt_id
	 */
	public void setMaster_rpt_id(Integer master_rpt_id) {
		this.master_rpt_id = master_rpt_id;
	}
	
	/**
	 * Gets the sub_rpt_id.
	 * 
	 * @return the sub_rpt_id
	 */
	public Integer getSub_rpt_id() {
		return sub_rpt_id;
	}
	
	/**
	 * Sets the sub_rpt_id.
	 * 
	 * @param sub_rpt_id the new sub_rpt_id
	 */
	public void setSub_rpt_id(Integer sub_rpt_id) {
		this.sub_rpt_id = sub_rpt_id;
	}
}
