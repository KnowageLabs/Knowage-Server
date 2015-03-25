<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="java.util.List,
		 		 java.util.Iterator,
		 		 java.util.Map,
				 java.util.HashMap,
		 		 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse,
		 		 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse,
                 it.eng.spago.navigation.LightNavigationManager,
                 it.eng.spagobi.commons.constants.SpagoBIConstants,
                 it.eng.spagobi.commons.constants.AdmintoolsConstants,
                 it.eng.spagobi.commons.constants.ObjectsTreeConstants,
                 it.eng.spagobi.commons.dao.DAOFactory,
                 it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO,
                 it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue,
                 it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail,
                 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter,
                 it.eng.spagobi.behaviouralmodel.lov.handlers.LovManager" %>
                 
<%
	SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("ListObjParuseModule");
	BIObjectParameter biParam = (BIObjectParameter) moduleResponse.getAttribute("biParameter");
	List allParuses = (List) moduleResponse.getAttribute("allParuses");
	List biParamCorrelations = (List) moduleResponse.getAttribute("biParamCorrelation");
	List otherBiParameters = (List) moduleResponse.getAttribute("otherBiParameters");
	List biParamViews = (List) moduleResponse.getAttribute("biParamViews");
	
	
	
	Map formUrlPars = new HashMap();
	formUrlPars.put("PAGE", "ListObjParusePage");
	formUrlPars.put("MESSAGEDET", AdmintoolsConstants.DETAIL_MOD);
	formUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");   
	String formUrl = urlBuilder.getUrl(request, formUrlPars);
	   
	Map backUrlPars = new HashMap();
	backUrlPars.put("PAGE", "ListObjParusePage");
	backUrlPars.put("MESSAGEDET", "EXIT_FROM_MODULE");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");   
	String backUrl = urlBuilder.getUrl(request, backUrlPars);

	String correlationsObjParuseUrl=urlBuilder.getResourceLink(request, "js/analiticalmodel/document/correlationsObjParuse.js");
	String viewsObjParuseUrl=urlBuilder.getResourceLink(request, "js/analiticalmodel/document/viewsObjParuse.js");
%>


<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview"%><script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
    <script type="text/javascript" src="<%=correlationsObjParuseUrl%>"></script>
    <script type="text/javascript" src="<%=viewsObjParuseUrl%>"></script>
    
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>



<script>

// QUI



var correlationManager = new correlationManagerObj(); 

var viewManager = new viewManagerObj(); 

function rigenerateCorrelationList() {
  var divBlockContainer = document.getElementById('divCorrelationsList');
	divBlockContainer.innerHTML == '';
	var html = "";
	html += "<table style='margin:1px solid gray;'>";
	if(correlationManager.correlations.length>0) {
  	for(i=0; i<correlationManager.correlations.length; i++) {
      var correl = correlationManager.correlations[i];
      html += "<tr  height='25' style='background:rgb(251,247,227);'>";
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='100px'>";
      var value = correl.preCond;
      html += "<a style='text-decoration:none;' href='javascript:addLeftBreak("+i+")'>";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/plus10.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.addLeftBreak" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.addLeftBreak" />' "; 
      html += "/></a>";
      html += "<a style='text-decoration:none;' href='javascript:removeLeftBreak("+i+")'>";
      html += "&nbsp;";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/minus10.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.removeLeftBreak" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.removeLeftBreak" />' "; 
      html += "/></a>";
      html += "&nbsp;&nbsp;&nbsp;&nbsp;";
      html += "<span id='visiblePrecondition"+i+"'>"+value+"</span>";
      html += "<input id='hiddenPrecondition"+i+"' value='"+value+"' type='hidden' />";
      html += "</td>";
      
      
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;'>";
      html += "<span style='font-size:10pt;'><b>" + correl.nameCondition + "</b> the value of the parameter <b>" + correl.nameParFather + "</b></span>";     
      html += "</td>";
     
     
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='100px'>";
      value = correl.postCond;
      html += "<a style='text-decoration:none;' href='javascript:addRightBreak("+i+")'>";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/plus10.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.addRightBreak" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.addRightBreak" />' "; 
      html += "/></a>";
      html += "<a style='text-decoration:none;' href='javascript:removeRightBreak("+i+")'>";
      html += "&nbsp;";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/minus10.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.removeRightBreak" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.removeRightBreak" />' "; 
      html += "/></a>";
      html += "&nbsp;&nbsp;&nbsp;&nbsp;";
      html += "<span id='visiblePostcondition"+i+"'>"+value+"</span>";
      html += "<input id='hiddenPostcondition"+i+"' value='"+value+"' type='hidden' />";
      html += "</td>"; 
     
      
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='100px'>";
      html += "<select onchange='correlationManager.setLogicOperator("+i+", this.options[this.selectedIndex].value)'>";
      html += "<option value=''></option>";
      var selLogOp = " ";
      if(correl.logicOper=='AND') {
        selLogOp = " selected ";
      }
      html += "<option "+selLogOp+" value='AND'>AND</option>";
      selLogOp = " ";
      if(correl.logicOper=='OR') {
        selLogOp = " selected ";
      }
      html += "<option "+selLogOp+" value='OR'>OR</option>";
      html += "</select>";
      html += "</td>";
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='30px'>";
      html += "<a href='javascript:viewCorrelation("+i+")'>";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.viewCondition" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.viewCondition" />' "; 
      html += "/></a>";
      html += "</td>";
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='30px'>";
      html += "<a href='javascript:deleteCorrelation("+i+")'>";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/erase.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.deleteCondition" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.deleteCondition" />' "; 
      html += "/></a>";
      html += "</td>";
      html += "</tr>";
    }
  } else {
     html += "<tr><td colspan='5'>&nbsp;</td></tr>";  
     html += "<tr><td colspan='5'><b>No correlation is currently set</b></td></tr>";  
     html += "<tr style='border-bottom:1px solid #bbb;'><td colspan='5'>&nbsp;</td></tr>";    
  }
  html += "</table>";
  divBlockContainer.innerHTML = html;
}

  
  
  function addLeftBreak(index) {
      var hidPreCond = document.getElementById("hiddenPrecondition"+index);
      var visPreCond = document.getElementById("visiblePrecondition"+index);
      var precond = hidPreCond.value;
      if(precond==null) {
        precond = "(";
      } else {
        precond = precond + "(";
      }
      hidPreCond.value = precond;
      visPreCond.innerHTML = precond;
      correlationManager.setPreCondition(index, precond);
  }	  

  function removeLeftBreak(index) {
      var hidPreCond = document.getElementById("hiddenPrecondition"+index);
      var visPreCond = document.getElementById("visiblePrecondition"+index);
      var precond = hidPreCond.value;
      if(precond==null) {
        return;
      }
      if(precond.length > 0) {
        precond = precond.substring(0, precond.length-1);
      } else {
        return;
      }
      hidPreCond.value = precond;
      visPreCond.innerHTML = precond;
      correlationManager.setPreCondition(index, precond);
  }	  
  
