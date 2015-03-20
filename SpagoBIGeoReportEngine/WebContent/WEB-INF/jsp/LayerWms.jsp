<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
 <%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.*"%>
<%@ page import="net.sf.json.JSON"%>
<%@ page import="net.sf.json.xml.XMLSerializer"%>
<%@ page import="javax.xml.parsers.DocumentBuilder"%>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@ page import="org.w3c.dom.Document"%>
<%@ page import="org.w3c.dom.Element"%>
<%@ page import="org.w3c.dom.Node"%>
<%@ page import="org.w3c.dom.NodeList"%>
<%

// TEST
//String result = "[{id:'0',layername:'TEST_1',srs:'EPSG:32632',imglegend:'http://wms.pcn.minambiente.it/cgi-bin/mapserv.exe?map=/ms_ogc/service/aanp_f32.map&SERVICE=WMS&VERSION=1.1.1&layer=aanp_32&REQUEST=getlegendgraphic&FORMAT=image/png'},{id:'1',layername:'TEST_2',srs:'EPSG:32632',imglegend:'http://wms.pcn.minambiente.it/cgi-bin/mapserv.exe?map=/ms_ogc/service/aanp_f32.map&SERVICE=WMS&VERSION=1.1.1&layer=aanp_32&REQUEST=getlegendgraphic&FORMAT=image/png'}]";
//out.println(result);

// Transform XML (getCapabilities response from WMS server) TO JSON 
/*
//String urlWms = "http://localhost:8080/geoserver/wms?request=getCapabilities"; 
// "http://raster.regione.abruzzo.it/ecwp/ecw_wms.dll?request=GetCapabilities&service=wms"
String urlWms = request.getParameter("urlWms");
String result = null;
    try
    {
    	URL url = new URL(urlWms);
        URLConnection conn = url.openConnection ();
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null)
        {
        sb.append(line);        
        }
        rd.close();
        result = sb.toString();
        //out.println(result);
      
         
         XMLSerializer xmlSerializer = new XMLSerializer(); 
         JSON json = xmlSerializer.read( result );  

         out.println( json.toString());
         
         
     }catch (Exception e)
      {
      e.printStackTrace();
      }  
*/

// Read XML (getCapabilities response from WMS server) and create a new JSON output


String urlWms = request.getParameter("urlWms");
urlWms = urlWms + "?"+ "request=getCapabilities";
String result = "";

try {
  //File file = new File("c:\\MyXMLFile.xml");
  
  URL url = new URL(urlWms);
  URLConnection conn = url.openConnection ();
  
  // DOM way:
  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  DocumentBuilder db = dbf.newDocumentBuilder();
  Document doc = db.parse(url.openStream());

  doc.getDocumentElement().normalize();
  //out.println("Root element " + doc.getDocumentElement().getNodeName());
  NodeList nodeLst = doc.getElementsByTagName("Layer");
  
  int n = (nodeLst.getLength());
  //out.println(n);

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
      
      /*NodeList titleNmElmntLst = fstElmnt.getElementsByTagName("Title");
      Element titleNmElmnt = (Element) titleNmElmntLst.item(0);     
      NodeList titleNm = titleNmElmnt.getChildNodes();
      */

      if(s == n-1){

      result = "{id:"+'\"'+s+'\"'+", layername:"+'\"'+((Node) fstNm.item(0)).getNodeValue()+'\"'+", srs:"+'\"'+((Node) srsNm.item(0)).getNodeValue()+'\"'+"}]" ;
       }
      else if (s == 1){
      result = "[{id:"+'\"'+s+'\"'+", layername:"+'\"'+((Node) fstNm.item(0)).getNodeValue()+'\"'+", srs:"+'\"'+((Node) srsNm.item(0)).getNodeValue()+'\"'+"}"+ ",";
      
      }
      else {
      result = "{id:"+'\"'+s+'\"'+", layername:"+'\"'+((Node) fstNm.item(0)).getNodeValue()+'\"'+", srs:"+'\"'+((Node) srsNm.item(0)).getNodeValue()+'\"'+"}"+ ",";
      
      }
            out.println(result);
    }            
                 
  }
  } catch (Exception e) {
    e.printStackTrace();
  }

%>