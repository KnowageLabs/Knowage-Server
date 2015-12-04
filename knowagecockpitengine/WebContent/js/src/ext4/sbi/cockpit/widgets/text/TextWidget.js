/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.text");

Sbi.cockpit.widgets.text.TextWidget = function(config) {

	Sbi.trace("[TextWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {
		fieldValue: ''
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.text.TextWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	
	
	// constructor
	Sbi.cockpit.widgets.text.TextWidget.superclass.constructor.call(this, c);
	
	this.createContent();
	
//	this.on("afterrender", function(){
//		this.textTitle.html = this.wgeneric.title;
////		this.doLayout();
////		this.getParentComponent().setTitle('');
//		Sbi.trace("[TextWidget]: afterrender - refresh title");
//	}, this);
	
	Sbi.trace("[TextWidget.constructor]: OUT");
};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.text.TextWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	widgetContent: null,
	
	//the title panel is handled by the WidgetContainerComponent
//	textTitle: null,

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	refresh:  function() {
    	Sbi.trace("[TextWidget.refresh]: IN");
    	Sbi.cockpit.widgets.text.TextWidget.superclass.refresh.call(this);
		this.createContent();
		this.doLayout();
		Sbi.trace("[TextWidget.refresh]: OUT");
	},
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	createContent: function() {
    	Sbi.trace("[TextWidget.createContent]: IN");
		this.widgetContent = new Ext.Panel({
			border: false
			, bodyBorder: false
			, hideBorders: true
			, frame: false
			, height: '100%'
			, html: this.wconf.textValue
		});
//		this.textTitle = new Ext.Panel({
//			border: false
//			, bodyBorder: false
//			, hideBorders: true
//			, frame: false
//			, height: '100%'
//			, html: this.wgeneric.title
//		});

		if(this.items){
			this.items.each( function(item) {
				this.items.remove(item);
				item.destroy();
			}, this);
		}
		
		if(this.widgetContent !== null) {
//			this.add(this.textTitle);
	    	this.add(this.widgetContent);
	    }
		
		Sbi.trace("[TextWidget.createContent]: OUT");
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});

Sbi.registerWidget('text', {
	name: LN('sbi.cockpit.widgets.text.textWidgetDesigner.text')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/text/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.text.TextWidget'
	, designerClass: 'Sbi.cockpit.widgets.text.TextWidgetDesigner'
});