function addRightBreak(index) {
      var hidPostCond = document.getElementById("hiddenPostcondition"+index);
      var visPostCond = document.getElementById("visiblePostcondition"+index);
      var postcond = hidPostCond.value;
      if(postcond==null) {
        postcond = ")";
      } else {
        postcond = postcond + ")";
      }
      hidPostCond.value = postcond;
      visPostCond.innerHTML = postcond;
      correlationManager.setPostCondition(index, postcond);
  }	  

  function removeRightBreak(index) {
      var hidPostCond = document.getElementById("hiddenPostcondition"+index);
      var visPostCond = document.getElementById("visiblePostcondition"+index);
      var postcond = hidPostCond.value;
      if(postcond==null) {
        return;
      }
      if(postcond.length > 0) {
        postcond = postcond.substring(0, postcond.length-1);
      } else {
        return;
      }
      hidPostCond.value = postcond;
      visPostCond.innerHTML = postcond;
      correlationManager.setPostCondition(index, postcond);
  }	  	  
  
  
  
function generateCorrBlockHtml(indexCorr) {
	var correlation = null;
    if(indexCorr!=null) {
       correlation = correlationManager.getCorrelation(indexCorr);
    } 
    blockHtml = "";
	blockHtml += "<br/>";
	blockHtml += "<div class='div_detail_area_forms_objParuse'>";
	blockHtml += "<table>";
	blockHtml += "	<tr>";
	blockHtml += "		<td width='50%' style='color:#074B88;font-weight:bold;'>";
	blockHtml += "		 	<span style='font-size:10pt;'><spagobi:message key = "SBIDev.listObjParuses.dependsFrom" args="<%=biParam.getLabel()%>"/></span>";
	blockHtml += "		</td>";
	blockHtml += "		<td>";
	blockHtml += "			<select style='width:150px' id='dependSelect' name='dependFrom'>";
	<%
		for(int i=0; i<otherBiParameters.size(); i++) {
			BIObjectParameter otherBiParameter = (BIObjectParameter) otherBiParameters.get(i);
	%>
	selBiParam = " "; 
	if(correlation!=null) {
    if(correlation.idParFather==<%=otherBiParameter.getId()%>) {
        selBiParam  = " selected ";
    }
}
	blockHtml += "  			<option value='<%=otherBiParameter.getId()%>' "+selBiParam+" >";
	blockHtml += "    				<%=otherBiParameter.getLabel()%>";
	blockHtml += "  			</option>";
	<%
	   }
	%>
	blockHtml += "			</select>";
	blockHtml += "		</td>";
	blockHtml += "	</tr>";
	blockHtml += "	<tr>";
	blockHtml += "		<td width='50%' >";
	blockHtml += "		  <br/>";
	blockHtml += "			<span style='font-size:10pt;color:#074B88;font-weight:bold;'><spagobi:message key="SBIDev.listObjParuses.filterOperator" /></span>";
	blockHtml += "			<span style='font-size:8pt;color:#074B88;'><spagobi:message key="SBIDev.listObjParuses.filterOperatorExplanation" args="<%=biParam.getLabel()%>" /></span>";
	blockHtml += "		</td>";
	blockHtml += "		<td>";
	//blockHtml += "		  <br/>";
	blockHtml += "			<select style='width:150px' id='conditionSelect' name='condition'>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.START_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.START_FILTER%>' "+selLogOper+" >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.startWith" />";
	blockHtml += "  </option>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.END_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.END_FILTER%>' "+selLogOper+"  >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.endWith" />";
	blockHtml += "  </option>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.CONTAIN_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.CONTAIN_FILTER%>' "+selLogOper+"  >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.contains" />";
	blockHtml += "  </option>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.EQUAL_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.EQUAL_FILTER%>' "+selLogOper+"   >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.isEquals" />";
	blockHtml += "  </option>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.LESS_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.LESS_FILTER%>' "+selLogOper+" >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.isLessThan" />";
	blockHtml += "  </option>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.LESS_OR_EQUAL_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.LESS_OR_EQUAL_FILTER%>' "+selLogOper+"  >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.isLessOrEqualThan" />";
	blockHtml += "  </option>";
	selLogOper = " ";
	if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.GREATER_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.GREATER_FILTER%>' "+selLogOper+"  >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.isGreaterThan" />";
	blockHtml += "  </option>";
	selLogOper = " ";
if(correlation!=null) {
    if(correlation.condition=='<%=SpagoBIConstants.GREATER_OR_EQUAL_FILTER%>') {
        selLogOper  = " selected ";
    }
}
	blockHtml += "  <option value='<%=SpagoBIConstants.GREATER_OR_EQUAL_FILTER%>' "+selLogOper+"   >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.isGreaterOrEqualThan" />";
	blockHtml += "  </option>";
	blockHtml += "</select></td></tr>";
	
	
	
	
	blockHtml += "</table>";
	blockHtml += "<br/>";
	blockHtml += "<span style='color:#074B88;font-weight:bold;'><spagobi:message key="SBIDev.listObjParuses.listParuses" args="<%=biParam.getLabel()%>" /></span>";
	blockHtml += "<hr/>";
	blockHtml += "<table style='border-spacing:5px;border-collapse:separate;'>";
	blockHtml += "<tr height='30px'>";
	blockHtml += "<td style='font-size:10pt;color:#074B88;' align='center'><spagobi:message key="SBIDev.listObjParuses.isActive" /></td>";
	blockHtml += "<td style='font-size:10pt;color:#074B88;' align='center'><spagobi:message key="SBIDev.listObjParuses.paruseName" /></td>";
	blockHtml += "<td style='font-size:10pt;color:#074B88;' align='center'><spagobi:message key="SBIDev.listObjParuses.paruseCols" /></td>";
	blockHtml += "</tr>";
	<%
	  Iterator itallpar = allParuses.iterator();
	  while(itallpar.hasNext()) {
		   ParameterUse paruse = (ParameterUse) itallpar.next();
	%>
	
	var parusesetting = null;
	if(correlation!=null) {
	  parusesetting = correlation.getParuseSetting(<%=paruse.getUseID()%>);
	}
	blockHtml += "  <tr><td align='center'>";
	checkActive = " ";
if(parusesetting!=null) {
    if(parusesetting.active) {
        checkActive  = " checked='true' ";
    }
}
	blockHtml += "    <input type='checkbox' "+checkActive+" id='corractive_<%=paruse.getUseID()%>' name='paruse_id' value='<%=paruse.getUseID()%>' />";
	blockHtml += "  </td>";
	blockHtml += "  <td align='center' class='portlet-font'>";
	blockHtml += "    <%=paruse.getLabel() + ": " + paruse.getDescription() %>";
	blockHtml += "  </td>";
	blockHtml += "  <td align='center'><select name='oncolumn' id='oncolumnSelect_<%=paruse.getUseID()%>' style='width:150px;'>";
	<%
	    IModalitiesValueDAO lovDAO = DAOFactory.getModalitiesValueDAO();
			ModalitiesValue lov = lovDAO.loadModalitiesValueByID(paruse.getIdLov());
			LovManager lovMan = new LovManager();
			List columnNames = lovMan.getAllColumnsNames(lov);
			Iterator iterColNames = columnNames.iterator();
			while(iterColNames.hasNext()){
			    String colName = (String)iterColNames.next();
	%>
	selColName = " ";
if(parusesetting!=null) {
    if(parusesetting.onColumn=='<%=colName%>') {
        selColName  = " selected ";
    }
}
	blockHtml += "		<option value='<%=colName%>' "+selColName+"  >";
	blockHtml += "		   	<%=colName%>";
	blockHtml += "		</option>";
	<%	
			}
	%>
	blockHtml += "  </select></td>";
	blockHtml += "  </tr>";
<%
	  }
	%>  
blockHtml += "</table>";
blockHtml += "<br/>";
if(indexCorr==null) {
	  blockHtml += "<center>";
	  blockHtml += " 	<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' onclick='closeWinCorr()' ";
	  blockHtml += " 	     title='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	     alt='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	/>";
	  blockHtml += "</center>";
} else {
  	  blockHtml += "<center>";
	  blockHtml += " 	<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' onclick='closeWinCorr("+indexCorr+")' ";
	  blockHtml += " 	     title='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	     alt='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	/>";
	  blockHtml += "</center>";
} 
blockHtml += "</div>";
return blockHtml;
}





