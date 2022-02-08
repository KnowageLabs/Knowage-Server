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

/**
 * @author albnale
 *
 */
public class SbiOrganizationTheme extends SbiHibernateModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1895036383390787978L;

	private SbiOrganizationThemeId id;
	private String config;
	private boolean active;

	public SbiOrganizationTheme() {

	}

	/**
	 * @param id
	 * @param themeName
	 * @param config
	 * @param active
	 */
	public SbiOrganizationTheme(SbiOrganizationThemeId id, String config, boolean active) {
		super();
		this.id = id;
		this.config = config;
		this.active = active;
	}

	/**
	 * @return the id
	 */
	public SbiOrganizationThemeId getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(SbiOrganizationThemeId id) {
		this.id = id;
	}

	/**
	 * @return the config
	 */
	public String getConfig() {
		return config;
	}

	/**
	 * @param config the config to set
	 */
	public void setConfig(String config) {
		this.config = config;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

}
