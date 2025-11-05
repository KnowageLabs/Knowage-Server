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
package it.eng.knowage.resourcemanager.log.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import it.eng.knowage.boot.validation.NotInvalidCharacters;
import it.eng.knowage.boot.validation.Xss;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
* DTO representing a folder node in the logs UI tree.
* - label: visible name for UI.
* - key: opaque identifier (client uses this to reference nodes).
* - fullPath: internal absolute path (ignored in JSON).
* - relativePath: path used by client to request lists/downloads.
* - children: only serialized when non-empty to keep payload compact.
*/
public class LogFolderDTO {

	@NotNull
	@Xss
	@NotInvalidCharacters
    // Visible label for the UI tree node.
	private String label;

    // Opaque key (hashed) used by clients to reference this node.
	private String key;

	@JsonIgnore
    // Internal absolute path, not exposed to clients.
	private String fullPath;

    // Relative path used by clients when requesting files or downloads.
	private String relativePath;

	@JsonInclude(Include.NON_EMPTY)
    // Child folders (kept out of JSON when empty).
	private List<LogFolderDTO> children;

	public LogFolderDTO() {
		super();
	}

    /*
    * Construct from a Path.
    * - label is the last element of the path.
    * - fullPath stored for internal resolution.
    */
	public LogFolderDTO(Path fullPath) {
		children = new ArrayList<>();
		this.label = fullPath.getName(fullPath.getNameCount() - 1).toString();
		this.fullPath = fullPath.toString();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<LogFolderDTO> getChildren() {
		return children;
	}

	public void addChildren(LogFolderDTO folder) {
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

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

    // Standard equals/hashCode to support tests and collection usage.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		LogFolderDTO other = (LogFolderDTO) obj;
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
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		} else if (!relativePath.equals(other.relativePath))
			return false;
		return true;
	}

}