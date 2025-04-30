/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.commons.metadata;

import java.util.Date;
import java.util.UUID;

import org.json.JSONObject;

/**
 * @author Marco Libanori
 */
public class SbiDashboardTheme extends SbiHibernateModel {

	private static final long serialVersionUID = 1895036383390787978L;

	private UUID id = UUID.randomUUID();
	private String themeName;
	private JSONObject config;
	private Boolean isDefault;
	private String userIn;
	private String userUp;
	private String userDe;
	private Date timeIn;
	private Date timeUp;
	private Date timeDe;
	private String sbiVersionIn;
	private String sbiVersionUp;
	private String sbiVersionDe;
	private String metaVersion;

	/**
	 * @return the config
	 */
	public JSONObject getConfig() {
		return config;
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @return the metaVersion
	 */
	public String getMetaVersion() {
		return metaVersion;
	}

	/**
	 * @return the sbiVersionDe
	 */
	public String getSbiVersionDe() {
		return sbiVersionDe;
	}

	/**
	 * @return the sbiVersionIn
	 */
	public String getSbiVersionIn() {
		return sbiVersionIn;
	}

	/**
	 * @return the sbiVersionUp
	 */
	public String getSbiVersionUp() {
		return sbiVersionUp;
	}

	/**
	 * @return the themeName
	 */
	public String getThemeName() {
		return themeName;
	}

	/**
	 * @return the timeDe
	 */
	public Date getTimeDe() {
		return timeDe;
	}

	/**
	 * @return the timeIn
	 */
	public Date getTimeIn() {
		return timeIn;
	}

	/**
	 * @return the timeUp
	 */
	public Date getTimeUp() {
		return timeUp;
	}

	/**
	 * @return the userDe
	 */
	public String getUserDe() {
		return userDe;
	}

	/**
	 * @return the userIn
	 */
	public String getUserIn() {
		return userIn;
	}

	/**
	 * @return the userUp
	 */
	public String getUserUp() {
		return userUp;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(JSONObject config) {
		this.config = config;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @param metaVersion the metaVersion to set
	 */
	public void setMetaVersion(String metaVersion) {
		this.metaVersion = metaVersion;
	}

	/**
	 * @param sbiVersionDe the sbiVersionDe to set
	 */
	public void setSbiVersionDe(String sbiVersionDe) {
		this.sbiVersionDe = sbiVersionDe;
	}

	/**
	 * @param sbiVersionIn the sbiVersionIn to set
	 */
	public void setSbiVersionIn(String sbiVersionIn) {
		this.sbiVersionIn = sbiVersionIn;
	}

	/**
	 * @param sbiVersionUp the sbiVersionUp to set
	 */
	public void setSbiVersionUp(String sbiVersionUp) {
		this.sbiVersionUp = sbiVersionUp;
	}

	/**
	 * @param themeName the themeName to set
	 */
	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}

	/**
	 * @param timeDe the timeDe to set
	 */
	public void setTimeDe(Date timeDe) {
		this.timeDe = timeDe;
	}

	/**
	 * @param timeIn the timeIn to set
	 */
	public void setTimeIn(Date timeIn) {
		this.timeIn = timeIn;
	}

	/**
	 * @param timeUp the timeUp to set
	 */
	public void setTimeUp(Date timeUp) {
		this.timeUp = timeUp;
	}

	/**
	 * @param userDe the userDe to set
	 */
	public void setUserDe(String userDe) {
		this.userDe = userDe;
	}

	/**
	 * @param userIn the userIn to set
	 */
	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}

	/**
	 * @param userUp the userUp to set
	 */
	public void setUserUp(String userUp) {
		this.userUp = userUp;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

}
