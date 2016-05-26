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
package it.eng.spagobi.behaviouralmodel.lov.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.objects.Couple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class DatasetDetail extends DependenciesPostProcessingLov implements ILovDetail {

	private static transient Logger logger = Logger.getLogger(DatasetDetail.class);

	private List visibleColumnNames = null;
	private String valueColumnName = "";
	private String descriptionColumnName = "";
	private List invisibleColumnNames = null;
	// private List treeLevelsColumns = null;
	// each entry of the list contains the name of the column to be considered as value column as first item, and the name of the column to be considered as
	// description column as second item
	private List<Couple<String, String>> treeLevelsColumns = null;

	private String lovType = "simple";

	private String datasetId;
	private String datasetLabel;

	public DatasetDetail() {
	}

	/**
	 * constructor.
	 *
	 * @param dataDefinition
	 *            xml representation of the script lov
	 *
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	public DatasetDetail(String dataDefinition) throws SourceBeanException {
		loadFromXML(dataDefinition);
	}

	/**
	 * @return the datasetId
	 */
	public String getDatasetId() {
		return datasetId;
	}

	/**
	 * @param datasetId
	 *            the datasetId to set
	 */
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * @return the datasetLabel
	 */
	public String getDatasetLabel() {
		return datasetLabel;
	}

	/**
	 * @param datasetLabel
	 *            the datasetLabel to set
	 */
	public void setDatasetLabel(String datasetLabel) {
		this.datasetLabel = datasetLabel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#toXML()
	 */
	@Override
	public String toXML() {
		// String XML = "<DATASET>" + "<ID>" + this.getDatasetId() + "</ID>" + "<LABEL>" + this.getDatasetLabel() + "</LABEL>" + "<VALUE-COLUMN>"
		// + this.getValueColumnName() + "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + this.getDescriptionColumnName() + "</DESCRIPTION-COLUMN>"
		// + "<VISIBLE-COLUMNS>" + GeneralUtilities.fromListToString(this.getVisibleColumnNames(), ",") + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
		// + GeneralUtilities.fromListToString(this.getInvisibleColumnNames(), ",") + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + this.getLovType()
		// + "</LOVTYPE>" + "<TREE-LEVELS-COLUMNS>" + GeneralUtilities.fromListToString(this.getTreeLevelsColumns(), ",") + "</TREE-LEVELS-COLUMNS>"
		// + "</DATASET>";
		String XML = "<DATASET>" + "<ID>" + this.getDatasetId() + "</ID>" + "<LABEL>" + this.getDatasetLabel() + "</LABEL>" + "<VISIBLE-COLUMNS>"
				+ GeneralUtilities.fromListToString(this.getVisibleColumnNames(), ",") + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
				+ GeneralUtilities.fromListToString(this.getInvisibleColumnNames(), ",") + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + this.getLovType()
				+ "</LOVTYPE>";
		if (this.isSimpleLovType()) {
			XML += "<VALUE-COLUMN>" + valueColumnName + "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + descriptionColumnName + "</DESCRIPTION-COLUMN>";
		} else {
			XML += "<VALUE-COLUMNS>" + GeneralUtilities.fromListToString(this.getTreeValueColumns(), ",") + "</VALUE-COLUMNS>" + "<DESCRIPTION-COLUMNS>"
					+ GeneralUtilities.fromListToString(this.getTreeDescriptionColumns(), ",") + "</DESCRIPTION-COLUMNS>";
		}
		XML += "</DATASET>";
		return XML;
	}

	/**
	 * loads the lov from an xml string.
	 *
	 * @param dataDefinition
	 *            the xml definition of the lov
	 *
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	@Override
	public void loadFromXML(String dataDefinition) throws SourceBeanException {
		logger.debug("IN");
		dataDefinition.trim();
		if (dataDefinition.indexOf("<ID>") != -1) {
			int startInd = dataDefinition.indexOf("<ID>");
			int endId = dataDefinition.indexOf("</ID>");
			String dataset = dataDefinition.substring(startInd + 6, endId);
			dataset = dataset.trim();
			if (!dataset.startsWith("<![CDATA[")) {
				dataset = "<![CDATA[" + dataset + "]]>";
				dataDefinition = dataDefinition.substring(0, startInd + 6) + dataset + dataDefinition.substring(endId);
			}
		}

		SourceBean source = SourceBean.fromXMLString(dataDefinition);
		SourceBean idBean = (SourceBean) source.getAttribute("ID");
		String id = idBean.getCharacters();
		SourceBean labelBean = (SourceBean) source.getAttribute("LABEL");
		String label = labelBean.getCharacters();
		SourceBean valCol = (SourceBean) source.getAttribute("VALUE-COLUMN");
		String valueColumn = valCol.getCharacters();
		SourceBean visCol = (SourceBean) source.getAttribute("VISIBLE-COLUMNS");
		String visibleColumns = visCol.getCharacters();
		SourceBean invisCol = (SourceBean) source.getAttribute("INVISIBLE-COLUMNS");
		String invisibleColumns = "";
		// compatibility control (versions till 1.9RC does not have invisible columns definition)
		if (invisCol != null) {
			invisibleColumns = invisCol.getCharacters();
			if (invisibleColumns == null) {
				invisibleColumns = "";
			}
		}
		SourceBean descCol = (SourceBean) source.getAttribute("DESCRIPTION-COLUMN");
		String descriptionColumn = null;
		// compatibility control (versions till 1.9.1 does not have description columns definition)
		if (descCol != null) {
			descriptionColumn = descCol.getCharacters();
			if (descriptionColumn == null) {
				descriptionColumn = valueColumn;
			}
		} else
			descriptionColumn = valueColumn;

		// compatibility control (versions till 3.6 does not have TREE-LEVELS-COLUMN definition)
		// SourceBean treeLevelsColumnsBean = (SourceBean) source.getAttribute("TREE-LEVELS-COLUMNS");
		// String treeLevelsColumnsString = null;
		// if (treeLevelsColumnsBean != null) {
		// treeLevelsColumnsString = treeLevelsColumnsBean.getCharacters();
		// }
		// if ((treeLevelsColumnsString != null) && !treeLevelsColumnsString.trim().equalsIgnoreCase("")) {
		// String[] treeLevelsColumnArr = treeLevelsColumnsString.split(",");
		// this.treeLevelsColumns = Arrays.asList(treeLevelsColumnArr);
		// }
		try {
			SourceBean treeLevelsColumnsBean = (SourceBean) source.getAttribute("TREE-LEVELS-COLUMNS");
			if (treeLevelsColumnsBean != null) {
				// compatibility control (versions till 5.1.0 does not have
				// VALUE-COLUMNS and DESCRIPTION-COLUMNS definition)
				String treeLevelsColumnsString = treeLevelsColumnsBean.getCharacters();
				String[] treeLevelsColumnArr = treeLevelsColumnsString.split(",");
				List<Couple<String, String>> levelsMap = new ArrayList<Couple<String, String>>();
				for (int i = 0; i < treeLevelsColumnArr.length; i++) {
					String aValueColumn = treeLevelsColumnArr[i];
					if (i == treeLevelsColumnArr.length - 1) {
						levelsMap.add(new Couple<String, String>(aValueColumn, descriptionColumn));
					} else {
						levelsMap.add(new Couple<String, String>(aValueColumn, aValueColumn));
					}
				}
				this.setValueColumnName(null);
				this.setDescriptionColumnName(null);
			} else {
				SourceBean valuesColumnsBean = (SourceBean) source.getAttribute("VALUE-COLUMNS");
				SourceBean descriptionColumnsBean = (SourceBean) source.getAttribute("DESCRIPTION-COLUMNS");
				if (valuesColumnsBean != null) {

					Assert.assertTrue(descriptionColumnsBean != null, "DESCRIPTION-COLUMNS tag not defined");

					List<Couple<String, String>> levelsMap = new ArrayList<Couple<String, String>>();
					String valuesColumnsStr = valuesColumnsBean.getCharacters();
					logger.debug("VALUE-COLUMNS is [" + valuesColumnsStr + "]");
					String descriptionColumnsStr = descriptionColumnsBean.getCharacters();
					logger.debug("DESCRIPTION-COLUMNS is [" + descriptionColumnsStr + "]");
					String[] valuesColumns = valuesColumnsStr.split(",");
					String[] descriptionColumns = descriptionColumnsStr.split(",");
					List<String> valuesColumnsList = Arrays.asList(valuesColumns);
					List<String> descriptionColumnsList = Arrays.asList(descriptionColumns);

					Assert.assertTrue(valuesColumnsList.size() == descriptionColumnsList.size(),
							"Value columns list and description columns list must have the same length");

					for (int i = 0; i < valuesColumnsList.size(); i++) {
						String aValueColumn = valuesColumnsList.get(i);
						String aDescriptionColumn = descriptionColumnsList.get(i);
						levelsMap.add(new Couple<String, String>(aValueColumn, aDescriptionColumn));
					}
					this.treeLevelsColumns = levelsMap;
				}
			}
		} catch (Exception e) {
			logger.error("Error while reading LOV definition from XML", e);
			throw new SpagoBIRuntimeException("Error while reading LOV definition from XML", e);
		}
		SourceBean lovTypeBean = (SourceBean) source.getAttribute("LOVTYPE");
		String lovType;
		if (lovTypeBean != null) {
			lovType = lovTypeBean.getCharacters();
			this.lovType = lovType;
		}

		setDatasetId(id);
		setDatasetLabel(label);
		setValueColumnName(valueColumn);
		setDescriptionColumnName(descriptionColumn);
		List visColNames = new ArrayList();
		if ((visibleColumns != null) && !visibleColumns.trim().equalsIgnoreCase("")) {
			String[] visColArr = visibleColumns.split(",");
			visColNames = Arrays.asList(visColArr);
		}
		setVisibleColumnNames(visColNames);
		List invisColNames = new ArrayList();
		if ((invisibleColumns != null) && !invisibleColumns.trim().equalsIgnoreCase("")) {
			String[] invisColArr = invisibleColumns.split(",");
			invisColNames = Arrays.asList(invisColArr);
		}
		setInvisibleColumnNames(invisColNames);
		logger.debug("OUT");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getLovResult(it.eng.spago.security.IEngUserProfile, java.util.List,
	 * it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance)
	 */
	@Override
	public String getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters, Locale locale)
			throws Exception {
		// gets the dataset object informations
		IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(new Integer(getDatasetId()));
		Map<String, String> params = getParametersNameToValueMap(BIObjectParameters);
		if (params == null) {
			dataset.setParamsMap(new HashMap<String, String>());
		} else {
			dataset.setParamsMap(params);
		}
		Map parameters = new HashMap();
		dataset.setParamsMap(parameters);
		dataset.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
		dataset.loadData();
		IDataStore ids = dataset.getDataStore();

		String resultXml = ids.toXml();
		return resultXml;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#requireProfileAttributes()
	 */
	@Override
	public boolean requireProfileAttributes() throws Exception {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getProfileAttributeNames()
	 */
	@Override
	public List getProfileAttributeNames() throws Exception {
		// Empty List because Profile Attributes are managed inside the Dataset logic
		return new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getVisibleColumnNames()
	 */
	@Override
	public List getVisibleColumnNames() {
		return visibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getInvisibleColumnNames()
	 */
	@Override
	public List getInvisibleColumnNames() {
		return invisibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getDescriptionColumnName()
	 */
	@Override
	public String getDescriptionColumnName() {
		return descriptionColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setVisibleColumnNames(java.util.List)
	 */
	@Override
	public void setVisibleColumnNames(List visibleColumnNames) {
		this.visibleColumnNames = visibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setInvisibleColumnNames(java.util.List)
	 */
	@Override
	public void setInvisibleColumnNames(List invisibleColumnNames) {
		this.invisibleColumnNames = invisibleColumnNames;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setDescriptionColumnName(java.lang.String)
	 */
	@Override
	public void setDescriptionColumnName(String descriptionColumnName) {
		this.descriptionColumnName = descriptionColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getValueColumnName()
	 */
	@Override
	public String getValueColumnName() {
		return valueColumnName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setValueColumnName(java.lang.String)
	 */
	@Override
	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	/**
	 * Splits an XML string by using some <code>SourceBean</code> object methods in order to obtain the source <code>DatasetDetail</code> objects whom XML has
	 * been built.
	 *
	 * @param dataDefinition
	 *            The XML input String
	 *
	 * @return The corrispondent <code>DatasetDetail</code> object
	 *
	 * @throws SourceBeanException
	 *             If a SourceBean Exception occurred
	 */
	public static DatasetDetail fromXML(String dataDefinition) throws SourceBeanException {
		return new DatasetDetail(dataDefinition);
	}

	@Override
	public String getLovType() {
		return lovType;
	}

	@Override
	public void setLovType(String lovType) {
		this.lovType = lovType;
	}

	// @Override
	// public List getTreeLevelsColumns() {
	// return treeLevelsColumns;
	// }
	//
	// @Override
	// public void setTreeLevelsColumns(List treeLevelsColumns) {
	// this.treeLevelsColumns = treeLevelsColumns;
	// }
	@Override
	public List<Couple<String, String>> getTreeLevelsColumns() {
		return treeLevelsColumns;
	}

	@Override
	public void setTreeLevelsColumns(List<Couple<String, String>> treeLevelsColumns) {
		this.treeLevelsColumns = treeLevelsColumns;
	}

	@Override
	public Set<String> getParameterNames() throws Exception {
		// Empty List because Profile Attributes are managed inside the Dataset logic
		return new HashSet<String>();
	}

}
