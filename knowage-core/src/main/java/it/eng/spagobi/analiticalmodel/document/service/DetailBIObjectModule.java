/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.document.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spago.validation.coordinator.ValidationCoordinator;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.utils.DetBIObjModHelper;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.community.mapping.SbiCommunity;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */

/**
 * Implements a module which handles all BI objects management: has methods for BI Objects load, detail, modify/insertion and deleting operations. The
 * <code>service</code> method has a switch for all these operations, differentiated the ones from the others by a <code>message</code> String.
 */

public class DetailBIObjectModule extends AbstractHttpModule {
	private static Logger logger = Logger.getLogger(DetailBIObjectModule.class);
	public static final String MODULE_PAGE = "DetailBIObjectPage";
	public static final String NAME_ATTR_OBJECT = "BIObjects";
	public static final String NAME_ATTR_LIST_OBJ_TYPES = "types";
	public static final String NAME_ATTR_LIST_ENGINES = "engines";
	public static final String NAME_ATTR_LIST_STATES = "states";
	public static final String NAME_ATTR_OBJECT_PAR = "OBJECT_PAR";
	public static final String NAME_ATTR_LIST_DS = "datasource";
	public static final String NAME_ATTR_LIST_LANGUAGES = "languages";
	public static final String NAME_ATTR_LIST_DATASET = "datasets";
	public static final String LOADING_PARS_DC = "loadingParsDC";
	public static final String NAME_ATTR_LIST_COMMUNITIES = "community";

	private EMFErrorHandler errorHandler = null;
	private IEngUserProfile profile;
	private String initialPath = null;
	private DetBIObjModHelper helper = null;
	private IBIObjectDAO biobjDAO = null;
	SessionContainer session = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insertion, modify, detail and deletion methods.
	 *
	 * @param request  The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 * @throws Exception the exception
	 */
	@Override
	public void service(SourceBean request, SourceBean response) throws Exception {
		Monitor loadingMonitor = MonitorFactory.start("Knowage.DetailBIObjectModule.service");
		// RECOVER REQUEST CONTAINER, SESSION CONTAINER, USER PROFILE AND ERROR HANDLER
		RequestContainer requestContainer = this.getRequestContainer();
		ResponseContainer responseContainer = this.getResponseContainer();
		session = requestContainer.getSessionContainer();
		SessionContainer permanentSession = session.getPermanentContainer();
		profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		biobjDAO = DAOFactory.getBIObjectDAO();
		biobjDAO.setUserProfile(profile);
		errorHandler = getErrorHandler();

		// CREATE THE HELPER
		helper = new DetBIObjModHelper(requestContainer, responseContainer, request, response);

		// GET THE EXECUTION MODALITY AND THE INITIAL PATH
		String modality = ChannelUtilities.getPreferenceValue(this.getRequestContainer(), BIObjectsModule.MODALITY, "");
		initialPath = null;
		if (modality != null && modality.equalsIgnoreCase(BIObjectsModule.FILTER_TREE)) {
			initialPath = ChannelUtilities.getPreferenceValue(this.getRequestContainer(),
					TreeObjectsModule.PATH_SUBTREE, "");
		}
		// GET MESSAGE FROM REQUEST
		String message = (String) request.getAttribute("MESSAGEDET");
		logger.debug(" MESSAGEDET = " + message);

		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.debug("The message parameter is null");
				throw userError;
			}

