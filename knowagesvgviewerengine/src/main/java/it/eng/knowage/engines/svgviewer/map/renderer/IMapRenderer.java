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
package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.component.ISvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.datamart.provider.IDataMartProvider;
import it.eng.knowage.engines.svgviewer.map.provider.IMapProvider;

import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMapRenderer.
 *
 * @author Andrea Gioia
 */
public interface IMapRenderer extends ISvgViewerEngineComponent {

	/**
	 * Render map.
	 *
	 * @param mapProvider
	 *            the map provider
	 * @param datamartProvider
	 *            the datamart provider
	 * @param outputFormat
	 *            the output format
	 *
	 * @return the file
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	public File renderMap(IMapProvider mapProvider, IDataMartProvider datamartProvider, String outputFormat) throws SvgViewerEngineRuntimeException;

	/**
	 * Render map.
	 *
	 * @param mapProvider
	 *            the map provider
	 * @param datamartProvider
	 *            the datamart provider
	 *
	 * @return the file
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	File renderMap(IMapProvider mapProvider, IDataMartProvider datamartProvider) throws SvgViewerEngineException;

	/**
	 * Gets the layer names.
	 *
	 * @return the layer names
	 */
	public String[] getLayerNames();

	/**
	 * Gets the layer.
	 *
	 * @param layerName
	 *            the layer name
	 *
	 * @return the layer
	 */
	public Layer getLayer(String layerName);

	/**
	 * Adds the layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public void addLayer(Layer layer);

	/**
	 * Clear layers.
	 */
	void clearLayers();

	void setSelectedMeasureName(String selectedMeasureName);
}
