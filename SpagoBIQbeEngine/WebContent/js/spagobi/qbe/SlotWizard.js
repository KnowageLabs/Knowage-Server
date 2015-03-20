/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 
/**
  * CalculatedFieldWizard - short description
  * 
  * Object documentation ...
  * 
  * by Monica Franceschini
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.SlotWizard = function(config) {	
	
	var c = Ext.apply({}, config || {}, {
		title: LN('sbi.qbe.bands.title')
		, width: 800
		, height: 450
		, resizable: true
		, hasBuddy: false
		, constrain : true
		
	});

	Ext.apply(this, c);
	if(c.fieldForSlot !== undefined){
		this.fieldForSlot = c.fieldForSlot;
	}

	
	this.initMainPanel(c);	
	
	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	Sbi.qbe.SlotWizard.superclass.constructor.call(this, c);  
	this.add(this.mainPanel);
  
	this.addEvents('apply'); 
	
	

};

Ext.extend(Sbi.qbe.SlotWizard, Ext.Window, {
	
	hasBuddy: null
    , buddy: null
   
    , mainPanel: null
    , firstCalculatedFiledPanel : null
    , secondSlotDefinitionPanel: null
    , buttonsConfig: null
    , startFromFirstPage : true
    , fieldForSlot: null
    , modality: 'add'
    , fieldId : null
    , expression : null

    , getExpression: function() {
    	var expression = null;
    	if(this.startFromFirstPage == true || this.modality == 'edit') {
    		var fs = this.firstCalculatedFiledPanel.getFormState();
        	expression = fs.expression;
    	}
    		
    	if(this.firstCalculatedFiledPanel.target) {
    		expression = expression || this.firstCalculatedFiledPanel.target.attributes.attributes.formState.expression;
    	}
    	return expression;
    }

    , setExpItems: function(itemGroupName, items) {
    	this.firstCalculatedFiledPanel.setExpItems(itemGroupName, items);
    }

	, setTargetRecord: function(record) {
		this.firstCalculatedFiledPanel.setTargetRecord(record);
	}

	, setTargetNode: function(node) {
		this.firstCalculatedFiledPanel.setTargetNode(node);
	}
    
    , getCalculatedFiledPanel : function(){		
		return this.firstCalculatedFiledPanel;
	}
	, initMainPanel: function(c) {
		if(c.startFromFirstPage !== undefined){
			this.startFromFirstPage = c.startFromFirstPage;
		}
		var editStore = new Ext.data.JsonStore({
	        root: 'slots',
	        data: {slots:[]},
	        fields: ['name', 'valueset']
	    });
		this.modality = c.modality;//add (not passed) or edit
		if(this.modality !== undefined && this.modality !== null && this.modality =='edit'){
			this.startFromFirstPage = false;//this function is to edit the slot only
			var field = this.fieldForSlot;
			try{
				var storedata = {slots: field.attributes.attributes.formState.slots}
				editStore.loadData(storedata);
				editStore.commitChanges();
			}catch(err){
				alert(LN('sbi.qbe.bands.noteditable'));
				return;
			}
		}
		var save = function(){
			this.save();
		};
		
		
		var navHandler = function(page){
			if(this.mainPanel !== null){
				var curr = this.mainPanel.layout.activeItem;
				if(page == 1){
					this.mainPanel.layout.setActiveItem(1);
					btnNext.disabled = true;
					btnPrev.disabled = false;
					btnFinish.disabled = false;
					btnNext.disable();
					btnPrev.enable();
					btnFinish.enable();
					///gets field id
					var fs = this.firstCalculatedFiledPanel.getFormState();
					this.expression = fs.expression;

				} else {
					//back
					this.mainPanel.layout.setActiveItem(0);
					//this.firstCalculatedFiledPanel.detailsFormPanel.syncSize()();
					this.mainP
					btnPrev.disabled = true;
					btnNext.disabled = false;
					btnFinish.disabled = true;
					
					btnPrev.disable();
					btnNext.enable();
					btnFinish.disable();
				}
			}
		};
		var btnPrev = new Ext.Button({
            id: 'move-prev',
            text: LN('sbi.qbe.bands.back.btn'),
            handler: navHandler.createDelegate(this, [-1])
		});
		
		var btnNext = new Ext.Button({
            id: 'move-next',
            text: LN('sbi.qbe.bands.next.btn'),
            handler: navHandler.createDelegate(this, [1])
		});
		
		var btnFinish = new Ext.Button({
            id: 'finish',
            text: LN('sbi.qbe.bands.finish.btn'),
            disabled: false,
            scope: this,
            handler: function(){
			    var formState = null;
				var target = this.firstCalculatedFiledPanel.target;
				//add band mode
				if(this.modality === undefined || this.modality == null || this.modality !='edit'){

					if(this.startFromFirstPage == undefined || this.startFromFirstPage == null || this.startFromFirstPage == false){
						
						var fieldUniqueName = this.fieldForSlot.attributes.id ;
					
						fieldUniqueName = fieldUniqueName.replace(new RegExp('\\(' , 'g'), '[');
						fieldUniqueName = fieldUniqueName.replace(new RegExp('\\)' , 'g'), ']');
			
						
						formState = {
								alias: LN('sbi.qbe.bands.prefix') + this.fieldForSlot.text
								, type: 'undefined'
								, nature:'ATTRIBUTE'
								, expression: fieldUniqueName
						};
						target = this.fieldForSlot.parentNode;
					}else{
						formState = this.firstCalculatedFiledPanel.getFormState();
					}
					this.addSlotToFormState(formState);
				} else {
					//edit band
					//formState = this.fieldForSlot.attributes.attributes.formState;
					formState = this.firstCalculatedFiledPanel.getFormState();
					if(formState.expression == undefined || formState.expression == null || formState.expression == ''){
						if(this.expression==undefined || this.expression==null|| this.expression==''){
							formState.expression = this.fieldForSlot.attributes.attributes.formState.expression;
						}else{
							formState.expression = this.expression;
						}
					}
					this.addSlotToFormState(formState);
					target = this.fieldForSlot;
				}
		    	this.fireEvent('apply', this, formState, target);
		        this.close();
		    }
		});

		this.firstCalculatedFiledPanel = new Sbi.qbe.CalculatedFieldEditorPanel({
			expItemGroups: c.expItemGroups
			, fields: c.fields
			, functions: c.functions
			, aggregationFunctions: c.aggregationFunctions
			, dateFunctions: c.dateFunctions
			, arithmeticFunctions : c.arithmeticFunctions
			, groovyFunctions : c.groovyFunctions
			, expertMode: c.expertMode
			, scopeComboBoxData: c.scopeComboBoxData   		
			, validationService: c.validationService
			, expertDisable: true

		});
		var firstPage = null;
		if(this.startFromFirstPage){
			firstPage = this.firstCalculatedFiledPanel;
			
		}
		var fieldID = null;
		if(this.fieldForSlot !== null){
			fieldID= this.fieldForSlot.attributes.id;
			if(fieldID !== undefined && fieldID !== null && fieldID.indexOf('xnode-') !== -1){
				fieldID = null;
				this.expression = this.fieldForSlot.attributes.attributes.formState.expression;
			}
		}
		this.secondSlotDefinitionPanel = new Sbi.qbe.SlotEditorPanel({
			height: 420,
			autoWidth: true,
			fieldId: fieldID,
			firstPage: firstPage,
			slotWizard: this,
			editStore: editStore
	    });
		var wizardPages = [];
		
		if(this.startFromFirstPage){
			wizardPages = [this.firstCalculatedFiledPanel, this.secondSlotDefinitionPanel] ; 
			btnPrev.disable();
			btnNext.enable();
			btnFinish.disable();
			

		}else{
			wizardPages = [this.secondSlotDefinitionPanel] ; 
			btnPrev.disable();
			btnNext.disable();
			btnFinish.enable();
			if(this.modality =='edit'){
				wizardPages = [this.firstCalculatedFiledPanel, this.secondSlotDefinitionPanel] ; 
			}
		}
		this.mainPanel = new Ext.Panel({  
			    layout: 'card',  
			    activeItem: 0,  
			    scope: this,
				height: 420,
				autoWidth: true,
				resizable: true,
			    defaults: {border:false},  
			    bbar: [
			           btnPrev,
			           '->', // greedy spacer so that the buttons are aligned to each side
			           btnNext,
			           btnFinish
			    ], 
			    items: wizardPages
		});  
		
		this.firstCalculatedFiledPanel.doLayout();
		this.secondSlotDefinitionPanel.doLayout();
		if(this.modality =='edit'){
			this.mainPanel.activeItem = 1;
			btnPrev.enable();
		}
		this.mainPanel.doLayout();

    }
	, addSlotToFormState: function(formState){
		
		var slotStore = this.secondSlotDefinitionPanel.gridPanel.store;
		var slots = [];
		if(slotStore !== null){
			for (var i = 0; i < slotStore.data.length; i++) { 
				var record = slotStore.getAt(i); 
				slots[i] = record.data; 
			}
		}
		formState.slots = slots;
		
	}


});