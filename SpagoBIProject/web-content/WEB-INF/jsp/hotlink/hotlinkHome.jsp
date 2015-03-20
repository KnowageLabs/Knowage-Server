<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.spagobi.hotlink.rememberme.bo.RememberMe"%>
<%@page import="it.eng.spagobi.hotlink.rememberme.bo.HotLink"%>
<%@page import="it.eng.spagobi.hotlink.service.HotLinkModule"%>
<%@page import="it.eng.spagobi.hotlink.constants.HotLinkConstants"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.monitoring.dao.AuditManager"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spagobi.analiticalmodel.execution.service.ExecuteDocumentAction"%>

<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf" %>
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/formats/LocaleUtils.js")%>'></script>
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/formats/" + locale.getLanguage() + ".js")%>'></script>


<%
List rememberMeList = null;
List mostPopularList = null;
List myRecentlyUsedList = null;

SourceBean hotlinkSB = (SourceBean) ConfigSingleton.getInstance().getAttribute(HotLinkConstants.HOTLINK);
SourceBean mostPopular = (SourceBean) hotlinkSB.getFilteredSourceBeanAttribute("SECTION", "name", HotLinkConstants.MOST_POPULAR);
SourceBean myRecentlyUsed = (SourceBean) hotlinkSB.getFilteredSourceBeanAttribute("SECTION", "name", HotLinkConstants.MY_RECENTLY_USED);
SourceBean rememberMe = (SourceBean) hotlinkSB.getFilteredSourceBeanAttribute("SECTION", "name", HotLinkConstants.REMEMBER_ME);
if (mostPopular != null) {
	int limit = Integer.parseInt((String) mostPopular.getAttribute(HotLinkConstants.ROWS_NUMBER));
	mostPopularList = AuditManager.getInstance().getMostPopular(userProfile, limit);
}
if (myRecentlyUsed != null) {
	int limit = Integer.parseInt((String) myRecentlyUsed.getAttribute(HotLinkConstants.ROWS_NUMBER));
	myRecentlyUsedList = AuditManager.getInstance().getMyRecentlyUsed(userProfile, limit);
}
if (rememberMe != null) {
	//rememberMeList = DAOFactory.getRememberMeDAO().getMyRememberMe(userProfile.getUserUniqueIdentifier().toString());
	rememberMeList = DAOFactory.getRememberMeDAO().getMyRememberMe(((UserProfile)userProfile).getUserId().toString()); 
}
%>


<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section-no-buttons' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "sbi.hotlink.title" />
		</td>
	</tr>
</table>

<div style="width:80%;" class="div_detail_area_forms">
	<p style="margin: 10px">
		<div id="renderTo_RememberMe"></div>
	</p>	
	<p style="margin: 10px">
		<div id="renderTo_MostPopular"></div>
	</p>
	<p style="margin: 10px">
		<div id="renderTo_MyRecentlyUsed"></div>
	</p>
</div>

