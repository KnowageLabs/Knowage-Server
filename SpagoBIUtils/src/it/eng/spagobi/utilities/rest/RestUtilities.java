/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.rest;

import it.eng.spagobi.utilities.json.JSONUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestUtilities {

	/**
	 * Reads the body of a request and return it as a string
	 *
	 * @param request
	 *            the HttpServletRequest request
	 * @return the body
	 * @throws IOException
	 */
	public static String readBody(HttpServletRequest request) throws IOException {

		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return stringBuilder.toString();
	}

	/**
	 *
	 * Reads the body of a request and return it as a JSONObject
	 *
	 * @param request
	 *            the HttpServletRequest request
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject readBodyAsJSONObject(HttpServletRequest request) throws IOException, JSONException {
		String requestBody = RestUtilities.readBody(request);
		if (requestBody == null || requestBody.equals("")) {
			return new JSONObject();
		}
		return new JSONObject(requestBody);
	}

	/**
	 *
	 * Reads the body of a request and return it as a JSONOArray
	 *
	 * @param request
	 *            the HttpServletRequest request
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONArray readBodyAsJSONArray(HttpServletRequest request) throws IOException, JSONException {
		String requestBody = RestUtilities.readBody(request);
		return JSONUtils.toJSONArray(requestBody);
	}

}
