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
package it.eng.spagobi.tools.catalogue.bo;

public class Artifact {

	private Integer id;

	private Integer currentContentId;

	private String name;

	private String description;

	private String type;

	private Boolean modelLocked;

	private String modelLocker;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getCurrentContentId() {
		return currentContentId;
	}

	public void setCurrentContentId(Integer currentContentId) {
		this.currentContentId = currentContentId;
	}

	public Boolean getModelLocked() {

		if (modelLocker == null) {
			return false;
		}
		return modelLocked;
	}

	public void setModelLocked(Boolean modelLocked) {
		this.modelLocked = modelLocked;
	}

	public String getModelLocker() {

		return modelLocker;
	}

	public void setModelLocker(String modelLocker) {
		this.modelLocker = modelLocker;
	}

	@Override
	public String toString() {
		return "Artifact [id=" + id + ", name=" + name + ", description=" + description + ", type=" + type + ", currentContentId=" + currentContentId + "]";
	}

}
