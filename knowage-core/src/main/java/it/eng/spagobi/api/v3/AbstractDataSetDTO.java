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

abstract class AbstractDataSetDTO {

	private static final Logger LOGGER = Logger.getLogger(DataSetMainDTO.class);

	private static final Map<String, String> TYPE_2_DS_TYPE_CD = DataSetConstants.code2name;

	protected final SbiDataSet dataset;
	private final List<DataSetResourceAction> actions = new ArrayList<>();
	private final int usedByNDocs;

	public AbstractDataSetDTO(SbiDataSet dataset) {
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
			LOGGER.warn("Error during check of Geo spatial column for dataset with id " + getId());
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

	public Integer getCatTypeId() {
		return dataset.getCategoryId();
	}

}
