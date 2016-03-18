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
 * A simple IFrame
 *     
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 

Ext.define('Sbi.widgets.EditorIFramePanel', {
	extend: 'Ext.ux.IFrame',


	cunstructor: function(){
		var defaultSettings = {
				loadMask : true
				, frame : true
				, height: '100%'
		};

		if (Sbi.settings && Sbi.settings.widgets && Sbi.settings.widgets.editoriframepanel) {
			defaultSettings = Ext.apply(defaultSettings,Sbi.settings.widgets.editoriframepanel);
		}

		var c = Ext.apply(defaultSettings, config || {});

		Ext.apply(this, c);

		// constructor
		this.callParent(arguments);
	}


	/**
	 * loads the url into the frame
	 * @param {String} url The url to load
	 */
	, laod: function(url){
		this.callParent(url);
	}
	



});