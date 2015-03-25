/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


	/**
	 * patch a bug in the datepicker (see SPAGOBI-324). 
	 * The error is in the way Ext.util.ClickRepeater manage repeated click event. The problem seem to be related to the event 
	 * mouseup that is not handled properly by ClickRepeater. This cause the click event sequence to continue even after the
	 * mouse button is released. This patch is not resolutive. It is just a work-around that eliminate ClickRepeater behaviour 
	 */



	Ext.override(Ext.DatePicker, {
		 // private
		
	    onRender : function(container, position){
	        var m = [
	             '<table cellspacing="0">',
	                '<tr><td class="x-date-left"><a href="#" title="', this.prevText ,'">&#160;</a></td><td class="x-date-middle" align="center"></td><td class="x-date-right"><a href="#" title="', this.nextText ,'">&#160;</a></td></tr>',
	                '<tr><td colspan="3"><table class="x-date-inner" cellspacing="0"><thead><tr>'];
	        var dn = this.dayNames;
	        for(var i = 0; i < 7; i++){
	            var d = this.startDay+i;
	            if(d > 6){
	                d = d-7;
	            }
	            m.push("<th><span>", dn[d].substr(0,1), "</span></th>");
	        }
	        m[m.length] = "</tr></thead><tbody><tr>";
	        for(var i = 0; i < 42; i++) {
	            if(i % 7 == 0 && i != 0){
	                m[m.length] = "</tr><tr>";
	            }
	            m[m.length] = '<td><a href="#" hidefocus="on" class="x-date-date" tabIndex="1"><em><span></span></em></a></td>';
	        }
	        m[m.length] = '</tr></tbody></table></td></tr><tr><td colspan="3" class="x-date-bottom" align="center"></td></tr></table><div class="x-date-mp"></div>';

	        var el = document.createElement("div");
	        el.className = "x-date-picker";
	        el.innerHTML = m.join("");

	        container.dom.insertBefore(el, position);

	        this.el = Ext.get(el);
	        this.eventEl = Ext.get(el.firstChild);

	       
	        this.el.child("td.x-date-left a").on('click', this.showPrevMonth, this);
	        this.el.child("td.x-date-right a").on('click', this.showNextMonth, this);
	        
	        /*
	        new Ext.util.ClickRepeater(this.el.child("td.x-date-left a"), {
	            handler: this.showPrevMonth,
	            scope: this,
	            preventDefault:true,
	            stopDefault:true
	        });

	        new Ext.util.ClickRepeater(this.el.child("td.x-date-right a"), {
	            handler: this.showNextMonth,
	            scope: this,
	            preventDefault:true,
	            stopDefault:true
	        });
	       */
	        
	        this.eventEl.on("mousewheel", this.handleMouseWheel,  this);

	        this.monthPicker = this.el.down('div.x-date-mp');
	        this.monthPicker.enableDisplayMode('block');
	        
	        var kn = new Ext.KeyNav(this.eventEl, {
	            "left" : function(e){
	                e.ctrlKey ?
	                    this.showPrevMonth() :
	                    this.update(this.activeDate.add("d", -1));
	            },

	            "right" : function(e){
	                e.ctrlKey ?
	                    this.showNextMonth() :
	                    this.update(this.activeDate.add("d", 1));
	            },

	            "up" : function(e){
	                e.ctrlKey ?
	                    this.showNextYear() :
	                    this.update(this.activeDate.add("d", -7));
	            },

	            "down" : function(e){
	                e.ctrlKey ?
	                    this.showPrevYear() :
	                    this.update(this.activeDate.add("d", 7));
	            },

	            "pageUp" : function(e){
	                this.showNextMonth();
	            },

	            "pageDown" : function(e){
	                this.showPrevMonth();
	            },

	            "enter" : function(e){
	                e.stopPropagation();
	                return true;
	            },

	            scope : this
	        });

	        this.eventEl.on("click", this.handleDateClick,  this, {delegate: "a.x-date-date"});

	        this.eventEl.addKeyListener(Ext.EventObject.SPACE, this.selectToday,  this);

	        this.el.unselectable();
	        
	        this.cells = this.el.select("table.x-date-inner tbody td");
	        this.textNodes = this.el.query("table.x-date-inner tbody span");

	        this.mbtn = new Ext.Button({
	            text: "&#160;",
	            tooltip: this.monthYearText,
	            renderTo: this.el.child("td.x-date-middle", true)
	        });

	        this.mbtn.on('click', this.showMonthPicker, this);
	        this.mbtn.el.child(this.mbtn.menuClassTarget).addClass("x-btn-with-menu");


	        var today = (new Date()).dateFormat(this.format);
	        this.todayBtn = new Ext.Button({
	            renderTo: this.el.child("td.x-date-bottom", true),
	            text: String.format(this.todayText, today),
	            tooltip: String.format(this.todayTip, today),
	            handler: this.selectToday,
	            scope: this
	        });
	        
	        if(Ext.isIE){
	            this.el.repaint();
	        }
	        this.update(this.value);
	    }
	    
	});	
	
	/**
	 * Imported from Ext 3.2.1
     * Returns true if the passed value is a string.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
	Ext.isString = function(v){
        return typeof v === 'string';
    };
    
	/**
	 * Imported from Ext 3.2.1
     * Copies a set of named properties fom the source object to the destination object.
     * 
     * @param {Object} The destination object.
     * @param {Object} The source object.
     * @param {Array/String} Either an Array of property names, or a comma-delimited list
     * of property names to copy.
     * @return {Object} The modified object.
    */

	Ext.copyTo = function(dest, source, names){
        if(Ext.isString(names)){
            names = names.split(/[,;\s]/);
        }
        Ext.each(names, function(name){
            if(source.hasOwnProperty(name)){
                dest[name] = source[name];
            }
        }, this);
        return dest;
    };
    
    /**
     * Imported from Ext 3.2.1
     * Returns true if the passed value is not undefined.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    Ext.isDefined = function(v){
        return typeof v !== 'undefined';
    };

    
	
	/**
    * Imported from Ext 3.2.1
	* Returns true if the passed object is a JavaScript array, otherwise false.
    * @param {Object} object The object to test
    * @return {Boolean}
    */	
	Ext.isArray = function(v){
        return v && typeof v.length == 'number' && typeof v.splice == 'function';
		//return Object.prototype.toString.apply(v) === '[object Array]';
    };
    
    /**
     * Imported from Ext 3.2.1
     * Returns true if the passed value is a JavaScript Object, otherwise false.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    Ext.isObject = function(v){
        return !!v && Object.prototype.toString.call(v) === '[object Object]';
    };
    
    /**
     * Imported from Ext 3.2.1
     * Returns true if the passed value is a JavaScript Function, otherwise false.
     * @param {Mixed} value The value to test
     * @return {Boolean}
     */
    Ext.isFunction = function(v){
        return Object.prototype.toString.call(v) === '[object Function]';
    };


    /**
     * Imported from Ext 3.2.1
     * Converts any iterable (numeric indices and a length property) into a true array
     * Don't use this on strings. IE doesn't support "abc"[0] which this implementation depends on.
     * For strings, use this instead: "abc".match(/./g) => [a,b,c];
     * @param {Iterable} the iterable object to be turned into a true Array.
     * @return (Array) array
     */
     Ext.toArray = function(){
         return Ext.isIE ?
             function(a, i, j, res){
                 res = [];
                 for(var x = 0, len = a.length; x < len; x++) {
                     res.push(a[x]);
                 }
                 return res.slice(i || 0, j || res.length);
             } :
             function(a, i, j){
                 return Array.prototype.slice.call(a, i || 0, j || a.length);
             }
     }();
     
     /**
      * Imported from Ext 3.2.1
      * 
      * Attempts to destroy and then remove a set of named properties of the passed object.
      * @param {Object} o The object (most likely a Component) who's properties you wish to destroy.
      * @param {Mixed} arg1 The name of the property to destroy and remove from the object.
      * @param {Mixed} etc... More property names to destroy and remove.
      */
     Ext.destroyMembers = function(o, arg1, arg2, etc){
         for(var i = 1, a = arguments, len = a.length; i < len; i++) {
             Ext.destroy(o[a[i]]);
             delete o[a[i]];
         }
     };
    
     /**
      * Imported from Ext 3.2.1
      * Rounds the passed number to the required decimal precision.
      * @param {Number/String} value The numeric value to round.
      * @param {Number} precision The number of decimal places to which to round the first parameter's value.
      * @return {Number} The rounded value.
      */
     Ext.util.Format.round = function(value, precision) {
         var result = Number(value);
         if (typeof precision == 'number') {
             precision = Math.pow(10, precision);
             result = Math.round(value * precision) / precision;
         }
         return result;
     },
     
  
     
  
     /**
      * Imported from Ext 3.2.1
      */
       
     Ext.override(Ext.Component, {
    	 
    	 // private
    	    clearMons : function(){
    	        Ext.each(this.mons, function(m){
    	            m.item.un(m.ename, m.fn, m.scope);
    	        }, this);
    	        this.mons = [];
    	    },

    	    // private
    	    createMons: function(){
    	        if(!this.mons){
    	            this.mons = [];
    	            this.on('beforedestroy', this.clearMons, this, {single: true});
    	        }
    	    },

    	    mon : function(item, ename, fn, scope, opt){
    	        this.createMons();
    	        if(Ext.isObject(ename)){
    	            var propRe = /^(?:scope|delay|buffer|single|stopEvent|preventDefault|stopPropagation|normalized|args|delegate)$/;

    	            var o = ename;
    	            for(var e in o){
    	                if(propRe.test(e)){
    	                    continue;
    	                }
    	                if(Ext.isFunction(o[e])){
    	                    // shared options
    	                    this.mons.push({
    	                        item: item, ename: e, fn: o[e], scope: o.scope
    	                    });
    	                    item.on(e, o[e], o.scope, o);
    	                }else{
    	                    // individual options
    	                    this.mons.push({
    	                        item: item, ename: e, fn: o[e], scope: o.scope
    	                    });
    	                    item.on(e, o[e]);
    	                }
    	            }
    	            return;
    	        }

    	        this.mons.push({
    	            item: item, ename: ename, fn: fn, scope: scope
    	        });
    	        item.on(ename, fn, scope, opt);
    	    },
    	    
    	    mun : function(item, ename, fn, scope){
    	        var found, mon;
    	        this.createMons();
    	        for(var i = 0, len = this.mons.length; i < len; ++i){
    	            mon = this.mons[i];
    	            if(item === mon.item && ename == mon.ename && fn === mon.fn && scope === mon.scope){
    	                this.mons.splice(i, 1);
    	                item.un(ename, fn, scope);
    	                found = true;
    	                break;
    	            }
    	        }
    	        return found;
    	    }
    	    
     });

	/**
	 * Override Ext.FormPanel so that in case we create a form without items it still has a item list.
	 * ERROR IS : this.items has no properties
	 */

	Ext.override(Ext.FormPanel, {
		// private
		initFields : function(){
			//BEGIN FIX It can happend that there is a form created without items (json)
			this.initItems();
			//END FIX
			var f = this.form;
			var formPanel = this;
			var fn = function(c){
				if(c.doLayout && c != formPanel){
					Ext.applyIf(c, {
						labelAlign: c.ownerCt.labelAlign,
						labelWidth: c.ownerCt.labelWidth,
						itemCls: c.ownerCt.itemCls
					});
					if(c.items){
						c.items.each(fn);
					}
				}else if(c.isFormField){
					f.add(c);
				}
			};
			this.items.each(fn);
		}
	});
	
	Ext.override(Ext.Component, {hideMode: 'offsets'});
	
	Ext.override(Ext.menu.Menu, {
	  render : function(){
	    if(this.el){
	      return;
	    }
	    var el = this.el = this.createEl();
	    
	    if(!this.keyNav){
	      this.keyNav = new Ext.menu.MenuNav(this);
	    }
	    if(this.plain){
	      el.addClass("x-menu-plain");
	    }
	    if(this.cls){
	      el.addClass(this.cls);
	    }
	    // generic focus element
	    this.focusEl = el.createChild({
	      tag: "a", cls: "x-menu-focus", href: "#", onclick: "return false;", tabIndex:"-1"
	    });
	    var ul = el.createChild({tag: "ul", cls: "x-menu-list"});
	    ul.on("click", this.onClick, this);
	    ul.on("mouseover", this.onMouseOver, this);
	    ul.on("mouseout", this.onMouseOut, this);
	    if (!this.topmenu) {
	      this.addEvents("mouseenter", "mouseexit");
	      this.mouseout = null;
	    }
	    el.on("mouseover", function(e, t){
	      if(this.topmenu){
	        clearTimeout(this.topmenu.mouseout);
	        this.topmenu.mouseout=null;
	      }else if (this.mouseout == null) this.fireEvent("mouseenter", this, e, t);
	      else {
	        clearTimeout(this.mouseout);
	        this.mouseout = null;
	      }
	    }, this);
	    el.on("mouseout", function(e, t){
	      if (this.topmenu) {
	        this.topmenu.mouseout = (function(){
	          this.topmenu.mouseout = null;
	          this.topmenu.fireEvent("mouseexit", this.topmenu, e, t);
	        }).defer(500, this);
	      } else {
	        this.mouseout = (function(){
	          this.mouseout = null;
	          this.fireEvent("mouseexit", this, e, t);
	        }).defer(500, this);
	      }
	    }, this);
	    el.on("mouseup", function(e, t){
	      e.stopEvent();
	    });
	    this.items.each(function(item){
	      var li = document.createElement("li");
	      li.className = "x-menu-list-item";
	      ul.dom.appendChild(li);
	      if(item.menu)item.menu.topmenu=this.topmenu||this;
	      item.render(li, this);
	    }, this);
	    this.ul = ul;
	    this.autoWidth();
	  }
	});

	Ext.override(Ext.form.ComboBox, {
    	
	    onTypeAhead : function(){
	        if(this.store.getCount() > 0){
	            var r = this.store.getAt(0);
	            var newValue = r.data[this.displayField];
	            var len = newValue.length;
	            var selStart = this.getRawValue().length;
	            if(selStart != len){
	                this.setRawValue(newValue);
	                this.selectText(selStart, newValue.length);
	            }
	            if (this.valueField) {
	            	this.setValue(r.data[this.valueField]);
	            }
	        }
	    },
		
        doQuery : function(q, forceAll){
            if(q === undefined || q === null){
                q = '';
            }
            var qe = {
                query: q,
                forceAll: forceAll,
                combo: this,
                cancel:false
            };
            if(this.fireEvent('beforequery', qe)===false || qe.cancel){
                return false;
            }
            q = qe.query;
            forceAll = qe.forceAll;
            if(forceAll === true || (q.length >= this.minChars)){
                if(this.lastQuery !== q){
                    this.lastQuery = q;
                    if(this.mode == 'local'){
                        this.selectedIndex = -1;
                        if(forceAll){
                            this.store.clearFilter();
                        }else{
                            this.store.filter(this.displayField, q);
                        }
                        this.onLoad();
                    }else{
                        this.store.baseParams[this.queryParam] = q;
                        this.store.load({
                            params: this.getParams(q)
                        });
                        this.expand();
                    }
                }else{
                    this.selectedIndex = -1;
                    this.onLoad();
                }
            }
            // if the store is empty, queries on valueField instead of displayField
            if (this.store.getCount() == 0 && this.mode == 'local' && q != '') {
                var qe = {
                    query: q,
                    forceAll: true,
                    combo: this,
                    cancel:false
                };
                if(this.fireEvent('beforequery', qe)===false || qe.cancel){
                    return false;
                }
                q = qe.query;
                this.selectedIndex = -1;
                if(forceAll){
                    this.store.clearFilter();
                }else{
                    this.store.filter(this.valueField, q);
                }
                this.onLoad();
            }
        },
        
        onLoad : function(){
            if(!this.hasFocus){
                return;
            }
            if(this.store.getCount() > 0){
                this.expand();
                this.restrictHeight();
                if(this.lastQuery == this.allQuery){
                    if(this.editable){
                        this.el.dom.select();
                    }
                    if(!this.selectByValue(this.value, true)){
                        this.select(0, true);
                    }
                }else{
                    this.selectNext();
                    if(this.typeAhead && this.lastKey != Ext.EventObject.BACKSPACE && this.lastKey != Ext.EventObject.DELETE){
                        this.taTask.delay(this.typeAheadDelay);
                    }
                }
            }else{
                this.onEmptyResults();
                // if the store is empty, the field value is the rawValue (the displayed value)
                if (this.getRawValue() !== undefined) {
            		this.setValue(this.getRawValue());
            	}
            }
            //this.el.focus();
        }
	});
	
	
	/**
	 * patch for Ext state management bug
	 * http://www.jasonclawson.com/2008/05/20/ext-21-state-managment-issues-dont-use-it/
	 */
	Ext.override(Ext.Component, {
		
	    saveState : function(){
	        if(Ext.state.Manager && this.stateful !== false){
	            var state = this.getState();
	            if(this.fireEvent('beforestatesave', this, state) !== false){
	                Ext.state.Manager.set(this.stateId || this.id, state);
	                this.fireEvent('statesave', this, state);
	            }
	        }
	    },
	    
	    stateful : false
	 
	});
	
	/**
	 * This is a workaround to solve the bug https://www.spagoworld.org/jira/browse/SPAGOBI-1005 
	 */
	Ext.override(Ext.TabPanel, {
		findTargets : function(e){
		        var item = null;
		        var itemEl = e.getTarget('li', this.strip);
		        if(itemEl){
		            item = this.getComponent(itemEl.id.split(this.idDelimiter)[1]);
		            try{
		                if(item.disabled!=null && item.disabled){	//this line override the Ext method..
		                    return {								//..We check if item.disabled is not null
		                        close : null,
		                        item : null,
		                        el : null
		                    };
		                }
		            }catch(e){}



		        }
		        return {
		            close : e.getTarget('.x-tab-strip-close', this.strip),
		            item : item,
		            el : itemEl
		        };
		    }
	});
	
	/* =============================================================================
	* Added by Davide Zerbetto (July 2013)
	* IE9 does not support createContextualFragment function
	* See https://spagobi.eng.it/jira/browse/SPAGOBI-1266#comment-33990
	* See http://www.marcolecce.com/blog/2011/05/19/sencha-extjs-createcontextualfragment-non-supportato-in-ie-9/
	============================================================================= */
	if (Ext.isIE && (typeof Range !== 'undefined') && !Range.prototype.createContextualFragment) {
		  Range.prototype.createContextualFragment = function(html) {
		    var frag = document.createDocumentFragment(),
		    div = document.createElement('div');
		    frag.appendChild(div);
		    div.outerHTML = html;
		    return frag;
		  };
	}
	/* =============================================================================
	* Added by Monica Franceschini (November 2013)
	* to avoid XSS Injection / HTML Injection vulnerabilities
	============================================================================= */

	Ext.override(Ext.grid.EditorGridPanel, {
		listeners: { 'validateedit': function(e){ 
			var v = e.value;
			if(v.indexOf('<') != -1 && v.indexOf('>') != -1){
				e.value = Ext.util.Format.stripTags(v); 
				alert("Characters < and > not allowed at the same time");
			}	
		} 
	}
	});
	
	
	/* =============================================================================
	* Added by Davide Zerbetto (Dicember 2013)
	* When a panel is initially collapsed into a border layout, there are problem when expanding the panel, 
	* in particular if the panel is a form and contains comboboxes, lookup fields ....
	* See http://www.sencha.com/forum/showthread.php?63985-FIXED-2.*-3.0-layout-in-collapsed-Panels
	============================================================================= */
	/*
	Ext.override(Ext.Container, {
	    doLayout : function(shallow){
	        if(!this.isVisible() || this.collapsed){
	            this.deferLayout = this.deferLayout || !shallow;
	            return;
	        }
	        shallow = shallow && !this.deferLayout;
	        delete this.deferLayout;
	        if(this.rendered && this.layout){
	            this.layout.layout();
	        }
	        if(shallow !== false && this.items){
	            var cs = this.items.items;
	            for(var i = 0, len = cs.length; i < len; i++) {
	                var c  = cs[i];
	                if(c.doLayout){
	                    c.doLayout();
	                }
	            }
	        }
	    },
	    onShow : function(){
	        Ext.Container.superclass.onShow.apply(this, arguments);
	        if(this.deferLayout !== undefined){
	            this.doLayout(true);
	        }
	    }
	});
	Ext.override(Ext.Panel, {
	    afterExpand : function(){
	        this.collapsed = false;
	        this.afterEffect();
	        if(this.deferLayout !== undefined){
	            this.doLayout(true);
	        }
	        this.fireEvent('expand', this);
	    }
	});
	
	Ext.override(Ext.Panel, {
	    afterExpand : function(){
	        this.collapsed = false;
	        this.afterEffect();
	        this.fireEvent('expand', this);
	        alert("mmmm....");
	        this.doLayout();
	    }
	});
	*/
	
	Ext.override(Ext.layout.TableLayout, {
	    onLayout : function(ct, target){
	        var cs = ct.items.items, len = cs.length, c, i;
	        if(!this.table){
	            target.addClass('x-table-layout-ct');

	            this.table = target.createChild(
	                {tag:'table', cls:'x-table-layout', cellspacing: 0, cn: {tag: 'tbody'}}, null, true);
	        }
		this.renderAll(ct, target);//move out that can render items more than once.
	    }
	});
	
	
    /**
     * Utility method for getting the width of the browser scrollbar. This can differ depending on
     * operating system settings, such as the theme or font size.
     * @param {Boolean} force (optional) true to force a recalculation of the value.
     * @return {Number} The width of the scrollbar.
     */
    Ext.getScrollBarWidth = function(force){
        if(!Ext.isReady){
            return 0;
        }

        if(force === true || Ext.scrollWidth === null){
                // Append our div, do our calculation and then remove it
            var div = Ext.getBody().createChild('<div class="x-hide-offsets" style="width:100px;height:50px;overflow:hidden;"><div style="height:200px;"></div></div>'),
                child = div.child('div', true);
            var w1 = child.offsetWidth;
            div.setStyle('overflow', (Ext.isWebKit || Ext.isGecko) ? 'auto' : 'scroll');
            var w2 = child.offsetWidth;
            div.remove();
            // Need to add 2 to ensure we leave enough space
            Ext.scrollWidth = w1 - w2 + 2;
        }
        return Ext.scrollWidth;
    };