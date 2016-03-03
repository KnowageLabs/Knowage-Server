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
 *
 * Toolbar for the execution.. The buttons are sortable and hiddable by the use.. The configuartion is stored in the cookies and restored every time the engine is opened
 *
 *
 *  @authors
 *  Alberto Ghedin (alberto.ghedin@eng.it), Monica Franceschini (monica.franceschini@eng.it), Giulio Gavardi (giulio.gavardi@eng.it),
 */



Ext.define('Sbi.olap.toolbar.OlapToolbar', {
	extend: 'Ext.toolbar.Toolbar',
	plugins : Ext.create('Ext.ux.BoxReorderer', {
		listeners: {
			Drop: {
				fn: function(plugin, container){
					Ext.util.Cookies.set(Sbi.config.documentLabel+'labelsToolbar',Ext.JSON.encode(container.getToolbarReordableItems()));
				}
			}
		}
	}),

	config:{
		toolbarConfig: {
			drillType: 'position'
		},
		mdx: "",
		hideSaveAsWindow: false,
		label2PropertyMap: {
			'BUTTON_HIDE_EMPTY':'suppressEmpty',
			'BUTTON_SHOW_PROPERTIES':'showProperties',
			'BUTTON_HIDE_SPANS':'hideSpans',
			'BUTTON_FATHER_MEMBERS':'showParentMembers'
		},
		whatIfButtons: [ 'BUTTON_SAVE','BUTTON_SAVE_NEW', 'BUTTON_UNDO','BUTTON_VERSION_MANAGER','BUTTON_EXPORT_OUTPUT','BUTTON_CALCULATED_MEMBERS','BUTTON_ALGORITHMS'],//,'BUTTON_ALGORITHMS'],
		unlockedButtons: [ 'BUTTON_SAVE','BUTTON_SAVE_NEW', 'BUTTON_UNDO','BUTTON_VERSION_MANAGER','BUTTON_ALGORITHMS'],
		olapToggleButtons:  ['BUTTON_FATHER_MEMBERS','BUTTON_HIDE_SPANS','BUTTON_HIDE_EMPTY','BUTTON_SHOW_PROPERTIES' ],
		olapButtons: ['BUTTON_MDX','BUTTON_FLUSH_CACHE',"BUTTON_EXPORT_XLS"],//'BUTTON_EDIT_MDX',
		lockButtons:['BUTTON_LOCK','BUTTON_UNLOCK','BUTTON_LOCK_OTHER']
	},

	/**
	 * @property {Ext.window.Window} mdxWindow
	 *  The window with the medx query
	 */
	mdxWindow: null,

	mdxContainerPanel: null,

//	fixed button in toolbar
	drillMode: null,

	// array containing lock buttons
	lockArray: null,

	// menu with buttons on toolbar
	menuButtons: null,

	// labels of button on toolbar and labels of buttons on menu
	labelsToolbar: null,
	labelsMenu: null,

	// two object cntaining label => button and label => configuration
	buttonsContainer: null,
	buttonsConfigContainer: null,

	// lock dinamic informations
	modelStatus: null,
	modelLocker: null,

	buttonHandlersMap: null,


	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.toolbar && Sbi.settings.olap.toolbar.OlapToolbar) {
			Ext.apply(this, Sbi.settings.olap.toolbar.OlapToolbar);
		}

		this.buttonHandlersMap = {
				'BUTTON_MDX': this.showMdxWindow,
				'BUTTON_EDIT_MDX': this.editMdxWindow,
				'BUTTON_FLUSH_CACHE': function(){
					Sbi.olap.eventManager.cleanCache();
				},
				'BUTTON_EXPORT_XLS': function(){
					Sbi.olap.eventManager.exportPivotTable();
				},
				'BUTTON_UNDO': function(){
					Sbi.olap.eventManager.undo();
				},
				'BUTTON_SAVE': function(){
					Sbi.olap.eventManager.persistTransformations();
				},
				'BUTTON_SAVE_NEW': this.openSaveAsWindow,
				'BUTTON_VERSION_MANAGER': this.openVersionManagerWindow,
				'BUTTON_EXPORT_OUTPUT': this.openOutputWindow,
				'BUTTON_CALCULATED_MEMBERS': this.openCalculatedMembersWindow,
				'BUTTON_CROSS_NAVIGATION':  function(){							
					Sbi.olap.eventManager.initCrossNavigation();
				}

		};

		this.callParent(arguments);

	},

	/*listeners: {
		render: function() {
			// After the component has been rendered, disable the default browser context menu
			Ext.getBody().on("contextmenu", Ext.emptyFn, null, {preventDefault: true});
		},
		contextmenu: function() {
		}
	},*/

	initComponent: function() {
		var thisPanel = this;
		this.labelsMenu = [];
		this.labelsToolbar = [];
		this.buttonsContainer = {};
		this.buttonsConfigContainer = {};

		this.addEvents(
				/**
				 * @event configChange
				 * The final user changes the configuration of the model
				 * @param {Object} configuration
				 */
				'configChange'
		);

		// FIXED Button on toolbar

		this.drillMode = Ext.create('Ext.container.ButtonGroup',
				{
			xtype: 'buttongroup',
			columns: 3,
			style:'border-radius: 10px;padding: 0px;margin: 0px;',
			reorderable: false,
			items: [{
				text: 'Position',
				scale: 'small',
				enableToggle: true,
				allowDepress: false,
				pressedCls: 'pressed-drill',
				toggleGroup: 'drill',
				cls: 'drill-btn-left',
				scope:thisPanel,
				handler: function() {
					this.setToolbarConf({drillType: 'position'});
				},
				reorderable: false
			},
			{
				text: 'Member',
				scale: 'small',
				enableToggle: true,
				allowDepress: false,
				toggleGroup: 'drill',
				pressedCls: 'pressed-drill',
				cls: 'drill-btn-center',
				scope:thisPanel,
				handler: function() {
					this.setToolbarConf({drillType: 'member'});
				},
				reorderable: false
			},
			{
				text: 'Replace',
				scale: 'small',
				enableToggle: true,
				allowDepress: false,
				toggleGroup: 'drill',
				pressedCls: 'pressed-drill',
				cls: 'drill-btn-right',
				scope:thisPanel,
				handler: function() {
					this.setToolbarConf({drillType: 'replace'});
				},
				reorderable: false

			}]
				}

		);

		//var pressedBtn = this.config.toolbarConfig.drillType;
		var pressedBtn = this.toolbarConfig.drillType;
		if(pressedBtn == 'position'){
			this.drillMode.items.items[0].pressed = true;
		}else if(pressedBtn == 'member'){
			this.drillMode.items.items[1].pressed = true;
		}else if(pressedBtn == 'replace'){
			this.drillMode.items.items[2].pressed = true;
		}

		// MENU BUTTONS CREATION //

		this.menuButtons = Ext.create('Ext.button.Split', {
			reorderable: false,
			renderTo: Ext.getBody(),
			iconCls: 'context-menu-icon',
			// handle a click on the button itself
			handler: function() {
			},
			menu: new Ext.menu.Menu({
				items: [
				        // these will render as dropdown menu items when the arrow is clicked:
				        {text: '', handler: function(){ }}
				        // , {text: 'Item 2', handler: function(){ alert("Item 2 clicked"); }}
				        ]
			})
		});
		this.menuButtons.setVisible(false);



		// CONFIGURATIONS //

		// create function to move button
		var sharedConfig = {
				scope:this,
				reorderable: true
		};


		for(var i=0; i<this.olapButtons.length;i++){
			var buttonLabel = this.olapButtons[i];
			this.buttonsConfigContainer[buttonLabel] = Ext.apply({
				tooltip: LN('sbi.olap.toolbar.'+buttonLabel),
				iconCls: buttonLabel,
				label: buttonLabel,
				handler: this.buttonHandlersMap[buttonLabel]
			}, sharedConfig);
		}


		for(var i=0; i<this.olapToggleButtons.length;i++){
			var buttonLabel = this.olapToggleButtons[i];
			this.buttonsConfigContainer[buttonLabel] = Ext.apply({
				tooltip: LN('sbi.olap.toolbar.'+buttonLabel),
				iconCls: buttonLabel,
				label: buttonLabel,
				toggleHandler: this.onButtonToggle,
				enableToggle: true
			}, sharedConfig);
		}


		//author: Maria Caterina Russo from Osmosit
		var buttonLabel = 'BUTTON_CROSS_NAVIGATION';
		this.buttonsConfigContainer[buttonLabel] = Ext.apply({
			tooltip: LN('sbi.olap.toolbar.'+buttonLabel),
			iconCls: buttonLabel,
			enableToggle: true,
			label: buttonLabel,
			handler: this.buttonHandlersMap[buttonLabel]
		}, sharedConfig);



		// if we are in standalone mode save and save new are always shown, if in spagobi mode not because model must be locked
		var saveHidden= true;
		if(Sbi.config.isStandalone == true){
			saveHidden = false;
		}


		for(var i=0; i<this.whatIfButtons.length;i++){
			var buttonLabel = this.whatIfButtons[i];
			this.buttonsConfigContainer[buttonLabel] = Ext.apply({
				tooltip: LN('sbi.olap.toolbar.'+buttonLabel),
				iconCls: buttonLabel,
				label: buttonLabel,
				hidden : saveHidden,
				handler: this.buttonHandlersMap[buttonLabel]
			}, sharedConfig);
		}


		var algorithmsService = Ext.create("Sbi.service.RestService",{
			url: "allocationalgorithm"
		});

		var algorithmsStore = Ext.create('Ext.data.Store', {
			model: 'Sbi.olap.AllocationAlgorithmModel',
			proxy: {
				type: 'rest',
				url: algorithmsService.getRestUrlWithParameters(),
				extraParams: algorithmsService.getRequestParams()
			},
			autoLoad: true
		});


		// Combo with the allocation algorithms
		this.buttonsConfigContainer['BUTTON_ALGORITHMS'] = {
			extType: 'Ext.form.field.ComboBox',
			tooltip: LN('sbi.olap.toolbar.BUTTON_ALGORITHMS'),
			iconCls: 'BUTTON_ALGORITHMS',
	        hideLabel: true,
	        store: algorithmsStore,
	        displayField: 'name',
	        valueField: 'className',
	        hidden : saveHidden,
	        typeAhead: true,
	        queryMode: 'local',
	        triggerAction: 'all',
	        emptyText: LN('sbi.olap.toolbar.BUTTON_ALGORITHMS'),
	        selectOnFocus: true,
	        width: 160,
	        indent: true,
	        listeners:{
				select:{
					fn: function( combo, records ){
						Sbi.olap.eventManager.setAllocationAlgorithm(records[0].data.name);
					}
				}
			}
		};


		// LOCK BUTTON CREATION

		this.lockModel = Ext.create('Ext.Button', {
			tooltip: LN('sbi.olap.toolbar.lock'),
			iconCls: 'lock-icon',
			handler: function() {
				Sbi.olap.eventManager.lockModel();
			},
			label: 'BUTTON_LOCK',
			scope:this,
			reorderable: false
		});
		this.lockModel.setVisible(false);

		this.unlockModel = Ext.create('Ext.Button', {
			tooltip: LN('sbi.olap.toolbar.unlock'),
			iconCls: 'unlock-icon',
			handler: function() {

				Sbi.olap.eventManager.unlockModel();
			},
			label: 'BUTTON_UNLOCK',
			scope:this,
			reorderable: false
		});
		this.unlockModel.setVisible(false);

		this.lockOtherModel = Ext.create('Ext.Button', {
			tooltip: LN('sbi.olap.toolbar.lock_other'),
			iconCls: 'lock-other-icon',
			handler: function() {
			},
			scope:this,
			label: 'BUTTON_LOCK_OTHER',
			reorderable: false
		});
		this.lockOtherModel.setVisible(false);

		if(Sbi.config.isStandalone == false){
			this.lockArray = new Array(this.lockModel, this.unlockModel, this.lockOtherModel);
		}

		this.callParent();
	}


	//**********************************
	//        Public methods
	//**********************************



	/**
	 * Updates the object after the execution of a mdx query
	 * @param {Sbi.olap.PivotModel} pivot model
	 */
	, updateAfterMDXExecution: function(pivot, modelConfig){
		this.mdx=pivot.get("mdxFormatted");
		var firstExecution = false;

		// first execution loads model config
		if(this.modelConfig==null){
			firstExecution = true;
			this.modelConfig = modelConfig;

			// change configuration by marking press configurations depending on modelConfig
			this.markPressedButtons();

			// try to get cookies else take configuration from model config
			// labels Toolbar

			var myCookieToolbar = Ext.util.Cookies.get(Sbi.config.documentLabel+'labelsToolbar');
			var toolbarVisibleButtons = modelConfig.toolbarVisibleButtons || new Array();
			var toolbarVisibleMenu = modelConfig.toolbarMenuButtons || new Array();


			//if no button is configured use all buttons
			if(toolbarVisibleButtons.length == 0 && toolbarVisibleMenu.length==0){
				toolbarVisibleButtons = Ext.Array.merge(toolbarVisibleButtons, this.olapButtons, this.olapToggleButtons, this.whatIfButtons );
			}

			var decodedCookieToolbar = null;
			this.labelsToolbar = new Array();
			this.labelsMenu = new Array();

			if(myCookieToolbar!=undefined && myCookieToolbar!=''  && myCookieToolbar!='null'){
				decodedCookieToolbar = Ext.JSON.decode(myCookieToolbar);

				//merge the visible buttons (toolbar and menu) in a list
				toolbarVisibleButtons = toolbarVisibleButtons.concat(toolbarVisibleMenu);

				//merge the cookies with the toolbarconfig and create the list of visible buttons in the toolbar
				for(var i=0; i<decodedCookieToolbar.length; i++){
					var tool = decodedCookieToolbar[i];
					var index = Ext.Array.indexOf(toolbarVisibleButtons, tool);
					if(index>=0){//if the button live in the cookies and it's visible
						this.labelsToolbar.push(tool);
						toolbarVisibleButtons.splice(index,1);
					}
				}

				//all the buttons not visible in the toolbar go in the menu
				for(var i=0; i<toolbarVisibleButtons.length; i++){
					this.labelsMenu.push(toolbarVisibleButtons[i]);
				}
			}else{
				this.labelsToolbar = toolbarVisibleButtons;
				this.labelsMenu = toolbarVisibleMenu;
			}

			//update the cookie
			Ext.util.Cookies.set(Sbi.config.documentLabel+'labelsToolbar',Ext.JSON.encode(this.labelsToolbar));

		}else{
			this.modelConfig.actualVersion = modelConfig.actualVersion;
		}

		// if scenario is not what if do not draw some buttons
		this.cleanButtonsIfNotWhatIfScenario(this.modelConfig.whatIfScenario);

		// draw Toolbar and menu
		this.drawToolbarAndMenu(this.modelConfig);

		// locker configuration must be set after buttons have been drawed
		this.setLockerConfiguration(firstExecution, this.modelConfig);

		// undo button is present only in what if scenario
		if(this.modelConfig.whatIfScenario != undefined && this.modelConfig.whatIfScenario == true){
			var undoButton = this.buttonsContainer["BUTTON_UNDO"];
			if(undoButton != undefined){
				undoButton.setDisabled( !pivot.get("hasPendingTransformations") );
			}
		}

		this.updateDrillMode(modelConfig);
	}

	/**
	 * Get the ordered visible items of the toolbar
	 */
	, getToolbarReordableItems: function(){
		var array = new Array();
		for(var i=0; i<this.items.items.length; i++){
			var label = this.items.items[i].label;
			if(label && !Ext.Array.contains(this.lockButtons, label) ){
				array.push(label);
			}
		}
		return array;
	}

	/**
	 *  Render the lock command
	 */
	, renderLockModel: function(result){
		// if result contains info that model was locked
		var resOb = Ext.JSON.decode(result.responseText);

		if(resOb.status == 'locked_by_user'){
			this.setLockByUserState();
		}
		else{
			if(resOb.status == 'unlocked'){
				this.setUnlockState();
				Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.olap.artifact.lock.error"));
			}
			else{
				this.setLockByOtherState(resOb.locker);
				Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.olap.artifact.unlock.errorOther")+': '+resOb.locker);
			}

		}
		this.modelStatus = resOb.status;
		this.modelLocker = resOb.locker;

	}

	/**
	 * Sets the state of the view. Updates the toolbar setting the order and the visibility of the buttons.
	 * Syncronize the configuration in the server
	 * @param {Object} state of the view
	 */
	, setToolbarConf: function (conf){

		this.toolbarConfig = Ext.apply(this.toolbarConfig, conf);
		this.fireEvent('configChange',this.toolbarConfig);
	}


	//**********************************
	//        Private methods
	//**********************************

	/**
	 * Update the drill mode
	 */
	,updateDrillMode: function(modelConfig){
		var pressedBtn = modelConfig.drillType;

		this.drillMode.items.items[0].toggle(false);
		this.drillMode.items.items[1].toggle(false);
		this.drillMode.items.items[2].toggle(false);

		if(pressedBtn == 'position'){
			this.drillMode.items.items[0].toggle(true);
		}else if(pressedBtn == 'member'){
			this.drillMode.items.items[1].toggle(true);
		}else if(pressedBtn == 'replace'){
			this.drillMode.items.items[2].toggle(true);
		}
	},

	/**
	 * @private
	 * Toggle the button
	 */
	onButtonToggle: function (item, pressed){
		if(this.buttonsContainer[item.label]){
			this.buttonsContainer[item.label].pressed = pressed;
		}
		var object = {};
		object[this.label2PropertyMap[item.label]]=pressed;
		this.setToolbarConf(object);
	}


	, drawToolbarAndMenu: function(modelConfig){

		// recreate toolbar without destroyng buttons
		this.removeAll(false);
		// add first buttons always on toolbar
		this.addToolbarFixedButtons();

		// lock buttons only in what if scenario, lock button is before custom button
		if(modelConfig.whatIfScenario != undefined && modelConfig.whatIfScenario==true){
			this.addLockModel();
		}

		// insert customized toolbar
		this.insertInToolbarArray( this.labelsToolbar);

		//add a spacer between toolbar and menu
		this.add({ xtype: 'tbspacer', flex:1, reorderable: false});

		// customized menu
		this.insertInMenuArray( this.labelsMenu);
		this.add(this.menuButtons);
		if( this.labelsMenu.length > 0){
			this.menuButtons.setVisible(true);
		} else{
			this.menuButtons.setVisible(false);
		}
	}

	, addLockModel: function(){
		this.add(this.lockModel);
		this.add(this.unlockModel);
		this.add(this.lockOtherModel);
	}

	, addToolbarFixedButtons: function(){
		this.add(this.drillMode);
	}

	, cleanButtonsIfNotWhatIfScenario: function(isWhatIf){
		//following buttons must not be present if scenario is not what if BUTTON_SAVE, BUTTON_SAVE_NEW
		if(isWhatIf == undefined || isWhatIf == false){
			for(var i=0; i<this.whatIfButtons.length; i++){
				Ext.Array.remove(this.labelsMenu, this.whatIfButtons[i]);
				Ext.Array.remove(this.labelsToolbar, this.whatIfButtons[i]);
			}
		}
	}

	/**
	 *  render after unlock command
	 */
	, renderUnlockModel: function(result){
		var resOb = Ext.JSON.decode(result.responseText);
		// check if model was really unlocked
		if(resOb.status == 'unlocked'){
			this.setUnlockState();
		}
		else{
			if(resOb.status == 'locked_by_user'){
				this.setLockByUserState();
				Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.olap.artifact.unlock.error"));
			}
			else{
				Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.olap.artifact.unlock.errorOther")+': '+resOb.locker);
				this.setLockByOtherState(resOb.locker);
			}

		}
		this.modelStatus = resOb.status;
		this.modelLocker = resOb.locker;

	}

	, setLockByUserState: function(locker){

		for(var i=0; i<this.unlockedButtons.length;i++){
			if(this.buttonsContainer[this.unlockedButtons[i]]){
				this.buttonsContainer[this.unlockedButtons[i]].show();
			}
		}
		this.lockModel.hide();
		this.unlockModel.show();
		this.lockOtherModel.hide();
	}

	, setLockByOtherState: function(locker){
		for(var i=0; i<this.unlockedButtons.length;i++){
			if(this.buttonsContainer[this.unlockedButtons[i]]){
				this.buttonsContainer[this.unlockedButtons[i]].hide();
			}
		}
		this.lockModel.hide();
		this.unlockModel.hide();
		this.lockOtherModel.show();
		this.lockOtherModel.setTooltip(LN('sbi.olap.toolbar.lock_other')+': '+locker);

	}

	, setUnlockState: function(){
		for(var i=0; i<this.unlockedButtons.length;i++){
			if(this.buttonsContainer[this.unlockedButtons[i]]){
				this.buttonsContainer[this.unlockedButtons[i]].hide();
			}
		}
		this.lockModel.show();
		this.unlockModel.hide();
		this.lockOtherModel.hide();

	}

	, setLockerConfiguration: function(firstExecution, modelConfig){
		// if it is first Execution take information from model config, else from global variables
		if(firstExecution==true){
			this.modelStatus = modelConfig.status;
			this.modelLocker = modelConfig.locker;
		}

		if(this.modelStatus == 'locked_by_user'){
			this.setLockByUserState(this.modelLocker);
		}
		else if(this.modelStatus == 'locked_by_other'){
			this.setLockByOtherState(this.modelLocker);
		}
		else if(this.modelStatus == 'unlocked'){
			this.setUnlockState();
		}


	}
	/**
	 * set buttons whose label are contained in array to visible or not according to boolean visible parameter
	 */
	, insertInToolbarArray: function(labelsToolbarArray){

		// visible is boolean to set visibile or not
		for( var j = 0; j < labelsToolbarArray.length; j++){
			var lab = labelsToolbarArray[j];
			this.insertButton(lab, false);
		}
	}

	, insertInMenuArray: function(labelsMenuArray){

		for( var j = 0; j < labelsMenuArray.length; j++){
			var lab = labelsMenuArray[j];
			this.insertButton(lab, true);
		}

		this.menuButtons.setVisible(true);

	}

	/**
	 *  function that recreate button fromm config and store it in toolbar or menu;
	 *  some information must be preserved from previous button (if existed)
	 */
	, insertButton: function(label, inMenu){

		var alreadyPresent = false;
		var presentPressed = false;
		var presentDisabled = false;

		if(this.buttonsContainer[label] != undefined && this.buttonsContainer[label] != null ){
			alreadyPresent = true;
		}

		if(alreadyPresent==true){
			presentPressed = this.buttonsContainer[label].pressed;
			presentDisabled = this.buttonsContainer[label].disabled;
			this.buttonsContainer[label].destroy();
		}

		var config = this.buttonsConfigContainer[label];

		if(config){
			var buttonCreated = this.createButton(config);

			this.addContextMenuListener(buttonCreated, inMenu);

			// recreate
			if(alreadyPresent==true){
				buttonCreated.pressed = presentPressed;
				buttonCreated.disabled = presentDisabled;
			}

			this.buttonsContainer[buttonCreated.label] = buttonCreated;

			// add particular pressed logic depending on button
			if(inMenu == true){
				buttonCreated.text = buttonCreated.tooltip;
				this.menuButtons.menu.add(buttonCreated);
				if(!Ext.Array.contains(this.labelsMenu, label)){
					this.labelsMenu.push(label);
				}

			}
			else{
				this.add(buttonCreated);
				if(!Ext.Array.contains(this.labelsToolbar, label)){
					this.labelsToolbar.push(label);
					Ext.util.Cookies.set(Sbi.config.documentLabel+'labelsToolbar',Ext.JSON.encode(this.labelsToolbar));
				}
			}

		}
	}

	/**
	 * Move button from menu to toolbar or viceversa
	 */
	, moveButton: function(button, inMenu){

		// if is in menu must insert in toolbar and viceversa
		if(inMenu==true){

			// in moving button from menu to toolbar the menu must be removed and re-added
			this.remove(this.menuButtons, false);

			// remove the space it is the last element after menu has been removed
			this.remove(this.items.length-1);


			this.insertButton(button.label, false);
			this.add({ xtype: 'tbspacer', flex:1, reorderable: false});
			this.add(this.menuButtons);
			this.labelsMenu = Ext.Array.remove(this.labelsMenu, button.label);


		}
		else{
			this.insertButton(button.label, true);
			this.labelsToolbar = Ext.Array.remove(this.labelsToolbar, button.label);
		}

		if(this.labelsMenu.length>0){
			this.menuButtons.setVisible(true);
		}
		else{
			this.menuButtons.setVisible(false);
		}

		Ext.util.Cookies.set(Sbi.config.documentLabel+'labelsToolbar',Ext.JSON.encode(this.labelsToolbar));

	}

	/**
	 * tells if button is in menu or toolbar by looking at labelsMenu, labelsToolbar arrays
	 */
	, isButtonInMenuOrToolbar: function(label){
		if( Ext.Array.contains(this.labelsToolbar,label)){
			return 'toolbar';
		}
		else if(Ext.Array.contains(this.labelsMenu,label)){
			return 'menu';
		} else {
			return 'none';
		}
	}

	, createButton: function(config){

		var button = null;

		if(config.extType){
			button = Ext.create(config.extType, config);
		} else {
			button = Ext.create('Ext.Button', config);
		}

		this.buttonsContainer[button.label] = button;
		button.setVisible(true);
		return button;
	}

	, addContextMenuListener: function(butt, isMenu){
		// add context menu listener
		var msg = null;
		if(isMenu == true){
			msg = 'add to toolbar';
		}
		else if(isMenu == false){
			msg = 'add to menu';
		}

		var thisPanel = this;
		butt.on('render', function(button){
			this.getEl().addListener('contextmenu',
					function(){
				var m = Ext.create('Ext.menu.Menu', {
					width: 100,
					height: 30,
					margin: '0 0 10 0',
					items: [{
						text: msg
						, listeners:{
							click: {
								fn: function(){
									var where = this.isButtonInMenuOrToolbar(button.label);
									if(where == 'toolbar'){
										this.moveButton(button, false);
									}
									else if(where == 'menu'){
										this.moveButton(button, true);
									} else {
										return;
									}

								}
					, scope: thisPanel
							}
						}
					}]
				});

				m.showAt(e.getXY());
			},
			thisPanel);
		});

	}

	/**
	 * this functions treats particular buttons that need to preserve memory if must be already pressed
	 * it is called on the first draw, when buttons has no memory and modelconfig stores info
	 */
	, markPressedButtons: function(){

		for(var i=0; i<this.olapToggleButtons.length; i++){
			var buttonConfig = this.buttonsConfigContainer[this.olapToggleButtons[i]];
			if(buttonConfig != undefined){
				var isValue= this.modelConfig[this.label2PropertyMap[this.olapToggleButtons[i]]];
				if(isValue == true){
					buttonConfig.pressed = true;
				}else{
					buttonConfig.pressed = false;
				}
			}
		}
	},

	//HANDLER FUNCTIONS

	/**
	 * Opens the version management window
	 */
	openVersionManagerWindow : function() {
		var window = Ext.create('Sbi.olap.toolbar.VersionManagerWindow',{
			actualVersion: this.modelConfig.actualVersion
		});
		window.show();
	},

	/**
	 * Opens the output table export configuration wizard
	 */
	openOutputWindow :function() {
		var window = Ext.create('Sbi.olap.toolbar.ExportWizardWindow',{
			actualVersion: this.modelConfig.actualVersion
		});
		window.show();
		window.on('exportOutput', function(params){
			Sbi.olap.eventManager.exportOutput(params);
		},this);
	},

	//TODO:osmosit
	/**
	 * Opens the calculated members wizard
	 */
	openCalculatedMembersWindow: function() {
		var window = Ext.create('Sbi.olap.toolbar.CalculatedMembersWindow',{
			actualVersion: this.modelConfig.actualVersion
		});
		window.show();
	},


	/**
	 * Opens the output table export configuration wizard
	 */
	openSaveAsWindow :function() {
		if(this.hideSaveAsWindow){
			Sbi.olap.eventManager.persistNewVersionTransformations({});
		}else{
			var window = Ext.create('Sbi.olap.toolbar.SaveAsWindow',{});
			window.show();
			window.on('saveAs', function(params){
				Sbi.olap.eventManager.persistNewVersionTransformations(params);
			},this);
		}
	},

	/**
	 * Open the mdx window
	 */
	showMdxWindow: function(){
		if(!this.mdxWindow){


			this.mdxContainerPanel = Ext.create('Ext.panel.Panel', {
				frame: false,
				layout: 'fit',
				autoScroll: true,
				html: ""
			});

			this.mdxWindow = Ext.create('Ext.window.Window', {
				height: 400,
				width: 300,
				layout: 'fit',
				closeAction: 'hide',
				items:[this.mdxContainerPanel],
				bbar:[
				      '->',    {
				    	  text: LN('sbi.common.close'),
				    	  handler: function(){
				    		  this.mdxWindow.hide();
				    	  },
				    	  scope: this
				      }]
			});
		}
		this.mdxContainerPanel.update(this.mdx);
		this.mdxWindow.show();
	},

	/**
	 * Open the mdx window
	 */
	editMdxWindow: function(){
		var editMdxContainerPanel = Ext.create('Sbi.olap.toolbar.EditMdxWindow', {});
		editMdxContainerPanel.show();
	}


});
