/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionManager;
import it.eng.spagobi.analiticalmodel.execution.service.SelectParametersLookupModule;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Executes a report, according to three phases; each phase is identified by a
 * message string.
 * <p>
 * 1) Creates the page
 * <p>
 * 2) Selects the role
 * <p>
 * 3) From the field input values loads the object and starts execution
 * 
 * @author Zerbetto
 * @author Fiscato
 * @author Bernabei
 * @author Mark Penningroth (Cincom Systems, Inc.)
 */
public class ExecuteBIObjectModule extends AbstractHttpModule {

	static private Logger logger = Logger.getLogger(ExecuteBIObjectModule.class);

	EMFErrorHandler errorHandler = null;
	RequestContainer requestContainer = null;
	SessionContainer permanentSession = null;
	CoreContextManager contextManager = null;

	public static final String MODULE_PAGE = "ExecuteBIObjectPage";
	public static final String MESSAGE_EXECUTION = "MESSAGEEXEC";
	public static final String SUBMESSAGEDET = "SUBMESSAGEDET";

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Manage all the request in order to exec all the different BIObject
	 * execution phases.
	 * 
	 * @param request The request source bean
	 * @param response The response Source bean
	 * 
	 * @throws Exception If an Exception occurred
	 */
	public void service(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");

		String messageExec = (String) request
		.getAttribute(SpagoBIConstants.MESSAGEDET);
		logger.debug("using message" + messageExec);
		String subMessageExec = (String) request.getAttribute(SUBMESSAGEDET);
		logger.debug("using sub-message" + subMessageExec);
		// if submessage is valorized it gives the value to message
		if (subMessageExec != null && !subMessageExec.equals(""))
			messageExec = subMessageExec;
		errorHandler = getErrorHandler();
		requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		contextManager = new CoreContextManager(new SpagoBISessionContainer(session), 
				new LightNavigatorContextRetrieverStrategy(request));

		permanentSession = session.getPermanentContainer();
		logger.debug("errorHanlder, requestContainer, session, permanentSession retrived ");

		try {
			if (messageExec == null || messageExec.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_CREATE_PAGE)) {
				initNewExecutionHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_SELECTED_ROLE)) {
				initNewExecutionHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_RUN_SUBOJECT)) {
				executionSubObjectHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_DELETE_SUBOJECT)) {
				deleteSubObjectHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_RETURN_FROM_LOOKUP)) {
				lookUpReturnHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_RUN)) {
				executionHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_PHASE_REFRESH)) {
				refreshHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_SNAPSHOT_MESSAGE)) {
				execSnapshotHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.ERASE_SNAPSHOT_MESSAGE)) {
				eraseSnapshotHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.VIEWPOINT_SAVE)) {
				saveViewPoint(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.VIEWPOINT_ERASE)) {
				eraseViewpoint(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.VIEWPOINT_EXEC)) {
				execViewpoint(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.VIEWPOINT_VIEW)) {
				viewViewpoint(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.EXEC_CROSS_NAVIGATION)) {
				executeCrossNavigationHandler(request, response);
			} else if (messageExec
					.equalsIgnoreCase(SpagoBIConstants.RECOVER_EXECUTION_FROM_CROSS_NAVIGATION)) {
				recoverExecutionFromCrossNavigationHandler(request, response);
			}
