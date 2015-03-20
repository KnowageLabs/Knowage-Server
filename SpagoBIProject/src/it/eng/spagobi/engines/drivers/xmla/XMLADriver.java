/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.drivers.xmla;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Driver Implementation (IEngineDriver Interface) for XMLA Engine.
 */
public class XMLADriver extends AbstractDriver implements IEngineDriver {

	static private Logger logger = Logger.getLogger(XMLADriver.class);


	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param biobject the biobject
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName) {
		logger.debug("IN");
		Map map = new Hashtable();
		try {
			BIObject biobj = (BIObject) biobject;
			map = getMap(biobj, profile, roleName);
		} catch (ClassCastException cce) {
			logger.error("The parameter is not a BIObject type", cce);
		}
		map = applySecurity(map, profile);
		logger.debug("OUT");
		return map;
	}

	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param subObject SubObject to execute
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param object the object
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName) {
		logger.debug("IN");
		Map map = new Hashtable();
		try {
			BIObject biobj = (BIObject) object;
			map = getMap(biobj, profile, roleName);
			SubObject subObj = (SubObject) subObject;
			map = getParameterMap(object, profile, roleName);
			String nameSub = (subObj.getName()==null)?"":subObj.getName();
			map.put("nameSubObject", nameSub);
			String descrSub = (subObj.getDescription()==null)?"":subObj.getDescription();
			map.put("descriptionSubObject", descrSub);
			String visStr = "Private";
			boolean visBool = subObj.getIsPublic().booleanValue();
			if (visBool)
				visStr = "Public";
			map.put("visibilitySubObject", visStr);
			map.put("subobjectId", subObj.getId());

		} catch (ClassCastException cce) {
			logger.error("The second parameter is not a SubObjectDetail type", cce);

		}
		map = applySecurity(map, profile);
		logger.debug("OUT");
		return map;
	}

	/**
	 * Starting from a BIObject extracts from it the map of the paramaeters for
	 * the execution call
	 * 
	 * @param biobj
	 *                BIObject to execute
	 * @return Map The map of the execution call parameters
	 */
	protected Map getMap(BIObject biobj, IEngUserProfile profile, String roleName) {
		logger.debug("IN");
		Map pars = new Hashtable();
		try {
			ObjTemplate objtemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobj.getId());
			if (objtemplate == null)
				throw new Exception("Active Template null");
			byte[] template = objtemplate.getContent();
			if (template == null)
				throw new Exception("Content of the Active template null");
			String documentId = biobj.getId().toString();
			pars.put("document", documentId);
			pars.put("query", "dynamicOlap");
			pars = addArtifactVersionId(template, pars);
			pars = addDataAccessParameter(profile, roleName, pars, template);
			pars = addBIParameters(biobj, pars);
			pars = addBIParameterDescriptions(biobj, pars);
		} catch (Exception e) {
			logger.error("Error while recovering execution parameter map: \n" + e);
		}
		logger.debug("OUT");
		return pars;
	}

	/**
	 * Adds parameter for data access based on the user functionalities
	 * 
	 * @param profile
	 *                Profile of the user
	 * @param roleName
	 *                Role name of the user
	 * @param pars
	 *                Map of previous parameters
	 * @param template
	 *                bytes of the biobject template
	 * @return The parameter map containing parameter for data access control
	 */
	protected Map addDataAccessParameter(IEngUserProfile profile, String roleName, Map pars, byte[] templateBy) {
		logger.debug("IN");
		try {
			// value of the parameter to send to the engine
			String valueAccessPar = "";
			// get the user functionalities associated to the execution role
			Collection profFuncts = profile.getFunctionalitiesByRole(roleName);
			String dapv = "{"; // data access parameter value (dapv)
			// trasnform template bytes into a string
			String templateStr = new String(templateBy);
			// read the template as a sourcebena
			SourceBean templateSB = SourceBean.fromXMLString(templateStr);
			// get the list of granted dimesions
			List grantDims = templateSB.getAttributeAsList("DATA-ACCESS.GRANTED-DIMENSIONS.DIMENSION");
			// for each granted dimension
			Iterator iterGrantDims = grantDims.iterator();
			while (iterGrantDims.hasNext()) {
				SourceBean dimSB = (SourceBean) iterGrantDims.next();
				// get name of the dimension
				String dimName = (String) dimSB.getAttribute("name");
				dapv = dapv + dimName + "{";
				// get the grantSource of the dimension
				String grantSource = (String) dimSB.getAttribute("grantSource");
				// based on grant source fill the data access token
				if ((grantSource != null) && grantSource.equalsIgnoreCase("ProfileFunctionalities")) {
					String dat = getDataAccessToken(dimSB, profFuncts);
					dapv = dapv + dat + "};";
				}
				if ((grantSource != null) && grantSource.equalsIgnoreCase("ProfileAttributes")) {
					String dat = getDataAccessToken(dimSB, profile);
					dapv = dapv + dat + "};";
				}
			}
			if (dapv.endsWith(";")) {
				dapv = dapv.substring(0, dapv.length() - 1);
			}
			dapv = dapv + "}";
			pars.put("dimension_access_rules", dapv);
		} catch (Exception e) {
			logger.error("Error:", e);
			return pars;
		}
		logger.debug("OUT");
		return pars;
	}

	private String getDataAccessToken(SourceBean dimSB, Collection profileFuncts) {
		logger.debug("IN");
		String datoken = "access=custom,";
		// get the dimension name
		String dimName = (String) dimSB.getAttribute("name");
		// for each funtionality check if it is a data access functionality and
		// if it is related to the template dimension
		Iterator iterProfFuncts = profileFuncts.iterator();
		while (iterProfFuncts.hasNext()) {
			Object objFunct = iterProfFuncts.next();
			if (objFunct instanceof String) {
				String strFunct = (String) objFunct;
				if (strFunct.startsWith("data_access:")) {
					strFunct = strFunct.substring(12);
					if (strFunct.startsWith("/" + dimName)) {
						strFunct = strFunct.replaceFirst("/", "[");
						if (strFunct.indexOf("/") == -1) {
							continue; // means that the path has only one
							// element (only the dimension name)
						}
						strFunct = strFunct.replaceAll("/", "].[");
						strFunct = strFunct + "]";
						datoken = datoken + "member=" + strFunct + "=all,";
					}
				}
			}
		}
		if (datoken.endsWith(",")) {
			datoken = datoken.substring(0, datoken.length() - 1);
		}
		logger.debug("OUT");
		return datoken;
	}

	private String getDataAccessToken(SourceBean dimSB, IEngUserProfile profile) {
		logger.debug("IN");
		String datoken = "";
		try {
			String dimensionName = (String) dimSB.getAttribute("name");
			SourceBean rulesSB = (SourceBean) dimSB.getAttribute("RULES");
			String access = (String) rulesSB.getAttribute("access");
			if (access == null) {
				access = "none";
				logger.warn("Access is not defined for dimension " + dimensionName + "."
						+ " Default value 'none' will be considered.");
			} else if (!access.equalsIgnoreCase("custom") && !access.equalsIgnoreCase("all")
					&& !access.equalsIgnoreCase("none")) {
				access = "none";
				logger.warn(" Default value 'none' will be considered.");
			}
			if (access.equalsIgnoreCase("none") || access.equalsIgnoreCase("all")) {
				datoken = "access=" + access.toLowerCase();
			} else {
				datoken = "access=custom";
				String topLevel = (String) rulesSB.getAttribute("topLevel");
				String bottomLevel = (String) rulesSB.getAttribute("bottomLevel");
				String rollupPolicy = (String) rulesSB.getAttribute("rollupPolicy");
				if (topLevel != null)
					datoken += ",topLevel=" + topLevel;
				if (bottomLevel != null)
					datoken += ",bottomLevel=" + bottomLevel;
				if (rollupPolicy != null)
					datoken += ",rollupPolicy=" + rollupPolicy;
				List members = rulesSB.getAttributeAsList("MEMBERS.MEMBER");
				Iterator membersIt = members.iterator();
				while (membersIt.hasNext()) {
					SourceBean member = (SourceBean) membersIt.next();
					String memberName = (String) member.getAttribute("name");
					String memberAccess = (String) member.getAttribute("access");
					if (memberAccess == null
							|| (!memberAccess.equalsIgnoreCase("all") && !memberAccess.equalsIgnoreCase("none"))) {
						logger.warn("Access is not defined correctly for member " + memberName + "."
								+ " Default value 'none' will be considered.");
						memberAccess = "none";
					}
					if (memberName != null) {
						// memberName =
						// StringUtilities.substituteProfileAttributesInString(memberName,
						// profile);
						// datoken += ",member=" + memberName + "=" +
						// memberAccess;
						List memberNames = new ArrayList();
						memberNames.add(memberName);
						memberNames = generateMemeberNames(memberNames, profile);
						Iterator memberNamesIter = memberNames.iterator();
						while (memberNamesIter.hasNext()) {
							String membName = (String) memberNamesIter.next();
							datoken += ",member=" + membName + "=" + memberAccess;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while returning data access token", e);
		}
		logger.debug("OUT");
		return datoken;
	}

	private List generateMemeberNames(List memNames, IEngUserProfile profile) {
		logger.debug("IN");
		List generatedMemList = new ArrayList();
		Iterator memNamesIter = memNames.iterator();
		// for all member names
		while (memNamesIter.hasNext()) {
			String memName = (String) memNamesIter.next();
			String tmpMemName = memName;
			// check if the name has a profile attribute
			int startInd = memName.indexOf("${"); 
			if (startInd != -1) {
				// if it has, recover the name and value of the profile
				// attribute
				int endInd = memName.indexOf("}", startInd);
				String nameProfAttr = memName.substring((startInd + 2), endInd);
				String valueProfAttr = null;

				try {
					valueProfAttr = (String) profile.getUserAttribute(nameProfAttr);

					//modify for using null or {,{''}} in EXO like default value for all elements
					if (valueProfAttr == null || valueProfAttr.equals("{,{''}}")) valueProfAttr = "*";

				} catch (Exception e) {
					logger.error("Error while recovering profile attribute " + nameProfAttr + " of the user "
							+ ((UserProfile)profile).getUserId(), e);
				}
				valueProfAttr = valueProfAttr.replaceAll("'","");
				// if the value of the profile attribute is not null
				if (valueProfAttr != null) {
					logger.debug("** valueProfAttr: -" + valueProfAttr + "-");
					// all values special character
					if (valueProfAttr.equalsIgnoreCase("*") || valueProfAttr.equalsIgnoreCase("")) {
						String tmp = tmpMemName.substring(0, startInd);
						int indlp = tmp.lastIndexOf(".");
						if (indlp != -1) {
							tmpMemName = tmpMemName.substring(0, indlp);
						} else {
							tmpMemName = "";
						}
						logger.debug("** Added  tmpMemName: -" + tmpMemName + "-");
						generatedMemList.add(tmpMemName);
						// the attribute is multivalue
					} else if (isAttributeMultivalue(valueProfAttr)) {
						String[] values = splitProfAttrValues(valueProfAttr);
						List tmpMemNames = new ArrayList();
						for (int i = 0; i < values.length; i++) {
							String val = values[i];
							String tmpMemNamei = tmpMemName.substring(0, startInd) + val
							+ tmpMemName.substring(endInd + 1);
							logger.debug("** Added  tmpMemNamei: -" + tmpMemNamei + "-");
							tmpMemNames.add(tmpMemNamei);
						}
						tmpMemNames = generateMemeberNames(tmpMemNames, profile);
						generatedMemList.addAll(tmpMemNames);
						// the attribute is single value
					} else {
						tmpMemName = tmpMemName.substring(0, startInd) + valueProfAttr
						+ tmpMemName.substring(endInd + 1);
						List tmpMemNames = new ArrayList();
						logger.debug("** Added tmpMemName: -" + tmpMemName + "-");
						tmpMemNames.add(tmpMemName);
						tmpMemNames = generateMemeberNames(tmpMemNames, profile);
						generatedMemList.addAll(tmpMemNames);
					}
				} else { // if(valueProfAttr!=null)
					logger.debug("** Added  memName: -" + memName + "-");
					generatedMemList.add(memName);
				}
			} else { // if(startInd!=-1)
				logger.debug("** Added  memName: -" + memName + "-");
				generatedMemList.add(memName);
			}
		}
		logger.debug("OUT");
		return generatedMemList;
	}

	private boolean isAttributeMultivalue(String value) {
		logger.debug("IN");
		if (!value.startsWith("{")) {
			logger.debug("OUT");
			return false;
		}
		if (value.length() < 6) {
			logger.debug("OUT");
			return false;
		}
		if (!value.endsWith("}}")) {
			logger.debug("OUT");
			return false;
		}
		if (value.charAt(2) != '{') {
			logger.debug("OUT");
			return false;
		}
		logger.debug("OUT");
		return true;
	}

	private String[] splitProfAttrValues(String value) {
		logger.debug("IN");
		char splitter = value.charAt(1);
		String valuesList = value.substring(3, value.length() - 2);
		String[] values = valuesList.split(String.valueOf(splitter));
		logger.debug("OUT");
		return values;
	}

	/**
	 * Add into the parameters map the BIObject's BIParameter names and values
	 * 
	 * @param biobj
	 *                BIOBject to execute
	 * @param pars
	 *                Map of the parameters for the execution call
	 * @return Map The map of the execution call parameters
	 */
	protected Map addBIParameters(BIObject biobj, Map pars) {
		logger.debug("IN");
		if (biobj == null) {
			logger.warn("BIObject parameter null");
			return pars;
		}
		if (biobj.getBiObjectParameters() != null) {
			BIObjectParameter biobjPar = null;
			String value = null;
			ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
			for (Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();) {
				try {
					biobjPar = (BIObjectParameter) it.next();
					/*
					 * value = (String)biobjPar.getParameterValues().get(0);
					 * pars.put(biobjPar.getParameterUrlName(), value);
					 */
					value = parValuesEncoder.encode(biobjPar);
					pars.put(biobjPar.getParameterUrlName(), value);
				} catch (Exception e) {
					logger.warn("Error while processing a BIParameter", e);
				}
			}
		}
		logger.debug("OUT");
		return pars;
	}
	

	/**
	 * Returns the url to be invoked for editing template document.
	 * 
	 * @param biobject The biobject
	 * @param profile the profile
	 * 
	 * @return the url to be invoked for editing template document
	 * 
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		parameters.put("forward", "editQuery.jsp");
		applySecurity(parameters, profile);
		byte[] template = null;
		try {
			ObjTemplate objTemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(obj.getId());
			if (objTemplate == null || objTemplate.getContent() == null || objTemplate.getContent().length == 0) {
				throw new Exception("Document's template is empty");
			}
			template = objTemplate.getContent();
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while loading document's template", e);
		}
		addArtifactVersionId(template, parameters);
		EngineURL engineURL = new EngineURL(url, parameters);
		logger.debug("OUT");
		return engineURL;
	}

	/**
	 * Returns the url to be invoked for creating a new template document.
	 * 
	 * @param biobject The biobject
	 * @param profile the profile
	 * 
	 * @return the url to be invoked for creating a new template document
	 * 
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		parameters.put("forward", "initialQueryCreator.jsp");
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url, parameters);
		logger.debug("OUT");
		return engineURL;
	}

	protected Map addArtifactVersionId(byte[] template, Map pars) {
		try {
			SourceBean sb = SourceBean.fromXMLString(new String(template));
			SourceBean cubeSb = (SourceBean) sb.getAttribute(SpagoBIConstants.MONDRIAN_CUBE);
			String reference = (String) cubeSb.getAttribute(SpagoBIConstants.MONDRIAN_REFERENCE);
			IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
			Artifact artifact = dao.loadArtifactByNameAndType(reference,
							SpagoBIConstants.MONDRIAN_SCHEMA);
			Content content = dao.loadActiveArtifactContent(artifact.getId());
			pars.put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, content.getId());
			return pars;
		} catch (SourceBeanException e) {
			logger.error("Error while decorating document's template", e);
			throw new SpagoBIRuntimeException("Error while decorating document's template", e);
		}
	}
}
