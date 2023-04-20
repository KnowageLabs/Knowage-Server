/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.api.dto;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViewOverDocument extends AbstractViewFolderItem {

	private static final String TYPE = "VIEW";

	private String label;

	private String name;

	private String description;

	private JSONObject drivers;

	private JSONObject settings;

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return the drivers
	 */
	public JSONObject getDrivers() {
		return drivers;
	}

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the settings
	 */
	public JSONObject getSettings() {
		return settings;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param drivers the drivers to set
	 */
	public void setDrivers(JSONObject drivers) {
		this.drivers = drivers;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(JSONObject settings) {
		this.settings = settings;
	}

}
