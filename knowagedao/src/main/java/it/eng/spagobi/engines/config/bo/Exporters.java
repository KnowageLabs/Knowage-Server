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

package it.eng.spagobi.engines.config.bo;

import java.io.Serializable;

public class Exporters implements Serializable {

	Integer domainId;
	Integer engineId;
	boolean defaultValue;
	String domainLabel;
	String engineLabel;
	boolean persisted;
	Integer updateDomainId;
	Integer updateEngineId;

	public Integer getUpdateDomainId() {
		return updateDomainId;
	}

	public void setUpdateDomainId(Integer updateDomainId) {
		this.updateDomainId = updateDomainId;
	}

	public Integer getUpdateEngineId() {
		return updateEngineId;
	}

	public void setUpdateEngineId(Integer updateEngineId) {
		this.updateEngineId = updateEngineId;
	}

	public boolean isPersisted() {
		return persisted;
	}

	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}

	public String getDomainLabel() {
		return domainLabel;
	}

	public void setDomainLabel(String domainLabel) {
		this.domainLabel = domainLabel;
	}

	public String getEngineLabel() {
		return engineLabel;
	}

	public void setEngineLabel(String engineLabel) {
		this.engineLabel = engineLabel;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	public Integer getEngineId() {
		return engineId;
	}

	public void setEngineId(Integer engineId) {
		this.engineId = engineId;
	}

	public boolean isDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

}