function addCorrelation() {
   winCorr = new Window('win_correlation', {className: "alphacube", title: "<spagobi:message key = "SBIDev.listObjParuses.condition" />", width:700, height:300});
	   winCorr.setDestroyOnClose();
   winCorr.getContent().innerHTML=generateCorrBlockHtml();
   winCorr.showCenter(true);
}

function viewCorrelation(indexCorr) {
   winCorr = new Window('win_correlation', {className: "alphacube", title: "<spagobi:message key = "SBIDev.listObjParuses.condition" />", width:700, height:300});
	   winCorr.setDestroyOnClose();
   winCorr.getContent().innerHTML=generateCorrBlockHtml(indexCorr);
   winCorr.showCenter(true);
}

function deleteCorrelation(indexCorr) {
   correlationManager.deleteCorrelation(indexCorr);
   rigenerateCorrelationList();
}

function correlation(idPF, namePF, cond, nameCond, prSet) {
	this.idParFather = idPF;
	this.nameParFather = namePF;
	this.condition = cond; 
	this.nameCondition = nameCond; 
	this.paruseSettings = prSet;
	this.preCond = "";
	this.postCond = "";
	this.logicOper = "";
	
  function getParuseSettingFunct(paruseid) {
    for(i=0; i<this.paruseSettings.length; i++) {
      var paruseSetting = this.paruseSettings[i];
      if(paruseSetting.paruseId==paruseid){
        return paruseSetting;
      }
    }
  }
  
  this.getParuseSetting = getParuseSettingFunct;
  
}


