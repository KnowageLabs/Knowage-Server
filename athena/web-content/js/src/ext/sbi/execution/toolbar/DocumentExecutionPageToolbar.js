/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.DocumentExecutionPageToolbar = function(config) {	
	// init properties...
	var defaultSettings = {
		// public
		documentMode: 'INFO'
		, expandBtnVisible: true
		, callFromTreeListDoc: false
		, height: 25
		// private
	};

	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.toolbar && Sbi.settings.execution.toolbar.documentexecutionpagetoolbar) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.toolbar.documentexecutionpagetoolbar);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	this.addEvents('beforeinit', 'click', 'showmask');
	this.initServices();
	this.init();
	
	Sbi.execution.toolbar.DocumentExecutionPageToolbar.superclass.constructor.call(this, c);
	
	
};

/**
 * @class Sbi.execution.toolbar.DocumentExecutionPageToolbar
 * @extends Ext.Toolbar
 * 
 * The toolbar used by DocumentExecutionPage. The content change accordingly to document's visualization mode (i.e. INFO,
 * VIEW, EDIT).
 */

/**
 * @cfg {Object} config Configuration object.
 * @cfg {Number} [config.documentMode=0]
 * @cfg {Number} [config.expandBtnVisible=1]
 * @cfg {Number} [config.callFromTreeListDoc=2]
 */
Ext.extend(Sbi.execution.toolbar.DocumentExecutionPageToolbar, Ext.Toolbar, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
	
	/**
     * @property {String} documentMode
     * Define which facet of the current documents shown to the user. The content of the toolbar change accordingly. 
     * There are three possibilities mode:
     * - INFO: shows document's metadata and shortcuts 
     * - VIEW: show the executed document
     * - EDIT: show the document in edit mode
     * 
     * The default is INFO
     */ 
	, documentMode: null
	
	/**
     * @property {Boolean} expandBtnVisible
     * True if expand button is visible, false otherwise. 
     * 
     * The default is true.
     */ 
	, expandBtnVisible: null
	
	/**
     * @property {Object} controller
     * The controller object. Must implement the following methods:
     *  - refreshDocument()
     *  - executeDocument(Object executionInstance)
     *  - showInfo()
     *  - openSubobjectSelectionWin()
     *  - openSnapshotSelectionWin()
     *  - getFrame()
     *  - getParameterValues()
     */ 
	, controller: null
	
	/**
     * @property {Object} callFromTreeListDoc
     * Specify if the document has been executed from document browser or from analytical documents'tree (i.e. old admin GUI)
     * 
     * The default is false.
     */ 
	, callFromTreeListDoc: false
	
	
	, executionInstance: null
	
	
	, fromMyAnalysis: false
   
	, isInsert: false
	
	, exportersMenu : null
	
	// =================================================================================================================
	// CONSTANTS
	// =================================================================================================================
	, PUBLIC_USER: 'public_user'
	
	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method
	 * 
	 * @return {String} return the url of the executed document. Null if #controller is null or #controller not implements
	 * methods <code>getFrame</code>. Note: this method is called by the Sbi.execution.toolbar.ExportersMenu in order to create
	 * the exportation url
	 */
	, getDocumentUrl: function() {
		var url = null;
		if(this.controller && this.controller.getFrame) {
			var frame = this.controller.getFrame();
		    url = frame.getDocumentURI();
		}
		return url;		
	}

	/**
	 * @method
	 * 
	 * @return {String} return the window tha conatins the executed document. Null if #controller is null or #controller not implements
	 * methods <code>getFrame</code>. Note: this method is called by the Sbi.execution.toolbar.ExportersMenu in order to create
	 * the exportation url
	 */
	, getDocumentWindow: function() {
		var window = null;
		if(this.controller && this.controller.getFrame) {
			var frame = this.controller.getFrame();
		    window = frame.getWindow();
		}
		return window;		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - showSendToForm: ... (by default SHOW_SEND_TO_FORM)
	 *    - saveIntoPersonalFolder: ... (by default SAVE_PERSONAL_FOLDER)
	 *    - getNotesService: ... (by default GET_NOTES_ACTION)
	 *    - updateDocumentService: ... (by default SAVE_DOCUMENT_ACTION)
	 *    
	 */
	, initServices: function() {
	
		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
		
		this.services = this.services || new Array();
		
		this.services['showSendToForm'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SHOW_SEND_TO_FORM'
			, baseParams: params
		});
		
		this.services['saveIntoPersonalFolder'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_PERSONAL_FOLDER'
			, baseParams: params
		});
		
		this.services['getNotesService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_NOTES_ACTION'
			, baseParams: params
		});
	
		
		var updateDocParams = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: 'DOC_UPDATE'};
		this.services['updateDocumentService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_DOCUMENT_ACTION'
			, baseParams: updateDocParams
		});
	}
	
	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	, init: Ext.emptyFn
	
	// -----------------------------------------------------------------------------------------------------------------
    // synchronization methods
	// -----------------------------------------------------------------------------------------------------------------
	, synchronize: function(controller, executionInstance) {
		Sbi.trace('[DocumentExecutionPageToolbar.synchronize]: IN');
		this.controller = controller;
	    this.executionInstance = executionInstance;
	
		// if toolbar is hidden, do nothing
		if (this.toolbarHiddenPreference) return;
		
		this.removeAllButtons();
		
		this.fireEvent('beforeinit', this);
		
		this.addFill();
		
		Sbi.trace('[DocumentExecutionPageToolbar.synchronize]: Document mode is equal to [' + this.documentMode + ']');
		if (this.documentMode === 'INFO') {
			this.addButtonsForInfoMode();
		} else if (this.documentMode === 'VIEW') {
			this.addButtonsForViewMode();
		} else {
			if(this.isCockpitEngine()){
				this.addButtonsForCockpitEditMode();
			} else {
				this.addButtonsForEditMode();
			}
		}
		Sbi.trace('[DocumentExecutionPageToolbar.synchronize]: OUT');
   }
	
	// -----------------------------------------------------------------------------------------------------------------
	// add/remove buttons methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Removes all buttons from the toolbar
	 */
	, removeAllButtons: function() {
		this.items.each( function(item) {
			this.items.remove(item);
            item.destroy();           
        }, this); 
	}
	
	/**
	 * @method 
	 * 
	 * Add buttons specific for INFO mode 
	 */
	, addButtonsForInfoMode: function () {
		Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForInfoMode]: IN');
		
		var drawRoleBack = false;
		
		if (this.executionInstance.isPossibleToComeBackToRolePage == undefined || this.executionInstance.isPossibleToComeBackToRolePage === true) {
			this.addButton(new Ext.Toolbar.Button({
				iconCls: 'icon-back' 
				, tooltip: LN('sbi.execution.parametersselection.toolbar.back')
			    , scope: this
			    , handler : function() {
			    	this.fireEvent('click', this, "backToRolePage");
			    }
			}));
			if (this.toolbar !== undefined) this.toolbar.addSeparator();
			drawRoleBack = true;
		}
		
		// 20100505
		if (this.callFromTreeListDoc == true && drawRoleBack == false) {
			this.addButton(new Ext.Toolbar.Button({
				iconCls: 'icon-back' 
				, tooltip: LN('sbi.execution.executionpage.toolbar.documentView')
				, scope: this
				, handler : function() {
					this.fireEvent('click', this, "backToAdminPage");
				}
			}));
		}
		
		
