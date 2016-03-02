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
package it.eng.qbe.datasource.sql;

import it.eng.spagobi.api.v2.DataSetResource;

import org.json.JSONObject;


/**
 * Override the DataSetPersister of the qbe. This is because if the dataset is tested in the dataset management interface ther isn't the need to call a service, but directly the api
 */
public class DataSetPersister{

	/**
	 * Override the perdidter of
	 * @param datasetLabels
	 * @return
	 * @throws Exception
	 */
	public JSONObject cacheDataSets(JSONObject datasetLabels, String userId) throws Exception {


		DataSetResource ds = new DataSetResource();
		String respString = ds.persistDataSets(datasetLabels);


		JSONObject ja = new JSONObject(respString);

		return ja;
	}

}
