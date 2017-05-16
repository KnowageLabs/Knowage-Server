/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.cockpit.api.export.pdf;

import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class PdfExporter {

	private static final Logger logger = Logger.getLogger(PdfExporter.class);

	private String requestUrl;
	private Map<String, String[]> parameterMap;

	public PdfExporter(String requestUrl, Map<String, String[]> parameterMap) {
		this.requestUrl = requestUrl;
		this.parameterMap = parameterMap;
	}

	public byte[] getBinaryData() {
		int sheetCount = getSheetCount();
		List<byte[]> images = new ArrayList<byte[]>(sheetCount);

		for (int sheet = 0; sheet < sheetCount; sheet++) {
			byte[] image = getImage(sheet);
		}

		return "This is not a PDF file!".getBytes();
	}

	private byte[] getImage(int sheet) {
		try {
			GetMethod getMethod = new GetMethod(requestUrl);
			List<NameValuePair> nameValuePairs = getNameValuePairs(sheet);
			getMethod.setQueryString(nameValuePairs.toArray(new NameValuePair[0]));

			String userId = parameterMap.get("user_id")[0];
			String encodedUserId = Base64.encode(userId.getBytes("UTF-8"));
			getMethod.addRequestHeader("Authorization", "Direct " + encodedUserId);

			HttpClient client = new HttpClient();
			int statusCode = client.executeMethod(getMethod);
			byte[] response = getMethod.getResponseBody();
			getMethod.releaseConnection();

			return response;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to get image for sheet [" + sheet + "]", e);
		}
	}

	private List<NameValuePair> getNameValuePairs(int sheet) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (String parameter : parameterMap.keySet()) {
			if (!"outputType".equals(parameter)) {
				String value = parameterMap.get(parameter)[0];
				nameValuePairs.add(new NameValuePair(parameter, value));
			}
		}
		nameValuePairs.add(new NameValuePair("export", "true"));
		nameValuePairs.add(new NameValuePair("sheet", String.valueOf(sheet)));
		return nameValuePairs;
	}

	private int getSheetCount() {
		int documentId = Integer.parseInt(parameterMap.get("document")[0]);
		try {
			ObjTemplate objTemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
			if (objTemplate == null) {
				throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
			}

			String templateString = new String(objTemplate.getContent());
			JSONObject template = new JSONObject(templateString);
			JSONArray sheets = template.getJSONArray("sheets");

			return sheets.length();
		} catch (EMFAbstractError e) {
			throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}
}
