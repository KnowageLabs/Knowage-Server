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

Ext.ns("Sbi.execution");

// TODO to be renamed in DocumentExecutionPage
Sbi.execution.ParametersSelectionPage = function(config, doc) {
	
	// init properties...
	var defaultSettings = {
		// public...
		labelAlign: 'left'
		, maskOnRender: true
		, parametersSliderWidth: 300
		, collapseParametersSliderOnExecution: false
		, collapseShortcutsSliderOnExecution: true
		, hideShortcutsSliderOnExecution: true
		, shortcutsHidden: false
		// private...
		, isParameterPanelReady: false
		, isParameterPanelReadyForExecution: false
		, isSubobjectPanelReady: false
		, isSnapshotPanelReady: false
		, isFromCross: false
		, callFromTreeListDoc: false
	};
	
	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.parametersselectionpage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.parametersselectionpage);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	//Ext.apply(this, c);

	this.addEvents(
		'beforetoolbarinit'
		, 'beforesynchronize'
		, 'beforeexecution'
		, 'synchronize'
		, 'synchronizeexception'
		, 'movenextrequest'
		, 'moveprevrequest'
		, 'collapse3'
		, 'backToAdmin'
		, 'crossnavigation'
		, 'loadurlfailure'
	);	
	
	this.initServices();
	this.init(c, doc);
	
	c = Ext.apply({}, c, {
		layout: 'fit',
		tbar: this.toolbar,
		border : false,
		//autoScroll : true,
		items: [{
			layout: 'border',
			border : false,
			listeners: {
			    'render': {
	            	fn: function() {
	            		if (!this.loadingMask) {
	            			this.loadingMask = new Sbi.decorator.LoadMask(this.body, {
	            				msg:LN('sbi.execution.parametersselection.loadingmsg')
	            			});
	            		}
	            		this.loadingMask.hide(); /*
	            								this is a workaround (work-around): when executing a document from administration tree or
	            								from menu, this loading mask does not appear. Invoking hide() solve the issue.
	            		 						*/
	          	 		if(this.maskOnRender === true) this.loadingMask.show();
	            	},
	            	scope: this
	          	}
	        },   	        
			items: [this.mainPanel ,this.parametersSlider]
		}]
	});   
	
	/*
	this.shortcutsPanel.on('subobjectexecutionrequest', this.onExecuteSubobject, this);
	this.shortcutsPanel.on('snapshotexcutionrequest', this.onExecuteSnapshot, this);
	this.shortcutsPanel.on('subobjectshowmetadatarequest', function (subObjectId) {
    	 var win_metadata = new Sbi.execution.toolbar.MetadataWindow({'OBJECT_ID': this.executionInstance.OBJECT_ID, 'SUBOBJECT_ID': subObjectId});
		 win_metadata.show();
	}, this);
	
	this.shortcutsPanel.subobjectsPanel.on('ready', function(){
		this.isSubobjectPanelReady = true;
		this.checkAutomaticStart();
	}, this);
	
	this.shortcutsPanel.snapshotsPanel.on('ready', function(snapshotId){
		this.isSnapshotPanelReady = true;
		this.snapshotId = snapshotId;
		this.checkAutomaticStart();
	}, this);
	*/
	
	// constructor
    Sbi.execution.ParametersSelectionPage.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.execution.ParametersSelectionPage
 * @extends Ext.Panel
 * @todo to be renamed in DocumentExecutionPage
 * 
 * This panel have a border layout. It uses the east region to visualize the Sbi.execution.ParametersPanel and the 
 * center region to visualize the document. The document can be viuslauzed in one of the following three modalities:
 * - INFO: Shows only document metadata and shortcuts (see Sbi.execution.InfoPage)
 * - VIEW: Shows the executed document (see Sbi.execution.DocumentPage)
 * - EDIT: Shows the document in edit mode
 */

/**
 * @cfg {String} labelAlign where to align parameter label in parameters selection panel. Possible values are 
 * left and top. The default value is left.
 */
/**
 * @cfg {Boolean} maskOnRender ...
 */
/**
 * @cfg {Boolean} collapseParametersSliderOnExecution true to collapse parameters panel when the executed document 
 * is displayed, false otherwise. The default is false.
 */
/**
 * @cfg {Boolean} collapseShortcutsSliderOnExecution true to collapse shortcuts panel when the executed document 
 * is displayed, false otherwise. The default is true.
 */
