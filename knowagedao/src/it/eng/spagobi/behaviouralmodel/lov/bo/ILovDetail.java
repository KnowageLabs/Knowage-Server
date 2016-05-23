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

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Defines methods to manage a lov
 */
public interface ILovDetail extends Serializable {

	/**
	 * serialize the lov to an xml string.
	 *
	 * @return the serialized xml string
	 */
	public String toXML();

	/**
	 * loads the lov from an xml string.
	 *
	 * @param dataDefinition
	 *            the xml definition of the lov
	 *
	 * @throws SourceBeanException
	 *             the source bean exception
	 */
	public void loadFromXML(String dataDefinition) throws SourceBeanException;

	/**
	 * Returns the result of the lov for a given user and for a particular execution instance with the given dependencies (optional).
	 *
	 * @param profile
	 *            the profile of the user
	 * @param dependencies
	 *            the dependencies to be considered for the lov. It may be null, since when testing a lov, there is no correlation.
	 * @param BIObjectParameters
	 *            used by QueryDetail implementation.
	 * @param locale
	 *            used by ScriptDetail implementation.
	 *
	 * @return the string result of the lov
	 *
	 * @throws Exception
	 *             the exception
	 */
	public String getLovResult(IEngUserProfile profile, List<ObjParuse> dependencies, List<BIObjectParameter> BIObjectParameters, Locale locale)
			throws Exception;

	/**
	 * Checks if the lov requires one or more profile attributes.
	 *
	 * @return true if the lov require one or more profile attributes, false otherwise
	 *
	 * @throws Exception
	 *             the exception
	 */
	public boolean requireProfileAttributes() throws Exception;

	/**
	 * Gets the list of names of the profile attributes required.
	 *
	 * @return list of profile attribute names
	 *
	 * @throws Exception
	 *             the exception
	 */
	public List getProfileAttributeNames() throws Exception;

	/**
	 * Gets the set of names of the parameters required.
	 *
	 * @return set of parameter names
	 *
	 * @throws Exception
	 *             the exception
	 */
	public Set<String> getParameterNames() throws Exception;

	/**
	 * Gets the visible column names.
	 *
	 * @return the visible column names
	 *
	 * @throws Exception
	 *             the exception
	 */
	public List getVisibleColumnNames() throws Exception;

	/**
	 * Gets the invisible column names.
	 *
	 * @return the invisible column names
	 *
	 * @throws Exception
	 *             the exception
	 */
	public List getInvisibleColumnNames() throws Exception;

	/**
	 * Gets the value column name.
	 *
	 * @return the value column name
	 *
	 * @throws Exception
	 *             the exception
	 */
	public String getValueColumnName() throws Exception;

	/**
	 * Gets the description column name.
	 *
	 * @return the description column name
	 *
	 * @throws Exception
	 *             the exception
	 */
	public String getDescriptionColumnName() throws Exception;

	/**
	 * Gets the type of the lov. Now the available types are: simple, tree
	 *
	 * @return name the type of the lov
	 *
	 * @throws Exception
	 *             the exception
	 */
	public String getLovType();

	public boolean isSimpleLovType();

	/**
	 * Gets the levels names of the tree lov
	 *
	 * @return treeLevelColumns levels names of the tree lov
	 *
	 * @throws Exception
	 *             the exception
	 */
	public List getTreeLevelsColumns() throws Exception;

	/**
	 * Sets the visible column names.
	 *
	 * @param visCols
	 *            the new visible column names
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void setVisibleColumnNames(List visCols) throws Exception;

	/**
	 * Sets the invisible column names.
	 *
	 * @param invisCols
	 *            the new invisible column names
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void setInvisibleColumnNames(List invisCols) throws Exception;

	/**
	 * Sets the value column name.
	 *
	 * @param name
	 *            the new value column name
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void setValueColumnName(String name) throws Exception;

	/**
	 * Sets the description column name.
	 *
	 * @param name
	 *            the new description column name
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void setDescriptionColumnName(String name) throws Exception;

	/**
	 * Sets the type of the lov. Now the available types are: simple, tree
	 *
	 * @param name
	 *            the type of the lov
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void setLovType(String name) throws Exception;

	/**
	 * Sets the levels names of the tree lov
	 *
	 * @param treeLevelColumns
	 *            levels names of the tree lov
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void setTreeLevelsColumns(List treeLevelColumns) throws Exception;

}