function paruseSetting(act, puid, oncol){
	this.active = act;
	this.paruseId = puid;
	this.onColumn = oncol;
}


function closeWinCorr(indexCorr) {
    depSel = document.getElementById('dependSelect');
    conSel = document.getElementById('conditionSelect');
    valueDepSel = depSel.options[depSel.selectedIndex].value;
    nameDepSel = depSel.options[depSel.selectedIndex].text;
    valueConSel = conSel.options[conSel.selectedIndex].value;
    nameConSel = conSel.options[conSel.selectedIndex].text;
    var paruseSettings = new Array();
    <%
	      itallpar = allParuses.iterator();
	      int i = 0;
      while(itallpar.hasNext()) {
		       ParameterUse paruse = (ParameterUse) itallpar.next();
	    %>
	       checkactive = document.getElementById('corractive_<%=paruse.getUseID()%>');
	       selectoncolumn = document.getElementById('oncolumnSelect_<%=paruse.getUseID()%>');
	       valueOnColSel = selectoncolumn.options[selectoncolumn.selectedIndex].value;
	       puset = new paruseSetting(checkactive.checked, <%=paruse.getUseID()%>, valueOnColSel);
	       paruseSettings[<%=i%>] = puset;
	    <%
	        i++;
    }
	    %>
	    corr = null;
	  
	  if(indexCorr==null) {  
	    // check if the correlation already exists
	    if(correlationManager.correlationExist(valueDepSel, valueConSel)) {
      		alert('<spagobi:message key = "SBIDev.listObjParuses.correlationExist" />');
      		return;
    	}
      }
	    
	    if(indexCorr==null) {
      corr = new correlation(valueDepSel, nameDepSel, valueConSel, nameConSel, paruseSettings);
      correlationManager.addCorrelation(corr);
    } else {
       corr = correlationManager.getCorrelation(indexCorr);
       corr.idParFather = valueDepSel;
	     corr.nameParFather = nameDepSel;
	     corr.condition = valueConSel; 
	     corr.nameCondition = nameConSel; 
	     corr.paruseSettings = paruseSettings;
	     correlationManager.setCorrelation(indexCorr, corr);
    }
    Windows.closeAll();
    rigenerateCorrelationList();
} 



function generateCorrelationsXml() {
	var xml = '';
	xml += '<correlations>';
	if(correlationManager.correlations.length>0) {
  	for(i=0; i<correlationManager.correlations.length; i++) {
      var correl = correlationManager.correlations[i];
      xml += '<correlation ';
      xml += ' precond="'+correl.preCond+'" ';
      xml += ' postcond="'+correl.postCond+'" ';
      xml += ' logicoperator="'+correl.logicOper+'" ';
      xml += ' idparameterfather="'+correl.idParFather+'" ';
      xml += ' condition="'+correl.condition+'" ';
      xml += '>';
      xml += '<parusesettings>';
      var parSetts = correl.paruseSettings;
      for(j=0; j<parSetts.length; j++) {
        parSett =  parSetts[j];
        if(parSett.active){
            xml += '<parusesetting ';
            xml += ' active="'+parSett.active+'" ';
            xml += ' paruseid="'+parSett.paruseId+'" ';
            xml += ' oncolumn="'+parSett.onColumn+'" ';
            xml += '/>';
        }
      }
      xml += '</parusesettings>';
      xml += '</correlation>';
    }
  }
  xml += '</correlations>';
  return xml;
}





function getParNameFromParId(parId){
  <%
    Iterator othBiParIter = otherBiParameters.iterator(); 
    while(othBiParIter.hasNext()) {
    	BIObjectParameter othBiPar = (BIObjectParameter) othBiParIter.next();
  %>
  	if(parId==<%=othBiPar.getId()%>) {
  		return "<%=othBiPar.getLabel()%>";
  	}
  <%  
    }
  %>
}

function getFilterOpNameFromCode(filterOpCode) {
	if(filterOpCode=='<%=SpagoBIConstants.START_FILTER%>') {
      return '<spagobi:message key = "SBIListLookPage.startWith" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.END_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.endWith" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.CONTAIN_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.contains" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.EQUAL_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.isEquals" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.LESS_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.isLessThan" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.LESS_OR_EQUAL_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.isLessOrEqualThan" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.GREATER_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.isGreaterThan" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.GREATER_OR_EQUAL_FILTER%>') {
       return '<spagobi:message key = "SBIListLookPage.isGreaterOrEqualThan" />';
    }
    if(filterOpCode=='<%=SpagoBIConstants.NOT_CONTAIN_FILTER%>') {
        return '<spagobi:message key = "SBIListLookPage.notcontains" />';
     }
}
    
