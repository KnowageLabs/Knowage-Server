/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.automatictasks;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionController;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.dossier.bo.ConfiguredBIDocument;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.engines.dossier.dao.IDossierDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO;
import it.eng.spagobi.engines.dossier.exceptions.OpenOfficeConnectionException;
import it.eng.spagobi.engines.dossier.utils.DossierAnalyticalDriversManager;
import it.eng.spagobi.monitoring.dao.AuditManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

import sun.misc.BASE64Decoder;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XCloseable;

public class ProcessOOTemplateAction implements ActionHandler {

	static private Logger logger = Logger.getLogger(ProcessOOTemplateAction.class);
	
    XDesktop xdesktop = null;
    XComponent xComponent = null;
    XBridge bridge = null;

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    public void execute(ExecutionContext context) throws Exception {
    	
    	String pathTmpFold = null;
		ContextInstance contextInstance = null;
		File tempDir = null;
		IDossierDAO dossierDAO = null;
		logger.debug("IN");
		try {
		    // RECOVER CONFIGURATION PARAMETER
		    contextInstance = context.getContextInstance();
		    logger.debug("Context Instance retrived " + contextInstance);
		    ProcessInstance processInstance = context.getProcessInstance();
		    Long workflowProcessId = new Long(processInstance.getId());
		    String dossierIdStr = (String) contextInstance.getVariable(DossierConstants.DOSSIER_ID);
		    logger.debug("Dossier id variable retrived " + dossierIdStr);
		    Integer dossierId = new Integer(dossierIdStr);
		    BIObject dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
		    dossier.setBiObjectParameters(DAOFactory.getBIObjectDAO().getBIObjectParameters(dossier));
		    setAnalyticalDriversValues(dossier, (Map) contextInstance.getVariable(DossierConstants.DOSSIER_PARAMETERS));
		    logger.debug("Dossier variable retrived " + dossier);
		    dossierDAO = DAOFactory.getDossierDAO();
		    pathTmpFold = dossierDAO.init(dossier);
		    logger.debug("Using tmp folder path " + pathTmpFold);
		    ConfigSingleton config = ConfigSingleton.getInstance();
		    tempDir = new File(pathTmpFold);
		    logger.debug("Create tmp folders " + tempDir);
	
		    // GETS OO TEMPLATE AND WRITE IT INTO THE TMP DIRECTORY
		    String templateFileName = dossierDAO.getPresentationTemplateFileName(pathTmpFold);
		    logger.debug("dossier oo template name retrived " + templateFileName);
		    InputStream contentTempIs = null;
		    byte[] contentTempBytes = null;
		    try {
			    contentTempIs = dossierDAO.getPresentationTemplateContent(pathTmpFold);
			    logger.debug("dossier oo template input stream retrived " + contentTempIs);
			    contentTempBytes = GeneralUtilities.getByteArrayFromInputStream(contentTempIs);
			    logger.debug("dossier oo template bytes retrived ");
		    } finally {
		    	if (contentTempIs != null) contentTempIs.close();
		    }
		    // write template content into a temp file
		    File templateOOFile = new File(tempDir, templateFileName);
		    FileOutputStream fosTemplate = new FileOutputStream(templateOOFile);
		    fosTemplate.write(contentTempBytes);
		    logger.debug("oo template bytes written into a tmp file ");
		    fosTemplate.flush();
		    fosTemplate.close();
	
		    // INITIALIZE OFFICE ENVIRONMENT
		    SourceBean officeConnectSB = (SourceBean) config.getAttribute("DOSSIER.OFFICECONNECTION");
		    if (officeConnectSB == null) {
		    	logger.error("Cannot found sourcebean DOSSIER.OFFICECONNECTION into configuration");
		    	throw new Exception("Cannot found sourcebean DOSSIER.OFFICECONNECTION into configuration");
		    } else {
		    	logger.debug("Dossier office connection sourcebean retrived " + officeConnectSB);
		    }
		    String host = (String) officeConnectSB.getAttribute("host");
		    String port = (String) officeConnectSB.getAttribute("port");
		    logger.debug("office connection, using host " + host + " and port " + port);
		    XComponentContext xRemoteContext = Bootstrap.createInitialComponentContext(null);
		    logger.debug("initial XComponentContext created " + xRemoteContext);
	
		    Object x = xRemoteContext.getServiceManager().createInstanceWithContext(
			    "com.sun.star.connection.Connector", xRemoteContext);
		    XConnector xConnector = (XConnector) UnoRuntime.queryInterface(XConnector.class, x);
		    logger.debug("XConnector retrieved: " + xConnector);
            XConnection connection = null;
            try {
			    connection = xConnector.connect("socket,host=" + host + ",port=" + port);
			    if (connection == null) {
			    	logger.error("Cannot connect to open office using host " + host + " and port " + port);
			    	throw new OpenOfficeConnectionException("Cannot connect to open office using host " + host
			    			+ " and port " + port);
			    }
            } catch (Exception e) {
            	logger.error("Cannot connect to open office using host " + host + " and port " + port, e);
            	throw new OpenOfficeConnectionException("Cannot connect to open office using host " + host + " and port " + port);
            }
		    logger.debug("XConnection retrieved: " + connection);
		    x = xRemoteContext.getServiceManager().createInstanceWithContext("com.sun.star.bridge.BridgeFactory",
			    xRemoteContext);
		    XBridgeFactory xBridgeFactory = (XBridgeFactory) UnoRuntime.queryInterface(XBridgeFactory.class, x);
		    logger.debug("XBridgeFactory retrieved: " + xBridgeFactory);
		    // this is the bridge that you will dispose
		    bridge = xBridgeFactory.createBridge("", "urp", connection, null);
		    logger.debug("XBridge retrieved: " + bridge);
		    XComponent xComp = (XComponent) UnoRuntime.queryInterface(XComponent.class, bridge);
		    // get the remote instance
		    x = bridge.getInstance("StarOffice.ServiceManager");
		    logger.debug("StarOffice.ServiceManager instance retrieved: " + x);
		    // Query the initial object for its main factory interface
		    XMultiComponentFactory xRemoteServiceManager = (XMultiComponentFactory) UnoRuntime.queryInterface(
			    XMultiComponentFactory.class, x);
	
		    // XMultiComponentFactory xRemoteServiceManager =
		    // (XMultiComponentFactory)UnoRuntime.queryInterface(XMultiComponentFactory.class,
		    // initialObject);
		    logger.debug("XMultiComponentFactory retrived " + xRemoteServiceManager);
		    XPropertySet xProperySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
			    xRemoteServiceManager);
		    logger.debug("xProperySet retrived " + xProperySet);
		    Object oDefaultContext = xProperySet.getPropertyValue("DefaultContext");
		    logger.debug("DefaultContext Propery  value retrived " + oDefaultContext);
		    xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, oDefaultContext);
		    logger.debug("remote XComponentContext retrived " + xRemoteContext);
		    Object desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop",
			    xRemoteContext);
		    logger.debug("Desktop object retrived " + desktop);
		    xdesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
		    logger.debug("XDesktop object retrived " + desktop);
	
		    // LOAD OO TEMPLATE INTO OPEN OFFICE
		    XComponentLoader xComponentLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class,
			    xdesktop);
		    logger.debug("XComponentLoader object retrived " + xComponentLoader);
		    xComponent = openTemplate(xComponentLoader, "file:///" + templateOOFile.getAbsolutePath());
		    logger.debug("Template loaded into openffice ");
	
		    // ANALYZE OO TEMPLATE, EXTRACT NAME OF PLACEHOLDERS, CALL ENGINE
		    // AND STORE RESULT IMAGES
		    // gets the number of the parts of the documents
		    XMultiServiceFactory xServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(
			    XMultiServiceFactory.class, xComponent);
		    logger.debug("XMultiServiceFactory of the Template retrived " + xServiceFactory);
		    XDrawPagesSupplier xDrawPageSup = (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class,
			    xComponent);
		    logger.debug("XDrawPagesSupplier retrived " + xDrawPageSup);
		    XDrawPages drawPages = xDrawPageSup.getDrawPages();
		    logger.debug("XDrawPages retrived " + drawPages);
		    int numPages = drawPages.getCount();
		    logger.debug("Template has " + numPages + " pages");
		    
		    // for each part of the document gets the image images and stores
		    // them into cms
		    for (int i = 0; i < numPages; i++) {
				int numPage = i + 1;
				logger.debug("processing page with index " + i);
				Object pageObj = drawPages.getByIndex(i);
				logger.debug("page object retrived " + pageObj);
				XDrawPage xDrawPage = (XDrawPage) UnoRuntime.queryInterface(XDrawPage.class, pageObj);
				logger.debug("XDrawPage retrived " + xDrawPage);
				XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);
				logger.debug("xShapes of pages retrived " + xShapes);
				int numShapes = xShapes.getCount();
				logger.debug("Page has " + numShapes + " shapes");
				for (int j = 0; j < numShapes; j++) {
				    logger.debug("processing shape with index " + j);
				    Object shapeObj = xShapes.getByIndex(j);
				    logger.debug("shape object retrived " + shapeObj);
				    XText xShapeText = (XText) UnoRuntime.queryInterface(XText.class, shapeObj);
				    logger.debug("XShapeText retrived " + xShapeText);
				    if (xShapeText != null) {
						String shapeText = xShapeText.getString();
						logger.debug("shape text retrived " + shapeText);
						shapeText = shapeText.trim();
						if (shapeText.startsWith("spagobi_placeholder_")) {
						    String logicalObjectName = shapeText.substring(20);
						    logger.debug("Logical Name of the shape " + logicalObjectName);
						    ConfiguredBIDocument confDoc = dossierDAO.getConfiguredDocument(logicalObjectName, pathTmpFold);
						    logger.debug("Configured document with Logical Name " + logicalObjectName
							    + " retrived " + confDoc);
						    storeDocImages(confDoc, numPage, dossier, workflowProcessId);
						}
				    }
				}
		    }
	
		} catch (Exception e) {
		    logger.error("Exception during execution : \n" + e);
		    // AUDIT UPDATE
		    if (contextInstance != null) {
				Integer auditId = (Integer) contextInstance.getVariable(AuditManager.AUDIT_ID);
				AuditManager auditManager = AuditManager.getInstance();
				auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
					.getMessage(), null);
		    }
		    throw e;
		} finally {
		    if (xComponent != null) {
				logger.debug("Start close xComponent (template document)");
				XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
				logger.debug("XModel interface retrived " + xModel);
				XCloseable xCloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xModel);
				logger.debug("XCloseable interface retrived " + xCloseable);
				try {
				    xCloseable.close(true);
				    logger.debug("xComponent (template document) closed");
				} catch (Exception e) {
				    logger.error("Cannot close openoffice template document \n " + e);
				}
		    }
	
		    // close the bridge
		    if (bridge != null) {
				XComponent xcomponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, bridge);
				xcomponent.dispose();
				logger.debug("Bridge disposed");
		    }
	
		    // deletes the dossier temporary folder
		    if (dossierDAO != null && pathTmpFold != null) {
		    	dossierDAO.clean(pathTmpFold);
		    	logger.debug("Dossier temp directory " + pathTmpFold + " deleted");
		    }
		    logger.debug("OUT");
		}

    }

    private void setAnalyticalDriversValues(BIObject dossier, Map variable) {
    	logger.debug("IN");
		List parameters = dossier.getBiObjectParameters();
		Iterator it = parameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter aParameter = (BIObjectParameter) it.next();
			List values = (List) variable.get(aParameter.getParameterUrlName());
			aParameter.setParameterValues(values);
		}
    	logger.debug("OUT");
	}

	private static void storeDocImages(ConfiguredBIDocument confDoc, int numPage, BIObject dossier, Long workflowProcessId) throws Exception {
    logger.debug("IN");
	try {
	    // get label of the biobject
	    String label = confDoc.getLabel();
	    logger.debug("using configured document / biobject label " + label);
	    // get the map of configured parameter
	    Map confPars = confDoc.getParameters();
	    logger.debug("Configured static parameters: " + confPars);
	    
	    // load the biobject
	    IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
	    BIObject biobj = biobjdao.loadBIObjectByLabel(label);
	    biobj.setBiObjectParameters(biobjdao.getBIObjectParameters(biobj));
	    logger.debug("biobject loaded: " + biobj);
	    
	    DossierAnalyticalDriversManager adManager = new DossierAnalyticalDriversManager();
	    adManager.fillEmptyAnalyticalDrivers(confPars, dossier, biobj);
	    logger.debug("Parameters updated using dossier parameters: " + confPars);
	    
		// create the execution controller 
		ExecutionController execCtrl = new ExecutionController();
		execCtrl.setBiObject(biobj);
		// fill parameters 
		execCtrl.refreshParameters(biobj, confPars);

	    IEngUserProfile profile = UserProfile.createWorkFlowUserProfile();
	    
		ExecutionProxy proxy = new ExecutionProxy();
		proxy.setBiObject(biobj);
		
		byte[] response = proxy.exec(profile, "WORKFLOW", "JPGBASE64");

	    // extract image from the response
	    String xmlRespStr = new String(response);
	    SourceBean xmlRespSB = SourceBean.fromXMLString(xmlRespStr);
	    logger.debug("response parsed into a sourcebean");
	    List images = xmlRespSB.getAttributeAsList("IMAGE");
	    logger.debug("list of images sourcebean extracted from response" + images);
	    SourceBean firstImageSB = (SourceBean) images.get(0);
	    logger.debug("first images sourcebean recovered");
	    String firstImgBase64 = firstImageSB.getCharacters();
	    logger.debug("base 64 encoded image bytes retrived " + firstImgBase64);
	    BASE64Decoder decoder = new BASE64Decoder();
	    byte[] firstImg = decoder.decodeBuffer(firstImgBase64);
	    logger.debug("image bytes decoded " + firstImg);
	    // store image into cms
	    IDossierPartsTempDAO dptDAO = DAOFactory.getDossierPartsTempDAO();
	    dptDAO.setUserProfile(profile);
	    dptDAO.storeImage(dossier.getId(), firstImg, confDoc.getLogicalName(), numPage, workflowProcessId);
	    logger.debug("image stored into cms");
	} catch (Exception e) {
	    logger.error("Error while generating and storing " + "images of the document"
		    + confDoc.getLogicalName() + " \n " + e);
	    throw e;
	}
    }

    private static XComponent openTemplate(XComponentLoader xComponentLoader, String pathTempFile) {
	logger.debug("IN");
	XComponent xComponent = null;
	try {
	    PropertyValue[] pPropValues = new PropertyValue[2];
	    pPropValues[0] = new PropertyValue();
	    pPropValues[0].Name = "Hidden";
	    pPropValues[0].Value = new Boolean(true);
	    pPropValues[1] = new PropertyValue();
	    pPropValues[1].Name = "OpenNewView";
	    pPropValues[1].Value = new Boolean(true);
	    String loadUrl = "private:factory/simpress";
	    xComponent = xComponentLoader.loadComponentFromURL(pathTempFile, "_blank", 0, pPropValues);
	    logger.debug("template document loaded " + xComponent);
	} catch (Exception e) {
	   logger.error("Cannot open oo template document", e);
	}
	logger.debug("OUT");
	return xComponent;
    }

}
