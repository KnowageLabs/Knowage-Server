/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * updateLayout(layout): update the layout of the active tab
 * 
 * updateActiveSheet(change) : update the sheet after tools value changed 
 * 
 * validate(): return null if the panel is valid, else return a validationError for each sheet
 * setSheetsState(state): set the state of the panels
 * getSheetsState(): get the state of the panel
 * 
 * Public Events
 * 
 * tabChange(activePanel): the tab is changed
 * 
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.SheetsContainerPanel = function(config) {
	
	var defaultSettings = {};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.sheetsContainerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.sheetsContainerPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	this.addEvents("attributeDblClick", "sheetchange");
	
	this.index = 0;
	
	this.addPanel = {
		id: 'addTab',
        title: '<br>',
        iconCls: 'newTabIcon'
    };
	
	c = {
		border: false,
		tabPosition: 'bottom',        
        enableTabScroll:true,
        defaults: {autoScroll:true},
        items: [this.addPanel],
        frame: true,
        plugins: new Sbi.worksheet.designer.SheetTabMenu()
	};
	
	this.initPanel();
	
	Sbi.worksheet.designer.SheetsContainerPanel.superclass.constructor.call(this, c);	 	
	
	if (this.sheets !== undefined && this.sheets !== null && this.sheets.length > 0) {
		if (config.smartFilter) {
			this.on('resize',this.setSheetsStateDefered,this);
		} else {
			this.setSheetsState(this.sheets);
		}
	} else if (config.smartFilter) {
		this.on('resize',this.addFirstTab,this);
	} else {
		this.on('render',function(){this.addTab();},this);
	}

};

