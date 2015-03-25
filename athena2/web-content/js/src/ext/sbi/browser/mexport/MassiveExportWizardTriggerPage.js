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
 *  [list]
 *
 *
 * Public Events
 *
 *  [list]
 *
 * Authors
 *
 * - Giulio gavardi (giulio.gavardi@eng.it)
 */

Ext.ns("Sbi.browser.mexport");

Sbi.browser.mexport.MassiveExportWizardTriggerPage = function(config) {

	var defaultSettings = {
			layout: 'fit'
			, width: 800
			, height: 300           	
			, closable: true
			, constrain: true
			, hasBuddy: false
			, resizable: true
			
			, showJobDetails: false
			, timeFledsMaxHeight: 180
	        , timeFledsIncrement: 10
	};
	if (Sbi.settings && Sbi.settings.browser 
			&& Sbi.settings.browser.mexport && Sbi.settings.browser.mexport.massiveExportWizardTriggerPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.browser.mexport.massiveExportWizardTriggerPage);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);

	this.services = this.services || new Array();

	this.initMainPanel(c);	
	c = Ext.apply(c, {
		layout: 'fit'
		, items: [this.mainPanel]	
	});

	// constructor
	Sbi.browser.mexport.MassiveExportWizardTriggerPage.superclass.constructor.call(this, c);
	
	
	this.addEvents('select', 'unselect');
	
	this.on('select', this.onSelection, this);
	this.on('unselect', this.onDeselection, this);	
};

