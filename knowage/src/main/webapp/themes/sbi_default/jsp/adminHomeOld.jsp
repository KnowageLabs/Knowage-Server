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

<%@page language="java" 
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%>
<%@ include file="/WEB-INF/jsp/wapp/homeBase.jsp"%>    
<!-- Include Ext stylesheets here: -->
<link id="spagobi-ext-4" rel="styleSheet" href ="<%=contextName %>/themes/geobi/css/home40/layout.css" type="text/css" />



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
	    	, dockedItems: [{
		   	    xtype: 'toolbar',
		   	    dock: 'left',
		   	    items: itemsM
	    	}]
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
 