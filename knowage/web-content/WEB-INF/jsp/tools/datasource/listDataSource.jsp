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


<%@page import="it.eng.spagobi.commons.bo.UserProfile" %>
<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>


<!-- spagobi:list moduleName="ListDataSourceModule" /-->

<%
	Boolean superadmin =(Boolean)((UserProfile)userProfile).getIsSuperadmin();
	if(superadmin != null && superadmin){
		
	}

%>

<script type="text/javascript">
	var conf = {
		isSuperadmin: false
	};
	<%if(superadmin != null && superadmin){%>
		conf.isSuperadmin=true;
	<%}%>

    Ext.onReady(function(){
		var datasourceDetail = Ext.create('Sbi.tools.datasource.DataSourceListDetailPanel',conf); //by alias
		var datasourceDetailViewport = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [datasourceDetail]
	    });
    });
	

</script>
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
