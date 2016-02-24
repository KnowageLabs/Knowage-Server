<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@page language="java" 
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%>
<%@ include file="/WEB-INF/jsp/wapp/homeBase.jsp"%>    

<%-- Javascript object useful for session expired management (see also sessionExpired.jsp) --%>
<script>
sessionExpiredSpagoBIJS = 'sessionExpiredSpagoBIJS';

Ext.onReady(function () {
	
	var firstUrl =  '<%= StringEscapeUtils.escapeJavaScript(firstUrlToCall) %>';  
	firstUrlTocallvar = firstUrl;
    Ext.tip.QuickTipManager.init();
    this.mainframe = Ext.create('Ext.ux.IFrame', { 
    	xtype: 'uxiframe'
    	, renderTpl: ['<iframe src="{src}" id="iframeDoc" name="{frameName}" width="100%" height="100%" frameborder="0"></iframe>']
    	, id: 'doc'
  	  	, src: firstUrl
  	  	, height: '100%'
  	});
    
    Sbi.execution.ExporterUtils.setIFrame( this.mainframe );
    
    this.titlePath = Ext.create("Ext.Panel",{title :'Home'});
    var itemsM = <%=jsonMenuList%>;
	for(i=0; i< itemsM.length; i++){
		var menuItem = itemsM[i];
		if(menuItem.itemLabel != null && menuItem.itemLabel == "LANG"){
	 		var languagesMenuItems = [];
	 		for (var j = 0; j < Sbi.config.supportedLocales.length ; j++) {
	 			var aLocale = Sbi.config.supportedLocales[j];
	 			aLocale.currTheme = '<%=currTheme%>';
 				var aLanguagesMenuItem = new Ext.menu.Item({
					id: '',
					text: aLocale.language,
					iconCls:'icon-' + aLocale.language,
					href: this.getLanguageUrl(aLocale)
				})
 				languagesMenuItems.push(aLanguagesMenuItem);
	 		}
	 		menuItem.menu= languagesMenuItems;
		}else if(menuItem.itemLabel != null && menuItem.itemLabel == "ROLE"){
			if(Sbi.user.roles && Sbi.user.roles.length == 1){
				menuItem.hidden=true;
			}
		}else if(menuItem.itemLabel != null && menuItem.itemLabel == "HOME"){
			menuItem.tooltip = '<p style="color: blue; ">'+LN('sbi.home.Welcome')+'<b>'+ 
			'<p style="color: white; font-weight: bold;">'+Sbi.user.userName+'</p>'
								+'<b></p>'
		}
		
	}
	function hideItem( menu, e, eOpts){
        //console.log('bye bye ');
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
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/menu/menuAppAdmin.js"></script>


<div data-ng-controller="menuCtrl" ng-app="menuAppAdmin">
<menu-aside></menu-aside>
<div id="divContainer" class="overlayButtonBar ">
	<a href="#" data-ng-click="toggleMenu()" class="menuKnowage"><i class="material-icons md-24">menu</i></a>
	<a href="#" class="logoKnowage"><img src="<%=urlBuilder.getResourceLinkByTheme(request, "/css/menuBar/logo_knowage.png", currTheme)%>" width="120"/></a>
</div>
</div>

<script>
$(document).ready(function() {
     $(function() { $('#divContainer').draggable({containment: 'window'}); });
     
});

</script>