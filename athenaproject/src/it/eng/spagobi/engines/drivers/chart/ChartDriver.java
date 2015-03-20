/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.chart;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.messages.IEngineMessageBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;



/**
 * Driver Implementation (IEngineDriver Interface) for Chart External Engine. 
 */
public class ChartDriver extends GenericDriver {
	
	static private Logger logger = Logger.getLogger(ChartDriver.class);
	private Locale locale;
	 
		
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param analyticalDocument the biobject
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object analyticalDocument, IEngUserProfile profile, String roleName) {
		Map parameters;
		BIObject biObject=null;
		
		logger.debug("IN");
		
		try {
			if(analyticalDocument instanceof BIObject) biObject=(BIObject) analyticalDocument;
			parameters = super.getParameterMap(analyticalDocument, profile, roleName);
			parameters = applyService(parameters, biObject);
		} finally {
			logger.debug("OUT");
		}
		return parameters;
	}
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param analyticalDocumentSubObject SubObject to execute
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param analyticalDocument the object
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object analyticalDocument, Object analyticalDocumentSubObject, IEngUserProfile profile, String roleName) {		
		return super.getParameterMap(analyticalDocument, analyticalDocumentSubObject, profile, roleName);	
		
	}
	
	 /**
 	 * Function not implemented. Thid method should not be called
 	 * 
 	 * @param biobject The BIOBject to edit
 	 * @param profile the profile
 	 * 
 	 * @return the edits the document template build url
 	 * 
 	 * @throws InvalidOperationRequest the invalid operation request
 	 */
    public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
    	logger.warn("Function not implemented");
    	throw new InvalidOperationRequest();
    }

    /**
     * Function not implemented. Thid method should not be called
     * 
     * @param biobject  The BIOBject to edit
     * @param profile the profile
     * 
     * @return the new document template build url
     * 
     * @throws InvalidOperationRequest the invalid operation request
     */
    public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
    	logger.warn("Function not implemented");
    	throw new InvalidOperationRequest();
    }

    private final static String PARAM_SERVICE_NAME = "ACTION_NAME";
    private final static String PARAM_NEW_SESSION = "NEW_SESSION";
    private final static String PARAM_MODALITY = "MODALITY";
    
	private Map applyService(Map parameters, BIObject biObject) {
		ObjTemplate template;
		
		logger.debug("IN");
		
		String family = null;
		try {
			template = getTemplate(biObject);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(template.getContent()));
			family = doc.getChildNodes().item(0).getNodeName();
		} catch (Throwable t){}
		
		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");
			//at the moment the initial aztion_name is fixed for extJS... in the future it will be set
			//by the template content (firstTag EXTCHART,...)
