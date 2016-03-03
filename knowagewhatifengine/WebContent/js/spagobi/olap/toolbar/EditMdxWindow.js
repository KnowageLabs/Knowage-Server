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
