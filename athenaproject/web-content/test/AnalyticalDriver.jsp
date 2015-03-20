
<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>

<script type="text/javascript">
	
		Ext.onReady(function(){
		var analyticalDriverDetail = Ext.create('Sbi.behavioural.analyticalDriver.AnalyticalDriverListDetailPanel',{}); 
		var analyticalDriverDetailViewport = Ext.create('Ext.container.Viewport', {
		layout:'fit',
	    items: [analyticalDriverDetail]
	    });
    });

	</script>