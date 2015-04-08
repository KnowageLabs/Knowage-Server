/**
 * Imported from http://www.sencha.com/forum/showthread.php?140793-Simple-Ext.ux.ColorField-plugin
 */

Ext.define('Ext.ux.ColorField', {
    extend: 'Ext.form.field.Trigger',
    alias: 'widget.colorfield',
    requires: ['Ext.form.field.VTypes', 'Ext.layout.component.field.Text'],

    lengthText: "Color hex values must be either 3 or 6 characters.",
    blankText: "Must have a hexidecimal value in the format ABCDEF.",

    regex: /^[0-9a-f]{3,6}$/i,


    validateValue : function(value){
        if(!this.getEl()) {
            return true;
        }
        if(value.length!=3 && value.length!=6) {
            this.markInvalid(Ext.String.format(this.lengthText, value));
            return false;
        }
        if((value.length < 1 && !this.allowBlank) || !this.regex.test(value)) {
            this.markInvalid(Ext.String.format(this.blankText, value));
            return false;
        }

        this.markInvalid();
        this.setColor(value);
        return true;
    },

    markInvalid : function( msg ) {
        Ext.ux.ColorField.superclass.markInvalid.call(this, msg);
        this.inputEl.setStyle({
            //'background-image': 'url(../resources/themes/images/default/grid/invalid_line.gif)'
        });
    },

    setValue : function(hex){
        Ext.ux.ColorField.superclass.setValue.call(this, hex);
        this.setColor(hex);
    },

    setColor : function(hex) {
    	var hexColor =  (hex.indexOf('#')>=0)? hex : '#' + hex;
        Ext.ux.ColorField.superclass.setFieldStyle.call(this, {
            'background-color': hexColor,
            'background-image': 'none'
        });
		this.fireEvent('colorUpdate', hexColor);



    },

    menuListeners : {
        select: function(m, d){
            this.setValue(d);
        },
        show : function(){
            this.onFocus();
        },
        hide : function(){
            this.focus();
            var ml = this.menuListeners;
            this.menu.un("select", ml.select,  this);
            this.menu.un("show", ml.show,  this);
            this.menu.un("hide", ml.hide,  this);
        }
    },

    onTriggerClick : function(e){
        if(this.disabled){
            return;
        }

        this.menu = new Ext.menu.ColorPicker({
            shadow: true,
            autoShow : true
        });
        this.menu.alignTo(this.inputEl, 'tl-bl?');
        this.menu.doLayout();

        this.menu.on(Ext.apply({}, this.menuListeners, {
            scope:this
        }));

        this.menu.show(this.inputEl);
    }
});