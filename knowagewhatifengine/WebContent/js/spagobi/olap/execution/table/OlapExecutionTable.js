/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Implementation of the pivot table. This component is only the table.
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionTable', {
	extend: 'Ext.panel.Panel',
	layout:'fit',
	html:'<div id="_table_container_" style="height: 100%; width:100%; ">   </div>',

	config:{
		border: false
		, autoScroll: true
	}


	,constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionTable) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionTable);
		}
		this.callParent(arguments);
		this.on("render",this.loadTable,this);
	},

    /**
     * Updates the html of the panel with the html of the pivot table
     * @param {String} pivotHtml the HTML representation of the pivot to render
     */
	updateAfterMDXExecution: function(pivotHtml){
		this.update(pivotHtml);
	},

    /**
     * Loads the template starting MDX
     */
	loadTable: function(){
		Sbi.olap.eventManager.notifyMdxChanged();
	}

});





