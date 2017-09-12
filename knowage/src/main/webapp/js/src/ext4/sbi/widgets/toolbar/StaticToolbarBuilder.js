Ext.define('Sbi.widget.toolbar.StaticToolbarBuilder', {
	statics: {
		
		/**
		 * Creates a new toolbar using the configuartion object "conf". adds all the items defined in the property conf.items.
		 * For the available buttons look at {@link Sbi.widget.toolbar.StaticToolbarBuilder.addButtonsToTheToolbar#addButtonsToTheToolbar}
		 * @param {Object} configuration object
		 */
		buildToolbar: function(conf){
			
			var toolbarConf = Ext.apply({},conf);
			toolbarConf.items = [ { xtype: 'tbspacer' }]
			var toolbar = Ext.create('Ext.toolbar.Toolbar', toolbarConf);
			
			toolbar = Sbi.widget.toolbar.StaticToolbarBuilder.addButtonsToTheToolbar(toolbar, conf);
			return toolbar;

		}

		/**
		 * Adds to the toolbar the buttons defined in the property conf.items.
		 * The available buttons are:
		 * - new
		 * - save
		 * - clone
		 * - test
		 * - ->
		 * 		@example
		 *		addButtonsToTheToolbar(toolbar, {items:[{name:'->'},{name:'test'},{name:'save'}]});
		 * @param {Object} configuration object
		 */
		, addButtonsToTheToolbar: function(toolbar, conf){
						
			for(var i=0; i<conf.items.length; i++){
				toolbar.add(Sbi.widget.toolbar.StaticToolbarBuilder.buildButtons(toolbar,conf.items[i]));
			}
			return toolbar;
		
		}

		/**
		 * Builds the buttons configuration. It builds the object and adds the linked event to the toolbar.
		 * For Example for the button save, the toolbar will throw the event save
		 * @param {Object} toolbar parent toolbar
		 * @param {Object} item the configuration of the button to add
		 */
		,buildButtons: function(toolbar, item){
			switch(item.name)
			{
			case 'new':
				return Sbi.widget.toolbar.StaticToolbarBuilder.addNewItemToolbarButton(toolbar, item);
			case 'clone':
				return Sbi.widget.toolbar.StaticToolbarBuilder.addCloneItemToolbarButton(toolbar, item);
			case 'save':
				return Sbi.widget.toolbar.StaticToolbarBuilder.addSaveItemToolbarButton(toolbar, item);
			case 'test':
				return Sbi.widget.toolbar.StaticToolbarBuilder.addTestItemToolbarButton(toolbar, item);
			case '->':
				return '->';
			default:
				return item;
			}
		}

		/**
		 * Builds the new button configuration. 
		 * @param {Object} toolbar parent toolbar
		 * @param {Object} item the type of the button to add
		 */
		,addNewItemToolbarButton: function(toolbar, conf){
			var toolbarconf = {
					//text: LN('sbi.generic.add'),
			         iconCls: 'icon-add',
			        handler: function(button, event) {
			        	toolbar.fireEvent("addnew");
					},
			        scope: this
			    };
			return Ext.apply(toolbarconf,conf||{});
		}
		
		/**
		 * Builds the clone button configuration. 
		 * @param {Object} toolbar parent toolbar
		 * @param {Object} item the type of the button to add
		 */
		,addCloneItemToolbarButton: function(toolbar, conf){
			var toolbarconf = {
					//text: LN('sbi.generic.clone'),
			         iconCls: 'icon-clone',
			        handler: function(button, event) {
			        	toolbar.fireEvent("clone");
					},
			        scope: this
			    };
			return Ext.apply(toolbarconf,conf||{});
		}
		
		/**
		 * Builds the save button configuration. 
		 * @param {Object} toolbar parent toolbar
		 * @param {Object} item the type of the button to add
		 */
		,addSaveItemToolbarButton: function(toolbar, conf){
			var toolbarconf = {
					//text: LN('sbi.generic.update'),
			         iconCls: 'icon-save',
			        handler: function(button, event) {
			        	toolbar.fireEvent("save");
					},
			        scope: this
			    };
			return Ext.apply(toolbarconf,conf||{});
		}
		
		/**
		 * Builds the add button configuration. 
		 * @param {Object} toolbar parent toolbar
		 * @param {Object} item the type of the button to add
		 */
		,addTestItemToolbarButton: function(toolbar, conf){
			var toolbarconf = {
					//text: LN('sbi.generic.update'),
			         iconCls: 'icon-test',
			        handler: function(button, event) {
			        	toolbar.fireEvent("test");
					},
			        scope: this
			    };
			return Ext.apply(toolbarconf,conf||{});
		}
		
	}
});