<%-- Start scripts for Remember Me list --%>
<% if (rememberMeList != null) { %>
<script type="text/javascript">
Ext.onReady(function(){

    Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

    var myDataRememberMe = [
    	<%
   		Iterator rememberMeListIt = rememberMeList.iterator();
		while (rememberMeListIt.hasNext()) {
			RememberMe rm = (RememberMe) rememberMeListIt.next();
			Map params = new HashMap();
			params.put("ACTION_NAME", ExecuteDocumentAction.SERVICE_NAME);
			params.put(ObjectsTreeConstants.OBJECT_ID, rm.getObjId().toString());
			String parameters = rm.getParameters() != null ? rm.getParameters() : "";
			params.put(ObjectsTreeConstants.PARAMETERS, parameters);
			params.put(SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS, "true");
			String subObjName = rm.getSubObjName();
			if (subObjName != null) {
				params.put(SpagoBIConstants.SUBOBJECT_NAME, subObjName);
			}	
			String executeUrl = urlBuilder.getUrl(request, params);
			executeUrl = executeUrl.replaceAll("&amp;", "&");
			params = new HashMap();
			params.put("PAGE", "HOT_LINK_PAGE");
			params.put("OPERATION", "DELETE_REMEMBER_ME");
			params.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "TRUE");
			params.put("REMEMBER_ME_ID", rm.getId().toString());
			String deleteUrl = urlBuilder.getUrl(request, params);
			deleteUrl = deleteUrl.replaceAll("&amp;", "&");
			%>['<%= StringEscapeUtils.escapeJavaScript(rm.getName()) %>','<%= StringEscapeUtils.escapeJavaScript(rm.getDescription()) %>','<%= StringEscapeUtils.escapeJavaScript(rm.getDocumentLabel()) %>','<%= StringEscapeUtils.escapeJavaScript(rm.getDocumentName()) %>',
				'<%= rm.getDocumentDescription() != null ? StringEscapeUtils.escapeJavaScript(rm.getDocumentDescription()) : "" %>',
				'<%= rm.getDocumentType() %>','<%= StringEscapeUtils.escapeJavaScript(executeUrl) %>','<%= StringEscapeUtils.escapeJavaScript(deleteUrl) %>']<%= rememberMeListIt.hasNext() ? "," : "" %><%
		}
		%>
    ];
    
    Ext.QuickTips.init();
    
    // create the data store
    var storeRememberMe = new Ext.data.SimpleStore({
        fields: [
           {name: 'Name'},
           {name: 'Description'},
           {name: 'DocumentLabel'},
           {name: 'DocumentName'},
           {name: 'DocumentDescription'},
           {name: 'DocumentType'},
           {name: 'Url'},
           {name: 'DeleteUrl'}
        ]
    });
    storeRememberMe.loadData(myDataRememberMe);
    
    <%
    if(currTheme==null || currTheme.equalsIgnoreCase(""))currTheme="sbi_default";
    %>
    
   	var menu = 
		new Ext.menu.Menu({
			id:'submenu',
			items: [{
				text:'<spagobi:message key = "sbi.hotlink.deleteRememberMe" />',
				scope: this,
				icon: '<%=urlBuilder.getResourceLinkByTheme(request, "/img/erase.gif", currTheme)%>',
				handler:function(){
					location.href = storeRememberMe.getAt(menu.rowIndex).get('DeleteUrl');
				}
			}]
		});
    
	Ext.ToolTip.prototype.onTargetOver =
		Ext.ToolTip.prototype.onTargetOver.createInterceptor(function(e) {
			this.baseTarget = e.getTarget();
		});
	Ext.ToolTip.prototype.onMouseMove =
		Ext.ToolTip.prototype.onMouseMove.createInterceptor(function(e) {
			if (!e.within(this.baseTarget)) {
				this.onTargetOver(e);
				return false;
			}
		});
    
    // create the Grid
    var gridRememberMe = new Ext.grid.GridPanel({
        store: storeRememberMe,
        columns: [
            {id: "Name", header: LN('sbi.hotlinks.name'), sortable: true, dataIndex: 'Name'},
            {header: LN('sbi.hotlinks.document'), sortable: true, dataIndex: 'DocumentLabel'},
            {header: LN('sbi.hotlinks.document.name'), sortable: true, dataIndex: 'DocumentName'},
            {header: LN('sbi.hotlinks.document.description'), sortable: true, dataIndex: 'DocumentDescription'},
            {header: LN('sbi.hotlinks.document.type'), sortable: true, dataIndex: 'DocumentType'}
        ],
		onRender: function() {
        	Ext.grid.GridPanel.prototype.onRender.apply(this, arguments);
        	this.addEvents("beforetooltipshow");
	        this.tooltip = new Ext.ToolTip({
	        	renderTo: Ext.getBody(),
	        	target: this.view.mainBody,
	        	listeners: {
	        		beforeshow: function(qt) {
	        			var v = this.getView();
			            var row = v.findRowIndex(qt.baseTarget);
			            var cell = v.findCellIndex(qt.baseTarget);
			            this.fireEvent("beforetooltipshow", this, row, cell);
	        		},
	        		scope: this
	        	}
	        });
        },
		viewConfig: {
        	forceFit: true
		},
        stripeRows: true,
        collapsible: true,
        //autoExpandColumn: 'Document',
        height:200,
        width:600,
        title:LN('sbi.hotlinks.rememberme'),
		listeners: {
			render: function(g) {
				g.on("beforetooltipshow", function(grid, row, col) {
					if (storeRememberMe.getAt(row)) {
						grid.tooltip.body.update(storeRememberMe.getAt(row).get('Description'));
					}
				});
			}
		}
    });
    
	gridRememberMe.on(
		'rowclick', function(grid, rowIndex, e) {
			location.href = storeRememberMe.getAt(rowIndex).get('Url');
		}
	);
	
	gridRememberMe.on(
		'rowcontextmenu', function(grid, rowIndex, e) {
			var record = grid.getStore().getAt(rowIndex);
			e.stopEvent();
			menu.rowIndex = rowIndex;
			menu.showAt(e.getXY());
		}
	);
	
    gridRememberMe.render('renderTo_RememberMe');

    //gridRememberMe.getSelectionModel().selectFirstRow();
});
</script>
<% } %>
<%-- End scripts for Remember Me list --%>

