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
package it.eng.knowage.engines.svgviewer.dataset;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.List;

/**
 * The Class DataSet.
 *
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
	private List<String> targetFeatureName;

	/**
	 * Constructor.
	 */
	public DataMart() {
		super();
	}

	/**
	 * Gets the target feature names.
	 *
	 * @return the target feature names
	 */
	public List<String> getTargetFeatureName() {
		return targetFeatureName;
	}

	/**
	 * Sets the target feature names.
	 *
	 * @param targetFeatureName
	 *            the new target feature names
	 */
	public void setTargetFeatureName(List<String> targetFeatureName) {
		this.targetFeatureName = targetFeatureName;
	}

	public IDataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}

}