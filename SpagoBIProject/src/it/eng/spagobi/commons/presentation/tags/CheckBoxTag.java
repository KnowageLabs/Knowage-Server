/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.ContextScooping;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * @author Gioia
 *
 */
public class CheckBoxTag extends TagSupport {
	static private Logger logger = Logger.getLogger(CheckBoxTag.class);
	
	private HttpServletRequest httpRequest = null;
	
    protected RequestContainer _requestContainer = null;
    protected ResponseContainer _responseContainer = null;
    
    protected SourceBean _serviceRequest = null;	
	protected SourceBean _serviceResponse = null;
    
    SessionContainer _session = null;
	
	protected String _actionName = null;
	protected String _moduleName = null;
	protected String pageName = null;
	
	protected String _serviceName = null;
	protected SourceBean _content = null;
	protected SourceBean _layout = null;
	protected String _providerURL = null;	
	protected StringBuffer _htmlStream = null;
	protected Vector _columns = null;
    protected String labelLinkSaltoPagina;
    protected String _filter = null;
    protected int pageNumber;
    protected int pagesNumber;
    //the navigation's variables
    protected String _prevUrl = null;
    protected String _nextUrl = null;
    protected String _firstUrl = null;
    protected String _lastUrl = null;
    protected String _lstElements = null;
    private String currTheme="";
    
	protected EMFErrorHandler _errorHandler = null;
    
    //  the _paramsMap contains all the ADDITIONAL parameters set by the action or module for the navigation buttons ("next", "previous", "filter" and "all" buttons)
    protected HashMap _paramsMap = new HashMap();
    
    // the _providerUrlMap contains all the parameters for the navigation buttons ("next", "previous", "filter" and "all" buttons)
    private HashMap _providerUrlMap = new HashMap();
    
    protected IUrlBuilder urlBuilder = null;
    protected IMessageBuilder msgBuilder = null;
    
    final static int END_RANGE_PAGES = 6;  
    
    private String rowColor="#F5F6BE";
    
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		SpagoBITracer.info("Admintools", "ListTag", "doStartTag", " method invoked");
		
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		_requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		//portReq = PortletUtilities.getPortletRequest();
		_serviceRequest = _requestContainer.getServiceRequest();
		_responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		_session = _requestContainer.getSessionContainer();
		_serviceResponse = _responseContainer.getServiceResponse();
		urlBuilder = UrlBuilderFactory.getUrlBuilder(_requestContainer.getChannelType());
		msgBuilder = MessageBuilderFactory.getMessageBuilder();