<%-- Start scripts for most popular list --%>
<% if (mostPopularList != null && mostPopularList.size() > 0) { %>
<script type="text/javascript">
Ext.onReady(function(){

    Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

    var myDataMostPopular = [
    	<%
		Iterator mostPopularListIt = mostPopularList.iterator();
		while (mostPopularListIt.hasNext()) {
			HotLink hotlink = (HotLink) mostPopularListIt.next();
			Map params = new HashMap();
			params.put("ACTION_NAME", ExecuteDocumentAction.SERVICE_NAME);
			params.put(ObjectsTreeConstants.OBJECT_ID, hotlink.getObjId().toString());
			params.put(ObjectsTreeConstants.PARAMETERS, hotlink.getParameters() != null ? StringEscapeUtils.escapeJavaScript(hotlink.getParameters()) : "");
			params.put(SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS, "true");
			String subObjName = hotlink.getSubObjName();
			if (subObjName != null) {
				params.put(SpagoBIConstants.SUBOBJECT_NAME, subObjName);
			}
			String executeUrl = urlBuilder.getUrl(request, params);
			executeUrl = executeUrl.replaceAll("&amp;", "&");
			%>['<%= hotlink.getDocumentLabel() %>','<%= hotlink.getDocumentName() %>','<%= hotlink.getDocumentDescription() %>',
				'<%= hotlink.getDocumentType() %>','<%= executeUrl %>']<%= mostPopularListIt.hasNext() ? "," : "" %><%
		}
		%>
    ];
    
    // create the data store
    var storeMostPopular = new Ext.data.SimpleStore({
        fields: [
           {name: 'DocumentLabel'},
           {name: 'DocumentName'},
           {name: 'DocumentDescription'},
           {name: 'DocumentType'},
           {name: 'Url'}
        ]
    });
    storeMostPopular.loadData(myDataMostPopular);
    
    // create the Grid
    var gridMostPopular = new Ext.grid.GridPanel({
        store: storeMostPopular,
        columns: [
            {id: "Document", header: LN('sbi.hotlinks.document'), sortable: true, dataIndex: 'DocumentLabel'},
            {header: LN('sbi.hotlinks.document.name'), sortable: true, dataIndex: 'DocumentName'},
            {header: LN('sbi.hotlinks.document.description'), sortable: true, dataIndex: 'DocumentDescription'},
            {header: LN('sbi.hotlinks.document.type'), sortable: true, dataIndex: 'DocumentType'}
        ],
		viewConfig: {
        	forceFit: true
		},
        stripeRows: true,
        collapsible: true,
        //autoExpandColumn: 'Document',
        height:200,
        width:600,
        title:LN('sbi.hotlinks.mostpopular')
    });
	gridMostPopular.on(
		'rowclick', function(grid, rowIndex, e) {
			location.href = storeMostPopular.getAt(rowIndex).get('Url');
		}
	);
    gridMostPopular.render('renderTo_MostPopular');

    //gridMostPopular.getSelectionModel().selectFirstRow();
});
</script>
<% } %>
<%-- End scripts for most popular list --%>

