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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.eng.knowage.boot.validation.FilesCheck;

/**
 * @author albnale
 *
 */
public class DownloadFilesDTO {
	private String key;

	@JsonDeserialize(as = ArrayList.class, contentAs = String.class)
	@JsonSerialize(as = ArrayList.class, contentAs = String.class)
	@FilesCheck(message = "One or more files are not valid")
	private List<String> selectedFilesNames;

	public DownloadFilesDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DownloadFilesDTO(String key, List<String> selectedFilesNames) {
		super();
		this.key = key;
		this.selectedFilesNames = selectedFilesNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((selectedFilesNames == null) ? 0 : selectedFilesNames.hashCode());
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
		DownloadFilesDTO other = (DownloadFilesDTO) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (selectedFilesNames == null) {
			if (other.selectedFilesNames != null)
				return false;
		} else if (!selectedFilesNames.equals(other.selectedFilesNames))
			return false;
		return true;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getSelectedFilesNames() {
		return selectedFilesNames;
	}

	public void setSelectedFilesNames(List<String> selectedFilesNames) {
		this.selectedFilesNames = selectedFilesNames;
	}

}
