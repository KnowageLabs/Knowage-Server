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
 * container of the columns definition of the pivot table
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionFilters', {
	extend: 'Sbi.olap.execution.table.OlapExecutionDimensions',

	layout: {
	    type: 'hbox',
	    pack: 'start',
	    align: 'stretch'
	},

	config:{
		/**
	     * @cfg {String} dimensionClassName
	     * The name of the class that extends the Sbi.olap.execution.table.OlapExecutionDimension class.
	     * The class name is used to build the subclass
	     */
		dimensionClassName: 'Sbi.olap.execution.table.OlapExecutionFilter'
    },

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionColumns) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionColumns);
		}
		this.callParent(arguments);
		this.addEvents("filterValueChanged");
	},

	updatePanelDefaultHtml: function() {

		if(this.store && this.store.getCount()>0){
			this.update("");
		}else{
			this.update('<div style=" margin-top: 11px; margin-left: 5px;">'+LN('sbi.olap.execution.table.filter.empty')+'</div>');
		}

		this.callParent();
	}
});







