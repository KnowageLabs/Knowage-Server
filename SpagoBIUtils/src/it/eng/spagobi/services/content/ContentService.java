/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.content;

import it.eng.spagobi.services.content.bo.Content;

import java.util.HashMap;



/**
 * This is the ContentService interfaces
 * @author Bernabei Angelo
 *
 */
public interface ContentService {

	/**
	 * return the user profile informations
	 * @param token
	 * @return
	 */
    Content readTemplate(String token,String user,String document,HashMap attributes);  

    
	/**
	 * get template by doc label
	 * @param token
	 * @return
	 */
    Content readTemplateByLabel(String token,String user,String document,HashMap attributes);  

    /**
     * 
     * @param token String
     * @param user String
     * @param nameSubObject String
     * @return  Content
     */
    Content readSubObjectContent(String token,String user,String nameSubObject);
    /**
     * 
     * @param token String
     * @param user String
     * @param nameSubObject String
     * @param objId Integer
     * @return  Content
     */
    Content readSubObjectContent(String token,String user,String nameSubObject, Integer objId);
    /**
     * 
     * @param token String
     * @param user String
     * @param documentiId String
     * @param analysisName String
     * @param analysisDescription String
     * @param visibilityBoolean String
     * @param content String
     * @return String
     */
    String saveSubObject(String token,String user,String documentiId,String analysisName,String analysisDescription,String visibilityBoolean,String content);
    /**
     * 
     * @param token String
     * @param user String
     * @param documentiId String
     * @param templateName String
     * @param content String
     * @return String
     */
    String saveObjectTemplate(String token,String user,String documentiId,String templateName,String content);
    /**
     * 
     * @param token String
     * @param user String
     * @param biobjectId String
     * @param fileName String
     * @return Content
     */
    Content downloadAll(String token,String user,String biobjectId,String fileName);
    
    /**
     * Replaces PublishServlet !!!!
     * @param token String
     * @param user String
     * @param name String
     * @param description String
     * @param encrypted String
     * @param visible String
     * @param type String
     * @param state String
     * @param functionalityCode String
     * @param template String
     * @return String
     */
    String publishTemplate(String token,String user,HashMap attributes);
    
    /**
     * Replaces MapCatalogueManagerServlet !!!!
     * @param token String
     * @param user String
     * @param operation String
     * @return String
     */
    String mapCatalogue(String token, String user, String operation,String path,String featureName,String mapName);

    
    /**
     * Read the SVG Map
     * @param token String
     * @param user String
     * @param mapName String
     * @return Content
     */
    Content readMap(String token,String user,String mapName);  
    
}