//************************************ End correlation checks ************************************

// ************************************ Start visibility checks  ************************************

function rigenerateViewList() {
  var divBlockContainer = document.getElementById('divViewsList');
	divBlockContainer.innerHTML == '';
	var html = "";
	html += "<table style='margin:1px solid gray;'>";
	if(viewManager.views.length>0) {
		//alert(viewManager.views.length);
  	for(i=0; i<viewManager.views.length; i++) {
      var viewV = viewManager.views[i];

      if(viewV.compareValues && viewV.compareValues.length>100 )
          {
    	  viewvalue = viewV.compareValues.substr(0,100)+'...';
    	  } 
      else { 
          viewvalue = viewV.compareValues;
          }
       
      html += "<tr  height='25' style='background:rgb(251,247,227);'>";

      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;'>";
      html += "<span style='font-size:10pt;'><spagobi:message key = "SBIDev.listObjParuses.ifAnalyticalDriver"/> <b> "+viewV.nameParFather+" </b></span>";     
      html += "<span style='font-size:10pt;'><b> "+viewV.nameCond +" </b></span>";     
      html += "<span style='font-size:10pt;'><b> "+ viewvalue +" </b></span>";     
      html += "<span style='font-size:10pt;'><spagobi:message key = "SBIDev.listObjParuses.viewAs"/>: <b> "+ viewV.viewLabel +" </b></span>";     
      html += "</td>";
//      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;'>";
//		if(viewV.prog){
//      html += "<span style='font-size:10pt;'><b> "+viewV.prog +" </b></span>";
//		}     
//      html += "</td>";

      
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='30px'>";
      html += "<a href='javascript:viewView("+i+")'>";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/detail.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.viewParDep" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.viewParDep" />' "; 
      html += "/></a>";
      html += "</td>";
      html += "<td style='vertical-align:middle;border-bottom:1px solid #bbb;' width='30px'>";
      html += "<a href='javascript:deleteView("+i+")'>";
      html += "<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/erase.gif", currTheme)%>' ";
      html += " title='<spagobi:message key = "SBIDev.listObjParuses.deleteCondition" />' "; 
      html += " alt='<spagobi:message key = "SBIDev.listObjParuses.deleteCondition" />' "; 
      html += "/></a>";
      html += "</td>";
      html += "</tr>";
    }
  } else {
     html += "<tr><td colspan='5'>&nbsp;</td></tr>";  
     html += "<tr><td colspan='5'><b>No View is currently set</b></td></tr>";  
     html += "<tr style='border-bottom:1px solid #bbb;'><td colspan='5'>&nbsp;</td></tr>";    
  }
  html += "</table>";
  divBlockContainer.innerHTML = html;
}

