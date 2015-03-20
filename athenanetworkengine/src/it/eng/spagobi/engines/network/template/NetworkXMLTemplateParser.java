/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.template;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.network.bean.CrossNavigationLink;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.json.JSONTemplateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NetworkXMLTemplateParser implements INetworkTemplateParser{

	public final static String CURRENT_VERSION = "0";
	
	public final static String ATTRIBUTE_VERSION = "version";
	public final static String TAG_GRAPHML = "GRAPHML";
	public final static String TAG_XGMML= "graph";
	public final static String TAG_NETWOK_DEFINITION = "NETWOK_DEFINITION";
	public static final String DRILL_TAG = "DRILL";
	public static final String INFO_TAG = "INFO";
	public static final String PARAM_TAG = "PARAM";
	public static final String INFO_TITLE = "TITLE";
	public static final String DRILL_DOCUMENT_ATTR = "document";
	public static final String DRILL_TARGHET_ATTR = "target";
	public static final String PARAM_NAME_ATTR = "name";
	public static final String PARAM_TYPE_ATTR = "type";
	public static final String PARAM_VALUE_ATTR = "value";
	public static final String PARAM_PROPERTY_ATTR = "property";
	public static final String PARAM_TYPE_RELATIVE = "RELATIVE";
	public static final String PARAM_TYPE_ABSOLUTE = "ABSOLUTE";
	public static final String NODE = "NODE";
	public static final String EDGE = "EDGE";
	public static final String NODES = "nodes";
	public static final String EDGES = "edges";

	
	public static transient Logger logger = Logger.getLogger(NetworkXMLTemplateParser.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.network.template.INetworkTemplateParser#parse(java.lang.Object, java.util.Map)
	 */
	public NetworkTemplate parse(Object templateObject, Map env) {

		NetworkTemplate networkTemplate;
		String encodingFormatVersion;
		SourceBean template;
		
		
		
		try {
			SourceBean xml =  SourceBean.fromXMLString((String)templateObject);
			Assert.assertNotNull(xml, "SourceBean in input cannot be not be null");
			logger.debug("Parsing template [" + xml.getName() + "] ...");
			
			networkTemplate = new NetworkTemplate();

			encodingFormatVersion = (String) xml.getAttribute(ATTRIBUTE_VERSION);
			
			if (encodingFormatVersion == null) {
				logger.debug("no version found, default is 0");
				encodingFormatVersion = "0";
			}
			
			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");
			
			if (encodingFormatVersion.equalsIgnoreCase(CURRENT_VERSION)) {				
				template = xml;
			} else {
				logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine [" + CURRENT_VERSION + "] ");
				logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version [" + CURRENT_VERSION + "]....");
				logger.debug("NO VERSION TRASFORMER IS SPECIFIED FOR VERSION "+CURRENT_VERSION);
				template = xml;
			}
			
			// This is the template in the pure format GRAPHML
			if (template.getName().equalsIgnoreCase(TAG_GRAPHML)) {
				//SourceBean graphmlTemplate = (SourceBean) template.getAttribute(TAG_GRAPHML);
				networkTemplate.setNetworkXML((String)templateObject);
				
			}else
				// This is the template in the pure format XGMML
			if (template.getName().equalsIgnoreCase(TAG_XGMML)) {
				//SourceBean graphmlTemplate = (SourceBean) template.getAttribute(TAG_GRAPHML);
				networkTemplate.setNetworkXML((String)templateObject);
			}else	
				
				
			// TAG_GRAPH_OPTIONS block
			if(template.containsAttribute(TAG_NETWOK_DEFINITION)) {
				SourceBean networkDefinitionBean = (SourceBean) template.getAttribute(TAG_NETWOK_DEFINITION);
				networkTemplate.setNetworkJSNO(loadTemplateFeatures(networkDefinitionBean));
				JSONObject info = getNetworkInfo(template);
				networkTemplate.setInfo(info);
			} 

			networkTemplate.setCrossNavigationLink(getDrill(template,new HashMap()));

			
			logger.debug("Templete parsed succesfully");

		} catch(Throwable t) {
			throw new NetworkTemplateParserException("Impossible to parse template [" + templateObject.toString()+ "]", t);
		} finally {
			logger.debug("OUT");
		}	

		return networkTemplate;
	}

	/**
	 * Load the json object with the option for the network from the template
	 * @param optionsBean the xml bean with the options
	 * @return a json object with the options
	 * @throws Exception
	 */
	private JSONObject loadTemplateFeatures(SourceBean optionsBean) throws Exception {
		JSONTemplateUtils ju = new JSONTemplateUtils();
		//JSONArray array = toJSONArray(this.paramsMap);
		JSONArray array =new JSONArray();
		JSONObject features = ju.getJSONTemplateFromXml(optionsBean, array);
		return features;
	}
	
	

//	private JSONArray toJSONArray(HashMap<String, String> paramsMap) {
//		JSONArray array = new JSONArray();
//		if (paramsMap != null && !paramsMap.isEmpty()) {
//			Iterator<String> it = paramsMap.keySet().iterator();
//			while (it.hasNext()) {
//				String name = it.next();
//				JSONObject obj = new JSONObject();
//				try {
//					obj.put("name", name);
//					obj.put("value", paramsMap.get(name));
//				} catch (JSONException e) {
//					throw new RuntimeException("cannot convert [" + paramsMap.toString() + "] into a JSONArray");
//				}
//				array.put(obj);
//			}
//		}
//		return array;
//	}
	
	


	/**
	 * Parse the tamplate to get the cross navigation link
	 * @param template template as ResourceBean
	 * @param paramsMap mp of the parameters
	 * @return
	 * @throws Exception
	 */
	private JSONObject getNetworkInfo(SourceBean template) throws Exception {

		SourceBean infoSB = null;
		String title = null;
		String content = null;

		logger.debug("IN");
		infoSB = (SourceBean)template.getAttribute(INFO_TAG);
		if(infoSB == null) {
			logger.debug("Cannot find title drill settings: tag name " + DRILL_TAG);
			return null;
		}
		title = (String)infoSB.getAttribute(INFO_TITLE);
		content = (String)infoSB.getCharacters();

		JSONObject info = new JSONObject();
		if(title!=null){
			info.put(INFO_TITLE, title);
		}
		info.put("content", content);
		return info;
	}

	/**
	 * Parse the tamplate to get the cross navigation link
	 * @param template template as ResourceBean
	 * @param paramsMap mp of the parameters
	 * @return
	 * @throws Exception
	 */
	private CrossNavigationLink getDrill(SourceBean template, Map paramsMap) throws Exception {
		
		SourceBean confSB = null;
		String documentName = null;
		String target = null;
		
		logger.debug("IN");
		confSB = (SourceBean)template.getAttribute(DRILL_TAG);
		if(confSB == null) {
			logger.debug("Cannot find title drill settings: tag name " + DRILL_TAG);
			return null;
		}
		documentName = (String)confSB.getAttribute(DRILL_DOCUMENT_ATTR);
		target = (String)confSB.getAttribute(DRILL_TARGHET_ATTR);

		CrossNavigationLink drill = new CrossNavigationLink(documentName);
		
		if(target!=null && target.length()>0){
			drill.setTarget(target);
		}
		
		List paramslist = (List)template.getAttributeAsList(DRILL_TAG+"."+PARAM_TAG);

		if(paramslist != null){
			
			for(int k=0; k<paramslist.size(); k++){
				SourceBean param = (SourceBean)paramslist.get(k);
				String paramName = (String)param.getAttribute(PARAM_NAME_ATTR);
				String paramType = (String)param.getAttribute(PARAM_TYPE_ATTR);
				String paramValue = (String)param.getAttribute(PARAM_VALUE_ATTR);
				String paramProperty = (String)param.getAttribute(PARAM_PROPERTY_ATTR);
				
				//FILLS RELATIVE TYPE PARAMETERS' VALUE FROM REQUEST
				if(paramType.equalsIgnoreCase(PARAM_TYPE_RELATIVE)){
					paramValue=  (String)paramsMap.get(paramName);
				}
				drill.addParameter(paramName, paramValue, paramType,paramProperty);
			}
		}
		
		logger.debug("OUT");	
		return drill;

	}
	
}
