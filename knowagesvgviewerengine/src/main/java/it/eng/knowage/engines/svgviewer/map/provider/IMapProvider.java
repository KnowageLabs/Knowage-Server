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
package it.eng.knowage.engines.svgviewer.map.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.component.ISvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.svg.SVGDocument;

/**
 * The Interface IMapProvider.
 *
 */
public interface IMapProvider extends ISvgViewerEngineComponent {

	/**
	 * Gets the sVG map stream reader.
	 *
	 * @return the sVG map stream reader
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	XMLStreamReader getSVGMapStreamReader() throws SvgViewerEngineException;

	/**
	 * Gets the sVG map stream reader.
	 *
	 * @param mapName
	 *            the map name
	 *
	 * @return the sVG map stream reader
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	XMLStreamReader getSVGMapStreamReader(String mapName) throws SvgViewerEngineException;

	/**
	 * Gets the sVG map dom document.
	 *
	 * @return the sVG map dom document
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	SVGDocument getSVGMapDOMDocument() throws SvgViewerEngineRuntimeException;

	/**
	 * Gets the sVG map dom document.
	 *
	 * @param mapName
	 *            the map name
	 *
	 * @return the sVG map dom document
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	SVGDocument getSVGMapDOMDocument(String mapName) throws SvgViewerEngineException;

	/**
	 * Gets the sVG map dom document.
	 *
	 * @param member
	 *            the active member
	 *
	 * @return the sVG map dom document
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	SVGDocument getSVGMapDOMDocument(HierarchyMember member) throws SvgViewerEngineRuntimeException;

	/**
	 * Gets the selected map name.
	 *
	 * @return the selected map name
	 */
	String getSelectedMapName();

	/**
	 * Sets the selected map name.
	 *
	 * @param mapName
	 *            the new selected map name
	 */
	void setSelectedMapName(String mapName);

	/**
	 * Gets the selected member.
	 */
	HierarchyMember getSelectedHierarchyMember();

	/**
	 * Gets the map names by feature.
	 *
	 * @param featureName
	 *            the feature name
	 *
	 * @return the map names by feature
	 *
	 * @throws Exception
	 *             the exception
	 */
	List getMapNamesByFeature(String featureName) throws Exception;

	/**
	 * Gets the feature names in map.
	 *
	 * @param mapName
	 *            the map name
	 *
	 * @return the feature names in map
	 *
	 * @throws Exception
	 *             the exception
	 */
	List getFeatureNamesInMap(String mapName) throws Exception;

	/**
	 * Gets default map from the MapProvider
	 *
	 * @return the default map name
	 *
	 * @throws Exception
	 *             the exception
	 */
	String getDefaultMapName() throws Exception;
}
