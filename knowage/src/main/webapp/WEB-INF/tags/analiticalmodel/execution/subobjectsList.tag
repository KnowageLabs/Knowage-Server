<%@tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tlds/spagobi.tld" prefix="spagobi" %>

<%@attribute name="subobjectsList" required="true" type="java.util.List"%>
<%@attribute name="uuid" required="true" type="java.lang.String"%>

<%@tag import="java.util.Iterator"%>
<%@tag import="it.eng.spagobi.analiticalmodel.document.bo.SubObject"%>
<%@tag import="it.eng.spago.base.RequestContainer"%>
<%@tag import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@tag import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@tag import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@tag import="it.eng.spago.security.IEngUserProfile"%>
<%@tag import="java.util.Map"%>
<%@tag import="java.util.HashMap"%>
<%@tag import="it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule"%>
<%@tag import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@tag import="it.eng.spago.navigation.LightNavigationManager"%>
<%@tag import="java.util.Date"%>
<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@tag import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@tag import="it.eng.spago.base.SessionContainer"%>
<%@tag import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@tag import="it.eng.spago.util.StringUtils"%>
<%@tag import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@tag import="org.apache.commons.lang.StringEscapeUtils"%>


<%
RequestContainer requestContainer = ChannelUtilities.getRequestContainer(request);
IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder(requestContainer.getChannelType());
IEngUserProfile profile = (IEngUserProfile) requestContainer.getSessionContainer().getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
SessionContainer permSess=requestContainer.getSessionContainer().getPermanentContainer();
Map baseDeleteSubObjUrlPars = new HashMap();
//baseDeleteSubObjUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
baseDeleteSubObjUrlPars.put("ACTION_NAME", SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);
baseDeleteSubObjUrlPars.put(SpagoBIConstants.MESSAGEDET, "DELETE_SUBOBJECT");
baseDeleteSubObjUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");

Map deleteSubObjUrlPars = new HashMap();
deleteSubObjUrlPars.putAll(baseDeleteSubObjUrlPars);
String deleteSubObjUrl = urlBuilder.getUrl(request, deleteSubObjUrlPars);

//Set Theme
String currTheme=ThemesManager.getCurrentTheme(requestContainer);
if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();


//if (subobjectsList == null || subobjectsList.size() == 0) {
	%>
	<%--<div class='portlet-font'><spagobi:message key="SBIDev.docConf.subBIObject.nosubobjects"/></div> --%>
	<%
//} else {
	%>
