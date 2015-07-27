/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.document");

Sbi.cockpit.widgets.document.DocumentWidget = function(config) {

	Sbi.trace("[DocumentWidget.constructor]: IN");
	var defaultSettings= {};
	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.document.DocumentWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);
	
	// constructor
	Sbi.cockpit.widgets.document.DocumentWidget.superclass.constructor.call(this, c);
	
	this.createContent();
	
	Sbi.trace("[DocumentWidget.constructor]: OUT");
};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.document.DocumentWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	widgetContent: null,

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	refresh:  function() {
    	Sbi.trace("[DocumentWidget.refresh]: IN");
    	Sbi.cockpit.widgets.document.DocumentWidget.superclass.refresh.call(this);
		this.createContent();
		this.doLayout();
		Sbi.trace("[DocumentWidget.refresh]: OUT");
	},
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	createContent: function() {
    	Sbi.trace("[DocumentWidget.createContent]: IN");
    	
    	var parametersString = "";
    	var parameters = this.wconf.parameters;
    	if(parameters){
    		parametersString = "&PARAMETERS=";
    		Object.keys(parameters).forEach(function(key){
    			parametersString += key + "%3D" + parameters[key] + "%26";
    		});
    	}
    	var url = Sbi.config.contextName+'/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&NEW_SESSION=TRUE&OBJECT_LABEL='+this.wconf.documentLabel+parametersString;
    	
    	this.widgetContent = new Ext.ux.IFrame({
    		src: url,
    		style: {height: '100%', width: '100%'},
    		listeners: {
    	        click: {
    	            element: 'el', //bind to the underlying el property on the panel
    	            fn: function(){ console.log('click el',el);alert('click!'); }
    	        },
    	        dblclick: {
    	            element: 'body', //bind to the underlying body property on the panel
    	            fn: function(){ console.log('dblclick body');alert('dblclickbody'); }
    	        }
    	    }
    	});
    	
		if(this.items){
			this.items.each( function(item) {
				this.items.remove(item);
				item.destroy();
			}, this);
		}
		
		if(this.widgetContent !== null) {
			this.add(this.widgetContent);
	    }
		
		Sbi.trace("[DocumentWidget.createContent]: OUT");
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});

Sbi.registerWidget('document', {
	name: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.name')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/document/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.document.DocumentWidget'
	, designerClass: 'Sbi.cockpit.widgets.document.DocumentWidgetDesigner'
});