/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * 
 * This is a grid panel linked to a Store.. It builds the model and the associated store. It adds the page to the grid and the filtering toolbar.
 * It adds the widgets to the grid rows and the buttons in the toolbar according to the configuration. You must define the property filteredProperties,
 * otherwise it will not find anything
 * 
 * 		@example
 * 		...
 *		var FixedGridPanelConf= {
 *			pagingConfig:{},
 *			storeConfig:{ 
 *				pageSize: 5
 *			},
 *			columnWidth: 2/5,
 *			buttonToolbarConfig:{
 *					newButton: true,
 *					cloneButton: true
 *			},
 *			buttonColumnsConfig:{
 *				deletebutton:true,
 *				selectbutton: true
 *			},
 *			modelName: "ModelName",
 *			columns: this.columns,
 *			filteredProperties: this.filteredProperties
 *		};
 *		
 *		Ext.apply(this,config||{});
 *		
 *		this.grid=Ext.create('Sbi.widgets.grid.FixedGridPanelInMemoryFiltered',FixedGridPanelConf);
 *		... 
 * 
 * @author
 * Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.widgets.grid.FixedGridPanelInMemoryFiltered', {
    extend: 'Sbi.widgets.grid.FixedGridPanel'

    ,config: {
     	/**
    	 * The list of the properties that should be filtered 
    	 */
    	filteredProperties: new Array(),
    	/**
    	 * Object with internal properties to filter
    	 */
    	filteredObjects: null
    }

	/**
	 * The constructor:
	 * 1) builds the store. You can change the behavior using the configuration storeConfig
	 * 2) add pagination and the additional button to the toolbar
	 */
	, constructor: function(config) {
    	
    	Sbi.debug('FixedGridPanelInMemoryFilterd store built');
    	for(var i=0; i<config.columns.length; i++){
    		config.columns[i].renderer =  this.onRenderCell;
    	}

      	this.callParent([config]);
    	Sbi.debug('FixedGridPanelInMemoryFilterd costructor OUT');
    },
       
    /**
     * Add the effect to the found substring in the cell
     * @private 
     * @param {String} value the value of the cell
     */
    onRenderCell: function(value) {
    	var filterString = this.filterString;
    	var startPosition;
    	var tempString = value;
    	var toReturn="";

    	if(filterString){
    		while(tempString.length>0){
    			startPosition = tempString.toLowerCase().indexOf(filterString.toLowerCase());      		
        		if(startPosition>=0){
        			//prefix
        			toReturn = toReturn+ tempString.substring(0,startPosition);
            		toReturn = toReturn+ "<span class='x-livesearch-match'>"+ tempString.substring(startPosition,startPosition+filterString.length)+"</span>";
            		tempString = tempString.substring(startPosition+filterString.length);
        		}else{
        			toReturn=toReturn+tempString;
        			tempString ="";
        		}
    		}
    		return toReturn;
    	}else{
    		return value;
    	}
    },
    
    /**
     * @private
     * Adds the toolbar with the search input to the grid
     */
    addToolbar: function(){
      	//Adds the additional buttons to the toolbar
      	this.additionalButtons = Sbi.widget.grid.StaticGridDecorator.getAdditionalToolbarButtons(this.buttonToolbarConfig, this);
      	
      	this.tbar = Ext.create('Sbi.widgets.toolbar.InLineFilterAndOrder',Ext.apply({store: this.store, additionalButtons:this.additionalButtons, addCustomCombo:this.customComboToolbarConfig }));
      	this.tbar.on("filter",function(filtercofing){
      		this.filterString = filtercofing.filterString;
      	},this);
      	  
    },
    
    /**
     * @override
     */
    buildStore: function(modelname){
		//BUILD THE STORE
    	Sbi.debug('FixedGridPanel bulding the store...');
    	
    	this.storeConfig = Ext.apply({
    		parentGrid: this,
    		model: modelname,
    		filteredProperties: this.filteredProperties,
    		filteredObjects: this.filteredObjects
    	},this.storeConfig||{});
    	Sbi.debug('FixedGridPanel store built.');
    	return Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);
    }
});


