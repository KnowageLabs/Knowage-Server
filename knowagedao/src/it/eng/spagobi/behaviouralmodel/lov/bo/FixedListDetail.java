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
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Defines method to manage lov of fixed list type
 */
public class FixedListDetail extends DependenciesPostProcessingLov implements ILovDetail {

	/**
	 * items of the list
	 */
	List items = new ArrayList();

	private List visibleColumnNames = null;
	private String valueColumnName = "VALUE";
	private String descriptionColumnName = "DESCRIPTION";
	private List invisibleColumnNames = null;
	private List treeLevelsColumns = null;
	private String lovType = "simple";

	/**
	 * constructor.
	 */
	public FixedListDetail() {
		visibleColumnNames = new ArrayList();
		visibleColumnNames.add("DESCRIPTION");
		invisibleColumnNames = new ArrayList();
		invisibleColumnNames.add("VALUE");
	}

	/**
	 * constructor.
	 *
	 * @param dataDefinition
	 *            the data definition
	 *
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	public FixedListDetail(String dataDefinition) throws SourceBeanException {
		loadFromXML(dataDefinition);
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
		dataDefinition.trim();
		SourceBean source = SourceBean.fromXMLString(dataDefinition);
		if (!source.getName().equals("FIXLISTLOV")) {
			SourceBean wrapper = new SourceBean("FIXLISTLOV");
			wrapper.setAttribute(source);
			source = wrapper;
		}
		// load data from xml
		List listRows = source.getAttributeAsList("ROWS.ROW");
		Iterator iterRows = listRows.iterator();
		ArrayList lovList = new ArrayList();
		while (iterRows.hasNext()) {
			FixedListItemDetail lov = new FixedListItemDetail();
			SourceBean element = (SourceBean) iterRows.next();
			String value = (String) element.getAttribute("VALUE");
			// ******** only for retro compatibility
			if (value == null)
				value = (String) element.getAttribute("NAME");
			// *************************************
			lov.setValue(value);
			String description = (String) element.getAttribute("DESCRIPTION");
			lov.setDescription(description);
			lovList.add(lov);
		}
		setLovs(lovList);

		// get and set value column
		String valueColumn = "VALUE";
		SourceBean valCol = (SourceBean) source.getAttribute("VALUE-COLUMN");
		if (valCol != null)
			valueColumn = valCol.getCharacters();
		setValueColumnName(valueColumn);
		// get and set the description column
		String descrColumn = "DESCRIPTION";
		SourceBean descColSB = (SourceBean) source.getAttribute("DESCRIPTION-COLUMN");
		if (descColSB != null)
			descrColumn = descColSB.getCharacters();
		setDescriptionColumnName(descrColumn);
		// get and set list of visible columns
		List visColNames = new ArrayList();
		SourceBean visColSB = (SourceBean) source.getAttribute("VISIBLE-COLUMNS");
		if (visColSB != null) {
			String visColConc = visColSB.getCharacters();
			if ((visColConc != null) && !visColConc.trim().equalsIgnoreCase("")) {
				String[] visColArr = visColConc.split(",");
				visColNames = Arrays.asList(visColArr);
			}
		}
		setVisibleColumnNames(visColNames);
		// get and set list of invisible columns
		List invisColNames = new ArrayList();
		SourceBean invisColSB = (SourceBean) source.getAttribute("INVISIBLE-COLUMNS");
		if (invisColSB != null) {
			String invisColConc = invisColSB.getCharacters();
			if ((invisColConc != null) && !invisColConc.trim().equalsIgnoreCase("")) {
				String[] invisColArr = invisColConc.split(",");
				invisColNames = Arrays.asList(invisColArr);
			}
		}
		setInvisibleColumnNames(invisColNames);

		// compatibility control (versions till 3.6 does not have TREE-LEVELS-COLUMN definition)
		SourceBean treeLevelsColumnsBean = (SourceBean) source.getAttribute("TREE-LEVELS-COLUMNS");
		String treeLevelsColumnsString = null;
		if (treeLevelsColumnsBean != null) {
			treeLevelsColumnsString = treeLevelsColumnsBean.getCharacters();
		}
		if ((treeLevelsColumnsString != null) && !treeLevelsColumnsString.trim().equalsIgnoreCase("")) {
			String[] treeLevelsColumnArr = treeLevelsColumnsString.split(",");
			this.treeLevelsColumns = Arrays.asList(treeLevelsColumnArr);
		}
		SourceBean lovTypeBean = (SourceBean) source.getAttribute("LOVTYPE");
		String lovType;
		if (lovTypeBean != null) {
			lovType = lovTypeBean.getCharacters();
			this.lovType = lovType;
		}

		// // set visible and invisible columns
		// List visColList = new ArrayList();
		// visColList.add("DESCRIPTION");
		// List invisColList = new ArrayList();
		// invisColList.add("VALUE");
		// setInvisibleColumnNames(invisColList);
		// setVisibleColumnNames(visColList);
	}

	/**
	 * serialize the lov to an xml string.
	 *
	 * @return the serialized xml string
	 */
	@Override
	public String toXML() {
		String lovXML = "";
		lovXML += "<FIXLISTLOV>";
		lovXML += "<ROWS>";
		FixedListItemDetail lov = null;
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			lov = (FixedListItemDetail) iter.next();
			String value = lov.getValue();
			String description = lov.getDescription();
			lovXML += "<ROW" + " VALUE=\"" + value + "\"" + " DESCRIPTION=\"" + description + "\"" + "/>";
		}
		lovXML += "</ROWS>";
		lovXML += "<VALUE-COLUMN>" + valueColumnName + "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + descriptionColumnName + "</DESCRIPTION-COLUMN>"
				+ "<VISIBLE-COLUMNS>" + GeneralUtilities.fromListToString(visibleColumnNames, ",") + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
				+ GeneralUtilities.fromListToString(invisibleColumnNames, ",") + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + this.getLovType() + "</LOVTYPE>"
				+ "<TREE-LEVELS-COLUMNS>" + GeneralUtilities.fromListToString(this.getTreeLevelsColumns(), ",") + "</TREE-LEVELS-COLUMNS>" + "</FIXLISTLOV>";
		return lovXML;
	}

	/**
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, ExecutionInstance
	 *      executionInstance) throws Exception;
	 */
	@Override
	public String getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters, Locale locale)
			throws Exception {
		String lovResult = "<ROWS>";
		FixedListItemDetail lov = null;
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			lov = (FixedListItemDetail) iter.next();
			String value = lov.getValue();
			String description = lov.getDescription();
			lovResult += "<ROW" + " VALUE=\"" + value + "\"" + " DESCRIPTION=\"" + description + "\"" + "/>";
		}
		lovResult += "</ROWS>";
		lovResult = StringUtilities.substituteProfileAttributesInString(lovResult, profile);

		Map<String, String> params = getParametersNameToValueMap(BIObjectParameters);
		if (params != null && !params.isEmpty()) {
			Map<String, String> types = getParametersNameToTypeMap(BIObjectParameters);
			lovResult = StringUtilities.substituteParametersInString(lovResult, params, types, false);
		}

		return lovResult;
	}

	/**
	 * Gets the list of names of the profile attributes required.
	 *
	 * @return list of profile attribute names
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public List getProfileAttributeNames() throws Exception {
		List names = new ArrayList();
		String lovResult = this.toXML();
		while (lovResult.indexOf("${") != -1) {
			int startind = lovResult.indexOf("${");
			int endind = lovResult.indexOf("}", startind);
			String attributeDef = lovResult.substring(startind + 2, endind);
			if (attributeDef.indexOf("(") != -1) {
				int indroundBrack = lovResult.indexOf("(", startind);
				String nameAttr = lovResult.substring(startind + 2, indroundBrack);
				names.add(nameAttr);
			} else {
				names.add(attributeDef);
			}
			lovResult = lovResult.substring(endind);
		}
		return names;
	}

	/**
	 * Gets the set of names of the parameters required.
	 *
	 * @return set of parameter names
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public Set<String> getParameterNames() throws Exception {
		Set<String> names = new HashSet<String>();
		String lovResult = this.toXML();
		while (lovResult.indexOf(StringUtilities.START_PARAMETER) != -1) {
			int startind = lovResult.indexOf(StringUtilities.START_PARAMETER);
			int endind = lovResult.indexOf("}", startind);
			String parameterDef = lovResult.substring(startind + 3, endind);
			if (parameterDef.indexOf("(") != -1) {
				int indroundBrack = lovResult.indexOf("(", startind);
				String nameParam = lovResult.substring(startind + 3, indroundBrack);
				names.add(nameParam);
			} else {
				names.add(parameterDef);
			}
			lovResult = lovResult.substring(endind);
		}
		return names;
	}

	/**
	 * Checks if the lov requires one or more profile attributes.
	 *
	 * @return true if the lov require one or more profile attributes, false otherwise
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public boolean requireProfileAttributes() throws Exception {
		boolean contains = false;
		String lovResult = this.toXML();
		if (lovResult.indexOf("${") != -1) {
			contains = true;
		}
		return contains;
	}

	/**
	 * Adds a lov to the lov Detail List.
	 *
	 * @param description
	 *            The added lov description
	 * @param value
	 *            the value
	 */
	public void add(String value, String description) {
		// if name or description are empty don't add
		if ((value == null) || (value.trim().equals("")))
			return;
		if ((description == null) || (description.trim().equals("")))
			return;
		// if the element already exists don't add
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			FixedListItemDetail lovDet = (FixedListItemDetail) iter.next();
			if (value.equals(lovDet.getValue()) && description.equals(lovDet.getDescription())) {
				return;
			}
		}
		// add the item
		FixedListItemDetail lovdet = new FixedListItemDetail();
		lovdet.setValue(value);
		lovdet.setDescription(description);
		items.add(lovdet);
	}

	/**
	 * Deletes a lov from the lov Detail List.
	 *
	 * @param value
	 *            The deleted lov name
	 * @param description
	 *            The deleted lov description
	 */
	public void remove(String value, String description) {
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			FixedListItemDetail lovDet = (FixedListItemDetail) iter.next();
			if (value.equals(lovDet.getValue()) && description.equals(lovDet.getDescription())) {
				items.remove(lovDet);
				break;
			}
		}
	}

	/**
	 * Splits an XML string by using some <code>SourceBean</code> object methods in order to obtain the source <code>LovDetail</code> objects whom XML has been
	 * built.
	 *
	 * @param dataDefinition
	 *            The XML input String
	 *
	 * @return The corrispondent <code>LovDetailList</code> object
	 *
	 * @throws SourceBeanException
	 *             If a SourceBean Exception occurred
	 */
	public static FixedListDetail fromXML(String dataDefinition) throws SourceBeanException {
		return new FixedListDetail(dataDefinition);
	}

	/**
	 * Gets item of the fixed list.
	 *
	 * @return items of the fixed list
	 */
	public List getItems() {
		return items;
	}

	/**
	 * Sets items of the fixed list.
	 *
	 * @param items
	 *            the items to set
	 */
	public void setLovs(List items) {
		this.items = items;
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
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setDescriptionColumnName(java.lang.String)
	 */
	@Override
	public void setDescriptionColumnName(String descriptionColumnName) {
		this.descriptionColumnName = descriptionColumnName;
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
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setInvisibleColumnNames(java.util.List)
	 */
	@Override
	public void setInvisibleColumnNames(List invisibleColumnNames) {
		this.invisibleColumnNames = invisibleColumnNames;
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
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#setVisibleColumnNames(java.util.List)
	 */
	@Override
	public void setVisibleColumnNames(List visibleColumnNames) {
		this.visibleColumnNames = visibleColumnNames;
	}

	@Override
	public String getLovType() {
		return lovType;
	}

	@Override
	public void setLovType(String lovType) {
		this.lovType = lovType;
	}

	@Override
	public List getTreeLevelsColumns() {
		return treeLevelsColumns;
	}

	@Override
	public void setTreeLevelsColumns(List treeLevelsColumns) {
		this.treeLevelsColumns = treeLevelsColumns;
	}

}
