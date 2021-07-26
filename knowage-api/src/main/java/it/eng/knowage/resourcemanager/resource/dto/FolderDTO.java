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

	private int key;

	@JsonIgnore
	private String fullPath;

	private String relativePath;

	private boolean modelFolder;

	private int level;

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

	public int getKey() {
		return key;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public boolean isModelFolder() {
		return modelFolder;
	}

	public void setModelFolder(boolean modelFolder) {
		this.modelFolder = modelFolder;
	}

	public int getLevel() {
		return this.level;

	}

	public void setLevel(int level) {
		this.level = level;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result + key;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + level;
		result = prime * result + (modelFolder ? 1231 : 1237);
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
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
		FolderDTO other = (FolderDTO) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (key != other.key)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (level != other.level)
			return false;
		if (modelFolder != other.modelFolder)
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		return true;
	}

}