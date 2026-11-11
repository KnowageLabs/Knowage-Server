package it.eng.spagobi.api.common;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public final class DocumentModificationAuthorization {

	private DocumentModificationAuthorization() {
	}

	/**
	 * Checks modification rights against a document loaded from the server, never request metadata.
	 */
	public static void checkCanModify(BIObject document, UserProfile profile) {
		if (document == null) {
			throw new SpagoBIRuntimeException("Document does not exist");
		}
		if (profile == null || profile.getOrganization() == null
				|| !profile.getOrganization().equals(document.getTenant())) {
			throw new SpagoBIRuntimeException("User has no rights to modify this document");
		}

		if (Boolean.TRUE.equals(profile.getIsSuperadmin()) || UserUtilities.isAdministrator(profile)
				|| (profile.getUserId() != null && profile.getUserId().toString().equals(document.getCreationUser()))) {
			return;
		}

		try {
			if (SpagoBIConstants.DOC_STATE_DEV.equals(document.getStateCode())
					&& profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV)
					&& ObjectsAccessVerifier.canDevBIObject(document, profile)) {
				return;
			}
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while evaluating document modification permission", e);
		}
		throw new SpagoBIRuntimeException("User has no rights to modify this document");
	}
}
