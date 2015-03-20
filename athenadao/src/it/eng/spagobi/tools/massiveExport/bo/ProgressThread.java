/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.massiveExport.bo;


public class ProgressThread {

	private Integer progressThreadId; 
	private String userId; 	
	private Integer partial;
	private Integer total;
	private String functionCd;
	private String status;
	private String randomKey;
	private String type;
	
	public static final String TYPE_MASSIVE_SCHEDULE = "MASSIVE_SCHEDULE";
	public static final String TYPE_MASSIVE_EXPORT = "MASSIVE_EXPORT";
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Integer getProgressThreadId() {
		return progressThreadId;
	}
	public void setProgressThreadId(Integer progressThreadId) {
		this.progressThreadId = progressThreadId;
	}
	public Integer getPartial() {
		return partial;
	}
	public void setPartial(Integer partial) {
		this.partial = partial;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public String getFunctionCd() {
		return functionCd;
	}
	public void setFunctionCd(String functionCd) {
		this.functionCd = functionCd;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public ProgressThread( String userId,
			Integer total, 
			String functionCd, 
			String status,
			String randomKey,
			String type) {
		super();
		this.userId = userId;
		this.total = total;
		this.functionCd = functionCd;
		this.status = status;
		this.randomKey = randomKey;
		this.type = type;
	}
	public ProgressThread() {
		super();

	}
	public String getRandomKey() {
		return randomKey;
	}
	public void setRandomKey(String randomKey) {
		this.randomKey = randomKey;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "userID: "+getUserId()+" functionCd: "+getFunctionCd() + "Partial/Total:"+getPartial()+"/"+getTotal()+" Messgae: "+getStatus()+" Type:"+type;
	}
	
}
