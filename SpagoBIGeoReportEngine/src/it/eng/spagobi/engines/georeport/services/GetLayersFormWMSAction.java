/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport.services;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.eng.spagobi.utilities.engines.BaseServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.service.AbstractBaseServlet;


/**
 * @authors Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
 */
public class GetLayersFormWMSAction extends AbstractBaseServlet {
	
	public static final String WMS_URL = "urlWms";
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GetLayersFormWMSAction.class);
    
    
	public void doService( BaseServletIOManager servletIOManager ) throws SpagoBIEngineException {
		 
		String wmsUrl;
		
		logger.debug("IN");
		
		try {
			
			wmsUrl = servletIOManager.getParameterAsString(WMS_URL);
			logger.debug("Parameter [" + WMS_URL + "] is equal to [" + wmsUrl + "]");
			
			wmsUrl = wmsUrl  + "?"+ "request=getCapabilities";
			String resultStr = "";	
			JSONArray results;
			
			URL url = new URL(wmsUrl);
			URLConnection conn = url.openConnection ();
			  
			// DOM way:
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(url.openStream());

			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("Layer");
			  
			int n = (nodeLst.getLength());
			
			results = new JSONArray();
			
			for (int s = 0; s < n; s++) {
			  
				Node fstNode = nodeLst.item(s);

			    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			    
			      Element fstElmnt = (Element) fstNode;
			           
			      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("Name");
			      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);            
			      NodeList fstNm = fstNmElmnt.getChildNodes();
			           
			      NodeList srsNmElmntLst = fstElmnt.getElementsByTagName("SRS");
			      Element srsNmElmnt = (Element) srsNmElmntLst.item(0);     
			      NodeList srsNm = srsNmElmnt.getChildNodes();
			      
			      /*
			      NodeList titleNmElmntLst = fstElmnt.getElementsByTagName("Title");
			      Element titleNmElmnt = (Element) titleNmElmntLst.item(0);     
			      NodeList titleNm = titleNmElmnt.getChildNodes();
			      */

			      JSONObject layerJSON = new JSONObject();
			      String layername = ((Node) fstNm.item(0)).getNodeValue();
			      String srs = ((Node) srsNm.item(0)).getNodeValue();
			      layerJSON.put("id", s);
			      layerJSON.put("layername", layername);
			      layerJSON.put("srs", srs);
			     
			      results.put(layerJSON);
			      			      
			      /*
			      if(s == n-1) {
			    	  resultStr = "{id:"+'\"'+s+'\"'+", layername:"+'\"'+((Node) fstNm.item(0)).getNodeValue()+'\"'+", srs:"+'\"'+((Node) srsNm.item(0)).getNodeValue()+'\"'+"}]" ;
			      } else if (s == 1) {
			    	  resultStr = "[{id:"+'\"'+s+'\"'+", layername:"+'\"'+((Node) fstNm.item(0)).getNodeValue()+'\"'+", srs:"+'\"'+((Node) srsNm.item(0)).getNodeValue()+'\"'+"}"+ ",";
			      } else {
			    	  resultStr = "{id:"+'\"'+s+'\"'+", layername:"+'\"'+((Node) fstNm.item(0)).getNodeValue()+'\"'+", srs:"+'\"'+((Node) srsNm.item(0)).getNodeValue()+'\"'+"}"+ ",";
			      } 
			      */           
	
			    }
			    
			}
			
			servletIOManager.tryToWriteBackToClient(results.toString());
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			logger.debug("OUT");
		}
	}

	public void handleException(BaseServletIOManager servletIOManager,
			Throwable t) {
		t.printStackTrace();		
	}

}
