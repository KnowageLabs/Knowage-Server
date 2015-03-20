/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

 
Ext.ns("Sbi.geo.tools");

/**
 * Sbi.geo.tools.MeasureCataloguePanel
 * A popup window that shows the content of measure catalogue
 */
Sbi.geo.tools.MeasuresCataloguePanel = function(config) {


	this.validateConfigObject(config);
	this.adjustConfigObject(config);

	var defaultSettings = {
		layout : 'fit',
		contextPath : "SpagoBI",
		columnsRef : [ 'dsName', 'dsCategory', 'dsType' ],
		measuresProperties : [ {
			header : 'Alias',
			dataIndex : 'alias'
		}, {
			header : 'Type',
			dataIndex : 'classType'
		}, {
			header : 'Column',
			dataIndex : 'columnName'
		} ],
		datasetsProperties : [ {
			header : 'Name',
			dataIndex : 'dsName'
		}, {
			header : 'Label',
			dataIndex : 'dsLabel'
		}, {
			header : 'Category',
			dataIndex : 'dsCategory'
		}, {
			header : 'Type',
			dataIndex : 'dsType'
		} ],
		filteringProperties : [ 'alias', 'dsName', 'dsCategory', 'dsType' ]
	};

	
	
	if(Sbi.settings && Sbi.settings.georeport && Sbi.settings.georeport.tools && Sbi.settings.georeport.tools.measurecatalogue) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.georeport.tools.measurecatalogue);
	}
	
	
	defaultSettings = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, defaultSettings);
	
	
	this.init();
	
	var c = ({
		store : this.buildStore(),
		view : new Ext.grid.GroupingView(
				{
					forceFit : true,
					groupTextTpl : '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Misure" : "Misura"]})'
				}),

		tbar : this.topToolbar,
		cm : this.columnModel,
		sm : this.selectionModel,
		plugins : this.expander
	});
	
	if(this.showBottomToolbar === false) {
		c.bbar = this.bottomToolbar;
	}

	this.addEvents('storeLoad');
	
	this.store.on("load", function(store, records, options) {
		var selected = [];
		
		Sbi.debug("[MeasuresCataloguePanel.onStoreLoad]: selected measures are [" + this.selectedMeasures + "]");
		
		if(this.selectedMeasures) {
			for(var i = 0; i < records.length; i++) {
				if( this.selectedMeasures[records[i].data.label] ) {
					Sbi.debug("[MeasuresCataloguePanel.onStoreLoad]: Record [" + records[i].data.label + "] selected");
					selected.push(records[i]);
				} else {
					Sbi.debug("[MeasuresCataloguePanel.onStoreLoad]: Record [" + records[i].data.label + "] not selected");
				}
				
			}
		}
		if(selected.length > 1) {
			Sbi.debug("[MeasuresCataloguePanel.onStoreLoad]: Select [" + selected.length + "] records");
			this.selectionModel.selectRecords(selected);
		}
		
		delete this.selectedMeasures;
		
	}, this);

	Sbi.geo.tools.MeasuresCataloguePanel.superclass.constructor.call(this,c);
};


