/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.image");

Sbi.cockpit.widgets.image.ImageWidget = function(config) {

	Sbi.trace("[ImageWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {
		itemSelected: ''
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.image.ImageWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	
	
	// constructor
	Sbi.cockpit.widgets.image.ImageWidget.superclass.constructor.call(this, c);
	
	this.createContent();
	
	this.on("afterrender", function(){
		this.textTitle.html = this.wgeneric.title;
		Sbi.trace("[ImageWidget]: afterrender - refresh title");
	}, this);
	
	Sbi.trace("[ImageWidget.constructor]: OUT");
};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.image.ImageWidget, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	widgetContent: null,
	textTitle: null,

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	refresh:  function() {
    	Sbi.trace("[ImageWidget.refresh]: IN");
    	Sbi.cockpit.widgets.image.ImageWidget.superclass.refresh.call(this);
		this.createContent();
		this.doLayout();
		Sbi.trace("[ImageWidget.refresh]: OUT");
	},
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	createContent: function() {
    	Sbi.trace("[ImageWidget.createContent]: IN");
		this.widgetContent = new Ext.create('Ext.Img',{
			src: this.wconf.itemSelected.url
		});
		this.textTitle = new Ext.Panel({
			border: false
			, bodyBorder: false
			, hideBorders: true
			, frame: false
			, height: '100%'
			, html: this.wgeneric.title
		});

		if(this.items){
			this.items.each( function(item) {
				this.items.remove(item);
				item.destroy();
			}, this);
		}
		
		if(this.widgetContent !== null) {
			this.add(this.textTitle);
	    	this.add(this.widgetContent);
	    }
		
		Sbi.trace("[ImageWidget.createContent]: OUT");
	}

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================

	// ...
});

Sbi.registerWidget('image', {
	name: LN('sbi.cockpit.widgets.image.imageWidgetDesigner.name')
	, icon: 'js/src/ext4/sbi/cockpit/widgets/image/dummy_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.image.ImageWidget'
	, designerClass: 'Sbi.cockpit.widgets.image.ImageWidgetDesigner'
});