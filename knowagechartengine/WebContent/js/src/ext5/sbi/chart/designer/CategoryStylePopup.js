/**
 * The popup that provide user the opportunity to specify the attribute (column) according
 * to which the chart (the result) will be constructed. In other words, this option provides
 * opportunity for the user to reorder results on the on the edge of the chart's circle (for 
 * RADAR and PIE charts) or on the X-axis of the rectangular chart (every other that has this
 * option - ordering by column: BAR, HEATMAP, LINE, SCATTER). 
 * 
 * ------------------------------------------
 * Inside the popup there are two comboboxes: 
 * ------------------------------------------
 * 	(1) order by column: 	user can pick the column (attribute of the dataset that is picked for
 * 							the document) among all available attributes provided by the dataset.
 * 	(2) order type: 		the type of ordering of the table that is the result of ordering by
 * 							the column that is picked. It can be ascending (ASC) or descending 
 * 							(DESC).
 * 
 * =================================================================================================
 * NOTE: For some chart types this option is disabled (the tool is hidden) since they cannot handle 
 * this feature. It is expected that also some of them, the ones that possess technical base for such 
 * a feature (like SUNBURST) should have this option in the future.   
 * =================================================================================================
 * 
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
Ext.define
(
	'Sbi.chart.designer.CategoryStylePopup', 
	
	{
		extend: 'Ext.form.Panel',
		id: 'categoryStylePopup',
	    title: LN("sbi.chartengine.designer.categorystyleconf"),	
	    
	    /**
	     * Customization taken from the older file of the similar nature - SerieStylePopup.js.
	     * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	     */
	    layout: 'border',
	    bodyPadding: 5,
		floating: true,
	    draggable: true,
	    closable : true,
	    closeAction: 'destroy',
	    
	    modal: true,
	    
		config: 
		{
			store: '',
			rowIndex: ''
		},
	    
		width: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.width,
	    height: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.height,
	    resizable: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.resizable,
	    overflowY: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.overflowY,
		
	    constructor: function(config)
    	{
	    	/**
	    	 * Important! Without the call of the constructor of the parent element (class, type)
	    	 * of this component, the one cannot be configured and rendered (cannot be shown when
	    	 * the user wants to open the window).
	    	 */
	    	this.callParent(config);
	    	
	    	store = config.store,	    	
			rowIndex = config.rowIndex;
	    	
			var dataAtRow = store.getAt(rowIndex);
			
			// Esc key pressing closes the modal
			this.keyMap = new Ext.util.KeyMap(Ext.getBody(), [{
				key: Ext.EventObject.ESC,
				defaultEventAction: 'preventDefault',
				scope: this,
				fn: function() {
					this.destroy()
				}
			}]);
			
			/**
			 * Taken from the SerieStylePopup.js.
			 */
			var LABEL_WIDTH = 115;
	    	
	    	this.categoryFieldSet = Ext.create
	    	(
    			'Ext.form.FieldSet', 
    			
    			{
					collapsible: true,
					title: LN("sbi.chartengine.structure.categoryStyleConfig.title"),	
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : LABEL_WIDTH
					},
				
					layout: 'anchor',					
					items : []
    			}
			);	
	    	
	    	categoryOrderColumn = dataAtRow.get('categoryOrderColumn') ? dataAtRow.get('categoryOrderColumn') : '';
	    	
	    	this.allAttributesComboBox = Ext.create
	    	(
    			'Ext.form.ComboBox', 
    			{
					store: 
					{
						fields: ['categoryColumn', 'categoryDataType'],
						data: config.categoriesPickerStore
					},
					
					value: (categoryOrderColumn && categoryOrderColumn.trim() != '') ? categoryOrderColumn.trim() : '',
					valueField: 'categoryColumn',
					displayField: 'categoryColumn',
					fieldLabel : LN("sbi.chartengine.structure.categoryStyleConfig.orderByColumn.labelText"),	
					editable: false,
					emptyText: LN("sbi.chartengine.structure.categoryStyleConfig.orderByColumn.emptyText")
    			}
			);	
	    	
	    	var categoryOrderType = dataAtRow.get('categoryOrderType');
	    	
			this.categoryOrderTypeCombo = Ext.create
			(
				'Sbi.chart.designer.SeriesOrderCombo', 
				{
					value: (categoryOrderType && categoryOrderType.trim() != '') ? categoryOrderType.trim() : '',
				}
			);
			
			this.categoryFieldSet.add(this.allAttributesComboBox);
			this.categoryFieldSet.add(this.categoryOrderTypeCombo);
	    	
	    	this.add(this.categoryFieldSet);	    	
    	},
    	
    	writeConfigsAndExit: function() 
    	{
    		var categoryOrderColumn = this.allAttributesComboBox.getValue();
    		var categoryOrderType = this.categoryOrderTypeCombo.getValue();
    		
    		var dataAtRow = store.getAt(rowIndex);    		
    		
    		dataAtRow.set('categoryOrderColumn', categoryOrderColumn);
    		dataAtRow.set('categoryOrderType', categoryOrderType);
    		
    		this.destroy();
    	},
    	
    	// Cancel and Save buttons
        buttons: 
    	[
    	 	{
    	 		text: LN('sbi.generic.cancel'),
    	 		
    	 		handler: function() 
    	 		{
    	 			Ext.getCmp('categoryStylePopup').destroy();
    	 		}
    	 	}, 
    	 	
    	 	{
    	 		text: LN('sbi.generic.save'),
            
    	 		handler: function() 
    	 		{
    	 			Ext.getCmp('categoryStylePopup').writeConfigsAndExit();
    	 		}
    	 	}
	 	],
	}
);