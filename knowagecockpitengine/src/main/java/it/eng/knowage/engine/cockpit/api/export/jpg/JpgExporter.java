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
package it.eng.knowage.engine.cockpit.api.export.jpg;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.slimerjs.wrapper.SlimerJS;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ZipUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class JpgExporter {

	private static final Logger logger = Logger.getLogger(JpgExporter.class);
	private final static String TEMP_SUFFIX = ".temp.png";
	private final int documentId;
	private final String userId;
	private final String requestUrl;
	private final RenderOptions renderOptions;

	public JpgExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions) {
		this.documentId = documentId;
		this.userId = userId;
		this.requestUrl = requestUrl;
		this.renderOptions = renderOptions;
	}

	public byte[] getBinaryData() throws Exception {
		BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
		int sheetCount = getSheetCount(document);
		URL url = new URL(requestUrl);
		Map<String, String> authenticationHeaders = new HashMap<>(1);
		String encodedUserId = Base64.encodeBase64String(userId.getBytes("UTF-8"));
		authenticationHeaders.put("Authorization", "Direct " + encodedUserId);
		List<InputStream> images = SlimerJS.render(url, sheetCount, renderOptions);
		if (images.size() == 1) {
			return IOUtils.toByteArray(images.get(0));
		} else {
			return ZipUtils.zipInputStreams(images, "sheet", "jpg");
		}

	}

	public int getSheetCount(BIObject document) {
		try {
			ObjTemplate objTemplate = document.getActiveTemplate();
			if (objTemplate == null) {
				throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
			}
			int numOfPages = 0;
			switch (document.getEngineLabel()) {
			case "knowagechartengine":
				numOfPages = 1;
				return numOfPages;
			case "knowagecockpitengine":
				String templateString = new String(objTemplate.getContent());
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				numOfPages = sheets.length();
				return numOfPages;

			default:
				return numOfPages;
			}

		} catch (EMFAbstractError e) {
			throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

}
