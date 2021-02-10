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
package it.eng.spagobi.profiling.bean;

import java.util.Date;
import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiCommonInfo;

// Generated 22-dic-2009 14.20.04 by Hibernate Tools 3.2.4.GA

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * Wrapper of {@link SbiUser}.
 *
 * Doesn't extend {@link SbiUser} because it's used by Hibernate.
 */
public class SbiUserHistory extends SbiHibernateModel {

	private final SbiUser wrappedObj;
	private SbiCommonInfo commonInfo;
	private int eventId;
	private Date timeStart;
	private Date timeEnd;

	public SbiUserHistory() {
		wrappedObj = new SbiUser();
		commonInfo = new SbiCommonInfo();
	}

	public SbiUserHistory(SbiUser wrappedObj) {
		this.wrappedObj = wrappedObj;
		this.commonInfo = new SbiCommonInfo(wrappedObj.getCommonInfo());
	}

	@Override
	public SbiCommonInfo getCommonInfo() {
		return commonInfo;
	}

	@Override
	public void setCommonInfo(SbiCommonInfo commonInfo) {
		this.commonInfo = commonInfo;
	}


	public int getId() {
		return wrappedObj.getId();
	}


	public void setId(int id) {
		wrappedObj.setId(id);
	}


	public String getUserId() {
		return wrappedObj.getUserId();
	}


	public void setUserId(String userId) {
		wrappedObj.setUserId(userId);
	}


	public String getPassword() {
		return wrappedObj.getPassword();
	}


	public void setPassword(String password) {
		wrappedObj.setPassword(password);
	}


	public String getFullName() {
		return wrappedObj.getFullName();
	}


	public void setFullName(String fullName) {
		wrappedObj.setFullName(fullName);
	}


	public Date getDtPwdBegin() {
		return wrappedObj.getDtPwdBegin();
	}


	public void setDtPwdBegin(Date dtPwdBegin) {
		wrappedObj.setDtPwdBegin(dtPwdBegin);
	}


	public Date getDtPwdEnd() {
		return wrappedObj.getDtPwdEnd();
	}


	public void setDtPwdEnd(Date dtPwdEnd) {
		wrappedObj.setDtPwdEnd(dtPwdEnd);
	}


	public Boolean getFlgPwdBlocked() {
		return wrappedObj.getFlgPwdBlocked();
	}


	public void setFlgPwdBlocked(Boolean flgPwdBlocked) {
		wrappedObj.setFlgPwdBlocked(flgPwdBlocked);
	}


	public Date getDtLastAccess() {
		return wrappedObj.getDtLastAccess();
	}


	public void setDtLastAccess(Date dtLastAccess) {
		wrappedObj.setDtLastAccess(dtLastAccess);
	}


	public Set<SbiExtRoles> getSbiExtUserRoleses() {
		return wrappedObj.getSbiExtUserRoleses();
	}


	public void setSbiExtUserRoleses(Set<SbiExtRoles> sbiExtUserRoleses) {
		wrappedObj.setSbiExtUserRoleses(sbiExtUserRoleses);
	}


	public Set<SbiUserAttributes> getSbiUserAttributeses() {
		return wrappedObj.getSbiUserAttributeses();
	}


	public void setSbiUserAttributeses(Set<SbiUserAttributes> sbiUserAttributeses) {
		wrappedObj.setSbiUserAttributeses(sbiUserAttributeses);
	}


	public Boolean getIsSuperadmin() {
		return wrappedObj.getIsSuperadmin();
	}


	public void setIsSuperadmin(Boolean isSuperadmin) {
		wrappedObj.setIsSuperadmin(isSuperadmin);
	}

	@Override
	public int hashCode() {
		return wrappedObj.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return wrappedObj.equals(obj);
	}

	@Override
	public String toString() {
		return wrappedObj.toString();
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public Date getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
	}

	public Date getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(Date timeEnd) {
		this.timeEnd = timeEnd;
	}

}