//		// if document is QBE datamart and user is a read-only user, he cannot execute main document, but only saved queries.
//		// If there is a subobject preference, the execution button starts the subobject execution
//		if (
//				this.executionInstance.document.typeCode != 'DATAMART' || 
//				(
//					Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality') || 
//					(this.preferenceSubobjectId !== undefined && this.preferenceSubobjectId !== null)
//				)
//			) {
//			this.addSeparator();
//			this.addButton(new Ext.Toolbar.Button({
//				iconCls: 'icon-execute'
//				, tooltip: LN('sbi.execution.parametersselection.toolbar.next')
//				, scope: this
//				, handler : function() {
//					if (this.preferenceSubobjectId !== undefined && this.preferenceSubobjectId !== null) {
//						this.executionInstance.SBI_SUBOBJECT_ID = this.preferenceSubobjectId;
//						this.controller.refreshDocument();
//					} else {
//						this.controller.executeDocument(this.executionInstance);
//					}
//				}
//			}));
//		}
		
		Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForInfoMode]: OUT');
	}
	
	/**
	 * @method 
	 * 
	 * Add buttons specific for VIEW mode 
	 */
	, addButtonsForViewMode: function () {
		   
		   Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForViewMode]: IN');
			
		  
		   // BACK TO ADMIN PAGE
		   if (this.callFromTreeListDoc == true) {
				this.addButton(new Ext.Toolbar.Button({
					iconCls: 'icon-back' 
					, tooltip: LN('sbi.execution.executionpage.toolbar.documentView')
					, scope: this
					, handler : function() {
						this.fireEvent('click', this, "backToAdminPage");
					}
				}));
		   }

		   if (this.executionInstance.document && this.executionInstance.document.decorators &&  
				   this.executionInstance.document.decorators.isSavable && 
				   (this.executionInstance.document.typeCode === 'MAP' 
					   || this.executionInstance.document.typeCode === 'WORKSHEET' 
						   || this.executionInstance.document.typeCode === 'DATAMART'
							   || this.executionInstance.document.typeCode === 'SMART_FILTER') && 
				   Sbi.user.userId !== this.PUBLIC_USER) {
			   
			   var saveButtonHidden = this.executionInstance.document.typeCode === 'MAP' ? 
					   !(this.executionInstance.document.creationUser == Sbi.user.userId)
					   : true;
					   
			   var conf = {
				   iconCls: 'icon-save' 
					   , tooltip: LN('sbi.execution.executionpage.toolbar.save')
					   , scope: this
					   , handler : this.saveButtonHandler
					   , hidden: saveButtonHidden
			  };
			 
			  this.saveButton = new Ext.Toolbar.Button(conf);
			  this.addButton(this.saveButton);
		   }
	   			
			if (Sbi.user.userId !== this.PUBLIC_USER && 
				Sbi.user.functionalities.contains('EditWorksheetFunctionality') && this.executionInstance.document.typeCode === 'WORKSHEET') {
				this.addButton(new Ext.Toolbar.Button({
					iconCls: 'icon-edit' 
					, tooltip: LN('sbi.execution.executionpage.toolbar.edit')
				    , scope: this
				    , handler : this.startWorksheetEditing	
				}));
			}
			
			if(this.isCockpitEngine() && 
					Sbi.config.serviceRegistry.baseParams.SBI_ENVIRONMENT === 'DOCBROWSER' && 
					Sbi.user.userId === this.executionInstance.document.creationUser) {
				
				this.addButton(new Ext.Toolbar.Button({
					iconCls: 'icon-edit' 
					, tooltip: LN('sbi.execution.executionpage.toolbar.edit')
				    , scope: this
				    , handler : this.startCockpitEditing	
				}));
			}
		
			this.addButton(new Ext.Toolbar.Button({
				iconCls: 'icon-refresh' 
				, tooltip: LN('sbi.execution.executionpage.toolbar.refresh')
			    , scope: this
			    , handler : function() {
						// save parameters into session
						// if type is QBE inform user that will lose configurations
						if(this.executionInstance.document.typeCode == 'DATAMART'){
							if(Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality') && Sbi.user.functionalities.contains('SaveSubobjectFunctionality')){
								
								Ext.MessageBox.confirm(
	    						    LN('sbi.generic.warning'),
	            					LN('sbi.execution.executionpage.toolbar.qberefresh'),            
	            					function(btn, text) {
	                					if (btn=='yes') {
											this.controller.refreshLastExecution();
	                					}
	            					},
	            					this
									);
								}else{
									//user who cannot build qbe queries
									this.controller.refreshLastExecution();
								}
						} // it 's not a qbe
						else {
							this.controller.refreshLastExecution();
					}
				}			
			}));
			
			this.addSeparator();
			
			this.addFileMenu();
			this.addInfoMenu();
			this.addShortcutsMenu();
			
			Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForViewMode]: OUT');	   
	}
	
	/**
	 * @method 
	 * 
	 * Add buttons specific for EDIT mode (at the moment used by Worksheet documents only)
	 */
	, addButtonsForEditMode: function () {

		Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForEditMode]: IN');
		
		   
		   if (this.executionInstance.document && this.executionInstance.document.decorators &&  this.executionInstance.document.decorators.isSavable) {
			   this.addButton(new Ext.Toolbar.Button({
				   iconCls: 'icon-save' 
					   , tooltip: LN('sbi.execution.executionpage.toolbar.save')
					   , scope: this
//					   , handler : this.saveWorksheet
//					   , handler : this.saveDocumentAs
					   , handler : function(){this.isInsert=false;this.saveDocumentAs()}
			   }));
		   }

		   this.addButton(new Ext.Toolbar.Button({
			   iconCls: 'icon-saveas' 
				   , tooltip: LN('sbi.execution.executionpage.toolbar.saveas')
				   , scope: this
//				   , handler : this.saveDocumentAs
				   , handler : function(){this.isInsert=true;this.saveDocumentAs()}
		   }));

		   if(this.executionInstance.document.exporters.length > 0){
			   var menu = new Sbi.execution.toolbar.ExportersMenu({
				    toolbar: this
					, executionInstance: this.executionInstance
				});
				
			   if(menu.isEmpty() === false) {
		       this.addButton(new Ext.Toolbar.MenuButton({
				   tooltip: 'Exporters'
				   , path: 'Exporters'	
				   , iconCls: 'icon-export' 	
				   , width: 15
				   , cls: 'x-btn-menubutton x-btn-text-icon bmenu '
				   , menu: menu
			    }));	
			   }
		   }
		   
		   this.addSeparator();
		   this.addButton(new Ext.Toolbar.Button({
			   iconCls: 'icon-view' 
				   , tooltip: LN('sbi.execution.executionpage.toolbar.view')
				   , scope: this
				   , handler : this.stopWorksheetEditing	
		   }));
		   
		   Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForEditMode]: OUT');	
	   }
	
	/**
	 * @method 
	 * 
	 * Add buttons specific for Cockpit EDIT mode (used by Cockpit documents only)
	 */
	, addButtonsForCockpitEditMode: function () {

		Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForCockpitEditMode]: IN');
		   
		   this.addButton(new Ext.Toolbar.Button({
			   iconCls: 'icon-view' 
				   , tooltip: LN('sbi.execution.executionpage.toolbar.view')
				   , scope: this
				   , handler : this.stopCockpitEditing	
		   }));
		   
		   Sbi.trace('[DocumentExecutionPageToolbar.addButtonsForCockpitEditMode]: OUT');	
	   }
	
	
	// -----------------------------------------------------------------------------------------------------------------
	// add/remove menus
	// -----------------------------------------------------------------------------------------------------------------  
    
    /**
	 * @method
	 * 
	 * Create the file menu
	 */   
    , addFileMenu: function() {
			
    	var menuItems = new Array();
    	
    	var baseMenuItemConfig = {
			text: LN('sbi.execution.GenericExport')
			, group: 'group_2'//ok, where's group_1?
			, iconCls: 'icon-pdf'  // use a generic icon here
			, scope: this
			, width: 15
			, handler : Ext.emptyFn
			, href: ''   
		}
    	// get menu items config
    	var itemConfig = null;
    	
    	// PRINT
    	itemConfig = Ext.apply({}, {
			text: LN('sbi.execution.executionpage.toolbar.print')
			, iconCls: 'icon-print'
			, handler : this.printExecution
        }, baseMenuItemConfig);    	
    	menuItems.push(	
			new Ext.menu.Item(itemConfig)
		); 
    	
    	// EXPORT
		if(this.executionInstance.document.exporters){
			var m = new Sbi.execution.toolbar.ExportersMenu({
			    toolbar: this
				, executionInstance: this.executionInstance
			});
			
			this.exportersMenu = m;
			
			menuItems.push({
				text: LN('sbi.execution.executionpage.toolbar.export')
				, path: 'Export'	
				, iconCls: 'icon-export' 	
	            , menu: m.isEmpty()? undefined: m
	            , disabled: m.isEmpty()
			});						
		}
		
    	
    	// SEND BY MAIL
    	if (Sbi.user.functionalities.contains('SendMailFunctionality') && !this.executionInstance.SBI_SNAPSHOT_ID
				&& this.executionInstance.document.typeCode == 'REPORT') {
	    	itemConfig = Ext.apply( {}, {
				text: LN('sbi.execution.executionpage.toolbar.send')
				, iconCls: 'icon-send-mail' 
				, handler : this.sendExecution
	        }, baseMenuItemConfig);    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
    	}
    	
    	// COPY IN MY FOLDER
    	if (Sbi.user.functionalities.contains('SaveIntoFolderFunctionality') && !this.executionInstance.SBI_SNAPSHOT_ID) {
	    	itemConfig = Ext.apply({}, {
				text: LN('sbi.execution.executionpage.toolbar.saveintopersonalfolder')
				, iconCls: 'icon-save-into-personal-folder' 
				, handler : this.saveExecution
	        }, baseMenuItemConfig);    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
    	}    	
    	
    	// create menu    	
    	var menu = new Ext.menu.Menu({
			items: menuItems    
		});	
    	
    	// create menu button
		var menuButton = new Ext.Toolbar.MenuButton({
			text: 'File'
			, tooltip: 'File'
			, path: 'File'	
			, width: 15
			, cls: 'x-btn-menubutton x-btn-text-icon bmenu '
			, menu: menu
		});
		
		this.add(menuButton);
	}
	   
    /**
	 * @method
	 * 
	 * Create the file menu
	 */   
    , addInfoMenu: function() {
			
    	var menuItems = new Array();
    	
    	var baseMenuItemConfig = {
			text: LN('sbi.execution.GenericExport')
			, group: 'group_2'//ok, where's group_1?
			, iconCls: 'icon-pdf'  // use a generic icon here
			, scope: this
			, width: 15
			, handler : Ext.emptyFn
			, href: ''   
		}
    	// get menu items config
    	var itemConfig = null;
    	
    	// METADATA
    	if (Sbi.user.functionalities.contains('SeeMetadataFunctionality') && !this.executionInstance.SBI_SNAPSHOT_ID) {
	    	itemConfig = Ext.apply({}, {
				text: LN('sbi.execution.executionpage.toolbar.metadata')
				, iconCls: 'icon-metadata' 
				, handler : this.metaExecution
	        }, baseMenuItemConfig);    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
    	}
    	
    	// NOTE
    	if (Sbi.user.functionalities.contains('SeeNotesFunctionality') && !this.executionInstance.SBI_SNAPSHOT_ID) {
    		this.getNoteIcon();
	    	itemConfig = Ext.apply({}, {
	    		id: 'noteIcon' // used by method getNoteIcon to replace the icon
				, text: LN('sbi.execution.executionpage.toolbar.annotate')
				, iconCls: 'icon-no-notes'
				, handler : this.annotateExecution
	        }, baseMenuItemConfig);    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
    	}
    	
    	// RANK
    	itemConfig = Ext.apply({}, {
			text: LN('sbi.execution.executionpage.toolbar.rating')
			, iconCls: 'icon-rating' 
			, handler : this.rateExecution
        }, baseMenuItemConfig);    	
    	menuItems.push(	
			new Ext.menu.Item(itemConfig)
		); 
    	
    	// create menu    	
    	var menu = new Ext.menu.Menu({
			items: menuItems    
		});	
    	
    	// create menu button
		var menuButton = new Ext.Toolbar.MenuButton({
			text: 'Info'
			, tooltip: 'Info'
			, path: 'File'	
			//, iconCls: 'icon-export' 	
			, width: 15
			, cls: 'x-btn-menubutton x-btn-text-icon bmenu '
			, menu: menu
		});
		
		this.add(menuButton);
	}
	   
    , addShortcutsMenu: function() {
    	var menuItems = new Array();
    	
    	var baseMenuItemConfig = {
			text: LN('sbi.execution.GenericExport')
			, group: 'group_2'//ok, where's group_1?
			, iconCls: 'icon-pdf'  // use a generic icon here
			, scope: this
			, width: 15
			, handler : Ext.emptyFn
			, href: ''   
		}
    	// get menu items config
    	var itemConfig = null;

    	// ADD/VIEW favorites
    	if (Sbi.user.functionalities.contains('SaveRememberMeFunctionality') && !this.executionInstance.SBI_SNAPSHOT_ID) {
    		itemConfig = Ext.apply({}, {
				text: LN('sbi.execution.executionpage.toolbar.showbookmark')
				, iconCls: 'icon-show-bookmark' 
				, handler : function() {this.controller.openFavouritesWin();} // function(){alert("Function not implemented yet");}
	        }, baseMenuItemConfig);    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
	    	
    		itemConfig = Ext.apply(baseMenuItemConfig, {
				text: LN('sbi.execution.executionpage.toolbar.addbookmark')
				, iconCls: 'icon-add-bookmark'
				, handler: this.bookmarkExecution
	        });    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
    	}
    	
    	menuItems.push('-'); 
    	
    	
    	
    	// ADD/VIEW Customized view
    	itemConfig = Ext.apply(baseMenuItemConfig, {
			text: LN('sbi.execution.executionpage.toolbar.showview')
			, iconCls: 'icon-show-subobject' 
			, handler : function() {this.controller.openSubobjectSelectionWin();}
        });    	
    	menuItems.push(	
			new Ext.menu.Item(itemConfig)
		); 
    	
		if (this.executionInstance.document.typeCode === 'DATAMART') {
			itemConfig = Ext.apply(baseMenuItemConfig, {
				text: LN('sbi.execution.executionpage.toolbar.saveview')
				, iconCls: 'icon-add-subobject' 
				, handler : this.saveQbe	
	        });    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
		} else if (this.executionInstance.document.typeCode === 'SMART_FILTER') {
			itemConfig = Ext.apply(baseMenuItemConfig, {
				text: LN('sbi.execution.executionpage.toolbar.saveview')
				, iconCls: 'icon-add-subobject' 
//				, handler : this.saveDocumentAs
				, handler : function(){this.isInsert=true;this.saveDocumentAs()}
	        });    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
		} else if (this.executionInstance.document.typeCode === 'OLAP' && this.executionInstance.document.engine.indexOf("What")>=0){
			itemConfig = Ext.apply(baseMenuItemConfig, {
				text: LN('sbi.execution.executionpage.toolbar.saveview')
				, iconCls: 'icon-add-subobject' 
				, handler : this.saveSubObject.createDelegate(this, ["whatIfPanel"])
	        });    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
		} else if ((this.executionInstance.document.typeCode === 'OLAP' && this.executionInstance.document.engine.indexOf("What")<0) 
			    || this.executionInstance.document.typeCode === 'MAP') {
			itemConfig = Ext.apply(baseMenuItemConfig, {
				text: LN('sbi.execution.executionpage.toolbar.saveview')
				, iconCls: 'icon-add-subobject' 
				, handler : function(){alert("Use the save button conatined in the executed document");}
	        });    	
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
		} else {
			itemConfig = Ext.apply({}, {
				text: LN('sbi.execution.executionpage.toolbar.saveview')
				, iconCls: 'icon-add-subobject' 
				, handler : function(){alert("Function not vailable for this type of document");}
				, disabled: true
	        }, baseMenuItemConfig);    
	    	menuItems.push(	
				new Ext.menu.Item(itemConfig)
			); 
		}
    	// TODO add SAVE SUBOBJECT item
		
		menuItems.push('-'); 
    	
    	// VIEW scheduled execution
    	itemConfig = Ext.apply({}, {
			text: LN('sbi.execution.executionpage.toolbar.showscheduled')
			, iconCls: 'icon-execute-snapshot' 
			, handler :  function() {
		    	this.controller.openSnapshotSelectionWin();
		    }
        }, baseMenuItemConfig);    	
    	menuItems.push(	
			new Ext.menu.Item(itemConfig)
		); 
    	
    	// create menu    	
    	var menu = new Ext.menu.Menu({
			items: menuItems    
		});	
    	
    	// create menu button
		var menuButton = new Ext.Toolbar.MenuButton({
			text: 'Shortcuts'
			, tooltip: 'Shortcuts'
			, path: 'Shortcuts'	
			//, iconCls: 'icon-export' 	
			, width: 15
			, cls: 'x-btn-menubutton x-btn-text-icon bmenu '
			, menu: menu
		});
		
		this.add(menuButton);
    }
    
    , saveSubObject: function(container){
    
    	var thisPanel = this;
    	var window = new Sbi.execution.toolbar.SaveSubObjectWindow();
    	window.show();
    	window.on("save",function(state){
			var iframe = thisPanel.controller.getFrame().getWindow();
			iframe[container].saveSubObject(state.name, state.description, state.scope);
			window.destroy();
    	});
    	
    } 

	// -----------------------------------------------------------------------------------------------------------------
	// private methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, getNoteIcon: function () {
  		Ext.Ajax.request({
  	        url: this.services['getNotesService'],
  	        params: {SBI_EXECUTION_ID: this.executionInstance.SBI_EXECUTION_ID, MESSAGE: 'GET_LIST_NOTES'},
  	        success : function(response, options) {
	      		if(response !== undefined && response.responseText !== undefined) {
	      			var content = Ext.util.JSON.decode( response.responseText );		 
	      			//checks if documents has some note for change icon     			
	      			if (content !== undefined && content.totalCount > 0) {		      		
	      				var el = Ext.getCmp('noteIcon');                
	      				el.setIconClass('icon-notes');
	      			}
	      		} else {
	      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
	      		}
  	        },
  	        scope: this,
  			failure: Sbi.exception.ExceptionHandler.handleFailure      
  		});
	}
	
	, rateExecution: function() {
		this.win_rating = new Sbi.execution.toolbar.RatingWindow({'OBJECT_ID': this.executionInstance.OBJECT_ID});
		this.win_rating.show();
	}
	
	, printExecution: function() {
		this.controller.getFrame().print();
	}
	
	, sendExecution: function () {
		var sendToIframeUrl = this.services['showSendToForm'] 
		        + '&objlabel=' + this.executionInstance.OBJECT_LABEL
		        + '&objid=' + this.executionInstance.OBJECT_ID
				+ '&' + Sbi.commons.Format.toStringOldSyntax(this.controller.getParameterValues());
		this.win_sendTo = new Sbi.execution.toolbar.SendToWindow({'url': sendToIframeUrl});
		this.win_sendTo.show();
	}
	
	, saveExecution: function () {
		Ext.Ajax.request({
	          url: this.services['saveIntoPersonalFolder'],
	          params: {documentId: this.executionInstance.OBJECT_ID},
	          success: function(response, options) {
		      		if (response.responseText !== undefined) {
		      			var responseText = response.responseText;
		      			var iconSaveToPF;
		      			var message;
		      			if (responseText=="sbi.execution.stpf.ok") {
		      				message = LN('sbi.execution.stpf.ok');
		      				iconSaveToPF = Ext.MessageBox.INFO;
		      			}
		      			if (responseText=="sbi.execution.stpf.alreadyPresent") {
		      				message = LN('sbi.execution.stpf.alreadyPresent');
		      				iconSaveToPF = Ext.MessageBox.WARNING;
		      			}
		      			if (responseText=="sbi.execution.stpf.error") {
		      				message = LN('sbi.execution.stpf.error');
		      				iconSaveToPF = Ext.MessageBox.ERROR;
		      			}
	
		      			var messageBox = Ext.MessageBox.show({
		      				title: 'Status',
		      				msg: message,
		      				modal: false,
		      				buttons: Ext.MessageBox.OK,
		      				width:300,
		      				icon: iconSaveToPF,
		      				animEl: 'root-menu'        			
		      			});
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
	          },
	          scope: this,
	  		  failure: Sbi.exception.ExceptionHandler.handleFailure      
	     });
	}
	
	, bookmarkExecution: function () {
		this.win_saveRememberMe = new Sbi.execution.toolbar.SaveRememberMeWindow({'SBI_EXECUTION_ID': this.executionInstance.SBI_EXECUTION_ID});
		this.win_saveRememberMe.show();
	}
	, annotateExecution: function () {
		this.win_notes = new Sbi.execution.toolbar.ListNotesWindow({'SBI_EXECUTION_ID': this.executionInstance.SBI_EXECUTION_ID});
		this.win_notes.show();
	}
	
	, metaExecution: function () {
		var subObjectId = this.executionInstance.SBI_SUBOBJECT_ID;
		if(subObjectId !== undefined){
			this.win_metadata = new Sbi.execution.toolbar.MetadataWindow({'OBJECT_ID': this.executionInstance.OBJECT_ID, 'SUBOBJECT_ID': subObjectId});
		}else{
			this.win_metadata = new Sbi.execution.toolbar.MetadataWindow({'OBJECT_ID': this.executionInstance.OBJECT_ID});
		}	
		this.win_metadata.show();
	}
	
	
   
   , startWorksheetEditing: function() {
	   this.documentMode = 'EDIT';
	   this.synchronize(this.controller, this.executionInstance);
	   var newUrl = this.changeDocumentExecutionUrlParameter('ACTION_NAME', 'WORKSHEET_START_EDIT_ACTION');
	   this.controller.getFrame().setSrc(newUrl);
   }
   
   , startCockpitEditing: function() {	
	   this.documentMode = 'EDIT';
	   this.synchronize(this.controller, this.executionInstance);
	   var newUrl = this.changeDocumentExecutionUrlParameter('documentMode', this.documentMode);
	   this.controller.getFrame().setSrc(newUrl);
   }
   
   
   , getDocumentTemplateAsString: function() {
		try {
			var thePanel = null;
			if(this.executionInstance.document.typeCode == 'WORKSHEET'){
				//the worksheet has been constructed starting from a qbe document
				thePanel = this.controller.getFrame().getWindow().qbe;
				if(thePanel==null){
					//the worksheet has been constructed starting from a smart filter document
					thePanel = this.controller.getFrame().getWindow().Sbi.formviewer.formEnginePanel;
				}
				if(thePanel==null){
					//the worksheet is alone with out the qbe
					thePanel = this.controller.getFrame().getWindow().workSheetPanel;
				}
			}else if(this.executionInstance.document.typeCode == 'DATAMART'){
				thePanel = this.controller.getFrame().getWindow().qbe;
			}else if(this.executionInstance.document.typeCode == 'SMART_FILTER'){
				thePanel = this.controller.getFrame().getWindow().Sbi.formviewer.formEnginePanel;
			}else if(this.executionInstance.document.typeCode == 'MAP'){
				thePanel = this.controller.getFrame().getWindow().geoReportPanel;
			}else{
				alert('Sorry, cannot perform operation. Invalid engine..');
				return null;
			}

			var template = thePanel.validate();	
			
			return template;
		} catch (err) {
			throw err;
		}
  }
   
   , getDocumentTemplateAsJSONObject: function() {
		var template = this.getDocumentTemplateAsString();
		if(template==null){
			return null;
		}
		var templateJSON = Ext.util.JSON.decode(template);
		return templateJSON;
  }
   
   , returnToMyAnalysis : function() {
	  
	   var url = Sbi.config.contextName + '/servlet/AdapterHTTP?ACTION_NAME=CREATE_DOCUMENT_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE';
	   window.location = url;
		
	}
   
   , saveDocumentAs: function () {

	   var templateJSON = this.getDocumentTemplateAsJSONObject();

		if(templateJSON==null){
			// if it is null validation error has been already showed in QbePanel
			//Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.worksheet.validation.error.text'),LN('sbi.worksheet.validation.error.title'));
		}else{

			var documentWindowsParams = this.getSaveDocumentWindowsParams(templateJSON);
			documentWindowsParams.fromMyAnalysis = this.fromMyAnalysis;
			documentWindowsParams.isInsert = this.isInsert;
			this.win_saveDoc = new Sbi.execution.SaveDocumentWindow(documentWindowsParams);		
			this.win_saveDoc.on('returnToMyAnalysis', this.returnToMyAnalysis, this);
			this.win_saveDoc.show();
		}
   } 
   
   , getSaveDocumentWindowsParams: function(templateJSON){
		var wkDefinition = templateJSON.OBJECT_WK_DEFINITION;
		var params = {
				'OBJECT_ID': this.executionInstance.OBJECT_ID,			
				'OBJECT_WK_DEFINITION': wkDefinition,
				'OBJECT_DATA_SOURCE': this.executionInstance.document.datasource,
				'OBJECT_FUNCTIONALITIES': this.executionInstance.document.functionalities,
				'OBJECT_SCOPE':  this.executionInstance.document.isPublic
			};
		if(this.executionInstance.document.typeCode == 'DATAMART' || this.executionInstance.document.typeCode == 'WORKSHEET'){
			params.OBJECT_QUERY = templateJSON.OBJECT_QUERY;
			params.OBJECT_FORM_VALUES = templateJSON.OBJECT_FORM_VALUES;  // the worksheet may be based on a smart filter document
			params.OBJECT_TYPE = 'WORKSHEET';
		}else if(this.executionInstance.document.typeCode == 'SMART_FILTER'){
			params.OBJECT_FORM_VALUES = templateJSON.OBJECT_FORM_VALUES;			
			params.OBJECT_TYPE = 'WORKSHEET';
		}else if(this.executionInstance.document.typeCode == 'MAP'){
			params.OBJECT_TYPE = 'MAP';
			params.typeid = 'GEOREPORT';
			params.OBJECT_TEMPLATE = Ext.util.JSON.encode(templateJSON);
		}
		params = Ext.apply(this.executionInstance, params);
		return params;
   }
   
   , stopWorksheetEditing: function() {
	   this.documentMode = 'VIEW';
	   this.synchronize(this.controller, this.executionInstance);
	   var newUrl = this.changeDocumentExecutionUrlParameter('ACTION_NAME', 'WORKSHEET_ENGINE_START_ACTION');
	   this.controller.getFrame().setSrc(newUrl);
   }
   
   , stopCockpitEditing: function() {
	   this.documentMode = 'VIEW';
	   this.synchronize(this.controller, this.executionInstance);
	   var newUrl = this.changeDocumentExecutionUrlParameter('documentMode', this.documentMode);
	   this.controller.getFrame().setSrc(newUrl);
   }
   
   , changeDocumentExecutionUrlParameter: function(parameterName, parameterValue) {
		var frame = this.controller.getFrame();
	    var docurl = frame.getDocumentURI();
	    var startIndex = docurl.indexOf('?')+1;
	    var endIndex = docurl.length;
	    var baseUrl = docurl.substring(0, startIndex);
	    var docurlPar = docurl.substring(startIndex, endIndex);
	    
	    docurlPar = docurlPar.replace(/\+/g, " ");
	    var parurl = Ext.urlDecode(docurlPar);
	    parurl[parameterName] = parameterValue;
	    parurl = Ext.urlEncode(parurl);
	    var endUrl = baseUrl +parurl;
	    return endUrl;
   }
	 
	 , saveQbe: function () {
		try {
			if (!Sbi.user.functionalities.contains('BuildQbeQueriesFunctionality')) {
				// If user is not a Qbe power user, he can only save worksheet
				this.isInsert = true;
				this.saveDocumentAs();
			} else {
				// If the user is a Qbe power user, he can save both current query and worksheet definition.
				// We must get the current active tab in order to understand what must be saved.
				var qbeWindow = this.controller.getFrame().getWindow();
				var qbePanel = qbeWindow.qbe;
				var anActiveTab = qbePanel.tabs.getActiveTab();
				var activeTabId = anActiveTab.getId();
				var isBuildingWorksheet = (activeTabId === 'WorksheetPanel');
				if (isBuildingWorksheet) {
					// save worksheet as document
					this.isInsert = true;
					this.saveDocumentAs();
				} else {
					// save query as customized view
					qbePanel.queryEditorPanel.showSaveQueryWindow();
				}
			}
		} catch (err) {
			alert('Sorry, cannot perform operation.');
			throw err;
		}
   }
	 
	 
	 , manageButton: function(button, property, value){
		 var aButton;
		 if(button == "saveworksheet" && this.saveButton){
			 aButton = this.saveButton;
		 }
		 if(aButton){
			 if(button == "saveworksheet"){
				 if(property=="visibility"){
					 if(value=="true"){
						 aButton.show();
					 }else{
						 aButton.hide();
					 }
				 }
			 } 
		 }

	 }
	
	 
	 ,
	 saveButtonHandler : function () {
		switch (this.executionInstance.document.typeCode) {
			case 'MAP':
			   this.isInsert = false;
			   this.saveDocumentAs();
			   return;
			case 'DATAMART':
			   this.isInsert = true;
			   this.saveDocumentAs();
			   return;
			case 'SMART_FILTER':
				   this.isInsert = true;
				   this.saveDocumentAs();
				   return;
			default:
				throw "Cannot save document type " + this.executionInstance.document.typeCode;
		}
	 }
	 
	 , isCockpitEngine: function() {
		 return (this.executionInstance.document.engine === 'Cockpit Engine');
	 }
	 
		// =================================================================================================================
		// EVENTS
		// =================================================================================================================
	 	
	 	//this.addEvents(
	 	/**
	     * @event beforeinit
	     * Fired when the toolbar is re-initiated (i.e. just after all buttons have been removed and before specific buttons 
		 * for the current documentMode are added. This event can be used to inject custom buttons in the toolbar. In 
		 * Sbi.execution.ExecutionPanel it is used to inject breadcrumbs buttons on the left part.
		 * 
	     * @param {Sbi.execution.toolbar.DocumentExecutionPageToolbar} this
	     */
	 	//  'beforeinit'
	 	/**
	     * @event click
	     * Fired when the user click on a button of the toolbar
		 * 
	     * @param {Sbi.execution.toolbar.DocumentExecutionPageToolbar} this
	     * @param {String} action 
	     */
	    //);
	 
	 
	 
});