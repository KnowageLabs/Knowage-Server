/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.container.SpagoBIHttpSessionContainer;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.content.stub.ContentServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * 
 * Proxy of Content Service
 *
 */
public final class ContentServiceProxy extends AbstractServiceProxy{

	static private final String SERVICE_NAME = "Content Service";
	
    static private Logger logger = Logger.getLogger(ContentServiceProxy.class);

    /**
     * use this i engine context only.
     * 
     * @param user user ID
     * @param session http session
     */
    public ContentServiceProxy(String user, HttpSession session) {
    	super( user,session);
    	if (user==null) logger.error("User ID IS NULL....");
    	if (session==null) logger.error("HttpSession IS NULL....");
    }
    
    private ContentServiceProxy() {
	super ();
    }    

    private it.eng.spagobi.services.content.stub.ContentService lookUp() throws SecurityException {
	try {
	    ContentServiceServiceLocator locator = new ContentServiceServiceLocator();   
	    it.eng.spagobi.services.content.stub.ContentService service=null;
	    if (serviceUrl!=null ){
		    service = locator.getContentService(serviceUrl);		
	    }else {
		    service = locator.getContentService();		
	    }
	    return service;
	} catch (ServiceException e) {
	    logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
	    throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
	}
    }


    /**
     * Read template.
     * 
     * @param document String
     * 
     * @return Content
     */
    public Content readMap(String mapName) {
	logger.debug("IN.mapName="+mapName);
	if (mapName==null || mapName.length()==0){
	    logger.error("mapName is NULL");
	    return null;
	}
	try {
	    return lookUp().readMap(readTicket(), userId, mapName);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
    
    /**
     * Read template.
     * 
     * @param document String
     * 
     * @return Content
     */
    public Content readTemplate(String document,HashMap attributes) {
	logger.debug("IN.document="+document);
	if (document==null || document.length()==0){
	    logger.error("Documenti ID is NULL");
	    return null;
	}
	try {
	    return lookUp().readTemplate(readTicket(), userId, document,attributes);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
    
    /**
     * Read template by label.
     * 
     * @param document String
     * 
     * @return Content
     */
    public Content readTemplateByLabel(String label,HashMap attributes) {
	logger.debug("IN.document="+label);
	if (label==null || label.length()==0){
	    logger.error("Documenti Label is NULL");
	    return null;
	}
	try {
	    return lookUp().readTemplateByLabel(readTicket(), userId, label,attributes);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
    
    /**
     * Publish template.
     * 
     * @param attributes HashMap
     * 
     * @return String
     */
    public String publishTemplate( HashMap attributes) {
	logger.debug("IN");
	if (attributes==null ){
	    logger.error("attributes is NULL");
	    return null;
	}	
	try {
	    return lookUp().publishTemplate(readTicket(), userId, attributes);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
    /**
     * Map catalogue.
     * 
     * @param operation String
     * @param path String
     * @param featureName String
     * @param mapName String
     * 
     * @return String
     */
    public String mapCatalogue( String operation,String path,String featureName,String mapName){
	logger.debug("IN");
	if (operation==null || operation.length()==0){
	    logger.error("operation is NULL");
	    return null;
	}	
	try {
	    return lookUp().mapCatalogue(readTicket(), userId, operation,path,featureName,mapName);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;	
    }
    
    /**
     * Read sub object content.
     * 
     * @param nameSubObject String
     * 
     * @return Content
     */
    public Content readSubObjectContent(String nameSubObject){
	logger.debug("IN.nameSubObject="+nameSubObject);
	if (nameSubObject==null || nameSubObject.length()==0){
	    logger.error("SubObject is NULL");
	    return null;
	}	
	try {
	    return lookUp().readSubObjectContent(readTicket(), userId, nameSubObject);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }

    /**Read sub object content.
     * @param nameSubObject
     * @param objId
     * @return
     * @throws java.rmi.RemoteException
     */
    public Content readSubObjectContent(String nameSubObject, Integer objId) throws java.rmi.RemoteException{
    	logger.debug("IN.nameSubObject="+nameSubObject);
    	if (nameSubObject==null || nameSubObject.length()==0){
    	    logger.error("SubObject is NULL");
    	    return null;
    	}	
    	try {
    	    return lookUp().readSubObjectContent(readTicket(), userId, nameSubObject, objId);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);

    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;
      }
    /**
     * Save sub object.
     * 
     * @param documentiId String
     * @param analysisName String
     * @param analysisDescription String
     * @param visibilityBoolean String
     * @param content  String
     * 
     * @return  String
     */
    public String saveSubObject(String documentiId,String analysisName,String analysisDescription,String visibilityBoolean,String content){
	logger.debug("IN.documentiId="+documentiId);
	if (documentiId==null || documentiId.length()==0){
	    logger.error("documentiId is NULL");
	    return null;
	}	
	try {
	    return lookUp().saveSubObject(readTicket(), userId, documentiId,analysisName, analysisDescription, visibilityBoolean, content);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
    /**
     * Save object template.
     * 
     * @param documentiId  String
     * @param templateName String
     * @param content String
     * 
     * @return String
     */
    public String saveObjectTemplate(String documentiId,String templateName,String content){
	logger.debug("IN.documentiId="+documentiId);
	if (documentiId==null || documentiId.length()==0){
	    logger.error("documentiId is NULL");
	    return null;
	}
	if (templateName==null || templateName.length()==0){
	    logger.error("templateName is NULL");
	    return null;
	}
	if (content==null || content.length()==0){
	    logger.warn("templateName is NULL");
	}	
	try {
	    return lookUp().saveObjectTemplate(readTicket(), userId, documentiId, templateName, content);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }

    /**
     * Download all.
     * 
     * @param biobjectId String
     * @param fileName String
     * 
     * @return  String
     */
    public Content downloadAll(String biobjectId,String fileName){
	logger.debug("IN");
	if (biobjectId==null || biobjectId.length()==0){
	    logger.error("biobjectId is NULL");
	    return null;
	}
	if (fileName==null || fileName.length()==0){
	    logger.error("fileName is NULL");
	    return null;
	}	
	try {
	    return lookUp().downloadAll(readTicket(), userId, biobjectId, fileName);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;
    }
    
}
