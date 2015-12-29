/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * container of the columns definition of the pivot table
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */




Ext.define('Sbi.olap.execution.table.OlapExecutionColumns', {
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
		dimensionClassName: 'Sbi.olap.execution.table.OlapExecutionColumn'
    },

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionColumns) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionColumns);
		}
		this.callParent(arguments);
	}
});





