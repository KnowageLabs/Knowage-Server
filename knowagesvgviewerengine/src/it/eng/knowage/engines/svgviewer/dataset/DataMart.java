package it.eng.knowage.engines.svgviewer.dataset;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

// TODO: Auto-generated Javadoc
/**
 * The Class DataSet.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataMart {

	IDataStore dataStore;

	/** The meta data. */
	private DataSetMetaData metaData;

	/**
	 * Gets the meta data.
	 *
	 * @return the meta data
	 */
	public DataSetMetaData getMetaData() {
		return metaData;
	}

	/** The target feature name. */
	private String targetFeatureName;

	/**
	 * Constructor.
	 */
	public DataMart() {
		super();
	}

	/**
	 * Gets the target feature name.
	 *
	 * @return the target feature name
	 */
	public String getTargetFeatureName() {
		return targetFeatureName;
	}

	/**
	 * Sets the target feature name.
	 *
	 * @param targetFeatureName
	 *            the new target feature name
	 */
	public void setTargetFeatureName(String targetFeatureName) {
		this.targetFeatureName = targetFeatureName;
	}

	public IDataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}

}