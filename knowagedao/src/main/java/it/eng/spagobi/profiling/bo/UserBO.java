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
package it.eng.spagobi.profiling.bo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.eng.spagobi.services.validation.Xss;

/**
 * Store data of a user.
 *
 * The field <code>password</code> must be hidden during deserialization because of security concerns: with {@link JsonIgnoreProperties} we enable it during
 * serialization only.
 */
@JsonIgnoreProperties(value = { "password" }, allowGetters = false, allowSetters = true)
public class UserBO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5176913503788924840L;

	private Integer id;
	@Xss
	@NotNull
	@Size(max = 100)
	private String userId;
	@Xss
	@Size(max = 150)
	private String password;
	@Xss
	@Size(max = 255)
	private String fullName;
	private Date dtPwdBegin;
	private Date dtPwdEnd;
	private Boolean flgPwdBlocked;
	private Date dtLastAccess;
	private Boolean isSuperadmin;
	private Integer defaultRoleId;
	private Integer failedLoginAttempts;
	private boolean blockedByFailedLoginAttempts;

	private List sbiExtUserRoleses = new ArrayList();
	private HashMap<Integer, HashMap<String, String>> sbiUserAttributeses = new HashMap<Integer, HashMap<String, String>>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	public void setSbiUserAttributeses(HashMap<Integer, HashMap<String, String>> sbiUserAttributeses) {
		this.sbiUserAttributeses = sbiUserAttributeses;
	}

	public Boolean getIsSuperadmin() {
		return this.isSuperadmin;
	}

	public void setIsSuperadmin(Boolean isSuperadmin) {
		this.isSuperadmin = isSuperadmin;
	}

	public Integer getDefaultRoleId() {
		return defaultRoleId;
	}

	public void setDefaultRoleId(Integer defaultRoleId) {
		this.defaultRoleId = defaultRoleId;
	}

	public Integer getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(Integer failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public boolean getBlockedByFailedLoginAttempts() {
		return blockedByFailedLoginAttempts;
	}

	public void setBlockedByFailedLoginAttempts(boolean blockedByFailedLoginAttempts) {
		this.blockedByFailedLoginAttempts = blockedByFailedLoginAttempts;
	}

}