function generateViewBlockHtml(indexView) {
	var view = null;
    if(indexView!=null) {
       view = viewManager.getView(indexView);
    } 
    blockHtml = "";
	blockHtml += "<br/>";
	blockHtml += "<div class='div_detail_area_forms_objParuse'>";
	blockHtml += "<table>";
	blockHtml += "	<tr>";
	blockHtml += "		<td width='50%' style='color:#074B88;font-weight:bold;'>";
	blockHtml += "		 	<span style='font-size:10pt;'><spagobi:message key = "SBIDev.listObjParuses.viewParDep" args="<%=biParam.getLabel()%>"/></span>";
	blockHtml += "		</td>";
	blockHtml += "		<td align='center'>";
	blockHtml += "			<select style='width:150px' id='dependViewSelect' name='dependFrom'>";
	<%
		for(int z=0; z<otherBiParameters.size(); z++) {
			BIObjectParameter otherBiParameter = (BIObjectParameter) otherBiParameters.get(z);
	%>
	selBiParam = " "; 
	if(view!=null) {
    if(view.idParFather==<%=otherBiParameter.getId()%>) {
        selBiParam  = " selected ";
    }
}
	blockHtml += "  		 <option value='<%=otherBiParameter.getId()%>' "+selBiParam+" >";
	blockHtml += "    				<%=otherBiParameter.getLabel()%>";
	blockHtml += "  			</option>";
	<%
	   }
	%>
	blockHtml += "			</select>";
	blockHtml += "		</td>";
	blockHtml += "	</tr>";
	blockHtml += "	<tr height='50px'>";
	blockHtml += "		<td width='50%' >";
	blockHtml += "		  <br/>";
	blockHtml += "			<span style='font-size:10pt;color:#074B88;font-weight:bold;'><spagobi:message key="SBIDev.listObjParuses.viewOperator" /></span>";
	blockHtml += "			<span style='font-size:8pt;color:#074B88;'><spagobi:message key="SBIDev.listObjParuses.viewOperatorExplanation" args="<%=biParam.getLabel()%>" /></span>";
	blockHtml += "		</td>";
	blockHtml += "		<td align='center'>";
	//blockHtml += "		  <br/>";
	blockHtml += "			<select style='width:150px' id='conditionViewSelect' name='condition'>";

	selLogOper = " ";

	//alert('disegno value '+ view.valueCond +' name '+view.nameCond);
	if(view!=null) {
   		 if(view.valueCond=='<%=SpagoBIConstants.CONTAIN_FILTER%>') {
     		  selLogOper  = " selected ";
  	  }
	}
	blockHtml += "  <option value='<%=SpagoBIConstants.CONTAIN_FILTER%>' "+selLogOper+" >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.contains" />";
	blockHtml += "  </option>";
	selLogOper = " ";


	if(view!=null) {
  		 if(view.valueCond=='<%=SpagoBIConstants.NOT_CONTAIN_FILTER%>') {
      		  selLogOper  = " selected ";
 	  }
	}
	blockHtml += "  <option value='<%=SpagoBIConstants.NOT_CONTAIN_FILTER%>' "+selLogOper+" >";
	blockHtml += "	   <spagobi:message key = "SBIListLookPage.notcontains" />";
	blockHtml += "  </option>";

	
	blockHtml += "</select></td></tr>";
	blockHtml += "<br>";
	blockHtml += "<tr><td>  </td></td></tr>";
	blockHtml += "<tr>";
	blockHtml += "		<td width='50%' style='color:#074B88;font-weight:bold;'>";
	blockHtml += "		 	<span style='font-size:10pt;'><spagobi:message key = "SBIDev.listObjParuses.listValues" args="<%=biParam.getLabel()%>"/></span>";
	blockHtml += "		</td>";
	
	//blockHtml += "<span style='color:#074B88;font-weight:bold;'><spagobi:message key="SBIDev.listObjParuses.listValues" args="<%=biParam.getLabel()%>" /></span>";
	//blockHtml += "<hr/>";
	//blockHtml += "<table style='border-spacing:5px;border-collapse:separate;'>";
	//blockHtml += "<tr height='30px'>";
	//blockHtml += "<td style='font-size:10pt;color:#074B88;' align='center'><spagobi:message key="SBIDev.listObjParuses.isActive" /></td>";
	//blockHtml += "<td style='font-size:10pt;color:#074B88;' align='center'><spagobi:message key="SBIDev.listObjParuses.values" /></td>";
	//blockHtml += "<td style='font-size:10pt;color:#074B88;' align='center'><spagobi:message key="SBIDev.listObjParuses.viewLabel" /></td>";
	//blockHtml += "</tr>";
	

    if(view && view.viewLabel) {
        viewLabel  = view.viewLabel;
    }
    else{
        viewLabel = "";
    }

    if(view && view.compareValues) {
        compareValues  = view.compareValues;
    }
    else{
    	compareValues = "";
    }
    
	//blockHtml += "  <tr>";
	blockHtml += "	<td align='center'>";
	blockHtml += "    <input type='text' style='width:150px' id='compareValues' name='compareValues' value='"+compareValues+"' />";
	blockHtml += "  </td>";
	blockHtml += "  </tr>";

	blockHtml += "<br>";
	blockHtml += "  <tr height='50px'>";
	blockHtml += "		<td width='50%' style='color:#074B88;font-weight:bold;'>";
	blockHtml += "		 	<span style='font-size:10pt;'><spagobi:message key = "SBIDev.listObjParuses.viewLabel"/></span>";
	blockHtml += "		</td>";
	blockHtml += "<td align='center'>";
	blockHtml += "    <input type='text' style='width:150px' id='viewLabel' name='viewLabel' value='"+viewLabel+"' />";
	blockHtml += "  </td>";

	
	blockHtml += "  </tr>";

blockHtml += "</table>";
blockHtml += "<br/>";
if(indexView==null) {
	  blockHtml += "<center>";
	  blockHtml += " 	<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' onclick='closeWinView()' ";
	  blockHtml += " 	     title='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	     alt='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	/>";
	  blockHtml += "</center>";
} else {
  	  blockHtml += "<center>";
	  blockHtml += " 	<img src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' onclick='closeWinView("+indexView+")' ";
	  blockHtml += " 	     title='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	     alt='<spagobi:message key = "editConf.save" />' ";
	  blockHtml += " 	/>";
	  blockHtml += "</center>";
} 
blockHtml += "</div>";
return blockHtml;
}

function view(idPF_, namePF_, valueCond_, nameCond_, compareValues_, viewLabel_, prog_ ) {
	this.idParFather = idPF_;
	this.nameParFather = namePF_;
	this.valueCond = valueCond_;
	this.nameCond = nameCond_;
	this.compareValues = compareValues_;
	this.viewLabel = viewLabel_;
	this.prog = prog_;
}

function closeWinView(indexView) {
    depSel = document.getElementById('dependViewSelect');
    conSel = document.getElementById('conditionViewSelect');
    compVals = document.getElementById('compareValues');
    viewLab = document.getElementById('viewLabel');

    valueDepSel = depSel.options[depSel.selectedIndex].value;
    nameDepSel = depSel.options[depSel.selectedIndex].text;
    valueConSel = conSel.options[conSel.selectedIndex].value;
    nameConSel = conSel.options[conSel.selectedIndex].text;

    compareValues = compVals.value;
	viewLabel = viewLab.value;

	    viewVar = null;
	  
	  if(indexView==null) {  
	    // check if the correlation already exists
	    if(viewManager.viewExist(valueDepSel, valueConSel)) {
      		alert('<spagobi:message key = "SBIDev.listObjParuses.correlationExist" />');
      		return;
    	}
      }
	    if(indexView==null) {
      		viewVar = new view(valueDepSel, nameDepSel, valueConSel, nameConSel, compareValues, viewLabel);
      		viewManager.addView(viewVar);

	    } else {
	    	viewVar = viewManager.getView(indexView);
	    	viewVar.idParFather = valueDepSel;
	    	viewVar.nameParFather = nameDepSel;
	    	viewVar.valueCond = valueConSel; 
	    	viewVar.nameCond = nameConSel; 
	    	viewVar.compareValues = compareValues; 
	    	viewVar.viewLabel = viewLabel; 
	   	  	 viewManager.setView(indexView, viewVar);
  	  }
    Windows.closeAll();
    rigenerateViewList();
} 



