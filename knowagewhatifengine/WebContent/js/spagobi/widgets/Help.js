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
 * This is a simple window with an html content.
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.widgets.Help', {
	extend: 'Ext.panel.Panel',

	config:{
		frame: true,
		width: 400,
		height: 500
	},

	/**
     * @property {String} content
     *  The content on the help window
     */
	content: "",

	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.Help) {
			Ext.apply(this, Sbi.settings.widgets.Help);
		}
	},

	initComponent: function() {

		Ext.apply(this, {
			html: content
		});
		this.callParent();
	}



});

