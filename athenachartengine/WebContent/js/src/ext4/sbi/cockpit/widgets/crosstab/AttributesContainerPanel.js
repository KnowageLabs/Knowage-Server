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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.AttributesContainerPanel = function(config) {

	var defaultSettings = {
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.crosstab && Sbi.settings.cockpit.widgets.crosstab.attributesContainerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.crosstab.attributesContainerPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	this.hasSegmentAttribute = false;

	Ext.apply(this, c); // this operation should overwrite this.initialData content, that is initial grid's content

	this.addEvents("beforeAddAttribute", "attributeDblClick", "attributeRemoved");

	this.init();

	Ext.apply(c, {
        store: this.store
        //, cm: this.cm
        //, sm: this.sm
        , enableDragDrop: true
        , ddGroup: this.ddGroup || 'worksheetDesignerDDGroup'
	    , layout: 'fit'
	    , hideHeaders: true
	    , forceFit: true
		, tools: [
	          {
	        	  type: 'close'
	        	, handler: this.removeAllAttributes
	          	, scope: this
	          	, qtip: LN('sbi.crosstab.attributescontainerpanel.tools.tt.removeall')
	          }
		]
        , listeners: {
			render: function(grid) { // hide the grid header
//				alert(Sbi.toSource(grid.getView().getEl()));
				//grid.getView().select('.x-box-inner').setStyle('display', 'none');
    		}
        	, keydown: function(e) {
        		if (e.keyCode === 46) {
        			this.removeSelectedAttributes();
      	      	}
      	    }
        	, mouseover: function(e, t) {
        		this.targetRow = t; // for Drag&Drop
        	}
        	, mouseout: function(e, t) {
        		this.targetRow = undefined;
        	}
        	, itemdblclick: this.itemDblClickHandler
		}

        , scope: this
        , type: 'attributesContainerPanel'
	});

	// constructor
	Sbi.cockpit.widgets.crosstab.AttributesContainerPanel.superclass.constructor.call(this, c);

    this.on('render', this.initDropTarget, this);

	//clean the selection of the grid the mouse quit from the grid
	//to avoid focusing problem when the user navigate between different
	//AttributesContainerPanels
    this.on('mouseout',function(){
		var sm = this.getSelectionModel();
		sm.clearSelections();
	},this);
};

Ext.extend(Sbi.cockpit.widgets.crosstab.AttributesContainerPanel, Ext.grid.Panel, {

	initialData: undefined
	, store: null
	, targetRow: null
	, calculateTotalsCheckbox: null
	, calculateSubtotalsCheckbox: null
	, validFields: null
	, Record: Ext.data.Record.create([
          {name: 'id', type: 'string'}
	      , {name: 'alias', type: 'string'}
	      , {name: 'funct', type: 'string'}
	      , {name: 'iconCls', type: 'string'}
	      , {name: 'nature', type: 'string'}
	      , {name: 'values', type: 'string'}
	      , {name: 'sortable', type: 'boolean'}
	      , {name: 'width', type: 'int'}
	])

	, templateArgs: {
         innerCls : '',
         splitCls : '',
         baseCls : Ext.baseCSSPrefix + 'btn',
         iconCls  : '',
         glyphCls: '',
         glyphFontFamily: Ext._glyphFontFamily,
         alias     : '&#160;'
     }

	, renderTpl1: [
               '<a id="button-{id}" class="x-btn x-unselectable x-btn-default-medium x-icon-text-left x-btn-icon-text-left x-btn-default-medium-icon-text-left" tabindex="0" unselectable="on" hidefocus="on" role="button">',
               '<span id="{id}-btnWrap" class="{baseCls}-wrap',
                    '<tpl if="splitCls"> {splitCls}</tpl>',
                    '{childElCls}" unselectable="on">',
                    '<span id="{id}-btnEl" class="{baseCls}-button">',
                        '<span id="{id}-btnInnerEl" class="{baseCls}-inner {innerCls}',
                            '{childElCls}" unselectable="on">',
                            '{alias}',
                        '</span>',
                        '<span role="img" id="{id}-btnIconEl" class="{baseCls}-icon-el {iconCls}',
                            '{childElCls} {glyphCls}" unselectable="on" style="',
                            '<tpl if="iconUrl">background-image:url({iconUrl});</tpl>',
                            '<tpl if="glyph && glyphFontFamily">font-family:{glyphFontFamily};</tpl>">',
                            '<tpl if="glyph">&#{glyph};</tpl><tpl if="iconCls || iconUrl">&#160;</tpl>',
                        '</span>',
                    '</span>',
                '</span>',
                // if "closable" (tab) add a close element icon
                '<tpl if="closable">',
                    '<span id="{id}-closeEl" class="{baseCls}-close-btn" title="{closeText}" tabIndex="0"></span>',
                '</tpl>'
                , '</a>'

   ]

	, onFieldDrop: function(ddSource) {
		if (ddSource.id === "field-grid-body") {
			this.notifyDropFromAttributesContainerPanel(ddSource);
		}
	}

	, notifyDropFromAttributesContainerPanel: function(ddSource) {
		Sbi.trace("[AttributesContainerPanel.notifyDropFromAttributesContainerPanel]: IN");

		var rows = ddSource.dragData.records;

		for (var i = 0; i < rows.length; i++) {
			var aRow = rows[i];

			if (this.store.findExact('id', aRow.data.id) !== -1) {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.attributealreadypresent'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}

			if (aRow.data.nature === 'measure' || aRow.data.nature === 'mandatory_measure') {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.measures'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			};

			if (this.fireEvent('beforeAddAttribute', this, aRow.data) == false) {
				return;
			}

			//this.addField(aRow.data);
			this.addAttribute(aRow.data);
			this.fireEvent('storeChanged', this.store.getCount());
		}
//		if (ddSource.grid.id === this.id) {
//			// DD on the same AttributesContainerPanel --> re-order the fields
//			var rows = ddSource.dragData.selections;
//			if (rows.length > 1) {
//				Ext.Msg.show({
//					   title:'Drop not allowed',
//					   msg: 'You can move only one field at a time',
//					   buttons: Ext.Msg.OK,
//					   icon: Ext.MessageBox.WARNING
//				});
//			} else {
//				var row = rows[0];
//				var rowIndex; // the row index on which the field has been dropped on
//				if(this.targetRow) {
//					rowIndex = this.getView().findRowIndex( this.targetRow );
//				}
//				if (rowIndex == undefined || rowIndex === false) {
//					rowIndex = undefined;
//				}
//
//	         	var rowData = this.store.getById(row.id);
//	         	/*
//	         	 * We suspend events since the remove method will raise the attributeRemoved event
//	         	 */
//	         	this.store.suspendEvents();
//            	this.store.remove(this.store.getById(row.id));
//            	this.store.resumeEvents();
//                if (rowIndex != undefined) {
//                	this.store.insert(rowIndex, rowData);
//                } else {
//                	this.store.add(rowData);
//                }
//
//		        this.getView().refresh();
//
//			}
//		} else {
//			// DD on another AttributesContainerPanel --> moving the fields from rows to columns or from columns to rows
//			var rows = ddSource.dragData.selections;
//			/*
//			 * operation must be performed in this order:
//			 * 1. add new rows
//			 * 2. remove rows from source
//			 * because the attributeRemoved is fired when removing the attribute from the source, and we may loose filters on domain values
//			 */
//			this.store.add(rows);
//			ddSource.grid.store.remove(rows);
//		}

		Sbi.trace("[AttributesContainerPanel.notifyDropFromAttributesContainerPanel]: OUT");
	}

	, addField : function (field) {
		//Default values
		field.sortable = true;
		field.width = 150;

		Sbi.trace("[QueryFieldsContainerPanel.addField]: IN");
		var data = Ext.apply({}, field); // making a clone
		var record = new this.Record(data);
		this.store.add(record);
		Sbi.trace("[QueryFieldsContainerPanel.addField]: field [" + Sbi.toSource(field)+ "] succesfully added");
		Sbi.trace("[QueryFieldsContainerPanel.addField]: OUT");
	}

	, getContainedAttributes: function () {
		var attributes = [];

		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			attributes.push(record.data);
		}
		return attributes;
	}

	, setAttributes: function (attributes) {

//		this.removeAllAttributes();
		for (var i = 0; i < attributes.length; i++) {
  			var attribute = attributes[i];
  			this.addAttribute(attribute);
  		}
	}

	, removeSelectedAttributes: function() {
        var sm = this.getSelectionModel();
        var rows = sm.getSelections();
        this.store.remove(rows);
	}

	, removeAllAttributes: function() {
		this.store.removeAll(false); // CANNOT BE SILENT!!! it must throw the clear event for attributeRemoved event
	}

	, itemDblClickHandler: function(grid, rowIndex, event) {
		var record = grid.store.getAt(rowIndex);
		this.removeSelectedAttributes();
//     	this.fireEvent("attributeDblClick", this, record.data);

	}

	, addAttribute : function (attribute) {

		var data = Ext.apply({}, attribute); // making a clone
		var row = new this.Record(data);
		this.store.add([row]);
		return row;
	}

	, validate: function (validFields) {

		this.validFields = validFields;
		var invalidFields = this.modifyStore(validFields);
		this.store.fireEvent("datachanged", this, null);
		return invalidFields;
	}

	, modifyStore: function (validFields) {
		var invalidFields='';

		var num = this.store.getCount();
		for(var i = 0; i < num; i++) {
			var record = this.store.getAt(i);
			var isValid = this.validateRecord(record,validFields);
			if(isValid == false){
				invalidFields+=''+record.data.alias+',';
			}
			record.data.valid = isValid;
		}

		return invalidFields;
	}

	, validateRecord: function (record, validFields) {
		var isValid = false;
		var i = 0;
		for(; i<validFields.length && isValid == false; i++){
			if(validFields[i].id == record.data.id){
			isValid = true;
			}
		}
		return isValid;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	, init: function() {
		this.initStore();
		this.initColumnModel();
	}

	, initStore: function() {
		Sbi.trace("[AttributesContainerPanel.initStore]: IN");

		this.store =  new Ext.data.ArrayStore({
	        fields: ['id', 'alias', 'funct', 'iconCls', 'nature', 'values', 'valid']
		});
		// if there are initialData, load them into the store
		if (this.initialData !== undefined) {
			for (i = 0; i < this.initialData.length; i++) {
				this.addAttribute(this.initialData[i]);
			}
		}
		this.store.on('remove', function (theStore, theRecord, index ) {
			this.fireEvent('attributeRemoved', this, theRecord.data);
		}, this);
		/*
		 * unfortunately, when removing all record with removeAll method, the event remove is not raised
		 */
		this.store.on('clear', function (theStore, theRecords ) {
			for (var i = 0 ; i < theRecords.length; i++) {
				var aRecord = theRecords[i];
				this.fireEvent('attributeRemoved', this, aRecord.data);
			}
		}, this);

		Sbi.trace("[AttributesContainerPanel.initStore]: OUT");
	}

	, initColumnModel: function() {
		Sbi.trace("[AttributesContainerPanel.initColumnModel]: IN");

        this.template = new Ext.XTemplate(this.renderTpl1);
        this.template.compile();

	    var fieldColumn = {
	    	header:  ''
	    	, flex: 0
	    	, dataIndex: 'alias'
	    	, hideable: false
	    	, hidden: false
	    	, sortable: false
	   	    , renderer : function(value, metaData, record, rowIndex, colIndex, store){
	   	    	Sbi.trace("[AttributesContainerPanel.renderGridRow]: IN");

	   	    	var templateData = Ext.apply({}, {
            		id: Ext.id()
            		, alias:  record.get("alias")
            		, iconCls: (record.data.valid != undefined && !record.data.valid)? 'x-btn-invalid': record.get("iconCls")
            	}, this.templateArgs);
        		var htmlFragment = this.template.apply(templateData);

        		Sbi.trace("[AttributesContainerPanel.renderGridRow]: OUT");
        		return htmlFragment;
	   	    	}
	        , scope: this
	    };
	    //this.cm = new Ext.grid.ColumnModel([fieldColumn]);
	    this.columns =[fieldColumn];

	    Sbi.trace("[AttributesContainerPanel.initColumnModel]: OUT");
	}

	, initRowSelectionModel: function(){
		this.sm = new Ext.grid.RowSelectionModel({
            singleSelect: false
		});
	}

	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		var dropTarget = new Sbi.cockpit.widgets.crosstab.GenericDropTarget(this, {
			ddGroup: this.ddGroup || 'worksheetDesignerDDGroup'
			, onFieldDrop: this.onFieldDrop
		});
	}

});