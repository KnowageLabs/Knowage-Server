<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@page language="java" 
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%>

<!-- %@ page trimDirectiveWhitespaces="true"% -->
<%@ include file="/WEB-INF/jsp/wapp/homeBase.jsp"%>

<link id="spagobi-ext-4" rel="styleSheet" href ="<%=contextName %>/themes/sbi_default/css/home40/layout.css" type="text/css" />

<%
/*
	if(isFirstUrlToCallEqualsToDefaultPage == true && jsonMenuList.length() > 0 ){
		JSONObject menuItem = jsonMenuList.getJSONObject(0);
		String url = menuItem.optString("firstUrl");
		if(url != null) {
			firstUrlToCall = url;
		}
	}
*/
%>

<%-- Javascript object useful for session expired management (see also sessionExpired.jsp) --%>
<script>
sessionExpiredSpagoBIJS = 'sessionExpiredSpagoBIJS';

var firstUrl =  '<%= StringEscapeUtils.escapeJavaScript(firstUrlToCall) %>';  
firstUrlTocallvar = firstUrl;

Ext.onReady(function () {
	

    Ext.tip.QuickTipManager.init();
    this.mainframe = Ext.create('Ext.ux.IFrame', 
    			{ xtype: 'uxiframe'
    			, renderTpl: ['<iframe src="{src}" id="iframeDoc" name="{frameName}" width="100%" height="100%" frameborder="0"></iframe>']
	  			, src: firstUrl
  	  			, height: '100%'
  	  			});
    Sbi.execution.ExporterUtils.setIFrame( this.mainframe );
    

	
	function hideItem( menu, e, eOpts){
       // console.log('bye bye ');
        menu.hide();
    }
	
	<%if (isDirectExec.equals("FALSE")){%>
		this.mainpanel =  Ext.create("Ext.panel.Panel",{
	    	autoScroll: true,
	    	height: '100%',
	    	items: [
				//this.titlePath	,		
	    	    mainframe]
	    	, dockedItems: [/*{
		   	    xtype: 'toolbar',
		   	    dock: 'left',
		   	    items: itemsM
	    	}*/]
	    });
	<%}else{%>	
		this.mainpanel =  Ext.create("Ext.panel.Panel",{
			autoScroll: true,
			height: '100%',
			items: [
				//this.titlePath	,		
			    mainframe]		
		});
	<%}%>  
	
    Ext.create('Ext.Viewport', {
    	
        layout: 'fit',
        items: [this.mainpanel]
    });
    
    
    
});

</script>
<!-- Include AngularJS application -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/menu/menuApp.js"></script>


<div data-ng-controller="menuCtrl" ng-app="menuApp">
<div id="divContainer" class="overlayButtonBar ">
	<a href="#" data-ng-click="toggleMenu()" class="menuKnowage"><i class="material-icons md-24">menu</i></a>
	<a href="#" aria-hidden="true" class="logoKnowage"><img src="<%=contextName %>/themes/sbi_default/css/menuBar/logo_knowage.png" width="120"/></a>
</div>
<menu-aside></menu-aside>

</div>

<script>
$(document).ready(function() {
     $(function() { $('#divContainer').draggable( {containment: 'window'}); });
		
});
</script>





 