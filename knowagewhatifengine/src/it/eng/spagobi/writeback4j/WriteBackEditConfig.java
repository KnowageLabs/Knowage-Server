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

package it.eng.spagobi.writeback4j;

import java.io.Serializable;
import java.util.List;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 * @class WriteBackEditConfig The configuration of the writeback..
 */
public class WriteBackEditConfig implements Serializable {

	private static final long serialVersionUID = 7446808818499425470L;

	private String editCubeName;

	private List<String> editableMeasures;

	private Integer initalVersion;

	public String getEditCubeName() {
		return editCubeName;
	}

	public void setEditCubeName(String editCubeName) {
		this.editCubeName = editCubeName;
	}

	public List<String> getEditableMeasures() {
		return editableMeasures;
	}

	public void setEditableMeasures(List<String> editableMeasures) {
		this.editableMeasures = editableMeasures;
	}

	public Integer getInitialVersion() {
		if (initalVersion == null) {
			return 0;
		}
		return initalVersion;
	}

	public void setInitialVersion(Integer defaultVersion) {
		this.initalVersion = defaultVersion;
	}

}
