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
package it.eng.knowage.resourcemanager.resource.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class FolderDTO {

	private String label;

	private String key;

	@JsonIgnore
	private String fullPath;

	@JsonInclude(Include.NON_EMPTY)
	private List<FolderDTO> children;

	public FolderDTO() {
		super();
	}

	public FolderDTO(String name) {
		children = new ArrayList<>();
		this.label = name.substring(name.lastIndexOf(File.separator) + 1);
		this.fullPath = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<FolderDTO> getChildren() {
		return children;
	}

	public void addChildren(FolderDTO folder) {
		this.children.add(folder);
	}

	@Override
	public String toString() {
		return "FolderDTO [label=" + label + ", children=" + children + "]";
	}

	public String getKey() {
		return key;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public void setKey(String key) {
		this.key = key;
	}

}