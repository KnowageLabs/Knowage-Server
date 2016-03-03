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
package it.eng.spagobi.commons.metadata;

import java.util.Date;


public class SbiCommonInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String SBI_VERSION="4.0";
	
	private String userIn="server";
	private String userUp=null;
	private String userDe=null;
	private String sbiVersionIn=null;
	private String sbiVersionUp=null;
	private String sbiVersionDe=null;
	private String organization=null;
	private Date timeIn = null;
	private Date timeUp = null;
	private Date timeDe = null;
	
	public SbiCommonInfo(){
		sbiVersionIn=SbiCommonInfo.SBI_VERSION;
		timeIn = new Date();
	}
	public String getUserIn() {
		return userIn;
	}
	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}
	public String getUserUp() {
		return userUp;
	}
	public void setUserUp(String userUp) {
		this.userUp = userUp;
	}
	public String getUserDe() {
		return userDe;
	}
	public void setUserDe(String userDe) {
		this.userDe = userDe;
	}
	public String getSbiVersionIn() {
		return sbiVersionIn;
	}
	public void setSbiVersionIn(String sbiVersionIn) {
		this.sbiVersionIn = sbiVersionIn;
	}
	public String getSbiVersionUp() {
		return sbiVersionUp;
	}
	public void setSbiVersionUp(String sbiVersionUp) {
		this.sbiVersionUp = sbiVersionUp;
	}
	public String getSbiVersionDe() {
		return sbiVersionDe;
	}
	public void setSbiVersionDe(String sbiVersionDe) {
		this.sbiVersionDe = sbiVersionDe;
	}

	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public Date getTimeIn() {
		return timeIn;
	}
	public void setTimeIn(Date timeIn) {
		this.timeIn = timeIn;
	}
	public Date getTimeUp() {
		return timeUp;
	}
	public void setTimeUp(Date timeUp) {
		this.timeUp = timeUp;
	}
	public Date getTimeDe() {
		return timeDe;
	}
	public void setTimeDe(Date timeDe) {
		this.timeDe = timeDe;
	}
}
