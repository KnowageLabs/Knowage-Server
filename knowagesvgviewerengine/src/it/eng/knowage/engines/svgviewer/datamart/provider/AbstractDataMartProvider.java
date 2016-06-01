package it.eng.knowage.engines.svgviewer.datamart.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.component.AbstractSvgViewerEngineComponent;
import it.eng.knowage.engines.svgviewer.datamart.provider.configurator.AbstractDataMartProviderConfigurator;
import it.eng.knowage.engines.svgviewer.dataset.DataMart;
import it.eng.knowage.engines.svgviewer.dataset.DataSetMetaData;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.spago.base.SourceBean;

import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractDatasetProvider.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class AbstractDataMartProvider extends AbstractSvgViewerEngineComponent implements IDataMartProvider {

	/** The meta data. */
	private DataSetMetaData metaData;

	/** The hierarchies. */
	private Map hierarchies;

	/** The selected hierarchy name. */
	private String selectedHierarchyName;

	/** The selected level name. */
	private String selectedLevelName;

	/**
	 * Instantiates a new abstract dataset provider.
	 */
	public AbstractDataMartProvider() {
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
		AbstractDataMartProviderConfigurator.configure(this, getConf());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getDataSet()
	 */
	@Override
	public DataMart getDataMart() throws SvgViewerEngineException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getDataDetails(java.lang.String)
	 */
	@Override
	public SourceBean getDataDetails(String filterValue) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getHierarchyNames()
	 */
	@Override
	public Set getHierarchyNames() {
		if (hierarchies != null) {
			return hierarchies.keySet();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getHierarchy(java.lang.String)
	 */
	@Override
	public Hierarchy getHierarchy(String name) {
		if (hierarchies != null) {
			return (Hierarchy) hierarchies.get(name);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getSelectedHierarchy()
	 */
	@Override
	public Hierarchy getSelectedHierarchy() {
		if (hierarchies != null) {
			return (Hierarchy) hierarchies.get(selectedHierarchyName);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getSelectedLevel()
	 */
	@Override
	public Hierarchy.Level getSelectedLevel() {
		Hierarchy selectedHierarchy = getSelectedHierarchy();
		if (selectedHierarchy != null) {
			return selectedHierarchy.getLevel(selectedLevelName);
		}
		return null;
	}

	/**
	 * Gets the meta data.
	 *
	 * @return the meta data
	 */
	public DataSetMetaData getMetaData() {
		return metaData;
	}

	/**
	 * Sets the meta data.
	 *
	 * @param metaData
	 *            the new meta data
	 */
	public void setMetaData(DataSetMetaData metaData) {
		this.metaData = metaData;
	}

	/**
	 * Sets the hierarchies.
	 *
	 * @param hierarchies
	 *            the new hierarchies
	 */
	public void setHierarchies(Map hierarchies) {
		this.hierarchies = hierarchies;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getSelectedHierarchyName()
	 */
	@Override
	public String getSelectedHierarchyName() {
		return selectedHierarchyName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#setSelectedHierarchyName(java.lang.String)
	 */
	@Override
	public void setSelectedHierarchyName(String selectedHierarchyName) {
		this.selectedHierarchyName = selectedHierarchyName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#getSelectedLevelName()
	 */
	@Override
	public String getSelectedLevelName() {
		return selectedLevelName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.IDatasetProvider#setSelectedLevelName(java.lang.String)
	 */
	@Override
	public void setSelectedLevelName(String selectedLevelName) {
		this.selectedLevelName = selectedLevelName;
	}
}
