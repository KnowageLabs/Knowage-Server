package it.eng.spagobi.commons.utilities;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;

public final class SnapshotAccessVerifier {

    private SnapshotAccessVerifier() {
    }

    public static void checkDocument(Integer documentId, IEngUserProfile profile) throws EMFUserError, EMFInternalError {
        if (!canSeeDocument(documentId, profile)) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    public static boolean canSeeDocument(Integer documentId, IEngUserProfile profile) throws EMFUserError, EMFInternalError {
        if (documentId == null || !(profile instanceof UserProfile)) {
            return false;
        }
        IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
        dao.setUserProfile(profile);
        BIObject document = dao.loadBIObjectById(documentId);
        String tenant = ((UserProfile) profile).getOrganization();
        return document != null && tenant != null && tenant.equals(document.getTenant())
                && ObjectsAccessVerifier.canSee(document, profile);
    }

    public static void checkSnapshot(Snapshot snapshot, Integer expectedDocumentId, IEngUserProfile profile)
            throws EMFUserError, EMFInternalError {
        if (snapshot == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (expectedDocumentId != null && !expectedDocumentId.equals(snapshot.getBiobjId())) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        checkDocument(snapshot.getBiobjId(), profile);
    }
}
