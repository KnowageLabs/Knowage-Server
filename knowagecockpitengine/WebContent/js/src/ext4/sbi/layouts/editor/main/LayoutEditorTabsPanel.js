/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**

  * Authors
  *
  * - Giorgio Federici (giorgio.federici@eng.it)
  */

	Ext.define('Sbi.layouts.editor.main.LayoutEditorTabsPanel', {
	extend: 'Ext.tab.Panel'
	, layout: 'fit'
	, config:{
		  services: null
		, layouts: null
		, border: false
		, height: 180
		, autoScroll: false
		, tabPosition: 'right'
		, margin: 0
		, padding: 0
		, bodyStyle: 'width: 100%; height: 100%'
	}
	
	/**
	 * @property layoutEditorViewModeTab
	 *  Tab for view mode layout options
	 */
	 , fontEditorViewModeTab: null

	, constructor : function(config) {
		Sbi.trace("[LayoutEditorTabsPanel.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[LayoutEditorTabsPanel.constructor]: OUT");
	}

	, initComponent: function() {
        Ext.apply(this, {
			items:[
		         this.layoutEditorViewModeTab
		         ]
        });
        this.callParent();
	}

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		//this.initStore();
		this.initTabs();
	}
	
	, initTabs: function() {
		
		var viewModeTabLayout = Sbi.storeManager.getLayout("viewModeLayouts");
		
		this.layoutEditorViewModeTab = Ext.create('Sbi.layouts.views.ViewModeLayoutTabPanel', {layouts: viewModeTabLayout});

	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
    , getLayoutsList: function(){
    	
    	this.layouts = [];
    	
    	var viewModeLayoutsObj = this.getTabFormLayoutsList(this.layoutEditorViewModeTab)
        
    	this.addLayoutObjectToLayouts(viewModeLayoutsObj);    
    	
    	return this.layouts;
    }

    , setLayoutList: function(l){
    	this.layouts = l;
    }

    , removeAllLayouts: function(){
    	this.layouts = new Array();
    }


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , addLayoutObjectToLayouts: function(tabObject) {
    
    	if(tabObject !== undefined && tabObject !== null && !Sbi.isEmptyObject(tabObject)){
    		this.layouts.push(tabObject);
    	}
    }
    
	, getTabFormLayoutsList: function(myTab){
		
		var result = {};
		var checkedProperties = {};		
    	
		var form = myTab.getForm();
        var values = form.getFieldValues();        
        
        for (var property in values) {
            if (values.hasOwnProperty(property)) {
            	if(values[property] !== null && values[property] !== ""){
            		checkedProperties[property] = values[property];
            	}
            }
        }
        
        if(!Sbi.isEmptyObject(checkedProperties)){
        	result = checkedProperties;
        	result.id = myTab.getId();
        }
        
        return result;
        
	}

});