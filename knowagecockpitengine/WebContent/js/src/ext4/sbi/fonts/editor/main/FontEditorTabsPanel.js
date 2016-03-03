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

	Ext.define('Sbi.fonts.editor.main.FontEditorTabsPanel', {
	extend: 'Ext.tab.Panel'
	, layout: 'fit'
	, config:{
		  services: null
		, fonts: null
		, border: false
		, height: 180
		, autoScroll: false
		, tabPosition: 'right'
		, margin: 0
		, padding: 0
		, bodyStyle: 'width: 100%; height: 100%'
	}
	
	/**
	 * @property fontEditorBarChartTab
	 *  Tab for bar chart font options
	 */
	 , fontEditorBarChartTab: null
	 
	 /**
	 * @property fontEditorLineChartTab
	 *  Tab for line chart font options
	 */
	 , fontEditorLineChartTab: null
	 
	 /**
	 * @property fontEditorPieChartTab
	 *  Tab for pie chart font options
	 */
	 , fontEditorPieChartTab: null
	 
	 /**
	 * @property fontEditorTableTab
	 *  Tab for table font options
	 */
	 , fontEditorTableTab: null
	 
	 /**
	 * @property fontEditorCrosstabTab
	 *  Tab for crosstab font options
	 */
	 , fontEditorCrosstabTab: null

	, constructor : function(config) {
		Sbi.trace("[FontEditorTabsPanel.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[FontEditorTabsPanel.constructor]: OUT");
	}

	, initComponent: function() {
        Ext.apply(this, {
			items:[
		         this.fontEditorBarChartTab,
		         this.fontEditorLineChartTab,
		         this.fontEditorPieChartTab,
		         this.fontEditorTableTab,
		         this.fontEditorCrosstabTab
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
		
//		var bcTabFonts = this.findTabFonts("barChartFonts");
//		var lcTabFonts = this.findTabFonts("lineChartFonts");
//		var pcTabFonts = this.findTabFonts("pieChartFonts");
//		var tbTabFonts = this.findTabFonts("tableFonts");
//		var ctTabFonts = this.findTabFonts("crosstabFonts");
		
		var bcTabFonts = Sbi.storeManager.getFont("barChartFonts");
		var lcTabFonts = Sbi.storeManager.getFont("lineChartFonts");
		var pcTabFonts = Sbi.storeManager.getFont("pieChartFonts");
		var tbTabFonts = Sbi.storeManager.getFont("tableFonts");
		var ctTabFonts = Sbi.storeManager.getFont("crosstabFonts");
		
		this.fontEditorBarChartTab = Ext.create('Sbi.fonts.views.BarChartFontTabPanel', {fonts: bcTabFonts});
        this.fontEditorLineChartTab = Ext.create('Sbi.fonts.views.LineChartFontTabPanel', {fonts: lcTabFonts});
        this.fontEditorPieChartTab = Ext.create('Sbi.fonts.views.PieChartFontTabPanel', {fonts: pcTabFonts});
        this.fontEditorTableTab = Ext.create('Sbi.fonts.views.TableFontTabPanel', {fonts: tbTabFonts});
        this.fontEditorCrosstabTab = Ext.create('Sbi.fonts.views.CrosstabFontTabPanel', {fonts: ctTabFonts});
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
    , getFontsList: function(){
    	
    	this.fonts = [];
    	
    	var barChartFontsObj = this.getTabFormFontsList(this.fontEditorBarChartTab)
    	var lineChartFontsObj = this.getTabFormFontsList(this.fontEditorLineChartTab);
    	var pieChartFontsObj = this.getTabFormFontsList(this.fontEditorPieChartTab);
    	var tableFontsObj = this.getTabFormFontsList(this.fontEditorTableTab);
    	var crosstabFontsObj = this.getTabFormFontsList(this.fontEditorCrosstabTab);
        
    	this.addFontObjectToFonts(barChartFontsObj);
    	this.addFontObjectToFonts(lineChartFontsObj);
    	this.addFontObjectToFonts(pieChartFontsObj);
    	this.addFontObjectToFonts(tableFontsObj);
    	this.addFontObjectToFonts(crosstabFontsObj);
    
    	
    	return this.fonts;
    }

    , setFontList: function(f){
    	this.fonts = f;
    }

    , removeAllFonts: function(){
    	this.fonts = new Array();
    }


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------
    
    , addFontObjectToFonts: function(tabObject) {
    
    	if(tabObject !== undefined && tabObject !== null && !Sbi.isEmptyObject(tabObject)){
    		this.fonts.push(tabObject);
    	}
    }
    
	, getTabFormFontsList: function(myTab){
		
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
	
//	, findTabFonts: function(tabId){
//		
//		var tabConf = {};
//		var tabIndex = -1;
//		
//		for(var i = 0; i < this.fonts.length; i++) {
//			if(Sbi.isValorized(this.fonts[i]) && this.fonts[i].tabId === tabId) {
//				tabIndex = i;
//				break;
//			}
//		}
//		
//		if(tabIndex >= 0){
//			tabConf.fonts = this.fonts[tabIndex]
//		}
//		
//		return tabConf;
//		
//	}

});