Ext.extend(Sbi.geo.tools.MeasuresCataloguePanel, Ext.grid.GridPanel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
	, topToolbar: null
	, bottomToolbar: null
	, showBottomToolbar: true
	
	   // =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	/**
	 * @method 
	 * 
	 * Controls that the configuration object passed in to the class constructor contains all the compulsory properties. 
	 * If it is not the case an exception is thrown. Use it when there are properties necessary for the object
	 * construction for whom is not possible to find out a valid default value.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the config object received as input
	 */
	, validateConfigObject: function(config) {
		
	}

	/**
	 * @method 
	 * 
	 * Modify the configuration object passed in to the class constructor adding/removing properties. Use it for example to 
	 * rename a property or to filter out not necessary properties.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the modified version config object received as input
	 * 
	 */
	, adjustConfigObject: function(config) {
		
	}

	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	// ...
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	// ...

	//-----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function() {
		this.topToolbar =  this.initTopToolbar();
		if(this.showBottomToolbar === true) {
			this.bottomToolbar =  this.initBottomToolbar();
		}
		this.expander = this.initExpanderPlugin();
		this.selectionModel = new Ext.grid.CheckboxSelectionModel({SingleSelect:false, grid:this});
		this.columnModel = this.initColumnModel(this.selectionModel, this.expander);
	}
	
	, initTopToolbar: function(){
		
		var thisPanel = this;
		
		this.search = new Ext.form.TriggerField({
			enableKeyEvents: true,
			cls: ' x-form-text-search',
			triggerClass:'x-form-clear-trigger',
	    	onTriggerClick: function(e) {
	    		if(this.el.dom.className.indexOf("x-form-text-search")<0){
            		this.el.dom.className+=" x-form-text-search";
            	}
	    		this.setValue("");
	    		thisPanel.filter("");
			},
			listeners:{
				keyup:function(textField, event){
					thisPanel.filter(textField.getValue());
	            	if(textField.getValue()==""){
	            		textField.el.dom.className+=" x-form-text-search";
	            	}else if(textField.el.dom.className.indexOf("x-form-text-search")>=0){
	            		textField.el.dom.className=textField.el.dom.className.replace("x-form-text-search","");
	            	}
				},
				scope: thisPanel
			}
		});

		
		var tb = new Ext.Toolbar(['->',this.search]);

		return tb;	
	}
	
	, initBottomToolbar: function(grid){
		
		var thisPanel = this;
		
		var joinMeasuresButton = new Ext.Toolbar.Button({
			text    : LN('sbi.tools.catalogue.measures.join.btn'),
			tooltip : LN('sbi.tools.catalogue.measures.join.tooltip'),
			handler : function() {
				thisPanel.executeJoin();
			}
		});

		
		var tb = new Ext.Toolbar(['->',joinMeasuresButton]);

		return tb;
		
	}
	
	, initColumnModel: function(sm, expander){
		var thisPanel = this;
		
		var highlightSearchString = function (value, a, b) {
			var searchString = thisPanel.search.getValue().toUpperCase();
			if (value != undefined && value != null && searchString != '') {
				return thisPanel.highlightSearchStringInternal(value, 0, searchString, thisPanel);
			}
			return value;
		};
		
		var columnsDesc = [expander];
		
		//Builds the columns of the grid
		for(var i=0; i<this.columnsRef.length; i++){
			var column = this.columnsRef[i];
			var object = {
					header: LN('sbi.tools.catalogue.measures.column.header.'+column),
					sortable: true,
					dataIndex: column,
					groupName: 'Dataset'
				};
			//if the column is involved in the filter we should add the renderer 
			if(this.filteringProperties.indexOf(column)>=0){
				object.renderer=highlightSearchString;
			}
			columnsDesc.push(object);
		}
		columnsDesc.push(sm);
		
		return new Ext.grid.ColumnModel(columnsDesc);
	}

	, buildStore: function(){
		this.store=  new Ext.data.GroupingStore({

			proxy:new Ext.data.HttpProxy({
				type: 'json',
				url : Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'measures', baseUrl:{contextPath: this.contextPath}})
			}),
			reader: new  Ext.data.JsonReader({
				fields: [
				         "label",
				         "id",
				         "alias",
				         "columnName",
				         "classType",
				         "dsType",
				         "dsId",
				         "dsCategory",
				         "dsName",
				         "dsLabel"
				         ],
				         groupField:'dsName',
				         root: 'measures'
			}),

			autoLoad:true,
			
			sortInfo:{field: 'dsName', direction: "ASC"}
			,  groupField:'dsName'

		});
		
		return this.store;
	}
	
	, filter: function(value){
		if(value!=null && value!=undefined && value!=''){
			this.getStore().filterBy(function(record,id){
				
				if(record!=null && record!=undefined){
					var data = record.data;
					if(data!=null && data!=undefined){
						for(var p in data){
							if(this.filteringProperties.indexOf(p)>=0){//if the column should be considered by the filter
								if(data[p]!=null && data[p]!=undefined && ((""+data[p]).toUpperCase()).indexOf(value.toUpperCase())>=0){
									return true;
								}
							}
						}
					}
				}
				return false;		
			},this);
		}else{
			this.getStore().clearFilter();
		}
	}
	
	, executeJoin: function(){
		var measuresLabels = new Array();
		var selected = this.getSelectionModel().getSelections();
		if(selected!=null && selected!=undefined && selected.length>0){
			for(var i=0; i<selected.length; i++){
				measuresLabels.push(selected[i].data.label);
			}
			if(measuresLabels.length<1){
				alert("Nothing to join"); 
				return;
			}
			Ext.MessageBox.wait('Please wait...');
			Ext.Ajax.request({
				url: Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'measures/join', baseUrl:{contextPath: this.contextPath}}),
				params: {labels: measuresLabels},
				success : function(response, options) {
					Ext.MessageBox.updateProgress(1);
					Ext.MessageBox.hide();
					if(response !== undefined && response.responseText !== undefined && response.statusText=="OK") {
						if(response.responseText!=null && response.responseText!=undefined){
							if(response.responseText.indexOf("error.mesage.description")>=0){
								Sbi.exception.ExceptionHandler.handleFailure(response);
							} else {
								var r = Ext.util.JSON.decode(response.responseText);
						
								var store = new Ext.data.JsonStore({
								    fields: r.metaData.fields
								});
								store.loadData(r.rows);
								this.fireEvent("storeLoad", this, options, store, r.metaData);
							}
						}
					} else {
						Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
					}
				},
				scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure
			});
		}
	}
	
	, initExpanderPlugin: function(){
		
    	var measuresProperties = "";
    	for(var i=0; i<this.measuresProperties.length; i++){
    		measuresProperties = measuresProperties+'<tr><td style="width: 100px"><p><b>'+this.measuresProperties[i].header+':</b></td><td><p>{'+this.measuresProperties[i].dataIndex+'}</p></td></tr>';
    	}
    	
    	var datasetsProperties = "";
    	for(var i=0; i<this.datasetsProperties.length; i++){
    		datasetsProperties = datasetsProperties+'<tr><td style="width: 100px"><p><b>'+this.datasetsProperties[i].header+':</b></td><td><p>{'+this.datasetsProperties[i].dataIndex+'}</p></td></tr>';
    	}
    	
		
	    var expander = new Ext.grid.RowExpander({

	    	

	        tpl : new Ext.Template(
	        		'<div class="htmltable">',
	        		'<div class="measure-detail-container"><div class="measure-detail-title"><h2><div class="group-header" style="background-image: none!important">'+LN('sbi.tools.catalogue.measures.measure.properties')+'</div></h2></div>',
	        		'<table>',
	        		'		<tr style="height: 90px">',
	        		'			<td class="measure-detail-measure">',
	        		'			</td>',
	        		'			<td><table>',
	        						measuresProperties,
	        		'			</table></td>',			
	        		'		</tr>',
	        		'</table></div>',
	        		'<div class="dataset-detail-container"><div class="measure-detail-title"><h2><div class="group-header" style="background-image: none!important">'+LN('sbi.tools.catalogue.measures.dataset.properties')+'</div></h2></div>',
	        		'<table>',
	        		'		<tr style="height: 100px">',
	        		'			<td class="measure-detail-dataset">',
	        		'			</td>',
	        		'			<td><table>',
	        						datasetsProperties,
	        		'			</table></td>',
	        		'		</tr>',
	        		'</table></div>',
	        		'</div>'
	        )
	    });
		
		
	    return expander;
	}
	


	
	/**
	 * @Private
	 */
	, highlightSearchStringInternal: function (value, startIndex, searchString, thisPanel) {
        var startPosition = value.toLowerCase().indexOf(searchString.toLowerCase(), startIndex);
        if (startPosition >= 0 ) {
            var prefix = "";
            if (startPosition > 0 ) {
                prefix = value.substring(0, startPosition);
            }
            var filterSpan = "<span class='x-livesearch-match'>" + value.substring(startPosition, startPosition + searchString.length) + "</span>";
            var suffix = value.substring(startPosition + searchString.length);
            var newValue = prefix + filterSpan + suffix;
            return thisPanel.highlightSearchStringInternal(newValue, startPosition + filterSpan.length, searchString, thisPanel);
        } else {
        	return value;
        }
	}

});