<%-- Start scripts for my recently used list --%>
<% if (myRecentlyUsedList != null && myRecentlyUsedList.size() > 0) { %>
<script type="text/javascript">
Ext.onReady(function(){

    Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

    var myDataMyRecentlyUsed = [
    	<%
		Iterator myRecentlyUsedListIt = myRecentlyUsedList.iterator();
		while (myRecentlyUsedListIt.hasNext()) {
			HotLink hotlink = (HotLink) myRecentlyUsedListIt.next();
			Map params = new HashMap();
			params.put("ACTION_NAME", ExecuteDocumentAction.SERVICE_NAME);
			params.put(ObjectsTreeConstants.OBJECT_ID, hotlink.getObjId().toString());
			params.put(ObjectsTreeConstants.PARAMETERS, hotlink.getParameters() != null ? StringEscapeUtils.escapeJavaScript(hotlink.getParameters()) : "");
			params.put(SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS, "true");
			String subObjName = hotlink.getSubObjName();
			if (subObjName != null) {
				params.put(SpagoBIConstants.SUBOBJECT_NAME, subObjName);
			}
			String executeUrl = urlBuilder.getUrl(request, params);
			executeUrl = executeUrl.replaceAll("&amp;", "&");
			%>['<%= hotlink.getDocumentLabel() %>','<%= hotlink.getDocumentName() %>','<%= hotlink.getDocumentDescription() %>',
				'<%= hotlink.getDocumentType() %>','<%= executeUrl %>']<%= myRecentlyUsedListIt.hasNext() ? "," : "" %><%
		}
		%>
    ];
    
    // create the data store
    var storeMyRecentlyUsed = new Ext.data.SimpleStore({
        fields: [
           {name: 'DocumentLabel'},
           {name: 'DocumentName'},
           {name: 'DocumentDescription'},
           {name: 'DocumentType'},
           {name: 'Url'}
        ]
    });
    storeMyRecentlyUsed.loadData(myDataMyRecentlyUsed);
    
    // create the Grid
    var gridMyRecentlyUsed = new Ext.grid.GridPanel({
        store: storeMyRecentlyUsed,
        columns: [
            {id: "Document", header: LN('sbi.hotlinks.document'), sortable: true, dataIndex: 'DocumentLabel'},
            {header: LN('sbi.hotlinks.document.name'), sortable: true, dataIndex: 'DocumentName'},
            {header: LN('sbi.hotlinks.document.description'), sortable: true, dataIndex: 'DocumentDescription'},
            {header: LN('sbi.hotlinks.document.type'), sortable: true, dataIndex: 'DocumentType'}
        ],
		viewConfig: {
        	forceFit: true
		},
        stripeRows: true,
        collapsible: true,
        //autoExpandColumn: 'Document',
        height:200,
        width:600,
        title:LN('sbi.hotlinks.recently')
    });
	gridMyRecentlyUsed.on(
		'rowclick', function(grid, rowIndex, e) {
			location.href = storeMyRecentlyUsed.getAt(rowIndex).get('Url');
		}
	);
    gridMyRecentlyUsed.render('renderTo_MyRecentlyUsed');

    //gridMyRecentlyUsed.getSelectionModel().selectFirstRow();
});
</script>
<% } %>
<%-- End scripts for my recently used list --%>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>