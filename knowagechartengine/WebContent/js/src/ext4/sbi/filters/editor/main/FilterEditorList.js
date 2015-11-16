/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**

  * Authors
  *
  * - Antonella Giachino (antonella.giachino@eng.it)
  */

Ext.define('Sbi.filters.editor.main.FilterEditorList', {
	extend: 'Ext.Panel'
	, layout: 'fit'
	, config:{
		  services: null
		, grid: null
		, store: null
		, storesList: null
		, filters: null
		, border: false
		, height: 180
		, autoScroll: false
	}
	/**
	 * @property grid
	 * The grid with the list of filters
	 */
	, grid: null

	/**
	 * @property currentFilter
	 * The current filter selected in the list
	 */
	, currentFilter : null

	/**
	 * @property analyticalDriversStore
	 * The store with all cockpit's analytical drivers
	 */
	, analyticalDriversStore: null

	, constructor : function(config) {
		Sbi.trace("[FilterEditorList.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
//		this.addEvents("addAssociation","removeAssociation","selectAssociation","updateIdentifier");
		Sbi.trace("[FilterEditorList.constructor]: OUT");
	}

	, initComponent: function() {
        Ext.apply(this, {
            items: [this.grid]
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
		this.initStore();
		this.initGrid();
	}

	, initStore: function() {
		Sbi.trace("[FilterEditorList.initStore]: IN");

		var initialData = [];

		if (this.filters !== null ){
		   for (var i=0; i< this.filters.length; i++){
				if (Sbi.isValorized(this.filters[i])) {
					var el = [this.filters[i].id,
					          this.filters[i].labelObj,
					          this.filters[i].nameObj,
					          this.filters[i].typeObj,
					          this.filters[i].namePar,
					          this.filters[i].typePar,
					          this.filters[i].scope,
					          this.filters[i].initialValue
					          ];
					initialData.push(el);
				}
		   }
		}

		this.store = new Ext.data.JsonStore({
				fields: [
				         'id',
				         'labelObj',
				         'nameObj',
				         'typeObj',
				         'namePar',
				         'typePar',
				         'scope',
				         'initialValue'
				         ]
				, data: initialData
		  });



//		  if (Sbi.isValorized(this.filters) && this.filters.length == 0 ){
			  if (this.storesList !== null ){
				   for (var i=0; i< this.storesList.length; i++){
						   Ext.Ajax.request({
								url: Sbi.config.serviceReg.getServiceUrl("loadDataSetParams", {
									pathParams: {datasetLabel: this.storesList[i]}
								}),
								success : function(response, options) {
									if(response !== undefined && response.responseText !== undefined && response.statusText=="OK") {
										if(response.responseText!=null && response.responseText!=undefined){
											if(response.responseText.indexOf("error.mesage.description")>=0){
												Sbi.exception.ExceptionHandler.handleFailure(response);
											} else {
												var r = Ext.util.JSON.decode(response.responseText);
												if (Sbi.isValorized(r.results) && r.results.length > 0){
													var f = r.results[0].id;
													var isFilterLoaded = Sbi.isValorized(Sbi.storeManager.getParameter(f));
													if (Sbi.isValorized(r.results) && !isFilterLoaded){
														this.store.loadData(r.results,true);
													}
												}
											}
										}
									} else {
										Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
									}
								},
								scope: this,
								failure: Sbi.exception.ExceptionHandler.handleFailure
							});
					   }

			   }
//		  }

		  //analitycal driver store (for combo of initial value)
		  var storeConfig = {
				   proxy:{
				    	type : 'rest',
				    	url : Sbi.config.serviceReg.getServiceUrl("loadDocumentParams", {
							pathParams: {documentLabel: Sbi.config.docLabel}
						}),
				    	reader : {type : 'json',root : 'results'}
				   	},
				   	autoLoad: true,
				   	fields: ['label', 'url'],
				   	listeners: {
                        'load': function (store, records, options) {
                        	var nullRecordDef = Ext.data.Record.create(['label', 'url']);
                            this.insert(0, new nullRecordDef({label:'--', url:'--'}));
                        }
                    }
		};
		this.analyticalDriversStore = Ext.create('Ext.data.Store', storeConfig);

		Sbi.trace("[FilterEditorList.initStore]: OUT");
	}

    , initGrid: function() {
    	var thisPanel = this;
    	var c = this.gridConfig;

        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        });

        var comboScope = new Ext.form.ComboBox({
			name : 'comboScope',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'comboScopeType',
		            'comboScopeValue'
		        ],
		        data: [ ['--', '--'] , ['Static', 'Static'], ['Relative', 'Relative']]
		    }),
			displayField : 'comboScopeType',
			valueField : 'comboScopeValue',
			typeAhead : true, forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			editable : false,
			allowBlank : true,
			validationEvent : false,
			queryMode: 'local'
			});

        this.textFieldEditor = new Ext.form.TextField({});

        this.comboEditor = new Ext.form.ComboBox({
//        	triggerCls: 'x-form-search-trigger' //lente
           store: this.analyticalDriversStore
          , displayField : 'label'
		  , valueField : 'url'
		  , mode : 'local'
		  ,	triggerAction : 'all'
		  ,	selectOnFocus : true
		  ,	editable : false
		  ,	validationEvent : false
		  , queryMode: 'local'
        });

        this.grid = Ext.create('Ext.grid.Panel', Ext.apply(c || {}, {
	        store: this.store,
	        selModel: {selType: 'rowmodel', mode: 'SINGLE', allowDeselect: true},
	        columns: [
                { dataIndex: 'id'
                , hidden: true
	            },{dataIndex: 'label'
                 , hidden: true
	            },{
	              header: LN('sbi.cockpit.filter.editor.wizard.list.nameObj')
            	, width: "15%"
            	, sortable: true
            	, dataIndex: 'nameObj'
            	, flex: 1
            	, renderer: Ext.Function.bind(this.fixedCellRender, this)
            	, style: 'font-weight:bold;'
            	}, {
        		  header: LN('sbi.cockpit.filter.editor.wizard.list.typeObj')
            	, width: "15%"
            	, sortable: true
            	, dataIndex: 'typeObj'
            	, renderer: Ext.Function.bind(this.fixedCellRender, this)
            	, style: 'font-weight:bold;'
            	},{
          		  header: LN('sbi.cockpit.filter.editor.wizard.list.namePar')
              	, width: "15%"
              	, sortable: true
              	, dataIndex: 'namePar'
              	, renderer: Ext.Function.bind(this.fixedCellRender, this)
              	, style: 'font-weight:bold;'
              	},{
          		  header: LN('sbi.cockpit.filter.editor.wizard.list.typePar')
              	, width: "15%"
              	, sortable: true
              	, dataIndex: 'typePar'
              	, renderer: Ext.Function.bind(this.fixedCellRender, this)
              	, style: 'font-weight:bold;'
              	},{
          		  header: LN('sbi.cockpit.filter.editor.wizard.list.scope')
              	, width: "15%"
              	, sortable: true
              	, dataIndex: 'scope'
              	, editor: comboScope
              	, style: 'font-weight:bold;'
                },{
          		  header: LN('sbi.cockpit.filter.editor.wizard.list.initialValue')
          		, id: 'cmbInitialValue'
              	, width: "15%"
              	, sortable: true
              	, dataIndex: 'initialValue'
              	, getEditor: Ext.Function.bind(this.getCellEditor, this)
              	, style: 'font-weight:bold;'
              	}
            ],
	        viewConfig: {
	        	stripeRows: true
	        },
	        plugins: [cellEditing]
	    }));
//        this.grid.on('itemclick', this.onCellClick, this);
        this.grid.on('edit', function(editor, e) {
            // commit the changes right after editing finished
            e.record.commit();
        }, this);
    }


	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
    , getFiltersList: function(){
    	if (this.filters == null) this.filters = [];
    	for (var i=0; i<this.store.data.length;i++){
    		var rec = this.store.getAt(i);
    		var filter = {
    				id:  rec.get('id')
    			  , labelObj: rec.get('labelObj')
    			  , typeObj: rec.get('typeObj')
    			  , nameObj: rec.get('nameObj')
    			  , namePar: rec.get('namePar')
    			  , typePar: rec.get('typePar')
    			  , scope: rec.get('scope')
    			  , initialValue: rec.get('initialValue')
    		};
    		this.filters.push(filter);
    	}
    	return this.filters;
    }

    , setFiltersList: function(f){
    	this.filters = f;
    }

    , removeAllFilters: function(){
    	this.filters = new Array();
    }


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------


   ,  getCellEditor : function(record) {
	    var toReturn = null;

		if(record.get('scope') == 'Relative'){
			toReturn = new Ext.grid.CellEditor({field: this.comboEditor});
			if (this.comboEditor.store.data.length == 1){
    			alert('Il documento che si sta gestendo non ha driver analitici associati. \n'+
    		      'Associare Driver Analitici al documento oppure definire filtri solo di tipo STATICO ! ');
			}
		}else{
			toReturn = new Ext.grid.CellEditor({field: this.textFieldEditor});
		}
		return toReturn;
   }

   , fixedCellRender : function (value, metaData, record, rowIndex, colIndex, store, view) {
       if (colIndex <5 ){
            metaData.attr = 'style="background-color:#f3f3f3 !important;"';
       }
        return value;
    }



});