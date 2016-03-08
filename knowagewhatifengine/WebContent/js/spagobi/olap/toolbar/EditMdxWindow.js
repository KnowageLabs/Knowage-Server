/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * Window that allows the editing of the mdx query
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */



Ext.define('Sbi.olap.toolbar.EditMdxWindow', {
	extend: 'Ext.window.Window',

	config:{
		height: 400,
		width: 300
	},



	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.toolbar && Sbi.settings.olap.toolbar.EditMdxWindow) {
			Ext.apply(this, Sbi.settings.olap.toolbar.EditMdxWindow);
		}


		var thisPanel = this;

		var area = Ext.create('Ext.form.field.TextArea', {
			height: 400,
			width: 300
		});



		this.items= [
		             area
		             ];
		this.bbar = [
		             '->',    {
		            	 text: LN('sbi.common.cancel'),
		            	 handler: function(){
		            		 thisPanel.destroy();
		            	 }
		             },    {
		            	 text: LN('sbi.common.select'),
		            	 tooltip: LN('sbi.olap.toolbar.versionmanagerwindow.version.select.warning'),
		            	 handler: function(){
		            		 Sbi.olap.eventManager.notifyMdxChanged(area.getValue());
		            		 thisPanel.destroy();
		            	 }
		             }];

		this.callParent(arguments);
	}




});
