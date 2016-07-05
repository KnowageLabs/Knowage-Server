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

 // no overrides yet

//alert("UPAO 3s");

Ext.feature.tests = [
                	        {
                	            
                	            name: 'CSSPointerEvents',
                	            fn: function(doc) {
                	                return 'pointerEvents' in doc.documentElement.style;
                	            }
                	        },
                	        {
                	            
                	            name: 'CSS3BoxShadow',
                	            fn: function(doc) {
                	                return 'boxShadow' in doc.documentElement.style || 'WebkitBoxShadow' in doc.documentElement.style || 'MozBoxShadow' in doc.documentElement.style;
                	            }
                	        },
                	        {
                	            
                	            name: 'ClassList',
                	            fn: function(doc) {
                	                return !!doc.documentElement.classList;
                	            }
                	        },
                	        {
                	            
                	            name: 'Canvas',
                	            fn: function() {
                	                var element = this.getTestElement('canvas');
                	                return !!(element && element.getContext && element.getContext('2d'));
                	            }
                	        },
                	        {
                	            
                	            name: 'Svg',
                	            fn: function(doc) {
                	                return !!(doc.createElementNS && !!doc.createElementNS("http:/" + "/www.w3.org/2000/svg", "svg").createSVGRect);
                	            }
                	        },
                	        {
                	            
                	            name: 'Vml',
                	            fn: function() {
                	                var element = this.getTestElement(),
                	                    ret = false;
                	                element.innerHTML = "<!--[if vml]><br><![endif]-->";
                	                ret = (element.childNodes.length === 1);
                	                element.innerHTML = "";
                	                return ret;
                	            }
                	        },
                	        {
                	            
                	            name: 'touchScroll',
                	            fn: function() {
                	                var supports = Ext.supports,
                	                    touchScroll = 0;
                	                if (navigator.msMaxTouchPoints || (Ext.isWebKit && supports.TouchEvents && Ext.os.is.Desktop)) {
                	                    touchScroll = 1;
                	                } else if (supports.Touch) {
                	                    touchScroll = 2;
                	                }
                	                return touchScroll;
                	            }
                	        },
                	        {
                	            
                	            name: 'Touch',
                	            fn: function() {
                	                
                	                var maxTouchPoints = navigator.msMaxTouchPoints || navigator.maxTouchPoints;
                	                
                	                
                	                
                	                
                	                
                	                
                	                
                	                return (Ext.supports.TouchEvents && maxTouchPoints !== 1) || maxTouchPoints > 1;
                	            }
                	        },
                	        {
                	            
                	            name: 'TouchEvents',
                	            fn: function() {
                	                return this.isEventSupported('touchend');
                	            }
                	        },
                	        {
                	            name: 'PointerEvents',
                	            fn: function() {
                	                return navigator.pointerEnabled;
                	            }
                	        },
                	        {
                	            name: 'MSPointerEvents',
                	            fn: function() {
                	                return navigator.msPointerEnabled;
                	            }
                	        },
                	        {
                	            
                	            name: 'Orientation',
                	            fn: function() {
                	                return ('orientation' in window) && this.isEventSupported('orientationchange');
                	            }
                	        },
                	        {
                	            
                	            name: 'OrientationChange',
                	            fn: function() {
                	                return this.isEventSupported('orientationchange');
                	            }
                	        },
                	        {
                	            
                	            name: 'DeviceMotion',
                	            fn: function() {
                	                return this.isEventSupported('devicemotion');
                	            }
                	        },
                	        {
                	            
                	            
                	            names: [
                	                'Geolocation',
                	                'GeoLocation'
                	            ],
                	            fn: function() {
                	                return 'geolocation' in window.navigator;
                	            }
                	        },
                	        {
                	            name: 'SqlDatabase',
                	            fn: function() {
                	                return 'openDatabase' in window;
                	            }
                	        },
                	        {
                	            name: 'WebSockets',
                	            fn: function() {
                	                return 'WebSocket' in window;
                	            }
                	        },
                	        {
                	            
                	            name: 'Range',
                	            fn: function() {
                	                return !!document.createRange;
                	            }
                	        },
                	        {
                	            
                	            name: 'CreateContextualFragment',
                	            fn: function() {
                	                var range = !!document.createRange ? document.createRange() : false;
                	                return range && !!range.createContextualFragment;
                	            }
                	        },
                	        {
                	            
                	            name: 'History',
                	            fn: function() {
                	                return ('history' in window && 'pushState' in window.history);
                	            }
                	        },
                	        {
                	            name: 'CssTransforms',
                	            fn: function() {
                	                return this.isStyleSupported('transform');
                	            }
                	        },
                	        {
                	            name: 'CssTransformNoPrefix',
                	            fn: function() {
                	                return this.isStyleSupportedWithoutPrefix('transform');
                	            }
                	        },
                	        {
                	            
                	            name: 'Css3dTransforms',
                	            fn: function() {
                	                
                	                return this.has('CssTransforms') && this.isStyleSupported('perspective') && !Ext.browser.is.AndroidStock2;
                	            }
                	        },
                	        
                	        
                	        {
                	            name: 'CssAnimations',
                	            fn: function() {
                	                return this.isStyleSupported('animationName');
                	            }
                	        },
                	        {
                	            
                	            names: [
                	                'CssTransitions',
                	                'Transitions'
                	            ],
                	            fn: function() {
                	                return this.isStyleSupported('transitionProperty');
                	            }
                	        },
                	        {
                	            
                	            
                	            names: [
                	                'Audio',
                	                'AudioTag'
                	            ],
                	            fn: function() {
                	                return !!this.getTestElement('audio').canPlayType;
                	            }
                	        },
                	        {
                	            
                	            name: 'Video',
                	            fn: function() {
                	                return !!this.getTestElement('video').canPlayType;
                	            }
                	        },
                	        {
                	            
                	            name: 'LocalStorage',
                	            fn: function() {
                	                try {
                	                    
                	                    
                	                    if ('localStorage' in window && window['localStorage'] !== null) {
                	                        
                	                        localStorage.setItem('sencha-localstorage-test', 'test success');
                	                        
                	                        localStorage.removeItem('sencha-localstorage-test');
                	                        return true;
                	                    }
                	                } catch (e) {}
                	                
                	                return false;
                	            }
                	        },
                	        {
                	            
                	            name: 'XHR2',
                	            fn: function() {
                	                return window.ProgressEvent && window.FormData && window.XMLHttpRequest && ('withCredentials' in new XMLHttpRequest());
                	            }
                	        },
                	        {
                	            
                	            name: 'XHRUploadProgress',
                	            fn: function() {
                	                if (window.XMLHttpRequest && !Ext.browser.is.AndroidStock) {
                	                    var xhr = new XMLHttpRequest();
                	                    return xhr && ('upload' in xhr) && ('onprogress' in xhr.upload);
                	                }
                	                return false;
                	            }
                	        },
                	        {
                	            
                	            name: 'NumericInputPlaceHolder',
                	            fn: function() {
                	                return !(Ext.browser.is.AndroidStock4 && Ext.os.version.getMinor() < 2);
                	            }
                	        },
                	        {
                	            name: 'ProperHBoxStretching',
                	            ready: true,
                	            fn: function() {
                	                
                	                var bodyElement = document.createElement('div'),
                	                    innerElement = bodyElement.appendChild(document.createElement('div')),
                	                    contentElement = innerElement.appendChild(document.createElement('div')),
                	                    innerWidth;
                	                bodyElement.setAttribute('style', 'width: 100px; height: 100px; position: relative;');
                	                innerElement.setAttribute('style', 'position: absolute; display: -ms-flexbox; display: -webkit-flex; display: -moz-flexbox; display: flex; -ms-flex-direction: row; -webkit-flex-direction: row; -moz-flex-direction: row; flex-direction: row; min-width: 100%;');
                	                contentElement.setAttribute('style', 'width: 200px; height: 50px;');
                	                document.body.appendChild(bodyElement);
                	                innerWidth = innerElement.offsetWidth;
                	                document.body.removeChild(bodyElement);
                	                return (innerWidth > 100);
                	            }
                	        },
                	        
                	        {
                	            name: 'matchesSelector',
                	            fn: function() {
                	                var el = document.documentElement,
                	                    w3 = 'matches',
                	                    wk = 'webkitMatchesSelector',
                	                    ms = 'msMatchesSelector',
                	                    mz = 'mozMatchesSelector';
                	                return el[w3] ? w3 : el[wk] ? wk : el[ms] ? ms : el[mz] ? mz : null;
                	            }
                	        },
                	        
                	        {
                	            name: 'RightMargin',
                	            ready: true,
                	            fn: function(doc, div) {
                	                var view = doc.defaultView;
                	                //alert("prvi duu");
                	                return !(view && view.getComputedStyle(div.firstChild.firstChild, null)!=null && view.getComputedStyle(div.firstChild.firstChild, null).marginRight != '0px');
                	            }
                	        },
                	        
                	        {
                	            name: 'DisplayChangeInputSelectionBug',
                	            fn: function() {
                	                var webKitVersion = Ext.webKitVersion;
                	                
                	                return 0 < webKitVersion && webKitVersion < 533;
                	            }
                	        },
                	        
                	        {
                	            name: 'DisplayChangeTextAreaSelectionBug',
                	            fn: function() {
                	                var webKitVersion = Ext.webKitVersion;
                	                
                	                return 0 < webKitVersion && webKitVersion < 534.24;
                	            }
                	        },
                	        
                	        {
                	            name: 'TransparentColor',
                	            ready: true,
                	            fn: function(doc, div, view) {
                	                view = doc.defaultView;
                	                ////alert()("drugi duu");
                	                return !(view && view.getComputedStyle(div.lastChild, null)!=null && view.getComputedStyle(div.lastChild, null).backgroundColor != 'transparent');
                	            }
                	        },
                	        
                	        {
                	            name: 'ComputedStyle',
                	            ready: true,
                	            fn: function(doc, div, view) {
                	                view = doc.defaultView;
                	                return view && view.getComputedStyle;
                	            }
                	        },
                	        
                	        {
                	            name: 'Float',
                	            fn: function(doc) {
                	                return 'cssFloat' in doc.documentElement.style;
                	            }
                	        },
                	        
                	        {
                	            name: 'CSS3BorderRadius',
                	            ready: true,
                	            fn: function(doc) {
                	                var domPrefixes = [
                	                        'borderRadius',
                	                        'BorderRadius',
                	                        'MozBorderRadius',
                	                        'WebkitBorderRadius',
                	                        'OBorderRadius',
                	                        'KhtmlBorderRadius'
                	                    ],
                	                    pass = false,
                	                    i;
                	                for (i = 0; i < domPrefixes.length; i++) {
                	                    if (doc.documentElement.style[domPrefixes[i]] !== undefined) {
                	                        pass = true;
                	                    }
                	                }
                	                return pass && !Ext.isIE9;
                	            }
                	        },
                	        
                	        {
                	            name: 'CSS3LinearGradient',
                	            fn: function(doc, div) {
                	                var property = 'background-image:',
                	                    webkit = '-webkit-gradient(linear, left top, right bottom, from(black), to(white))',
                	                    w3c = 'linear-gradient(left top, black, white)',
                	                    moz = '-moz-' + w3c,
                	                    ms = '-ms-' + w3c,
                	                    opera = '-o-' + w3c,
                	                    options = [
                	                        property + webkit,
                	                        property + w3c,
                	                        property + moz,
                	                        property + ms,
                	                        property + opera
                	                    ];
                	                div.style.cssText = options.join(';');
                	                return (("" + div.style.backgroundImage).indexOf('gradient') !== -1) && !Ext.isIE9;
                	            }
                	        },
                	        
                	        {
                	            name: 'MouseEnterLeave',
                	            fn: function(doc) {
                	                return ('onmouseenter' in doc.documentElement && 'onmouseleave' in doc.documentElement);
                	            }
                	        },
                	        
                	        {
                	            name: 'MouseWheel',
                	            fn: function(doc) {
                	                return ('onmousewheel' in doc.documentElement);
                	            }
                	        },
                	        
                	        {
                	            name: 'Opacity',
                	            fn: function(doc, div) {
                	                
                	                if (Ext.isIE8) {
                	                    return false;
                	                }
                	                div.firstChild.style.cssText = 'opacity:0.73';
                	                return div.firstChild.style.opacity == '0.73';
                	            }
                	        },
                	        
                	        {
                	            name: 'Placeholder',
                	            fn: function(doc) {
                	                return 'placeholder' in doc.createElement('input');
                	            }
                	        },
                	        
                	        {
                	            name: 'Direct2DBug',
                	            fn: function(doc) {
                	                return Ext.isString(doc.documentElement.style.msTransformOrigin) && Ext.isIE9m;
                	            }
                	        },
                	        
                	        {
                	            name: 'BoundingClientRect',
                	            fn: function(doc) {
                	                return 'getBoundingClientRect' in doc.documentElement;
                	            }
                	        },
                	        
                	        {
                	            name: 'RotatedBoundingClientRect',
                	            ready: true,
                	            fn: function(doc) {
                	                var body = doc.body,
                	                    supports = false,
                	                    el = this.getTestElement(),
                	                    style = el.style;
                	                if (el.getBoundingClientRect) {
                	                    style.WebkitTransform = style.MozTransform = style.msTransform = style.OTransform = style.transform = 'rotate(90deg)';
                	                    style.width = '100px';
                	                    style.height = '30px';
                	                    body.appendChild(el);
                	                    supports = el.getBoundingClientRect().height !== 100;
                	                    body.removeChild(el);
                	                }
                	                return supports;
                	            }
                	        },
                	        
                	        {
                	            name: 'ChildContentClearedWhenSettingInnerHTML',
                	            ready: true,
                	            fn: function() {
                	                var el = this.getTestElement(),
                	                    child;
                	                el.innerHTML = '<div>a</div>';
                	                child = el.firstChild;
                	                el.innerHTML = '<div>b</div>';
                	                return child.innerHTML !== 'a';
                	            }
                	        },
                	        {
                	            name: 'IncludePaddingInWidthCalculation',
                	            ready: true,
                	            fn: function(doc, div) { ////alert()("mnmn");
                	                return div.childNodes[1] ? div.childNodes[1].firstChild.offsetWidth == 210 : false;
                	            }
                	        },
                	        {
                	            name: 'IncludePaddingInHeightCalculation',
                	            ready: true,
                	            fn: function(doc, div) { ////alert()("mnmn2");
                	                return div.childNodes[1] ? div.childNodes[1].firstChild.offsetHeight == 210 : false;
                	            }
                	        },
                	        
                	        {
                	            name: 'TextAreaMaxLength',
                	            fn: function(doc) {
                	                return ('maxlength' in doc.createElement('textarea'));
                	            }
                	        },
                	        
                	        
                	        {
                	            name: 'GetPositionPercentage',
                	            ready: true,
                	            fn: function(doc, div) {
                	                return Ext.feature.getStyle(div.childNodes[2], 'left') == '10%';
                	            }
                	        },
                	        
                	        {
                	            name: 'PercentageHeightOverflowBug',
                	            ready: true,
                	            fn: function(doc) {
                	                var hasBug = false,
                	                    style, el;
                	                if (Ext.getScrollbarSize().height) {
                	                    
                	                    el = this.getTestElement();
                	                    style = el.style;
                	                    style.height = '50px';
                	                    style.width = '50px';
                	                    style.overflow = 'auto';
                	                    style.position = 'absolute';
                	                    el.innerHTML = [
                	                        '<div style="display:table;height:100%;">',
                	                        
                	                        
                	                        
                	                        '<div style="width:51px;"></div>',
                	                        '</div>'
                	                    ].join('');
                	                    doc.body.appendChild(el);
                	                    if (el.firstChild.offsetHeight === 50) {
                	                        hasBug = true;
                	                    }
                	                    doc.body.removeChild(el);
                	                }
                	                return hasBug;
                	            }
                	        },
                	        
                	        {
                	            name: 'xOriginBug',
                	            ready: true,
                	            fn: function(doc, div) {
                	                div.innerHTML = '<div id="b1" style="height:100px;width:100px;direction:rtl;position:relative;overflow:scroll">' + '<div id="b2" style="position:relative;width:100%;height:20px;"></div>' + '<div id="b3" style="position:absolute;width:20px;height:20px;top:0px;right:0px"></div>' + '</div>';
                	                var outerBox = document.getElementById('b1').getBoundingClientRect(),
                	                    b2 = document.getElementById('b2').getBoundingClientRect(),
                	                    b3 = document.getElementById('b3').getBoundingClientRect();
                	                return (b2.left !== outerBox.left && b3.right !== outerBox.right);
                	            }
                	        },
                	        
                	        {
                	            name: 'ScrollWidthInlinePaddingBug',
                	            ready: true,
                	            fn: function(doc) {
                	                var hasBug = false,
                	                    style, el;
                	                el = doc.createElement('div');
                	                style = el.style;
                	                style.height = '50px';
                	                style.width = '50px';
                	                style.padding = '10px';
                	                style.overflow = 'hidden';
                	                style.position = 'absolute';
                	                el.innerHTML = '<span style="display:inline-block;zoom:1;height:60px;width:60px;"></span>';
                	                doc.body.appendChild(el);
                	                if (el.scrollWidth === 70) {
                	                    hasBug = true;
                	                }
                	                doc.body.removeChild(el);
                	                return hasBug;
                	            }
                	        },
                	        
                	        {
                	            name: 'rtlVertScrollbarOnRight',
                	            ready: true,
                	            fn: function(doc, div) {
                	                div.innerHTML = '<div style="height:100px;width:100px;direction:rtl;overflow:scroll">' + '<div style="width:20px;height:200px;"></div>' + '</div>';
                	                var outerBox = div.firstChild,
                	                    innerBox = outerBox.firstChild;
                	                return (innerBox.offsetLeft + innerBox.offsetWidth !== outerBox.offsetLeft + outerBox.offsetWidth);
                	            }
                	        },
                	        
                	        {
                	            name: 'rtlVertScrollbarOverflowBug',
                	            ready: true,
                	            fn: function(doc, div) {
                	                div.innerHTML = '<div style="height:100px;width:100px;direction:rtl;overflow:auto">' + '<div style="width:95px;height:200px;"></div>' + '</div>';
                	                
                	                
                	                
                	                var outerBox = div.firstChild;
                	                return outerBox.clientHeight === outerBox.offsetHeight;
                	            }
                	        },
                	        {
                	            identity: 'defineProperty',
                	            fn: function() {
                	                if (Ext.isIE8m) {
                	                    Ext.Object.defineProperty = Ext.emptyFn;
                	                    return false;
                	                }
                	                return true;
                	            }
                	        },
                	        {
                	            identify: 'nativeXhr',
                	            fn: function() {
                	                if (typeof XMLHttpRequest !== 'undefined') {
                	                    return true;
                	                }
                	                
                	                XMLHttpRequest = function() {
                	                    try {
                	                        return new ActiveXObject('MSXML2.XMLHTTP.3.0');
                	                    } catch (ex) {
                	                        return null;
                	                    }
                	                };
                	                return false;
                	            }
                	        },
                	        
                	        {
                	            name: 'SpecialKeyDownRepeat',
                	            fn: function() {
                	                return Ext.isWebKit ? parseInt(navigator.userAgent.match(/AppleWebKit\/(\d+)/)[1], 10) >= 525 : !((Ext.isGecko && !Ext.isWindows) || (Ext.isOpera && Ext.operaVersion < 12));
                	            }
                	        },
                	        
                	        {
                	            name: 'EmulatedMouseOver',
                	            fn: function() {
                	                
                	                return Ext.os.is.iOS;
                	            }
                	        },
                	        
                	        {
                	            
                	            name: 'Hashchange',
                	            fn: function() {
                	                
                	                var docMode = document.documentMode;
                	                return 'onhashchange' in window && (docMode === undefined || docMode > 7);
                	            }
                	        },
                	        
                	        {
                	            name: 'FixedTableWidthBug',
                	            ready: true,
                	            fn: function() {
                	                if (Ext.isIE8) {
                	                    
                	                    return false;
                	                }
                	                var outer = document.createElement('div'),
                	                    inner = document.createElement('div'),
                	                    width;
                	                outer.setAttribute('style', 'display:table;table-layout:fixed;');
                	                inner.setAttribute('style', 'display:table-cell;min-width:50px;');
                	                outer.appendChild(inner);
                	                document.body.appendChild(outer);
                	                
                	                outer.offsetWidth;
                	                outer.style.width = '25px';
                	                width = outer.offsetWidth;
                	                document.body.removeChild(outer);
                	                return width === 50;
                	            }
                	        },
                	        
                	        {
                	            name: 'FocusinFocusoutEvents',
                	            
                	            
                	            
                	            
                	            fn: function() {
                	                return !Ext.isGecko;
                	            }
                	        }
                	    ];

//Ext.override
//(
//	Ext.feature, 
//	
//	{
//		
//	    
//	    
//	}			
//);

//alert("ttt");
//Ext.feature.detect();