/**
 * @author Benedetto Milazzo (benedetto.milazzo@eng.it)
 */

Ext.define('Sbi.chart.designer.components.ColorPicker', {
	extend: 'Ext.form.field.Text',
	requires: ['Ext.form.field.VTypes'],
	
	alias: 'widget.designerColorPicker',

	lengthText: LN('sbi.chartengine.designer.components.colorpicker.lengthText'),
	blankText: LN('sbi.chartengine.designer.components.colorpicker.blankText'),

	regex6Digits: /^[0-9a-f]{6}$/i,
	regexTransparent: /^transparent$/i,
	
	triggers: {
		picker: {
			handler: 'onTriggerClick', 
			scope: 'this'
		}
	},

	/**
	 * Listen for every change that is made for the value of the color text field
	 * (manually (typing the hexadecimal code value of the color or picking one
	 * among predefined ones) or by applying a style). If the field for which the
	 * change is made is mandatory for particular chart type, fire an appropriate
	 * event depending on the content of the color text field (valid or not valid).
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	listeners:
	{
		change: function(el,color)
		{				
			if (this.isColorMandatory)
			{				
				if (color!="" && color.toLowerCase()!="transparent" && this.validateValue(color))
				{
					this.fireEvent("colorRendered",this.config.initiator);
				}
				else
				{
					this.fireEvent("colorNotValid",this.config.initiator);
				}
			}
		}		
	},
	
	validateValue : function(value){
		if(!this.getEl()) {
			return true;
		}

		if(value !== undefined && value !== null && value !== ''){
			if(value.length != 6 && value.length != ('transparent').length) {
				this.markInvalid(Ext.String.format(this.lengthText, value));
				this.setColor('transparent');
				return false;
			}
			if((value.length < 1 && !this.allowBlank) 
					|| (!this.regex6Digits.test(value) && !this.regexTransparent.test(value))) {
				
				this.markInvalid(Ext.String.format(this.blankText, value));
				this.setColor('transparent');
				return false;
			}	 
		}
		
		this.markInvalid();
		this.setColor(value);
		return true;
	},

	markInvalid : function( msg ) {
		Sbi.chart.designer.components.ColorPicker.superclass.markInvalid.call(this, msg);
	},

	setValue : function(hex){
		if(this.menu && this.menu.close) {
			this.menu.close();
		}
		
		var value = (hex || "").toUpperCase();
		
		Sbi.chart.designer.components.ColorPicker.superclass.setValue.call(this, value);
		if(this.regex6Digits.test(value) || this.regexTransparent.test(value)) {
			this.setColor(value);	
			
//			/**
//			 * Important when this component is mandatory for the chart.
//			 * Firing an event will inform us that color is picked and we
//			 * don't need the flag that warns the user.
//			 * 
//			 * @author: danristo (danilo.ristovski@mht.net)
//			 */
//			this.fireEvent("colorPicked",this.config.initiator);
		}
	},
	
	setRawValue : function(hex){
		if(this.menu && this.menu.close) {
			this.menu.close();
		}
		
		var value = (hex || "").toUpperCase();

		Sbi.chart.designer.components.ColorPicker.superclass.setRawValue.call(this, hex);
		if(this.regex6Digits.test(value) || this.regexTransparent.test(value)) {
			this.setColor(value);
		}
	},

	setColor : function(colorValue) {
		if(colorValue !== undefined && colorValue !== null && colorValue !== ''){
			var hexColor = this.regex6Digits.test(colorValue)?
					(colorValue.indexOf('#') >= 0 )? colorValue : '#' + colorValue
							: colorValue;
			Sbi.chart.designer.components.ColorPicker.superclass.setFieldStyle.call(this, {
				'background-color': hexColor,
				'background-image': 'none'
			});
		} else{
//			var hexColor = '#FFF';
			var hexColor = 'transparent';
			Sbi.chart.designer.components.ColorPicker.superclass.setFieldStyle.call(this, {		    			
				'background-color': hexColor,
				'background-image': 'none'
			});
		}
	},
	
	getColor: function(){
		var colorValue = '' + this.getValue().replace('#', '').trim();
		
		if(colorValue != '' && this.regex6Digits.test(colorValue)) {
			colorValue = '#' + colorValue;
		}
		
		return colorValue;
	},

	onTriggerClick : function(e){
		if(this.disabled){
			return;
		}

		if(this.menu) {
			this.menu.destroy();
		}
		
		this.menu = new Ext.menu.ColorPicker({
			shadow: true,
			autoShow : true,
//			closable : true,
			closeAction : 'destroy',
			
			listeners: {
				afterrender : function(){
					this.focus();
					this.onFocus();
				},
			}
		});
		this.menu.alignTo(this.inputEl, 'tl-bl?');
		this.menu.doLayout();

		this.menu.on(Ext.apply({}, this.menuListeners, {
			scope:this
		}));
	},
	
	menuListeners : {
		select: function(component, selectedColor){
			var color = '' + (selectedColor || '');
			
			if(this.fieldBind && this.viewModel){
				var fieldBind = this.fieldBind;
				var bindValue = fieldBind.replace(/\{\w+\.(\w+)\}/, '$1');
				
				this.viewModel.data.configModel.set(bindValue, color);
			}
			
			this.setValue(color);
		},
		close : function(){
			this.menu.focus();
			var ml = this.menuListeners;
			this.menu.un("select", ml.select, this);
			this.menu.un("close", ml.close, this);
			this.menu.un("hide", ml.hide, this);
		},
		hide : function(){
			this.menu.focus();
			var ml = this.menuListeners;
			this.menu.un("select", ml.select, this);
			this.menu.un("close", ml.close, this);
			this.menu.un("hide", ml.hide, this);
			this.menu.destroy();
		},
	},
});