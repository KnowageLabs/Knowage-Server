/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.util.ContextScooping;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Builds and presents all objects list for all admin 
 * SpagoBI's list modules. Once a list module has been executed, 
 * the list tag builds all the correspondent jsp page and gives the results
 * 
 */

public class ListTag extends TagSupport
{
	static private Logger logger = Logger.getLogger(ListTag.class);

	protected String _actionName = null;
	protected String _moduleName = null;
	protected String _bundle = null;
	protected String _serviceName = null;
	protected SourceBean _content = null;
	protected SourceBean _layout = null;
	protected String _providerURL = null;
	protected RequestContainer _requestContainer = null;
	protected SourceBean _serviceRequest = null;
	protected ResponseContainer _responseContainer = null;
	protected SourceBean _serviceResponse = null;
	protected EMFErrorHandler _errorHandler = null;
	protected StringBuffer _htmlStream = null;
	protected Vector _columns = null;
	protected Vector _titleButton = null;	
	protected String labelLinkSaltoPagina;
	protected String _filter = null;
	//the navigation's variables
	protected String _prevUrl = null;
	protected String _nextUrl = null;
	protected String _firstUrl = null;
	protected String _lastUrl = null;
	protected String _refreshUrl = null;
	protected IEngUserProfile profile = null;
	protected String currTheme="";

	protected HttpServletRequest httpRequest = null;
	protected HttpServletResponse response = null;
	protected IUrlBuilder urlBuilder = null;
	protected IMessageBuilder msgBuilder = null;

	// the _providerUrlMap contains all the parameters for the navigation buttons ("next", "previous", "filter" and "all" buttons)
	private HashMap _providerUrlMap = null;
	// the _paramsMap contains all the ADDITIONAL parameters set by the action or module for the navigation buttons ("next", "previous", "filter" and "all" buttons)
	protected HashMap _paramsMap = null;

	final static int END_RANGE_PAGES = 6;    
	final static String[] EXCEPTION_MODULES ={"JobManagementPage", "TriggerManagementPage"};
	final static String[] EXCEPTION_ATTRIBUTES ={"JOBNAME","JOBGROUPNAME"};

	protected String requestIdentity = null;

	private String rowColor="#F5F6BE";

	/**
	 * Constructor.
	 */
	public ListTag()
	{
		labelLinkSaltoPagina = "Vai alla Pagina";	
	}


	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		logger.info(" method invoked");
		_providerUrlMap = new HashMap();
		_paramsMap = new HashMap();
		
		// Angelo (19/06/2009) BUG FiX: navigator of Events doesn't work
		if(ChannelUtilities.isWebRunning()) {
			_providerUrlMap.put(SpagoBIConstants.WEBMODE, "TRUE");
		}
		
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		_requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		_responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		_serviceRequest = _requestContainer.getServiceRequest();
		_serviceResponse = _responseContainer.getServiceResponse();
		_errorHandler = _responseContainer.getErrorHandler();
		response = (HttpServletResponse) pageContext.getResponse();
		urlBuilder = UrlBuilderFactory.getUrlBuilder(_requestContainer.getChannelType());
		msgBuilder = MessageBuilderFactory.getMessageBuilder();

    	currTheme=ThemesManager.getCurrentTheme(_requestContainer);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
		if (_bundle == null)
			_bundle = "messages";

		profile = (IEngUserProfile) _requestContainer.getSessionContainer().getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		// identity string for object of the page
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuid = uuidGen.generateTimeBasedUUID();
		requestIdentity = uuid.toString();
		requestIdentity = requestIdentity.replaceAll("-", "");