    	currTheme=ThemesManager.getCurrentTheme(_requestContainer);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
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
		} 
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
			
			pageName = (String) _serviceRequest.getAttribute("PAGE");
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
		} else {
			// if the LightNavigator is abled, its LIGHT_NAVIGATOR_REPLACE_LAST function will be used while navigating the list
			_providerUrlMap.put(LightNavigationManager.LIGHT_NAVIGATOR_REPLACE_LAST, "true");
		}
		
		if (_content.getAttribute("optChecked") != null)
			_providerUrlMap.put("optChecked",_content.getAttribute("optChecked"));
		try {
			
		} 
		catch (NumberFormatException ex) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeNavigationButton:: PAGES_NUMBER nullo");
		} 
		
		String pageNumberString = (String) _content.getAttribute("PAGED_LIST.PAGE_NUMBER");
		String pagesNumberString = (String) _content.getAttribute("PAGED_LIST.PAGES_NUMBER");
		pageNumber = 1; 
		pagesNumber = 1;
		try {
			pageNumber = Integer.parseInt(pageNumberString);
			pagesNumber = Integer.parseInt(pagesNumberString);
		} 
		catch (NumberFormatException ex) {
			TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.WARNING,
				"ListTag::makeNavigationButton:: PAGE(s)_NUMBER nullo");
		} 
		
		_htmlStream = new StringBuffer();
		HashMap params = new HashMap();
		params.putAll(_providerUrlMap);
		params.put("MESSAGE", "HANDLE_CHECKLIST"); 
		_session.setAttribute("CHECKEDOBJECTS", _content.getAttribute("CHECKEDOBJECTS"));
		params.put("PAGE_NUMBER", new Integer(pageNumber).toString());
		String url = urlBuilder.getUrl(httpRequest, params);

		_lstElements = (String)_content.getAttribute("checkedElements");
		_htmlStream.append(" <form method='POST' id='form' action='" + url + "'>\n");
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
	}
	
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
			String title = msgBuilder.getMessage(titleCode, "messages", httpRequest);
			_htmlStream.append(" <table class=\"header-table-portlet-section\">\n");
			_htmlStream.append("	<tr class='header-row-portlet-section'>\n");
			_htmlStream.append("			<td class=\"header-title-column-portlet-section\" style=\"vertical-align:middle;padding-left:5px;\" >" + title + "</td>\n");
			_htmlStream.append("			<td class=\"header-empty-column-portlet-section\">&nbsp;</td>\n");
			_htmlStream.append(				makeButton(buttons) + "\n");
			_htmlStream.append("	</tr>\n");
			_htmlStream.append(" </table>\n");
			
	
		}
		defineColumns();
		makeNavigationButton();
		makeColumns();
		makeRows();
		makeFooterList();
		
	} // public void makeForm()

	protected void defineColumns() throws JspException{
		_columns = new Vector();
		List columnsVector = _layout.getAttributeAsList("COLUMNS.COLUMN");
		for (int i = 0; i < columnsVector.size(); i++) {
			String hidden = (String)((SourceBean) columnsVector.get(i)).getAttribute("HIDDEN");
			if (hidden == null || hidden.trim().equalsIgnoreCase("FALSE"))
				_columns.add((SourceBean) columnsVector.get(i));
		}
		if ((_columns == null) || (_columns.size() == 0)) {
			logger.error("Columns names not defined");
			throw new JspException("Columns names not defined");
		} 
	}
	
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
			if (nameColumn.equalsIgnoreCase("description")) nameColumn = "descr";
			String labelColumnCode = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("LABEL");
			String labelColumn = "";
			if (labelColumnCode != null) labelColumn = 
				msgBuilder.getMessage(labelColumnCode, "messages", httpRequest);
			else labelColumn = nameColumn;
			// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
			String align = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("horizontal-align");
			if (align == null || align.trim().equals("")) align = "left";
			//defines order url for dynamic ordering
			HashMap orderParamsMap = new HashMap();
			orderParamsMap.putAll(_providerUrlMap);			
			orderParamsMap.put("FIELD_ORDER", nameColumn);
			orderParamsMap.put("checkedElements",_lstElements);
			orderParamsMap.put("TYPE_ORDER"," ASC");			
			String orderUrlAsc = createUrl(orderParamsMap);
			orderParamsMap.remove("TYPE_ORDER");
			orderParamsMap.put("TYPE_ORDER"," DESC");
			orderParamsMap.put("checkedElements",_lstElements);
			String orderUrlDesc = createUrl(orderParamsMap);
			//_htmlStream.append("<TD class='portlet-section-header' valign='center' align='" + align + "'  >" + labelColumn + "</TD>\n");
			_htmlStream.append("<TD class='portlet-section-header' valign='center' align='" + align + "'  >" + labelColumn + "\n");
			_htmlStream.append("	<A href=\""+orderUrlAsc+"\">\n");
			_htmlStream.append("		<img  src='"+urlBuilder.getResourceLinkByTheme(httpRequest,"/img/commons/ArrowUp.gif",currTheme )+"'/>\n");
			_htmlStream.append("	</A>\n");
			_htmlStream.append("	<A href=\""+orderUrlDesc+"\">\n");
			_htmlStream.append("		<img  src='"+urlBuilder.getResourceLinkByTheme(httpRequest,"/img/commons/ArrowDown.gif",currTheme)+"'/>\n");
			_htmlStream.append("	</A>\n");
			 _htmlStream.append("</TD>\n");
		} 
		for(int i=0; i<numCaps; i++) {
			_htmlStream.append("<TD class='portlet-section-header' align='center'>&nbsp;</TD>\n");
		} 
		_htmlStream.append("	<TD class='portlet-section-header' align='center'>&nbsp;</TD>\n");
		_htmlStream.append("</TR>\n");
	} 
	
	
	
	
	
	/**
	 * Builds Table list rows, reading all query information.
	 * 
	 * @throws JspException If any Exception occurs.
	 */
	
	
	protected void makeRows() throws JspException {

		List rows = _content.getAttributeAsList("PAGED_LIST.ROWS.ROW");
		 _htmlStream.append("<input type =\"hidden\" id=\"checkedElements\" name=\"checkedElements\" value=\""+_lstElements+"\" />");
		 
		// js function for item action confirm
		String confirmCaption = msgBuilder.getMessage("ListTag.confirmCaption", "messages", httpRequest);
		_htmlStream.append(" <script>\n");
		_htmlStream.append("	function actionConfirm(message, url){\n");
		_htmlStream.append("		if (confirm('" + confirmCaption + " ' + message + '?')){\n");
		_htmlStream.append("			location.href = url;\n");
		_htmlStream.append("		}\n");
		_htmlStream.append("	}\n");
		
		_htmlStream.append("	var lstElements = '" + _lstElements + "';\n");
		_htmlStream.append("	function addRemoveElement(value, flgChecked){\n");
		_htmlStream.append("		if (flgChecked){\n");
		_htmlStream.append("			//adds new element \n");
		_htmlStream.append("			if (lstElements != '')\n");
		_htmlStream.append("				lstElements = lstElements + ',' + value;\n");
		_htmlStream.append("		 	else \n");
		_htmlStream.append("				lstElements = value;\n");
		_htmlStream.append("		}else{\n");
		_htmlStream.append("			//removes an element \n");
		_htmlStream.append("			var tmpValue = lstElements.split(','); \n");
		_htmlStream.append("			var tmpStr = '';\n");
		_htmlStream.append("			for (i = 0; i < tmpValue.length; i++){\n");
		_htmlStream.append("				if (tmpValue[i] != value){\n");
		_htmlStream.append("					tmpStr = tmpStr + tmpValue[i];\n");
		_htmlStream.append("					if (i < tmpValue.length -1 ) \n");
		_htmlStream.append("						tmpStr = tmpStr +  ',';\n");
		_htmlStream.append("				}\n");
		_htmlStream.append("			}\n");
		_htmlStream.append("			 \n");
		_htmlStream.append("			lstElements = tmpStr;\n");
		_htmlStream.append("			if (lstElements.substr(lstElements.length-1,1) == ',')\n");
		_htmlStream.append("				lstElements = lstElements.substr(0, lstElements.length-1);\n");
		_htmlStream.append("		}\n");
		_htmlStream.append("		document.getElementById('form').checkedElements.value  = lstElements;\n");
		_htmlStream.append("	}\n");
		
		_htmlStream.append(" </script>\n");
       
		for(int i = 0; i < rows.size(); i++) {
			SourceBean row = (SourceBean) rows.get(i);
             
            _htmlStream.append(" <tr onMouseOver=\"this.bgColor='"+rowColor+"';\" onMouseOut=\"this.bgColor='#FFFFFF';\">\n");
			for (int j = 0; j < _columns.size(); j++) {
				String nameColumn = (String) ((SourceBean) _columns.elementAt(j)).getAttribute("NAME");
				Object fieldObject = row.getAttribute(nameColumn);
				String field = null;
				if (fieldObject != null)
					field = fieldObject.toString();
				else
					field = "&nbsp;";
				// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
				String align = (String) ((SourceBean) _columns.elementAt(j)).getAttribute("horizontal-align");
				if (align == null || align.trim().equals("")) align = "left";
				_htmlStream.append(" <td>" + field + "</td>\n");
			} 
			
			
			SourceBean captionsSB = (SourceBean) _layout.getAttribute("CAPTIONS");
			List captions = captionsSB.getContainedSourceBeanAttributes();
			Iterator iter = captions.iterator();
			
			while (iter.hasNext()) {
				SourceBeanAttribute captionSBA = (SourceBeanAttribute)iter.next();
				SourceBean captionSB = (SourceBean)captionSBA.getValue();
				List parameters = captionSB.getAttributeAsList("PARAMETER");
				HashMap paramsMap = getParametersMap(parameters, row);
				paramsMap.put("checkedElements",_lstElements);
				String img = (String)captionSB.getAttribute("image");
				String labelCode = (String)captionSB.getAttribute("label");
				String label = msgBuilder.getMessage(labelCode, "messages", httpRequest);
				String buttonUrl = urlBuilder.getUrl(httpRequest, paramsMap);
				boolean confirm = false;
				if (captionSB.getAttribute("confirm") != null &&
						((String)captionSB.getAttribute("confirm")).equalsIgnoreCase("TRUE")){
					confirm = true;
				}
				_htmlStream.append(" <td width='20'>\n");
				if (confirm){
					_htmlStream.append("     <a href='javascript:actionConfirm(\"" + label + "\", \"" + buttonUrl.toString() + "\");'>\n");	
				}else{
					_htmlStream.append("     <a href='"+buttonUrl+"'>\n");	
				}
				_htmlStream.append("			<img title='"+label+"' alt='"+label+"' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' />\n");
				_htmlStream.append("     </a>\n");
				_htmlStream.append(" </td>\n");
			}
			_htmlStream.append(" <td width='30px'>\n");	
			String checked = (String)row.getAttribute("CHECKED");
			
			//String objectIdName = (String)((SourceBean) _layout.getAttribute("KEYS.OBJECT")).getAttribute("key");
			Object key = row.getAttribute("ROW_ID");			
			if(checked.equalsIgnoreCase("true")) {
				_htmlStream.append("\t<input type='checkbox' name='checkbox' value='" + GeneralUtilities.substituteQuotesIntoString(key.toString()) + "' checked onClick='addRemoveElement(this.value, this.checked);'>\n");
			}
			else {
				_htmlStream.append("\t<input type='checkbox' name='checkbox' value='" + GeneralUtilities.substituteQuotesIntoString(key.toString()) + "' onClick='addRemoveElement(this.value,  this.checked);'>\n");
			}
			
			_htmlStream.append(" </td>\n");
			
			_htmlStream.append(" </tr>\n");
			
		}
		
		_htmlStream.append(" </table>\n");
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
				
		_htmlStream.append("<script>\n");
		
		_htmlStream.append("function submitForm() {\n");
		_htmlStream.append("	var checkFilter=document.getElementById('checkFilter'); \n");
		_htmlStream.append("	var filterCheckbox=document.getElementById('filterCheckbox'); \n");
		_htmlStream.append("	var checked=document.getElementById('checked'); \n");
		_htmlStream.append("	if(filterCheckbox.checked == false){\n");
		_htmlStream.append("		checked.value = 'false'\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("	else{");
		_htmlStream.append("		checked.value = 'true'\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("	checkFilter.value='checkFilter';\n");
		_htmlStream.append("	document.getElementById('form').submit();\n");
		_htmlStream.append("} \n");
		
		_htmlStream.append("function changePage(type) {\n");
		_htmlStream.append("	if(type == 'firstPage'){\n");
		_htmlStream.append("		document.getElementById('form').LIST_PAGE.value = '"+  String.valueOf(prevPage)+"';\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("	else if (type == 'prevPage'){\n");
		_htmlStream.append("		document.getElementById('form').LIST_PAGE.value = '" +  String.valueOf(1)+"';\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("	else if (type == 'nextPage'){\n");
		_htmlStream.append("		document.getElementById('form').LIST_PAGE.value = '" +  String.valueOf(nextPage)+"';\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("	else if (type == 'lastPage'){\n");
		_htmlStream.append("		document.getElementById('form').LIST_PAGE.value = '" +  String.valueOf(pagesNumber)+"';\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("	else if (type.match('goTo_')){\n");
		_htmlStream.append("		document.getElementById('form').LIST_PAGE.value = type.substring(5);\n");
		_htmlStream.append("	}\n");
		_htmlStream.append("} \n");
		
		_htmlStream.append("</script>\n");
				
		_htmlStream.append(" <TABLE CELLPADDING=0 CELLSPACING=0  WIDTH='100%' BORDER=0>\n");
		_htmlStream.append("<input type =\"hidden\" id=\"LIST_PAGE\" name=\"LIST_PAGE\" value=\"\" />");
		_htmlStream.append("	<TR>\n");
		_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='14'>\n");

		if(pageNumber != 1) {
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "firstPage" + "' " +					  
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png", currTheme) + "' " + 
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)' "+
					  "alt='" + "GO To First Page" + "'>\n");
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  align='left' width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "prevPage" + "' " +					  
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme) + "' " + 
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)' " +
					  "alt='" + "GO To Previous Page" + "'>\n");
			_htmlStream.append("		</TD>\n");
		} else {
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0 />\n");				
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0 />\n");
			_htmlStream.append("		</TD>\n");			
		}	
				
		// create center blank cell
		_htmlStream.append("		<TD class='portlet-section-footer'>\n");
		_htmlStream.append("						    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n");
		
		
		//form for checked elements filtering
		
		if (_filter != null && _filter.equalsIgnoreCase("enabled")) {
			String checked = "";
			String isChecked = (String)_serviceRequest.getAttribute("checked");
			if (isChecked  == null )
				isChecked = (String) _content.getAttribute("optChecked");
			if(isChecked == null){
				isChecked = "true";
			}
			if(isChecked.equals("true")){
				checked = "checked='checked'";
			}
			
			_htmlStream.append("						    <br/><br/>\n");
			String label = msgBuilder.getMessage("CheckboxTag.showChecked", "messages", httpRequest);
			_htmlStream.append("						    "+label+"\n");
			_htmlStream.append("<input type=\"checkbox\"" + checked + " \n");
			_htmlStream.append("				onclick=\"submitForm()\" name=\"filterCheckbox\" id=\"filterCheckbox\" value=\"true\"/>\n");
			_htmlStream.append("<input type =\"hidden\" id=\"checkFilter\" name=\"checkFilter\" value=\"\" />");
			_htmlStream.append("<input type =\"hidden\" id=\"checked\" name=\"checked\" value=\"" + isChecked + "\" />");
			_htmlStream.append("						    </TD>\n");
			
		}
		
		
		//	 create links for navigation
		if(pageNumber != pagesNumber) {	
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "nextPage" + "' " +
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme) + "' " +
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)'"+
					  "alt='" + "GO To Next Page" + "'>\n");
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "lastPage" + "' " +
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme) + "' " +
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)'"+
					  "alt='" + "GO To Last Page" + "'>\n");
			_htmlStream.append("		</TD>\n");
		} else {
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0>\n");
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0>\n");
			_htmlStream.append("		</TD>\n");
		}	
	
		
		_htmlStream.append("	</TR>\n");
		_htmlStream.append("</TABLE>\n");
	} 

	
	/**
	 * Builds Table list footer, reading all request information.
	 * 
	 * @throws JspException If any Exception occurs.
	 */
	protected void makeFooterList() throws JspException {
		String pageNumberString = (String) _content.getAttribute("PAGED_LIST.PAGE_NUMBER");
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
		
		int startRangePages = 1;
		int endRangePages = END_RANGE_PAGES;
		int deltaPages = pagesNumber - endRangePages;
		String dotsStart = "";
		String dotsEnd = "";
		if (deltaPages > 0){
			startRangePages = (pageNumber - 3 > 0)?pageNumber - 3:1;
			endRangePages = ((pageNumber + 3 <= pagesNumber) && (pageNumber + 3 >  END_RANGE_PAGES))?pageNumber + 3: END_RANGE_PAGES;
			if (pageNumber + 3 <= pagesNumber){ 
				if (pageNumber + 3 >  END_RANGE_PAGES) endRangePages = pageNumber + 3;				
				else endRangePages = END_RANGE_PAGES;
			}
			else {
				startRangePages = startRangePages - (pageNumber + 3 - pagesNumber);
				endRangePages = pagesNumber;
			}
			if (endRangePages < pagesNumber) dotsEnd = "... ";
			if (startRangePages > 1) dotsStart = "... ";			
		}
		else {
			startRangePages = 1;
			endRangePages = pagesNumber;
		}
					
		_htmlStream.append(" <TABLE CELLPADDING=0 CELLSPACING=0  WIDTH='100%' BORDER=0>\n");
		_htmlStream.append("	<TR>\n");		
        // visualize page numbers
		String pageLabel = msgBuilder.getMessage("ListTag.pageLable", "messages", httpRequest);
		String pageOfLabel = msgBuilder.getMessage("ListTag.pageOfLable", "messages", httpRequest);
		_htmlStream.append("		<TD class='portlet-section-footer' align='left' width='10%'>\n");
		_htmlStream.append("				<font class='aindice'>&nbsp;"+pageLabel+ " " + pageNumber + " " +pageOfLabel+ " " + pagesNumber + "&nbsp;</font>\n");
		//_htmlStream.append("			    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n");
		_htmlStream.append("		</TD>\n");
		_htmlStream.append("		<TD  class='portlet-section-footer' width='28%'>\n");
		_htmlStream.append("			    &nbsp;\n");
		_htmlStream.append("		</TD>\n");		
		// visualize navigation's icons
		if(pageNumber != 1) {
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "firstPage" + "' " +					  
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme) + "' " + 
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)' "+
					  "alt='" + "GO To First Page" + "'>\n");
			_htmlStream.append("	</TD>\n");
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  align='left' width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "prevPage" + "' " +					  
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme) + "' " + 
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)' " +
					  "alt='" + "GO To Previous Page" + "'>\n");
			_htmlStream.append("	</TD>\n");
		} else {
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0 />\n");				
			_htmlStream.append("	</TD>\n");
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0 />\n");
			_htmlStream.append("	</TD>\n");			
		}		
		_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='15%'>\n");
		_htmlStream.append(dotsStart+"\n");
		for (int i=startRangePages; i <= endRangePages; i++){
//			// create link for last page
//			HashMap tmpParamsMap = new HashMap();
//			tmpParamsMap.putAll(_providerUrlMap);
//			tmpParamsMap.put("MESSAGE", "LIST_PAGE");
//			tmpParamsMap.put("LIST_PAGE", String.valueOf(i));
//			tmpParamsMap.put("checkedElements",_lstElements);
//			String tmpUrl = createUrl(tmpParamsMap);
//			
//			String valueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.VALUE_FILTER);
//			String typeValueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
//			String columnFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.COLUMN_FILTER);
//			String typeFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_FILTER);
//			if (valueFilter != null && columnFilter != null && typeFilter != null) {
//				tmpParamsMap.put(SpagoBIConstants.VALUE_FILTER, valueFilter);
//				tmpParamsMap.put(SpagoBIConstants.TYPE_VALUE_FILTER, typeValueFilter);
//				tmpParamsMap.put(SpagoBIConstants.COLUMN_FILTER, columnFilter);
//				tmpParamsMap.put(SpagoBIConstants.TYPE_FILTER, typeFilter);
//				tmpUrl = createUrl(tmpParamsMap);
//			}		
			
			_htmlStream.append("	<A href=\"javascript:changePage('goTo_"+String.valueOf(i)+"');document.getElementById('form').submit();\">"+String.valueOf(i)+ "</a>\n");
			_htmlStream.append("&nbsp;&nbsp;\n");			
		}
		_htmlStream.append(dotsEnd+"\n");
		_htmlStream.append("	</TD>\n");
		if(pageNumber != pagesNumber) {	
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "nextPage" + "' " +
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme) + "' " +
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)'"+
					  "alt='" + "GO To Next Page" + "'>\n");
			_htmlStream.append("	</TD>\n");
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("<input type='image' " +
					  "name='" + "lastPage" + "' " +
					  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme) + "' " +
					  "align='right' border='0' " +
					  "onClick='changePage(this.name)'"+
					  "alt='" + "GO To Last Page" + "'>\n");
			_htmlStream.append("	</TD>\n");
		} else {
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0>\n");
			_htmlStream.append("	</TD>\n");
			_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0>\n");
			_htmlStream.append("	</TD>\n");
		}		
		_htmlStream.append("		<TD class='portlet-section-footer' width='38%'>\n");
		_htmlStream.append("			    &nbsp;\n");
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
			
			String name = (String) buttonSB.getAttribute("name");
			String img = (String) buttonSB.getAttribute("image");
			String labelCode = (String) buttonSB.getAttribute("label");			
			String label = msgBuilder.getMessage(labelCode, "messages", httpRequest);
			
			htmlStream.append("<td class=\"header-button-column-portlet-section\">\n");
			htmlStream.append("<input type='image' " +
									  "name='" + name + "' " +
									  "title='" + label + "' " +
									  "class='header-button-image-portlet-section'" + 	
									  "src ='"+ urlBuilder.getResourceLinkByTheme(httpRequest, img,currTheme) + "' " +
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
	protected String createUrl(HashMap paramsMap) {
		paramsMap.put("TYPE_LIST", "CHECK_LIST");
		String url = urlBuilder.getUrl(httpRequest, paramsMap);
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
}


















