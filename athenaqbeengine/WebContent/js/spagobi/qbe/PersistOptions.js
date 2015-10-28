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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Chiara Chiarelli (chiara.chiarelli@eng.it) Monica Franceschini
 * (monica.franceshini@eng.it)
 */
// var thisPanel;
Ext.ns("Sbi.qbe");

Sbi.qbe.PersistOptions = function(config) {
	
	// init properties...
	var defaultSettings = {
			title : LN('sbi.ds.persist'),
			itemId : 'persistPanel',
			width : '100%' // 500,			
	};
	
	if (Sbi.settings && Sbi.settings.tools && Sbi.settings.qbe && Sbi.settings.qbe.persistOptions) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.qbe.PersistOptions);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);

	this.initPanel();
	this.activatePersistForm(null, false, null, null);
	
	var c = Ext.apply({}, config, {
		items : [
		         this.isPersisted,
		         this.persistDetail,
		         this.isScheduled,
		         this.schedulingDetail
		         ]
	});  
	
	Sbi.qbe.PersistOptions.superclass.constructor.call(this, c);
};

Ext.extend(
		Sbi.qbe.PersistOptions,
		Ext.Panel,
		{

			persistDetail : null,
			schedulingDetail : null,
			isPersisted : null,
			isScheduled : null,
			persistTableName : null,
			schedulingCronLine : null,
			startDateField : null,
			endDateField : null,
			
			
			initPanel: function() {
				
				this.persistDetail = this.initPersistTab();

				this.schedulingDetail = this.initSchedulingPersistencePanel();
				
				this.persistDetailFieldset = new Ext.form.FieldSet(
						{
							id : 'persist-detail',
							itemId : 'persist-detail',
							xtype : 'fieldset',
							scope : this,
							labelWidth : 90,
							defaultType : 'textfield',
							autoHeight : true,
							autoScroll : true,
							bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
									: 'padding:0px 0px;',
							border : false,
							style : {
								"margin-left" : "0px",
								"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
										: "-13px")
										: "0"
							}
//						,	items : [ this.isPersisted,
//						 	          this.persistDetail,
//						 	          this.isScheduled,
//						 	          this.schedulingDetail ]
						});
			
			}
		
		,	initPersistTab : function() {
			this.isPersisted = new Ext.form.Checkbox({
				xtype : 'checkbox',
				itemId : 'isPersisted',
				name : 'isPersisted',
				boxLabel : LN('sbi.ds.isPersisted')
			});
			this.isPersisted.addListener('check',
					this.activatePersistForm, this);

			this.persistTableName = new Ext.form.TextField({
				maxLength : 50,
				minLength : 1,
				width : 200,
				regexText : LN('sbi.roles.alfanumericString'),
				fieldLabel : LN('sbi.ds.persistTableName'),
				allowBlank : false,
				validationEvent : true,
				name : 'persistTableName'
			});
			
			var fsPersist = new Ext.form.FieldSet(
					{
						labelWidth : 150,
						defaults : {
							// width : 200,
							border : false
						},
						defaultType : 'textfield',
						autoHeight : true,
						autoScroll : true,
						bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
								: 'padding:10px 15px;',
						border : true,
						style : {
							"margin-left" : "10px",
							"margin-top" : "10px",
							"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
									: "-13px")
									: "10px"
						},
						items : [ this.persistTableName ]
					});
			return fsPersist;

			

		}
			
			,
			initSchedulingPersistencePanel : function() {

				this.isScheduled = new Ext.form.Checkbox(
						{
							bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
									: 'padding:10px 15px;',
							xtype : 'checkbox',
							hidden : true,
							style : {
								"margin-left" : "10px",
								"margin-top" : "10px",
								"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
										: "-13px")
										: "10px"
							},
							itemId : 'isScheduled',
							name : 'isScheduled',
							boxLabel : LN('sbi.ds.isScheduled')
						});
				this.isScheduled.addListener('check',
						this.showScheduleForm, this);

				
				this.startDateField = new Ext.form.DateField(
						{
							fieldLabel : LN('sbi.ds.persist.cron.startdate'),
							name : 'startDate',
							format : 'd/m/Y'
						});
				this.endDateField = new Ext.form.DateField(
						{
							fieldLabel : LN('sbi.ds.persist.cron.enddate'),
							name : 'endDate',
							format : 'd/m/Y'
						});
				
				
				
				/* Datepicker */
				var datefield = new Ext.form.FieldSet(
						{
							// renderTo: 'datefield',
							labelWidth : 100, // label settings here
												// cascade unless
												// overridden
							// title: 'Datepicker',
							bodyStyle : 'padding:5px 5px 0',
							width : 360,
							defaults : {
								width : 220
							},
							defaultType : 'datefield',
							items : [
							         	this.startDateField,
							         	this.endDateField
//									{
//										fieldLabel : LN('sbi.ds.persist.cron.startdate'),
//										name : 'startDate',
//										format : 'd/m/Y'
//									},
//									{
//										fieldLabel : LN('sbi.ds.persist.cron.enddate'),
//										name : 'endDate',
//										format : 'd/m/Y'
//									} 
									]
						});

				var minutesDs = new Ext.data.ArrayStore({
					data : [ [ '0', '00' ], [ '1', '01' ],
							[ '2', '02' ], [ '3', '03' ],
							[ '4', '04' ], [ '5', '05' ],
							[ '6', '06' ], [ '7', '07' ],
							[ '8', '08' ], [ '9', '09' ],
							[ '10', '10' ], [ '11', '11' ],
							[ '12', '12' ], [ '13', '13' ],
							[ '14', '14' ], [ '15', '15' ],
							[ '16', '16' ], [ '17', '17' ],
							[ '18', '18' ], [ '19', '19' ],
							[ '20', '20' ], [ '21', '21' ],
							[ '22', '22' ], [ '23', '23' ],
							[ '24', '24' ], [ '25', '25' ],
							[ '26', '26' ], [ '27', '27' ],
							[ '28', '28' ], [ '29', '29' ],
							[ '30', '30' ], [ '31', '31' ],
							[ '32', '32' ], [ '33', '33' ],
							[ '34', '34' ], [ '35', '35' ],
							[ '36', '36' ], [ '37', '37' ],
							[ '38', '38' ], [ '39', '39' ],
							[ '40', '40' ], [ '41', '41' ],
							[ '42', '42' ], [ '43', '43' ],
							[ '44', '44' ], [ '45', '45' ],
							[ '46', '46' ], [ '47', '47' ],
							[ '48', '48' ], [ '49', '49' ],
							[ '50', '50' ], [ '51', '51' ],
							[ '52', '52' ], [ '53', '53' ],
							[ '54', '54' ], [ '55', '55' ],
							[ '56', '56' ], [ '57', '57' ],
							[ '58', '58' ], [ '59', '59' ] ],
					fields : [ 'value', 'text' ]
				});

				var hoursDs = new Ext.data.ArrayStore({
					data : [ [ '0', '0' ], [ '1', '1' ], [ '2', '2' ],
							[ '3', '3' ], [ '4', '4' ], [ '5', '5' ],
							[ '6', '6' ], [ '7', '7' ], [ '8', '8' ],
							[ '9', '9' ], [ '10', '10' ],
							[ '11', '11' ], [ '12', '12' ],
							[ '13', '13' ], [ '14', '14' ],
							[ '15', '15' ], [ '16', '16' ],
							[ '17', '17' ], [ '18', '18' ],
							[ '19', '19' ], [ '20', '20' ],
							[ '21', '21' ], [ '22', '22' ],
							[ '23', '23' ] ],
					fields : [ 'value', 'text' ]
				});

				var daysDs = new Ext.data.ArrayStore({
					data : [ [ '1', '1' ], [ '2', '2' ], [ '3', '3' ],
							[ '4', '4' ], [ '5', '5' ], [ '6', '6' ],
							[ '7', '7' ], [ '8', '8' ], [ '9', '9' ],
							[ '10', '10' ], [ '11', '11' ],
							[ '12', '12' ], [ '13', '13' ],
							[ '14', '14' ], [ '15', '15' ],
							[ '16', '16' ], [ '17', '17' ],
							[ '18', '18' ], [ '19', '19' ],
							[ '20', '20' ], [ '21', '21' ],
							[ '22', '22' ], [ '23', '23' ],
							[ '24', '24' ], [ '25', '25' ],
							[ '26', '26' ], [ '27', '27' ],
							[ '28', '28' ], [ '29', '29' ],
							[ '30', '30' ] ],
					fields : [ 'value', 'text' ]
				});

				var monthsDs = new Ext.data.ArrayStore(
						{
							data : [
									[
											'1',
											LN('sbi.ds.persist.cron.month.january') ],
									[
											'2',
											LN('sbi.ds.persist.cron.month.february') ],
									[
											'3',
											LN('sbi.ds.persist.cron.month.march') ],
									[
											'4',
											LN('sbi.ds.persist.cron.month.april') ],
									[
											'5',
											LN('sbi.ds.persist.cron.month.may') ],
									[
											'6',
											LN('sbi.ds.persist.cron.month.june') ],
									[
											'7',
											LN('sbi.ds.persist.cron.month.july') ],
									[
											'8',
											LN('sbi.ds.persist.cron.month.august') ],
									[
											'9',
											LN('sbi.ds.persist.cron.month.september') ],
									[
											'10',
											LN('sbi.ds.persist.cron.month.october') ],
									[
											'11',
											LN('sbi.ds.persist.cron.month.november') ],
									[
											'12',
											LN('sbi.ds.persist.cron.month.december') ] ],
							fields : [ 'value', 'text' ]
						});

				var weekdaysDs = new Ext.data.ArrayStore(
						{
							data : [
									[
											'1',
											LN('sbi.ds.persist.cron.weekday.monday') ],
									[
											'2',
											LN('sbi.ds.persist.cron.weekday.tuesday') ],
									[
											'3',
											LN('sbi.ds.persist.cron.weekday.wednesday') ],
									[
											'4',
											LN('sbi.ds.persist.cron.weekday.thursday') ],
									[
											'5',
											LN('sbi.ds.persist.cron.weekday.friday') ],
									[
											'6',
											LN('sbi.ds.persist.cron.weekday.saturday') ],
									[
											'7',
											LN('sbi.ds.persist.cron.weekday.sunday') ] ],
							fields : [ 'value', 'text' ]
						});

				var minuteColumn = [ {
					bodyStyle : 'padding-right:5px;',
					// items: {
					xtype : 'fieldset',
					title : 'Minute',
					defaultType : 'radio', // each item will be a radio
											// button
					items : [
							{
								checked : true,
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.everyminute'),
								name : 'minute-choose',
								id : 'minute-every',
								inputValue : 'every',
								scope : this,
								handler : function(ctl, val) {
									var multiselect = Ext
											.getCmp('minutesMultiselect');
									if (val) {
										multiselect.disable();
										// this.setSchedulingCronLine();
									} else {
										multiselect.enable();
										// this.setSchedulingCronLine();
									}
								}
							},
							{
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.choose'),
								name : 'minute-choose',
								id : 'minute-choose',
								inputValue : 'choose'
							},
							{
								hideLabel : true,
								xtype : "multiselect",
								id : "minutesMultiselect",
								dataFields : [ "value", "text" ],
								valueField : "value",
								displayField : "text",
								width : 75,
								height : 150,
								disabled : true,
								allowBlank : false,
								bodyStyle : 'overflowY: auto; position:relative;',
								store : minutesDs,
								listeners : {
									// change:
									// this.setSchedulingCronLine,
									scope : this
								}
							} ]
				// }
				} ];
				var hourColumn = [ {
					bodyStyle : 'padding-right:5px;',
					// items: {
					xtype : 'fieldset',
					title : 'Hour',
					defaultType : 'radio', // each item will be a radio
											// button
					items : [
							{
								checked : true,
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.everyhour'),
								name : 'hour-choose',
								id : 'hour-every',
								inputValue : 'every',
								scope : this,
								handler : function(ctl, val) {
									var multiselect = Ext
											.getCmp('hoursMultiselect');
									if (val) {
										multiselect.disable();
										// this.setSchedulingCronLine();
									} else {
										multiselect.enable();
										// this.setSchedulingCronLine();
									}
								}
							},
							{
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.choose'),
								name : 'hour-choose',
								id : 'hour-choose',
								inputValue : 'choose'
							}, {
								hideLabel : true,
								xtype : "multiselect",
								id : "hoursMultiselect",
								dataFields : [ "value", "text" ],
								valueField : "value",
								displayField : "text",
								width : 75,
								height : 150,
								disabled : true,
								allowBlank : false,
								store : hoursDs,
								listeners : {
									// change:
									// this.setSchedulingCronLine,
									scope : this
								}
							} ]
				// }
				} ];
				var dayColumn = [ {
					bodyStyle : 'padding-right:5px;',
					// items: {
					xtype : 'fieldset',
					title : 'Day',
					defaultType : 'radio', // each item will be a radio
											// button
					items : [
							{
								checked : true,
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.everyday'),
								name : 'day-choose',
								id : 'day-every',
								inputValue : 'every',
								scope : this,
								handler : function(ctl, val) {
									var multiselect = Ext
											.getCmp('daysMultiselect');
									if (val) {
										multiselect.disable();
										// this.setSchedulingCronLine();
									} else {
										multiselect.enable();
										// this.setSchedulingCronLine();
									}
								}
							},
							{
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.choose'),
								name : 'day-choose',
								id : 'day-choose',
								inputValue : 'choose'
							}, {
								hideLabel : true,
								xtype : "multiselect",
								id : "daysMultiselect",
								dataFields : [ "value", "text" ],
								valueField : "value",
								displayField : "text",
								width : 75,
								height : 150,
								disabled : true,
								allowBlank : false,
								store : daysDs,
								listeners : {
									// change:
									// this.setSchedulingCronLine,
									scope : this
								}
							} ]
				// }
				} ];
				var monthColumn = [ {
					bodyStyle : 'padding-right:5px;',
					// items: {
					xtype : 'fieldset',
					title : 'Month',
					defaultType : 'radio', // each item will be a radio
											// button
					items : [
							{
								checked : true,
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.everymonth'),
								name : 'month-choose',
								id : 'month-every',
								inputValue : 'every',
								scope : this,
								handler : function(ctl, val) {
									var multiselect = Ext
											.getCmp('monthsMultiselect');
									if (val) {
										multiselect.disable();
										// this.setSchedulingCronLine();
									} else {
										multiselect.enable();
										// this.setSchedulingCronLine();
									}
								}
							},
							{
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.choose'),
								name : 'month-choose',
								id : 'month-choose',
								inputValue : 'choose'
							}, {
								hideLabel : true,
								xtype : "multiselect",
								id : "monthsMultiselect",
								dataFields : [ "value", "text" ],
								valueField : "value",
								displayField : "text",
								width : 75,
								height : 150,
								disabled : true,
								allowBlank : false,
								store : monthsDs,
								listeners : {
									// change:
									// this.setSchedulingCronLine,
									scope : this
								}
							} ]
				// }
				} ];
				var weekdayColumn = [ {
					bodyStyle : 'padding-right:5px;',
					// items: {
					xtype : 'fieldset',
					title : 'Weekday',
					defaultType : 'radio', // each item will be a radio
											// button
					items : [
							{
								checked : true,
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.everyweekday'),
								name : 'weekday-choose',
								id : 'weekday-every',
								inputValue : 'every',
								scope : this,
								handler : function(ctl, val) {
									var multiselect = Ext
											.getCmp('weekdaysMultiselect');
									if (val) {
										multiselect.disable();
										// this.setSchedulingCronLine();
									} else {
										multiselect.enable();
										// this.setSchedulingCronLine();
									}
								}
							},
							{
								hideLabel : true,
								boxLabel : LN('sbi.ds.persist.cron.choose'),
								name : 'weekday-choose',
								id : 'weekday-choose',
								inputValue : 'choose'
							}, {
								hideLabel : true,
								xtype : "multiselect",
								id : "weekdaysMultiselect",
								dataFields : [ "value", "text" ],
								valueField : "value",
								displayField : "text",
								width : 75,
								height : 150,
								disabled : true,
								allowBlank : false,
								store : weekdaysDs,
								listeners : {
									// change:
									// this.setSchedulingCronLine,
									scope : this
								}
							} ]
				// }
				} ];

				this.schedulingCronLine = new Ext.form.TextField(
						{
							width : 300,
							value : '0 * * * * *',
							fieldLabel : LN('sbi.ds.persist.cron.schedulingline'),
							labelSeparator : ':',
							readOnly : true,
							hidden : true,
							allowBlank : true,
							validationEvent : true,
							id : 'schedulingCronLine'
						});

				var cronPanel = new Ext.form.FieldSet(
						{
							autoHeight : true,
							hidden : true,
							title : 'Update: scheduling detail',
							bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
									: 'padding:10px 15px;',
							border : true,
							style : {
								"margin-left" : "10px",
								"margin-top" : "10px",
								"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
										: "-13px")
										: "10px"
							},
							items : [
									datefield,
									{
										layout : 'column',
										border : false,
										id : 'cronColumn',
										// defaults are applied to all
										// child items unless otherwise
										// specified by child item
										defaults : {
											columnWidth : '.2',
											border : false
										},
										items : [ minuteColumn,
												hourColumn, dayColumn,
												monthColumn,
												weekdayColumn ]
									}, this.schedulingCronLine ]
						});
				return cronPanel;
			}
			
			,
			activatePersistForm : function(check, checked) {
				// var persistSelected = newValue;
				var persistSelected = checked;
				if (persistSelected != null && persistSelected == true) {
					this.persistDetail.setVisible(true);
					this.isScheduled.setVisible(true);
				} else {
					this.persistDetail.setVisible(false);
					this.isScheduled.setVisible(false);
					this.isScheduled.setValue(false);
				}
			}
			,
			showScheduleForm : function(check, checked) {
				if (checked) {
					this.schedulingDetail.setVisible(true);
				} else {
					this.schedulingDetail.setVisible(false);
				}
			}
			,
			setSchedulingCronLine : function() {
				var second, minute, hour, day, month, weekday;

				second = '0';
				minute = this.getSelection('minute');
				hour = this.getSelection('hour');
				day = this.getSelection('day');
				month = this.getSelection('month');
				weekday = this.getSelection('weekday');
				// Support for specifying both a day-of-week and a
				// day-of-month value is not complete
				// (you must currently use the '?' character in one of
				// these fields).
				if (day == '*' && weekday != '*') {
					day = '?';
				} else {
					weekday = '?';
				}

				Ext.get('schedulingCronLine').dom.value = second + " "
						+ minute + " " + hour + " " + day + " " + month
						+ " " + weekday;
			}
			,
			getSelection : function(name) {
				var chosen;
				if (Ext.get(name + "-every").dom.checked) {
					chosen = '*';
				} else {
					chosen = Ext.getCmp(name + 'sMultiselect')
							.getValue();
					if (!chosen.length) {
						chosen = '*';
					}
				}
				return chosen;
			}




	
});
