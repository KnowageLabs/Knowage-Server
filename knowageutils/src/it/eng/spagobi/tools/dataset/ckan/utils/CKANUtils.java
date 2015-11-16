package it.eng.spagobi.tools.dataset.ckan.utils;

import it.eng.spagobi.tools.dataset.ckan.resource.impl.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class CKANUtils {

	private static transient Logger logger = Logger.getLogger(CKANUtils.class);

	public static Integer portAsInteger(String input) {
		logger.debug("Getting integer port from string");
		try {
			int port = Integer.parseInt(input);
			if (port <= 0)
				logger.debug("Integer lower than 1: it is not a valid port.", new Exception());
			logger.debug("Integer port obtained");
			return port;
		} catch (Exception e) {
			return 0;
		}
	}

	public static boolean isCompatibleWithSpagoBI(Resource rs) {
		logger.debug("IN: start checking if the CKAN Resource is compatible with SpagoBI file dataset");
		String extension = FilenameUtils.getExtension(rs.getUrl());

		// if (rs.getFormat().equalsIgnoreCase("csv") || extension.equalsIgnoreCase("csv") || rs.getContentType().equals("text/csv")) {
		if (extension.equalsIgnoreCase("csv")) {
			rs.setFormat("CSV");
			logger.debug("OUT");
			return true;
			// } else if (rs.getFormat().equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xls") || rs.getContentType().equals("application/vnd.ms-excel"))
			// {
		} else if (extension.equalsIgnoreCase("xls")) {
			rs.setFormat("XLS");
			logger.debug("OUT");
			return true;
		}
		logger.debug("OUT");
		return false;
	}

	public static JSONObject getJsonObjectFromCkanResource(Resource resource) {
		logger.debug("IN");
		JSONObject rootObj = new JSONObject();
		JSONObject configurationObj = new JSONObject();
		JSONObject packageDetailObj = new JSONObject();
		JSONObject resourceDetailObj = new JSONObject();
		JSONObject ownerDetailObj = new JSONObject();
		try {
			rootObj.put("dsTypeCd", "Ckan");
			rootObj.put("name", resource.getName());
			rootObj.put("description", resource.getDescription());
			rootObj.put("label", resource.getPackage_name() + "." + resource.getName());
			rootObj.put("owner", "TDB");

			packageDetailObj.put("package_name", resource.getPackage_name());
			packageDetailObj.put("package_url", resource.getPackage_url());
			packageDetailObj.put("isPrivate", resource.isPackage_isPrivate());
			packageDetailObj.put("isSearchable", resource.isPackage_isSearchable());
			packageDetailObj.put("license", resource.getPackage_license());

			resourceDetailObj.put("name", resource.getName());
			resourceDetailObj.put("description", resource.getDescription());
			resourceDetailObj.put("format", resource.getFormat());
			resourceDetailObj.put("url", resource.getUrl());
			resourceDetailObj.put("created", resource.getCreated());
			resourceDetailObj.put("last_modified", resource.getLast_modified());

			configurationObj.put("Package", packageDetailObj);
			configurationObj.put("Resource", resourceDetailObj);
			configurationObj.put("Owner", ownerDetailObj);
			configurationObj.put("ckanId", resource.getId());
			// configurationObj.put("spagobi", spagobiDetailObj);

			rootObj.put("configuration", configurationObj);
		} catch (JSONException e) {
			logger.error("Cannot convert from CKAN resource to JSON Object");
			return null;
		}
		return rootObj;
	}
}
