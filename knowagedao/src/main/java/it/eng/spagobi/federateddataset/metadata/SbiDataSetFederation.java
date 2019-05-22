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

package it.eng.spagobi.federateddataset.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

public class SbiDataSetFederation extends SbiHibernateModel {

	private static final long serialVersionUID = 1L;

	private SbiDataSetFederationId id;
	private SbiDataSet dataSet;
	private SbiFederationDefinition federation;

	public SbiDataSetFederation() {

	}

	public SbiDataSetFederation(SbiDataSetFederationId id, SbiDataSet dataSet, SbiFederationDefinition federation) {
		super();
		this.id = id;
		this.dataSet = dataSet;
		this.federation = federation;
	}

	public SbiDataSetFederationId getId() {
		return id;
	}

	public void setId(SbiDataSetFederationId id) {
		this.id = id;
	}

	public SbiDataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(SbiDataSet dataSet) {
		this.dataSet = dataSet;
	}

	public SbiFederationDefinition getFederation() {
		return federation;
	}

	public void setFederation(SbiFederationDefinition federation) {
		this.federation = federation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataSet == null) ? 0 : dataSet.hashCode());
		result = prime * result + ((federation == null) ? 0 : federation.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		SbiDataSetFederation other = (SbiDataSetFederation) obj;
		if (dataSet == null) {
			if (other.dataSet != null)
				return false;
		} else if (!dataSet.equals(other.dataSet))
			return false;
		if (federation == null) {
			if (other.federation != null)
				return false;
		} else if (!federation.equals(other.federation))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
