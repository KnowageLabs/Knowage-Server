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
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Defines the <code>JavaClassDetail</code> objects. This object is used to store JavaClass Wizard detail information.
 */
public class JavaClassDetail extends DependenciesPostProcessingLov implements ILovDetail {

	/**
	 * name of the class which return the data
	 */
	private String javaClassName = "";
	private List visibleColumnNames = null;
	private String valueColumnName = "";
	private String descriptionColumnName = "";
	private List invisibleColumnNames = null;
	private List treeLevelsColumns = null;
	private String lovType = "simple";

	/**
	 * constructor.
	 */
	public JavaClassDetail() {
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
	public JavaClassDetail(String dataDefinition) throws SourceBeanException {
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
		// build the sourcebean
		SourceBean source = SourceBean.fromXMLString(dataDefinition);
		// get and set the java class name
		SourceBean javaClassNameSB = (SourceBean) source.getAttribute("JAVA_CLASS_NAME");
		String javaClassName = javaClassNameSB.getCharacters();
		setJavaClassName(javaClassName);
		// get and set value column
		String valueColumn = "";
		SourceBean valCol = (SourceBean) source.getAttribute("VALUE-COLUMN");
		if (valCol != null)
			valueColumn = valCol.getCharacters();
		setValueColumnName(valueColumn);
		// get and set the description column
		String descrColumn = "";
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
	}

	/**
	 * serialize the lov to an xml string.
	 *
	 * @return the serialized xml string
	 */
	@Override
	public String toXML() {
		String XML = "<JAVACLASSLOV>" + "<JAVA_CLASS_NAME>" + this.getJavaClassName() + "</JAVA_CLASS_NAME>" + "<VALUE-COLUMN>" + this.getValueColumnName()
				+ "</VALUE-COLUMN>" + "<DESCRIPTION-COLUMN>" + this.getDescriptionColumnName() + "</DESCRIPTION-COLUMN>" + "<VISIBLE-COLUMNS>"
				+ SpagoBIUtilities.fromListToString(this.getVisibleColumnNames(), ",") + "</VISIBLE-COLUMNS>" + "<INVISIBLE-COLUMNS>"
				+ SpagoBIUtilities.fromListToString(this.getInvisibleColumnNames(), ",") + "</INVISIBLE-COLUMNS>" + "<LOVTYPE>" + this.getLovType()
				+ "</LOVTYPE>" + "<TREE-LEVELS-COLUMNS>" + GeneralUtilities.fromListToString(this.getTreeLevelsColumns(), ",") + "</TREE-LEVELS-COLUMNS>"
				+ "</JAVACLASSLOV>";
		return XML;
	}

	/**
	 * @see it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail#getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, ExecutionInstance
	 *      executionInstance) throws Exception;
	 */
	@Override
	public String getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters, Locale locale)
			throws Exception {
		IJavaClassLov javaClassLov = createClassInstance();
		if (javaClassLov instanceof AbstractJavaClassLov) {
			((AbstractJavaClassLov) javaClassLov).setBIObjectParameter(BIObjectParameters);

		}
		String result = javaClassLov.getValues(profile);
		result = result.trim();
		// check if the result must be converted into the right xml sintax
		boolean toconvert = checkSintax(result);
		if (toconvert) {
			result = convertResult(result);
		}
		return result;
	}

	/**
	 * checks if the result is formatted in the right xml structure
	 * 
	 * @param result
	 *            the result of the lov
	 * @return true if the result is formatted correctly false otherwise
	 * @throws EMFUserError
	 */
	public boolean checkSintax(String result) throws EMFUserError {
		return JavaClassUtils.checkSintax(result);
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
		IJavaClassLov javaClassLov = createClassInstance();
		List attrNames = javaClassLov.getNamesOfProfileAttributeRequired();
		return attrNames;
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
		boolean requires = false;
		IJavaClassLov javaClassLov = createClassInstance();
		List attrNames = javaClassLov.getNamesOfProfileAttributeRequired();
		if (attrNames.size() != 0) {
			requires = true;
		}
		return requires;
	}

	/**
	 * Creates and returns an instance of the lov class
	 * 
	 * @return instance of the lov class which must implement IJavaClassLov interface
	 * @throws EMFUserError
	 */
	private IJavaClassLov createClassInstance() throws EMFUserError {
		String javaClassName = getJavaClassName();
		if (javaClassName == null || javaClassName.trim().equals("")) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getLovResult", "The java class name is not specified");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1071");
		}
		IJavaClassLov javaClassLov = null;
		Class javaClass = null;
		try {
			javaClass = Class.forName(javaClassName);
		} catch (ClassNotFoundException e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getLovResult", "Java class '" + javaClassName + "' not found!!");
			List pars = new ArrayList();
			pars.add(javaClassName);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1072", pars);
		}
		try {
			javaClassLov = (IJavaClassLov) javaClass.newInstance();
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getLovResult", "Error while instatiating Java class '"
					+ javaClassName + "'.");
			List pars = new ArrayList();
			pars.add(javaClassName);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1073", pars);
		}
		return javaClassLov;
	}

	/**
	 * Wraps the result of the query execution into the right xml structure
	 * 
	 * @param result
	 *            the result of the query (which is not formatted with the right xml structure)
	 * @return the xml structure of the result
	 */
	public String convertResult(String result) {
		return JavaClassUtils.convertResult(result);
	}

	/**
	 * Gets the class name.
	 *
	 * @return the complete name of the class
	 */
	public String getJavaClassName() {
		return javaClassName;
	}

	/**
	 * Sets the class name.
	 *
	 * @param javaClassName
	 *            the complete name of the class
	 */
	public void setJavaClassName(String javaClassName) {
		this.javaClassName = javaClassName;
	}

	/**
	 * Builds a JavaClassDetail starting from ax xml representation.
	 *
	 * @param dataDefinition
	 *            the data definition
	 *
	 * @return The JavaClassDetail object
	 *
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	public static JavaClassDetail fromXML(String dataDefinition) throws SourceBeanException {
		JavaClassDetail jcd = new JavaClassDetail();
		jcd.loadFromXML(dataDefinition);
		return jcd;
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

	@Override
	public Set<String> getParameterNames() throws Exception {
		return null;
	}

}
