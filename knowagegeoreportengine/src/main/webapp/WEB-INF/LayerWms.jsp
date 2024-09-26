<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.*"%>
<%@ page import="javax.xml.parsers.DocumentBuilder"%>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@ page import="org.w3c.dom.Document"%>
<%@ page import="org.w3c.dom.Element"%>
<%@ page import="org.w3c.dom.Node"%>
<%@ page import="org.w3c.dom.NodeList"%>
<%@ page import="java.io.IOException"%>
<%@ page import="javax.net.ssl.SSLException"%>
<%
// Read XML (getCapabilities response from WMS server) and create a new JSON output

String urlWms = request.getParameter("urlWms");
urlWms = urlWms + "?" + "request=getCapabilities";
String result = "";
InputStream is = null;

try {
	//File file = new File("c:\\MyXMLFile.xml");

	URL url = new URL(urlWms);
	URLConnection conn = url.openConnection();
	is = conn.getInputStream();

	// DOM way:
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
	dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);

	dbf.setXIncludeAware(false);
	dbf.setExpandEntityReferences(false);

	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(is);

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

	if (s == n - 1) {

		result = "{id:" + '\"' + s + '\"' + ", layername:" + '\"' + ((Node) fstNm.item(0)).getNodeValue() + '\"' + ", srs:" + '\"'
				+ ((Node) srsNm.item(0)).getNodeValue() + '\"' + "}]";
	} else if (s == 1) {
		result = "[{id:" + '\"' + s + '\"' + ", layername:" + '\"' + ((Node) fstNm.item(0)).getNodeValue() + '\"' + ", srs:" + '\"'
				+ ((Node) srsNm.item(0)).getNodeValue() + '\"' + "}" + ",";

	} else {
		result = "{id:" + '\"' + s + '\"' + ", layername:" + '\"' + ((Node) fstNm.item(0)).getNodeValue() + '\"' + ", srs:" + '\"'
				+ ((Node) srsNm.item(0)).getNodeValue() + '\"' + "}" + ",";

	}
	out.println(result);
		}

	}
} catch (SSLException sslException) {
	logger.error("SSLException occurred while creating socket in LayerWMS: ", sslException);
} catch (IOException ioException) {
	logger.error("IOException occurred while creating socket in LayerWMS: ", ioException);
} finally {
	if(is != null) {
		is.close()
	}
}
%>