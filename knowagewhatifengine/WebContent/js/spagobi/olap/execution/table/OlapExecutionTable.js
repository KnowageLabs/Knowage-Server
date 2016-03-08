/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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





