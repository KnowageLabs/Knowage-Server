<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

 
<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>

<!-- spagobi:list moduleName="ListDataSourceModule" /-->


<script type="text/javascript"> 
	
	Ext.onReady
	(
		function() 
		{
			var lovDetailPanel = Ext.create("Sbi.behavioural.lov.LOVListDetailPanel", {});
			
			var lovViewport = Ext.create
			(
				"Ext.container.Viewport",
				
				{
					layout:'fit',
					items:[ lovDetailPanel ]
				}
			);
		}
	);

</script>