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
import it.eng.knowage.engines.svgviewer.dataset.provider.Link;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.transformer.IDataStoreTransformer;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class AddLinkFieldsTransformer implements IDataStoreTransformer {
	String[] measureColumnNames;
	// Hierarchy.Level level;
	String level;
	Map env;

	public static transient Logger logger = Logger.getLogger(AddLinkFieldsTransformer.class);

	// public AddLinkFieldsTransformer(String[] measureColumnNames, Hierarchy.Level level, Map env) {
	public AddLinkFieldsTransformer(String[] measureColumnNames, String level, Map env) {
		this.measureColumnNames = measureColumnNames;
		this.level = level;
		this.env = env;
	}

	// @Override
	// public void transform(IDataStore dataStore) {
	// List fieldsMeta = dataStore.getMetaData().findFieldMeta("ROLE", "MEASURE");
	// logger.debug("found " + fieldsMeta.size() + " measure column in dataset");
	// for (int i = 0; i < fieldsMeta.size(); i++) {
	//
	// IFieldMetaData fieldMeta = (IFieldMetaData) fieldsMeta.get(i);
	// String measureFiledName = fieldMeta.getName();
	//
	// String linkFiledName = measureFiledName + "_LINK";
	// // Link link = level.getLink(measureFiledName);
	// Link link = null;
	// addLinkField(linkFiledName, link, dataStore);
	// }
	//
	// }

	@Override
	public void transform(IDataStore dataStore) {
		// do nothing
	}

	public void addLinkField(String fieldName, Link link, IDataStore dataStore) {

		IMetaData dataStoreMeta;
		FieldMetadata fieldMeta;
		IRecord record;
		IField field;

		try {
			Assert.assertNotNull(fieldName, "Input parametr [fieldName] cannot be null");
			// Link parameter can be null; in that case Link.DEFAULT_BASE_URL will be used
			Assert.assertNotNull(dataStore, "Input parametr [dataStore] cannot be null");

			try {
				logger.debug("Adding link column [" + fieldName + ": " + link + "] ...");

				dataStoreMeta = dataStore.getMetaData();
				fieldMeta = new FieldMetadata();

				fieldMeta.setName(fieldName);
				fieldMeta.setType(String.class);
				fieldMeta.setProperty("ROLE", "CROSSNAVLINK");

				dataStoreMeta.addFiedMeta(fieldMeta);

				logger.debug("Link column [" + fieldName + ": " + link + "] added succesfully");
			} catch (Throwable t) {
				throw new SvgViewerEngineException("Impossible to add link column [" + fieldName + ": " + link + "] to datastore", t);
			}

			record = null;
			try {
				logger.debug("Valorizing link column [" + fieldName + ": " + link + "] for each record in the dataset ...");
				Iterator it = dataStore.iterator();
				while (it.hasNext()) {
					record = (IRecord) it.next();
					if (link != null) {
						logger.debug("Added link value [" + link.toXString(record, env) + "]");
						field = new Field(link.toXString(record, env));
					} else {
						field = new Field(Link.DEFAULT_BASE_URL);
						logger.debug("Added link value [" + Link.DEFAULT_BASE_URL + "]");
					}

					record.appendField(field);
				}
				logger.debug("Link column [" + fieldName + ": " + link + "] has been succesfully valorized for each record in the dataset ");
			} catch (Throwable t) {
				throw new SvgViewerEngineException("Impossible to valorize link column for record [" + record + "]", t);
			}
		} catch (Throwable t) {
			SvgViewerEngineRuntimeException e;
			if (t instanceof SvgViewerEngineRuntimeException) {
				e = (SvgViewerEngineRuntimeException) t;
			} else {
				e = new SvgViewerEngineRuntimeException("An unpredicted error occurred while adding link fields to datastore", t);
			}

			throw e;
		}
	}

}