Ext.extend(Sbi.browser.mexport.MassiveExportWizardTriggerPage, Ext.Panel, {

	services: null
    , mainPanel: null
    , generalConfFields: null
	, cronConfFields: null
    , currentPage: null
    , generalInfoFieldSet: null
    , oneShotOptionsFieldSet: null
    , minuteOptionsFieldSet: null
    , hourlyOptionsFieldSet: null
    , dailyOptionsFieldSet: null
    , weeklyOptionsFieldSet: null
    , monthlyOptionsFieldSet: null
    
    
	// ----------------------------------------------------------------------------------------
	// public methods
	// ----------------------------------------------------------------------------------------

	, onSelection: function() {
		this.currentPage = true;
		this.wizard.setPageTitle('Trigger', 'Setup trigger\'s configuration');
	}
	
	, onDeselection: function() {
		this.currentPage = false;
	}
	
	, isTheCurrentPage: function() {
		return this.currentPage;
	}
	
	, getPageIndex: function() {
		var i;		
		for(i = 0; i < this.wizard.pages.length; i++) {
			if(this.wizard.pages[i] == this) break;
		}		
		return i;
	}
	
	, getPreviousPage: function() {
		var pages = this.wizard.pages;
		var i = this.getPageIndex();
		return (i != 0)? this.wizard.pages[i-1]: null;
	}
	
	, getNextPage: function() {
		var pages = this.wizard.pages;
		var i = this.getPageIndex();
		return (i != (pages.length-1))? this.wizard.pages[i+1]: null;
	}
	
	, getName: function(){
		return 'Sbi.browser.mexport.MassiveExportWizardTriggerPage';
	}
	
	, getContent: function() {
		var state;
		
		state = {};
		
		state.generalConf = {}; // job + trigger			
		for(var fieldSet in this.generalConfFields) {
			state.generalConf[fieldSet] = {};
			var fieldsInFieldSet = this.generalConfFields[fieldSet];
			for(var i = 0; i < fieldsInFieldSet.length; i++) {
				var field = fieldsInFieldSet[i];
				state.generalConf[fieldSet][field.getName()] = field.getValue();
			}
		}
		
		state.cronConf = {};
		//alert(this.activeFieldSet.title + " : " + this.activeFieldSet.name);
		for(var fieldSet in this.cronConfFields) {
			//alert(this.fieldSet + " = " + this.activeFieldSet.name);
			if(fieldSet != this.activeFieldSet.name) continue;
			state.cronConf[fieldSet] = {};
			var fieldsInFieldSet = this.cronConfFields[fieldSet];
			for(var i = 0; i < fieldsInFieldSet.length; i++) {
				var field = fieldsInFieldSet[i];
				if(field.getName() == 'inDays') {
					if(!state.cronConf[fieldSet][field.getName()]) {
						state.cronConf[fieldSet][field.getName()] = [];
					}
					if(field.getValue()) {
						state.cronConf[fieldSet][field.getName()].push(field.data);
					}
				} else {
					state.cronConf[fieldSet][field.getName()] = field.getValue();
				}
			}
		}
		
		//alert(state.toSource());
		
		return state;
	}
	
    // ----------------------------------------------------------------------------------------
	// private methods
	// ----------------------------------------------------------------------------------------

    , initMainPanel: function() {
    	
    	this.initGeneralConfFieldSet();
    	this.initCronConfFiledSet();
    	
    	
    	
		this.mainPanel = new Ext.FormPanel({
			labelWidth: 75, // label settings here cascade unless overridden
		    frame:true,
		    bodyStyle:'padding:5px 5px 0',
		    width: 350,
		    autoScroll: true,
	        items: [
				this.generalInfoFieldSet
				, this.oneShotOptionsFieldSet
				, this.minuteOptionsFieldSet
				, this.hourlyOptionsFieldSet
				, this.dailyOptionsFieldSet
				, this.weeklyOptionsFieldSet
				, this.monthlyOptionsFieldSet
			]
		});
    }	
    
    /**
     * Initialize general info conf (job + trigger)
     */
    , initGeneralConfFieldSet: function() {
    	var field;

    	this.generalConfFields = {};
    	this.initJobConfFields();
    	this.initTriggerConfFields();
    	
    	var fields = this.generalConfFields['job'].concat(this.generalConfFields['trigger']);
    	
    	this.generalInfoFieldSet = new Ext.form.FieldSet({
    		//checkboxToggle:true,
            collapsible: true,
            collapsed: false,
            title: 'General info',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            items : fields
    	});
    }
    
    , initJobConfFields: function() {
    	
    	this.generalConfFields['job'] = [];
    	
    	if(this.showJobDetails) {
	    	field = new Ext.form.TextField({
	            fieldLabel: 'Name',
	            name: 'name',
	            allowBlank:false
	        });
	    	this.generalConfFields['job'].push(field);
	    	
	    	field = new Ext.form.TextField({
	            fieldLabel: 'Description',
	            name: 'description',
	            allowBlank:false
	        });    	
	    	this.generalConfFields['job'].push(field);
    	}
    }
    
    
    , initTriggerConfFields: function() {
    	
    	this.generalConfFields['trigger'] = [];
    	
    	field = new Ext.form.DateField({
            fieldLabel: 'Start date',
            name: 'startDate',
            format: Sbi.config.localizedDateFormat || 'm/d/Y',
            allowBlank:false
        });
    	this.generalConfFields['trigger'].push(field);
    	
    	field = new Ext.form.TimeField({
    		fieldLabel: 'Start time',
            name: 'startTime',
            maxHeight: this.timeFledsMaxHeight,
            //format: 'H:i',
            increment: this.timeFledsIncrement,
            allowBlank:true
        });
    	this.generalConfFields['trigger'].push(field);
    	
    	field = new Ext.form.DateField({
    		fieldLabel: 'End date',
            name: 'endDate',
            format: Sbi.config.localizedDateFormat || 'm/d/Y',
			allowBlank:true
    	});
    	this.generalConfFields['trigger'].push(field);
    	
    	field = new Ext.form.TimeField({
   		 	fieldLabel: 'End time',
            name: 'endTime',
            maxHeight: this.timeFledsMaxHeight,
            //format: 'H:i',
            increment: this.timeFledsIncrement,
            allowBlank:true
    	});
    	this.generalConfFields['trigger'].push(field);
    }
    
    
    /**
     * Initialize cron conf (minute, hourly, weekly, monthly, yearly)
     */    
    , initCronConfFiledSet: function() {
    	this.cronConfFields = {};
    	this.initOneShotConfFieldSet();
    	this.initMinuteConfFieldSet();
    	this.initHourlyConfFieldSet();
    	this.initDailyConfFieldSet();
    	this.initWeeklyConfFieldSet();
    	this.initMonthlyConfFieldSet();
    }
    
    , initOneShotConfFieldSet: function() {
    	var field;
    	
    	this.cronConfFields['oneshot'] = [];
    	
    	field = new Ext.form.Checkbox({
    		fieldLabel: 'Enable',
            name: 'enabled',
            checked: false,
            allowBlank:false
    	});
    	this.cronConfFields['oneshot'].push(field);
    	
    	this.oneShotOptionsFieldSet = new Ext.form.FieldSet({
    		checkboxToggle:true,
            title: 'Oneshot',
            name: 'oneshot',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items : this.cronConfFields['oneshot']
    	});	
    	
    	this.oneShotOptionsFieldSet.on('expand', this.onExpand, this);
    }
    
    , initMinuteConfFieldSet: function() {
    	var field;
    	
    	this.cronConfFields['minutes'] = [];
    	
    	field = new Ext.form.NumberField({
    		fieldLabel: 'Every n minutes',
            name: 'minutes',
            allowBlank:false
    	});
    	this.cronConfFields['minutes'].push(field);
    	
    	this.minuteOptionsFieldSet = new Ext.form.FieldSet({
    		checkboxToggle:true,
            title: 'Minutes',
            name: 'minutes',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items : this.cronConfFields['minutes']
    	});	
    	this.minuteOptionsFieldSet.on('expand', this.onExpand, this);
    }
    
    , initHourlyConfFieldSet: function() {
    	var field;
    	
    	this.cronConfFields['hourly'] = [];
    	
    	field = new Ext.form.NumberField({
    		fieldLabel: 'Every n houres',
            name: 'houres',
            allowBlank:false
    	});
    	this.cronConfFields['hourly'].push(field);
    	
    	this.hourlyOptionsFieldSet = new Ext.form.FieldSet({
    		checkboxToggle:true,
            title: 'Hourly',
            name: 'hourly',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items : this.cronConfFields['hourly']
    	});
    	this.hourlyOptionsFieldSet.on('expand', this.onExpand, this);
    }
    
    , initDailyConfFieldSet: function() {
    	var field;
    	
    	this.cronConfFields['daily'] = [];
    	
    	field = new Ext.form.NumberField({
    		fieldLabel: 'Every n days',
            name: 'days',
            allowBlank:false
    	});
    	this.cronConfFields['daily'].push(field);
    	
    	this.dailyOptionsFieldSet = new Ext.form.FieldSet({
    		checkboxToggle:true,
            title: 'Daily',
            name: 'daily',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items : this.cronConfFields['daily']
    	});	
    	this.dailyOptionsFieldSet.on('expand', this.onExpand, this);
    }
    
    , initWeeklyConfFieldSet: function() {
    	var field;
    	
    	this.cronConfFields['weekly'] = [];
    	
    	var dayOfTheWeekName = ['Monday', 'Tuesday', 'Wednesday'
    	                    , 'Thursday', 'Friday', 'Saturday'
    	                    , 'Sunday'];
    	
    	var dayOfTheWeekId = ['MON', 'TUE', 'WED'
        	                  , 'THU', 'FRI', 'SAT'
        	                  , 'SUN'];
    	
    	field = new Ext.form.Checkbox({
    		fieldLabel: 'In day',
    		boxLabel: dayOfTheWeekName[0],
    		data: dayOfTheWeekId[0],
            name: 'inDays',
            allowBlank:false
    	});
    	this.cronConfFields['weekly'].push(field);
    	
    	for(var i = 1; i < dayOfTheWeekName.length; i++)  {
	    	field = new Ext.form.Checkbox({
	    		boxLabel: dayOfTheWeekName[i],
	    		data: dayOfTheWeekId[i],
	    		fieldLabel: '',
	            labelSeparator: '',
	            name: 'inDays',
	            allowBlank:false
	    	});
	    	this.cronConfFields['weekly'].push(field);
    	}
    	
    	
    	this.weeklyOptionsFieldSet = new Ext.form.FieldSet({
    		checkboxToggle:true,
            title: 'Weekly',
            name: 'weekly',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items : this.cronConfFields['weekly']
    	});	
    	this.weeklyOptionsFieldSet.on('expand', this.onExpand, this);
    }
    
    , initMonthlyConfFieldSet: function() {
    	var field;
    	
    	this.cronConfFields['monthly'] = [];
    	
    	field = new Ext.form.NumberField({
    		fieldLabel: 'In day [1-31]',
    		minValue: 0,
    		maxValue: 31,    		
            name: 'inDay',
            allowBlank:false
    	});
    	this.cronConfFields['monthly'].push(field);
    	
    	this.monthlyOptionsFieldSet = new Ext.form.FieldSet({
    		checkboxToggle:true,
            title: 'Monthly',
            name: 'monthly',
            autoHeight:true,
            defaults: {width: 210},
            defaultType: 'textfield',
            collapsed: true,
            items : this.cronConfFields['monthly']
    	});	
    	this.monthlyOptionsFieldSet.on('expand', this.onExpand, this);
    }
    
    , onExpand: function(fieldSet) {
    	//alert('expanded');
    	if(this.activeFieldSet) this.activeFieldSet.collapse();
    	this.activeFieldSet = fieldSet;
    }
});