function addView() {
	   winView = new Window('win_view', {className: "alphacube", title: "<spagobi:message key = "SBIDev.listObjParuses.condition" />", width:700, height:300});
		   winView.setDestroyOnClose();
	   winView.getContent().innerHTML=generateViewBlockHtml();
	   winView.showCenter(true);
	}

function viewView(indexView) {
	   winView = new Window('win_view', {className: "alphacube", title: "<spagobi:message key = "SBIDev.listObjParuses.condition" />", width:700, height:300});
		 winView.setDestroyOnClose();
	   winView.getContent().innerHTML=generateViewBlockHtml(indexView);
	   winView.showCenter(true);
	}

	function deleteView(indexView) {
	   viewManager.deleteView(indexView);
	   rigenerateViewList();
	}


	function generateViewsXml() {
		var xml = '';
		xml += '<views>';
		if(viewManager.views.length>0) {
	  	for(i=0; i<viewManager.views.length; i++) {
	      var viewV = viewManager.views[i];
	      xml += '<view ';
	      xml += ' idParFather="'+viewV.idParFather+'" ';
	      xml += ' valueCond="'+viewV.valueCond+'" ';
	      xml += ' compareValues="'+viewV.compareValues+'" ';
	      xml += ' viewLabel="'+viewV.viewLabel+'" ';
	      xml += '>';
	      xml += '</view>';
	    }
	  }
	  xml += '</views>';
	  return xml;
	}

	


	function saveCorrelationsAndViews() {
		   xmlCorrInpHid = document.getElementById('correlations_xml'); 

			   if(xmlCorrInpHid){
		   xmlCorrGen = generateCorrelationsXml();
		   xmlCorrInpHid.value = xmlCorrGen;
		   }
		   
		   xmlViewInpHid = document.getElementById('views_xml');
		   xmlViewGen = generateViewsXml();
		   xmlViewInpHid.value = xmlViewGen;
	
	//alert(xmlCorrGen);
	//alert(xmlViewGen);
		   saveform = document.getElementById('objParusesForm');
		   saveform.submit();
		}

