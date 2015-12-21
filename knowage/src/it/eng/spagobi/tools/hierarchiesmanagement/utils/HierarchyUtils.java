package it.eng.spagobi.tools.hierarchiesmanagement.utils;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HierarchyUtils {

	private static Logger logger = Logger.getLogger(HierarchyUtils.class);

	/**
	 * This method creates a JSON from a Hierarchy field
	 *
	 * @param field
	 *            Field read from hierarchies config
	 * @param isHierarchyField
	 *            to manage differences between dimensions and hierarchies fields
	 * @return a JSON that represents a field
	 * @throws JSONException
	 */
	private static JSONObject createJSONObjectFromField(Field field, boolean isHierarchyField) throws JSONException {

		logger.debug("START");

		JSONObject result = new JSONObject();

		Assert.assertNotNull(field, "Impossible to create a JSON from a null field");

		result.put(HierarchyConstants.FIELD_ID, field.getId());
		logger.debug("Field [" + HierarchyConstants.FIELD_ID + "] is " + field.getId());

		result.put(HierarchyConstants.FIELD_NAME, field.getName());
		logger.debug("Field [" + HierarchyConstants.FIELD_NAME + "] is " + field.getName());

		result.put(HierarchyConstants.FIELD_VISIBLE, field.isVisible());
		logger.debug("Field [" + HierarchyConstants.FIELD_VISIBLE + "] is " + field.isVisible());

		result.put(HierarchyConstants.FIELD_EDITABLE, field.isEditable());
		logger.debug("Field [" + HierarchyConstants.FIELD_EDITABLE + "] is " + field.isEditable());

		result.put(HierarchyConstants.FIELD_TYPE, field.getType());
		logger.debug("Field [" + HierarchyConstants.FIELD_TYPE + "] is " + field.getType());

		if (isHierarchyField) { // add these values only for hierarchies fields

			logger.debug("This Field is a Hierarchy field");

			result.put(HierarchyConstants.FIELD_SINGLE_VALUE, field.isSingleValue());
			logger.debug("Field [" + HierarchyConstants.FIELD_SINGLE_VALUE + "] is " + field.isSingleValue());

			result.put(HierarchyConstants.FIELD_REQUIRED, field.isRequired());
			logger.debug("Field [" + HierarchyConstants.FIELD_REQUIRED + "] is " + field.isRequired());
		}

		logger.debug("END");
		return result;

	}

	/**
	 * This method creates a JSON from a list of fields with the specified name as key
	 *
	 * @param fields
	 *            List of Fields read from hierarchies config
	 * @param name
	 *            the key for the JSON related to the fields list
	 * @param hierarchyFields
	 *            to manage differences between dimensions and hierarchies fields
	 * @return a JSON that represents fields
	 * @throws JSONException
	 */
	public static JSONObject createJSONObjectFromFieldsList(List<Field> fields, String name, boolean hierarchyFields) throws JSONException {

		logger.debug("START");

		JSONObject result = new JSONObject();
		JSONArray jsonFieldsArray = new JSONArray();

		for (Field tmpField : fields) {
			jsonFieldsArray.put(createJSONObjectFromField(tmpField, hierarchyFields));
		}

		result.put(name, jsonFieldsArray);

		logger.debug("END");
		return result;

	}

	/**
	 * Returns the datasource object referenced to the dimension in input
	 *
	 * @param dimension
	 * @return
	 * @throws SpagoBIServiceException
	 */
	public static IDataSource getDataSource(String dimension) throws SpagoBIServiceException {
		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			return dataSource;
		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving hierarchy datasource informations");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy datasource informations", t);
		}

	}

}