//			else if (messageExec.equalsIgnoreCase(SpagoBIConstants.SELECT_ALL)) {
//				selectAllValueForPar(request, response);
//			}
			else if (messageExec.equalsIgnoreCase(SpagoBIConstants.DESELECT_ALL)) {
				selectNoneValueForPar(request, response);
			}

		} catch (EMFUserError e) {
			errorHandler.addError(e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void recoverExecutionFromCrossNavigationHandler(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		try {
			// recovers required execution details
			String executionFlowId = (String) request.getAttribute("EXECUTION_FLOW_ID");
			String executionId = (String) request.getAttribute("EXECUTION_ID");
			ExecutionManager executionManager = (ExecutionManager) contextManager.get(ExecutionManager.class.getName());
			if (executionManager == null) {
				throw new Exception("Execution Manager not found. Cannot recover execution details.");
			}
			ExecutionInstance instance = executionManager.recoverExecution(executionFlowId, executionId);
			// set execution instance in session
			setExecutionInstance(instance);
			// sets the flag in order to skip snapshots/viewpoints/parameters/subobjects page
			request.setAttribute(SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS, "true");
			// starts new execution
			executionHandler(request, response);
		} finally {
			logger.debug("OUT");
		}
	}

	private void executeCrossNavigationHandler(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		try {
			ExecutionInstance instance = getExecutionInstance();
			// registers the current execution in the ExecutionManager
			ExecutionManager executionManager = (ExecutionManager) contextManager.get(ExecutionManager.class.getName());
			if (executionManager == null) {
				executionManager = new ExecutionManager();
				contextManager.set(ExecutionManager.class.getName(), executionManager);
			}
			executionManager.registerExecution(instance);
			// starts new execution 
			request.setAttribute(SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS, "true");
			initNewExecutionHandler(request, response);
		} finally {
			logger.debug("OUT");
		}
	}

	private void eraseSnapshotHandler(SourceBean request, SourceBean response)
	throws EMFUserError, SourceBeanException, NumberFormatException, EMFInternalError {
		logger.debug("IN");
		UserProfile profile = (UserProfile) this.getUserProfile();
		// only if user is administrator, he can erase snapshots
		if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
			ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
			List snapshotIdsList = request.getAttributeAsList(SpagoBIConstants.SNAPSHOT_ID);
			if (snapshotIdsList != null && !snapshotIdsList.isEmpty()) {
				Iterator it = snapshotIdsList.iterator();
				while (it.hasNext()) {
					String snapshotIdStr = (String) it.next();
					Integer snapId = new Integer(snapshotIdStr);
					logger.error("Deleting snaphost with id = " + snapId + " ...");
					snapdao.deleteSnapshot(snapId);
				}
			}
		} else {
			logger.error("Current user [" + profile.getUserId().toString() + "] CANNOT erase snapshots!!");
		}
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
		logger.debug("OUT");
	}

	/**
	 * Starts a new execution
	 * 
	 * @param request
	 *            The Spago Request SourceBean
	 * @param response
	 *            The Spago Response SourceBean
	 */
	private void initNewExecutionHandler(SourceBean request, SourceBean response)
	throws Exception {

		logger.debug("IN");

		// get the current user profile
		IEngUserProfile profile = getUserProfile();
		BIObject obj = getRequiredBIObject(request);
		// get the list of the subObjects
		List subObjects = getSubObjectsList(obj, profile);
		// get the list of snapshots
		List snapshots = getSnapshotList(obj);
		// get the list of viewpoints
		List viewpoints = getViewpointList(obj);
		// get required snapshot
		Snapshot snapshot = getRequiredSnapshot(request, snapshots);
		// get required subObject
		SubObject subObj = getRequiredSubObject(request, subObjects);
		// get parameters
		String userProvidedParametersStr = (String) request.getAttribute(ObjectsTreeConstants.PARAMETERS);
		logger.debug("Used defined parameters: [" + userProvidedParametersStr + "]");
		// get execution modality
		String modality = (String) request.getAttribute(ObjectsTreeConstants.MODALITY);
		if (modality == null) modality = SpagoBIConstants.NORMAL_EXECUTION_MODALITY;
		logger.debug("Execution modality: [" + modality + "]");

		Integer id = obj.getId();
		logger.debug("BIObject id = " + id);

		boolean canSee = ObjectsAccessVerifier.canSee(obj, profile);
		if (!canSee) {
			logger.error("Object with label = '" + obj.getLabel()
					+ "' cannot be executed by the user!!");
			Vector v = new Vector();
			v.add(obj.getLabel());
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1075", v, null);
		}
		// get all correct execution roles
		List correctRoles = getCorrectRolesForExecution(profile, id);
		if (correctRoles == null || correctRoles.size() == 0) {
			logger.warn("Object cannot be executed by no role of the user");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1006);
		}
		// get role specified on request
		String role = (String) request.getAttribute(SpagoBIConstants.ROLE);
		if (role != null && !correctRoles.contains(role)) {
			logger.warn("Role [" + role + "] is not a correct role for execution");
			Vector v = new Vector();
			v.add(role);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1078, v, null);
		}

		// if role is not set, sees if role selection is required 
		if (role == null) {
			if (snapshot != null || subObj != null) {
				// for executing a snapshot or a subObject, role selection is not mandatory, so the first role is selected
				role = (String) correctRoles.get(0);
			} else {
				if (correctRoles.size() > 1) {
					response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectSelectRole");
					response.setAttribute("roles", correctRoles);
					response.setAttribute(ObjectsTreeConstants.OBJECT_ID, id);
					logger.debug("more than one correct roles for execution, redirect to the role selection page");
					return;
				} else {
					role = (String) correctRoles.get(0);
				}
			}
		}

		// instantiates a new Execution controller for the current execution
		ExecutionInstance instance = createExecutionInstance(id, role, profile, request, modality);
		// put execution instance in session
		contextManager.set(ExecutionInstance.class.getName(), instance);
		instance.refreshParametersValues(request, true);
		instance.setParameterValues(userProvidedParametersStr, true);
		// refresh obj variable because createExecutionInstance load the BIObject in a different way
		obj = instance.getBIObject();

		// if a snapshot is required, executes it
		if (snapshot != null) {
			executeSnapshot(snapshot, response);
			return;
		}

		// TODO cancellare quando anche la check list è nella finestra di lookup
		Map paramsDescriptionMap = new HashMap();
		List biparams = obj.getBiObjectParameters();
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
			String nameUrl = biparam.getParameterUrlName();
			paramsDescriptionMap.put(nameUrl, "");
		}
		contextManager.set("PARAMS_DESCRIPTION_MAP", paramsDescriptionMap);

		// finds if it is requested to ignore subobjects/snapshots/viewpoints if present
		String ignoreSubNodesStr = (String) request.getAttribute(SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS);
		boolean ignoreSubNodes = false;
		if (ignoreSubNodesStr != null && ignoreSubNodesStr.trim().equalsIgnoreCase("true")) {
			ignoreSubNodes = true;
		}
		
		// finds if it is not important that all parameters have a value
		String runAnyway = (String) request.getAttribute(SpagoBIConstants.RUN_ANYWAY);
		boolean runAnywayB = false;
		if (runAnyway != null && runAnyway.trim().equalsIgnoreCase("true")) {
			runAnywayB = true;
		}

		// check parameters values 
		List errors = instance.getParametersErrors();

		// (if the object can be directly executed (because it hasn't any parameter to be
		// filled by the user) and if the object has no subobject / snapshots / viewpoints saved
		// or the request esplicitely asks to ignore subnodes) or (a valid subobject
		// is specified by request) then execute it directly without pass through parameters page
		if (((instance.isDirectExecution()||runAnywayB) && ((subObjects.size() == 0
				&& snapshots.size() == 0 && viewpoints.size() == 0) || ignoreSubNodes))
				|| subObj != null) {

			logger.debug("Document can be directly executed");
			// add errors into error handler if any
			if (errors.size() != 0) {
				Iterator errorsIt = errors.iterator();
				while (errorsIt.hasNext()) {
					errorHandler.addError((EMFUserError) errorsIt.next());
				}
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
				return;
			}
			execute(instance, subObj, null, response);
		} else {
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
		}
		logger.debug("OUT");
	}

	/**
	 * creates a new ExecutionInstance
	 * 
	 * @param contextManager The object for session access
	 * @param aRoleName the a role name
	 * @param biobjectId the id of the current document
	 * @param userProvidedParametersStr the user provided parameters str
	 * @param profile the user profile
	 * 
	 * @return the BI object
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	private ExecutionInstance createExecutionInstance(Integer biobjectId, String aRoleName, IEngUserProfile profile, SourceBean request, String modality) throws EMFUserError {
		// create execution id
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		String executionId = uuidObj.toString();
		executionId = executionId.replaceAll("-", "");
		// find execution flow id; it is not specified, it means that a new flow is starting, so it is set to execution id value
		String executionFlowId = (String) request.getAttribute("EXECUTION_FLOW_ID");
		logger.debug("Execution flow id request parameter: " + executionFlowId);
		if (executionFlowId == null) 
			executionFlowId = executionId;
		// find if toolbar must be displayed or not, default value is true
		String displayToolbarStr = (String) request.getAttribute(SpagoBIConstants.TOOLBAR_VISIBLE);
		logger.debug("Display toolbar request parameter: " + displayToolbarStr);
		if (displayToolbarStr == null || displayToolbarStr.trim().equals("")) displayToolbarStr = "true";
		boolean displayToolbar = Boolean.parseBoolean(displayToolbarStr);
		// find if sliders must be displayed or not, default value is true
		String displaySliderStr = (String) request.getAttribute(SpagoBIConstants.SLIDERS_VISIBLE);
		logger.debug("Display sliders request parameter: " + displaySliderStr);
		if (displaySliderStr == null || displaySliderStr.trim().equals("")) displaySliderStr = "true";
		boolean displaySlider = Boolean.parseBoolean(displaySliderStr);
		// create new execution instance
		ExecutionInstance instance = null;
		try {
			instance = new ExecutionInstance(profile, executionFlowId, executionId, biobjectId, aRoleName, modality, displayToolbar, displaySlider, null);
		} catch (Exception e) {
			logger.error(e);
		}
		return instance;
	}

	private List getCorrectRolesForExecution(IEngUserProfile profile, Integer id) throws EMFInternalError, EMFUserError {
		logger.debug("IN");
		List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(id, profile);
		logger.debug("OUT");
		return correctRoles;
	}

	/**
	 * Get the required BIObject by attribute "OBJECT_ID" or "OBJECT_LABEL" on request.
	 * @param request The service request
	 * @return the required BIObject
	 * @throws EMFUserError if the document is not found or if request attribute "OBJECT_ID" or "OBJECT_LABEL" are missing
	 */
	private BIObject getRequiredBIObject(SourceBean request) throws EMFUserError {
		logger.debug("IN");
		BIObject obj = null;
		String idStr = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_ID);
		String label = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_LABEL);
		logger.debug("Request parameters: " + "biobject id = [" + idStr + "]; object label = [" + label + "].");
		if (idStr == null && label == null) {
			logger.error("Cannot load BIObject: neither OBJECT_ID nor OBJECT_LABEL are specified on request");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1083");
		}
		if (label != null) {
			logger.debug("Loading biobject with label = [" + label + "] ...");
			obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
			if (obj == null) {
				logger.error("Object with label = [" + label + "] not found!!");
				Vector v = new Vector();
				v.add(label);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1074", v);
			}
		} else {
			logger.debug("Loading biobject with id = [" + idStr + "] ...");
			Integer id = new Integer(idStr);
			try {
				obj = DAOFactory.getBIObjectDAO().loadBIObjectById(id);
			} catch (EMFUserError error) {
				logger.error("Object with id = [" + idStr + "] not found!!");
				Vector v = new Vector();
				v.add(idStr);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1082", v);
			}
		}
		logger.debug("OUT");
		return obj;
	}

	/**
	 * Finds the subobject required by the request, if any.
	 * It consider only the subobejcts of the current document, so if a subobject of another document is required it is not found.
	 * It loads the current document's subobjects and invokes the getRequiredSubObject(request, subobjects)  
	 * 
	 * @param request
	 * @return the required subobject
	 * @throws Exception
	 */
	private SubObject getRequiredSubObject(SourceBean request) throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		BIObject obj = instance.getBIObject();
		List subObjects = getSubObjectsList(obj, getUserProfile());
		logger.debug("OUT");
		return getRequiredSubObject(request, subObjects);
	}

	/**
	 * Find the subobject with the name specified by the attribute
	 * "LABEL_SUB_OBJECT" or "SUBOBJECT_ID" on request among the list of subobjects in input (that must be the current 
	 * document's subobjects list); if those attributes are missing, null is returned. 
	 * If such a subobject does not exist or the current user is not able to see that subobject, an error is added into the Error Handler
	 * and null is returned.
	 * @param request The service request
	 * @param subObjects The list of all existing subobjects for the current document
	 * @return the required subobject
	 */
	// MPenningroth 25-JAN-2008
	// Handle new LABEL_SUB_OBJECT Preference
	private SubObject getRequiredSubObject(SourceBean request, List subObjects) {
		logger.debug("IN");
		SubObject subObj = null;
		String subObjectName = (String) request.getAttribute(SpagoBIConstants.SUBOBJECT_NAME);
		String subObjectIdStr = (String) request.getAttribute(SpagoBIConstants.SUBOBJECT_ID);
		if (subObjectName == null && subObjectIdStr == null) {
			logger.debug("Neither LABEL_SUB_OBJECT nor SUBOBJECT_ID attribute in request are specified. Returning null.");
			return null;
		}
		if (subObjectName != null) {
			logger.debug("Looking for subobject with name [" + subObjectName + "] ...");
			if (subObjects != null && subObjects.size() > 0) {
				Iterator iterSubs = subObjects.iterator();
				while (iterSubs.hasNext() && subObj == null) {
					SubObject sd = (SubObject) iterSubs.next();
					if (sd.getName().equals(subObjectName.trim())) {
						subObj = sd;
						break;
					}
				}
			}
		} else {
			logger.debug("Looking for subobject with id [" + subObjectIdStr + "] ...");
			Integer subObjId = new Integer(subObjectIdStr);
			if (subObjects != null && subObjects.size() > 0) {
				Iterator iterSubs = subObjects.iterator();
				while (iterSubs.hasNext() && subObj == null) {
					SubObject sd = (SubObject) iterSubs.next();
					if (sd.getId().equals(subObjId)) {
						subObj = sd;
						break;
					}
				}
			}
		}
		// case subobject not found
		if (subObj == null) {
			logger.error("Subobject not found.");
			List l = new ArrayList();
			l.add(subObjectName);
			EMFUserError userError = new EMFUserError(
					EMFErrorSeverity.ERROR, 1080, l);
			errorHandler.addError(userError);
		} else {
			boolean canSeeSubobject = canSeeSubobject(getUserProfile(), subObj);
			if (!canSeeSubobject) {
				List l = new ArrayList();
				l.add(subObj.getName());
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 1079, l);
				errorHandler.addError(userError);
				subObj = null;
			}
		}
		logger.debug("OUT");
		return subObj;
	}

	/**
	 * Finds the snapshot required by the request, if any.
	 * It consider only the snapshots of the current document, so if a snapshots of another document is required it is not found.
	 * It loads the current document's snapshots and invokes the getRequiredSnapshot(request, snapshots)  
	 * @param request The service request
	 * @return the required snapshot
	 */
	private Snapshot getRequiredSnapshot(SourceBean request) throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		BIObject obj = instance.getBIObject();
		List snapshots = getSnapshotList(obj);
		logger.debug("OUT");
		return getRequiredSnapshot(request, snapshots);
	}

	/**
	 * Find the snapshot with the name specified by the attribute
	 * "SNAPSHOT_NAME" and number specified by "SNAPSHOT_NUMBER" or by 
	 * the attribute "SNAPSHOT_ID" on request among the list of snapshots in input (that must be the current 
	 * document's snapshots list); if SNAPSHOT_NAME attribute is missing, null is returned. 
	 * If such a snapshot does not exist an error is added into the Error Handler
	 * and null is returned.
	 * @param request The service request
	 * @param snapshots The list of existing snapshots for the current document
	 * @return the required snapshot
	 */
	private Snapshot getRequiredSnapshot(SourceBean request, List snapshots) {
		logger.debug("IN");
		Snapshot snapshot = null;
		String snapshotIdStr = (String) request.getAttribute(SpagoBIConstants.SNAPSHOT_ID);
		String snapshotName = (String) request.getAttribute(SpagoBIConstants.SNAPSHOT_NAME);
		if (snapshotName == null && snapshotIdStr == null) {
			logger.debug("Neither SNAPSHOT_NAME nor SNAPSHOT_ID are specified on request. Returning null.");
			return null;
		}
		// get also the snapshot number
		String snapshotNumberStr = (String) request.getAttribute(SpagoBIConstants.SNAPSHOT_HISTORY_NUMBER);
		int snapshotNumber = 0;
		if (snapshotNumberStr != null) {
			try {
				snapshotNumber = new Integer(snapshotNumberStr).intValue();
			} catch (Exception e) {
				logger.error("Snapshot history specified [" + snapshotNumberStr + "] is not a valid integer number, using default 0");
				snapshotNumber = 0;
			}
		}
		if (snapshotName != null) {
			logger.debug("Looking for snapshot with name [" + snapshotName + "] and number [" + snapshotNumberStr + "] ...");
			try {
				snapshot = SchedulerUtilities.getNamedHistorySnapshot(snapshots, snapshotName, snapshotNumber);
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			try {
				Integer snapshotId = new Integer(snapshotIdStr);
				Iterator it = snapshots.iterator();
				while (it.hasNext() && snapshot == null) {
					Snapshot aSnapshot = (Snapshot) it.next();
					if (aSnapshot.getId().equals(snapshotId)) {
						snapshot = aSnapshot;
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		if (snapshot == null) {
			logger.error("Snapshot not found.");
			List l = new ArrayList();
			l.add(snapshotName);
			l.add(snapshotNumberStr);
			EMFUserError userError = new EMFUserError(
					EMFErrorSeverity.ERROR, 1081, l);
			errorHandler.addError(userError);
		}
		logger.debug("OUT");
		return snapshot;
	}

	/**
	 * Controls if the user can execute the input subobject
	 * 
	 * @param subObj The subobject to be executed
	 * @param profile The user profile
	 * @return true if the user can see the subobject, false otherwise
	 */
	private boolean canSeeSubobject(IEngUserProfile profile, SubObject subObj) {
		logger.debug("IN");
		boolean toReturn = true;
		if (!subObj.getIsPublic().booleanValue()
				&& !subObj.getOwner().equals(
						((UserProfile)profile).getUserId())) {
			toReturn = false;
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Find bi obj par id.
	 * 
	 * @param parIdObj the par id obj
	 * 
	 * @return the int
	 */
	public int findBIObjParId(Object parIdObj) {
		logger.debug("IN");
		String parIdStr = "";
		if (parIdObj instanceof String) {
			parIdStr = (String) parIdObj;
		} else if (parIdObj instanceof List) {
			List parIdList = (List) parIdObj;
			Iterator it = parIdList.iterator();
			while (it.hasNext()) {
				Object item = it.next();
				if (item instanceof SourceBean)
					continue;
				if (item instanceof String)
					parIdStr = (String) item;
			}
		}
		int parId = Integer.parseInt(parIdStr);
		logger.debug("OUT");
		return parId;
	}

	private Object getAttributeFromSession(SourceBean request, String attributeName) {
		logger.debug("IN");
		Object attribute = null;
		attribute = request.getAttribute(attributeName);
		if (attribute == null) {
			attribute = contextManager.get(attributeName);
			if (attribute != null)
				contextManager.remove(attributeName);
		}
		logger.debug("OUT");
		return attribute;
	}

	/**
	 * Gets the as list.
	 * 
	 * @param o the o
	 * 
	 * @return the as list
	 */
	public List getAsList(Object o) {
		logger.debug("IN");
		ArrayList list = new ArrayList();

		if (o instanceof String) {
			String parameterValueFromLookUp = (String) o;
			list.add(parameterValueFromLookUp);
		} else {
			list.addAll((Collection) o);
		}
		logger.debug("OUT");
		return list;
	}

	/**
	 * Called after the parameter value lookup selection to continue the
	 * execution phase
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void lookUpReturnHandler(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		// get the object from the session
		BIObject obj = instance.getBIObject();
		// get the parameter name and value from the request
		String parameterNameFromLookUp = (String) request
		.getAttribute("LOOKUP_PARAMETER_NAME");
		if (parameterNameFromLookUp == null)
			parameterNameFromLookUp = contextManager.getString("LOOKUP_PARAMETER_NAME");

		String returnStatus = (String) getAttributeFromSession(request, "RETURN_STATUS");
		if (returnStatus == null)
			returnStatus = "OK";

		Object lookUpValueObj = getAttributeFromSession(request, "LOOKUP_VALUE");
		Object lookUpDescObj = getAttributeFromSession(request, "LOOKUP_DESC");

		if (lookUpValueObj != null && !returnStatus.equalsIgnoreCase("ABORT")) {

			List paramValues = getAsList(lookUpValueObj);
			List paramDescriptions = (lookUpDescObj == null) ? paramValues
					: getAsList(lookUpDescObj);

			// Set into the righr object parameter the list value
			List biparams = obj.getBiObjectParameters();
			Iterator iterParams = biparams.iterator();
			while (iterParams.hasNext()) {
				BIObjectParameter biparam = (BIObjectParameter) iterParams
				.next();
				String nameUrl = biparam.getParameterUrlName();

				if (nameUrl.equalsIgnoreCase(parameterNameFromLookUp)) {
					biparam.setParameterValues(paramValues);

					// refresh also the description
					HashMap paramsDescriptionMap = (HashMap) contextManager.get("PARAMS_DESCRIPTION_MAP");
					String desc = "";
					for (int i = 0; i < paramDescriptions.size(); i++) {
						desc += (i == 0 ? "" : ";")
						+ paramDescriptions.get(i).toString();
					}
					paramsDescriptionMap.put(nameUrl, desc);
				}
			}
		}
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
		"ExecuteBIObjectPageParameter");
		logger.debug("OUT");
	}

	/**
	 * Delete a subObject of the current document
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void deleteSubObjectHandler(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		UserProfile profile = (UserProfile) getUserProfile();
		String userId = profile.getUserId().toString();
		ISubObjectDAO subobjdao = DAOFactory.getSubObjectDAO();
		List subobjectsIdsList = request.getAttributeAsList(SpagoBIConstants.SUBOBJECT_ID);
		if (subobjectsIdsList != null && !subobjectsIdsList.isEmpty()) {
			Iterator it = subobjectsIdsList.iterator();
			while (it.hasNext()) {
				String subobjectIdStr = (String) it.next();
				Integer subobjectId = new Integer(subobjectIdStr);
				// check if the user is able to erase the subobject
				boolean canDelete = false;
				// if user is administrator, he can delete it
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
					canDelete = true;
				} else {
					// if user is not administrator, he can delete it only if he is the owner
					SubObject subobject = subobjdao.getSubObject(subobjectId);
					if (subobject == null) {
						logger.warn("Subobject with id = " + subobjectId + " not found!");
						continue;
					}
					if (subobject.getOwner().equals(userId)) {
						canDelete = true;
					}
				}
				if (canDelete) {
					logger.error("Deleting subobject with id = " + subobjectId + " ...");
					subobjdao.deleteSubObject(subobjectId);
				} else {
					logger.error("Current user [" + userId + "] CANNOT erase subobject with id = " + subobjectId);
				}
			}
		}
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
		logger.debug("OUT");
	}

	/**
	 * Get the list of subObjects of a BIObject for the current user
	 * 
	 * @param obj
	 *            BIObject container of the subObjects
	 * @param profile
	 *            profile of the user
	 * @return the List of the BIObject's subobjects visible to the current user
	 */
	private List getSubObjectsList(BIObject obj, IEngUserProfile profile) {
		logger.debug("IN");
		List subObjects = new ArrayList();
		try {
			ISubObjectDAO subobjdao = DAOFactory.getSubObjectDAO();
			subObjects = subobjdao
			.getAccessibleSubObjects(obj.getId(), profile);
		} catch (Exception e) {
			logger.error("Error retriving the subObject list", e);
		}
		logger.debug("OUT");
		return subObjects;
	}

	/**
	 * Get the list of BIObject sbapshots
	 * 
	 * @param obj
	 *            BIObject container of the snapshot
	 * @return the List of the BIObject snapshots
	 */
	private List getSnapshotList(BIObject obj) {
		logger.debug("IN");
		List snapshots = new ArrayList();
		try {
			ISnapshotDAO snapdao = DAOFactory.getSnapshotDAO();
			snapshots = snapdao.getSnapshots(obj.getId());
		} catch (Exception e) {
			logger.error("Error retriving the snapshot list", e);
		}
		logger.debug("OUT");
		return snapshots;
	}

	/**
	 * Based on the object type launches the right execution mechanism. For
	 * objects executed by an external engine instantiates the driver for
	 * execution, gets the execution call parameters map, adds in reponse the
	 * map of the parameters. For objects executed by an internal engine,
	 * instantiates the engine class and launches execution method.
	 * 
	 * @param instance
	 *            The execution instance
	 * @param subObj
	 *            The SubObjectDetail subObject to be executed (in case it is
	 *            not null)
	 * @param response
	 *            The response Source Bean
	 */
	private void execute(ExecutionInstance instance, SubObject subObj, String[] vpParameters,
			SourceBean response) {
		logger.debug("IN");

		EMFErrorHandler errorHandler = getErrorHandler();

		BIObject obj = instance.getBIObject();
		// GET ENGINE ASSOCIATED TO THE BIOBJECT
		Engine engine = obj.getEngine();

		// GET THE TYPE OF ENGINE (INTERNAL / EXTERNAL) AND THE SUITABLE
		// BIOBJECT TYPES
		Domain engineType = null;
		Domain compatibleBiobjType = null;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(
					engine.getEngineTypeId());
			compatibleBiobjType = DAOFactory.getDomainDAO().loadDomainById(
					engine.getBiobjTypeId());
		} catch (EMFUserError error) {
			logger.error("Error retrieving document's engine information",
					error);
			errorHandler.addError(error);
			return;
		}
		String compatibleBiobjTypeCd = compatibleBiobjType.getValueCd();
		String biobjTypeCd = obj.getBiObjectTypeCode();

		// CHECK IF THE BIOBJECT IS COMPATIBLE WITH THE TYPES SUITABLE FOR THE
		// ENGINE
		if (!compatibleBiobjTypeCd.equalsIgnoreCase(biobjTypeCd)) {
			// the engine document type and the biobject type are not compatible
			logger.warn("Engine cannot execute input document type: "
					+ "the engine " + engine.getName() + " can execute '"
					+ compatibleBiobjTypeCd + "' type documents "
					+ "while the input document is a '" + biobjTypeCd + "'.");
			Vector params = new Vector();
			params.add(engine.getName());
			params.add(compatibleBiobjTypeCd);
			params.add(biobjTypeCd);
			errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
					2002, params));
			return;
		}

		// GET USER PROFILE
		IEngUserProfile profile = getUserProfile();

		// IF USER CAN'T EXECUTE THE OBJECT RETURN
		if (!canExecute(profile, obj))
			return;

		// GET THE EXECUTION ROLE FROM SESSION
		String executionRole = instance.getExecutionRole();

		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			try {
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageExecution");
				// instance the driver class
				String driverClassName = obj.getEngine().getDriverName();
				IEngineDriver aEngineDriver = (IEngineDriver) Class.forName(
						driverClassName).newInstance();
				// get the map of the parameters
				Map mapPars = null;

				if (subObj != null)
					mapPars = aEngineDriver.getParameterMap(obj, subObj,
							profile, executionRole);
				else
					mapPars = aEngineDriver.getParameterMap(obj, profile,
							executionRole);

				// adding or substituting parameters for viewpoint
				if (vpParameters != null) {
					for (int i = 0; i < vpParameters.length; i++) {
						String param = (String) vpParameters[i];
						String name = param.substring(0, param.indexOf("="));
						String value = param.substring(param.indexOf("=") + 1);
						if (mapPars.get(name) != null) {
							mapPars.remove(name);
							mapPars.put(name, value);
						} else
							mapPars.put(name, value);
					}
				}

				//GET DOC CONFIG FOR DOCUMENT COMPOSITION
				if (contextManager.get("docConfig") != null)
					mapPars.put("docConfig", (DocumentCompositionConfiguration) contextManager.get("docConfig"));

				// set into the reponse the parameters map
				response.setAttribute(ObjectsTreeConstants.REPORT_CALL_URL,
						mapPars);
				if (subObj != null) {
					response.setAttribute(SpagoBIConstants.SUBOBJECT, subObj);
				}

			} catch (Exception e) {
				logger.error("Error During object execution", e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
						100));
			}

			// IF THE ENGINE IS INTERNAL
		} else {

			String className = engine.getClassName();
			logger.debug("Try instantiating class " + className
					+ " for internal engine " + engine.getName() + "...");
			InternalEngineIFace internalEngine = null;
			// tries to instantiate the class for the internal engine
			try {
				if (className == null && className.trim().equals(""))
					throw new ClassNotFoundException();
				internalEngine = (InternalEngineIFace) Class.forName(className)
				.newInstance();
			} catch (ClassNotFoundException cnfe) {
				logger.error("The class ['" + className
						+ "'] for internal engine " + engine.getName()
						+ " was not found.", cnfe);
				Vector params = new Vector();
				params.add(className);
				params.add(engine.getName());
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
						2001, params));
				return;
			} catch (Exception e) {
				logger.error("Error while instantiating class " + className, e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
						100));
				return;
			}

			logger
			.debug("Class "
					+ className
					+ " instantiated successfully. Now engine's execution starts.");

			// starts engine's execution
			try {

				if (subObj != null)
					internalEngine.executeSubObject(this.getRequestContainer(),
							obj, response, subObj);
				else
					internalEngine.execute(this.getRequestContainer(), obj,
							response);
			} catch (EMFUserError e) {
				logger.error("Error while engine execution", e);
				errorHandler.addError(e);
			} catch (Exception e) {
				logger.error("Error while engine execution", e);
				errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
						100));
			}

		}
		logger.debug("OUT");
	}

	private boolean isSubRptStatusAdmissible(String masterRptStatus,
			String subRptStatus) {

		if (masterRptStatus.equalsIgnoreCase("DEV")) {
			if (subRptStatus.equalsIgnoreCase("DEV")
					|| subRptStatus.equalsIgnoreCase("REL"))
				return true;
			else
				return false;
		} else if (masterRptStatus.equalsIgnoreCase("TEST")) {
			if (subRptStatus.equalsIgnoreCase("TEST")
					|| subRptStatus.equalsIgnoreCase("REL"))
				return true;
			else
				return false;
		} else if (masterRptStatus.equalsIgnoreCase("REL")) {
			if (subRptStatus.equalsIgnoreCase("REL"))
				return true;
			else
				return false;
		}
		return false;
	}

	private boolean isSubRptExecutableByUser(IEngUserProfile profile,
			BIObject subrptbiobj) {
		logger.debug("IN");
		String subrptbiobjStatus = subrptbiobj.getStateCode();
		List functionalities = subrptbiobj.getFunctionalities();
		Iterator functionalitiesIt = functionalities.iterator();
		boolean isExecutableByUser = false;
		while (functionalitiesIt.hasNext()) {
			Integer functionalityId = (Integer) functionalitiesIt.next();
			if (ObjectsAccessVerifier.canDev(subrptbiobjStatus,
					functionalityId, profile)) {
				isExecutableByUser = true;
				break;
			}
			if (ObjectsAccessVerifier.canTest(subrptbiobjStatus,
					functionalityId, profile)) {
				isExecutableByUser = true;
				break;
			}
			if (ObjectsAccessVerifier.canExec(subrptbiobjStatus,
					functionalityId, profile)) {
				isExecutableByUser = true;
				break;
			}
		}
		logger.debug("OUT");
		return isExecutableByUser;
	}

	private boolean canExecute(IEngUserProfile profile, BIObject biobj) {
		logger.debug("IN");
		Integer masterReportId = biobj.getId();
		String masterReportStatus = biobj.getStateCode();

		try {
			ISubreportDAO subrptdao = DAOFactory.getSubreportDAO();
			IBIObjectDAO biobjectdao = DAOFactory.getBIObjectDAO();

			List subreportList = subrptdao
			.loadSubreportsByMasterRptId(masterReportId);
			for (int i = 0; i < subreportList.size(); i++) {
				Subreport subreport = (Subreport) subreportList.get(i);
				BIObject subrptbiobj = biobjectdao
				.loadBIObjectForDetail(subreport.getSub_rpt_id());
				if (!isSubRptStatusAdmissible(masterReportStatus, subrptbiobj
						.getStateCode())) {
					errorHandler.addError(new EMFUserError(
							EMFErrorSeverity.ERROR, 1062));
					return false;
				}
				if (!isSubRptExecutableByUser(profile, subrptbiobj)) {
					errorHandler.addError(new EMFUserError(
							EMFErrorSeverity.ERROR, 1063));
					return false;
				}
			}
		} catch (EMFUserError e) {
			logger.error("Error while reading subreports", e);
			return false;
		} finally {
			logger.debug("OUT");
		}

		return true;
	}

	/**
	 * Exec a biobject snapshot.
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void execSnapshotHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		Snapshot snapshot = getRequiredSnapshot(request);
		if (snapshot!= null) {
			executeSnapshot(snapshot, response);
		}
		logger.debug("OUT");
	}

	private void executeSnapshot(Snapshot snapshot, SourceBean response) throws Exception {
		logger.debug("IN");
		response.setAttribute(SpagoBIConstants.SNAPSHOT, snapshot);
		// set information for the publisher
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ViewSnapshotPubJ");
		logger.debug("OUT");
	}

	/**
	 * Based on the object type lauch the right subobject execution mechanism.
	 * For object executed by an external engine instances the driver for
	 * execution, get the execution call parameters map, add in reponse the map
	 * of the parameters.
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void executionSubObjectHandler(SourceBean request,
			SourceBean response) throws Exception {
		logger.debug("IN");
		SubObject subObj = getRequiredSubObject(request);
		// get object from session
		ExecutionInstance instance = getExecutionInstance();
		instance.eraseParametersValues();
		// execution
		execute(instance, subObj, null, response);
		logger.debug("OUT");
	}

	/**
	 * get ExecutionInstance from session
	 * @throws Exception 
	 */
	private ExecutionInstance getExecutionInstance() {
		return contextManager.getExecutionInstance(ExecutionInstance.class.getName());
	}

	/**
	 * get IEngUserProfile from session
	 */
	private IEngUserProfile getUserProfile() {
		return (IEngUserProfile) this.permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	/**
	 * set object in session
	 */
	private void setExecutionInstance(ExecutionInstance instance) {
		contextManager.set(ExecutionInstance.class.getName(), instance);
	}

	private boolean isMultivalueParameter(BIObjectParameter biparam) {
		return (biparam.getParameterValues() != null && biparam
				.getParameterValues().size() > 1);
	}

	private Object getLookedUpObjId(SourceBean request) {
		return (request.getAttribute("LOOKUP_OBJ_PAR_ID"));
	}

	private boolean isLookupCall(SourceBean request) {
		return (getLookedUpObjId(request) != null);
	}

	private Object getRefreshCorrelationObj(SourceBean request) {
		return (request.getAttribute("REFRESH_CORRELATION"));
	}

	private boolean isRefreshCorrelationCall(SourceBean request) {
		return (getRefreshCorrelationObj(request) != null);
	}

	private Integer getLookedUpParameterId(SourceBean request) {
		return (new Integer(findBIObjParId(getLookedUpObjId(request))));
	}

	/**
	 * Gets the looked up parameter.
	 * 
	 * @param request the request
	 * 
	 * @return the looked up parameter
	 * @throws Exception 
	 */
	public BIObjectParameter getLookedUpParameter(SourceBean request) throws Exception {
		logger.debug("IN");
		BIObjectParameter lookedupBIParameter = null;

		Integer objParId = getLookedUpParameterId(request);
		BIObject obj = getExecutionInstance().getBIObject();
		Iterator iterParams = obj.getBiObjectParameters().iterator();
		while (iterParams.hasNext()) {
			BIObjectParameter aBIParameter = (BIObjectParameter) iterParams
			.next();
			if (aBIParameter.getId().equals(objParId)) {
				lookedupBIParameter = aBIParameter;
				break;
			}
		}
		logger.debug("OUT");
		return lookedupBIParameter;
	}

	private void refreshHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		SubObject subobject = getRequiredSubObject(request);
		execute(instance, subobject, null, response);
		logger.debug("OUT");
	}


	/**
	 * Called when user has selected all values for a multi-value-parameter
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	/*
	private void selectAllValueForPar(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		instance.refreshParametersValues(request, false);
		//String roleName=(String)request.getAttribute("roleName");
		String roleName=instance.getExecutionRole();
		List toAddValues=new ArrayList();
		List toAddDescription=new ArrayList();
		IEngUserProfile profile = getUserProfile();

		//String parIdS=(String)request.getAttribute("parameterId");

		//id of BIParameter selected
		String parIdS=(String)request.getAttribute("objParId");
		Integer id=Integer.valueOf(parIdS);

		// List of BiParameters, find the one to change
		List biObjPars=instance.getBIObject().getBiObjectParameters();


		BIObjectParameter currbiObjPar=null;
		// Find the right BIparameter
		boolean found=false;
		for (Iterator iterator = biObjPars.iterator(); iterator.hasNext() && found==false;) {
			BIObjectParameter bipar = (BIObjectParameter) iterator.next();
			if(bipar.getId().equals(id)){
				currbiObjPar=bipar;	
				found=true;
			}
		}
		
		ModalitiesValue modVal = currbiObjPar.getParameter().getModalityValue();
		// get the lov provider
		String looProvider = modVal.getLovProvider();
		// get from the request the type of lov
		ILovDetail lovDetail = LovDetailFactory.getLovFromXML(looProvider);
		//IEngUserProfile profile = GeneralUtilities.createNewUserProfile(userIndentifierToBeUsed);
		String result = lovDetail.getLovResult(profile);
		SourceBean rowsSourceBean = SourceBean.fromXMLString(result);
		// filters for parameters correlation
		rowsSourceBean = filterForParametersCorrelation(rowsSourceBean, currbiObjPar, roleName, request);
		List rows = null;
		if(rowsSourceBean != null) {
			rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if (rows != null && rows.size() != 0) {
				Iterator it = rows.iterator();
				while(it.hasNext()) {
					SourceBean row = (SourceBean) it.next();
					Object value = row.getAttribute(lovDetail.getValueColumnName());
					Object description=row.getAttribute(lovDetail.getDescriptionColumnName());
					if (value != null) {
						toAddValues.add(value.toString());
						if(description!=null) 
							toAddDescription.add(description.toString());
						else
							toAddDescription.add("");
					}
				}
			}
		} 

		// set list of all values
		currbiObjPar.setParameterValues(toAddValues);
		currbiObjPar.setParameterValuesDescription(toAddDescription);

		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
		"ExecuteBIObjectPageParameter");
		logger.debug("OUT");

		/////////////////////////////////////
	}
	*/

	
	private SourceBean filterForParametersCorrelation(SourceBean rowsSourceBean, BIObjectParameter currbiObjPar, String roleName, SourceBean request) throws Exception {
		logger.debug("IN");
		SourceBean toReturn = null;
		if (rowsSourceBean != null) {
			List rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
			if (rows != null && rows.size() != 0) {
				// builds list
				PaginatorIFace paginator = new GenericPaginator();
				ListIFace list = new GenericList();
				list.setPaginator(paginator);
				for (int i = 0; i < rows.size(); i++) {
					paginator.addRow(rows.get(i));
				}
				// filters for correlations
				IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
				ParameterUse paruse = parusedao.loadByParameterIdandRole(currbiObjPar.getParID(), roleName);
				list = SelectParametersLookupModule.filterListForParametersCorrelation(paruse, request, list, new HashMap(), errorHandler);
				// gets SourceBean from filtered list
				toReturn = list.getPaginator().getAll();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}



	/**
	 * Called when user has selected none values for a multi-value-parameter
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void selectNoneValueForPar(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		instance.refreshParametersValues(request, false);

		String objParIdS=(String)request.getAttribute("objParId");
		//String roleName=(String)request.getAttribute("roleName");
		String roleName=instance.getExecutionRole();

		Integer objParId=Integer.valueOf(objParIdS);

		BIObjectParameter currbiObjPar=null;
		List biObjPars=instance.getBIObject().getBiObjectParameters();

		//currbiObjPar.set

		boolean found=false;
		for (Iterator iterator = biObjPars.iterator(); iterator.hasNext() && found==false;) {
			BIObjectParameter bipar = (BIObjectParameter) iterator.next();
			if(bipar.getId().equals(objParId)){
				currbiObjPar=bipar;	
				found=true;
			}
		}

		currbiObjPar.setParameterValues(null);
		currbiObjPar.setParameterValuesDescription(null);

		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
		"ExecuteBIObjectPageParameter");
		logger.debug("OUT");

		/////////////////////////////////////
	}













	/**
	 * Handles the final execution of the object
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void executionHandler(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		instance.refreshParametersValues(request, false);

		String pendingDelete = (String) request.getAttribute("PENDING_DELETE");
		HashMap paramsDescriptionMap = (HashMap) contextManager.get("PARAMS_DESCRIPTION_MAP");
		if (pendingDelete != null && !pendingDelete.trim().equals("")) {
			BIObject object = instance.getBIObject();
			List biparams = object.getBiObjectParameters();
			Iterator iterParams = biparams.iterator();
			while (iterParams.hasNext()) {
				BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
				if (paramsDescriptionMap.get(biparam.getParameterUrlName()) != null)
					paramsDescriptionMap.put(biparam.getParameterUrlName(), "");
			}
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
			return;
		}

		// it is a lookup call
		Object lookupObjParId = request.getAttribute("LOOKUP_OBJ_PAR_ID");
		if (isLookupCall(request)) {

			BIObjectParameter lookupBIParameter = getLookedUpParameter(request);

			if (lookupBIParameter == null) {
				logger.error("The BIParameter with id = "
						+ getLookedUpParameterId(request).toString()
						+ " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1041);
			}
			ModalitiesValue modVal = lookupBIParameter.getParameter()
			.getModalityValue();

			String lookupType = (String) request.getAttribute("LOOKUP_TYPE");
			if (lookupType == null)
				lookupType = "LIST";

			if (lookupType.equalsIgnoreCase("CHECK_LIST")) {
				response.setAttribute("CHECKLIST", "true");
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
				"ChecklistLookupPublisher");
			} else if (lookupType.equalsIgnoreCase("LIST")) {
				response.setAttribute("LIST", "true");
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
				"LookupPublisher");
			} else {
				response.setAttribute("LIST", "true");
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
				"LookupPublisher");
			}

			response.setAttribute("mod_val_id", modVal.getId().toString());
			response.setAttribute("LOOKUP_PARAMETER_NAME", lookupBIParameter
					.getParameterUrlName());
			response.setAttribute("LOOKUP_PARAMETER_ID", lookupBIParameter
					.getId().toString());
			String correlatedParuseId = (String) request
			.getAttribute("correlatedParuseIdForObjParWithId_"
					+ lookupObjParId);
			if (correlatedParuseId != null && !correlatedParuseId.equals(""))
				response.setAttribute("correlated_paruse_id",
						correlatedParuseId);
			return;
		}

		// check parameters values: this operation also load parameter values description into parameters objects
		List errors = instance.getParametersErrors();

		// if this is a correlation refresh call, errors are ignored
		if (isRefreshCorrelationCall(request)) {
			if (errors.size() > 0) {
				// puts into error handler only errors on parameter values (that are instances of EMFUserError), not on 
				// checks (that are instances of EMFValidationError)
				Iterator errorsIt = errors.iterator();
				while (errorsIt.hasNext()) {
					EMFUserError error = (EMFUserError) errorsIt.next();
					if (error instanceof EMFValidationError) continue;
					else errorHandler.addError(error);
				}
			}
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
			"ExecuteBIObjectPageParameter");

			return;
		}

		// add errors into error handler
		Iterator errorsIt = errors.iterator();
		while (errorsIt.hasNext()) {
			errorHandler.addError((EMFUserError) errorsIt.next());
		}

		// if there are some errors into the errorHandler does not execute the
		// BIObject
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
			"ExecuteBIObjectPageParameter");

			return;
		}
		// call the execution method
		execute(instance, null, null, response);
		logger.debug("OUT");
	}

	/**
	 * Get the list of viewpoints
	 * 
	 * @param obj
	 *            BIObject container of the viewpoint
	 * @return the List of the viewpoints
	 */
	private List getViewpointList(BIObject obj) {
		logger.debug("IN");
		List viewpoints = new ArrayList();
		try {
			IViewpointDAO biVPDAO = DAOFactory.getViewpointDAO();
			IEngUserProfile profile = getUserProfile();
			viewpoints = biVPDAO.loadAccessibleViewpointsByObjId(obj.getId(), profile);
		} catch (Exception e) {
			logger.error("Error retriving the viewpoint list", e);
		} finally {
			logger.debug("OUT");
		}
		return viewpoints;
	}

	/**
	 * Save a viewpoint.
	 * 
	 * @param request The request SourceBean
	 * @param response The response SourceBean
	 * 
	 * @throws Exception the exception
	 */
	public void saveViewPoint(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		ExecutionInstance instance = getExecutionInstance();
		// get the current user profile
		IEngUserProfile profile = getUserProfile();
		String nameVP = (String) request.getAttribute("tmp_nameVP");
		String descVP = (String) request.getAttribute("tmp_descVP");
		String scopeVP = (String) request.getAttribute("tmp_scopeVP");
		if (scopeVP != null && scopeVP.equalsIgnoreCase("Public")) {
			scopeVP = "Public";
		} else {
			scopeVP = "Private";
		}
		String ownerVP = (String) ((UserProfile)profile).getUserId();


		instance.refreshParametersValues(request, false);
		// check parameters values 
		List errors = instance.getParametersErrors();
		// add errors into error handler
		Iterator errorsIt = errors.iterator();
		while (errorsIt.hasNext()) {
			errorHandler.addError((EMFUserError) errorsIt.next());
		}
		// if there are some errors into the errorHandler does not save the viewpoint
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
			"ExecuteBIObjectPageParameter");
			return;
		}

		BIObject obj = instance.getBIObject();
		// gets parameter's values and creates a string of values
		List parameters = obj.getBiObjectParameters();
		Iterator iterParams = parameters.iterator();
		String contentVP = "";
		while (iterParams.hasNext()) {
			BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
			String value = biparam.getParameterValuesAsString();
			if (value == null) value = "";
			String labelUrl = biparam.getParameterUrlName();
			// defines the string of parameters to save into db
			contentVP = contentVP + labelUrl + "%3D" + value + "%26";
		}
		if (contentVP != null && contentVP.endsWith("%26")) {
			contentVP = contentVP.substring(0, contentVP.length() - 3);
		}

		IViewpointDAO biViewpointDAO = DAOFactory.getViewpointDAO();
		biViewpointDAO.setUserProfile(profile);
		// check if a viewpoint with the same name yet exists
		Viewpoint tmpVP = biViewpointDAO.loadViewpointByNameAndBIObjectId(nameVP, obj.getId());
		if (tmpVP != null) {
			errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
					6002, null));
			// set into the response the right information for loopback
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
			"ExecuteBIObjectPageParameter");
			logger.debug("OUT");
			return;
		}
		Viewpoint aViewpoint = new Viewpoint();
		aViewpoint.setBiobjId(obj.getId());
		aViewpoint.setVpName(nameVP);
		aViewpoint.setVpOwner(ownerVP);
		aViewpoint.setVpDesc(descVP);
		aViewpoint.setVpScope(scopeVP);
		aViewpoint.setVpValueParams(contentVP);
		aViewpoint.setVpCreationDate(new Timestamp(System.currentTimeMillis()));
		biViewpointDAO.insertViewpoint(aViewpoint);

		// set data in response
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");

		logger.debug("OUT");
		return;
	}

	/**
	 * Delete a viewpoint.
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 * @throws EMFInternalError 
	 */
	private void eraseViewpoint(SourceBean request, SourceBean response)
	throws EMFUserError, SourceBeanException, EMFInternalError {
		logger.debug("IN");
		UserProfile profile = (UserProfile) getUserProfile();
		String userId = profile.getUserId().toString();
		IViewpointDAO vpDAO = DAOFactory.getViewpointDAO();
		List viewpointsIdsList = request.getAttributeAsList("vpId");
		if (viewpointsIdsList != null && !viewpointsIdsList.isEmpty()) {
			Iterator it = viewpointsIdsList.iterator();
			while (it.hasNext()) {
				String vpIdStr = (String) it.next();
				Integer vpId = new Integer(vpIdStr);
				// check if the user is able to erase the viewpoint
				boolean canDelete = false;
				// if user is administrator, he can delete it
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
					canDelete = true;
				} else {
					// if user is not administrator, he can delete it only if he is the owner
					Viewpoint vp = vpDAO.loadViewpointByID(vpId);
					if (vp == null) {
						logger.warn("Viewpoiny with id = " + vpId + " not found!");
						continue;
					}
					if (vp.getVpOwner().equals(userId)) {
						canDelete = true;
					}
				}
				if (canDelete) {
					logger.error("Deleting viewpoint with id = " + vpId + " ...");
					vpDAO.eraseViewpoint(vpId);
				} else {
					logger.error("User cannot delete selected viewpoint!! UserId = [" + userId + "]; viepoint id =[" + vpId + "]");
				}
			}
		}
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
		logger.debug("OUT");
	}

	/**
	 * Exec a viewpoint.
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void execViewpoint(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("IN");
		// get object from session
		ExecutionInstance instance = getExecutionInstance();
		//Integer id =(Integer) request.getAttribute("id");
		
		// built the url for the content recovering
		String content = (request.getAttribute("content") == null) ? ""
				: (String) request.getAttribute("content");
		content = content.replace("%26", "&");
		content = content.replace("%3D", "=");
		// get the current user profile
		instance.applyViewpoint( content, false);
		//instance.setParameterValues(content, false);
		// check parameters values 
		List errors = instance.getParametersErrors();
		// add errors into error handler
		Iterator errorsIt = errors.iterator();
		while (errorsIt.hasNext()) {
			errorHandler.addError((EMFUserError) errorsIt.next());
		}
		// if there are some errors into the errorHandler does not execute the BIObject
		if (!errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME,
			"ExecuteBIObjectPageParameter");
			return;
		}
		execute(instance, null, null, response);
		logger.debug("OUT");
	}

	/**
	 * Gets viewpoint's parameters and view theme.
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param response
	 *            The response SourceBean
	 */
	private void viewViewpoint(SourceBean request, SourceBean response)
	throws Exception {
		logger.debug("OUT");
		String id = (String) request.getAttribute("vpId");

		IViewpointDAO VPDAO = DAOFactory.getViewpointDAO();
		Viewpoint vp = VPDAO.loadViewpointByID(new Integer(id));
		ExecutionInstance instance = getExecutionInstance();
		BIObject obj = instance.getBIObject();
		// gets parameter's values and creates a string of values
		List parameters = obj.getBiObjectParameters();
		Iterator iterParams = parameters.iterator();

		String allParametersValues = vp.getVpValueParams();
		allParametersValues = allParametersValues.replace("%26", "&");
		allParametersValues = allParametersValues.replace("%3D", "=");
		instance.applyViewpoint( allParametersValues, false);
		//instance.setParameterValues(allParametersValues, false);
		// check parameters values 
		List errors = instance.getParametersErrors();
		// add errors into error handler
		Iterator errorsIt = errors.iterator();
		while (errorsIt.hasNext()) {
			errorHandler.addError((EMFUserError) errorsIt.next());
		}

//		HashMap paramsDescriptionMap = (HashMap) contextManager.get("PARAMS_DESCRIPTION_MAP");
//		while (iterParams.hasNext()) {
//		BIObjectParameter biparam = (BIObjectParameter) iterParams.next();
//		String labelUrl = biparam.getParameterUrlName();
//		String descr = biparam.getParameterValuesDescription();
//		paramsDescriptionMap.put(labelUrl, descr);
//		}

		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ExecuteBIObjectPageParameter");
		logger.debug("OUT");
		return;
	}
}
