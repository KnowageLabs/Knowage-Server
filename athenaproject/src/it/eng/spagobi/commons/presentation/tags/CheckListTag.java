/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerPortletAccess;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.ResponseContainerPortletAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.ContextScooping;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * @author Andrea Gioia
 */

public class CheckListTag extends TagSupport
{
	protected String _actionName = null;
	protected String _moduleName = null;
	protected String _serviceName = null;
	protected SourceBean _content = null;
	protected SourceBean _layout = null;
	protected String _providerURL = null;
	protected RequestContainer _requestContainer = null;
	protected SourceBean _serviceRequest = null;
	protected ResponseContainer _responseContainer = null;
	protected SourceBean _serviceResponse = null;
	protected StringBuffer _htmlStream = null;
	protected Vector _columns = null;
    protected String labelLinkSaltoPagina;
    protected String _filter = null;
//  the _paramsMap contains all the ADDITIONAL parameters set by the action or module for the navigation buttons ("next", "previous", "filter" and "all" buttons)
    protected HashMap _paramsMap = new HashMap();
    
    private HttpServletRequest httpRequest = null;
    protected RenderResponse renderResponse = null;
    protected RenderRequest renderRequest = null;
//    the _providerUrlMap contains all the parameters for the navigation buttons ("next", "previous", "filter" and "all" buttons)
    private HashMap _providerUrlMap = new HashMap();
    PortletRequest portReq = null;
    SessionContainer _session = null;
    
	protected IMessageBuilder msgBuilder = null;
	protected String _bundle = null;

    
    /**
     * Consructor.
     */
    public CheckListTag()
    {
    	labelLinkSaltoPagina = "Vai alla Pagina";	
    }

    
    
    /**
     * Do start tag.
     * 
     * @return the int
     * 
     * @throws JspException the jsp exception
     * 
     * @see it.eng.spagobi.commons.presentation.tags.CheckListTag#doStartTag()
     */
    
	public int doStartTag() throws JspException {
		SpagoBITracer.info("Admintools", "ListTag", "doStartTag", " method invoked");
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		renderResponse =(RenderResponse)httpRequest.getAttribute("javax.portlet.response");
		renderRequest =(RenderRequest)httpRequest.getAttribute("javax.portlet.request");
				
		_requestContainer = RequestContainerPortletAccess.getRequestContainer(httpRequest);
		portReq = PortletUtilities.getPortletRequest();
		_serviceRequest = _requestContainer.getServiceRequest();
		_responseContainer = ResponseContainerPortletAccess.getResponseContainer(httpRequest);
		
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		if (_bundle == null)
			_bundle = "messages";

		
		_session = _requestContainer.getSessionContainer();
		
		_serviceResponse = _responseContainer.getServiceResponse();
		ConfigSingleton configure = ConfigSingleton.getInstance();
		if (_actionName != null) {
			_serviceName = _actionName;
			_content = _serviceResponse;
			SourceBean actionBean =
				(SourceBean) configure.getFilteredSourceBeanAttribute("ACTIONS.ACTION", "NAME", _actionName);
			_layout = (SourceBean) actionBean.getAttribute("CONFIG");
			if (_layout == null) {
				// if the layout is dinamically created it is an attribute of the response
				_layout = (SourceBean) _serviceResponse.getAttribute("CONFIG");
			}
			_providerURL = "ACTION_NAME=" + _actionName + "&";
			_providerUrlMap.put("ACTION_NAME", _actionName);
			HashMap params = (HashMap) _serviceResponse.getAttribute("PARAMETERS_MAP");
			if (params != null) {
				_paramsMap = params;
				_providerUrlMap.putAll(_paramsMap);
			}
		} // if (_actionName != null)
		else if (_moduleName != null) {
			_serviceName = _moduleName;
			SpagoBITracer.debug("Admintools", "ListTag", "doStartTag", " Module Name: " + _moduleName);
			_content = (SourceBean) _serviceResponse.getAttribute(_moduleName);
			SourceBean moduleBean =
				(SourceBean) configure.getFilteredSourceBeanAttribute("MODULES.MODULE", "NAME", _moduleName);
			
			if(moduleBean!=null) SpagoBITracer.debug("Admintools", "ListTag", "doStartTag", _moduleName + " configuration loaded");
			_layout = (SourceBean) moduleBean.getAttribute("CONFIG");
			if (_layout == null) {
				// if the layout is dinamically created it is an attribute of the response
				_layout = (SourceBean) _serviceResponse.getAttribute(_moduleName + ".CONFIG");
			}
			String pageName = (String) _serviceRequest.getAttribute("PAGE");
			SpagoBITracer.debug("Admintools", "ListTag", "doStartTag", " PAGE: " + pageName);
			_providerURL = "PAGE=" + pageName + "&MODULE=" + _moduleName + "&";
			_providerUrlMap.put("PAGE", pageName);
			_providerUrlMap.put("MODULE", _moduleName);
			HashMap params = (HashMap) _serviceResponse.getAttribute(_moduleName + ".PARAMETERS_MAP");
			if (params != null) {
				_paramsMap = params;
				_providerUrlMap.putAll(_paramsMap);
			}
		} // if (_moduleName != null)
		else {
			SpagoBITracer.critical("Admintools", "ListTag", "doStartTag", "service name not specified");
			throw new JspException("Business name not specified !");
		} // if (_content == null)
		if (_content == null) {
			SpagoBITracer.warning("Admintools", "ListTag", "doStartTag", "list content null");
			return SKIP_BODY;
		} // if (_content == null)
		if (_layout == null) {
			SpagoBITracer.warning("Admintools", "ListTag", "doStartTag", "list module configuration null");
			return SKIP_BODY;
		} // if (_layout == null)
		// if the LightNavigator is disabled entering the list, it is kept disabled untill exiting the list
		Object lightNavigatorDisabledObj = _serviceRequest.getAttribute(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED);
		if (lightNavigatorDisabledObj != null) {
			String lightNavigatorDisabled = (String) lightNavigatorDisabledObj;
			_providerUrlMap.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, lightNavigatorDisabled);
		}
		_htmlStream = new StringBuffer();
		
