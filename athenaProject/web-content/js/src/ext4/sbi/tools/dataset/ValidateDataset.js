Ext.define('Sbi.tools.dataset.ValidateDataset', {
	extend: 'Ext.Panel'
	
	,config: {
		id: 'dsValidationGrid',
		border: false,
		frame: false,
		fieldsColumns:null,
		selModel:null,
		emptyStore: true,
        store: null,		        
        frame: true,
        autoscroll: true
	}


	, constructor: function(config) {
		
		var panelItems;
		panelItems = this.initDatasetValidationPanel(panelItems,config);
		
		config.items = [panelItems];
		
		thisDatasetValidationPanel = this;

		Ext.apply(this, config || {});
	
	    this.callParent(arguments);
	    
		//invokes before each ajax request 
	    Ext.Ajax.on('beforerequest', this.showMask, this);   
	    // invokes after request completed 
	    Ext.Ajax.on('requestcomplete', this.hideMask, this);            
	    // invokes if exception occured 
	    Ext.Ajax.on('requestexception', this.hideMask, this); 
	}
	
	,initDatasetValidationPanel : function(items,config){
		

    	this.warningMessage = new Ext.form.Label({
			text :  LN('sbi.ds.metadata.msgOK'),
			width:  400,
			name: 'warningMessage',
			readOnly:true,
			style: 'color: #1ec31b'
		});

		
		// Main Panel ----------------------
		
		this.mainPanel = new Ext.Panel({
			  margins: '50 50 50 50',
	          labelAlign: 'left',
	          bodyStyle:'padding:5px',
			  defaultType: 'displayfield',
			  height: 330, //318,
			  layout: 'form',
			  items: [this.warningMessage]
			});
		
		return this.mainPanel;
	}
	
	//Public Methods
	
	, createDynamicGrid: function(values){
		this.mainPanel.remove(this.warningMessage);
    	this.warningMessage = new Ext.form.Label({
			text :  LN('sbi.ds.metadata.msgOK'),
			width:  400,
			name: 'warningMessage',
			readOnly:true,
			style: 'color: #1ec31b'
		});
		//remove previous instance of grid (if any)
		if ((this.gridDataset != null) && (this.gridDataset != undefined)){
			this.mainPanel.remove(this.gridDataset);

		}
		this.gridDataset = new Sbi.tools.dataset.ValidateDatasetGrid(values);
		this.mainPanel.add(this.gridDataset);
    	this.mainPanel.add(this.warningMessage);

		
		this.gridDataset.on('validationErrorFound', this.showError, this);

	}
	
	, showError: function(){
    	Sbi.debug('ValidateDataset SHOWING ERROR');
		this.mainPanel.remove(this.warningMessage);
    	this.warningMessage = new Ext.form.Label({
			text : LN('sbi.ds.metadata.msgKO'),
			width:  400,
			name: 'warningMessage',
			readOnly:true,
			style: 'color: #900'
		});
    	this.mainPanel.add(this.warningMessage);
	}

	
	/**
	 * Opens the loading mask 
	*/
    , showMask : function(){
    	this.un('afterlayout',this.showMask,this);
    	if (this.loadMask == null) {    		
    		this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "  Wait...  "});
    	}
    	if (this.loadMask){
    		this.loadMask.show();
    	}
    }

	/**
	 * Closes the loading mask
	*/
	, hideMask: function() {
    	if (this.loadMask && this.loadMask != null) {	
    		this.loadMask.hide();
    	}
	} 
});
