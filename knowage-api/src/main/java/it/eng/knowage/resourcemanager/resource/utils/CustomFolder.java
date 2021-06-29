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
package it.eng.knowage.resourcemanager.resource.utils;

import java.util.ArrayList;
import java.util.List;

public class CustomFolder {

	private String label;

	private String key;

//	private String icon;

//	List<CustomFile> files;

	List<CustomFolder> children;

	public CustomFolder(String name) {
//		files = new ArrayList<>();
		children = new ArrayList<>();
		this.label = name;
//		this.icon = "far fa-folder";
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

//	public List<CustomFile> getFiles() {
//		return files;
//	}

//	public void addFile(CustomFile file) {
//		this.files.add(file);
//	}

	public List<CustomFolder> getChildren() {
		return children;
	}

	public void addChildren(CustomFolder folder) {
		this.children.add(folder);
	}

	@Override
	public String toString() {
		return "CustomFolder [label=" + label + ", children=" + children + "]";
	}

//	public String getIcon() {
//		return icon;
//	}
//
//	public void setIcon(String icon) {
//		this.icon = icon;
//	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}