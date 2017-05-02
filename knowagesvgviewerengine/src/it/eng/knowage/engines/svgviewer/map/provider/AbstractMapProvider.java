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
import it.eng.knowage.engines.svgviewer.component.AbstractSvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.map.provider.configurator.AbstractMapProviderConfigurator;
import it.eng.spago.base.SourceBean;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

/**
 * The Class AbstractMapProvider.
 *
 */
public class AbstractMapProvider extends AbstractSvgViewerEngineComponent implements IMapProvider {

	/** The selected map name. */
	private String selectedMapName;

	/** The selected map name. */
	private HierarchyMember selectedHierarchyMember;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(AbstractMapProvider.class);

	/**
	 * Instantiates a new abstract map provider.
	 */
	public AbstractMapProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.AbstractGeoEngineComponent#init(java.lang.Object)
	 */
	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		super.init(conf);
		AbstractMapProviderConfigurator.configure(this, getConf());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getSVGMapStreamReader()
	 */
	@Override
	public XMLStreamReader getSVGMapStreamReader() throws SvgViewerEngineException {
		return getSVGMapStreamReader(selectedMapName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getSVGMapStreamReader(java.lang.String)
	 */
	@Override
	public XMLStreamReader getSVGMapStreamReader(String mapName) throws SvgViewerEngineException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getSVGMapDOMDocument()
	 */
	@Override
	public SVGDocument getSVGMapDOMDocument() throws SvgViewerEngineRuntimeException {
		return getSVGMapDOMDocument(selectedMapName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getSVGMapDOMDocument(java.lang.String)
	 */
	@Override
	public SVGDocument getSVGMapDOMDocument(String mapName) throws SvgViewerEngineRuntimeException {
		return null;
	}

	@Override
	public SVGDocument getSVGMapDOMDocument(HierarchyMember member) throws SvgViewerEngineRuntimeException {
		return getSVGMapDOMDocument(member);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getSelectedMapName()
	 */
	@Override
	public String getSelectedMapName() {
		return selectedMapName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#setSelectedMapName(java.lang.String)
	 */
	@Override
	public void setSelectedMapName(String selectedMapName) {
		this.selectedMapName = selectedMapName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getMapNamesByFeature(java.lang.String)
	 */
	@Override
	public List getMapNamesByFeature(String featureName) throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.map.provider.IMapProvider#getFeatureNamesInMap(java.lang.String)
	 */
	@Override
	public List getFeatureNamesInMap(String mapName) throws Exception {
		return null;
	}

	@Override
	public String getDefaultMapName() throws Exception {
		SourceBean confSB = (SourceBean) getConf();
		return (String) confSB.getAttribute("map_name");
	}

	/**
	 * @return the selectedHierarchyMember
	 */
	@Override
	public HierarchyMember getSelectedHierarchyMember() {
		return selectedHierarchyMember;
	}

	/**
	 * @param selectedHierarchyMember
	 *            the selectedHierarchyMember to set
	 */
	public void setSelectedHierarchyMember(HierarchyMember selectedHierarchyMember) {
		this.selectedHierarchyMember = selectedHierarchyMember;
	}

}
