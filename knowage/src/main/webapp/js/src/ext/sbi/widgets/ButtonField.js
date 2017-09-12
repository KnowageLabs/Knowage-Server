/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
  
 
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

Sbi.widgets.ButtonField = function(config){
	Ext.form.Field.superclass.constructor.call(this, config);    
};

Ext.extend(Sbi.widgets.ButtonField, Ext.form.Field,  {
     
     // private
    autoWidth : function(){
        if(this.el){
            this.el.setWidth("auto");
            if(Ext.isIE7 && Ext.isStrict){
                var ib = this.el.child('button');
                if(ib && ib.getWidth() > 20){
                    ib.clip();
                    ib.setWidth(Ext.util.TextMetrics.measure(ib, this.text).width+ib.getFrameWidth('lr'));
                }
            }
            if(this.minWidth){
                if(this.hidden){
                    this.el.beginMeasure();
                }
                if(this.el.getWidth() < this.minWidth){
                    this.el.setWidth(this.minWidth);
                }
                if(this.hidden){
                    this.el.endMeasure();
                }
            }
        }
    },
    // private
    onClick : function(e){
        if(e){
            e.preventDefault();
        }
        if(!this.disabled){
            if(this.enableToggle){
                this.toggle();
            }
            if(this.menu && !this.menu.isVisible()){
                this.menu.show(this.el, this.menuAlign);
            }
            this.fireEvent("click", this, e);
            if(this.handler){
                this.el.removeClass("x-btn-over");
                this.handler.call(this.scope || this, this, e);
            }
        }
    },
    // private
    onMouseOver : function(e){
        if(!this.disabled){
            this.el.addClass("x-btn-over");
            this.fireEvent('mouseover', this, e);
        }
    },
    // private
    onMouseOut : function(e){
        if(!e.within(this.el,  true)){
            this.el.removeClass("x-btn-over");
            this.fireEvent('mouseout', this, e);
        }
    },
    // private
    onMouseDown : function(){
        if(!this.disabled){
            this.el.addClass("x-btn-click");
            Ext.get(document).on('mouseup', this.onMouseUp, this);
        }
    },
    // private
    onMouseUp : function(){
        this.el.removeClass("x-btn-click");
        Ext.get(document).un('mouseup', this.onMouseUp, this);
    },
    // private
    onMenuShow : function(e){
        this.el.addClass("x-btn-menu-active");
    },
    // private
    onMenuHide : function(e){
        this.el.removeClass("x-btn-menu-active");
    }  ,
   onRender: function (ct) {

        if(!Ext.form.Button.buttonTemplate){
        	// hideous table template
            Ext.form.Button.buttonTemplate = new Ext.Template(
            		'<table border="0" cellpadding="0" cellspacing="0" class="x-btn-wrap"><tbody><tr>',
                    '<td class="x-btn-left"><i>&#160;</i></td><td class="x-btn-center"><em><button class="x-btn-text">{0}</button></em></td><td class="x-btn-right"><i>&#160;</i></td><td style="padding-left:4px"></td>',
                    "</tr></tbody></table>");
            }
        	this.template = Ext.form.Button.buttonTemplate;            
        	btn = this.template.append(ct, [this.text || '&#160;'], true);      
        	this.el =btn;
        	var btnEl = btn.child("button:first");
    
            if(this.cls){
                btn.addClass(this.cls);
            }
            
            if(this.icon){
                btnEl.setStyle('background-image', 'url(' +this.icon +')');
            }
            
            if(this.tooltip){
                if(typeof this.tooltip == 'object'){
                    Ext.QuickTips.tips(Ext.apply({
                          target: btnEl.id
                    }, this.tooltip));
                } else {
                    btnEl.dom[this.tooltipType] = this.tooltip;
                }
            }
            if (this.descriptionText)
            	btn.insertHtml("beforeBegin",this.descriptionText);
            
            if(this.id){
            	this.el.dom.id = this.el.id = this.id;
            }
            
            if(this.menu){
	            this.el.child(this.menuClassTarget).addClass("x-btn-with-menu");
	            this.menu.on("show", Ext.Button.onMenuShow, this);
	            this.menu.on("hide", Ext.Button.onMenuHide, this);
            }     
            btn.addClass("x-btn");
            
            alert('brkp1');
            
            if(Ext.isIE && !Ext.isIE7){
            	this.autoWidth.defer(1, this);
            }else{
            	this.autoWidth();
            }        
	        btn.on("click", this.onClick, this);
	        btn.on("mouseover", this.onMouseOver, this);
	        btn.on("mouseout", this.onMouseOut, this);
	        btn.on("mousedown", this.onMouseDown, this);
	        //btn.on("mouseup", this.onMouseUp, this);
	        if(this.hidden){
	            this.hide();
	        }
	        if(this.disabled){
	            this.disable();
	        }
	        Ext.ButtonToggleMgr.register(this);
	        if(this.pressed){
	            this.el.addClass("x-btn-pressed");
	        }
	        if(this.repeat){
	        	var repeater = new Ext.util.ClickRepeater(btn,
	                typeof this.repeat == "object" ? this.repeat : {}
	            );
	        	repeater.on("click", this.onClick,  this);
	        }               
   } //end render
});
    
