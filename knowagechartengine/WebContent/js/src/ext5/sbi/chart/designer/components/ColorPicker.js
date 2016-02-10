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
		var value = (hex || "").toUpperCase();
		
		Sbi.chart.designer.components.ColorPicker.superclass.setValue.call(this, value);
		if(this.regex6Digits.test(value) || this.regexTransparent.test(value)) {
			this.setColor(value);
		}
	},
	
	setRawValue : function(hex){
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

		this.menu = new Ext.menu.ColorPicker({
			shadow: true,
			autoShow : true,
//			closable : true,
//			closeAction : 'destroy'
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
//		show : function(){
//			this.onFocus();
//		},
		hide : function(){
			this.focus();
			var ml = this.menuListeners;
			this.menu.un("select", ml.select, this);
//			this.menu.un("show", ml.show, this);
			this.menu.un("hide", ml.hide, this);
		},
	},
});