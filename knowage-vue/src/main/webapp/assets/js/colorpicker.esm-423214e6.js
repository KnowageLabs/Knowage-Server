import{Z as d,L as a,Q as v,P as g,S as k,r as L,o as h,c,G as p,A as f,f as m,w as b,T as y,a as u}from"./index-1e676509.js";var M={name:"ColorPicker",emits:["update:modelValue","change","show","hide"],props:{modelValue:{type:null,default:null},defaultColor:{type:null,default:"ff0000"},inline:{type:Boolean,default:!1},format:{type:String,default:"hex"},disabled:{type:Boolean,default:!1},tabindex:{type:String,default:null},autoZIndex:{type:Boolean,default:!0},baseZIndex:{type:Number,default:0},appendTo:{type:String,default:"body"},panelClass:null},data(){return{overlayVisible:!1}},hsbValue:null,outsideClickListener:null,documentMouseMoveListener:null,documentMouseUpListener:null,scrollHandler:null,resizeListener:null,hueDragging:null,colorDragging:null,selfUpdate:null,picker:null,colorSelector:null,colorHandle:null,hueView:null,hueHandle:null,watch:{modelValue:{immediate:!0,handler(e){this.hsbValue=this.toHSB(e),this.selfUpdate?this.selfUpdate=!1:this.updateUI()}}},beforeUnmount(){this.unbindOutsideClickListener(),this.unbindDragListeners(),this.unbindResizeListener(),this.scrollHandler&&(this.scrollHandler.destroy(),this.scrollHandler=null),this.picker&&this.autoZIndex&&d.clear(this.picker),this.clearRefs()},mounted(){this.updateUI()},methods:{pickColor(e){let t=this.colorSelector.getBoundingClientRect(),n=t.top+(window.pageYOffset||document.documentElement.scrollTop||document.body.scrollTop||0),r=t.left+document.body.scrollLeft,l=Math.floor(100*Math.max(0,Math.min(150,(e.pageX||e.changedTouches[0].pageX)-r))/150),i=Math.floor(100*(150-Math.max(0,Math.min(150,(e.pageY||e.changedTouches[0].pageY)-n)))/150);this.hsbValue=this.validateHSB({h:this.hsbValue.h,s:l,b:i}),this.selfUpdate=!0,this.updateColorHandle(),this.updateInput(),this.updateModel(),this.$emit("change",{event:e,value:this.modelValue})},pickHue(e){let t=this.hueView.getBoundingClientRect().top+(window.pageYOffset||document.documentElement.scrollTop||document.body.scrollTop||0);this.hsbValue=this.validateHSB({h:Math.floor(360*(150-Math.max(0,Math.min(150,(e.pageY||e.changedTouches[0].pageY)-t)))/150),s:100,b:100}),this.selfUpdate=!0,this.updateColorSelector(),this.updateHue(),this.updateModel(),this.updateInput(),this.$emit("change",{event:e,value:this.modelValue})},updateModel(){switch(this.format){case"hex":this.$emit("update:modelValue",this.HSBtoHEX(this.hsbValue));break;case"rgb":this.$emit("update:modelValue",this.HSBtoRGB(this.hsbValue));break;case"hsb":this.$emit("update:modelValue",this.hsbValue);break}},updateColorSelector(){if(this.colorSelector){let e=this.validateHSB({h:this.hsbValue.h,s:100,b:100});this.colorSelector.style.backgroundColor="#"+this.HSBtoHEX(e)}},updateColorHandle(){this.colorHandle&&(this.colorHandle.style.left=Math.floor(150*this.hsbValue.s/100)+"px",this.colorHandle.style.top=Math.floor(150*(100-this.hsbValue.b)/100)+"px")},updateHue(){this.hueHandle&&(this.hueHandle.style.top=Math.floor(150-150*this.hsbValue.h/360)+"px")},updateInput(){this.$refs.input&&(this.$refs.input.style.backgroundColor="#"+this.HSBtoHEX(this.hsbValue))},updateUI(){this.updateHue(),this.updateColorHandle(),this.updateInput(),this.updateColorSelector()},validateHSB(e){return{h:Math.min(360,Math.max(0,e.h)),s:Math.min(100,Math.max(0,e.s)),b:Math.min(100,Math.max(0,e.b))}},validateRGB(e){return{r:Math.min(255,Math.max(0,e.r)),g:Math.min(255,Math.max(0,e.g)),b:Math.min(255,Math.max(0,e.b))}},validateHEX(e){var t=6-e.length;if(t>0){for(var n=[],r=0;r<t;r++)n.push("0");n.push(e),e=n.join("")}return e},HEXtoRGB(e){let t=parseInt(e.indexOf("#")>-1?e.substring(1):e,16);return{r:t>>16,g:(t&65280)>>8,b:t&255}},HEXtoHSB(e){return this.RGBtoHSB(this.HEXtoRGB(e))},RGBtoHSB(e){var t={h:0,s:0,b:0},n=Math.min(e.r,e.g,e.b),r=Math.max(e.r,e.g,e.b),l=r-n;return t.b=r,t.s=r!==0?255*l/r:0,t.s!==0?e.r===r?t.h=(e.g-e.b)/l:e.g===r?t.h=2+(e.b-e.r)/l:t.h=4+(e.r-e.g)/l:t.h=-1,t.h*=60,t.h<0&&(t.h+=360),t.s*=100/255,t.b*=100/255,t},HSBtoRGB(e){var t={r:null,g:null,b:null},n=Math.round(e.h),r=Math.round(e.s*255/100),l=Math.round(e.b*255/100);if(r===0)t={r:l,g:l,b:l};else{var i=l,o=(255-r)*l/255,s=(i-o)*(n%60)/60;n===360&&(n=0),n<60?(t.r=i,t.b=o,t.g=o+s):n<120?(t.g=i,t.b=o,t.r=i-s):n<180?(t.g=i,t.r=o,t.b=o+s):n<240?(t.b=i,t.r=o,t.g=i-s):n<300?(t.b=i,t.g=o,t.r=o+s):n<360?(t.r=i,t.g=o,t.b=i-s):(t.r=0,t.g=0,t.b=0)}return{r:Math.round(t.r),g:Math.round(t.g),b:Math.round(t.b)}},RGBtoHEX(e){var t=[e.r.toString(16),e.g.toString(16),e.b.toString(16)];for(var n in t)t[n].length===1&&(t[n]="0"+t[n]);return t.join("")},HSBtoHEX(e){return this.RGBtoHEX(this.HSBtoRGB(e))},toHSB(e){let t;if(e)switch(this.format){case"hex":t=this.HEXtoHSB(e);break;case"rgb":t=this.RGBtoHSB(e);break;case"hsb":t=e;break}else t=this.HEXtoHSB(this.defaultColor);return t},onOverlayEnter(e){this.updateUI(),this.alignOverlay(),this.bindOutsideClickListener(),this.bindScrollListener(),this.bindResizeListener(),this.autoZIndex&&d.set("overlay",e,this.$primevue.config.zIndex.overlay),this.$emit("show")},onOverlayLeave(){this.unbindOutsideClickListener(),this.unbindScrollListener(),this.unbindResizeListener(),this.clearRefs(),this.$emit("hide")},onOverlayAfterLeave(e){this.autoZIndex&&d.clear(e)},alignOverlay(){this.appendTo==="self"?a.relativePosition(this.picker,this.$refs.input):a.absolutePosition(this.picker,this.$refs.input)},onInputClick(){this.disabled||(this.overlayVisible=!this.overlayVisible)},onInputKeydown(e){switch(e.which){case 32:this.overlayVisible=!this.overlayVisible,e.preventDefault();break;case 27:case 9:this.overlayVisible=!1;break}},onColorMousedown(e){this.disabled||(this.bindDragListeners(),this.onColorDragStart(e))},onColorDragStart(e){this.disabled||(this.colorDragging=!0,this.pickColor(e),a.addClass(this.$el,"p-colorpicker-dragging"),e.preventDefault())},onDrag(e){this.colorDragging&&(this.pickColor(e),e.preventDefault()),this.hueDragging&&(this.pickHue(e),e.preventDefault())},onDragEnd(){this.colorDragging=!1,this.hueDragging=!1,a.removeClass(this.$el,"p-colorpicker-dragging"),this.unbindDragListeners()},onHueMousedown(e){this.disabled||(this.bindDragListeners(),this.onHueDragStart(e))},onHueDragStart(e){this.disabled||(this.hueDragging=!0,this.pickHue(e),a.addClass(this.$el,"p-colorpicker-dragging"))},isInputClicked(e){return this.$refs.input&&this.$refs.input.isSameNode(e.target)},bindDragListeners(){this.bindDocumentMouseMoveListener(),this.bindDocumentMouseUpListener()},unbindDragListeners(){this.unbindDocumentMouseMoveListener(),this.unbindDocumentMouseUpListener()},bindOutsideClickListener(){this.outsideClickListener||(this.outsideClickListener=e=>{this.overlayVisible&&this.picker&&!this.picker.contains(e.target)&&!this.isInputClicked(e)&&(this.overlayVisible=!1)},document.addEventListener("click",this.outsideClickListener))},unbindOutsideClickListener(){this.outsideClickListener&&(document.removeEventListener("click",this.outsideClickListener),this.outsideClickListener=null)},bindScrollListener(){this.scrollHandler||(this.scrollHandler=new v(this.$refs.container,()=>{this.overlayVisible&&(this.overlayVisible=!1)})),this.scrollHandler.bindScrollListener()},unbindScrollListener(){this.scrollHandler&&this.scrollHandler.unbindScrollListener()},bindResizeListener(){this.resizeListener||(this.resizeListener=()=>{this.overlayVisible&&!a.isTouchDevice()&&(this.overlayVisible=!1)},window.addEventListener("resize",this.resizeListener))},unbindResizeListener(){this.resizeListener&&(window.removeEventListener("resize",this.resizeListener),this.resizeListener=null)},bindDocumentMouseMoveListener(){this.documentMouseMoveListener||(this.documentMouseMoveListener=this.onDrag.bind(this),document.addEventListener("mousemove",this.documentMouseMoveListener))},unbindDocumentMouseMoveListener(){this.documentMouseMoveListener&&(document.removeEventListener("mousemove",this.documentMouseMoveListener),this.documentMouseMoveListener=null)},bindDocumentMouseUpListener(){this.documentMouseUpListener||(this.documentMouseUpListener=this.onDragEnd.bind(this),document.addEventListener("mouseup",this.documentMouseUpListener))},unbindDocumentMouseUpListener(){this.documentMouseUpListener&&(document.removeEventListener("mouseup",this.documentMouseUpListener),this.documentMouseUpListener=null)},pickerRef(e){this.picker=e},colorSelectorRef(e){this.colorSelector=e},colorHandleRef(e){this.colorHandle=e},hueViewRef(e){this.hueView=e},hueHandleRef(e){this.hueHandle=e},clearRefs(){this.picker=null,this.colorSelector=null,this.colorHandle=null,this.hueView=null,this.hueHandle=null},onOverlayClick(e){g.emit("overlay-click",{originalEvent:e,target:this.$el})}},computed:{containerClass(){return["p-colorpicker p-component",{"p-colorpicker-overlay":!this.inline}]},inputClass(){return["p-colorpicker-preview p-inputtext",{"p-disabled":this.disabled}]},pickerClass(){return["p-colorpicker-panel",this.panelClass,{"p-colorpicker-overlay-panel":!this.inline,"p-disabled":this.disabled,"p-input-filled":this.$primevue.config.inputStyle==="filled","p-ripple-disabled":this.$primevue.config.ripple===!1}]}},components:{Portal:k}};const H=["tabindex","disabled"],C={class:"p-colorpicker-content"},x={class:"p-colorpicker-color"};function S(e,t,n,r,l,i){const o=L("Portal");return h(),c("div",{ref:"container",class:p(i.containerClass)},[n.inline?f("",!0):(h(),c("input",{key:0,ref:"input",type:"text",class:p(i.inputClass),readonly:"readonly",tabindex:n.tabindex,disabled:n.disabled,onClick:t[0]||(t[0]=(...s)=>i.onInputClick&&i.onInputClick(...s)),onKeydown:t[1]||(t[1]=(...s)=>i.onInputKeydown&&i.onInputKeydown(...s))},null,42,H)),m(o,{appendTo:n.appendTo,disabled:n.inline},{default:b(()=>[m(y,{name:"p-connected-overlay",onEnter:i.onOverlayEnter,onLeave:i.onOverlayLeave,onAfterLeave:i.onOverlayAfterLeave},{default:b(()=>[n.inline||l.overlayVisible?(h(),c("div",{key:0,ref:i.pickerRef,class:p(i.pickerClass),onClick:t[10]||(t[10]=(...s)=>i.onOverlayClick&&i.onOverlayClick(...s))},[u("div",C,[u("div",{ref:i.colorSelectorRef,class:"p-colorpicker-color-selector",onMousedown:t[2]||(t[2]=s=>i.onColorMousedown(s)),onTouchstart:t[3]||(t[3]=s=>i.onColorDragStart(s)),onTouchmove:t[4]||(t[4]=s=>i.onDrag(s)),onTouchend:t[5]||(t[5]=s=>i.onDragEnd())},[u("div",x,[u("div",{ref:i.colorHandleRef,class:"p-colorpicker-color-handle"},null,512)])],544),u("div",{ref:i.hueViewRef,class:"p-colorpicker-hue",onMousedown:t[6]||(t[6]=s=>i.onHueMousedown(s)),onTouchstart:t[7]||(t[7]=s=>i.onHueDragStart(s)),onTouchmove:t[8]||(t[8]=s=>i.onDrag(s)),onTouchend:t[9]||(t[9]=s=>i.onDragEnd())},[u("div",{ref:i.hueHandleRef,class:"p-colorpicker-hue-handle"},null,512)],544)])],2)):f("",!0)]),_:1},8,["onEnter","onLeave","onAfterLeave"])]),_:1},8,["appendTo","disabled"])],2)}function V(e,t){t===void 0&&(t={});var n=t.insertAt;if(!(!e||typeof document=="undefined")){var r=document.head||document.getElementsByTagName("head")[0],l=document.createElement("style");l.type="text/css",n==="top"&&r.firstChild?r.insertBefore(l,r.firstChild):r.appendChild(l),l.styleSheet?l.styleSheet.cssText=e:l.appendChild(document.createTextNode(e))}}var w=`
.p-colorpicker {
    display: inline-block;
}
.p-colorpicker-dragging {
    cursor: pointer;
}
.p-colorpicker-overlay {
    position: relative;
}
.p-colorpicker-panel {
    position: relative;
    width: 193px;
    height: 166px;
}
.p-colorpicker-overlay-panel {
    position: absolute;
    top: 0;
    left: 0;
}
.p-colorpicker-preview {
    cursor: pointer;
}
.p-colorpicker-panel .p-colorpicker-content {
    position: relative;
}
.p-colorpicker-panel .p-colorpicker-color-selector {
    width: 150px;
    height: 150px;
    top: 8px;
    left: 8px;
    position: absolute;
}
.p-colorpicker-panel .p-colorpicker-color {
    width: 150px;
    height: 150px;
}
.p-colorpicker-panel .p-colorpicker-color-handle {
    position: absolute;
    top: 0px;
    left: 150px;
    border-radius: 100%;
    width: 10px;
    height: 10px;
    border-width: 1px;
    border-style: solid;
    margin: -5px 0 0 -5px;
    cursor: pointer;
    opacity: 0.85;
}
.p-colorpicker-panel .p-colorpicker-hue {
    width: 17px;
    height: 150px;
    top: 8px;
    left: 167px;
    position: absolute;
    opacity: 0.85;
}
.p-colorpicker-panel .p-colorpicker-hue-handle {
    position: absolute;
    top: 150px;
    left: 0px;
    width: 21px;
    margin-left: -2px;
    margin-top: -5px;
    height: 10px;
    border-width: 2px;
    border-style: solid;
    opacity: 0.85;
    cursor: pointer;
}
`;V(w);M.render=S;export{M as s};
//# sourceMappingURL=colorpicker.esm-423214e6.js.map
