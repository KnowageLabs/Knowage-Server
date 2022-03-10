/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.resourcemanager.resource.dto;

import javax.validation.constraints.NotNull;

import it.eng.knowage.boot.validation.Xss;

/**
 * @author albnale
 *
 */
public class CreateFolderDTO {
	private String key;

	@NotNull
	@Xss
	private String folderName;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((folderName == null) ? 0 : folderName.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CreateFolderDTO other = (CreateFolderDTO) obj;
		if (folderName == null) {
			if (other.folderName != null)
				return false;
		} else if (!folderName.equals(other.folderName))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	public CreateFolderDTO(String key, String folderName) {
		super();
		this.key = key;
		this.folderName = folderName;
	}

	/**
	 *
	 */
	public CreateFolderDTO() {
		super();
	}
}
