
/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 

/**
  * Object name 
  * 
  * [description]
  * 
  * Public Properties
  * 
  * [list]
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * Public Events
  * 
  *  [list]
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.MapField = function(config) {
	
	Ext.apply(this, config);
	
	var c = Ext.apply({}, config, {
		triggerCls: 'fa-trigger my-map-trigger' , 
		enableKeyEvents: true ,
		width: 150 ,
	});   
	
	// constructor
	Sbi.widgets.MapField.superclass.constructor.call(this, c);
	
	this.on("render", function(field) {
		field.trigger.on("click", function(e) {
			if(!this.disabled) {
				this.onOpenFilter(); 
			}
		}, this);
	}, this);
	this.addEvents('select');	
};

Ext.extend(Sbi.widgets.MapField, Ext.form.TriggerField, {
    
	// ----------------------------------------------------------------------------------------
	// members
	// ----------------------------------------------------------------------------------------
    
	// STATE MEMBERS
	  valueField: null
    , displayField: null
    , descriptionField: null
    
    , drawFilterToolbar: null
    
    // oggetto (value: description, *)
    , xvalue: null
    // oggetto (value: description, *)
    , xselection: null
    , xdirty: false
    
    , singleSelect: true
    
    , paging: true
    , start: 0 
    , limit: 20
    
	// SUB-COMPONENTS MEMBERS
	, store: null
	, sm: null
	, cm: null
    , grid: null
    , win: null
    
	, onOpenFilter: function() {
		var MAP_FIELD = this;
		
		var parameter = this.parameter;
		
		var valueData = '';
		if(this.getValue().trim() != '') {
			valueData = '&SELECTEDPROPDATA=' + this.getValue().trim();
		}
		
		var mapFilterSrc = 
			Sbi.config.contextName + '/restful-services/publish?PUBLISHER=' 
				+ '/WEB-INF/jsp/behaviouralmodel/analyticaldriver/mapFilter/geoMapFilter.jsp?'
					+ 'SELECTEDLAYER=' + parameter.selectedLayer
					+ '&SELECTEDLAYERPROP=' + parameter.selectedLayerProp
					+ '&MULTIVALUE=' + parameter.multivalue
					+ valueData;
	
		
		Ext.IframeWindow = Ext.extend(Ext.Window, {
			title : LN('sbi.execution.parametersselection.mapfilter.select') + ' - ' + parameter.label,
			resizable : true,
			draggable : false,
			closeAction : 'destroy',
			modal : true,
			style : {
				maxHeight : '90%',
				width : '95%'
			},
			onRender : function() {
				this.bodyCfg = {
					tag : 'iframe',
					src : mapFilterSrc,
					style : {
						height : '100%',
						width : '100%'
					}
				};
				
				//Workaround for IE iframe window sizing
				this.setHeight(this.container.getHeight() * 0.9);
				
				Ext.IframeWindow.superclass.onRender.apply(this, arguments);
			},
			onDestroy: function () {
				console.log('angularWindow destroy');
				
				var selectedFeaturesProperties = parent.mapFilterSelectedProp();
				
				if(selectedFeaturesProperties && Array.isArray(selectedFeaturesProperties) 
						&& selectedFeaturesProperties.length > 0) {
					
					var stringValueToSet = '';
					for(var i = 0; i < selectedFeaturesProperties.length ; i++) {
						if(i > 0) {
							stringValueToSet += ',';
						}
						stringValueToSet += "'" + selectedFeaturesProperties[i] + "'";
					}
					
					MAP_FIELD.setValue(stringValueToSet);
				}
			}
		});

		var angularWindow = new Ext.IframeWindow();

		angularWindow.show();
	}
	
});