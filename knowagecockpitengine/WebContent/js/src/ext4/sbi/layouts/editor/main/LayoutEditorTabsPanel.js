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