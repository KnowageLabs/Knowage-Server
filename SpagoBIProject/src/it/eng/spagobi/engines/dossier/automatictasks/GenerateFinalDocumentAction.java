/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.automatictasks;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.dossier.bo.DossierPresentation;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.engines.dossier.dao.IDossierDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO;
import it.eng.spagobi.monitoring.dao.AuditManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.presentation.XPresentationPage;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XCloseable;

public class GenerateFinalDocumentAction implements ActionHandler {

	static private Logger logger = Logger.getLogger(GenerateFinalDocumentAction.class);
	
    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    public void execute(ExecutionContext context) throws Exception {
	XComponent xComponent = null;
	XDesktop xdesktop = null;
	IDossierDAO dossierDAO = null;
	XBridge bridge = null;
	String tempFolder = null;
	ContextInstance contextInstance = null;
	try {
	    logger.debug("Start execution");
	    contextInstance = context.getContextInstance();
	    logger.debug("Context Instance retrived " + contextInstance);
	    ProcessInstance processInstance = context.getProcessInstance();
	    Long workflowProcessId = new Long(processInstance.getId());
	    logger.debug("Workflow process id: " + workflowProcessId);
	    String dossierIdStr = (String) contextInstance.getVariable(DossierConstants.DOSSIER_ID);
	    logger.debug("Dossier id variable retrived " + dossierIdStr);
	    Integer dossierId = new Integer(dossierIdStr);
	    BIObject dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
	    logger.debug("Dossier retrived " + dossier);
	    dossierDAO = DAOFactory.getDossierDAO();
	    tempFolder = dossierDAO.init(dossier);
	    logger.debug("Path tmp folder dossier =" + tempFolder + " created.");

	    // gets the template file data
	    String templateFileName = dossierDAO.getPresentationTemplateFileName(tempFolder);
	    logger.debug("Template file name: " + templateFileName);
	    InputStream contentTempIs = dossierDAO.getPresentationTemplateContent(tempFolder);
	    logger.debug("InputStream opened on dossier template.");
	    byte[] contentTempBytes = GeneralUtilities.getByteArrayFromInputStream(contentTempIs);
	    logger.debug("DossierTemplateContent stored into a byte array.");
	    contentTempIs.close();
	    // write template content into a temp file
	    File templateFile = new File(tempFolder, templateFileName);
	    FileOutputStream fosTemplate = new FileOutputStream(templateFile);
	    fosTemplate.write(contentTempBytes);
	    fosTemplate.flush();
	    fosTemplate.close();
	    logger.debug("Dossier template content written into a temp file.");

	    // initialize openoffice environment
	    ConfigSingleton config = ConfigSingleton.getInstance();
	    SourceBean officeConnectSB = (SourceBean) config.getAttribute("DOSSIER.OFFICECONNECTION");
	    logger.debug("Office connection Sourcebean retrieved: " + officeConnectSB.toXML());
	    String host = (String) officeConnectSB.getAttribute("host");
	    String port = (String) officeConnectSB.getAttribute("port");
	    logger.debug("Office connection host: " + host);
	    logger.debug("Office connection port: " + port);
	    XComponentContext xRemoteContext = Bootstrap.createInitialComponentContext(null);
	    logger.debug("InitialComponentContext xRemoteContext created: " + xRemoteContext);

	    Object x = xRemoteContext.getServiceManager().createInstanceWithContext(
		    "com.sun.star.connection.Connector", xRemoteContext);
	    XConnector xConnector = (XConnector) UnoRuntime.queryInterface(XConnector.class, x);
	    logger.debug("XConnector retrieved: " + xConnector);
	    XConnection connection = xConnector.connect("socket,host=" + host + ",port=" + port);
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
	    logger.debug("xRemoteServiceManager: " + xRemoteServiceManager);
	    XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
		    xRemoteServiceManager);
	    logger.debug("XPropertySet: " + xPropertySet);
	    Object oDefaultContext = xPropertySet.getPropertyValue("DefaultContext");
	    logger.debug("DefaultContext: " + oDefaultContext);
	    xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, oDefaultContext);
	    logger.debug("xRemoteContext: " + xRemoteContext);
	    Object desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop",
		    xRemoteContext);
	    logger.debug("Desktop object instance created: " + desktop);
	    xdesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
	    logger.debug("XDesktop object: " + xdesktop);

	    XComponentLoader xComponentLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class,
		    xdesktop);
	    // load the template into openoffice
	    logger.debug("Path template = " + templateFile.getAbsolutePath());
	    xComponent = openTemplate(xComponentLoader, "file:///" + templateFile.getAbsolutePath());
	    logger.debug("Template opened: " + xComponent);
	    XMultiServiceFactory xServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(
		    XMultiServiceFactory.class, xComponent);
	    logger.debug("xServiceFactory: " + xServiceFactory);
	    // get draw pages
	    XDrawPagesSupplier xDrawPageSup = (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class,
		    xComponent);
	    XDrawPages drawPages = xDrawPageSup.getDrawPages();
	    logger.debug("Draw pages found: " + drawPages);
	    int numPages = drawPages.getCount();
	    logger.debug("Draw pages number: " + numPages);
	    IDossierPartsTempDAO dptDAO = DAOFactory.getDossierPartsTempDAO();
	    for (int i = 0; i < numPages; i++) {
		logger.debug("Start examining page " + i);
		// get images corresponding to that part of the template
		int pageNum = i + 1;
		Map images = dptDAO.getImagesOfDossierPart(dossierId, pageNum, workflowProcessId);
		logger.debug("Images map retrieved: " + images);
		// get draw page
		Object pageObj = drawPages.getByIndex(i);
		XDrawPage xDrawPage = (XDrawPage) UnoRuntime.queryInterface(XDrawPage.class, pageObj);
		logger.debug("Draw page: " + xDrawPage);
		// get shapes of the page
		XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDrawPage);
		logger.debug("Shapes found: " + xShapes);
		int numShapes = xShapes.getCount();
		logger.debug("Shapes number: " + numShapes);
		// prepare list for shapes to remove and to add
		List shapetoremove = new ArrayList();
		List shapetoadd = new ArrayList();
		Object oBitmapsObj = xServiceFactory.createInstance("com.sun.star.drawing.BitmapTable");
		XNameContainer oBitmaps = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, oBitmapsObj);
		// check each shape
		for (int j = 0; j < numShapes; j++) {
		    logger.debug("Start examining shape " + j + " of page " + i);
		    Object shapeObj = xShapes.getByIndex(j);
		    XShape xshape = (XShape) UnoRuntime.queryInterface(XShape.class, shapeObj);
		    logger.debug("xshape: " + xshape);
		    XText xShapeText = (XText) UnoRuntime.queryInterface(XText.class, shapeObj);
		    logger.debug("XShapeText retrived " + xShapeText);
		    if (xShapeText == null) {
			continue;
		    }
		    String shapeText = xShapeText.getString();
		    logger.debug("shape text retrived " + shapeText);
		    shapeText = shapeText.trim();
		    // sobstitute the placeholder with the correspondent image
		    if (shapeText.startsWith("spagobi_placeholder_")) {
			String nameImg = shapeText.substring(20);
			logger.debug("Name of the image corresponding to the placeholder: " + nameImg);
			Size size = xshape.getSize();
			Point position = xshape.getPosition();
			logger.debug("Stored shape size and position on local variables");
			shapetoremove.add(xshape);
			logger.debug("Shape loaded on shapes to be removed");
			Object newShapeObj = xServiceFactory.createInstance("com.sun.star.drawing.GraphicObjectShape");
			logger.debug("New shape object instantiated: " + newShapeObj);
			XShape xShapeNew = (XShape) UnoRuntime.queryInterface(XShape.class, newShapeObj);
			logger.debug("New XShape instantiated from the new shape object: " + xShapeNew);
			xShapeNew.setPosition(position);
			xShapeNew.setSize(size);
			logger.debug("Stored size and position set for the new XShape");
			// write the corresponding image into file system
			String pathTmpImgFolder = tempFolder + "/tmpImgs/";
			logger.debug("Path tmp images: " + pathTmpImgFolder);
			File fileTmpImgFolder = new File(pathTmpImgFolder);
			fileTmpImgFolder.mkdirs();
			logger.debug("Folder tmp images: " + pathTmpImgFolder + " created.");
			String pathTmpImg = pathTmpImgFolder + nameImg + ".jpg";
			logger.debug("Path tmp image file: " + pathTmpImg);
			File fileTmpImg = new File(pathTmpImg);
			FileOutputStream fos = new FileOutputStream(fileTmpImg);
			byte[] content = (byte[]) images.get(nameImg);
			if (content == null)
			    logger.debug("Image with name \"" + nameImg + "\" was NOT found!!!");
			else
			    logger.debug("Image with name \"" + nameImg + "\" was found");
			fos.write(content);
			fos.flush();
			fos.close();
			logger.debug("Tmp image file written");
			// load the image into document
			XPropertySet xSPS = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShapeNew);
			try {
			    String fileoopath = transformPathForOpenOffice(fileTmpImg);
			    logger.debug("Path image loaded into openoffice = " + fileoopath);
			    String externalGraphicUrl = "file:///" + fileoopath;
			    if (!oBitmaps.hasByName(nameImg)) {
				logger.debug("Bitmap table does not contain an element with name '" + nameImg
					+ "'.");
				oBitmaps.insertByName(nameImg, externalGraphicUrl);
			    } else {
				logger.debug("Bitmap table already contains an element with name '" + nameImg
					+ "'.");
			    }
			    Object internalGraphicUrl = oBitmaps.getByName(nameImg);
			    logger.debug("Retrieved internal url for image '" + nameImg + "': "
				    + internalGraphicUrl);
			    xSPS.setPropertyValue("GraphicURL", internalGraphicUrl);
			} catch (Exception e) {
				logger.error("error while adding graphic shape", e);
			}
			shapetoadd.add(xShapeNew);
			logger.debug("New shape loaded on shapes to be added");
		    }
		}
		// add and remove shape
		Iterator iter = shapetoremove.iterator();
		while (iter.hasNext()) {
		    XShape shape = (XShape) iter.next();
		    xShapes.remove(shape);
		}
		logger.debug("Removed shapes to be removed from document");
		iter = shapetoadd.iterator();
		while (iter.hasNext()) {
		    XShape shape = (XShape) iter.next();
		    xShapes.add(shape);
		}
		logger.debug("Added shapes to be added to the document");
		// add notes
		XPresentationPage xPresPage = (XPresentationPage) UnoRuntime.queryInterface(XPresentationPage.class,
			xDrawPage);
		XDrawPage notesPage = xPresPage.getNotesPage();
		logger.debug("Notes page retrieved: " + notesPage);
		XShapes xShapesNotes = (XShapes) UnoRuntime.queryInterface(XShapes.class, notesPage);
		logger.debug("Shape notes retrieved: " + xShapesNotes);
		int numNoteShapes = xShapesNotes.getCount();
		logger.debug("Number of shape notes: " + numNoteShapes);
		for (int indShap = 0; indShap < numNoteShapes; indShap++) {
		    logger.debug("Start examining shape note number " + indShap + " of page " + i);
		    Object shapeNoteObj = xShapesNotes.getByIndex(indShap);
		    XShape xshapeNote = (XShape) UnoRuntime.queryInterface(XShape.class, shapeNoteObj);
		    logger.debug("xshapeNote: " + xshapeNote);
		    String type = xshapeNote.getShapeType();
		    logger.debug("Shape type: " + type);
		    if (type.endsWith("NotesShape")) {
			XText textNote = (XText) UnoRuntime.queryInterface(XText.class, shapeNoteObj);
			logger.debug("XText: " + textNote);
			byte[] notesByte = dptDAO.getNotesOfDossierPart(dossierId, pageNum, workflowProcessId);
			String notes = null;
			if (notesByte == null) {
			    logger.debug("Notes bytes array is null!!!!");
			    notes = "";
			} else {
			    logger.debug("Notes bytes array retrieved");
			    notes = new String(notesByte);
			}
			textNote.setString(notes);
			logger.debug("Notes applied to the XText");
		    }
		}
	    }

	    // save final document
	    String pathFinalDoc = tempFolder + "/" + dossier.getName() + ".ppt";
	    logger.debug("Path final document = " + pathFinalDoc);
	    File fileFinalDoc = new File(pathFinalDoc);
	    String fileoopath = transformPathForOpenOffice(fileFinalDoc);
	    logger.debug("Open Office path: " + fileoopath);
	    if (fileoopath.equals(""))
		return;
	    XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, xComponent);
	    logger.debug("XStorable: " + xStorable);
	    PropertyValue[] documentProperties = new PropertyValue[2];
	    documentProperties[0] = new PropertyValue();
	    documentProperties[0].Name = "Overwrite";
	    documentProperties[0].Value = new Boolean(true);
	    documentProperties[1] = new PropertyValue();
	    documentProperties[1].Name = "FilterName";
	    documentProperties[1].Value = "MS PowerPoint 97";
	    try {
	    	logger.debug("Try to store document with path = " + "file:///" + fileoopath);
	    	xStorable.storeAsURL("file:///" + fileoopath, documentProperties);
	    	logger.debug("Document stored with path = " + "file:///" + fileoopath);
	    } catch (IOException e) {
	    	logger.error("Error while storing the final document", e);
	    }
	    FileInputStream fis = new FileInputStream(pathFinalDoc);
	    byte[] docCont = GeneralUtilities.getByteArrayFromInputStream(fis);
	    IDossierPresentationsDAO dpDAO = DAOFactory.getDossierPresentationDAO();
	    
	    DossierPresentation dossierPresentation = new DossierPresentation();
	    dossierPresentation.setApproved(null);
	    dossierPresentation.setProg(null);
	    dossierPresentation.setBiobjectId(dossier.getId());
	    dossierPresentation.setContent(docCont);
	    dossierPresentation.setName(dossier.getName());
	    dossierPresentation.setWorkflowProcessId(workflowProcessId);
	    dpDAO.insertPresentation(dossierPresentation);
	    logger.debug("Document stored.");
	    fis.close();
	    dptDAO.cleanDossierParts(dossierId, workflowProcessId);
	    logger.debug("Dossier temporary parts relevant to document id = [" + dossierId + "] " +
	    		"and workflow process id = [" + workflowProcessId + "] deleted.");
	} catch (Exception e) {
		logger.error("Error during the generation of the final document", e);
	    // AUDIT UPDATE
	    if (contextInstance != null) {
		Integer auditId = (Integer) contextInstance.getVariable(AuditManager.AUDIT_ID);
		AuditManager auditManager = AuditManager.getInstance();
		auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
			.getMessage(), null);
	    }
	    // store as final document the template

	} finally {
	    // close open document and environment
	    if (xComponent != null) {
			XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComponent);
			XCloseable xCloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xModel);
			try {
			    xCloseable.close(true);
			} catch (Exception e) {
				logger.error("Cannot close openoffice template document", e);
			}
	    }
	    // cleans dossier temp folder
	    if (dossierDAO != null && tempFolder != null) {
	    	dossierDAO.clean(tempFolder);
		    logger.debug("Deleted folder " + tempFolder);
	    }
	    logger.debug("OUT");
	}

    }

    private static XComponent openTemplate(XComponentLoader xComponentLoader, String pathTempFile) {
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
	} catch (Exception e) {
		logger.error("Cannot open template document", e);
	}
	return xComponent;
    }

    private static String transformPathForOpenOffice(File file) {
	String path = "";
	try {
	    path = file.getCanonicalPath();
	    path = path.replace('\\', '/');
	    String prefix = path.substring(0, 2);
	    String afterPrefix = path.substring(2);
	    String secondChar = path.substring(1, 2);
	    if (secondChar.equals(":")) {
		path = prefix.toLowerCase() + afterPrefix;
	    }
	} catch (Exception e) {
		logger.error("Error while transforming file path", e);
	}
	return path;
    }

}