<form method='POST' action='<%= deleteSubObjUrl %>' id='subobjectsForm<%= uuid %>' name='subobjectsForm<%= uuid %>'>
		<table style='width:100%;' align='left' id="subObjectTable_<%= uuid %>">
			<thead>
				<tr>
					<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
					  	<spagobi:message key="SBIDev.docConf.subBIObject.name"/>
					</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
				   		<spagobi:message key="SBIDev.docConf.subBIObject.owner"/>
					</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
						<spagobi:message key="SBIDev.docConf.subBIObject.description"/>
					</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
						<spagobi:message key="SBIDev.docConf.subBIObject.creationDate"/>
					</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
						<spagobi:message key="SBIDev.docConf.subBIObject.lastModificationDate"/>
					</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td style='vertical-align:middle;' align='left' class='portlet-section-header'>
						<spagobi:message key="SBIDev.docConf.subBIObject.visibility"/>
					</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td align='left' class='portlet-section-header'>&nbsp;</td>
					<td align='left' class='portlet-section-header'>
						<img 
       						src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/expertok.gif", currTheme) %>' 
       						name='selectDeselectAllImg' alt='<spagobi:message key="SBIDev.docConf.subBIObject.selectAll"/>' 
       						title='<spagobi:message key="SBIDev.docConf.subBIObject.selectAll"/>' 
       						onClick="selectDeselectAllSubobjects<%= uuid %>();" />
						<img 
	       					src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/analiticalmodel/ico_delete.gif", currTheme) %>' 
	       					alt='<spagobi:message key="SBIDev.docConf.ListdocDetParam.deleteCaption"/>' 
	       					title='<spagobi:message key="SBIDev.docConf.ListdocDetParam.deleteCaption"/>' 
	       					onClick="deleteSubobjects<%= uuid %>();" />
	       					
       				<script>
       					function getSubobjects<%= uuid %>() {
       						var toReturn = document.subobjectsForm<%= uuid %>.<%= SpagoBIConstants.SUBOBJECT_ID %>;
       						if (toReturn != null && !toReturn.length) {
       							var temp = new Array();
       							temp.push(toReturn);
       							toReturn = temp;
       						}
       						return toReturn;
       					}
       				
	       				function deleteSubobjects<%= uuid %>() {
							checks = getSubobjects<%= uuid %>();
							atLeastOneSelected = false;
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (check.checked) {
									atLeastOneSelected = true;
									break;
								}
							}
							if (!atLeastOneSelected) {
								alert('<spagobi:message key="SBIDev.docConf.subBIObject.noSubBIObjectSelected" />');
								return;
							}
							var conf = confirm('<spagobi:message key="ConfirmMessages.DeleteSubObject" />');
							if (conf) {
								document.getElementById('subobjectsForm<%= uuid %>').submit();
							}
						}
						
						selectableSubobjects<%= uuid %> = new Array();
						<%
						if (subobjectsList != null && subobjectsList.size() > 0) {
							Iterator iterSubs =  subobjectsList.iterator();
							while(iterSubs.hasNext()) {
								SubObject subObj = (SubObject)iterSubs.next();
								if (subObj.getOwner().equals(((UserProfile)profile).getUserId().toString())
										|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
									%>
									selectableSubobjects<%= uuid %>.push(<%= subObj.getId() %>);
									<%
								}
							}
						}
						%>
						
						selectedSubobjects<%= uuid %> = new Array();
						
						function selectDeselectAllSubobjects<%= uuid %>() {
							if (selectedSubobjects<%= uuid %>.length == 0) {
								selectAllSubobjects<%= uuid %>();
							} else if (selectedSubobjects<%= uuid %>.length == selectableSubobjects<%= uuid %>.length) {
								deselectAllSubobjects<%= uuid %>();
							} else {
								selectAllSubobjects<%= uuid %>();
							}
						}
						
						function selectAllSubobjects<%= uuid %>() {
							checks = getSubobjects<%= uuid %>();
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (!check.checked) {
									check.click();
								}
							}
						}
						
						function deselectAllSubobjects<%= uuid %>() {
							checks = getSubobjects<%= uuid %>();
							for (var i = 0; i < checks.length; i++) {
								check = checks[i];
								if (check.checked) {
									check.click();
								}
							}
						}
					</script>
					
					</td>
				</tr>
			</thead>
			<tboby>
	<%
	SubObject subObj = null;
	Integer idSub = null;
	String nameSub = null;
	String descr = null;
	String visib = null;
	String delete = null;
	String owner = null;
	String creationDate = null;
	String lastModificationDate = null;
	String execSubObjUrl = null;
	boolean alternate = false;
	String rowClass = null;
	
    Map baseExecSubObjUrlPars = new HashMap();
    //baseExecSubObjUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE );
	baseExecSubObjUrlPars.put("ACTION_NAME", SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);  
    baseExecSubObjUrlPars.put(SpagoBIConstants.MESSAGEDET, "EXEC_SUBOBJECT");
    baseExecSubObjUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED,"true");
    
    String dateFormat=GeneralUtilities.getLocaleDateFormat(permSess)+" hh:mm:ss";
    
	if (subobjectsList != null && subobjectsList.size() > 0) {
		Iterator iterSubs =  subobjectsList.iterator();
		while(iterSubs.hasNext()) {
			subObj = (SubObject)iterSubs.next();
			rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
			alternate = !alternate;
			idSub = subObj.getId();
			nameSub = subObj.getName();
	        descr = subObj.getDescription();
	        owner = subObj.getOwner();
	        
	        Date creationDateD = subObj.getCreationDate();
			creationDate = StringUtils.dateToString(creationDateD, dateFormat);

	        Date lastModificationDateD=subObj.getLastChangeDate();
	        lastModificationDate = StringUtils.dateToString(lastModificationDateD, dateFormat);
	                    
	        visib = "Private";
	        if (subObj.getIsPublic().booleanValue()) {
	        	visib = "Public";
	        } 
	        //if (owner.equals(profile.getUserUniqueIdentifier().toString())) {
	        if (owner.equals(((UserProfile)profile).getUserId().toString())) {
	        	delete = "delete";
	        }
	        
		    Map execSubObjUrlPars = new HashMap();
		    execSubObjUrlPars.putAll(baseExecSubObjUrlPars);
		    execSubObjUrlPars.put(SpagoBIConstants.SUBOBJECT_ID, idSub);
		    execSubObjUrl = urlBuilder.getUrl(request, execSubObjUrlPars);
		    
	        %>
					<tr class='portlet-font'>
	       		    	<td style='vertical-align:middle;' class='<%= rowClass %>'>
	       		    		<%= nameSub %>
	           			</td>
	           			<td class='<%= rowClass %>' width='20px'>&nbsp;</td> 
	           			<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(owner) %></td>
	           			<td class='<%= rowClass %>' width='20px'>&nbsp;</td> 
	           			<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(descr) %></td>
	           			<td class='<%= rowClass %>' width='20px'>&nbsp;</td> 
	           			<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(creationDate) %></td>
	           			<td class='<%= rowClass %>' width='20px'>&nbsp;</td> 
	           			<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(lastModificationDate) %></td>
	           			<td class='<%= rowClass %>' width='20px'>&nbsp;</td> 
	           			<td style='vertical-align:middle;' class='<%= rowClass %>' ><%= StringEscapeUtils.escapeHtml(visib) %></td>
	           			<td class='<%= rowClass %>' width='20px'>&nbsp;</td>
	           			<td style='vertical-align:middle;' class='<%= rowClass %>' width='40px'>
	           				<a href='<%= execSubObjUrl %>'>
	           					<img 
	  	   							src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/exec.gif", currTheme) %>' 
	  	   							name='execSub' 
	  	            				alt='<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt" />' 
	                				title='<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt" />' />
	           				</a>
	           			</td>
	           			<%
	           			if (owner.equals(((UserProfile)profile).getUserId().toString())
	           				|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
	           			%>
	                   		<td style='vertical-align:middle;text-align:center;' class='<%= rowClass %>' width='40px'>
                   				<input type="checkbox" name="<%= SpagoBIConstants.SUBOBJECT_ID %>" id="<%= SpagoBIConstants.SUBOBJECT_ID %>"
				   					value="<%= idSub %>" 
				   					onClick="if (this.checked) {selectedSubobjects<%= uuid %>.push(this.value);} else {selectedSubobjects<%= uuid %>.removeFirst(this.value);}"/>
	               			</td>
	               		<%
	           			} else {
	           			%>
	           				<td style='vertical-align:middle;' class='<%= rowClass %>' width='40px'>
	           					&nbsp;
	           				</td>
	           			<%
	           			}
	           			%>
	           		</tr>
	     	<%
	    }
	}
    %>
    		</tboby>
		</table>
	</form>
	
	<br/>
	<br/>
	<br/>
	<br/>
	<br/>
	
	<%
	String baseDeleteSubObjUrl = urlBuilder.getUrl(request, baseDeleteSubObjUrlPars);
	String baseExecSubObjUrl = urlBuilder.getUrl(request, baseExecSubObjUrlPars);
	%>
	
	<script type="text/javascript">
	
	var subobject;
	
	function addSubobject(windowName, response) {
		stringToEval = "subobject = " + response + ";";
		var uuid = windowName.substr('iframeexec'.length);
		eval(stringToEval);
		var table = document.getElementById('subObjectTable_' + uuid);
		var rowsNumber = table.rows.length;
		//if (rowsNumber == 1) {
		//	var subObjectsArrow = document.getElementById('subobjectsSliderArrow' + uuid);
		//	var subObjectsPopout = document.getElementById('popout_SubObject' + uuid);
		//	if (subObjectsArrow) {
		//		subObjectsArrow.style.display = "inline";
		//	}
		//	if (subObjectsPopout) {
		//		subObjectsPopout.style.display = "inline";
		//	}
		//}
		var newRow = table.insertRow(rowsNumber);
		var nameCell = newRow.insertCell(0);
		var empty1 = newRow.insertCell(1);
		var ownerCell = newRow.insertCell(2);
		var empty3 = newRow.insertCell(3);
		var descrCell = newRow.insertCell(4);
		var empty5 = newRow.insertCell(5);
		var creationDateCell = newRow.insertCell(6);
		var empty7 = newRow.insertCell(7);
		var lastModificationDateCell = newRow.insertCell(8);
		var empty9 = newRow.insertCell(9);
		var visib = newRow.insertCell(10);
		var empty11 = newRow.insertCell(11);
		var execCell = newRow.insertCell(12);
		var deleteCell = newRow.insertCell(13);
		var cellClass = 'portlet-section-alternate';
		newRow.className = 'portlet-font';
		nameCell.innerHTML = subobject.name;
		nameCell.className = cellClass;
		ownerCell.innerHTML = subobject.owner;
		ownerCell.className = cellClass;
		descrCell.innerHTML = subobject.description;
		descrCell.className = cellClass;
		creationDateCell.innerHTML = subobject.creationDate;
		creationDateCell.className = cellClass;
		lastModificationDateCell.innerHTML = subobject.lastModificationDate;
		lastModificationDateCell.className = cellClass;
		if (subobject.isPublic) {
			visib.innerHTML = "Public";
		} else {
			visib.innerHTML = "Private";
		}
		visib.className = cellClass;
		
		deleteCell.innerHTML = '<input type="checkbox" name="<%= SpagoBIConstants.SUBOBJECT_ID %>" id="<%= SpagoBIConstants.SUBOBJECT_ID %>" value="' + subobject.id + '" onClick="if (this.checked) {selectedSubobjects' + uuid + '.push(this.value);} else {selectedSubobjects' + uuid + '.removeFirst(this.value);}"/>';
		deleteCell.className = cellClass;
		deleteCell.style.width = '40px';
		deleteCell.style.textAlign = 'center';
		execCell.innerHTML = '<a href="<%= baseExecSubObjUrl.toString() %>&<%= SpagoBIConstants.SUBOBJECT_ID %>=' + subobject.id + '" ><img src="<%= urlBuilder.getResourceLinkByTheme(request, "/img/exec.gif", currTheme) %>"  name="execSub" alt="<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt" />" title="<spagobi:message key="SBIDev.docConf.execBIObjectParams.execButt" />" /></a>';
		execCell.className = cellClass;
		execCell.style.width = '40px';
		
		empty1.className = cellClass;
		empty3.className = cellClass;
		empty5.className = cellClass;
		empty7.className = cellClass;
		empty9.className = cellClass;
		empty11.className = cellClass;
		
		eval('selectableSubobjects' + uuid + '.push(' + subobject.id + ')');
	}
	
	function loadSubObject(windowName, subObjId) {
		var params;
		Ext.Ajax.request({
			url: '<%= GeneralUtilities.getSpagoBIProfileBaseUrl((String) profile.getUserUniqueIdentifier()) %>&ACTION_NAME=GET_SUBOBJECT_INFO&<%=SpagoBIConstants.SUBOBJECT_ID%>=' + subObjId + '&DATE_FORMAT=<%= dateFormat %>&<%= LightNavigationManager.LIGHT_NAVIGATOR_DISABLED %>=TRUE',
			method: 'get',
			success: function (result, request) {
				response = result.responseText || "";
				addSubobject(windowName, response);
			},
			params: params,
			failure: somethingWentWrong
		});
	}
	
	function somethingWentWrong() {
		alert('Something went wrong during ajax call');
	}
	</script>
	
    <%
//}
%>