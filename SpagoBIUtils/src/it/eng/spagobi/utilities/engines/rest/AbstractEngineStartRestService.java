/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;

import java.io.IOException;

public abstract class AbstractEngineStartRestService extends AbstractEngineRestService {

	/**
	 * Gets the analysis metadata.
	 *
	 * @return the analysis metadata
	 */
	public EngineAnalysisMetadata getAnalysisMetadata() {
		if (analysisMetadata != null) {
			return analysisMetadata;
		}

		logger.debug("IN");

		analysisMetadata = new EngineAnalysisMetadata();

		if (requestContainsAttribute(SUBOBJ_ID)) {

			Integer id = getAttributeAsInteger(SUBOBJ_ID);
			if (id == null) {
				logger.warn("Value [" + getAttribute(SUBOBJ_ID).toString() + "] is not a valid subobject id");
			}
			analysisMetadata.setId(id);

			if (requestContainsAttribute(SUBOBJ_NAME)) {
				analysisMetadata.setName(getAttributeAsString(SUBOBJ_NAME));
			} else {
				logger.warn("No name attribute available in request for subobject [" + getAttributeAsString(SUBOBJ_ID) + "]");
				analysisMetadata.setName(getAttributeAsString(SUBOBJ_ID));
			}

			if (requestContainsAttribute(SUBOBJ_DESCRIPTION)) {
				analysisMetadata.setDescription(getAttributeAsString(SUBOBJ_DESCRIPTION));
			} else {
				logger.warn("No description attribute available in request for subobject [" + getAttributeAsString(SUBOBJ_ID) + "]");
				analysisMetadata.setDescription("");
			}

			if (requestContainsAttribute(SUBOBJ_VISIBILITY)) {
				if (requestContainsAttribute(SUBOBJ_VISIBILITY, "Public")) {
					analysisMetadata.setScope(EngineAnalysisMetadata.PUBLIC_SCOPE);
				} else {
					logger.warn("No visibility attribute available in request for subobject [" + getAttributeAsString(SUBOBJ_ID) + "]");
					analysisMetadata.setScope(EngineAnalysisMetadata.PRIVATE_SCOPE);
				}
			}
		}

		logger.debug("OUT");

		return analysisMetadata;
	}

	/**
	 * Gets the analysis state row data.
	 *
	 * @return the analysis state row data
	 */
	public byte[] getAnalysisStateRowData() {
		Content spagoBISubObject;
		byte[] rowData;

		if (analysisStateRowData == null && getAnalysisMetadata().getId() != null) {

			logger.debug("IN");

			spagoBISubObject = getContentServiceProxy().readSubObjectContent(getAnalysisMetadata().getId().toString());
			try {
				rowData = DECODER.decodeBuffer(spagoBISubObject.getContent());
				analysisStateRowData = rowData;
			} catch (IOException e) {
				logger.warn("Impossible to decode the content of " + getAnalysisMetadata().getId().toString() + " subobject");
				return null;
			}

			logger.debug("OUT");
		}

		return analysisStateRowData;
	}

}
