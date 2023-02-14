/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import commonj.work.Work;
import it.eng.knowage.engines.dossier.template.AbstractDossierTemplate;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DocumentExecutionWorkForDoc extends AbstractDocumentExecutionWork implements Work {

	private static transient Logger logger = Logger.getLogger(DocumentExecutionWorkForDoc.class);

	public DocumentExecutionWorkForDoc(AbstractDossierTemplate dossierTemplate, List<BIObjectPlaceholdersPair> documents, IEngUserProfile userProfile,
			Integer progressThreadId, String randomKey) {
		super();
		this.documents = documents;
		this.userProfile = userProfile;
		this.progressThreadId = progressThreadId;
		this.randomKey = randomKey;
		this.dossierTemplate = dossierTemplate;
	}

	@Override
	public void run() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("TYPE", "DOC_TEMPLATE");
			jsonObject.put("MESSAGE", dossierTemplate.getDocTemplate().getName());
			this.setJsonObjectTemplate(jsonObjectTemplate);

			this.setTenant();
			this.runInternal(jsonObject);
		} catch (Exception e) {
			logger.error("Error while creating dossier activity", e);
			throw new SpagoBIRuntimeException(e.getMessage(), e);
		} finally {
			TenantManager.unset();
		}
	}

}