			if (message.trim().equalsIgnoreCase(ObjectsTreeConstants.DETAIL_SELECT)) {
				getDetailObject(request, response);
			} else if (message.trim().equalsIgnoreCase(ObjectsTreeConstants.DETAIL_MOD)) {
				modBIObject(request, ObjectsTreeConstants.DETAIL_MOD, response);
			} else if (message.trim().equalsIgnoreCase(ObjectsTreeConstants.DETAIL_NEW)) {
				newBIObject(request, response);
			} else if (message.trim().equalsIgnoreCase(ObjectsTreeConstants.DETAIL_INS)) {
				modBIObject(request, ObjectsTreeConstants.DETAIL_INS, response);
			} else if (message.trim().equalsIgnoreCase(ObjectsTreeConstants.DETAIL_DEL)) {
				delDetailObject(request, ObjectsTreeConstants.DETAIL_DEL, response, profile);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.ERASE_VERSION)) {
				eraseVersion(request, response);
			} else if (message.trim().equalsIgnoreCase("EXIT_FROM_DETAIL")) {
				exitFromDetail(request, response);
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
		loadingMonitor.stop();
	}

	private void setLoopbackContext(SourceBean request, String message) throws EMFUserError {
		BIObject obj = null;
		try {
			obj = helper.recoverBIObjectDetails(message);
		} catch (Exception e) {
			logger.error("Exception", e);
		}
		BIObjectParameter biObjPar = helper.recoverBIObjectParameterDetails(obj.getId());
		session.setAttribute("LookupBIObject", obj);
		session.setAttribute("LookupBIObjectParameter", biObjPar);
		session.setAttribute("modality", message);
		session.setAttribute("modalityBkp", message);
	}

	private void delateLoopbackContext() {
		session.delAttribute("LookupBIObject");
		session.delAttribute("LookupBIObjectParameter");
		session.delAttribute("modality");
		session.delAttribute("modalityBkp");
	}

	private Integer getBIObjectIdFromLoopbackContext() {
		Integer id = null;
		BIObject obj = (BIObject) session.getAttribute("LookupBIObject");
		if (obj != null)
			id = obj.getId();
		return id;
	}

	private void startParametersLookupHandler(SourceBean request, String message, SourceBean response)
			throws EMFUserError, SourceBeanException {
		setLoopbackContext(request, message);
		response.setAttribute("parametersLookup", "true");
	}

	private void startLinksLookupHandler(SourceBean request, String message, SourceBean response)
			throws EMFUserError, SourceBeanException {
		modBIObject(request, ObjectsTreeConstants.DETAIL_MOD, response);
		String idStr = (String) request.getAttribute("id");
		session.setAttribute("SUBJECT_ID", idStr);
		response.setAttribute("linksLookup", "true");
	}

	private void startDependenciesLookupHandler(SourceBean request, String message, SourceBean response)
			throws Exception {
		BIObject obj = null;
		try {
			obj = helper.recoverBIObjectDetails(message);
		} catch (Exception e) {
			logger.error("Exception", e);
		}

		BIObjectParameter biObjPar = helper.recoverBIObjectParameterDetails(obj.getId());
		String saveBIObjectParameter = (String) request.getAttribute("saveBIObjectParameter");
		if (saveBIObjectParameter != null && saveBIObjectParameter.equalsIgnoreCase("yes")) {
			// it is requested to save the visible BIObjectParameter
			ValidationCoordinator.validate("PAGE", "BIObjectParameterValidation", this);
			// If it's a new BIObjectParameter or if the Parameter was changed controls
			// that the BIObjectParameter url name is not already in use
			urlNameControl(obj.getId(), biObjPar);
			verifyForDependencies(biObjPar);
			if (!errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
				helper.fillResponse(initialPath);
				prepareBIObjectDetailPage(request, response, obj, biObjPar, biObjPar.getId().toString(),
						ObjectsTreeConstants.DETAIL_MOD, false, false);
				return;
			}
			IBIObjectParameterDAO dao = DAOFactory.getBIObjectParameterDAO();
			dao.setUserProfile(profile);
			dao.modifyBIObjectParameter(biObjPar);
		} else {
			biObjPar = DAOFactory.getBIObjectParameterDAO().loadForDetailByObjParId(biObjPar.getId());
		}
		// refresh of the initial_BIObjectParameter in session
		BIObjectParameter biObjParClone = DetBIObjModHelper.clone(biObjPar);
		session.setAttribute("initial_BIObjectParameter", biObjParClone);
		// set lookup objects
		session.setAttribute("LookupBIObject", obj);
		session.setAttribute("LookupBIObjectParameter", biObjPar);
		session.setAttribute("modality", message);
		session.setAttribute("modalityBkp", message);
		response.setAttribute("dependenciesLookup", "true");
	}

	private void lookupReturnBackHandler(SourceBean request, SourceBean response)
			throws SourceBeanException, EMFUserError {

		BIObject obj = (BIObject) session.getAttribute("LookupBIObject");
		BIObjectParameter biObjPar = (BIObjectParameter) session.getAttribute("LookupBIObjectParameter");
		String modality = (String) session.getAttribute("modality");
		if (modality == null)
			modality = (String) session.getAttribute("modalityBkp");

		session.delAttribute("LookupBIObject");
		session.delAttribute("LookupBIObjectParameter");
		session.delAttribute("modality");
		session.delAttribute("modalityBkp");
		helper.fillResponse(initialPath);
		prepareBIObjectDetailPage(request, response, obj, biObjPar, biObjPar.getId().toString(), modality, false,
				false);

	}

	private void lookupReturnHandler(SourceBean request, SourceBean response) throws EMFUserError, SourceBeanException {

		BIObject obj = (BIObject) session.getAttribute("LookupBIObject");
		logger.debug(" BIObject = " + obj);

		BIObjectParameter biObjPar = (BIObjectParameter) session.getAttribute("LookupBIObjectParameter");
		logger.debug(" BIObjectParameter = " + biObjPar);

		String modality = (String) session.getAttribute("modality");
		if (modality == null)
			modality = (String) session.getAttribute("modalityBkp");
		logger.debug(" modality = " + modality);

		String newParIdStr = (String) session.getAttribute("PAR_ID");
		Integer newParIdInt = Integer.valueOf(newParIdStr);
		Parameter newParameter = new Parameter();
		newParameter.setId(newParIdInt);
		biObjPar.setParameter(newParameter);
		biObjPar.setParID(newParIdInt);

		delateLoopbackContext();

		helper.fillResponse(initialPath);
		prepareBIObjectDetailPage(request, response, obj, biObjPar, biObjPar.getId().toString(), modality, false,
				false);
		session.delAttribute("PAR_ID");
	}

	/**
	 * Gets the detail of a BI object choosed by the user from the BI objects list. It reaches the key from the request and asks to the DB all detail BI objects
	 * information, by calling the method <code>loadBIObjectForDetail</code>.
	 *
	 * @param request  The request Source Bean
	 * @param response The response Source Bean
	 * @throws Exception
	 */
	private void getDetailObject(SourceBean request, SourceBean response) throws Exception {
		Map<String, String> logParam = new HashMap<>();
		try {
			String idStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);
			Integer id = new Integer(idStr);
			BIObject obj = biobjDAO.loadBIObjectForDetail(id);

			if (obj == null) {
				logger.error("BIObject with id " + id + " cannot be retrieved.");
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 1040);
				errorHandler.addError(error);
				return;
			}
			Object selectedObjParIdObj = request.getAttribute("selected_obj_par_id");
			String selectedObjParIdStr = "";
			if (selectedObjParIdObj != null) {
				int selectedObjParId = DetBIObjModHelper.findBIObjParId(selectedObjParIdObj);
				selectedObjParIdStr = Integer.toString(selectedObjParId);
			}
			prepareBIObjectDetailPage(request, response, obj, null, selectedObjParIdStr,
					ObjectsTreeConstants.DETAIL_MOD, true, true);
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY", logParam, "ERR");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	/**
	 * Controls if there are some BIObjectParameter objects that depend by the BIObjectParameter object at input, given its id.
	 *
	 * @param objParFatherId The id of the BIObjectParameter object to check
	 * @throws EMFUserError
	 */
	public static EMFValidationError checkForDependancies(Integer objParFatherId) throws EMFUserError {
		EMFValidationError error = null;
		IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
		List<String> objParametersCorrelated = objParuseDAO.getDependencies(objParFatherId);
		if (objParametersCorrelated != null && !objParametersCorrelated.isEmpty()) {
			Map<Object, Object> params = new HashMap<>();
			params.put(AdmintoolsConstants.PAGE, DetailBIObjectModule.MODULE_PAGE);
			List<Object> l = new ArrayList<>();
			l.add(objParametersCorrelated.toString());
			error = new EMFValidationError(EMFErrorSeverity.ERROR, 1049, l, params);
		}
		return error;
	}

	/**
	 * Before modifing a BIObjectParameter (not inserting), this method must be invoked in order to verify that the BIObjectParameter stored into db (to be modified
	 * as per the BIObjectParameter in input) has dependencies associated; if it is the case, verifies that the associated Parameter was not changed. In case of
	 * changed Parameter adds a EMFValidationError into the error handler.
	 *
	 * @param objPar The BIObjectParameter to verify
	 * @throws EMFUserError
	 */
	private void verifyForDependencies(BIObjectParameter objPar) throws EMFUserError {
		Integer objParId = objPar.getId();
		if (objParId == null || objParId.intValue() == -1) {
			// it means that the BIObjectParameter in input must be inserted, not modified
			return;
		}
		// Controls that, if the are some dependencies for the BIObjectParameter, the associated parameter was not changed
		IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
		List<ObjParuse> correlations = objParuseDAO.loadObjParuses(objParId);
		if (correlations != null && !correlations.isEmpty()) {
			IBIObjectParameterDAO objParDAO = DAOFactory.getBIObjectParameterDAO();
			BIObjectParameter initialObjPar = objParDAO.loadForDetailByObjParId(objParId);
			if (initialObjPar.getParID().intValue() != objPar.getParID().intValue()) {
				// the ParameterUse was changed to manual input or the lov id was changed
				Map<Object, Object> params = new HashMap<>();
				params.put(AdmintoolsConstants.PAGE, "DetailBIObjectPage");
				List<Object> l = new ArrayList<>();
				EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, 1061, l, params);
				errorHandler.addError(error);
				return;
			}
		}
	}

	/**
	 * Controls that the BIObjectParameter url name is not in use by another BIObjectParameter
	 *
	 * @param objId    The id of the document
	 * @param biObjPar The BIObjectParameter to control before inserting/modifying
	 */
	private void urlNameControl(Integer objId, BIObjectParameter biObjPar) {
		if (objId == null || objId.intValue() < 0 || biObjPar == null || biObjPar.getParameterUrlName() == null)
			return;
		try {
			IBIObjectParameterDAO objParDAO = DAOFactory.getBIObjectParameterDAO();
			List<BIObjectParameter> paruses = objParDAO.loadBIObjectParametersById(objId);
			Iterator<BIObjectParameter> it = paruses.iterator();
			while (it.hasNext()) {
				BIObjectParameter aBIObjectParameter = it.next();
				if (aBIObjectParameter.getParameterUrlName().equals(biObjPar.getParameterUrlName())
						&& !aBIObjectParameter.getId().equals(biObjPar.getId())) {
					Map<Object, Object> params = new HashMap<>();
					params.put(AdmintoolsConstants.PAGE, DetailBIObjectModule.MODULE_PAGE);
					List<Object> l = new ArrayList<>();
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, 1046, l, params);
					errorHandler.addError(error);
				}
			}
		} catch (HibernateException e) {
			logger.error("Error while url name control", e);
		}

	}

	private BIObjectParameter reloadBIObjectParameter(Integer objId, String objParUrlName) throws EMFInternalError {
		if (objId == null || objId.intValue() < 0 || objParUrlName == null || objParUrlName.trim().equals(""))
			throw new EMFInternalError(EMFErrorSeverity.ERROR,
					"Invalid input data for method reloadBIObjectParameter in DetailBIObjectModule");
		BIObjectParameter objPar = null;
		try {
			IBIObjectParameterDAO objParDAO = DAOFactory.getBIObjectParameterDAO();
			List<BIObjectParameter> paruses = objParDAO.loadBIObjectParametersById(objId);
			Iterator<BIObjectParameter> it = paruses.iterator();
			while (it.hasNext()) {
				BIObjectParameter aBIObjectParameter = it.next();
				if (aBIObjectParameter.getParameterUrlName().equals(objParUrlName)) {
					objPar = aBIObjectParameter;
					break;
				}
			}
		} catch (HibernateException e) {
			logger.error("Cannot reload BIObjectParameter", e);
		}
		if (objPar == null) {
			logger.error("BIObjectParameter with url name '" + objParUrlName + "' not found.");
			objPar = DetBIObjModHelper.createNewBIObjectParameter(objId);
		}
		return objPar;
	}

	private BIObject manageCommunities(BIObject obj, SourceBean request) throws SourceBeanException, EMFUserError {
		List<SbiCommunity> communities = DAOFactory.getCommunityDAO()
				.loadSbiCommunityByUser(((UserProfile) profile).getUserId().toString());
		ILowFunctionalityDAO functDao = DAOFactory.getLowFunctionalityDAO();
		String codeFcomm = (String) request.getAttribute(NAME_ATTR_LIST_COMMUNITIES);
		if (codeFcomm != null && !codeFcomm.equals("")) {
			if (communities != null) {
				for (int i = 0; i < communities.size(); i++) {
					SbiCommunity community = communities.get(i);
					String functCode = community.getFunctCode();
					if (codeFcomm.equals(functCode)) {
						LowFunctionality funct = functDao.loadLowFunctionalityByCode(functCode, false);
						Integer functId = funct.getId();
						List<Integer> functIds = obj.getFunctionalities();
						functIds.add(functId);
					}
				}
			}
		}
		return obj;
	}

	/**
	 * Fills the response SourceBean with the elements that will be displayed in the BIObject detail page: the BIObject itself and the required BIObjectParameter.
	 *
	 * @param response                 The response SourceBean to be filled
	 * @param obj                      The BIObject to be displayed
	 * @param biObjPar                 The BIObjectParameter to be displayed: if it is null the selectedObjParIdStr will be considered.
	 * @param selectedObjParIdStr      The id of the BIObjectParameter to be displayed. If it is blank or null the first BIObjectParameter will be diplayed but in
	 *                                 case the BIObject has no BIObjectParameter a new empty BIObjectParameter will be displayed. If it is "-1" a new empty
	 *                                 BIObjectParameter will be displayed.
	 * @param detail_mod               The modality
	 * @param initialBIObject          Boolean: if true the BIObject to be visualized is the initial BIObject and a clone will be put in session.
	 * @param initialBIObjectParameter Boolean: if true the BIObjectParameter to be visualized is the initial BIObjectParameter and a clone will be put in session.
	 * @throws SourceBeanException
	 * @throws EMFUserError
	 */
	private void prepareBIObjectDetailPage(SourceBean request, SourceBean response, BIObject obj,
			BIObjectParameter biObjPar, String selectedObjParIdStr, String detail_mod, boolean initialBIObject,
			boolean initialBIObjectParameter) throws SourceBeanException, EMFUserError {

		List<BIObjectParameter> biObjParams = DAOFactory.getBIObjectParameterDAO()
				.loadBIObjectParametersById(obj.getId());
		obj.setDrivers(biObjParams);
		if (biObjPar == null) {
			if (selectedObjParIdStr == null || "".equals(selectedObjParIdStr)) {
				if (biObjParams == null || biObjParams.isEmpty()) {
					biObjPar = DetBIObjModHelper.createNewBIObjectParameter(obj.getId());
					selectedObjParIdStr = "-1";
				} else {
					biObjPar = biObjParams.get(0);
					selectedObjParIdStr = biObjPar.getId().toString();
				}
			} else if ("-1".equals(selectedObjParIdStr)) {
				biObjPar = DetBIObjModHelper.createNewBIObjectParameter(obj.getId());
				selectedObjParIdStr = "-1";
			} else {
				int selectedObjParId = Integer.parseInt(selectedObjParIdStr);
				Iterator<BIObjectParameter> it = biObjParams.iterator();
				while (it.hasNext()) {
					biObjPar = it.next();
					if (biObjPar.getId().equals(selectedObjParId))
						break;
				}
			}
		}

		// if going on Drivers List keep track of current infos
		if (biObjPar != null && request.getAttribute("toDriversList") != null
				&& request.getAttribute("toDriversList").equals("true")) {
			getTemporaryParDataFromRequestDetailCase(request, biObjPar);
		}

		response.setAttribute("selected_obj_par_id", selectedObjParIdStr);
		response.setAttribute(NAME_ATTR_OBJECT, obj);
		response.setAttribute(NAME_ATTR_OBJECT_PAR, biObjPar);

		response.setAttribute(ObjectsTreeConstants.MODALITY, detail_mod);

		if (initialBIObject) {
			BIObject objClone = DetBIObjModHelper.clone(obj);
			session.setAttribute("initial_BIObject", objClone);
		}

		if (initialBIObjectParameter) {
			BIObjectParameter biObjParClone = DetBIObjModHelper.clone(biObjPar);
			session.setAttribute("initial_BIObjectParameter", biObjParClone);
		}
	}

	void getTemporaryParDataFromRequestDetailCase(SourceBean request, BIObjectParameter biObjPar) {
		logger.debug("IN");

		String parUrlName = request.getAttribute("parurl_nm") != null ? request.getAttribute("parurl_nm").toString()
				: null;
		if (parUrlName != null) {
			biObjPar.setParameterUrlName(parUrlName);
		}
		String objParLabel = request.getAttribute("objParLabel") != null
				? request.getAttribute("objParLabel").toString()
				: null;
		if (objParLabel != null) {
			biObjPar.setLabel(objParLabel);
		}
		String priority = request.getAttribute("priority") != null ? request.getAttribute("priority").toString() : null;
		if (priority != null) {
			Integer priOInt = Integer.valueOf(priority);
			biObjPar.setPriority(priOInt);
		}
		String view_fl = request.getAttribute("view_fl") != null ? request.getAttribute("view_fl").toString() : null;
		if (view_fl != null) {
			Integer view = Integer.valueOf(view_fl);
			biObjPar.setVisible(view);
		}
		String req_fl = request.getAttribute("req_fl") != null ? request.getAttribute("req_fl").toString() : null;
		if (req_fl != null) {
			Boolean req = Boolean.valueOf(req_fl.equals("1") ? true : false);
			biObjPar.setRequired(req);
		}
		String mult_fl = request.getAttribute("mult_fl") != null ? request.getAttribute("mult_fl").toString() : null;
		if (mult_fl != null) {
			Boolean mult = Boolean.valueOf(mult_fl.equals("1") ? true : false);
			biObjPar.setMultivalue(mult);
		}

		logger.debug("OUT");

	}

	/**
	 * Deletes a BI Object chosen by user. If the folder id is specified, it deletes only the instance of the object in that folder. If the folder id is not
	 * specified: if the user is an administrator the object is deleted from all the folders, else it is deleted from the folder on which the user is a developer.
	 *
	 * @param request  The request SourceBean
	 * @param mod      A request string used to differentiate delete operation
	 * @param response The response SourceBean
	 * @throws EMFUserError        If an Exception occurs
	 * @throws SourceBeanException If a SourceBean Exception occurs
	 */
	private void delDetailObject(SourceBean request, String mod, SourceBean response, IEngUserProfile profile)
			throws EMFUserError, SourceBeanException {
		BIObject obj = null;
		Map<String, String> logParam = new HashMap<>();

		try {
			String idObjStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);
			Integer idObj = new Integer(idObjStr);
			IBIObjectDAO objdao = biobjDAO;
			obj = objdao.loadBIObjectById(idObj);
			if (obj != null) {
				logParam.put("Document_name", obj.getName());
				logParam.put("Document_label", obj.getLabel());
				if (obj.getId() != null)
					logParam.put("Document_id", obj.getId().toString());
			}
			String idFunctStr = (String) request.getAttribute(ObjectsTreeConstants.FUNCT_ID);
			if (idFunctStr != null) {
				Integer idFunct = new Integer(idFunctStr);
				if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
					// deletes the document from the specified folder, no matter the permissions
					objdao.eraseBIObject(obj, idFunct);
				} else {
					// deletes the document from the specified folder if the profile is a developer for that folder
					if (ObjectsAccessVerifier.canDev(obj.getStateCode(), idFunct, profile)) {
						objdao.eraseBIObject(obj, idFunct);
					}
				}
			} else {
				if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
					if (initialPath != null && !initialPath.trim().equals("")) {
						// in case of local administrator, deletes the document in the folders where he can admin
						List<Integer> funcsId = obj.getFunctionalities();
						for (Iterator<Integer> it = funcsId.iterator(); it.hasNext();) {
							Integer idFunct = it.next();
							LowFunctionality folder = DAOFactory.getLowFunctionalityDAO()
									.loadLowFunctionalityByID(idFunct, false);
							String folderPath = folder.getPath();
							if (folderPath.equalsIgnoreCase(initialPath) || folderPath.startsWith(initialPath + "/")) {
								objdao.eraseBIObject(obj, idFunct);
							}
						}
					} else {
						// deletes the document from all the folders, no matter the permissions
						objdao.eraseBIObject(obj, null);
					}
				} else {
					// deletes the document from all the folders on which the profile is a developer
					List<Integer> funcsId = obj.getFunctionalities();
					for (Iterator<Integer> it = funcsId.iterator(); it.hasNext();) {
						Integer idFunct = it.next();
						if (ObjectsAccessVerifier.canDev(obj.getStateCode(), idFunct, profile)) {
							objdao.eraseBIObject(obj, idFunct);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Cannot erase object", ex);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.DELETE", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute("loopback", "true");

		try {
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.DELETE", logParam, "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Instantiates a new <code>BIObject<code> object when a new BI object insertion is required, in order to prepare the page for the insertion.
	 *
	 * @param response The response SourceBean
	 * @throws EMFUserError If an Exception occurred
	 */
	private void newBIObject(SourceBean request, SourceBean response) throws EMFUserError {
		try {

			response.setAttribute(ObjectsTreeConstants.MODALITY, ObjectsTreeConstants.DETAIL_INS);
			BIObject obj = new BIObject();
			obj.setId(0);
			obj.setEngine(null);
			obj.setDataSourceId(null);
			obj.setDataSetId(null);
			obj.setDescription("");
			obj.setLabel("");
			obj.setName("");
			obj.setEncrypt(0);
			obj.setVisible(1);
			obj.setRelName("");
			obj.setStateID(null);
			obj.setStateCode("");
			obj.setBiObjectTypeID(null);
			obj.setBiObjectTypeCode("");
			obj.setRefreshSeconds(0);
			obj.setLockedByUser(null);
			Domain state = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("STATE", "DEV");
			obj.setStateCode(state.getValueCd());
			obj.setStateID(state.getValueId());
			List<Integer> functionalitites = new ArrayList<>();

			obj.setFunctionalities(functionalitites);

			response.setAttribute(NAME_ATTR_OBJECT, obj);
		} catch (Exception ex) {
			logger.error("Cannot prepare page for the insertion", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	/**
	 * Erase version.
	 *
	 * @param request  the request
	 * @param response the response
	 * @throws EMFUserError the EMF user error
	 */
	public void eraseVersion(SourceBean request, SourceBean response) throws EMFUserError {
		// get object' id and name version
		String tempIdStr = (String) request.getAttribute(SpagoBIConstants.TEMPLATE_ID);
		String objIdStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);
		try {
			Integer objId = new Integer(objIdStr);
			Integer tempId = new Integer(tempIdStr);
			DAOFactory.getObjTemplateDAO().deleteBIObjectTemplate(tempId);
			// populate response
			BIObject obj = biobjDAO.loadBIObjectForDetail(objId);
			helper.fillResponse(initialPath);
			prepareBIObjectDetailPage(request, response, obj, null, "", ObjectsTreeConstants.DETAIL_MOD, false, false);
		} catch (Exception e) {
			logger.error("Cannot erase version", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	/**
	 * Clean the SessionContainer from no more useful objects.
	 *
	 * @param request  The request SourceBean
	 * @param response The response SourceBean
	 * @throws SourceBeanException
	 */
	private void exitFromDetail(SourceBean request, SourceBean response) throws SourceBeanException {
		session.delAttribute("initial_BIObject");
		session.delAttribute("initial_BIObjectParameter");
		session.delAttribute("modality");
		response.setAttribute("loopback", "true");
	}

	/**
	 * Inserts/Modifies the detail of a BI Object according to the user request. When a BI Object is modified, the <code>modifyBIObject</code> method is called;
	 * when a new BI Object is added, the <code>insertBIObject</code>method is called. These two cases are differentiated by the <code>mod</code> String input value
	 * .
	 *
	 * @param request  The request information contained in a SourceBean Object
	 * @param mod      A request string used to differentiate insert/modify operations
	 * @param response The response SourceBean
	 * @throws EMFUserError        If an exception occurs
	 * @throws SourceBeanException If a SourceBean exception occurs
	 */
	private void modBIObject(SourceBean request, String mod, SourceBean response)
			throws EMFUserError, SourceBeanException {
		Map<String, String> logParam = new HashMap<>();
		try {

			// build a biobject using data in request
			BIObject obj = helper.recoverBIObjectDetails(mod);

			if (obj != null) {
				logParam.put("Document_name", obj.getName());
				logParam.put("Document_Label", obj.getLabel());
				logParam.put("Creation_User", obj.getCreationUser());
				logParam.put("path", obj.getPath());
				logParam.put("tenant", obj.getTenant());
				logParam.put("state", obj.getStateCode());
				if (obj.getBiObjectTypeID() != null)
					logParam.put("Object Type", obj.getBiObjectTypeID().toString());
				if (obj.getEngine() != null)
					logParam.put("Engine", obj.getEngine().getName());
			}

			boolean flgReloadTemp = false;
			// define variable that contains the id of the parameter selected
			String selectedObjParIdStr = null;
			selectedObjParIdStr = "-1";

			// next attribute defines if load automatically all parameters for a document composition type or not.
			boolean loadParsDCClicked = request.getAttribute("loadParsDC") != null;

			// make a validation of the request data
			ValidationCoordinator.validate("PAGE", "BIObjectValidation", this);

			// build and ObjTemplate object using data into request
			ObjTemplate objTemp = helper.recoverBIObjTemplateDetails();
			if (objTemp != null) {
				objTemp.setBiobjId(obj.getId());
			}
			// if the template is not loaded check if default version is changed
			if (objTemp == null) {
				String strCurTempVer = (String) request.getAttribute("versionTemplate");
				if (strCurTempVer != null && !strCurTempVer.equals("")) {
					Integer idCurTempVer = Integer.valueOf((strCurTempVer).trim());
					if (idCurTempVer != null) {
						objTemp = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(obj.getId());
						if (objTemp.getId().compareTo(idCurTempVer) != 0) {
							flgReloadTemp = true;
							List<ObjTemplate> lstTemplatesObj = DAOFactory.getObjTemplateDAO()
									.getBIObjectTemplateList(obj.getId());
							for (int i = 0; i < lstTemplatesObj.size(); i++) {
								objTemp = lstTemplatesObj.get(i);
								if (objTemp.getId().compareTo(idCurTempVer) == 0)
									break;
							}
						}
					}
				}
			} else
				flgReloadTemp = true;

			// if there are some validation errors into the errorHandler return without write into DB
			if (!errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
				helper.fillResponse(initialPath);
				prepareBIObjectDetailPage(request, response, obj, null, selectedObjParIdStr, mod, false, false);

				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_VALIDATION_ERROR", logParam,
						"ERR");
				return;
			}
			// manage communities
			manageCommunities(obj, request);
			// based on the modality do different tasks
			if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
				// inserts into DB the new BIObject
				if (objTemp == null) {
					biobjDAO.insertBIObject(obj, loadParsDCClicked);
				} else {
					biobjDAO.insertBIObject(obj, objTemp, loadParsDCClicked);
				}

				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.ADD", logParam, "OK");
			} else if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {

				BIObjectParameter biObjPar = null;
				Object selectedObjParIdObj = request.getAttribute("selected_obj_par_id");
				Object deleteBIObjectParameter = request.getAttribute("deleteBIObjectParameter");
				if (selectedObjParIdObj != null) {

					// it is requested to view another BIObjectParameter than the one visible
					int selectedObjParId = helper.findBIObjParId(selectedObjParIdObj);
					selectedObjParIdStr = Integer.toString(selectedObjParId);
					String saveBIObjectParameter = (String) request.getAttribute("saveBIObjectParameter");
					if (saveBIObjectParameter != null && saveBIObjectParameter.equalsIgnoreCase("yes")) {
						// it is requested to save the visible BIObjectParameter
						ValidationCoordinator.validate("PAGE", "BIObjectParameterValidation", this);
						biObjPar = helper.recoverBIObjectParameterDetails(obj.getId());
						// If it's a new BIObjectParameter or if the Parameter was changed controls
						// that the BIObjectParameter url name is not already in use
						urlNameControl(obj.getId(), biObjPar);
						helper.fillResponse(initialPath);
						verifyForDependencies(biObjPar);
						// if there are some validation errors into the errorHandler does not write into DB
						if (!errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
							helper.fillResponse(initialPath);
							prepareBIObjectDetailPage(request, response, obj, biObjPar, biObjPar.getId().toString(),
									ObjectsTreeConstants.DETAIL_MOD, false, false);

							AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD",
									logParam, "OK");
							return;
						}
						IBIObjectParameterDAO objParDAO = DAOFactory.getBIObjectParameterDAO();
						objParDAO.setUserProfile(profile);
						if (biObjPar.getId().intValue() == -1) {
							// it is requested to insert a new BIObjectParameter
							objParDAO.insertBIObjectParameter(biObjPar);
						} else {
							// it is requested to modify a BIObjectParameter
							objParDAO.modifyBIObjectParameter(biObjPar);
						}
						prepareBIObjectDetailPage(request, response, obj, null, selectedObjParIdStr,
								ObjectsTreeConstants.DETAIL_MOD, false, true);

						logParam.put("Document_name", obj.getName());
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD", logParam,
								"OK");
						return;
					} else {
						helper.fillResponse(initialPath);
						prepareBIObjectDetailPage(request, response, obj, null, selectedObjParIdStr,
								ObjectsTreeConstants.DETAIL_MOD, false, true);
						// exits without writing into DB

						logParam.put("Document_name", obj.getName());
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD", logParam,
								"OK");
						return;
					}

				} else if (deleteBIObjectParameter != null) {

					// it is requested to delete the visible BIObjectParameter
					int objParId = helper.findBIObjParId(deleteBIObjectParameter);
					Integer objParIdInt = objParId;
					EMFValidationError error = checkForDependancies(objParIdInt);
					if (error != null) {
						errorHandler.addError(error);
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD", logParam,
								"KO");
					}
					helper.fillResponse(initialPath);
					// if there are some validation errors into the errorHandler does not write into DB
					if (!errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
						helper.fillResponse(initialPath);
						prepareBIObjectDetailPage(request, response, obj, biObjPar, biObjPar.getId().toString(),
								ObjectsTreeConstants.DETAIL_MOD, false, false);
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD", logParam,
								"KO");
						return;
					}
					// deletes the BIObjectParameter
					IBIObjectParameterDAO objParDAO = DAOFactory.getBIObjectParameterDAO();
					BIObjectParameter objPar = objParDAO.loadForDetailByObjParId(objParId);
					objParDAO.eraseBIObjectParameter(objPar, true);
					selectedObjParIdStr = "";
					prepareBIObjectDetailPage(request, response, obj, null, selectedObjParIdStr,
							ObjectsTreeConstants.DETAIL_MOD, false, true);

					logParam.put("Document_name", obj.getName());
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD", logParam,
							"OK");
					return;

				} else {

					// It is request to save the BIObject with also the visible BIObjectParameter
					biObjPar = helper.recoverBIObjectParameterDetails(obj.getId());
					// If a new BIParameter was visualized and no fields were inserted, the BIParameter is not validated and saved
					boolean biParameterToBeSaved = true;
					if ((obj.getBiObjectTypeCode().equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITE_TYPE)
							&& flgReloadTemp)
							|| (GenericValidator.isBlankOrNull(biObjPar.getLabel()) && biObjPar.getId().intValue() == -1
									&& GenericValidator.isBlankOrNull(biObjPar.getParameterUrlName())
									&& biObjPar.getParID().intValue() == -1))
						biParameterToBeSaved = false;
					if (biParameterToBeSaved) {
						ValidationCoordinator.validate("PAGE", "BIObjectParameterValidation", this);
						// If it's a new BIObjectParameter or if the Parameter was changed controls
						// that the BIObjectParameter url name is not already in use
						urlNameControl(obj.getId(), biObjPar);
					}
					ValidationCoordinator.validate("PAGE", "BIObjectValidation", this);
					verifyForDependencies(biObjPar);
					// if there are some validation errors into the errorHandler does not write into DB
					if (!errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
						helper.fillResponse(initialPath);
						prepareBIObjectDetailPage(request, response, obj, biObjPar, biObjPar.getId().toString(),
								ObjectsTreeConstants.DETAIL_MOD, false, false);

						logParam.put("Document_name", obj.getName());
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY_DETAIL_MOD", logParam,
								"OK");
						return;
					}

					// it is requested to modify the main values of the BIObject
					if (objTemp == null) {
						biobjDAO.modifyBIObject(obj, loadParsDCClicked);
					} else {
						biobjDAO.modifyBIObject(obj, objTemp, loadParsDCClicked);
					}

					// reloads the BIObject
					obj = biobjDAO.loadBIObjectForDetail(obj.getId());
					// check if there's a parameter to save and in case save it
					if (biParameterToBeSaved) {
						IBIObjectParameterDAO objParDAO = DAOFactory.getBIObjectParameterDAO();
						objParDAO.setUserProfile(profile);
						if (biObjPar.getId().intValue() == -1) {
							// it is requested to insert a new BIObjectParameter
							objParDAO.insertBIObjectParameter(biObjPar);
							// reload the BIObjectParameter with the given url name
							biObjPar = reloadBIObjectParameter(obj.getId(), biObjPar.getParameterUrlName());
						} else {
							// it is requested to modify a BIObjectParameter
							objParDAO.modifyBIObjectParameter(biObjPar);
						}
						selectedObjParIdStr = biObjPar.getId().toString();
					} else
						selectedObjParIdStr = "-1";
				}

				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.MODIFY", logParam, "OK");
			}

			// reloads the BIObject with the correct Id
			obj = biobjDAO.loadBIObjectForDetail(obj.getId());

			// Save relation between dataset and document (1:1) (for Data Lineage/Impact Analysis)
			DAOFactory.getSbiObjDsDAO().insertRelationsFromObj(obj);

			// based on the kind of back put different data into response
			Object saveAndGoBack = request.getAttribute("saveAndGoBack");
			if (saveAndGoBack != null) {
				// it is request to save the main BIObject details and to go back
				response.setAttribute("loopback", "true");
			} else {
				// it is requested to save and remain in the BIObject detail page
				response.setAttribute(ObjectsTreeConstants.OBJECT_ID, obj.getId().toString());
				response.setAttribute("selected_obj_par_id", selectedObjParIdStr);
				response.setAttribute("saveLoop", "true");
			}

		} catch (EMFUserError error) {
			logger.error("Cannot fill response container", error);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.ADD/MODIFY", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw error;
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "DOCUMENT.ADD/MODIFY", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}
}
