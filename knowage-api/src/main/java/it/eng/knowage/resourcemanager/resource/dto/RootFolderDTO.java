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

import java.util.List;

public class RootFolderDTO {

	private List<FolderDTO> root;

	public RootFolderDTO() {
		super();
	}

	public RootFolderDTO(List<FolderDTO> root) {
		super();
		this.root = root;
	}

	public List<FolderDTO> getRoot() {
		return root;
	}

	public void setRoot(List<FolderDTO> root) {
		this.root = root;
	}

}
