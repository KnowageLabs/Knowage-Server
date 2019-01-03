/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.tag;

import java.io.Serializable;

import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

public class SbiDatasetTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private SbiDatasetTagId dsTagId;
	private SbiDataSet dataSet;
	private SbiTag tag;

	public SbiDatasetTag() {

	}

	public SbiDatasetTag(SbiDatasetTagId dsTagId) {
		this.dsTagId = dsTagId;
	}

	public SbiDatasetTag(SbiDatasetTagId dsTagId, SbiDataSet dataSet, SbiTag tag) {
		this.dsTagId = dsTagId;
		this.dataSet = dataSet;
		this.tag = tag;
	}

	public SbiDatasetTagId getDsTagId() {
		return dsTagId;
	}

	public void setDsTagId(SbiDatasetTagId dsTagId) {
		this.dsTagId = dsTagId;
	}

	public SbiDataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(SbiDataSet dataSet) {
		this.dataSet = dataSet;
	}

	public SbiTag getTag() {
		return tag;
	}

	public void setTag(SbiTag tag) {
		this.tag = tag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		result = prime * result + ((dsTagId == null) ? 0 : dsTagId.hashCode());
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
		SbiDatasetTag other = (SbiDatasetTag) obj;
		if (dsTagId == null) {
			if (other.dsTagId != null)
				return false;
		} else if (!dsTagId.equals(other.dsTagId))
			return false;

		return true;
	}

}
