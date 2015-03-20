/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.profiling.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserBO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5176913503788924840L;
	
	private int id;
	private String userId;
	private String password;
	private String fullName;
	private Date dtPwdBegin;
	private Date dtPwdEnd;
	private Boolean flgPwdBlocked;
	private Date dtLastAccess;
	private Boolean isSuperadmin;
	
	private List sbiExtUserRoleses = new ArrayList();
	private HashMap<Integer, HashMap<String, String>> sbiUserAttributeses = new HashMap<Integer, HashMap<String,String>>();
	

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public Date getDtPwdBegin() {
		return dtPwdBegin;
	}
	public void setDtPwdBegin(Date dtPwdBegin) {
		this.dtPwdBegin = dtPwdBegin;
	}
	public Date getDtPwdEnd() {
		return dtPwdEnd;
	}
	public void setDtPwdEnd(Date dtPwdEnd) {
		this.dtPwdEnd = dtPwdEnd;
	}
	public Boolean getFlgPwdBlocked() {
		return flgPwdBlocked;
	}
	public void setFlgPwdBlocked(Boolean flgPwdBlocked) {
		this.flgPwdBlocked = flgPwdBlocked;
	}
	public Date getDtLastAccess() {
		return dtLastAccess;
	}
	public void setDtLastAccess(Date dtLastAccess) {
		this.dtLastAccess = dtLastAccess;
	}
	public List getSbiExtUserRoleses() {
		return sbiExtUserRoleses;
	}
	public void setSbiExtUserRoleses(List sbiExtUserRoleses) {
		this.sbiExtUserRoleses = sbiExtUserRoleses;
	}
	public HashMap<Integer, HashMap<String, String>> getSbiUserAttributeses() {
		return sbiUserAttributeses;
	}
	public void setSbiUserAttributeses(
			HashMap<Integer, HashMap<String, String>> sbiUserAttributeses) {
		this.sbiUserAttributeses = sbiUserAttributeses;
	}
	public Boolean getIsSuperadmin() {
		return this.isSuperadmin;
	}

	public void setIsSuperadmin(Boolean isSuperadmin) {
		this.isSuperadmin = isSuperadmin;
	}

}
