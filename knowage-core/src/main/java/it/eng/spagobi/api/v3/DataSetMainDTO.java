/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

class DataSetMainDTO {

	static protected Logger logger = Logger.getLogger(DataSetMainDTO.class);

	private static final Map<String, String> TYPE_2_DS_TYPE_CD = DataSetConstants.code2name;

	protected final SbiDataSet dataset;
	private final List<DataSetResourceAction> actions = new ArrayList<>();
	private final int usedByNDocs;

	public DataSetMainDTO(SbiDataSet dataset) {
		super();
		this.dataset = dataset;

		Integer dsId = dataset.getId().getDsId();

		Integer numObjAssociated = DAOFactory.getDataSetDAO().countBIObjAssociated(dsId);
		Integer numFederAssociated = DAOFactory.getFedetatedDatasetDAO().countFederationsUsingDataset(dsId);
		usedByNDocs = numObjAssociated + numFederAssociated;
	}

	public Integer getId() {
		return dataset.getId().getDsId();
	}

	@JsonIgnore
	public boolean isGeoDataSet() {
		boolean ret = false;
		try {
			String meta = getDsMetadata();

			if (meta != null && !meta.equals("")) {
				ret = ExecuteAdHocUtility.hasGeoHierarchy(meta);
			}
		} catch (Exception e) {
			logger.warn("Error during check of Geo spatial column for dataset with id " + getId());
		}

		return ret;
	}

	public String getLabel() {
		return dataset.getLabel();
	}

	public String getName() {
		return dataset.getName();
	}

	public String getOwner() {
		return dataset.getOwner();
	}

	public List<DataSetResourceAction> getActions() {
		return actions;
	}

	@JsonIgnore
	public String getType() {
		return dataset.getType();
	}

	public String getDsTypeCd() {
		String type = dataset.getType();
		String ret = "";

		if (TYPE_2_DS_TYPE_CD.containsKey(type)) {
			ret = TYPE_2_DS_TYPE_CD.get(type);
		}

		return ret;
	}

	@JsonIgnore
	public String getDsMetadata() {
		return dataset.getDsMetadata();
	}

	public int getUsedByNDocs() {
		return usedByNDocs;
	}

}