		PortletURL url = renderResponse.createActionURL();
		url.setParameter("PAGE", "DetailBIObjectPage"); 
		url.setParameter("MESSAGEDET", "RETURN_FROM_SUBREPORTS_LOOKUP"); 
		url.setParameter("LIGHT_NAVIGATOR_DISABLED", "true"); 		
		 
		_htmlStream.append(" <form method='POST' action='" + url.toString() + "'>\n");
		makeForm();
		_htmlStream.append(" </form>\n");
		try {
			pageContext.getOut().print(_htmlStream);
		} // try
		catch (Exception ex) {
			SpagoBITracer.critical("Admintools", "ListTag", "doStartTag", "Impossible to send the stream");
			throw new JspException("Impossible to send the stream");
		} // catch (Exception ex)
		return SKIP_BODY;
	} // public int doStartTag() throws JspException

	
	
	
	
	/**
	 * Creates a form into the jsp page.
	 * 
	 * @throws JspException If any exception occurs.
	 */
	
	protected void makeForm() throws JspException {
		
		String titleCode = (String) _layout.getAttribute("TITLE");
		SourceBean buttonsSB = (SourceBean) _layout.getAttribute("BUTTONS");
		List buttons = buttonsSB.getContainedSourceBeanAttributes();
		
		if (titleCode != null && buttons.size() > 0) {
			//String title = PortletUtilities.getMessage(titleCode, "messages");
			String title = msgBuilder.getMessage(titleCode, _bundle, httpRequest);
			_htmlStream.append(" <table class=\"header-table-portlet-section\">\n");
			_htmlStream.append("	<tr class='header-row-portlet-section'>\n");
			_htmlStream.append("			<td class=\"header-title-column-portlet-section\" style=\"vertical-align:middle;padding-left:5px;\" >" + title + "</td>\n");
			_htmlStream.append("			<td class=\"header-empty-column-portlet-section\">&nbsp;</td>\n");
			_htmlStream.append(				makeButton(buttons) + "\n");
			_htmlStream.append("	</tr>\n");
			_htmlStream.append(" </table>\n");
		}

		makeColumns();
		makeRows();
		makeNavigationButton();
		
	} // public void makeForm()

	
	/**
	 * Builds Table list columns, reading all request information.
	 * 
	 * @throws JspException If any Exception occurs.
	 */
	
	protected void makeColumns() throws JspException {
		
		SourceBean captionSB = (SourceBean) _layout.getAttribute("CAPTIONS");
		List captions = captionSB.getContainedSourceBeanAttributes();
		int numCaps = captions.size();
		
		_columns = new Vector();
		List columnsVector = _layout.getAttributeAsList("COLUMNS.COLUMN");
		for (int i = 0; i < columnsVector.size(); i++) {
			String hidden = (String)((SourceBean) columnsVector.get(i)).getAttribute("HIDDEN");
			if (hidden == null || hidden.trim().equalsIgnoreCase("FALSE"))
				_columns.add((SourceBean) columnsVector.get(i));
		}
		if ((_columns == null) || (_columns.size() == 0)) {
			SpagoBITracer.critical("Admintools", "ListTag", "doStartTag", "Columns names not defined");
			throw new JspException("Columns names not defined");
		} 
		
		_htmlStream.append("<TABLE style='width:100%;margin-top:1px'>\n");
		_htmlStream.append("	<TR>\n");

		for (int i = 0; i < _columns.size(); i++) {
			String nameColumn = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("NAME");
			String labelColumnCode = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("LABEL");
			String labelColumn = "";
			if (labelColumnCode != null) labelColumn = msgBuilder.getMessage(labelColumnCode, _bundle, httpRequest);
			else labelColumn = nameColumn;
			// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
			String align = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("horizontal-align");
			if (align == null || align.trim().equals("")) align = "left";
			_htmlStream.append("<TD class='portlet-section-header' valign='center' align='" + align + "'  >" + labelColumn + "</TD>\n");
		} 
		for(int i=0; i<numCaps; i++) {
			_htmlStream.append("<TD class='portlet-section-header' align='center'>&nbsp;</TD>\n");
		} 
		_htmlStream.append("</TR>\n");
	} 
	
	
	
	private SourceBean getSubreports(Integer id, Map subreportMap){
		SourceBean subreports = null;
		try {
			subreports = new SourceBean("ROWS");
			Iterator it = subreportMap.keySet().iterator();
			while(it.hasNext()){
				String key = (String)it.next();
				Integer value = (Integer)subreportMap.get(key);
				SourceBean row = new SourceBean("ROW");
				row.setAttribute("MASTER_ID", id);
				row.setAttribute("SUBREPORT_ID", value);
				subreports.setAttribute(row);
			}
		} catch (SourceBeanException e) {
			e.printStackTrace();
		}
		return subreports;
	}
	
	/**
	 * Builds Table list rows, reading all query information.
	 * 
	 * @throws JspException If any Exception occurs.
	 */
	
	
	protected void makeRows() throws JspException 
	{
		List rows = _content.getAttributeAsList("PAGED_LIST.ROWS.ROW");
				
		Integer masterId =  (Integer) _session.getAttribute("MASTER_ID");
		SourceBean subreports = (SourceBean) _session.getAttribute("SUBREPORTS");
		
		TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeRows:SessionContainer session: MASTER_ID = " + masterId);
		
		TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeRows:request: SUBREPORTS = " + subreports);
		
				
		List subreportsList = subreports.getAttributeAsList("ROW");
		Map subreportMap = new HashMap();
		for(int i = 0; i < subreportsList.size(); i++) {
			SourceBean subreport = (SourceBean)subreportsList.get(i);
			Integer id = (Integer)subreport.getAttribute("SUBREPORT_ID");
			if(id!=null) 
				TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"ListTag::makeRows:request: SUBREPORT_ID = " + id);
			subreportMap.put(id.toString(), id);
		}
		
		TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeRows:request: SUBREPORTS = " + getSubreports(masterId, subreportMap));
		
		boolean alternate = false;
        String rowClass;
        String rowId = "";
		for(int i = 0; i < rows.size(); i++) 
		{
			SourceBean row = (SourceBean) rows.get(i);
			TracerSingleton.log(
					Constants.NOME_MODULO,
					TracerSingleton.WARNING,
					"ListTag::makeRows:request: RIGA = " + row);
            rowId = ((Integer)row.getAttribute("OBJ_ID")).toString();
            if(rowId.equalsIgnoreCase(masterId.toString())) continue;
			
            rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
            alternate = !alternate;    
            
            
            
            _htmlStream.append(" <tr class='portlet-font'>\n");
			for (int j = 0; j < _columns.size(); j++) {
				String nameColumn = (String) ((SourceBean) _columns.elementAt(j)).getAttribute("NAME");
				Object fieldObject = row.getAttribute(nameColumn);
				String field = null;
				if (fieldObject != null)
					field = fieldObject.toString();
				else
					field = "&nbsp;";
				
				if(nameColumn.equalsIgnoreCase("OBJ_ID")) rowId = field;
				
				// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
				String align = (String) ((SourceBean) _columns.elementAt(j)).getAttribute("horizontal-align");
				if (align == null || align.trim().equals("")) align = "left";
				_htmlStream.append(" <td class='" + rowClass + "' align='" + align + "' valign='top' >" + field + "</td>\n");
			} 
			
			_htmlStream.append(" <td width='20' class='" + rowClass + "'>\n");
			if(subreportMap.containsKey(rowId)) {
				_htmlStream.append("<input type='checkbox' name='checkbox:" + rowId + "' checked='true'>");
				subreportMap.remove(rowId);
			}
			else {
				_htmlStream.append("<input type='checkbox' name='checkbox:" + rowId + "'>");
			}
			
			_htmlStream.append(" </td>\n");
			_htmlStream.append(" </tr>\n");
		}
		
		_htmlStream.append(" </table>\n");
		_session.delAttribute("SUBREPORTS");
		_session.setAttribute("SUBREPORTS", getSubreports(masterId, subreportMap));
	} 
	
	
	
	
	/**
	 * Builds list navigation buttons inside the list tag. If the number
	 * of elements is higher than 10, they are divided into pages; this
	 * methods creates forward and backward arrows and page number information 
	 * for navigation.
	 * 
	 * @throws JspException If any Exception occurs
	 */
	

	protected void makeNavigationButton() throws JspException {
		
		String pageNumberString = (String) _content.getAttribute("PAGED_LIST.PAGE_NUMBER");
		if(_session.getAttribute("PAGE_NUMBER") != null) 
			_session.delAttribute("PAGE_NUMBER");
		_session.setAttribute("PAGE_NUMBER", pageNumberString);
		
		int pageNumber = 1;
		try {
			pageNumber = Integer.parseInt(pageNumberString);
		} 
		catch (NumberFormatException ex) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeNavigationButton:: PAGE_NUMBER nullo");
		} 
		String pagesNumberString = (String) _content.getAttribute("PAGED_LIST.PAGES_NUMBER");
		int pagesNumber = 1;
		try {
			pagesNumber = Integer.parseInt(pagesNumberString);
		} 
		catch (NumberFormatException ex) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeNavigationButton:: PAGES_NUMBER nullo");
		} 

		int prevPage = pageNumber - 1;
		if (prevPage < 1)
			prevPage = 1;
		int nextPage = pageNumber + 1;
		if (nextPage > pagesNumber)
			nextPage = pagesNumber;
		
		
		// get the right parameters of the request
		//HashMap paramsMap  = getQueryStringParameter();
		// add the parameter for the provider
		//paramsMap.putAll(_providerUrlMap);
				
		_htmlStream.append(" <TABLE CELLPADDING=0 CELLSPACING=0  WIDTH='100%' BORDER=0>\n");
		_htmlStream.append("	<TR>\n");
		_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='14'>\n");
		
        // create link for previous page
		HashMap prevParamsMap = new HashMap();
		prevParamsMap.putAll(_providerUrlMap);
		prevParamsMap.put("MESSAGE", "LIST_PAGE");
		prevParamsMap.put("LIST_PAGE", String.valueOf(prevPage));
		PortletURL prevUrl = createUrl(prevParamsMap);	
		
		HashMap nextParamsMap = new HashMap();
		nextParamsMap.putAll(_providerUrlMap);
		nextParamsMap.put("MESSAGE", "LIST_PAGE");
		nextParamsMap.put("LIST_PAGE", String.valueOf(nextPage));
		PortletURL nextUrl = createUrl(nextParamsMap);
		
		// identity string for object of the page
	    UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
	    UUID uuid = uuidGen.generateTimeBasedUUID();
	    String requestIdentity = uuid.toString();
	    requestIdentity = requestIdentity.replaceAll("-", "");
		String formId = "formFilter" + requestIdentity;
		
		String valueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.VALUE_FILTER);
		String columnFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.COLUMN_FILTER);
		String typeFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_FILTER);
		if (valueFilter != null && columnFilter != null && typeFilter != null) {
			prevUrl.setParameter(SpagoBIConstants.VALUE_FILTER, valueFilter);
			prevUrl.setParameter(SpagoBIConstants.COLUMN_FILTER, columnFilter);
			prevUrl.setParameter(SpagoBIConstants.TYPE_FILTER, typeFilter);
			nextUrl.setParameter(SpagoBIConstants.VALUE_FILTER, valueFilter);
			nextUrl.setParameter(SpagoBIConstants.COLUMN_FILTER, columnFilter);
			nextUrl.setParameter(SpagoBIConstants.TYPE_FILTER , typeFilter);
		} else {
			valueFilter = "";
			columnFilter = "";
			typeFilter = "";
		}
		
		if(pageNumber != 1) {	
			//_htmlStream.append("		<A href=\""+prevUrl.toString()+"\"><IMG src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/img/prevPage.gif")+"' ALIGN=RIGHT border=0></a>\n"); 
			_htmlStream.append("<input type='image' " +
					  "name='" + "prevPage" + "' " +					  
					  "src ='"+ renderResponse.encodeURL(renderRequest.getContextPath() + "/img/prevPage.gif") + "' " + 
					  "align='left' border=0" +
					  "alt='" + "GO To Previous Page" + "'>\n");
		} else {
			_htmlStream.append("		<IMG src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/img/prevPage.gif")+"' ALIGN=RIGHT border=0>\n");
		}		
		_htmlStream.append("		</TD>\n");
				
		// create center blank cell
		//_htmlStream.append("		<TD class='portlet-section-footer'>&nbsp;</TD>\n");
		
        // visualize page numbers
		String pageLabel = msgBuilder.getMessage("ListTag.pageLable", _bundle, httpRequest);
		String pageOfLabel = msgBuilder.getMessage("ListTag.pageOfLable", _bundle, httpRequest);
		_htmlStream.append("						<TD class='portlet-section-footer' align='center'>\n");
		_htmlStream.append("							<font class='aindice'>&nbsp;"+pageLabel+ " " + pageNumber + " " +pageOfLabel+ " " + pagesNumber + "&nbsp;</font>\n");
		_htmlStream.append("						    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n");
		
		// Form for list filtering; if not specified, the filter is enabled
		if (_filter == null || _filter.equalsIgnoreCase("enabled")) {
			
			PortletURL allUrl = createUrl(_providerUrlMap);
			PortletURL filterURL = createUrl(_providerUrlMap);
			
			String label = msgBuilder.getMessage("SBIListLookPage.labelFilter", _bundle, httpRequest);
			String labelStart = msgBuilder.getMessage("SBIListLookPage.startWith", _bundle, httpRequest);;
			String labelEnd = msgBuilder.getMessage("SBIListLookPage.endWith", _bundle, httpRequest);;
			String labelContain = msgBuilder.getMessage("SBIListLookPage.contains", _bundle, httpRequest);;
			String labelEqual = msgBuilder.getMessage("SBIListLookPage.isEquals", _bundle, httpRequest);;
			String labelFilter = msgBuilder.getMessage("SBIListLookPage.filter", _bundle, httpRequest);
			String labelAll = msgBuilder.getMessage("SBIListLookPage.all", _bundle, httpRequest);
			
			_htmlStream.append("						    <br/><br/>\n");
			_htmlStream.append("						    <form action='"+filterURL+"' id='" + formId +"' method='post'>\n");
			_htmlStream.append("						    "+label+"\n");
			_htmlStream.append("						    <select name='" + SpagoBIConstants.COLUMN_FILTER + "'>\n");
			
			for (int i = 0; i < _columns.size(); i++) {
				String nameColumn = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("NAME");
				String labelColumnCode = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("LABEL");
				String labelColumn = new String(nameColumn);
				if (labelColumnCode != null) labelColumn = msgBuilder.getMessage(labelColumnCode, _bundle, httpRequest);
				String selected = "";
				if (nameColumn.equalsIgnoreCase(columnFilter))
					selected = " selected='selected' "; 
				_htmlStream.append("						    	<option value='"+nameColumn+"' "+selected+" >"+labelColumn+"</option>\n");
			}
			String selected = "";
			_htmlStream.append("						    </select>\n");
			_htmlStream.append("						    <select name='" + SpagoBIConstants.TYPE_FILTER + "'>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.START_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.START_FILTER +"' "+selected+" >"+labelStart+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.END_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.END_FILTER +"' "+selected+" >"+labelEnd+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.EQUAL_FILTER +"' "+selected+" >"+labelEqual+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.CONTAIN_FILTER +"' "+selected+" >"+labelContain+"</option>\n");
			_htmlStream.append("						    </select>\n");
			_htmlStream.append("						    <input type=\"text\" name=\"" + SpagoBIConstants.VALUE_FILTER + "\" size=\"10\" value=\""+valueFilter+"\" /> \n");
			_htmlStream.append("						    <a href='javascript:document.getElementById(\"" + formId +"\").submit()'>"+labelFilter+"</a> \n");
			_htmlStream.append(" <a href='"+allUrl.toString()+"'>"+labelAll+"</a> \n");
			_htmlStream.append("						    </form> \n");
			
		}
			
		// create link for next page
		_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='right' width='14'>\n");				
		if(pageNumber != pagesNumber) {	
			//_htmlStream.append("		<A href=\""+nextUrl.toString()+"\"><IMG src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/img/nextPage.gif")+"' ALIGN=RIGHT border=0></a>\n"); 
			_htmlStream.append("<input type='image' " +
					  "name='" + "nextPage" + "' " +
					  "src ='"+ renderResponse.encodeURL(renderRequest.getContextPath() + "/img/nextPage.gif") + "' " +
					  "align='right' border='0'" +
					  "alt='" + "GO To Next Page" + "'>\n");
		} else {
			_htmlStream.append("		<IMG src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/img/nextPage.gif")+"' ALIGN=RIGHT border=0>\n");
		}		
		_htmlStream.append("		</TD>\n");
		_htmlStream.append("	</TR>\n");
		_htmlStream.append("</TABLE>\n");
	} 

	
	
	/**
	 * Starting from the module <code>buttonsSB</code> object, 
	 * creates all buttons for the jsp list. 
	 * @param buttons The list of the buttons 
	 * 
	 * @throws JspException If any exception occurs.
	 */
	
	protected StringBuffer makeButton(List buttons) throws JspException {

		StringBuffer htmlStream = new StringBuffer();

		Iterator iter = buttons.listIterator();
		while (iter.hasNext()) {
			SourceBeanAttribute buttonSBA = (SourceBeanAttribute)iter.next();
			SourceBean buttonSB = (SourceBean)buttonSBA.getValue();
			List parameters = buttonSB.getAttributeAsList("PARAMETER");
			HashMap paramsMap = getParametersMap(parameters, null);
			
			String name = (String) buttonSB.getAttribute("name");
			String img = (String) buttonSB.getAttribute("image");
			String labelCode = (String) buttonSB.getAttribute("label");			
			String label = msgBuilder.getMessage(labelCode, _bundle, httpRequest);
			
			PortletURL buttonUrl = createUrl(paramsMap);
			
			htmlStream.append("<td class=\"header-button-column-portlet-section\">\n");
			htmlStream.append("<input type='image' " +
									  "name='" + name + "' " +
									  "title='" + label + "' " +
									  "class='header-button-image-portlet-section'" + 	
									  "src ='"+ renderResponse.encodeURL(renderRequest.getContextPath() + img) + "' " +
									  "alt='" + label + "'>\n");
			htmlStream.append("</td>\n");
		}
		
		return htmlStream;
	} 
	
		
	/**
	 * Gets all parameter information from a module, putting them into a HashMap.
	 * 
	 * @param parameters The parameters list
	 * @param row The value objects Source Bean 
	 * @return The parameters Hash Map
	 * @throws JspException If any Exception occurred
	 */
	
	protected HashMap getParametersMap(List parameters, SourceBean row) throws JspException {
		
		HashMap params = new HashMap(); 
       
		for (int i = 0; i < parameters.size(); i++) {
			String name = (String) ((SourceBean) parameters.get(i)).getAttribute("NAME");
			String type = (String) ((SourceBean) parameters.get(i)).getAttribute("TYPE");
			String value = (String) ((SourceBean) parameters.get(i)).getAttribute("VALUE");
			String scope = (String) ((SourceBean) parameters.get(i)).getAttribute("SCOPE");
			
			if (name != null) {
				//name = JavaScript.escape(name.toUpperCase());
				name = name.toUpperCase();
				
				if ((type != null) && type.equalsIgnoreCase("RELATIVE")) {
					if ((scope != null) && scope.equalsIgnoreCase("LOCAL")) {
						if (row == null) {
							SpagoBITracer.critical("adminTools", "ListTag", "getParametersMap", "Impossible to associate local scope to the button");
							throw new JspException("Impossible to associate local scope to the button");
						} // if (row == null)
						Object valueObject = row.getAttribute(value);
						if (valueObject != null)
							value = valueObject.toString();
					} // if ((scope != null) && scope.equalsIgnoreCase("LOCAL"))
					else
						value =	(String)(ContextScooping.getScopedParameter(_requestContainer, _responseContainer, value, scope)).toString();
				} // if ((type != null) && type.equalsIgnoreCase("RELATIVE"))
				if (value == null)
					value = "";
				//value = JavaScript.escape(value);
			} // if (name != null)
			
			params.put(name, value);
			
		} // for (int i = 0; i < parameters.size(); i++)
		return params;
	} // protected StringBuffer getParametersList(Vector parameters, SourceBean row) throws JspException

	
	/**
	 * From the parameter HashMap at input, creates the reference navigation url.
	 * 
	 * @param paramsMap The parameter HashMap
	 * @return A <code>portletURL</code> object representing the navigation URL
	 */
	protected PortletURL createUrl(HashMap paramsMap) {
		PortletURL url = renderResponse.createActionURL();
		Set paramsKeys = paramsMap.keySet();
		Iterator iter = paramsKeys.iterator();
		while(iter.hasNext()) {
			String paramKey = (String)iter.next();
			String paramValue = (String)paramsMap.get(paramKey);
//			paramKey = JavaScript.escape(paramKey.toUpperCase());
//			paramValue = JavaScript.escape(paramValue.toUpperCase());
			//paramKey = JavaScript.escape(paramKey);
			//paramValue = JavaScript.escape(paramValue);
            url.setParameter(paramKey, paramValue); 		
		}
		return url;
	}
	

	
	/**
	 * Traces the setting of an action name.
	 * 
	 * @param actionName The action name string at input.
	 */
	public void setActionName(String actionName) {
		TracerSingleton.log(
			Constants.NOME_MODULO,
			TracerSingleton.DEBUG,
			"DefaultDetailTag::setActionName:: actionName [" + actionName + "]");
		_actionName = actionName;
	} // public void setActionName(String actionName)

	/**
	 * Traces the setting of a module name.
	 * 
	 * @param moduleName The module name string at input.
	 */
	
	public void setModuleName(String moduleName) {
		TracerSingleton.log(
			Constants.NOME_MODULO,
			TracerSingleton.INFORMATION,
			"ListTag::setModuleName:: moduleName [" + moduleName + "]");
		_moduleName = moduleName;
	} // public void setModuleName(String moduleName)

	/**
	 * Do end tag.
	 * 
	 * @return the int
	 * 
	 * @throws JspException the jsp exception
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	
	public int doEndTag() throws JspException {
		TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.INFORMATION, "ListTag::doEndTag:: invocato");
		_actionName = null;
		_moduleName = null;
		_serviceName = null;
		_content = null;
		_layout = null;
		_providerURL = null;
		_requestContainer = null;
		_serviceRequest = null;
		_responseContainer = null;
		_serviceResponse = null;
		_htmlStream = null;
		_columns = null;
		return super.doEndTag();
	} // public int doEndTag() throws JspException



	/**
	 * Sets the filter.
	 * 
	 * @param filter the new filter
	 */
	public void setFilter(String filter) {
		TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.INFORMATION,
				"ListTag::setFilter:: filter " + filter);
		_filter = filter;
	}
} // public class ListTag extends TagSupport