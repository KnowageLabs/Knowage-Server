/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2008 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 
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
  * - name (mail)
  */


Ext.ns("Sbi.widgets");

Sbi.widgets.TriggerFieldMultiButton = function(config) {	
	Sbi.widgets.TriggerFieldMultiButton.superclass.constructor.call(this, config);	
	this.state = this.EDITING;
};

Ext.extend(Sbi.widgets.TriggerFieldMultiButton, Ext.form.ComboBox, {
  // Additional buttons definition to be passed in instance definition.
  imgButtons:[{
    //applyTo: 'whatever', // -> Use existing IMG tag on page...
    //src: 'images/whatever.gif', // -> ...or a url to an image.
    //onTrigger: function(){},
    //triggerClass: 'whateverClassName',
    //hideTrigger:true
  }],

  initComponent : function(){
    this.triggerConfig = {
      tag:'span',
      cls:'x-form-twin-triggers',
      cn:[{
        tag: "img",
        src: Ext.BLANK_IMAGE_URL,
        cls: "x-form-trigger " + this.triggerClass
      }]
    };

    // Use the additional btn definitions to create more trigger definitions.
    for (var i=0, maxi=this.imgButtons.length; i<maxi; i++) {
      var btn = this.imgButtons[i];
      var imgElem = Ext.get(btn.applyTo);
      this.triggerConfig.cn[i+1] = {
        tag: "img",
        src: btn.src || ((imgElem) ? imgElem.dom.src : Ext.BLANK_IMAGE_URL),
        cls: ((btn.triggerClass) ? 'x-form-trigger ' + btn.triggerClass : 'x-form-trigger')
      }
    }

    Ext.ux.MultiTriggerComboBox.superclass.initComponent.call(this);
  },

  // Override TriggerField's method.
  initTrigger : function(){
    var ts = this.trigger.select('.x-form-trigger', true);
    this.wrap.setStyle('overflow', 'hidden');
    var triggerField = this;
    ts.each(function(t, all, index){
      t.hide = function(){
        var w = triggerField.wrap.getWidth();
        this.dom.style.display = 'none';
        triggerField.el.setWidth(w-triggerField.trigger.getWidth());
      };

      t.show = function(){
        var w = triggerField.wrap.getWidth();
        this.dom.style.display = '';
        triggerField.el.setWidth(w+triggerField.trigger.getWidth()); // This was a minus sign in the original code: bug??
      };

      if (this.hideTrigger) {
        t.dom.style.display = 'none';
      }

      if (index == 0) {
        // The first one is always the default dropdown button.
        t.on("click", this['onTriggerClick'], this, {preventDefault:true});
      } else {
        var btnDef = this.imgButtons[index-1];
        if (btnDef.hideTrigger) {
          t.dom.style.display = 'none';
        }
        t.on("click", btnDef.onTrigger, this, {preventDefault:true});
      }
      t.addClassOnOver('x-form-trigger-over');
      t.addClassOnClick('x-form-trigger-click');
    }, this);
    this.triggers = ts.elements;
  },

  getTrigger : function(index){
    return this.triggers[index];
  }

});