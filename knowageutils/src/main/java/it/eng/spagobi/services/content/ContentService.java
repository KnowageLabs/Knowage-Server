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
