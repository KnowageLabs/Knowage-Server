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
package it.eng.spagobi.api.v2.export;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobDataMap;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;

abstract class AbstractExportJob implements Job {

	public static final String MAP_KEY_DATA_SET_ID = "dataSetId";

	public static final String MAP_KEY_DRIVERS = "drivers";

	public static final String MAP_KEY_ID = "id";

	public static final String MAP_KEY_LOCALE = "locale";

	public static final String MAP_KEY_PARAMETERS = "parameters";

	public static final String MAP_KEY_RESOURCE_PATH = "resourcePath";

	public static final String MAP_KEY_USER_PROFILE = "userProfile";

	protected final IDataSet getDataSet(Integer dataSetId, Map<String, Object> drivers, Map<String, String> parameters, UserProfile userProfile) {
		IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
		dsDAO.setUserProfile(userProfile);
		IDataSet dataSet = dsDAO.loadDataSetById(dataSetId);

		dataSet.setDrivers(drivers);
		dataSet.setParamsMap(parameters);

		dataSet.setUserProfileAttributes(userProfile.getUserAttributes());
		return dataSet;
	}

	protected final Integer getDataSetId(JobDataMap mergedJobDataMap) {
		return (Integer) mergedJobDataMap.get(MAP_KEY_DATA_SET_ID);
	}

	protected final Locale getLocale(JobDataMap mergedJobDataMap) {
		return (Locale) mergedJobDataMap.get(MAP_KEY_LOCALE);
	}

	protected final Map<String, Object> getDriversData(JobDataMap mergedJobDataMap) {
		return (Map<String, Object>) mergedJobDataMap.get(MAP_KEY_DRIVERS);
	}

	protected final UUID getJobId(JobDataMap mergedJobDataMap) {
		return (UUID) mergedJobDataMap.get(MAP_KEY_ID);
	}

	protected final Map<String, String> getParametersData(JobDataMap mergedJobDataMap) {
		return (Map<String, String>) mergedJobDataMap.get(MAP_KEY_PARAMETERS);
	}

	protected final String getResourcePathString(JobDataMap mergedJobDataMap) {
		return (String) mergedJobDataMap.get(MAP_KEY_RESOURCE_PATH);
	}

	protected final UserProfile getUserProfile(JobDataMap mergedJobDataMap) {
		return (UserProfile) mergedJobDataMap.get(MAP_KEY_USER_PROFILE);
	}

}
