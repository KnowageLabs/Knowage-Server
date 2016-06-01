package it.eng.knowage.engines.svgviewer.datamart.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.component.ISvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.dataset.DataMart;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.spago.base.SourceBean;

import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IDataMartProvider extends ISvgViewerEngineComponent {

	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 *
	 * @throws GeoEngineException
	 *             the geo engine exception
	 */
	DataMart getDataMart() throws SvgViewerEngineException;

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
	 * Sets the selected level name.
	 *
	 * @param levelName
	 *            the new selected level name
	 */
	void setSelectedLevelName(String levelName);

	/**
	 * Gets the selected level name.
	 *
	 * @return the selected level name
	 */
	String getSelectedLevelName();

	/**
	 * Gets the hierarchy names.
	 *
	 * @return the hierarchy names
	 */
	Set getHierarchyNames();

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

	/**
	 * Gets the selected level.
	 *
	 * @return the selected level
	 */
	Hierarchy.Level getSelectedLevel();
}
