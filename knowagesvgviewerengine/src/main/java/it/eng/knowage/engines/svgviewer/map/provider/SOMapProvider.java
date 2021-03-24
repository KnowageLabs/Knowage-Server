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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.map.provider.configurator.SOMapProviderConfigurator;
import it.eng.knowage.engines.svgviewer.map.utils.SVGMapLoader;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.utilities.callbacks.mapcatalogue.MapCatalogueAccessUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * The Class SOMapProvider.
 */
public class SOMapProvider extends AbstractMapProvider {

	/** The map catalogue service proxy. */
	private MapCatalogueAccessUtils mapCatalogueServiceProxy;
	private SVGMapLoader svgMapLoader;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SOMapProvider.class);

	/**
	 * Instantiates a new sO map provider.
	 */
	public SOMapProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.AbstractMapProvider#init(java.lang.Object)
	 */
	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		super.init(conf);
		svgMapLoader = new SVGMapLoader();
		SOMapProviderConfigurator.configure(this, getConf());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.AbstractMapProvider#getSVGMapDOMDocument(java.lang.String)
	 */
	@Override
	public SVGDocument getSVGMapDOMDocument(String mapName) throws SvgViewerEngineRuntimeException {
		logger.debug("IN.mapName=" + mapName);
		SVGDocument svgDocument = null;
		Content map = null;

		try {
			map = mapCatalogueServiceProxy.readMap(mapName);
		} catch (Exception e) {
			logger.error("An error occurred while invoking mapCatalogueService method: readMap()");
			throw new SvgViewerEngineRuntimeException("Impossible to load map from map catalogue", e);
		}

		try {

			svgDocument = svgMapLoader.loadMapAsDocument(map);
		} catch (IOException e) {
			logger.error("Impossible to load map from map catalogue");
			throw new SvgViewerEngineRuntimeException("Impossible to load map from map catalogue", e);
		}

		logger.debug("OUT");

		return svgDocument;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.AbstractMapProvider#getSVGMapDOMDocument(java.lang.String)
	 */
	@Override
	public SVGDocument getSVGMapDOMDocument(HierarchyMember member) throws SvgViewerEngineRuntimeException {
		logger.debug("IN.memberName=" + member.getName());
		SVGDocument svgDocument = null;
		Content map = null;

		try {
			GeoMap drilledMap = DAOFactory.getSbiGeoMapsDAO().loadMapByHierarchyKey(member.getHierarchy(), member.getName(), member.getLevel().toString());
			if (drilledMap == null) {
				logger.error("SVG with hierarchyName [" + member.getHierarchy() + "], memberName [" + member.getName() + "] and level [" + member.getLevel()
						+ "] doesn't exist into the Map Catalogue.");
				// throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException("", getEngineInstance(), null);
				throw new SpagoBIServiceException("DrillMap", "SVG with hierarchyName [" + member.getHierarchy() + "], memberName [" + member.getName()
						+ "] and level [" + member.getLevel() + "] doesn't exist into the Map Catalogue.");
			}

			map = new Content();

			byte[] template = DAOFactory.getBinContentDAO().getBinContent(drilledMap.getBinId());

			if (template == null) {
				logger.info("Template map is empty. Try uploadyng the svg.");
				return null;
			}
			Base64.Encoder bASE64Encoder = Base64.getEncoder();
			map.setContent(bASE64Encoder.encodeToString(template));
			logger.debug("template read");
			map.setFileName(member.getName() + ".svg");

		} catch (Exception e) {
			logger.error("An error occurred while invoking mapCatalogueService method: readMap()");
			throw new SvgViewerEngineRuntimeException("Impossible to load map from map catalogue", e);
		}

		try {

			svgDocument = svgMapLoader.loadMapAsDocument(map);
		} catch (IOException e) {
			logger.error("Impossible to load map from map catalogue");
			throw new SvgViewerEngineRuntimeException("Impossible to load map from map catalogue", e);
		}

		logger.debug("OUT");

		return svgDocument;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.AbstractMapProvider#getSVGMapStreamReader(java.lang.String)
	 */
	@Override
	public XMLStreamReader getSVGMapStreamReader(String mapName) throws SvgViewerEngineException {
		XMLStreamReader streamReader = null;
		String mapUrl = null;

		try {
			mapUrl = mapCatalogueServiceProxy.getMapUrl(mapName);
		} catch (Exception e) {
			logger.error("An error occurred while invoking mapCatalogueService method: getMapUrl()");
			throw new SvgViewerEngineException("Impossible to load map from url: " + mapUrl, e);
		}

		try {
			streamReader = SVGMapLoader.getMapAsStream(mapUrl);
		} catch (XMLStreamException e) {
			logger.error("An error occurred while processing xml stream of the svg map");
			throw new SvgViewerEngineException("An error occurred while processing xml stream of the svg map", e);
		} catch (FileNotFoundException e) {
			logger.error("Map file not found at url: " + mapUrl);
			throw new SvgViewerEngineException("Map file not found at url: " + mapUrl, e);
		}

		return streamReader;
	}

	/**
	 * Gets the map catalogue service proxy.
	 *
	 * @return the map catalogue service proxy
	 */
	public MapCatalogueAccessUtils getMapCatalogueServiceProxy() {
		return mapCatalogueServiceProxy;
	}

	/**
	 * Sets the map catalogue service proxy.
	 *
	 * @param mapCatalogueServiceProxy
	 *            the new map catalogue service proxy
	 */
	public void setMapCatalogueServiceProxy(MapCatalogueAccessUtils mapCatalogueServiceProxy) {
		this.mapCatalogueServiceProxy = mapCatalogueServiceProxy;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.AbstractMapProvider#getMapNamesByFeature(java.lang.String)
	 */
	@Override
	public List getMapNamesByFeature(String featureName) throws Exception {
		return getMapCatalogueServiceProxy().getMapNamesByFeature(featureName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.AbstractMapProvider#getFeatureNamesInMap(java.lang.String)
	 */
	@Override
	public List getFeatureNamesInMap(String mapName) throws Exception {
		return getMapCatalogueServiceProxy().getFeatureNamesInMap(mapName);
	}

	/**
	 * Sets the map catalogue service proxy.
	 *
	 * @param mapCatalogueServiceProxy
	 *            the new map catalogue service proxy
	 */
	public void getConf(MapCatalogueAccessUtils mapCatalogueServiceProxy) {
		this.mapCatalogueServiceProxy = mapCatalogueServiceProxy;
	}
}