		ConfigSingleton configure = ConfigSingleton.getInstance();
		if (_actionName != null) {
			_serviceName = _actionName;
			_content = _serviceResponse;
			SourceBean actionBean =	(SourceBean) configure.getFilteredSourceBeanAttribute("ACTIONS.ACTION", "NAME", _actionName);
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
			logger.debug(" Module Name: " + _moduleName);
			_content = (SourceBean) _serviceResponse.getAttribute(_moduleName);
			SourceBean moduleBean = (SourceBean) configure.getFilteredSourceBeanAttribute("MODULES.MODULE", "NAME", _moduleName);
			if(moduleBean!=null) logger.debug(" configuration loaded");
			_layout = (SourceBean) moduleBean.getAttribute("CONFIG");
			if (_layout == null) {
				// if the layout is dinamically created it is an attribute of the response
				_layout = (SourceBean) _serviceResponse.getAttribute(_moduleName + ".CONFIG");
			}
			String pageName = (String) _serviceRequest.getAttribute("PAGE");
			logger.debug(" PAGE: " + pageName);
			_providerURL = "PAGE=" + pageName + "&MODULE=" + _moduleName + "&";
			_providerUrlMap.put("PAGE", pageName);
			_providerUrlMap.put("MODULE", _moduleName);


			//checks for exception module (ie. for job and trigger must added the parameter MESSAGEDET into url
			for (int i = 0; i < EXCEPTION_MODULES.length; i++){
				if (pageName.equalsIgnoreCase(EXCEPTION_MODULES[i])){
					_providerUrlMap = updateUrlForExceptions(_providerUrlMap, _serviceRequest);														
					break;
				}
			}
			HashMap params = (HashMap) _serviceResponse.getAttribute(_moduleName + ".PARAMETERS_MAP");
			if (params != null) {
				_paramsMap = params;
				_providerUrlMap.putAll(_paramsMap);
			}

		} // if (_moduleName != null)
		else {
			logger.error("service name not specified");
			throw new JspException("Business name not specified !");
		} // if (_content == null)
		if (_content == null) {
			logger.warn("list content null");
			return SKIP_BODY;
		} // if (_content == null)
		if (_layout == null) {
			logger.warn("list module configuration null");
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
		_htmlStream = new StringBuffer();
		makeForm();
		try {
			pageContext.getOut().print(_htmlStream);
		} // try
		catch (Exception ex) {
			logger.error("Impossible to send the stream");
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
			String title = msgBuilder.getMessage(titleCode, _bundle, httpRequest);
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
		//makeNavigationButton();

	} // public void makeForm()

	protected void defineColumns() throws JspException{
		_columns = new Vector();
		_titleButton=new Vector();
		List columnsVector = _layout.getAttributeAsList("COLUMNS.COLUMN");
		for (int i = 0; i < columnsVector.size(); i++) {
			String hidden = (String)((SourceBean) columnsVector.get(i)).getAttribute("HIDDEN");
			if (hidden == null || hidden.trim().equalsIgnoreCase("FALSE")){
				SourceBean sb=(SourceBean) columnsVector.get(i);
				_columns.add(sb);

				// check if there are columsn buttons	
				SourceBean sbButton = (SourceBean) sb.getAttribute("BUTTONS");
				if(sbButton!=null){
					List buttons = sbButton.getContainedSourceBeanAttributes();
					_titleButton.add(buttons);	
					//makeTitleButton(buttons);
				}
				else{
					_titleButton.add(new ArrayList());
				}
			}

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
		String columnFilter = (String)_serviceRequest.getAttribute("columnFilter");
		String typeFilter= (String)_serviceRequest.getAttribute("typeFilter");
		String typeValueFilter = (String)_serviceRequest.getAttribute("typeValueFilter");
		String valueFilter = (String)_serviceRequest.getAttribute("valueFilter");


		_htmlStream.append("<TABLE class='list' style='width:100%;margin-top:1px'>\n");
		_htmlStream.append("	<TR>\n");

		for (int i = 0; i < _columns.size(); i++) {
			String nameColumn = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("NAME");
			String labelColumnCode = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("LABEL");
			String labelColumn = "";
			if (labelColumnCode != null) labelColumn = msgBuilder.getMessage(labelColumnCode, _bundle, httpRequest);
			else labelColumn = nameColumn;
			// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
			String align = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("horizontal-align");
			String orderButtons = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("order_buttons");
			boolean hideOrderButtons=false;
			if(orderButtons!=null && orderButtons.equalsIgnoreCase("false"))hideOrderButtons=true;

			if (align == null || align.trim().equals("")) align = "left";
			//defines order url for dynamic ordering

			HashMap orderParamsMap = new HashMap();
			orderParamsMap.putAll(_providerUrlMap);			
			orderParamsMap.put("FIELD_ORDER", nameColumn);
			orderParamsMap.put("TYPE_ORDER"," ASC");	
			if (columnFilter!= null && typeFilter != null && typeValueFilter!=null){	
				orderParamsMap.put("columnFilter", columnFilter);
				orderParamsMap.put("typeFilter", typeFilter);
				orderParamsMap.put("typeValueFilter", typeValueFilter);
				orderParamsMap.put("valueFilter", valueFilter);
			}
			String orderUrlAsc = createUrl(orderParamsMap);
			orderUrlAsc =  StringEscapeUtils.escapeHtml(orderUrlAsc);
			
			orderParamsMap.remove("TYPE_ORDER");
			orderParamsMap.put("TYPE_ORDER"," DESC");

			List _makeTitleButton=(List)_titleButton.elementAt(i);

			//orderParamsMap.put("MESSAGEDET",SpagoBIConstants.MESSAGE_ORDER_JOB_LIST);

			String orderUrlDesc = createUrl(orderParamsMap);
			orderUrlDesc =  StringEscapeUtils.escapeHtml(orderUrlDesc);
			
			_htmlStream.append("<TD class='portlet-section-header' style='vertical-align:middle;text-align:" + align + ";'  >" );			
			_htmlStream.append(   labelColumn);						
			if(!hideOrderButtons){
			if (!nameColumn.equalsIgnoreCase("INSTANCES")){
				_htmlStream.append("	<A href=\""+orderUrlAsc+"\">\n");
				_htmlStream.append("		<img  src='"+urlBuilder.getResourceLinkByTheme(httpRequest,"/img/commons/ArrowUp.gif",currTheme)+"'/>\n");
				_htmlStream.append("	</A>\n");
				_htmlStream.append("	<A href=\""+orderUrlDesc+"\">\n");
				_htmlStream.append("		<img  src='"+urlBuilder.getResourceLinkByTheme(httpRequest,"/img/commons/ArrowDown.gif",currTheme)+"'/>\n");
				_htmlStream.append("	</A>\n");
			}
			}
			
			if(_makeTitleButton.size()>0){

				_htmlStream.append(				makeTitleButton(_makeTitleButton) + "\n");

			}

			_htmlStream.append("</TD>\n");
		} 
		for(int i=0; i<numCaps; i++) {
			_htmlStream.append("<TD class='portlet-section-header' style='text-align:center'>&nbsp;</TD>\n");
		} 
		_htmlStream.append("</TR>\n");
	} 





	/**
	 * Builds Table list rows, reading all query information.
	 * 
	 * @throws JspException If any Exception occurs.
	 */


	protected void makeRows() throws JspException 
	{
		List rows = _content.getAttributeAsList("PAGED_LIST.ROWS.ROW");

		//gets the eventual map for the checklist
		Map subreportMap = new HashMap();
		for(int i = 0; i < rows.size(); i++) {
			SourceBean subreport = (SourceBean)rows.get(i);
			Integer id = (Integer)subreport.getAttribute("SUBREPORT_ID");
			if(id!=null) {
				logger.debug("ListTag::makeRows:request: SUBREPORT_ID = " + id);
				subreportMap.put(id.toString(), id);
			}

		}

		// js function for item action confirm
		_htmlStream.append(" <script>\n");
		_htmlStream.append("	function actionConfirm(message, url, functionToEval){\n");
		_htmlStream.append("		if (confirm(message + '?')){\n");
		_htmlStream.append("			if (functionToEval) eval(functionToEval);\n");
		_htmlStream.append("			location.href = url;\n");
		_htmlStream.append("		}\n");
		_htmlStream.append("	}\n");
		_htmlStream.append(" </script>\n");


		int prog=0;

		for(int i = 0; i < rows.size(); i++) 
		{
			prog++ ;
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

				if(field.equalsIgnoreCase("&nbsp;")){
					_htmlStream.append(" <td>" + field + "</td>\n");					
				}
				else
				{
					_htmlStream.append(" <td>" + StringEscapeUtils.escapeHtml(field) + "</td>\n");
				}
			} 

			SourceBean captionsSB = (SourceBean) _layout.getAttribute("CAPTIONS");
			List captions = captionsSB.getContainedSourceBeanAttributes();
			Iterator iter = captions.iterator();

			while (iter.hasNext()) {

				SourceBeanAttribute captionSBA = (SourceBeanAttribute)iter.next();
				SourceBean captionSB = (SourceBean)captionSBA.getValue();
				String captionName = captionSB.getName();
				SourceBean conditionsSB = (SourceBean) captionSB.getAttribute("CONDITIONS");
				boolean conditionsVerified = verifyConditions(conditionsSB, row);

				//verifies if it's a checklist
				String checklist = (String)captionSB.getAttribute("checkList");
				boolean isChecklist = false ;
				if (checklist!=null && checklist.equalsIgnoreCase("true")) isChecklist=true;

				//gets the parameters for the pop up window
				String popupStr = (String)captionSB.getAttribute("popup");
				String popupWidth = (String)captionSB.getAttribute("popupW");
				String popupHeight = (String)captionSB.getAttribute("popupH");
				String popupCloseRefresh = (String)captionSB.getAttribute("popupCandR");
				String popupSaveStr = (String)captionSB.getAttribute("popupSave");
				String popupSaveFunction = (String)captionSB.getAttribute("popupSaveFunc");

				boolean popup=false;
				boolean popupSave = false;
				boolean closeRefresh = false;
				if (popupStr!=null && popupStr.equalsIgnoreCase("true")) popup=true;
				if (popupSaveStr!=null && popupSaveStr.equalsIgnoreCase("true")) popupSave=true;
				if (popupCloseRefresh!=null && popupCloseRefresh.equalsIgnoreCase("true")) closeRefresh=true;

				if ( !conditionsVerified) {
					// if conditions are not verified puts an empty column
					_htmlStream.append(" <td width='40px'  >&nbsp;</td>\n");
					continue;
				}


				// onclick function
				SourceBean onClickSB = (SourceBean) captionSB.getAttribute("ONCLICK");
				String onClickFunction = readOnClickFunction(onClickSB, row);
				String onClickFunctionName = onClickSB != null ? captionSB.getName() + i + requestIdentity : null;
				if (onClickFunction != null) {
					_htmlStream.append("	<script type='text/javascript'>\n");
					_htmlStream.append("	function " + onClickFunctionName + "() {\n");
					_htmlStream.append(onClickFunction + "\n");
					_htmlStream.append("	}\n");
					_htmlStream.append("	</script>\n");
				}


				List parameters = captionSB.getAttributeAsList("PARAMETER");
				if (parameters == null || parameters.size() == 0) {
					// creates a checklist
					if (isChecklist){

						// gets the value of the current row and puts it as id of the input type checkbox
						SourceBean rowVal = (SourceBean) captionSB.getAttribute("ROWVALUE");
						String rowValue = readOnClickFunction(rowVal, row);

						_htmlStream.append(" <td width='20'>\n");

						if (onClickFunctionName != null) {								
							_htmlStream.append("<input onclick='" + onClickFunctionName + "()' type='checkbox' id='" + rowValue + "' name='checkbox:" + rowValue + "'>");
						}else{
							_htmlStream.append("<input type='checkbox'  id='" + rowValue + "' name='checkbox:" + rowValue + "' >");	
						}

						_htmlStream.append(" </td>\n");

						// sets the js function that controls if the actual row has already been checked and if yes checks it
						SourceBean clicked = (SourceBean) captionSB.getAttribute("CLICKED");
						String clickedFunction = readOnClickFunction(clicked, row);

						if (clickedFunction != null) {
							_htmlStream.append("	<script type='text/javascript'>\n");
							//_htmlStream.append("	function check" + rowValue + "() {\n");
							_htmlStream.append(         clickedFunction + "\n");
							//_htmlStream.append("	}\n");
							//_htmlStream.append("	check" + rowValue + "() ;\n");
							_htmlStream.append("	</script>\n");
						}

					}else{
						// if there are no parameters puts an empty column
						String img = (String)captionSB.getAttribute("image");
						String labelCode = (String)captionSB.getAttribute("label");
						String label = msgBuilder.getMessage(labelCode, _bundle, httpRequest);					 
						_htmlStream.append(" <td width='40px'>\n");
						_htmlStream.append(" 	<a name=\""+label+"\" " + (onClickFunctionName != null ? " href='javascript:void(0);' onclick='" + onClickFunctionName + "()' " : "") + " >\n");
						_htmlStream.append(" 		<img title=\""+label+"\" alt=\""+label+"\" src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' />\n");
						_htmlStream.append(" 	</a>\n");
						_htmlStream.append(" </td>\n");
					}

				} else {
					HashMap paramsMap = getParametersMap(parameters, row);
					String img = (String)captionSB.getAttribute("image");
					String labelCode = (String)captionSB.getAttribute("label");
					//String label = PortletUtilities.getMessage(labelCode, "messages");
					String label = msgBuilder.getMessage(labelCode, _bundle, httpRequest);
					String buttonUrl = null;
					if (!paramsMap.isEmpty()){
						buttonUrl = createUrl(paramsMap);
					}

					boolean confirm = false;
					// If caption's 'confirm' attribute is true, then all rows will have the confirmation alert
					// with message code that is specified in the 'label' attribute of the caption tag);
					// if there is also a CONFIRM_CONDITION tag, then the alert message code will be overwritten by 
					// 'msg' attribute of CONFIRM_CONDITION tag (with the bundle specified in 'bundle' attribute).
					// If caption's 'confirm' attribute is false, only rows that satisfy CONFIRM_CONDITION will have 
					// the confirmation alert with 'msg' attribute of CONFIRM_CONDITION tag as alert message.
					if (captionSB.getAttribute("confirm") != null &&
							((String)captionSB.getAttribute("confirm")).equalsIgnoreCase("TRUE")){
						confirm = true;
					}
					_htmlStream.append(" <td width='40px' >\n");
					String msg = label;
					SourceBean confirmConditionSB = (SourceBean) captionSB.getAttribute("CONFIRM_CONDITION");
					if (confirmConditionSB != null) {
						if (verifyConditions(confirmConditionSB, row)) {
							String msgCode = (String) confirmConditionSB.getAttribute("msg");
							String bundle = (String) confirmConditionSB.getAttribute("bundle");
							if (bundle == null || bundle.trim().equals("")) bundle = _bundle;
							msg = msgBuilder.getMessage(msgCode, bundle, httpRequest);
							confirm = true;
						}
					}


					if (confirm && buttonUrl!=null){

						if (onClickFunctionName != null) {
							_htmlStream.append("     <a href='javascript:actionConfirm(\"" + msg + "\", \"" + buttonUrl+ "\", '" + onClickFunctionName + "()');'>\n");
						} else 			
							if (popup){

								_htmlStream.append("     <a id='linkDetail_"+captionName+"_"+prog+"' >\n");
								// insert javascript for open popup
								_htmlStream.append(" <script>\n");
								_htmlStream.append("Ext.get('linkDetail_"+captionName+"_"+prog+"').on('click', function(){ \n");
								_htmlStream.append("  if (confirm(\"" + msg + "\") and win"+captionName+"_"+prog+" == null ) {\n");
								_htmlStream.append("   var win"+captionName+"_"+prog+"; \n");
								_htmlStream.append("   win"+captionName+"_"+prog+"=new Ext.Window({id:'win"+captionName+"_"+prog+"',\n");
								_htmlStream.append("            bodyCfg:{");
								_htmlStream.append("                tag:'div'");
								_htmlStream.append("                ,cls:'x-panel-body'");
								_htmlStream.append("               ,children:[{");
								_htmlStream.append("                    tag:'iframe',");
								_htmlStream.append("                    name: 'dynamicIframe"+captionName+"_"+prog+"',");
								_htmlStream.append("                    id  : 'dynamicIframe"+captionName+"_"+prog+"',");
								_htmlStream.append("                    src: '" +createUrl_popup(paramsMap)+ "',");
								_htmlStream.append("                    frameBorder:0,");
								_htmlStream.append("                    width:'100%',");
								_htmlStream.append("                    height:'100%',");
								_htmlStream.append("                    style: {overflow:'auto'}   ");        
								_htmlStream.append("               }]");
								_htmlStream.append("            },");
								_htmlStream.append("            modal: true,\n");
								_htmlStream.append("            layout:'fit',\n");
								if (popupHeight!=null) {  _htmlStream.append("           height:"+popupHeight+",\n");}
								else {  _htmlStream.append("            height:200,\n");}
								if (popupWidth!=null) {  _htmlStream.append("            width:"+popupWidth+",\n");}
								else {  _htmlStream.append("            width:500,\n");}
								_htmlStream.append("            closeAction:'hide',\n");
								if(closeRefresh==true){ _htmlStream.append("            closable : false ,\n");}

								_htmlStream.append("            scripts: true, \n");

								if(closeRefresh==true || popupSave==true) {
									_htmlStream.append("            buttons: [ \n");
									if(popupSave==true){
										_htmlStream.append("          { text: 'Save', \n");
										_htmlStream.append("    	   handler: function(){ \n"); 
										_htmlStream.append("           		dynamicIframe"+captionName+"_"+prog+"."+popupSaveFunction+"(); \n");
										_htmlStream.append("              } } "); 
									}
									if(closeRefresh==true && popupSave==true)  _htmlStream.append(","); 
									if(closeRefresh==true){
										_htmlStream.append("          {text: 'Close', \n");
										_htmlStream.append("           handler: function(){ \n"); 
										_htmlStream.append("            	refresh(); \n");
										_htmlStream.append("             	win"+captionName+"_"+prog+".hide(); \n"); 
										_htmlStream.append("              }} \n"); 
									}
									_htmlStream.append("           ], \n");
								}
								_htmlStream.append(" buttonAlign : 'left',\n");
								_htmlStream.append("            plain: true \n");
								_htmlStream.append("        });\n");
								_htmlStream.append("    };\n");
								_htmlStream.append("   win"+captionName+"_"+prog+".show() \n");
								_htmlStream.append("  }\n");
								_htmlStream.append(");\n");
								_htmlStream.append(" </script>\n");						
							}else{
								_htmlStream.append("     <a href='javascript:actionConfirm(\"" + msg + "\", \"" + buttonUrl+ "\");'>\n");
							}
					}else{
						if (popup){
							_htmlStream.append("     <a id='linkDetail_"+captionName+"_"+prog+"' >\n");
							// insert javascript for open popup
							_htmlStream.append(" <script>\n");
							_htmlStream.append("   var win"+captionName+"_"+prog+"; \n");
							_htmlStream.append("Ext.get('linkDetail_"+captionName+"_"+prog+"').on('click', function(){ \n");

							_htmlStream.append("   if ( win"+captionName+"_"+prog+" == null ) {win"+captionName+"_"+prog+"=new Ext.Window({id:'win"+captionName+"_"+prog+"',\n");
							_htmlStream.append("            bodyCfg:{ \n" );
							_htmlStream.append("                tag:'div' \n");
							_htmlStream.append("                ,cls:'x-panel-body' \n");
							_htmlStream.append("               ,children:[{ \n");
							_htmlStream.append("                    tag:'iframe', \n");
							_htmlStream.append("                    name: 'dynamicIframe"+captionName+"_"+prog+"', \n");
							_htmlStream.append("                    id  : 'dynamicIframe"+captionName+"_"+prog+"', \n");
							_htmlStream.append("                    src: '" +createUrl_popup(paramsMap)+ "', \n");
							_htmlStream.append("                    frameBorder:0, \n");
							_htmlStream.append("                    width:'100%', \n");
							_htmlStream.append("                    height:'100%', \n");
							_htmlStream.append("                    style: {overflow:'auto'}  \n ");        
							_htmlStream.append("               }] \n");
							_htmlStream.append("            }, \n");
							_htmlStream.append("            modal: true,\n");
							_htmlStream.append("            layout:'fit',\n");
							if (popupHeight!=null) {  _htmlStream.append("           height:"+popupHeight+",\n");}
							else {  _htmlStream.append("            height:200,\n");}
							if (popupWidth!=null) {  _htmlStream.append("            width:"+popupWidth+",\n");}
							else {  _htmlStream.append("            width:500,\n");}

							_htmlStream.append("            closeAction:'hide',\n");
							if(closeRefresh==true){ _htmlStream.append("            closable : false ,\n");}

							_htmlStream.append("            scripts: true, \n");

							if(closeRefresh==true || popupSave==true) {
								_htmlStream.append("            buttons: [ \n");
								if(popupSave==true){
									_htmlStream.append("          { text: 'Save', \n");
									_htmlStream.append("    	   handler: function(){ \n"); 
									_htmlStream.append("           		dynamicIframe"+captionName+"_"+prog+"."+popupSaveFunction+"(); \n");
									_htmlStream.append("              } } "); 
								}
								if(closeRefresh==true && popupSave==true)  _htmlStream.append(","); 
								if(closeRefresh==true){
									_htmlStream.append("          {text: 'Close', \n");
									_htmlStream.append("           handler: function(){ \n"); 
									_htmlStream.append("            	refresh(); \n");
									_htmlStream.append("             	win"+captionName+"_"+prog+".hide(); \n"); 
									_htmlStream.append("              }} \n"); 
								}
								_htmlStream.append("           ], \n");
							}
							_htmlStream.append(" buttonAlign : 'left',\n");
							_htmlStream.append("            plain: true \n");

							_htmlStream.append("        }); }; \n");
							_htmlStream.append("   win"+captionName+"_"+prog+".show(); \n");
							_htmlStream.append("	} \n");
							_htmlStream.append(");\n");
							_htmlStream.append(" </script>\n");

						}else{ 
							if(buttonUrl!=null) {
								if (onClickFunctionName != null) {
									_htmlStream.append("     <a href='javascript:" + onClickFunctionName + "();location.href=\"" + buttonUrl + "\"'>\n");
								} else {
									_htmlStream.append("     <a href='"+buttonUrl+"'>\n");
								}
							}
						}

					}
					_htmlStream.append("			<img title=\""+label+"\" alt=\""+label+"\" src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' />\n");
					_htmlStream.append("     </a>\n");
					_htmlStream.append(" </td>\n");
				}
			}
			_htmlStream.append(" </tr>\n");
		}

		_htmlStream.append(" </table>\n");
		_htmlStream.append(" <script>\n");
		_htmlStream.append("        function refresh(){ \n");
		_htmlStream.append("  			location.href = '"+_refreshUrl+"' ; \n" );
		_htmlStream.append("        } \n");
		_htmlStream.append(" </script>\n");
	} 

	private String readOnClickFunction(SourceBean onClickSB, SourceBean row) {
		logger.debug("IN");
		String onClickFunction = onClickSB != null ? onClickSB.getCharacters() : null;
		if (onClickFunction != null) {
			int startIndex = onClickFunction.indexOf("<PARAMETER ");
			while (startIndex != -1) {
				int endIndex = onClickFunction.indexOf("/>", startIndex);
				String parameterSBStr = onClickFunction.substring(startIndex, endIndex + 2);
				try {
					SourceBean parameterSB = SourceBean.fromXMLString(parameterSBStr);
					String parameterName = (String) parameterSB.getAttribute("NAME");
					String parameterScope = (String) parameterSB.getAttribute("SCOPE");
					String inParameterValue = null;
					Object parameterValueObject = null;
					if (parameterScope != null && parameterScope.equalsIgnoreCase("LOCAL")) {
						parameterValueObject = row.getAttribute(parameterName);
					} else {
						parameterValueObject = ContextScooping.getScopedParameter(_requestContainer,
								_responseContainer, parameterName, parameterScope);
					}
					if (parameterValueObject != null) {
						inParameterValue = parameterValueObject.toString();
						onClickFunction = onClickFunction.substring(0, startIndex) + inParameterValue + onClickFunction.substring(endIndex + 2);
					}
				} catch (SourceBeanException e) {
					logger.error(e);
				}
				startIndex = onClickFunction.indexOf("<PARAMETER ", startIndex + 1);
			}
		}
		logger.debug("OUT");
		return onClickFunction;
	}


	protected boolean verifyConditions (SourceBean conditionsSB, SourceBean row) throws JspException {
		boolean conditionVerified = true;
		if (conditionsSB != null) {
			List conditions = conditionsSB.getAttributeAsList("PARAMETER");
			if (conditions != null && conditions.size() > 0) {
				for (int j = 0; j < conditions.size(); j++) {
					SourceBean condition = (SourceBean) conditions.get(j);
					String parameterName = (String) condition.getAttribute("NAME");
					String parameterScope = (String) condition.getAttribute("SCOPE");
					String parameterValue = (String) condition.getAttribute("VALUE");
					String functionality = (String) condition.getAttribute("user_functionality");
					String inParameterValue = null;
					Object parameterValueObject = null;

					if (functionality != null && !functionality.equalsIgnoreCase("")) {

						try {
							if (!profile.isAbleToExecuteAction(functionality)){
								conditionVerified = false;
								break;
							}
							else {
								continue;
							}

						} catch (EMFInternalError e) {
							e.printStackTrace();
							logger.error(e);
						} 
					}


					if (parameterScope != null && parameterScope.equalsIgnoreCase("LOCAL")) {
						if (row == null) {
							logger.error("Impossible to associate LOCAL scope: the row is null");
							throw new JspException("Impossible to associate LOCAL scope: the row is null");
						} // if (row == null)
						parameterValueObject = row.getAttribute(parameterName);
					} else {
						parameterValueObject = ContextScooping
						.getScopedParameter(_requestContainer,
								_responseContainer, parameterName,
								parameterScope);
					}
					if (parameterValueObject != null)
						inParameterValue = parameterValueObject.toString();
					if (parameterValue.equalsIgnoreCase("AF_DEFINED")) {
						if (inParameterValue == null) {
							conditionVerified = false;
							break;
						} // if (inParameterValue == null)
						continue;
					} // if (parameterValue.equalsIgnoreCase("AF_DEFINED"))
					if (parameterValue.equalsIgnoreCase("AF_NOT_DEFINED")) {
						if (inParameterValue != null) {
							conditionVerified = false;
							break;
						} // if (inParameterValue != null)
						continue;
					} // if (parameterValue.equalsIgnoreCase("AF_NOT_DEFINED"))

					String operator = (String) condition.getAttribute("OPERATOR");
					if (operator == null || operator.trim().equals("")) operator = "EQUAL_TO";
					else operator = operator.trim();
					if (operator.equalsIgnoreCase("EQUAL_TO")) {
						if (!(parameterValue.equalsIgnoreCase(inParameterValue))) {
							conditionVerified = false;
							break;
						} // if (!(parameterValue.equalsIgnoreCase(inParameterValue)))
					}// if (operator.equalsIgnoreCase("EQUAL_TO"))
					if (operator.equalsIgnoreCase("GREATER_THAN")) {
						double parameterValueDouble = Double.parseDouble(parameterValue);
						double inParameterValueDouble = Double.parseDouble(inParameterValue);
						if (!(inParameterValueDouble > parameterValueDouble)) {
							conditionVerified = false;
							break;
						} // if (!(inParameterValueDouble > parameterValueDouble))
					}// if (operator.equalsIgnoreCase("GREATER_THAN"))					
					if (operator.equalsIgnoreCase("GREATER_OR_EQUAL_THAN")) {
						double parameterValueDouble = Double.parseDouble(parameterValue);
						double inParameterValueDouble = Double.parseDouble(inParameterValue);
						if (!(inParameterValueDouble >= parameterValueDouble)) {
							conditionVerified = false;
							break;
						} // if (!(inParameterValueDouble >= parameterValueDouble))
					}// if (operator.equalsIgnoreCase("GREATER_OR_EQUAL_THAN"))						
					if (operator.equalsIgnoreCase("LESS_THAN")) {
						double parameterValueDouble = Double.parseDouble(parameterValue);
						double inParameterValueDouble = Double.parseDouble(inParameterValue);
						if (!(inParameterValueDouble > parameterValueDouble)) {
							conditionVerified = false;
							break;
						} // if (!(inParameterValueDouble < parameterValueDouble))
					}// if (operator.equalsIgnoreCase("LESS_THAN"))	
					if (operator.equalsIgnoreCase("LESS_OR_EQUAL_THAN")) {
						double parameterValueDouble = Double.parseDouble(parameterValue);
						double inParameterValueDouble = Double.parseDouble(inParameterValue);
						if (!(inParameterValueDouble <= parameterValueDouble)) {
							conditionVerified = false;
							break;
						} // if (!(inParameterValueDouble <= parameterValueDouble))
					}// if (operator.equalsIgnoreCase("LESS_OR_EQUAL_THAN"))	
				} 
			}
		}
		return conditionVerified;
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

		_htmlStream.append(" <TABLE CELLPADDING=0 CELLSPACING=0  WIDTH='100%' BORDER=0>\n");
		_htmlStream.append("	<TR>\n");
		//_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='14'>\n");

		// create link for previous page		
		HashMap prevParamsMap = new HashMap();
		prevParamsMap.putAll(_providerUrlMap);
		prevParamsMap.put("MESSAGE", "LIST_PAGE");
		prevParamsMap.put("LIST_PAGE", String.valueOf(prevPage));		
		_prevUrl = createUrl(prevParamsMap);	
		//_prevUrl = response.encodeURL(_prevUrl);
		_prevUrl=StringEscapeUtils.escapeHtml(_prevUrl);
		

		// create url for refresh page		
		HashMap refreshParamsMap = new HashMap();
		refreshParamsMap.putAll(_providerUrlMap);
		refreshParamsMap.put("MESSAGE", "LIST_PAGE");
		refreshParamsMap.put("LIST_PAGE", String.valueOf(pageNumber));
		_refreshUrl = createUrl(refreshParamsMap);
		_refreshUrl = _refreshUrl.replaceAll("&amp;", "&");
		//_refreshUrl = response.encodeURL(_refreshUrl);
		_refreshUrl=StringEscapeUtils.escapeHtml(_refreshUrl);

		// create link for next page
		HashMap nextParamsMap = new HashMap();
		nextParamsMap.putAll(_providerUrlMap);
		nextParamsMap.put("MESSAGE", "LIST_PAGE");
		nextParamsMap.put("LIST_PAGE", String.valueOf(nextPage));
		_nextUrl = createUrl(nextParamsMap);
		//_nextUrl = response.encodeURL(_nextUrl);
		_nextUrl=StringEscapeUtils.escapeHtml(_nextUrl);

		// create link for first page
		HashMap firstParamsMap = new HashMap();
		firstParamsMap.putAll(_providerUrlMap);
		firstParamsMap.put("MESSAGE", "LIST_PAGE");
		firstParamsMap.put("LIST_PAGE","1");
		_firstUrl = createUrl(firstParamsMap);
		//_firstUrl = response.encodeURL(_firstUrl);
		_firstUrl=StringEscapeUtils.escapeHtml(_firstUrl);

		// create link for last page
		HashMap lastParamsMap = new HashMap();
		lastParamsMap.putAll(_providerUrlMap);
		lastParamsMap.put("MESSAGE", "LIST_PAGE");
		lastParamsMap.put("LIST_PAGE", String.valueOf(pagesNumber));
		_lastUrl = createUrl(lastParamsMap);
		//_lastUrl = response.encodeURL(_lastUrl);
		_lastUrl=StringEscapeUtils.escapeHtml(_lastUrl);

		String formId = "formFilter" + requestIdentity;

		String valueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.VALUE_FILTER);
		String typeValueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
		String columnFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.COLUMN_FILTER);
		String typeFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_FILTER);
		if (valueFilter != null && columnFilter != null && typeFilter != null) {
			prevParamsMap.put(SpagoBIConstants.VALUE_FILTER, valueFilter);
			prevParamsMap.put(SpagoBIConstants.TYPE_VALUE_FILTER, typeValueFilter);
			prevParamsMap.put(SpagoBIConstants.COLUMN_FILTER, columnFilter);
			prevParamsMap.put(SpagoBIConstants.TYPE_FILTER, typeFilter);
			_prevUrl = createUrl(prevParamsMap);
			//_prevUrl = response.encodeURL(_prevUrl);
			_prevUrl=StringEscapeUtils.escapeHtml(_prevUrl);

			nextParamsMap.put(SpagoBIConstants.VALUE_FILTER, valueFilter);
			nextParamsMap.put(SpagoBIConstants.TYPE_VALUE_FILTER, typeValueFilter);
			nextParamsMap.put(SpagoBIConstants.COLUMN_FILTER, columnFilter);
			nextParamsMap.put(SpagoBIConstants.TYPE_FILTER , typeFilter);
			_nextUrl = createUrl(nextParamsMap);
			//_nextUrl = response.encodeURL(_nextUrl);
			_nextUrl=StringEscapeUtils.escapeHtml(_nextUrl);

			firstParamsMap.put(SpagoBIConstants.VALUE_FILTER, valueFilter);
			firstParamsMap.put(SpagoBIConstants.TYPE_VALUE_FILTER, typeValueFilter);
			firstParamsMap.put(SpagoBIConstants.COLUMN_FILTER, columnFilter);
			firstParamsMap.put(SpagoBIConstants.TYPE_FILTER , typeFilter);
			_firstUrl = createUrl(firstParamsMap);
			//_firstUrl = response.encodeURL(_firstUrl);
			_firstUrl=StringEscapeUtils.escapeHtml(_firstUrl);

			lastParamsMap.put(SpagoBIConstants.VALUE_FILTER, valueFilter);
			lastParamsMap.put(SpagoBIConstants.TYPE_VALUE_FILTER, typeValueFilter);
			lastParamsMap.put(SpagoBIConstants.COLUMN_FILTER, columnFilter);
			lastParamsMap.put(SpagoBIConstants.TYPE_FILTER , typeFilter);
			_lastUrl = createUrl(lastParamsMap);
			//_lastUrl = response.encodeURL(_lastUrl);
			_lastUrl=StringEscapeUtils.escapeHtml(_lastUrl);
		} else {
			valueFilter = "";
			typeValueFilter = "";
			columnFilter = "";
			typeFilter = "";
		}

		if(pageNumber != 1) {
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<A href=\""+_firstUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0></a>\n");
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  align='left' width='1%'>\n");
			_htmlStream.append("			<A href=\""+_prevUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0></a>\n");
			_htmlStream.append("		</TD>\n");
		} else {
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0 />\n");				
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme)+"' ALIGN=RIGHT border=0 />\n");
			_htmlStream.append("		</TD>\n");			
		}		
		//_htmlStream.append("		</TD>\n");


		// Form for list filtering; if not specified, the filter is enabled
		_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='middle' width='80%'>\n");
		if (_filter == null || _filter.equalsIgnoreCase("enabled")) {


			/*HashMap allUrlMap = (HashMap) _providerUrlMap.clone();
			allUrlMap.remove("valueFilter");
			allUrlMap.remove("columnFilter");
			allUrlMap.remove("typeFilter");
			allUrlMap.remove("typeValueFilter");*/

			String allUrl = createUrl(_providerUrlMap);
			String filterURL = createUrl(_providerUrlMap);
			filterURL=StringEscapeUtils.escapeHtml(filterURL);

			String label =  msgBuilder.getMessage("SBIListLookPage.labelFilter", "messages", httpRequest);
			String labelTypeValueFilter =  msgBuilder.getMessage("SBIListLookPage.labelTypeValueFilter", "messages", httpRequest);
			String labelNumber = msgBuilder.getMessage("SBIListLookPage.labelNumber", "messages", httpRequest);
			String labelString =  msgBuilder.getMessage("SBIListLookPage.labelString", "messages", httpRequest);
			String labelDate =  msgBuilder.getMessage("SBIListLookPage.labelDate", "messages", httpRequest);
			String labelStart =  msgBuilder.getMessage("SBIListLookPage.startWith", "messages", httpRequest);
			String labelEnd =  msgBuilder.getMessage("SBIListLookPage.endWith", "messages", httpRequest);
			String labelContain =  msgBuilder.getMessage("SBIListLookPage.contains", "messages", httpRequest);
			String labelEqual =  msgBuilder.getMessage("SBIListLookPage.isEquals", "messages", httpRequest);
			String labelIsLessThan =  msgBuilder.getMessage("SBIListLookPage.isLessThan", "messages", httpRequest);
			String labelIsLessOrEqualThan =  msgBuilder.getMessage("SBIListLookPage.isLessOrEqualThan", "messages", httpRequest);
			String labelIsGreaterThan =  msgBuilder.getMessage("SBIListLookPage.isGreaterThan", "messages", httpRequest);
			String labelIsGreaterOrEqualThan =  msgBuilder.getMessage("SBIListLookPage.isGreaterOrEqualThan", "messages", httpRequest);
			String labelFilter =  msgBuilder.getMessage("SBIListLookPage.filter", "messages", httpRequest);
			String labelAll =  msgBuilder.getMessage("SBIListLookPage.all", "messages", httpRequest);

			//_htmlStream.append("						    <br/><br/>\n");
			_htmlStream.append("						    <form action='"+filterURL+"' id='" + formId +"' method='post'>\n");
			_htmlStream.append("						    "+label+"\n");
			_htmlStream.append("						    <select name='" + SpagoBIConstants.COLUMN_FILTER + "'>\n");

			for (int i = 0; i < _columns.size(); i++) {
				String nameColumn = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("NAME");
				String labelColumnCode = (String) ((SourceBean) _columns.elementAt(i)).getAttribute("LABEL");
				String labelColumn = new String(nameColumn);
				if (labelColumnCode != null) labelColumn =  msgBuilder.getMessage(labelColumnCode, _bundle, httpRequest);
				String selected = "";
				if (nameColumn.equalsIgnoreCase(columnFilter))
					selected = " selected='selected' "; 
				_htmlStream.append("						    	<option value='"+nameColumn+"' "+selected+" >"+labelColumn+"</option>\n");
			}
			String selected = "";
			_htmlStream.append("						    </select>\n");
			_htmlStream.append("						    "+labelTypeValueFilter+"\n");
			_htmlStream.append("						    <select name='" + SpagoBIConstants.TYPE_VALUE_FILTER + "'>\n");
			if (typeValueFilter.equalsIgnoreCase(SpagoBIConstants.STRING_TYPE_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.STRING_TYPE_FILTER +"' "+selected+" >"+labelString+"</option>\n");
			if (typeValueFilter.equalsIgnoreCase(SpagoBIConstants.NUMBER_TYPE_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.NUMBER_TYPE_FILTER +"' "+selected+" >"+labelNumber+"</option>\n");
			if (typeValueFilter.equalsIgnoreCase(SpagoBIConstants.DATE_TYPE_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.DATE_TYPE_FILTER +"' "+selected+" >"+labelDate+"</option>\n");
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
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.CONTAIN_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.CONTAIN_FILTER +"' "+selected+" >"+labelContain+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.EQUAL_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.EQUAL_FILTER +"' "+selected+" >"+labelEqual+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.LESS_FILTER +"' "+selected+" >"+labelIsLessThan+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.LESS_OR_EQUAL_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.LESS_OR_EQUAL_FILTER +"' "+selected+" >"+labelIsLessOrEqualThan+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.GREATER_FILTER +"' "+selected+" >"+labelIsGreaterThan+"</option>\n");
			if (typeFilter.equalsIgnoreCase(SpagoBIConstants.GREATER_OR_EQUAL_FILTER))
				selected = " selected='selected' ";
			else selected = "";
			_htmlStream.append("						    	<option value='"+SpagoBIConstants.GREATER_OR_EQUAL_FILTER +"' "+selected+" >"+labelIsGreaterOrEqualThan+"</option>\n");
			_htmlStream.append("						    </select>\n");
			_htmlStream.append("						    <input type=\"text\" name=\"" + SpagoBIConstants.VALUE_FILTER + "\" size=\"10\" value=\""+StringEscapeUtils.escapeHtml(valueFilter)+"\" /> \n");
			_htmlStream.append("						    <a href='javascript:document.getElementById(\"" + formId +"\").submit()'>"+StringEscapeUtils.escapeHtml(labelFilter)+"</a> \n");
			_htmlStream.append(" <a href='"+allUrl+"'>"+StringEscapeUtils.escapeHtml(labelAll)+"</a> \n");
			_htmlStream.append("						    </form> \n");

			// visualize any validation error present in the errorHandler
			boolean thereAreValidationErrors = false;
			StringBuffer errorsHtmlString = new StringBuffer("");
			if (_errorHandler != null) {
				Collection errors = _errorHandler.getErrors();
				if (errors != null && errors.size() > 0) {
					errorsHtmlString.append("	<div class='filter-list-errors'>\n");
					Iterator iterator = errors.iterator();
					EMFAbstractError error = null;
					String description = "";
					while (iterator.hasNext()) {
						error = (EMFAbstractError) iterator.next();
						if (error instanceof EMFValidationError) {
							description = error.getDescription();
							errorsHtmlString.append("		" + description + "<br/>\n");
							thereAreValidationErrors = true;
						}
					}
					errorsHtmlString.append("	</div>\n");
				}
			}
			if (thereAreValidationErrors) _htmlStream.append(errorsHtmlString);

		}
		_htmlStream.append("		</TD>\n");	
		// create link for next page
		//_htmlStream.append("		<TD class='portlet-section-footer' valign='center' align='right' width='14'>\n");				
		if(pageNumber != pagesNumber) {	
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<A href=\""+_nextUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0 /></a>\n");
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<A href=\""+_lastUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0 /></a>\n");
			_htmlStream.append("		</TD>\n");
		} else {
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0>\n");
			_htmlStream.append("		</TD>\n");
			_htmlStream.append("		<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme)+"' ALIGN=RIGHT border=0>\n");
			_htmlStream.append("		</TD>\n");
		}		
//		_htmlStream.append("		</TD>\n");
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
		String dotsStart = null;
		String dotsEnd = null;
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
		_htmlStream.append("		<TD class='portlet-section-footer' style='vertical-align:top;horizontal-align:left;width:30%;'>\n");
		_htmlStream.append("				<font class='aindice'>&nbsp;"+pageLabel+ " " + pageNumber + " " +pageOfLabel+ " " + pagesNumber + "&nbsp;</font>\n");
		//_htmlStream.append("			    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n");
		_htmlStream.append("		</TD>\n");
		//_htmlStream.append("		<TD  class='portlet-section-footer' width='23%'>\n");
		//_htmlStream.append("			    &nbsp;\n");
		//_htmlStream.append("		</TD>\n");	
		_htmlStream.append("		<TD  class='portlet-section-footer' style='vertical-align:top;horizontal-align:center;width:40%;'>\n");
		// visualize navigation's icons
		if(pageNumber != 1) {
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<A href=\""+_firstUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme)+"' border=0></a>\n");
			//_htmlStream.append("	</TD>\n");
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  align='left' width='1%'>\n");
			_htmlStream.append("			<A href=\""+_prevUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme)+"' border=0></a>\n");
			//_htmlStream.append("	</TD>\n");
		} else {
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2leftarrow.png",currTheme)+"' border=0 />\n");				
			//_htmlStream.append("	</TD>\n");
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center' align='left' width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1leftarrow.png",currTheme)+"' border=0 />\n");
			//_htmlStream.append("	</TD>\n");			
		}
		//_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='20%'>\n");
		if (dotsStart != null) {
			_htmlStream.append("<A style='vertical-align:top;'>"+dotsStart+"</a>\n");
			_htmlStream.append("&nbsp;&nbsp;\n");
		}
		for (int i=startRangePages; i <= endRangePages; i++){
			// create link for last page
			HashMap tmpParamsMap = new HashMap();
			tmpParamsMap.putAll(_providerUrlMap);
			tmpParamsMap.put("MESSAGE", "LIST_PAGE");
			tmpParamsMap.put("LIST_PAGE", String.valueOf(i));
			String tmpUrl = createUrl(tmpParamsMap);

			String ORDER = (String) _serviceRequest.getAttribute("ORDER");
			String FIELD_ORDER = (String) _serviceRequest.getAttribute("FIELD_ORDER");
			if (FIELD_ORDER!= null && ORDER != null){
				tmpParamsMap.put("FIELD_ORDER", FIELD_ORDER);
				tmpParamsMap.put("ORDER", ORDER);
			}

			String valueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.VALUE_FILTER);
			String typeValueFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
			String columnFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.COLUMN_FILTER);
			String typeFilter = (String) _serviceRequest.getAttribute(SpagoBIConstants.TYPE_FILTER);
			if (valueFilter != null && columnFilter != null && typeFilter != null) {
				tmpParamsMap.put(SpagoBIConstants.VALUE_FILTER, valueFilter);
				tmpParamsMap.put(SpagoBIConstants.TYPE_VALUE_FILTER, typeValueFilter);
				tmpParamsMap.put(SpagoBIConstants.COLUMN_FILTER, columnFilter);
				tmpParamsMap.put(SpagoBIConstants.TYPE_FILTER, typeFilter);
				tmpUrl = createUrl(tmpParamsMap);
			}			
			tmpUrl=StringEscapeUtils.escapeHtml(tmpUrl);
			_htmlStream.append("	<A style='vertical-align:top;' href=\""+tmpUrl+"\">"+String.valueOf(i)+ "</a>\n");
			_htmlStream.append("&nbsp;&nbsp;\n");			
		}
		if (dotsEnd != null) {
			_htmlStream.append("<A style='vertical-align:top;'>"+dotsEnd+"</a>\n");
		}
		//_htmlStream.append("	</TD>\n");
		if(pageNumber != pagesNumber) {	
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<A href=\""+_nextUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme)+"' border=0 /></a>\n");
			//_htmlStream.append("	</TD>\n");
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<A href=\""+_lastUrl+"\"><IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme)+"' border=0 /></a>\n");
			//_htmlStream.append("	</TD>\n");
		} else {
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/1rightarrow.png",currTheme)+"' border=0>\n");
			//_htmlStream.append("	</TD>\n");
			//_htmlStream.append("	<TD class='portlet-section-footer' valign='center'  width='1%'>\n");
			_htmlStream.append("			<IMG src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/commons/2rightarrow.png",currTheme)+"' border=0>\n");
			//_htmlStream.append("	</TD>\n");
		}
		_htmlStream.append("		</TD>\n");
		_htmlStream.append("		<TD class='portlet-section-footer' style='width:30%;'>\n");
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

			String buttonName = buttonSB.getName();
			SourceBean conditionsSB = (SourceBean) buttonSB.getAttribute("CONDITIONS");
			SourceBean row;
			try {
				row = new SourceBean("ROWS");
				boolean conditionsVerified = verifyConditions(conditionsSB, row);

				if(ChannelUtilities.isWebRunning()){
					String onlyPort = (String)buttonSB.getAttribute("onlyPortletRunning");
					if( (onlyPort!=null) && onlyPort.equalsIgnoreCase("true"))  { 
						continue;
					}
				}
				if ( !conditionsVerified) {
					// if conditions are not verified puts an empty column
					_htmlStream.append(" <td class='header-button-column-portlet-section' width='40px'>&nbsp;</td>\n");
					continue;
				}

				List parameters = buttonSB.getAttributeAsList("PARAMETER");
				HashMap paramsMap = getParametersMap(parameters, null);

				String img = (String) buttonSB.getAttribute("image");
				String labelCode = (String) buttonSB.getAttribute("label");

				//String label = PortletUtilities.getMessage(labelCode, "messages");
				String label = msgBuilder.getMessage(labelCode, _bundle, httpRequest);
				String buttonUrl = createUrl(paramsMap);
				buttonUrl = StringEscapeUtils.escapeHtml(buttonUrl);

				htmlStream.append("<td class=\"header-button-column-portlet-section\">\n");
				htmlStream.append("<a href='"+buttonUrl+"'><img class=\"header-button-image-portlet-section\" title='" + label + "' alt='" + label + "' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' /></a>\n");
				htmlStream.append("</td>\n");
			} catch (SourceBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return htmlStream;
	} 


	/**
	 * Starting from the module <code>buttonsSB</code> object, 
	 * creates all buttons for the jsp list. These buttons are next to colums names 
	 * @param buttons The list of the buttons 
	 * 
	 * @throws JspException If any exception occurs.
	 */

	protected StringBuffer makeTitleButton(List buttons) throws JspException {

		StringBuffer htmlStream = new StringBuffer();
		try{
			Iterator iter = buttons.listIterator();
			boolean buttonYes=false;
			if(iter.hasNext()){buttonYes=true;
			htmlStream.append("<td class=\"portlet-section-header\" style=\"width: 32px;\">\n");

			}


			while (iter.hasNext()) {
				SourceBeanAttribute buttonSBA = (SourceBeanAttribute)iter.next();
				SourceBean buttonSB = (SourceBean)buttonSBA.getValue();

				String buttonName = buttonSB.getName();

				SourceBean conditionsSB = (SourceBean) buttonSB.getAttribute("CONDITIONS");
				SourceBean row;
				row = new SourceBean("ROWS");
				boolean conditionsVerified = verifyConditions(conditionsSB, row);

				if(ChannelUtilities.isWebRunning()){
					String onlyPort = (String)buttonSB.getAttribute("onlyPortletRunning");
					if( (onlyPort!=null) && onlyPort.equalsIgnoreCase("true"))  { 
						continue;
					}
				}
				if ( !conditionsVerified) {
					// if conditions are not verified puts an empty column
					_htmlStream.append(" <td class='header-button-column-portlet-section' width='40px'>&nbsp;</td>\n");
					continue;
				}

				List parameters = buttonSB.getAttributeAsList("PARAMETER");
				HashMap paramsMap = getParametersMap(parameters, null);

				String img = (String) buttonSB.getAttribute("image");
				String labelCode = (String) buttonSB.getAttribute("label");

				//String label = PortletUtilities.getMessage(labelCode, "messages");
				String label = msgBuilder.getMessage(labelCode, _bundle, httpRequest);
				//String buttonUrl = createUrl(paramsMap);


				// if there is some javascript: onclick function
				SourceBean onClickSB = (SourceBean) buttonSB.getAttribute("ONCLICK");

				boolean onClick=false;	
				if(onClickSB!=null) onClick=true;

				if(onClick==true){
					String onClickFunction = onClickSB != null ? onClickSB.getCharacters() : null;

					String functionName = onClickSB != null ? buttonSB.getName() : "";

					String onClickFunctionName = onClickSB != null ? buttonSB.getName() + requestIdentity : null;

					if (onClickFunction != null) {
						_htmlStream.append("	<script type='text/javascript'>\n");
						_htmlStream.append("	function " + onClickFunctionName + "() {\n");
						_htmlStream.append(onClickFunction + "\n");
						_htmlStream.append("	}\n");
						_htmlStream.append("	</script>\n");
					}

					//String immagine=urlBuilder.getResourceLink(httpRequest, img);
					//htmlStream.append("<td class=\"portlet-section-header\">\n");
					//htmlStream.append("<td class=\"header-button-column-portlet-section\">\n");
					htmlStream.append("<div style=\"float: left;\">");
					htmlStream.append("<a href='javascript:"+onClickFunctionName+"()'><img class=\"header-button-image-portlet-section\" title=\""+label+"\" alt=\""+label+"\" style='height: 95%;width: 95%;' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' /></a>\n");
					htmlStream.append("</div>");
					//htmlStream.append("</td>\n");

				}

				if(onClick!=true){
					String buttonUrl = createUrl(paramsMap);

					htmlStream.append("<td class=\"header-button-column-portlet-section\">\n");
					htmlStream.append("<a href='"+buttonUrl+"'><img class=\"header-button-image-portlet-section\" title=\"" + label + "\" alt=\"" + label + "\" src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' /></a>\n");
					htmlStream.append("</td>\n");}

			}
			if(buttonYes)htmlStream.append("</td>\n");
		}
		catch (SourceBeanException e) {
			logger.error("Error");
			e.printStackTrace();
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
							logger.error("Impossible to associate local scope to the button");
							throw new JspException("Impossible to associate local scope to the button");
						} // if (row == null)
						Object valueObject = row.getAttribute(value);
						if (valueObject != null)
							value = valueObject.toString();
						else value = "";
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
		paramsMap.put("TYPE_LIST", "TYPE_LIST");
		String url = urlBuilder.getUrl(httpRequest, paramsMap);
		return url;
	}
	protected String createUrl_popup(HashMap paramsMap) {

		String url = GeneralUtilities.getSpagoBIProfileBaseUrl(((UserProfile)profile).getUserUniqueIdentifier().toString());
		paramsMap.put("TYPE_LIST", "TYPE_LIST");
		if (paramsMap != null){
			Iterator keysIt = paramsMap.keySet().iterator();
			String paramName = null;
			Object paramValue = null;
			while (keysIt.hasNext()){
				paramName = (String)keysIt.next();
				paramValue = paramsMap.get(paramName); 
				url += "&"+paramName+"="+paramValue.toString();
			}
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
	 * Traces the setting of a bundle name.
	 * 
	 * @param bundle The bundle name string at input.
	 */

	public void setBundle(String bundle) {
		TracerSingleton.log(
				Constants.NOME_MODULO,
				TracerSingleton.INFORMATION,
				"ListTag::setBundle:: bundle [" + bundle + "]");
		_bundle = bundle;
	} // public void setBundle(String bundle)

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

	/**
	 * For exception cases adds into parameters list the attributes necessary for the correct management of list. 
	 * 
	 * @param providerUrlMap The map whit parameters. 
	 * @param serviceRequest The serviceRequest sourcebean.
	 */
	private HashMap updateUrlForExceptions(HashMap providerUrlMap, SourceBean serviceRequest) { 

		providerUrlMap.put("MESSAGEDET",SpagoBIConstants.MESSAGE_ORDER_LIST);
		for (int i=0; i<EXCEPTION_ATTRIBUTES.length; i++){
			if (serviceRequest.getAttribute(EXCEPTION_ATTRIBUTES[i]) != null ){
				String value =(String)serviceRequest.getAttribute(EXCEPTION_ATTRIBUTES[i]);
				providerUrlMap.put(EXCEPTION_ATTRIBUTES[i], value);
			}				
		}
		return providerUrlMap;
	}
} // public class ListTag extends TagSupport






