package it.eng.knowage.engine.kpi.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.json.Xml;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/kpisTemplate")
@ManageAuthorization
public class KpiResource extends AbstractFullKpiEngineResource {
	static protected Logger logger = Logger.getLogger(KpiResource.class);

	@POST
	@Path("/getKpiTemplate")
	public String loadTemplate(@Context HttpServletRequest req)
			throws EMFUserError, EMFInternalError, JSONException, TransformerFactoryConfigurationError, TransformerException {
		ObjTemplate template;
		try {
			JSONObject request = RestUtilities.readBodyAsJSONObject(req);

			template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(request.getInt("id"));

		} catch (Exception e) {
			logger.error("Error converting JSON Template to XML...", e);
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);

		}
		return new JSONObject(Xml.xml2json(new String(template.getContent()))).toString();

	}

	// ===================================================================
	// UTILITY METHODS
	// ===================================================================

	private void saveTemplate(String docLabel, String xml, UserProfile user) {
		ObjTemplate template = new ObjTemplate();
		template.setName("Template.xml");
		template.setContent(xml.getBytes());
		template.setDimension(Long.toString(xml.getBytes().length / 1000) + " KByte");
		try {
			IBIObjectDAO biObjectDao;
			BIObject document;
			biObjectDao = DAOFactory.getBIObjectDAO();
			biObjectDao.setUserProfile(user);
			document = biObjectDao.loadBIObjectById(new Integer(docLabel));
			saveDocument(document, template, user);
		} catch (EMFUserError e) {
			logger.error("Error saving JSON Template to XML...", e);
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		}
	}

	private boolean saveDocument(BIObject document, ObjTemplate template, UserProfile user) throws EMFUserError {

		Boolean overwrite;

		Assert.assertNotNull(document, "Input parameter [document] cannot be null");
		IBIObjectDAO documentDAO = DAOFactory.getBIObjectDAO();
		documentDAO.setUserProfile(user);
		if (isAnExistingDocument(document)) {
			try {
				documentDAO.modifyBIObject(document, template);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to update object [" + document.getLabel() + "]", t);
			}
			overwrite = true;
			logger.debug("Document [" + document.getLabel() + "] succesfully updated");
		} else {
			try {
				Integer id = documentDAO.insertBIObject(document, template);
				document.setId(id);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to insert object [" + document.getLabel() + "]", t);
			}

			overwrite = false;
			logger.debug("Document with [" + document.getLabel() + "] succesfully inserted with id [" + document.getId() + "]");
		}

		return overwrite;
	}

	private boolean isAnExistingDocument(BIObject document) {
		Integer documentId;

		Assert.assertNotNull(document, "Input parameter [document] cannot be null");

		documentId = document.getId();
		return (documentId != null && documentId.intValue() != 0);
	}
}
