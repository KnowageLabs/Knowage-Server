Ext.define('Sbi.behavioural.analyticalDriver.AnalyticalDriverUsePanel', {
			extend: 'Ext.tab.Panel'
			
				,config: {
					defaults: {
						width: 400
					},   
				
					fieldDefaults: {
						labelAlign: 'right',
						msgTarget: 'side'
					},
					border: false
				}
				,constructor: function(config) {
					
					this.initConfig(config);
					this.initFields();
					this.items=[this.newUse];
					this.callParent(arguments);
					
				},
				
				initFields: function(){
					
					this.adid = Ext.create("Ext.form.field.Text", {
			    		//name: "ID",
			    		fieldLabel: "AD ID"
			    	});
					
					this.useid = Ext.create("Ext.form.field.Text", {
			    		//name: "USEID",
			    		fieldLabel: "Use ID"
			    	});
					
					this.name = Ext.create("Ext.form.field.Text", {
			    		//name: "Name",
			    		fieldLabel: "Name"
			    	});
					
					this.label = Ext.create("Ext.form.field.Text", {
			    		//name: "Label",
			    		fieldLabel: "Label"
			    	});
					
					this.description = Ext.create("Ext.form.field.Text", {
			    		//name: "Desc",
			    		fieldLabel: "Description"
			    	});
					

					this.newUse = new Ext.form.Panel({
						
						title: 'New...',
						items: [this.adid, this.useid, this.name, this.label, this.description]
						
					});
					
				}
});