Ext.extend(Sbi.worksheet.designer.SheetsContainerPanel, Ext.TabPanel, {
	index: null,
	//sheets: null, from the config object the sheets to be displayed initially; to be passed as a property of the constructor's input object!!!
	
	initPanel: function(){
	    this.on('tabchange',function(tabPanel, tab){
	    	if(tab==null || tab.id=='addTab'){
	    		this.addTab();
	    		tabPanel.setActiveTab(tabPanel.items.length-2);
	    	}
	    	this.fireEvent('sheetchange',tab);
	    },this);
	}

	, addTab: function(sheetConf){
		this.suspendEvents();

		
		this.remove('addTab');
		
		//The title property is overridden: see setSheetsState
		var sheet = new Sbi.worksheet.designer.SheetPanel({
	        title: 'Sheet ' + (++this.index),
	        closable: true
	    });
		// propagate event
		sheet.on(
			'attributeDblClick' , 
			function (theSheet, attribute) { 
				this.fireEvent("attributeDblClick", this, attribute, theSheet); 
			}, 
			this
		);

		if (sheetConf !== undefined && sheetConf !== null && sheetConf.length > 0) {
			sheet.setSheetState(sheetConf) ;
		}
		
		sheet.contentPanel.on('addDesigner', this.addDesignerHandler, this);
		
	    var tab = this.add(sheet);
	    this.add(this.addPanel);

	    
	    if(this.getActiveTab()==null){
	    	this.setActiveTab(0);
	    }
	    
	    this.resumeEvents();
	    
	    tab.on('beforeClose',function(panel){
			Ext.MessageBox.confirm(
					LN('sbi.worksheet.designer.msg.deletetab.title'),
					LN('sbi.worksheet.designer.msg.deletetab.msg'),            
		            function(btn, text) {
		                if (btn=='yes') {
		                	this.remove(panel);
		                }
		            },
		            this
				);
			return false;
	    }, this);

	    return sheet;
	}
	
	,addFirstTab: function(){
		if(!this.alreadyBuilded){
			this.un('resize',this.addFirstTab,this);
			this.alreadyBuilded= true;
			this.addTab();
		}
	}
	
	, addDesignerHandler: function (sheet, state) {
		var newSheet = this.addTab({});
		newSheet.contentPanel.addDesigner(state);
		this.activate(newSheet);
		this.notifySheetAdded.defer(500, this, [LN('sbi.worksheet.designer.msg.newsheet.title'), LN('sbi.worksheet.designer.msg.newsheet.msg')]);
	}
	
    , createBox: function (t, s) {
        return '<div class="msg"><h3>' + t + '</h3><p>' + s + '</p></div>';
    }
    
    , notifySheetAdded : function(title, format) {
        if(!this.msgCt){
        	this.msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
        }
        this.msgCt.applyStyles({'z-index': 20000});
        this.msgCt.applyStyles.defer(4000, this.msgCt, [{'z-index': -10}]); // when the effect is finished, hide the container (it's a workaround)
        var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
        var m = Ext.DomHelper.append(this.msgCt, this.createBox(title, s), true);
        m.hide();
        m.slideIn('b', { duration: 1 }).pause( 1 ).ghost("b", { duration: 2, remove: true});
    }
	
	//Update the layout of the active panel
	, updateLayout: function (layout) {
		var activeTab = this.getActiveTab();
		if(activeTab==null){
			this.setActiveTab(0);
			activeTab = this.getActiveTab();
		}
		activeTab.updateLayout(layout);
	}
	
	//Update the sheet after tools value changed 
	, updateActiveSheet: function(change){
		if(change.sheetLayout!=null && change.sheetLayout!=undefined){
			this.updateLayout(change.sheetLayout);
		}
	}
	
	, getSheetsState: function(){
		if(this.sheets!=undefined && this.sheets!=null && this.sheets.length>0){//the sheet panel is not already rendered
			return {'sheets' : this.sheets};
		}
		var sheets = [];
		if(this.items.items.length>1){
			var i=0;
			for(; i<this.items.items.length-1; i++){//-1 because of the add panel tab
				sheets.push(this.items.items[i].getSheetState());
			}
		}
		return {'sheets' : sheets};
	}
	
	/**
	 * Set the tabs..  THE PANEL SHOULD BE ALREADY RENDERED
	 * The title is overridden: if the saved panels has title [Sheet 1, Sheet 2, Sheet 7]
	 * the new titles are  [Sheet 1, Sheet 2, Sheet 3]
	 */
	, setSheetsState: function(sheets){
		
		
		if(this.items.length>1){
			this.remove(this.items[0]);//remove the first panel
		}

		//add the panels
		if(this.rendered){
			this.sheets = null;
			var i=0;
			for(; i<sheets.length; i++){
				var aSheetPanel = this.addTab();
				aSheetPanel.setSheetState(sheets[i]);
				this.updateTabLastName(sheets[i].name);
			}
		}else{
			this.on('render',function(){
				this.sheets = null;
				var i=0;
				for(; i<sheets.length; i++){
					var aSheetPanel = this.addTab();
					aSheetPanel.setSheetState(sheets[i]);
					this.updateTabLastName(sheets[i].name);
				}
			},this);
		}		
	}
	
	, updateTabLastName: function(sheetName){
		//6= length("Sheet ")
		var prefix = sheetName.substring(0, 6);
		if (prefix == "Sheet ") {
			var actualSheetNumber = parseInt(sheetName.substring(6));
			if ( !isNaN(actualSheetNumber) ) {
				this.index = actualSheetNumber;
			}
		}
	}
	
	, setSheetsStateDefered: function(){
		if(!this.alreadyBuilded){
			this.un('resize',this.setSheetsStateDefered,this);
			this.alreadyBuilded= true;
			this.setSheetsState(this.sheets);
		}
	}
		
	, validate: function(validFields){
		//var valid = true;
		var toReturn = new Array();
		if ( this.items.items.length > 1 ) {
			var errCounter = 0;
			var i = 0;
			for(; i<this.items.items.length-1; i++){//-1 because of the add panel tab
				var aSheet = this.items.items[i];
				
				// check if another sheet with the same name exists
				var j = 0;
				for(; j < this.items.items.length-1; j++) {
					var otherSheet = this.items.items[j];
					if (i != j && otherSheet.title == aSheet.title) {
						var valError = new Sbi.worksheet.exception.ValidationError(
								aSheet.getName(),
								LN('sbi.worksheet.designer.msg.samenamesheets')
						);
						toReturn[errCounter] = valError;
						errCounter++;
					}
				}
				
				if (aSheet.rendered) { 
					/* workaround (work-around): the sheet is validated only if it is rendered, 
					because a non-rendered sheet hasn't the state set. TODO improve this behaviour */
					var errMessage = aSheet.validate(validFields);
					if (errMessage) {
						var valError = new Sbi.worksheet.exception.ValidationError(
								aSheet.getName(),
								errMessage
						);
						toReturn[errCounter] = valError;
						errCounter++;
					}
				}
			}
		}else{
			var errMessage = LN('sbi.worksheet.designer.msg.emptyworksheet');
			var valError =new Sbi.worksheet.exception.ValidationError(
								'Worksheet',
								errMessage
								);
			toReturn.push(valError);
		}
		return toReturn;
	}

	, showState: function(event, toolEl, panel) {
		Ext.MessageBox.confirm(
				LN('sbi.worksheet.designer.msg.deletetab.title'),
				Ext.encode(this.getSheetsState()),            
	            function(btn, text) {
	                if (btn=='yes') {
	                	this.remove(panel);
	                }
	            },
	            this
			);
  	} 
    
	
});