//			if("D3CHART".equalsIgnoreCase(family))
//			{
//				parameters.put(PARAM_SERVICE_NAME, "CHART_ENGINE_D3_START_ACTION");
//			}
//			else
//			{
//				parameters.put(PARAM_SERVICE_NAME, "CHART_ENGINE_EXTJS_START_ACTION");
//			}
			parameters.put(PARAM_SERVICE_NAME, "CHART_ENGINE_START_ACTION");
			
			parameters.put(PARAM_MODALITY, "VIEW");			
			parameters.put(PARAM_NEW_SESSION, "TRUE");
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to guess from template extension the engine startup service to call");
		} finally {
			logger.debug("OUT");
		}
		return parameters;
	}
	
	  public void applyLocale(Locale locale){
	    	logger.warn("Method implemented for chart driver.");
	    	if (locale == null){
	    		logger.warn("Locale not defined.");
	    		return;
	    	}
	    	this.locale = locale;
	    	logger.warn("Setted locale as " + locale.getCountry() + " - " + locale.getLanguage());
	    }

	  
	private ObjTemplate getTemplate(BIObject biObject) {
		ObjTemplate template;
		IObjTemplateDAO templateDAO;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(biObject, "Input [biObject] cannot be null");
			
			templateDAO = DAOFactory.getObjTemplateDAO();
			Assert.assertNotNull(templateDAO, "Impossible to instantiate templateDAO");
		
			template = templateDAO.getBIObjectActiveTemplate( biObject.getId() );
			Assert.assertNotNull(template, "Loaded template cannot be null");	
			
			logger.debug("Active template [" + template.getName() + "] of document [" + biObject.getLabel() + "] loaded succesfully");
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to load template for document [" + biObject.getLabel()+ "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return template;
	}
  
    private Locale getLocale() {
    	logger.debug("IN");
		try {
			if (this.locale == null){			
				RequestContainer requestContainer = RequestContainer.getRequestContainer();
				SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
				String language = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
				String country = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
				logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
				this.locale = new Locale(language, country);
			}
			return locale;
		} catch (Exception e) {
		    logger.error("Error while getting locale; using default one", e);
		    return GeneralUtilities.getDefaultLocale();
		} finally  {
			logger.debug("OUT");
		}	
	}
   
    
	public byte[] ElaborateTemplate(byte[] template) throws InvalidOperationRequest {
		logger.warn("Internationalization chart template begin...");
		byte[] toReturn = null;

		SourceBean content;
		try {
			content = SourceBean.fromXMLString(new String(template));
		} catch (Throwable t) {
			// no xml but json
			logger.warn("Impossible to parse template as XML", t);
			logger.debug("Orginal template will be returned without futher elaboration", t);
		
			String templateStr = new String(template);
			String localizedTemplate = replaceMessagesInString(templateStr);
			
			return localizedTemplate.getBytes();
		}

		try {
			List atts = ((SourceBean) content).getContainedAttributes();
			for (int i = 0; i < atts.size(); i++) {
				SourceBeanAttribute object = (SourceBeanAttribute) atts.get(i);
				replaceAllMessages(object);
			}
			String result = content.toString();
			toReturn = result.getBytes();
		} catch (Exception e) {
			logger.error("Error while elaborating chart's template: " + e.getMessage());
		}
		logger.warn("Internationalization chart template end");
		return toReturn;
	}
	
	private String replaceMessagesInString(String targetString) {
		StringBuffer sb = new StringBuffer();

		IMessageBuilder engineMessageBuilder = MessageBuilderFactory
				.getMessageBuilder();

		String remainingString = targetString;
		int nextMessageStartIndex = -1;
		int nextMessageEndIndex = -1;
		while ((nextMessageStartIndex = remainingString.indexOf("$R{")) != -1) {
			nextMessageEndIndex = remainingString.indexOf("}",
					nextMessageStartIndex);

			String headString = remainingString.substring(0,
					nextMessageStartIndex);
			String parName = remainingString.substring(
					nextMessageStartIndex + 3, nextMessageEndIndex);
			remainingString = remainingString
					.substring(nextMessageEndIndex + 1);
			if (!parName.equals("")) {
				try {
					if (headString != null && !headString.equals("")) {
						sb.append(headString);
					}
					String val = engineMessageBuilder.getI18nMessage(
							this.getLocale(), parName).replaceAll("'", "");
					// String val = parName.toUpperCase();
					if (!val.equals("%")) {
						sb.append(val);
					}
				} catch (Exception e1) {
					logger.error("Error while replacing message in value: ", e1);
				}

			} else {
				sb.append("");
			}
		}

		if (remainingString != null && !remainingString.equals("")) {
			sb.append(remainingString);
		}

		return sb.toString();
	}
	
    
    private String replaceMessagesInValue(String valueString, boolean addFinalSpace) {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(valueString);
		IMessageBuilder engineMessageBuilder = MessageBuilderFactory.getMessageBuilder();
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.indexOf("$R{") != -1) {
				String parName = tok.substring(tok.indexOf("$R{") + 3, tok.indexOf("}"));
				String remnantString = tok.substring(tok.indexOf("}") + 1);
				if (!parName.equals("")) {
					try {						
						String val = engineMessageBuilder.getI18nMessage(this.getLocale(), parName).replaceAll("'", "");
						if (!val.equals("%")) {
							sb.append(val);
						}
						if (remnantString != null && !remnantString.equals("")) {
							sb.append(remnantString);
							addFinalSpace = false;
						}
						if (addFinalSpace)
							sb.append(" ");
					} catch (Exception e1) {
						logger.error("Error while replacing message in value: " + e1.getMessage());
					}
				}
			} else {
				sb.append(tok);
				sb.append(" ");
			}
		}

		return sb.toString();
	}
	
	/**
	 * Replaces all messages reading by i18n table.
	 * 
	 * @param sb the source bean
	 */
	private void replaceAllMessages(SourceBeanAttribute sb) {
		try {
			if (sb.getValue() instanceof SourceBean) {
				SourceBean sbSubConfig = (SourceBean) sb.getValue();
				List subAtts = sbSubConfig.getContainedAttributes();

				// standard tag attributes
				for (int i = 0; i < subAtts.size(); i++) {
					SourceBeanAttribute object = (SourceBeanAttribute) subAtts.get(i);
					if (object.getValue() instanceof SourceBean) {
						replaceAllMessages(object);
					}else {
						String value = String.valueOf(object.getValue());
						if(value.contains("$R{")) {
							String key = object.getKey();
							boolean addFinalSpace = (key.equals("text") ? true : false);
							Object finalValue = replaceMessagesInValue(value, addFinalSpace);
							object.setValue(finalValue);
						}						
					}
				}
			} else {
				// puts the simple value attribute
				String value = String.valueOf(sb.getValue());
				if(value.contains("$R{")) {
					String key = sb.getKey();
					boolean addFinalSpace = (key.equals("text") ? true : false);
					Object finalValue = replaceMessagesInValue(value, addFinalSpace);
					sb.setValue(finalValue);
				}
			}
		} catch (Exception e) {
			logger.error("Error while defining json chart template: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		ChartDriver driver = new ChartDriver();
		//String str = "xxx $R{pippo}tail head$R{pippo}tail $R{pippo} xxx";
		String str = "dnsadn dknwk ldwnd lkwndxlkw xnwlknd";
		System.out.println(str);
		String newStr = driver.replaceMessagesInString(str);
		System.out.println(newStr);
	}

}

