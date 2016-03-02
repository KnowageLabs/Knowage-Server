<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@ page import="it.eng.spagobi.engines.documentcomposition.utils.DocumentCompositionUtils,
                 it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration,
                 it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration.Document,
                 it.eng.spago.error.EMFErrorHandler,
                 it.eng.spagobi.commons.utilities.ChannelUtilities,
                 it.eng.spago.error.EMFUserError,
                 it.eng.spago.error.EMFErrorSeverity,
                 java.util.ArrayList,
                 java.util.Collection"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page	import="it.eng.spagobi.engines.documentcomposition.SpagoBIDocumentCompositionInternalEngine"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ include file="/WEB-INF/jsp/analiticalmodel/execution/header.jsp"%>
<LINK rel='StyleSheet' 
      href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/execution/main.css",currTheme)%>'  type='text/css' />
<%! private static transient Logger logger=Logger.getLogger(SpagoBIDocumentCompositionInternalEngine.class);%>

<%  logger.debug("IN");
	EMFErrorHandler errorHandler = aResponseContainer.getErrorHandler();
	AuditManager auditManager2 = AuditManager.getInstance();

	try{		
		// AUDIT UPDATE
		
		if (executionAuditId != null) {
			auditManager2.updateAudit(executionAuditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null,
			    null);
		}

   	//acquisizione info come template a cui girare la richiesta
    String nameTemplate = "";
    String codeError = "";

    //get object configuration
    DocumentCompositionConfiguration docConfig = null;
    docConfig = (DocumentCompositionConfiguration)contextManager.get("docConfig");
     
    // put in session 
    if(docConfig!=null){
    	session.setAttribute("DOC_COMP_CONF",docConfig);
    }
    
    //get template file
    nameTemplate = docConfig.getTemplateFile();
    logger.debug("name TemplateFile: " + nameTemplate);
    
    //get list of documents
    List lstDoc = docConfig.getDocumentsArray();
    
    //get information for document composition
    Map lstUrl = new HashMap();    
    Map lstTitles = new HashMap();
    Map lstExport = new HashMap();
    Map lstStyle = new HashMap();
    Map lstStylePanel = new HashMap();
    Map lstUrlParams  = new HashMap();
    Map lstDocLinked = new HashMap();
    Map lstFieldLinked = new HashMap();
    Map lstCrossLinked = new HashMap(); 
    Map lstExporterTypes = new HashMap();  
    Map lstDocTypes = new HashMap();  
    List lstTestUrl = new ArrayList();
    //loop on documents
    for (int i = 0; i < lstDoc.size(); i++){
    	//gets url, parameters and other informations
    	Document tmpDoc = (Document)lstDoc.get(i);
    	String docType =  tmpDoc.getType();
    	
    	String tmpUrl = DocumentCompositionUtils.getExecutionUrl(tmpDoc.getSbiObjLabel(), aSessionContainer, aRequestContainer.getServiceRequest());
    	codeError = tmpUrl.substring(0,tmpUrl.indexOf("|"));
    	tmpUrl = tmpUrl.substring(tmpUrl.indexOf("|")+1);
    	if (codeError!= null && !codeError.equals("")){
    		List l = new ArrayList();
			l.add(tmpDoc.getSbiObjLabel());
    		EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, codeError, l, SpagoBIDocumentCompositionInternalEngine.messageBundle);
			errorHandler.addError(error);
    	}
    	else{
	    	lstUrlParams.put("SBI_DOC_LABEL__"+(i),  tmpDoc.getSbiObjLabel());
	    	//prepare list of values for the document that it's loading
	    	docConfig.getInfoDocumentLinked(tmpDoc.getSbiObjLabel());
	  
	    	lstUrl.put("URL_DOC__" + (i), tmpUrl);
	    	lstStyle = docConfig.getLstDivStyle();
	    	lstStylePanel = docConfig.getLstPanelStyle();
	        lstDocLinked = docConfig.getLstDocLinked();
	        lstFieldLinked = docConfig.getLstFieldLinked(); 
	        lstTitles = docConfig.getLstDocTitles();
	        lstExport = docConfig.getLstDocExport();
	        lstCrossLinked = docConfig.getLstCrossLinked();
	        lstDocTypes.put("SBI_DOC_TYPE__"+ (i), docType); 
	      	//retrieve document's exporters
	      	if(lstExport != null && lstExport.size()!= 0){
	      		List<String> exporters = DocumentCompositionUtils.getAvailableExporters(tmpDoc.getSbiObjLabel(), aSessionContainer, aRequestContainer.getServiceRequest());
	      		lstExporterTypes.put(tmpDoc.getSbiObjLabel(), exporters);
	      	}
    	}
    	String tmpTestUrl = DocumentCompositionUtils.getEngineTestUrl(tmpDoc.getSbiObjLabel(), aSessionContainer, aRequestContainer.getServiceRequest());
    	codeError = tmpTestUrl.substring(0,tmpTestUrl.indexOf("|"));
    	tmpTestUrl = tmpTestUrl.substring(tmpTestUrl.indexOf("|")+1);
    	if (codeError!= null && !codeError.equals("")){
    		List l = new ArrayList();
			l.add(tmpDoc.getSbiObjLabel());
    		EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, codeError, l, SpagoBIDocumentCompositionInternalEngine.messageBundle);
			errorHandler.addError(error);
    	}
    	else{
    		if (!tmpTestUrl.equals("") && !lstTestUrl.contains(tmpTestUrl)){
    			lstTestUrl.add(tmpTestUrl);
    		}
    	}
    } //for 
	%>
<%@ include
	file="/WEB-INF/jsp/engines/documentcomposition/template/dynamicTemplate.jsp"%>
<%
    if (executionAuditId != null) {
    	auditManager2.updateAudit(executionAuditId, null,new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null,
		    null);
	}
	}catch (Exception e) {
	// Audit Update
		if(executionAuditId!=null){
			auditManager2.updateAudit(executionAuditId, null, new Long(System.currentTimeMillis()), "EXECUTION_FAILED", e
			    .getMessage(), null);	
		logger.error(e);
	   }
	return;    
	}
    logger.debug("OUT"); 
  
    %>

