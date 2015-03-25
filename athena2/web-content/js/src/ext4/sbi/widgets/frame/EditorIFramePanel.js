/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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