</script>

	<!-- SAVE AND BACK FORM -->
	
			<table class='header-table-portlet-section'>		
			<tr class='header-row-portlet-section'>
				<td class='header-title-column-portlet-section' 
				    style='vertical-align:middle;padding-left:5px;'>
					<spagobi:message key = "SBIDev.listObjParuses.title" /> <%=" " + biParam.getLabel()%>
				</td>
				<td class='header-empty-column-portlet-section'>&nbsp;</td>
				<td class='header-button-column-portlet-section'>
					<a href="javascript:saveCorrelationsAndViews()"> 
		      			<img class='header-button-image-portlet-section' 
		      			     title='<spagobi:message key = "SBIDev.listObjParuses.saveButt" />' 
		      			     src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/save.png", currTheme)%>' 
		      			     alt='<spagobi:message key = "SBIDev.listObjParuses.saveButt" />' /> 
					</a>
				</td>
				<td class='header-button-column-portlet-section'>
					<a href='<%= backUrl.toString() %>'> 
		      			<img class='header-button-image-portlet-section' 
		      			     title='<spagobi:message key = "SBIDev.listObjParuses.backButt" />' 
		      			     src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
		      			     alt='<spagobi:message key = "SBIDev.listObjParuses.backButt" />' />
					</a>
				</td>
			</tr>
		</table>
	
	<!--  PARUSES -->
	
	<!--  IF THERE'S NO PARUSES SHOW A MESSAGE AND ONLY THE BACK BUTTON -->
	<%
	boolean emptyParuses = false;
	if (allParuses == null || allParuses.size() == 0) {
		emptyParuses = true;
	}

 // show the correlation only if some paruses exist
 if(!emptyParuses){
	
	%>
		
		<div class='div_background_no_img' style='padding:5px;'>
			
			<br/>
      <table width='100%' cellspacing='0' border='0'> <!-- Header of correlation table -->
				<tr>
					<td class='titlebar_level_2_text_section' style='vertical-align:middle;'>
						<spagobi:message key = "SBIDev.listObjParuses.correlExpression" />
					</td>
					<td class='titlebar_level_2_empty_section'>&nbsp;</td>
					<td class='titlebar_level_2_button_section'>
						<a style='text-decoration:none;' href='javascript:addCorrelation();'>
							<img width='20px' height='20px' 
								 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/attach.gif", currTheme)%>'
								 name='info' 
								 alt='<spagobi:message key = "SBIDev.listObjParuses.addCondition" />'
								 title='<spagobi:message key = "SBIDev.listObjParuses.addCondition" />'/>
						</a>
					</td>
				</tr>
		  </table>					
	  
	  
	    <div id="divCorrelationsList" style="width:100%;">  <!--  List of correlations -->
	    </div>
	    
	    <br/><br/>
	  
	  </div>
	  
    
    <script>
	 	<%
	 		Iterator iterCorr = biParamCorrelations.iterator();
	 	    int prog = 0;
	 	    while(iterCorr.hasNext()) {
	 	    	ObjParuse corr = (ObjParuse) iterCorr.next();
	 	    	Integer pfid = corr.getObjParFatherId();
	 	    	String fo = corr.getFilterOperation();
	 	%>
	 	      
          try {
             paruseSettings<%=pfid%><%=fo%>.length;
          } catch (err) {
              paruseSettings<%=pfid%><%=fo%> = new Array();
          }
	 	    	puset<%=prog%> = new paruseSetting(true, <%=corr.getParuseId()%>, '<%=corr.getFilterColumn()%>');
	 	      paruseSettings<%=pfid%><%=fo%>[paruseSettings<%=pfid%><%=fo%>.length] = puset<%=prog%>;
	 	<%   
	 	    prog ++;
	 	    }  
	 	%>
    
        
        <%
	 		iterCorr = biParamCorrelations.iterator();
	 	    while(iterCorr.hasNext()) {
	 	    	ObjParuse corr = (ObjParuse) iterCorr.next();
	 	    	if(corr.getPreCondition()==null){
	 	    		corr.setPreCondition("");
	 	    	}
	 	    	if(corr.getPostCondition()==null){
	 	    		corr.setPostCondition("");
	 	    	}
	 	    	Integer pfid = corr.getObjParFatherId();
	 	    	String fo = corr.getFilterOperation();
	 	%>
	 	    	try {
            paruseSettings<%=pfid%><%=fo%>.length;
          } catch (err) {
            paruseSettings<%=pfid%><%=fo%> = new Array();
          }
          
          try{
            correlation<%=pfid%><%=fo%>==null;
          } catch (err) {
            correlation<%=pfid%><%=fo%> = new correlation(<%=corr.getObjParFatherId()%>, getParNameFromParId(<%=corr.getObjParFatherId()%>), "<%=corr.getFilterOperation()%>", getFilterOpNameFromCode("<%=corr.getFilterOperation()%>"), paruseSettings<%=pfid%><%=fo%>);
            correlation<%=pfid%><%=fo%>.preCond = "<%=corr.getPreCondition()%>";
    	      correlation<%=pfid%><%=fo%>.postCond = "<%=corr.getPostCondition()%>";
    	      correlation<%=pfid%><%=fo%>.logicOper = "<%=corr.getLogicOperator()%>";
            correlationManager.addCorrelation(correlation<%=pfid%><%=fo%>);
          }
	 	<%   
	 	    }  
	 	%>
    
      rigenerateCorrelationList();
    </script>
      
      <%  
      // Close not empty paruse case
	}
 
 // Parameters visibility checks can be set even with a manual input parameter
      %>
      
      	     <!--   PARAMETERS VIEW -->
    
          <table width='100%' cellspacing='0' border='0'>
				<tr>
					<td class='titlebar_level_2_text_section' style='vertical-align:middle;'>
						<spagobi:message key = "SBIDev.listObjParuses.viewExpression" />
					</td>
					<td class='titlebar_level_2_empty_section'>&nbsp;</td>
					<td class='titlebar_level_2_button_section'>
						<a style='text-decoration:none;' href='javascript:addView();'>
							<img width='20px' height='20px' 
								 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/attach.gif", currTheme)%>'
								 name='info' 
								 alt='<spagobi:message key = "SBIDev.listObjParuses.addCondition" />'
								 title='<spagobi:message key = "SBIDev.listObjParuses.addCondition" />'/>
						</a>
					</td>
				</tr>
		  </table>		 
     
     
           	    <div id="divViewsList" style="width:100%;">  <!--  List of views -->
	    </div>
         <br/><br/>
      
      
          <script>
        <%
	 		Iterator iterViews = biParamViews.iterator();
	 	    while(iterViews.hasNext()) {
	 	    	ObjParview view = (ObjParview) iterViews.next();
		    	Integer id = view.getObjParId();
		    	Integer pfid = view.getObjParFatherId();
	 	    	String operation = view.getOperation();
	 	    	String comparevalue = view.getCompareValue();
	 	    	Integer progG = view.getProg();
	 	    	String viewLabel = view.getViewLabel();
	 	    	
	 	%>

	compareValueNoPoint = "<%=comparevalue%>";
	// replace for variable naming ; with -
	compareValueNoPoint = compareValueNoPoint.replace(';','-');


	
		{ // if removing this block pay attention not to have more variables with same name, comparevalueNoPoint should be use as value
	            view<%=pfid%><%=operation%>compareValueNoPoint = new view(<%=view.getObjParFatherId()%>, getParNameFromParId(<%=view.getObjParFatherId()%>), "<%=view.getOperation()%>", getFilterOpNameFromCode("<%=view.getOperation()%>"), "<%=view.getCompareValue()%>", "<%=view.getViewLabel()%>", <%=view.getProg()%> );
      			 viewManager.addView(view<%=pfid%><%=operation%>compareValueNoPoint);
	         } 	 
        <%   
	 	    }  
	 	%>
	      rigenerateViewList();
    </script>
      
      
      	  <form method='POST' action='<%=formUrl%>' id='objParusesForm'>
	       <input type="hidden" name="obj_par_id" value="<%=biParam.getId()%>" />
	       <% if (!emptyParuses){ %>
	       <input type="hidden" id="correlations_xml" name="correlations_xml" value="" />
	       <% }%>
	       <input type="hidden" id="views_xml" name="views_xml" value="" />
    </form>

	    
	    

    
    

	    
	
