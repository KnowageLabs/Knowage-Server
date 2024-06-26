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

package it.eng.spagobi.tools.dataset.measurecatalogue.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogue;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueMeasure;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueMeasureNotVisibleException;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueSingleton;
import it.eng.spagobi.tools.dataset.measurecatalogue.materializer.InMemoryMaterializer;
import it.eng.spagobi.tools.dataset.measurecatalogue.materializer.exception.NoCommonDimensionsRuntimeException;
import it.eng.spagobi.tools.dataset.measurecatalogue.materializer.exception.NoCompleteCommonDimensionsRuntimeException;

/**
 * This class contains the services to perform the CRUD action on the measure catalogue
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

@Path("/measures")
public class MeasureCatalogueCRUD {
	public static transient Logger logger = Logger.getLogger(MeasureCatalogueCRUD.class);
	private static String noCommonDimensionsRuntimeException = "error.mesage.description.measure.join.no.common.dimension";
	private static String noCompleteCommonDimensionsRuntimeException = "error.mesage.description.measure.join.no.complete.common.dimension";

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MEASURES_CATALOGUE_MANAGEMENT })
	public String getAllMeasures(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String measures = MeasureCatalogueSingleton.getMeasureCatologue().toString(((UserProfile) profile).getUserId().toString(),
				UserUtilities.isAdministrator(profile));
		return measures;
	}

	@POST
	@Path("/join")
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MEASURES_CATALOGUE_MANAGEMENT })
	public String join(@Context HttpServletRequest req, MultivaluedMap<String, String> form) {

		logger.debug("IN");

		IDataStore dataStore;
		logger.debug("Loading measure catalogue ...");
		MeasureCatalogue catalogue = MeasureCatalogueSingleton.getMeasureCatologue();
		logger.debug("Measure catalogue succesfully loaded");

		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		List<String> labels = form.get("labels");
		List<MeasureCatalogueMeasure> measures = new ArrayList<MeasureCatalogueMeasure>();
		for (int i = 0; i < labels.size(); i++) {
			MeasureCatalogueMeasure aMeasure = catalogue.getMeasureByLabel(labels.get(i));
			if (aMeasure != null) {
				if (!aMeasure.isVisibleToUser(profile)) {
					logger.error("The measure " + aMeasure.getAlias() + " of the dataset " + aMeasure.getDsName() + " is not visible for the user "
							+ ((UserProfile) profile).getUserId().toString());
					throw new MeasureCatalogueMeasureNotVisibleException(aMeasure);
				} else {
					logger.debug("The measure [" + aMeasure.getAlias() + "] of the dataset [" + aMeasure.getDsName() + "] whose label is equal to ["
							+ aMeasure.getDsId() + "] is visible for the user " + ((UserProfile) profile).getUserId().toString()
							+ " and so will be joined in the final dataset");
					measures.add(aMeasure);
				}
			}
		}

		logger.debug("Creating joined dataset ...");
		InMemoryMaterializer imm = new InMemoryMaterializer();
		try {
			dataStore = imm.joinMeasures(measures);
		} catch (NoCommonDimensionsRuntimeException e) {
			return (ExceptionUtilities.serializeException(noCommonDimensionsRuntimeException, null));
		} catch (NoCompleteCommonDimensionsRuntimeException e) {
			return (ExceptionUtilities.serializeException(noCompleteCommonDimensionsRuntimeException, null));
		}
		logger.debug("Joined dataset succesfully created. It contains [" + dataStore.getRecordsCount() + "] records");

		if (dataStore.getRecordsCount() == 0) {
			String joinedDatasetStr = "";
			for (MeasureCatalogueMeasure measure : measures)
				joinedDatasetStr += measure.getDsName() + "(" + measure.getDsId() + "); ";
			throw new RuntimeException("There is no join between datasets [" + joinedDatasetStr + "] that contain the selected measures");
		}

		logger.debug("Serializing joined dataset ...");

		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject dataStroreJSON = (JSONObject) dataSetWriter.write(dataStore);
		JSONObject metaData;

		try {
			metaData = dataStroreJSON.getJSONObject("metaData");
			JSONArray fieldsMetaJSON = metaData.getJSONArray("fields");
			List<IFieldMetaData> geoRefFieldMeta = new ArrayList<IFieldMetaData>();
			for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				IFieldMetaData fieldMeta = dataStore.getMetaData().getFieldMeta(i);
				JSONObject fieldMetaJSON = fieldsMetaJSON.getJSONObject(i + 1);
				if (fieldMeta.getFieldType().equals(FieldType.MEASURE)) {
					fieldMetaJSON.put("role", FieldType.MEASURE.toString());
				} else if (fieldMeta.getFieldType().equals(FieldType.SPATIAL_ATTRIBUTE)) {
					fieldMetaJSON.put("role", FieldType.SPATIAL_ATTRIBUTE.toString());
				} else if (fieldMeta.getFieldType().equals(FieldType.ATTRIBUTE)) {
					fieldMetaJSON.put("role", FieldType.ATTRIBUTE.toString());
					String hierarchy = (String) fieldMeta.getProperty("hierarchy");
					if (hierarchy != null) {
						fieldMetaJSON.put("hierarchy", hierarchy);
						fieldMetaJSON.put("hierarchy_level", fieldMeta.getProperty("hierarchy_level"));
						if (hierarchy.equalsIgnoreCase("GEO")) {
							geoRefFieldMeta.add(fieldMeta);
						}
					}
				}

				fieldMetaJSON.put("naturalKey", fieldMeta.getName());
				fieldMetaJSON.put("dataset", fieldMeta.getProperty("dataset"));
			}

			if (geoRefFieldMeta.size() == 0) {
				throw new RuntimeException("Internal server error: generated dataset have no reference to geographical dimension");
			}
			if (geoRefFieldMeta.size() > 1) {
				throw new RuntimeException("Internal server error: generated dataset have more than one reference to geographical dimension");
			}

			metaData.put("geoId", geoRefFieldMeta.get(0).getName());
			metaData.put("geoIdHierarchyLevel", geoRefFieldMeta.get(0).getProperty("hierarchy_level"));
			// TODO bisogna strippare la label del datset dal prefisso dell'header di colonna
		} catch (JSONException t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}

		logger.debug("Joined dataset succesfully serialized");

		logger.debug("OUT");

		return dataStroreJSON.toString();
	}

}