/**
 * @cfg {String} shortcutsHidden true if shortcuts panel is hidden, 
 * false otherwise. The default is false.
 */
/**
 * @cfg {Object} subobject an object that contains information related to the subobject to execute. It is optional.
 */

Ext.extend(Sbi.execution.ParametersSelectionPage, Ext.Panel, {
    
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null	
	
	/**
     * @property {Object} executionInstance
     * This object contains all the informations related to the current execution
     */
	, executionInstance: null
	
	/**
     * @property {Boolean} isParameterPanelReady
     * true if parameters panel has been loaded, false otherwise.
     */
	, isParameterPanelReady: null
	
	/**
	 * @property {Boolean} isParameterPanelReadyForExecution
	 * true if parameters panel has been loaded parameters panel has been loaded 
	 * and there are no parameters to be filled, false otherwise.
	 */
	, isParameterPanelReadyForExecution: null
	
	/**
     * @property {Boolean} isSubobjectPanelReady
     * true if subobject panel has been loaded, false otherwise.
     */
	, isSubobjectPanelReady: null
	
	/**
     * @property {Boolean} isSnapshotPanelReady
     * true if snapshot panel has been loaded, false otherwise.
     */
	, isSnapshotPanelReady: null
	
	/**
     * @property {Object} subobject an object that contains information related to the subobject 
     * to execute. null if no specific subobject has been specified
     */
	, subobject: null
	
	/**
     * @property {String} snapshotId the id of the snapshot to execute. 
     * null if no specific snapshot has been specified.
     */
	, snapshotId: null
	
	/**
	 * @property {Boolean} collapseParametersSliderOnExecution true to collapse parameters panel when the executed document 
	 * is displayed, false otherwise. The default is false.
	 */
	, collapseParametersSliderOnExecution: null
	/**
	 * @property {Boolean} collapseShortcutsSliderOnExecution true to collapse shortcuts panel when the executed document 
	 * is displayed, false otherwise. The default is true.
	 */
	, collapseShortcutsSliderOnExecution: null
	/**
     * @property {String} shortcutsHidden true if shortcuts panel is hidden, 
     * false otherwise. The default is false.
     */
	, shortcutsHidden: null
	
	/**
     * @property {Sbi.execution.toolbar.DocumentExecutionPageToolbar} toolbar The panel toolbar. It changes
     * its content according to the document current visualization modality (INFO, VIEW or EDIT) 
     * 
     */
	, toolbar: null
	
	/**
     * @property {Ext.Panel} mainPanel The main panel shown in the central region. It contains 
     * the documentPanel that properly shows the document according to its current visualization modality 
     * and the shortcutsSlider.
     * 
     */
	, mainPanel: null
	/**
     * @property {Ext.Panel} documentPanel The panel that display the the document according to its current visualization modality. 
     * It use a card layout. Each item is a panel used to visualize the document when it is in one specific visualization modality:
     * 
     *  - infoPage (Sbi.execution.InfoPage) is used when visualization modality is equal to INFO
     *  - documentPage (Sbi.execution.DocumentPage) is used when visualization modality is equal to VIEW
     * 
     */
    , documentPanel: null
    /**
     * @property {Sbi.execution.InfoPage} infoPage The panel used to visualize document when visualization modality is equal to INFO
     */
    , infoPage: null 
    /**
     * @property {Sbi.execution.DocumentPage} documentPage The panel used to visualize document when visualization modality is equal to VIEW
     */
    , documentPage: null
    /**
     * @property {Ext.Panel} shortcutsSlider The slider panel that contains the shortchutPanel
     */
    , shortcutsSlider: null
    /**
     * @property {Sbi.execution.ShortcutsPanel} shortcutsPanel The shortcutsPanel
     */
    , shortcutsPanel: null
    
    /**
     * @property {Ext.Panel} parametersSlider The slider panel that contains the parametersPanel
     */
	, parametersSlider: null
	 /**
     * @property {Sbi.execution.ParametersPanel} shortcutsPanel The parametersPanel
     */
    , parametersPanel: null
    /**
     * @property {Ext.Window} subobjectWin The snapshot window. It appears when the user click on "Execute subobject" button
     * in the toolbar. It is destroyed on close and regenerated on show. 
     */
    , subobjectWin: null
    /**
     * @property {Ext.Window} snapshotWin The snapshot window. It appears when the user click on "Execute snapshot" button
     * in the toolbar. It is destroyed on close and regenerated on show. 
     */
    , snapshotWin: null
   
    , loadingMask: null
    , maskOnRender: null
   
    // =================================================================================================================
	// METHODS
	// =================================================================================================================
		
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
    // NOTE: the following methods initialize the interface with empty widgets. There are not yet a specific execution 
    // instance to work on. The interface itself can change then when synchronization methods
    // are invoked passing in a specific execution instance.
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - none
	 */
	, initServices: Ext.emptyFn
    
	, init: function(config, doc) {
		this.initToolbar(config, doc);
		this.initMainPanel(config, doc);
		this.initParametersSlider(config, doc);
	}
	
	/**
	 * @method
	 * 
	 * Initialize the toolbar. By default the toolbar contains only back button
	 * 
	 * @param {Object} config the configuration object 
	 * @param {Object} doc the document configuration
	 */
	
	, initToolbar: function(config, doc) {
		
		this.toolbarHiddenPreference = config.toolbarHidden!== undefined ? config.toolbarHidden : false;
		if (this.toolbarHiddenPreference || this.hideToolbar(doc.engine)) return;
		
		config.executionToolbarConfig = config.executionToolbarConfig || {};
		config.executionToolbarConfig.callFromTreeListDoc = config.callFromTreeListDoc;
		config.executionToolbarConfig.preferenceSubobjectId = this.getSubObjectId();
		this.toolbar = new Sbi.execution.toolbar.DocumentExecutionPageToolbar(config.executionToolbarConfig);
		
		//this.toolbar.on('showmask', this.showMask, this);

		this.toolbar.on('beforeinit', function () {
			this.fireEvent('beforetoolbarinit', this, this.toolbar);
		}, this);
		
		this.toolbar.on('click', function (toolbar, action) {
			if(action === 'backToRolePage') {
				this.fireEvent('moveprevrequest');
			} else if (action === 'backToAdminPage') { 
				this.fireEvent('backToAdmin');
			} else if(action === 'expand') { 
				this.fireEvent('collapse3');
			}
		}, this);
		
		this.toolbar.on('beforerefresh', function (formState) {
				this.fireEvent('beforerefresh', this, this.executionInstance, formState);
		}, this);

	}
	
	/**
	 * @method
	 * 
	 *  Initialize the central panel
	 * 
	 * @param {Object} config the configuration object 
	 * @param {Object} doc the document configuration
	 */
	, initMainPanel: function(config, doc) {
		this.initShortcutsPanel(config, doc);
		
		var shortcutsHidden = (!Sbi.user.functionalities.contains('SeeSnapshotsFunctionality') 
				&& !Sbi.user.functionalities.contains('SeeSubobjectsFunctionality'))
				||
				this.shortcutsHidden;

		var shortcutsPanelHeight = 
			(Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.shortcutsPanel && Sbi.settings.execution.shortcutsPanel.height) 
			? Sbi.settings.execution.shortcutsPanel.height : 280;

		this.shortcutsSlider = new Ext.Panel({
			region:'south'
			, border: true
			, frame: false
			, collapsible: true
			, collapsed: false
			, hideCollapseTool: true
			, titleCollapse: true
			, collapseMode: 'mini'
			, split: true
			, autoScroll: true
			, height: shortcutsPanelHeight
			, layout: 'fit'
			, items: [this.shortcutsPanel]
			, hidden: shortcutsHidden
		});
		
		config.border = false;
		this.infoPage = new Sbi.execution.InfoPage(config, doc);
		this.documentPage = new Sbi.execution.DocumentPage(config, doc);
		this.documentPage.on('crossnavigation', function(config) {
			Sbi.trace('[ParametersSelectionPage.documentPage.on(\'crossnavigation\')]: IN');
			this.fireEvent('crossnavigation', config);
			Sbi.trace('[ParametersSelectionPage.documentPage.on(\'crossnavigation\')]: OUT');
		}, this);
		this.documentPage.on('loadurlfailure', function(errors ) {
			Sbi.trace('[ParametersSelectionPage.documentPage.on(\'loadurlfailure\')]: IN');
			this.showInfo();
			var messageBox = Ext.MessageBox.show({
				title: 'Error',
				msg: errors,
				modal: false,
				buttons: Ext.MessageBox.OK,
				width:300,
				icon: Ext.MessageBox.ERROR,
				animEl: 'root-menu'        			
			});
			this.fireEvent('loadurlfailure', errors);
			Sbi.trace('[ParametersSelectionPage.documentPage.on(\'loadurlfailure\')]: OUT');
		}, this);
		
		this.documentPanel = new Ext.Panel({
			region:'center'
			, layout:'card'
			, border: false
			, hideMode: !Ext.isIE ? 'nosize' : 'display'
			, activeItem: 0
			, items: [
			    this.infoPage , this.documentPage
			]
		});
		
		this.mainPanel = new Ext.Panel({
			layout: 'fit'
			, region:'center'
			, border: false
			, items:[{
				layout: 'border'
				, items: [this.shortcutsSlider, this.documentPanel]
			}]
		});
		
		return this.mainPanel;
	}
	
	/**
	 * @method
	 * 
	 *  Initialize the parameter slider
	 * 
	 * @param {Object} config the configuration object 
	 * @param {Object} doc the document configuration
	 */
	, initParametersSlider: function(config, doc) {
		
		this.initParametersPanel(config, doc);
		
		if(this.parametersPanel && this.parametersPanel.width){
			this.parametersSliderWidth = this.parametersPanel.width + 10;
		}
		
		this.parametersSlider = new Ext.Panel({
			region:'east'
			, title: LN('sbi.execution.parametersselection.parameters')
			, border: true
			, frame: false
			, collapsible: true
			, collapsed: false
			//, hideCollapseTool: true
			//, titleCollapse: true
			//, collapseMode: 'mini'
			//, split: true
			, autoScroll: true
			, width: this.parametersSliderWidth 
			, layout: 'fit'
			, items: [this.parametersPanel]
		});
		
		return this.parametersSlider;
	}
	
	
	
	/**
	 * @method
	 * 
	 *  Initialize the parameters panel
	 * 
	 * @param {Object} config the configuration object 
	 * @param {Object} doc the document configuration
	 */
	, initParametersPanel: function( config, doc ) {
		Ext.apply(config, {pageNumber: 2, parentPanel: this}); // this let the ParametersPanel know that it is on parameters selection page
		
		
		config.isFromCross = this.isFromCross;
		this.parametersPanel = new Sbi.execution.ParametersPanel(config, doc);
		
		this.parametersPanel.on('beforesynchronize', function() {
			if (!this.loadingMask) {
				this.loadingMask = new Sbi.decorator.LoadMask(this.body, {
					msg:LN('sbi.execution.parametersselection.loadingmsg')
				}); 
			}
			this.loadingMask.hide(); /*
									this is a workaround (work-around): when executing a document from administration tree or
									from menu, this loading mask does not appear. Invoking hide() solve the issue.
									 */
			this.loadingMask.show();
		}, this);
		
		this.parametersPanel.on('synchronize', function() {
			if(this.shortcutsPanelSynchronizationPending === false) {
				this.fireEvent('synchronize', this);
			}
			this.parametersPanelSynchronizationPending = false;
		}, this);
		
		this.parametersPanel.on('synchronize', 
			function(panel, readyForExecution, parametersPreference) {
				this.isParameterPanelReady = true;
				if (readyForExecution) {
					this.isParameterPanelReadyForExecution = true;
				}
				// try to find from session the value used for execution
				if (!this.isFromCross){
					Sbi.execution.SessionParametersManager.restoreStateObject(panel);
				}
				// restore memento (= the list of last N value inputed for each parameters)
				Sbi.execution.SessionParametersManager.restoreMementoObject(panel);
				if(this.automaticStartChecked === false) {
					this.automaticStartChecked = true;
					this.checkAutomaticStart();
				}
				
			}
		, this);

		this.parametersPanel.on('viewpointexecutionrequest', this.onExecuteViewpoint, this);
		
		this.parametersPanel.on('hideparameterspanel', function(){
			this.parametersSlider.hide();
			this.doLayout();
		}, this);
		
		
		return this.parametersPanel;
	}
	
	/**
	 * @method
	 * 
	 *  Initialize the shortcut panel
	 * 
	 * @param {Object} config the configuration object 
	 * @param {Object} doc the document configuration
	 */
	, initShortcutsPanel: function( config, doc ) {
		this.shortcutsPanel = new Sbi.execution.ShortcutsPanel(config, doc);
		this.shortcutsPanel.on('synchronize', function() {
			if(this.parametersPanelSynchronizationPending === false) {
				this.fireEvent('synchronize', this);
			}
			this.shortcutsPanelSynchronizationPending = false;
		}, this)
		
		this.shortcutsPanel.on('subobjectexecutionrequest', this.onExecuteSubobject, this);
		this.shortcutsPanel.on('snapshotexcutionrequest', this.onExecuteSnapshot, this);
		this.shortcutsPanel.on('subobjectshowmetadatarequest', function (subObjectId) {
	    	 var win_metadata = new Sbi.execution.toolbar.MetadataWindow({'OBJECT_ID': this.executionInstance.OBJECT_ID, 'SUBOBJECT_ID': subObjectId});
			 win_metadata.show();
		}, this);
		
		this.shortcutsPanel.subobjectsPanel.on('ready', function(){
			this.isSubobjectPanelReady = true;
			this.checkAutomaticStart();
		}, this);
		
		this.shortcutsPanel.snapshotsPanel.on('ready', function(snapshotId){
			this.isSnapshotPanelReady = true;
			this.snapshotId = snapshotId;
			this.checkAutomaticStart();
		}, this);	
		
		return this.shortcutsPanel;
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // synchronization methods
	// -----------------------------------------------------------------------------------------------------------------
	// This methods change properly the interface according to the specific execution instance passed in
	
	/**
	 * Called by Sbi.execution.ExecutionWizard when a new document execution starts. Force re-synchronization
	 * of the following child objects:
	 * 
	 *  - {@link Sbi.execution.ParametersSelectionPage#toolbar toolbar} 
	 *  - {@link Sbi.execution.ParametersSelectionPage#infoPage infoPage} 
	 *  - {@link Sbi.execution.ParametersSelectionPage#parametersPanel parametersPanel}
	 *  - {@link Sbi.execution.ParametersSelectionPage#shortcutsPanel shortcutsPanel}  
	 *  
	 *  
	 * {@link Sbi.execution.ParametersSelectionPage#documentPage documentPage} is re-synchronized only when it is shown to the user using method #showDocument
	 * 
	* @param {Object} executionInstance the execution configuration
	* 
	 * @method
	 */
    , synchronize: function( executionInstance ) {
    	Sbi.trace('[ParametersSelectionPage.synchronize]: IN');
		if(this.fireEvent('beforesynchronize', this, executionInstance, this.executionInstance) !== false){
			this.executionInstance = executionInstance;
			
			
			
			this.infoPage.synchronize( executionInstance );
			this.showInfo();
			
			this.parametersPanelSynchronizationPending = true;
			this.parametersPanel.synchronize( this.executionInstance );
			
			this.shortcutsPanelSynchronizationPending = true;
			this.shortcutsPanel.synchronize( this.executionInstance );
		}
		Sbi.trace('[ParametersSelectionPage.synchronize]: OUT');
	}

    , synchronizeToolbar: function( executionInstance, documentMode ){
    	Sbi.trace('[ParametersSelectionPage.synchronizeToolbar]: IN');
		if(this.toolbar){
			this.toolbar.documentMode = documentMode || 'INFO';
			this.toolbar.synchronize( this, executionInstance);
		}
		Sbi.trace('[ParametersSelectionPage.synchronizeToolbar]: OUT');
    }

	// ----------------------------------------------------------------------------------------
	// public methods
	// ----------------------------------------------------------------------------------------
	/**
	 * Show the document info in the central panel
	 * 
	 * @method
	 */
	, showInfo: function() {
		Sbi.trace('[ParametersSelectionPage.showInfo]: IN');
		this.synchronizeToolbar( this.executionInstance, 'INFO' );
		if(this.hideShortcutsSliderOnExecution === true) {
			this.showShortcutsSlider();
		}
		this.expandShortcutsSlider();
		this.documentPanel.getLayout().setActiveItem( 0 );
		Sbi.trace('[ParametersSelectionPage.showInfo]: OUT');
	}
	
	/**
	 * Show the executed document in the central panel. The document is not re-executed,
	 * it is just shown. To rexecute the document before to show it use the method executeDocument.
	 * 
	 * @method
	 */
	, showDocument: function() {
		Sbi.trace('[ParametersSelectionPage.showDocument]: IN');
		
		this.synchronizeToolbar( this.executionInstance, 'VIEW' );
		
		if(this.collapseParametersSliderOnExecution === true) {
			this.collapseParametersSlider();
		}
		if(this.collapseShortcutsSliderOnExecution === true) {
			this.collapseShortcutsSlider();
		}
		
		if(this.hideShortcutsSliderOnExecution === true) {
			this.hideShortcutsSlider();
		}
		
		this.documentPanel.getLayout().setActiveItem( 1 );
		Sbi.trace('[ParametersSelectionPage.showDocument]: OUT');
	}
	
	/**
	 * @method
	 * 
	 *  Collapse the shortcut panel
	 */
	, collapseShortcutsSlider: function() {
		if(this.shortcutsSlider.collapsed === false) {
			this.shortcutsSlider.collapse(false);
			Sbi.trace('[ParametersSelectionPage.collapseShortcutsSlider]: slider succesfully collapsed');
		}
	}
	/**
	 * @method
	 * 
	 *  Expand the shortcut panel
	 */
	, expandShortcutsSlider: function() {
		if(this.shortcutsSlider.collapsed === true) {
			this.shortcutsSlider.expand(false);
			Sbi.trace('[ParametersSelectionPage.expandShortcutsSlider]: slider succesfully expanded');
		}
	}
	
	/**
	 * @method
	 * 
	 *  show the shortcut panel
	 */
	, showShortcutsSlider: function() {
		if(this.shortcutsSlider.hidden === true) {
			this.shortcutsSlider.show();
			Sbi.trace('[ParametersSelectionPage.showShortcutsSlider]: slider succesfully shown');
		}
	}
	
	/**
	 * @method
	 * 
	 *  hide the shortcut panel
	 */
	, hideShortcutsSlider: function() {
		if(this.shortcutsSlider.hidden === false) {
			this.shortcutsSlider.hide();
			Sbi.trace('[ParametersSelectionPage.hideShortcutsSlider]: slider succesfully hided');
		}
	}

	/**
	 * @method
	 * 
	 *  Collapse the parameter panel
	 */
	, collapseParametersSlider: function() {
		this.parametersSlider.collapse(false);
		Sbi.trace('[ParametersSelectionPage.collapseParametersSlider]: slider succesfully collapsed');
	}
	
	/**
	 * @method
	 * 
	 *  Expand the parameter panel
	 */
	, expandParametersSlider: function() {
		this.parametersSlider.expand(false);
		Sbi.trace('[ParametersSelectionPage.expandParametersSlider]: slider succesfully expanded');
	}
	
	/**
	 * @method
	 * 
	 *  Expand the parameter panel
	 */
	, showParametersSlider: function() {
		this.parametersSlider.show();
		Sbi.trace('[ParametersSelectionPage.showParametersSlider]: slider succesfully shown');
	}
	
	/**
	 * @method
	 * 
	 *  hide the parameter panel
	 */
	, hideParametersSlider: function() {
		this.parametersSlider.hide();
		Sbi.trace('[ParametersSelectionPage.hideParametersSlider]: slider succesfully hided');
	}
	
	
	
	/**
	 * @method
	 * 
	 * @return {String} the ID of the subobject to execute. null if no specific subobject has
	 * been specified
	 */
	, getSubObjectId: function() {
		return ( !Ext.isEmpty(this.subobject) && Ext.isEmpty(this.subobject.id) ) 
				? this.subobject.id 
				: null;
	}
	/**
	 * @method
	 * 
	 * @return {String} the ID of the snapshot to execute. null if no specific snapshot has
	 * been specified
	 */
	, getSnapshotId: function() {
		return this.snapshotId;
	}
	
	/**
	 * @method
	 * 
	 * Open the subobject window
	 */
	, openSubobjectSelectionWin: function() {
		var subobjectPanel =  new Sbi.execution.SubobjectsPanel({showTitle:false}, this.executionInstance.document);
		subobjectPanel.on('executionrequest', function(subObjectId) {
			this.executionInstance.SBI_SUBOBJECT_ID = subObjectId;
			this.subobjectWin.cardPanel.getLayout().setActiveItem( 1 );
			this.subobjectWin.executeButton.enable();
			if(!this.subobjectWin.parametersPanel.sync) {
				this.subobjectWin.parametersPanel.synchronize( this.executionInstance );
				this.subobjectWin.parametersPanel.sync = true;
			}
	    }, this);
		subobjectPanel.on('showmetadatarequest', function (subObjectId) {
	    	 var win_metadata = new Sbi.execution.toolbar.MetadataWindow({'OBJECT_ID': this.executionInstance.OBJECT_ID, 'SUBOBJECT_ID': subObjectId});
			 win_metadata.show();
		}, this);
		
	
		var parametersPanel = new Sbi.execution.ParametersPanel({
			isFromCross: this.isFromCross
			, pageNumber: 2
			, parentPanel: this}, this.executionInstance.document);
		
		parametersPanel.on('synchronize', function() {
			this.subobjectWin.parametersPanel.setFormState(this.parametersPanel.getFormState());
		}, this);
		
		var cardPanel = new Ext.Panel({layout:'card'
			, border: false
			, hideMode: !Ext.isIE ? 'nosize' : 'display'
			, activeItem: 0
			, items: [
			    subobjectPanel , parametersPanel
			]
		});
		
		var executeButton = new Ext.Button({
            text:'Execute',
            disabled:true,
            scope: this,
            handler: function(){
           	 this.subobjectWin.close();
                this.parametersPanel.setFormState(this.subobjectWin.parametersPanel.getFormState());
                this.refreshDocument();
            }
        });
		
		this.subobjectWin = new Ext.Window({
			title: "Execute customized view...",
			layout: 'fit',
			width: 600,
			height: 400,
			modal: true,
			buttons: [executeButton ,{
                 text: 'Close',
                 scope: this,
                 handler: function(){
                	 this.subobjectWin.close();
                 }
            }],
			items: [cardPanel]
		});
		this.subobjectWin.cardPanel = cardPanel;
		this.subobjectWin.parametersPanel = parametersPanel;
		this.subobjectWin.executeButton = executeButton;
		this.subobjectWin.show();
		
		subobjectPanel.synchronize( this.executionInstance );		
	}
	
	/**
	 * @method
	 * 
	 * Open the snapshot window
	 */
	, openSnapshotSelectionWin: function() {
		var snapshotsPanel =  new Sbi.execution.SnapshotsPanel({showTitle:false}, this.executionInstance.document);
		snapshotsPanel.on('executionrequest', function(snapshotId) {
	    	this.onExecuteSnapshot(snapshotId);
	    }, this);
		
		this.snapshotWin = new Ext.Window({
			title: "Execute scheduled documents...",
			layout: 'fit',
			modal: true,
			width: 400,
			height: 400,
			items: [snapshotsPanel]
		});
		this.snapshotWin.show();
		snapshotsPanel.synchronize( this.executionInstance );
	}
	
	
	
	// ----------------------------------------------------------------------------------------
	// controller methods (invoked by the toolbar)
	// ----------------------------------------------------------------------------------------
	
	/**
	 * Execute the document (not the subobject) passing to the engine the parameter values set by the user
	 * in the selection panel. Then show the executed document.
	 * 
	 * @method
	 */
	, executeDocument: function(executionInstance) {
		Sbi.trace('[ParametersSelectionPage.executeDocument]: IN');
		
		var formState = this.parametersPanel.getFormState();
		if(this.fireEvent('beforeexecution', this, this.executionInstance, formState) !== false){
			delete executionInstance.SBI_SUBOBJECT_ID;
			delete executionInstance.SBI_SNAPSHOT_ID;
			this.doExecuteDocumunt(executionInstance, formState);
		}
		this.showDocument();
		Sbi.trace('[ParametersSelectionPage.executeDocument]: OUT');
	}
	
	/**
	 * Execute the the subobject if selected or the document otherwise passing 
	 * to the engine the parameter values set by the user
	 * in the selection panel. Then show the executed document.
	 * 
	 * @method
	 */
	, refreshDocument: function(executionInstance) {
		Sbi.trace('[ParametersSelectionPage.refreshDocument]: IN');
		
		var formState = this.parametersPanel.getFormState();
		if(this.fireEvent('beforeexecution', this, this.executionInstance, formState) !== false){
			this.doExecuteDocumunt(executionInstance, formState);
		}		
		this.showDocument();
		Sbi.trace('[ParametersSelectionPage.refreshDocument]: OUT');
	}
	
	, doExecuteDocumunt: function(executionInstance, formState) {
		this.memorizeParametersInSession();
		this.executionInstance.PARAMETERS = Sbi.commons.JSON.encode( formState );
		Sbi.trace('[ParametersSelectionPage.doExecuteDocumunt]: Executing document with these parameters: ' + this.executionInstance.PARAMETERS);
		this.documentPage.synchronize( this.executionInstance );
	}

	
	, getFrame: function() {
		return this.documentPage.getMiFrame().getFrame(); 
	}

	, memorizeParametersInSession: function() {
		if (!this.isFromCross){
			Sbi.execution.SessionParametersManager.saveStateObject(this.parametersPanel);
			Sbi.execution.SessionParametersManager.updateMementoObject(this.parametersPanel);
		}
	}
	
	, getParameterValues: function() {
		return this.parametersPanel.getFormState();
	}
	
	, collapseSliders: function() {
		this.collapseShortcutsSlider();
		this.collapseParametersSlider();
	}

	
	
	
	// ----------------------------------------------------------------------------------------
	// private methods
	// ----------------------------------------------------------------------------------------
	
	
	, automaticStartChecked: false
	, checkAutomaticStart: function() {
		
		
		// must wait parameters/subobjects/snapshots panels have been loaded
		if (this.isSubobjectPanelReady === false || this.isSnapshotPanelReady === false || this.isParameterPanelReady === false) {
			return;
		}
		
		if(this.loadingMask) this.loadingMask.hide();
		
		// subobject preference wins: if a subobject preference is specified, subobject is executed
		if (this.getSubObjectId() != null) {
			// if document is datamart type and there are some parameters to be filled, subobject execution cannot start automatically
			if (this.executionInstance.document.typeCode != 'DATAMART' || this.isParameterPanelReadyForExecution === true) {
				this.executionInstance.isPossibleToComeBackToParametersPage = false;
				this.onExecuteSubobject(this.getSubObjectId());
			}
			return;
		}
		// snapshot preference follows: if a snapshot preference is specified, snapshot is executed
		if (this.snapshotId != null) {
			this.executionInstance.isPossibleToComeBackToParametersPage = false;
			this.onExecuteSnapshot(this.snapshotId);
			return;
		}
		// parameters form follows: if there are no parameters to be filled, start main document execution
		if (this.isParameterPanelReadyForExecution == true) {
			this.executionInstance.isPossibleToComeBackToParametersPage = false;
			this.executeDocument(this.executionInstance);
		}
	}
	
	, onExecuteViewpoint: function(v) {
		this.parametersPanel.applyViewPoint(v);
		this.executeDocument(this.executionInstance);
	}
	
	, onExecuteSubobject: function (subObjectId) {
		this.executionInstance.SBI_SUBOBJECT_ID = subObjectId;
		this.refreshDocument(this.executionInstance);
	}
	
	, onExecuteSnapshot: function (snapshotId) {
		this.executionInstance.SBI_SNAPSHOT_ID = snapshotId;
		this.refreshDocument(this.executionInstance);
	}
	
	, hideToolbar: function(engine){
		var toReturn = false;
		if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.toolbar &&
			Sbi.settings.execution.toolbar.hideForEngineLabels) {
			var listEnginesToHide = Sbi.settings.execution.toolbar.hideForEngineLabels;
			for (var i=0; i < listEnginesToHide.length; i++ ){
				if(listEnginesToHide[i] === engine){
					toReturn = true;
					break;
				}
			}
		}
		
		return toReturn;
	}
	
	// =================================================================================================================
	// EVENTS
	// =================================================================================================================
	
	//this.addEvents(
	/**
     * @event beforetoolbarinit
     * Fired before toolbar inizialization. Can be used by external object to inject buttons in this panel toolbar.
     * @param {Sbi.execution.ParametersSelectionPage} this
     * @param {Ext.Toolbar} The empty toolbar object ready to be initialized
     */
	//'beforetoolbarinit'
	/**
     * @event beforesynchronize
     * Fired before panel synchronization. If the callback returns false synchronization will be not
     * performed
     * @param {Sbi.execution.ParametersSelectionPage} this
     * @param {Object} oldExecutionInstance The old execution instance
     * @param {Object} newExecutionInstance The new execution instance
     */
	//, 'beforesynchronize'
	/**
     * @event beforesynchronize
     * Fired before document execution. If the callback returns false execution will be not
     * performed
     * @param {Sbi.execution.ParametersSelectionPage} this
     * @param {Object} executionInstance Current execution instance
     * @param {Object} parameterValues Current parameter's form state
     */
	//, 'beforeexecution'
	/**
     * @event synchronize
     * ...
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	//, 'synchronize'
	/**
     * @event synchronizeexception
     * ...
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	//, 'synchronizeexception'
	/**
     * @event movenextrequest
     * Fired when button clicked.
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	//, 'movenextrequest'
	/**
     * @event moveprevrequest
     * ...
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	//, 'moveprevrequest'
	/**
     * @event collapse3
     * ...
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	//, 'collapse3'
	/**
     * @event backToAdmin
     * ...
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	//, 'backToAdmin'
	//);	
	
});