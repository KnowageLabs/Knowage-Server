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
 * Authors - Antonella Giachino
 */

Ext.define('Sbi.cockpit.core.SheetsContainerPanel', {
	extend: 'Ext.TabPanel'
	, layout:'fit'
	, plugins:	Ext.create('Sbi.cockpit.core.SheetTabMenu',{showCloseAll:false,
															showCloseOthers: false,
															listeners: {

		sheetremove: function(tab){		
			//removes the selected widget container from the internal list
			this.tabPanel.hideElementsOfTab(tab);
			this.tabPanel.widgetContainerList.remove(tab);
			if (this.tabPanel.widgetContainerList.length > 0) {
				this.tabPanel.setActiveTab(this.tabPanel.widgetContainerList[0]);
			}
		},
		sheetremoveothers: function(tab){		
			//removes all widget container except the selected one from the internal list
			for (var i=0; i < this.tabPanel.widgetContainerList.length; i++){
				var tmpWc = this.tabPanel.widgetContainerList[i];
				if (tmpWc.id !== tab.id){
					this.tabPanel.hideElementsOfTab(tmpWc);
					this.tabPanel.widgetContainerList.remove(tmpWc);
				}
			}
			if (this.tabPanel.widgetContainerList.length > 0) {				
				this.tabPanel.setActiveTab(this.tabPanel.widgetContainerList[0]);
			}
		},
		sheetremoveall: function(tab){		
			//remove all widget containers from the internal list
			for (var i=0; i < this.tabPanel.widgetContainerList.length; i++){
				var tmpWc = this.tabPanel.widgetContainerList[i];	
				this.tabPanel.hideElementsOfTab(tmpWc);
				this.tabPanel.widgetContainerList.remove(tmpWc);		
			}
			if (this.tabPanel.widgetContainerList.length > 0) {
				this.tabPanel.setActiveTab(this.tabPanel.widgetContainerList[0]);
			}
		}
		}})

          
	, config:{
		cls: 'sheetsContainer',
		border: false,
		tabPosition: 'bottom',        
        enableTabScroll:true,
        defaults: {autoScroll:true},
        frame: true,
    	index: 0,
    	sheetId: 0
	}


	/**
	 * @property {Sbi.data.AssociationEditorWizardPanel} editorMainPanel
	 *  Container of the wizard panel
	 */
	, containerPanelList: null

	, constructor : function(config) {
		Sbi.trace("[SheetsContainerPanel.constructor]: IN");
		
		this.initConfig(config);
		
		this.callParent(arguments);
		
		this.initPanel();
		this.initEvents();
		
		this.addEvents("sheetremove", "sheetremoveothers", "sheetremoveall");

		Sbi.trace("[SheetsContainerPanel.constructor]: OUT");
	}	

	
	// -----------------------------------------------------------------------------------------------------------------
    //  methods
	// -----------------------------------------------------------------------------------------------------------------

	, addTab: function(sheetConf){
		//add a new tab		
		this.suspendEvents();
		if (this.widgetContainerList == null) this.widgetContainerList = new Array(); 
			
		this.remove('addTab');

		var addPanel = {
			id: 'addTab',
	        title: '<br>',
	        iconCls: 'newTabIcon'
		};
		

		var conf = {};
		if(Sbi.isValorized(this.lastSavedAnalysisState) && !Sbi.isValorized(sheetConf) ) {
			conf = this.lastSavedAnalysisState.widgetsConf;
		}		
		 
		var isFirstTab = false;
		var loadingFromTemplate = Sbi.isValorized(conf) && !Sbi.isValorized(sheetConf);
//		if (Sbi.isValorized(conf) && !Sbi.isValorized(sheetConf)){
		if (loadingFromTemplate){
			//execution: cycle for view all tabs
			for (var j=0; j<conf.length; j++){
				var sheetConf = conf[j].sheetConf;
				var widgetContainer = new Sbi.cockpit.core.WidgetContainer(sheetConf);

				var sheet =  widgetContainer; 
				sheet.id = this.sheetId; //conf[j].sheetId;
				sheet.title= conf[j].sheetTitle;
				sheet.index = this.index;
		        sheet.closable=  ((Sbi.config.environment === 'MYANALYSIS' && Sbi.user.userId == Sbi.config.docAuthor) || (Sbi.config.documentMode === 'EDIT'));
		        sheet.bodyCls= this.bodyCls;   
		        // adds the newest widgetContainer into the global list
		        this.widgetContainerList.push(sheet);
		        var tab = this.add(sheet);
		        if (j<conf.length-1) {
		        	//update index for the default name
		        	this.index = this.index + 1;		        	
		        }		        	       
		        //update the id of the sheet (unique for all sheets)
		        this.sheetId = this.sheetId +1;
		        
		        //force loading all widgets for all sheet
		        this.setActiveTab(sheet.id);	
		        isFirstTab = false;
			}
		}else {
			//creation: add one tab anytime
			var widgetContainer = new Sbi.cockpit.core.WidgetContainer(conf);
			
			var sheet =  widgetContainer; 
			sheet.id = this.sheetId;
			sheet.title= 'Sheet ' + this.sheetId;
			sheet.index = this.index;
	        sheet.closable= ((Sbi.config.environment === 'MYANALYSIS' && Sbi.user.userId == Sbi.config.docAuthor) || (Sbi.config.documentMode === 'EDIT'));
	        sheet.bodyCls= this.bodyCls; 
	        // adds the newest widgetContainer into the global list
	        this.widgetContainerList.push(sheet);
	        var tab = this.add(sheet);
	        this.index = this.index + 1;
	        this.sheetId = this.sheetId +1;	 
	        this.setActiveTab(tab.id);
	        if (!isFirstTab && tab.id == 1) isFirstTab = true;
		}
        
		//In MyAnalysis the default behaviour is to open the document as editable if the user is the author
	    if ((Sbi.config.environment === 'MYANALYSIS' && Sbi.user.userId == Sbi.config.docAuthor) || (Sbi.config.documentMode === 'EDIT')){
	    	this.add(addPanel);   
	    }

//	    if (this.getActiveTab()==null){
	    if (isFirstTab || loadingFromTemplate || this.getActiveTab()==null){	    	
	    	this.setActiveTab(0);
	    	this.widgetContainer = this.activeTab; //update of main widget manager with the newest one
	    }
	    
	    this.resumeEvents();
	     
	    tab.on('beforeClose',function(panel){
			Ext.MessageBox.confirm(
					LN('sbi.cockpit.msg.deletetab.title'),
					LN('sbi.cockpit.msg.deletetab.msg'),            
		            function(btn, text) {
		                if (btn=='yes') {		                
		                	this.hideElementsOfTab(panel);
		                	this.widgetContainerList.remove(panel);
		                	this.remove(panel);		                	
		                }
		            },
		            this
				);
			return false;
	    }, this);
	    
	}
	 		
	
	/**
	 * Update the size of internal components after a resize of the page
	 */
	, updateSheetsContainerSize: function(ratioW, ratioH){
		if(this.widgetContainerList){
			for(var i=0; i<this.widgetContainerList.length;i++){
				this.widgetContainerList[i].updateWidgetContainerSize(ratioW, ratioH);
			}
		}
	}
	
	, manageVisibilityTabsWindows: function(tab){	
		
		Sbi.trace("[SheetsContainerPanel.manageVisibilityTabsWindows]: IN");
		this.suspendEvents();
		
		
		for (var i=0; i<this.widgetContainerList.length; i++){
			var currentTab = this.widgetContainerList[i];
			
			//hides all windows of other sheets
			if (currentTab.id !== tab.id){
				if (Sbi.isValorized(currentTab.getComponents())){					
					this.hideElementsOfTab(currentTab); 
				}
			}
			
			//shows all windows of selected sheets
			if (currentTab.id === tab.id){
				if (Sbi.isValorized(currentTab.getComponents())){					
					this.showElementsOfTab(currentTab); 
				}
			}
		}
		
		this.resumeEvents();
		Sbi.trace("[SheetsContainerPanel.manageVisibilityTabsWindows]: OUT");
	}
	
	// Hides  all elements (windows) of input tab
	, hideElementsOfTab: function(tab){
		var tabWindows =  tab.getComponents();
		if (Sbi.isValorized(tabWindows) && tabWindows.length>0 ){						
			for (var j=0; j<tabWindows.length; j++){
				var w = tabWindows[j];
				w.hide();
			}
		}
	}
	
	// Removes  all elements (windows) of input tab
	, removeElementsOfTab: function(tab){
		var tabWindows =  tab.getComponents();
		if (Sbi.isValorized(tabWindows) && tabWindows.length>0 ){						
			for (var j=0; j<tabWindows.length; j++){
				var w = tabWindows[j];
				w.remove();
			}
		}
	}
	
	// Shows  all elements (windows) of input tab
	, showElementsOfTab: function(tab){
		var tabWindows =  tab.getComponents();
		if (Sbi.isValorized(tabWindows) && tabWindows.length>0 ){						
			for (var j=0; j<tabWindows.length; j++){
				var w = tabWindows[j];
				//check x, y coordinates before showing widget:
				var widgetConfiguration = w.getWidgetConfiguration();
				if (Sbi.isValorized(widgetConfiguration) && 
						Sbi.isValorized(widgetConfiguration.wlayout)){
					var wl = widgetConfiguration.wlayout.region;
					if (w.x== 0 && Sbi.isValorized(wl.x) && typeof wl.x == 'string')					
						w.setX( tab.convertToAbsoluteY(wl.x));
					if (w.y== 0 && Sbi.isValorized(wl.y) && typeof wl.y == 'string')
						w.setY( tab.convertToAbsoluteY(wl.y));
				}		
				w.show();

			}
		}
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[SheetsContainerPanel.init]: IN");
		//add init methods here
		Sbi.trace("[SheetsContainerPanel.init]: OUT");
	}

	, initEvents: function() {
		Sbi.trace("[SheetsContainerPanel.initEvents]: IN");
		
		
		this.on('render',function(){ 			
							this.index = 1;
							this.addTab();
						},
						this);
		
		this.on('afterlayout',function(){ 				
			this.manageVisibilityTabsWindows(this.activeTab);
		},
		this);

		Sbi.trace("[SheetsContainerPanel.initEvents]: OUT");
	}
	
	, initPanel: function(){
		Sbi.trace("[SheetsContainerPanel.initPanel]: IN");
		
		this.sheetId = 1;
		
		this.on('tabchange',function(tabPanel, tab){
	    	if(tab==null || tab.id=='addTab'){
	    		this.addTab('addTab');
//	    	    tabPanel.setActiveTab(tabPanel.items.length-2);
//	    	    tabPanel.setActiveTab(tabPanel.items.length-1);
	    	}else{
	    		this.manageVisibilityTabsWindows(tab);
	    		//update contents of new tab
	    		var associationGroups = Sbi.storeManager.getAssociationGroups();
				var selections = this.widgetContainer.getWidgetManager().getSelectionsByAssociations();
				
		    	if(Sbi.isValorized(associationGroups) && Sbi.isValorized(selections)) {
		    		for (s in selections){
		    			var actualSelection = selections[s];
		    			if (Sbi.isValorized(actualSelection) && actualSelection.length>0){
							for (var i=0; i < associationGroups.length; i++ ){
								var associationGroup = associationGroups[i];
								this.widgetContainer.widgetManager.applySelectionsOnAssociationGroup(associationGroup);	
							}
		    			}
		    		}
		    	}
	    	}

	    	this.widgetContainer = this.activeTab; //update of main widget manager with the active one
	    },this);
				
		
		Sbi.trace("[SheetsContainerPanel.initPanel]: OUT");
	}
	
});