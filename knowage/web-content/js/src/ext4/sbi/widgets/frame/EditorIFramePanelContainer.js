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
 * Container of an IFrame. It is a panel that contains an IFrame, so it gives some additional  features such as Toolbar.
 * It is an Abstract class all the additional functionalities should be implemented in the child class
 * 
 *     
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
  
Ext.define('Sbi.widgets.EditorIFramePanelContainer', {
	extend: 'Ext.Panel',

	iframe: null

	, constructor : function(config) {
		Ext.apply(this, {}||config);
		this.init(config);
		this.callParent(arguments);
	}

	/**
	 * Init the component
	 * @param {Object} config configuration object
	 */
	, init: function(config){
		this.layout = 'fit';
		if(!this.iframe){
			this.iframe = Ext.create('Sbi.widgets.EditorIFramePanel',{}); 
		}
		this.items=[this.iframe];
	}
	
	/**
	 * loads the url into the frame
	 * @param {String} url The url to load
	 */
	, load: function(url){
		this.iframe.load(url);
	}
    
	
});