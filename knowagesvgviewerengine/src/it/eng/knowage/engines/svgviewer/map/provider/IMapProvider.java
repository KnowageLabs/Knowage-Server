package it.eng.knowage.engines.svgviewer.map.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.component.ISvgViewerEngineComponent;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.svg.SVGDocument;

// TODO: Auto-generated Javadoc
/**
 * The Interface IMapProvider.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
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
	SVGDocument getSVGMapDOMDocument() throws SvgViewerEngineException;

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
}
