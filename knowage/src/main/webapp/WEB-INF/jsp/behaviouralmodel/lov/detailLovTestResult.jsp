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


<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>


<%@ page import="javax.portlet.PortletURL,
			it.eng.spagobi.commons.constants.SpagoBIConstants,
			it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue,
			it.eng.spagobi.behaviouralmodel.lov.bo.ScriptDetail,
			it.eng.spagobi.behaviouralmodel.lov.bo.JavaClassDetail,
			it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail,
			it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse,
			it.eng.spagobi.commons.dao.DAOFactory,
			it.eng.spago.navigation.LightNavigationManager,
			java.util.List,
			java.util.ArrayList,
			java.util.Iterator"%>
<%@page import="it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory"%>
<%@page import="it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.behaviouralmodel.lov.handlers.LovManager"%>
<%@page import="it.eng.spagobi.commons.utilities.PortletUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.utilities.objects.Couple"%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

<%
	SourceBean detailMR = (SourceBean) aServiceResponse.getAttribute("DetailModalitiesValueModule"); 
	SourceBean listLovMR = (SourceBean) aServiceResponse.getAttribute("ListTestLovModule"); 

	String lovProviderModified = (String)aSessionContainer.getAttribute(SpagoBIConstants.LOV_MODIFIED);
	if (lovProviderModified == null) 
		lovProviderModified = "false";
	
	String modality = null;
	if (detailMR != null) modality = (String) detailMR.getAttribute("modality");
	if (modality == null) modality = (String) aSessionContainer.getAttribute(SpagoBIConstants.MODALITY);
  	String messagedet = "";
  	if (modality.equals(SpagoBIConstants.DETAIL_INS))
		messagedet = SpagoBIConstants.DETAIL_INS;
	else messagedet = SpagoBIConstants.DETAIL_MOD;
	
  	Map saveUrlPars = new HashMap();
  	saveUrlPars.put("PAGE", "DetailModalitiesValuePage");
  	saveUrlPars.put(SpagoBIConstants.MESSAGEDET, messagedet);
  	// saveUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "2");
  	saveUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
  	saveUrlPars.put("RETURN_FROM_TEST_MSG","SAVE");
    String saveUrl = urlBuilder.getUrl(request, saveUrlPars);
  	
    Map backUrlPars = new HashMap();
    backUrlPars.put("PAGE", "DetailModalitiesValuePage");
    backUrlPars.put(SpagoBIConstants.MESSAGEDET, messagedet);
    backUrlPars.put("modality", modality);
    //backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
    backUrlPars.put("RETURN_FROM_TEST_MSG", "DO_NOT_SAVE");
    if(!lovProviderModified.trim().equals(""))
    	backUrlPars.put("lovProviderModified", lovProviderModified);
    String backUrl = urlBuilder.getUrl(request, backUrlPars);
  	
  	ModalitiesValue modVal = (ModalitiesValue) aSessionContainer.getAttribute(SpagoBIConstants.MODALITY_VALUE_OBJECT);
  	String lovProv = modVal.getLovProvider();
  	ILovDetail lovDet = LovDetailFactory.getLovFromXML(lovProv);
    String readonly = "" ;
    boolean isreadonly = true;
    if (userProfile.isAbleToExecuteAction(SpagoBIConstants.PARAMETER_MANAGEMENT)){
   	isreadonly = false;
   	readonly = "readonly";
   	}
    
    /*
    String treeColumnNames = "";
    if(lovDet!=null && lovDet.getTreeLevelsColumns()!=null && lovDet.getTreeLevelsColumns().size()>0){
    	treeColumnNames = treeColumnNames+"['"+lovDet.getTreeLevelsColumns().get(0)+"'";
    	 for(int i=1; i<lovDet.getTreeLevelsColumns().size(); i++){
    		 treeColumnNames =treeColumnNames +",'"+ lovDet.getTreeLevelsColumns().get(i)+"'";
    	 }
    	 treeColumnNames = treeColumnNames+"]";
    }else{
    	treeColumnNames="[]";
    }*/
    
    String treeColumnNames = "[";
    if (lovDet != null && lovDet.getTreeLevelsColumns() != null && lovDet.getTreeLevelsColumns().size() > 0) {
		Iterator<Couple<String, String>> it = lovDet.getTreeLevelsColumns().iterator();
		while (it.hasNext()) {
			Couple<String, String> entry = it.next();
			treeColumnNames += "{name : '" + entry.getFirst() + "', description : '" + entry.getSecond() + "'}";
			if (it.hasNext()) {
				treeColumnNames += ",";
			}
		}
    }
    treeColumnNames += "]";
    
    String contextName = GeneralUtilities.getSpagoBiContext();
    String adapterHTTPName = GeneralUtilities.getSpagoAdapterHttpUrl();
    contextName = contextName+adapterHTTPName;
   
%>









<script type="text/javascript">



	var addLovTestEvents = function(lovTest, lovPanel, lovConfig, modality, contextName){
		lovTest.on('lovTypeChanged',function(type){
			lovPanel.remove(lovTest,'true');
			lovConfig.lovType=type;
			lovTest = Ext.create('Sbi.behavioural.lov.TestLovPanel',{contextName: contextName,lovConfig:lovConfig, modality:modality}); //by alias
			addLovTestEvents(lovTest,lovPanel, lovConfig, modality, contextName);
			lovPanel.add(lovTest);
		},this);
	}
    
    Ext.onReady(function(){
		var lovConfig = {}
		lovConfig.descriptionColumnName =  '<%= lovDet.getDescriptionColumnName()%>';
		lovConfig.valueColumnName =  '<%= lovDet.getValueColumnName()%>';
		lovConfig.visibleColumnNames =  '<%= lovDet.getVisibleColumnNames()%>';
		
		lovConfig.lovType =  '<%= lovDet.getLovType()%>';
		
		var treeColumnNames =  '<%= lovDet.getTreeLevelsColumns()%>';
		if(treeColumnNames && treeColumnNames!='null'){
			lovConfig.treeColumnNames =  <%=treeColumnNames%>;
		}
		
		var modality =  '<%= messagedet%>'; 
		var contextName = '<%= contextName%>'; 
    	var lovTest = Ext.create('Sbi.behavioural.lov.TestLovPanel',{contextName: contextName, lovConfig:lovConfig, modality:modality}); //by alias
		var lovPanel = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [lovTest]
	    });
		addLovTestEvents(lovTest,lovPanel, lovConfig, modality, contextName);
		
		 
    });
	

</script>

