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

Ext.ns("Sbi.cockpit.widgets.image");

Sbi.cockpit.widgets.image.ImageWidget = function(config) {

	Sbi.trace("[ImageWidget.constructor]: IN");
	// init properties...
	var defaultSettings = {
		listeners: {
			'forceResize': function(args) {
				this.applyAspectRatioCss(args);
			}
		}
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.image.ImageWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	// constructor
	Sbi.cockpit.widgets.image.ImageWidget.superclass.constructor.call(this, c);
	
	this.createContent();
	this.applyAspectRatioCss();
	
	this.addEvents('forceResize');
	
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
	imageWidgetCSSClass : 'imageWidgetCSSClass',
	
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
    	Sbi.trace("[ImageWidget.refresh]: IN");
    	Sbi.cockpit.widgets.image.ImageWidget.superclass.refresh.call(this);
    	this.createContent();
    	this.applyAspectRatioCss();
		this.doLayout();
		Sbi.trace("[ImageWidget.refresh]: OUT");
	},
	
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
	createContent: function() {
    	Sbi.trace("[ImageWidget.createContent]: IN");
		this.widgetContent = Ext.create('Ext.Img',{
			src: this.wconf.itemSelected?this.wconf.itemSelected.url:'',
			cls: this.imageWidgetCSSClass,
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
 		Sbi.trace("[ImageWidget.createContent]: OUT");
	},

	applyAspectRatioCss: function(newCssValues) {
		newCssValues = newCssValues || {};
		
		var clsClass = this.imageWidgetCSSClass;
		var clsClassId = this.imageWidgetCSSClass + "_" + this.id;
		
		var clsClassCSS = "#" + this.id + " ." + clsClass	+ " {";
		
		clsClassCSS += " max-height: ";
		clsClassCSS += ((newCssValues.height != undefined)? (newCssValues.height + "px;") : "100%;");
		
		clsClassCSS += " max-width: "; 
		clsClassCSS += ((newCssValues.width != undefined)? (newCssValues.width + "px;") : "100%;" );
		
		clsClassCSS += "}";
		
		Ext.util.CSS.removeStyleSheet(clsClassId);
		Ext.util.CSS.createStyleSheet(clsClassCSS, clsClassId);
	},
	
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