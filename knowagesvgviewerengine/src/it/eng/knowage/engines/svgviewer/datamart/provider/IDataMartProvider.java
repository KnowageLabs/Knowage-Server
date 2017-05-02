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
package it.eng.knowage.engines.svgviewer.datamart.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.component.ISvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.dataset.DataMart;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.spago.base.SourceBean;

import java.util.Set;

public interface IDataMartProvider extends ISvgViewerEngineComponent {

	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 *
	 * @throws SvgViewerEngineRuntimeException
	 *             the svgviewer engine exception
	 */
	DataMart getDataMart() throws SvgViewerEngineRuntimeException;

	/**
	 * Gets the data details.
	 *
	 * @param filterValue
	 *            the filter value
	 *
	 * @return the data details
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	SourceBean getDataDetails(String filterValue) throws SvgViewerEngineException;

	/**
	 * Sets the selected hierarchy name.
	 *
	 * @param hierarchyName
	 *            the new selected hierarchy name
	 */
	void setSelectedHierarchyName(String hierarchyName);

	/**
	 * Gets the selected hierarchy name.
	 *
	 * @return the selected hierarchy name
	 */
	String getSelectedHierarchyName();

	/**
	 * Sets the selected level.
	 *
	 * @param level
	 *            the new selected level
	 */
	void setSelectedLevel(String level);

	/**
	 * Gets the selected level.
	 *
	 * @return the selected level
	 */
	String getSelectedLevel();

	/**
	 * Gets the hierarchy member names.
	 *
	 * @return the hierarchy member names
	 */
	Set getHierarchyMembersNames();

	/**
	 * Gets the hierarchy.
	 *
	 * @param name
	 *            the name
	 *
	 * @return the hierarchy
	 */
	Hierarchy getHierarchy(String name);

	/**
	 * Gets the selected hierarchy.
	 *
	 * @return the selected hierarchy
	 */
	Hierarchy getSelectedHierarchy();

	// /**
	// * Gets the selected level.
	// *
	// * @return the selected level
	// */
	// Hierarchy.Level getSelectedLevel();

	/**
	 * Sets the selected member name.
	 *
	 * @param memberName
	 *            the new selected member name
	 */
	void setSelectedMemberName(String memberName);

	/**
	 * Gets the selected member name.
	 *
	 * @return the selected member name
	 */
	String getSelectedMemberName();

	/**
	 * Gets the specific hierarchy member.
	 *
	 * @param name
	 *            the name
	 *
	 * @return the hierarchy member
	 */
	HierarchyMember getHierarchyMember(String name);

	/**
	 * Sets the selected member info.
	 *
	 * @param memberName
	 *            the new selected member info
	 */
	void setSelectedMemberInfo(String info);

	/**
	 * Gets the selected member info.
	 *
	 * @return the selected member info
	 */
	String getSelectedMemberInfo();
}
