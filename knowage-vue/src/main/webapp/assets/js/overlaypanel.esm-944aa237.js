import{Z as c,P as o,L as a,Q as y,U as m,R as k,S as L,r as C,y as g,o as d,h as w,w as p,f as E,c as h,a as u,J as x,z,A as f,K as S,T}from"./index-1e676509.js";var O={name:"OverlayPanel",inheritAttrs:!1,emits:["show","hide"],props:{dismissable:{type:Boolean,default:!0},showCloseIcon:{type:Boolean,default:!1},appendTo:{type:String,default:"body"},baseZIndex:{type:Number,default:0},autoZIndex:{type:Boolean,default:!0},ariaCloseLabel:{type:String,default:"close"},breakpoints:{type:Object,default:null}},data(){return{visible:!1}},watch:{dismissable:{immediate:!0,handler(e){e?this.bindOutsideClickListener():this.unbindOutsideClickListener()}}},selfClick:!1,target:null,eventTarget:null,outsideClickListener:null,scrollHandler:null,resizeListener:null,container:null,styleElement:null,overlayEventListener:null,beforeUnmount(){this.dismissable&&this.unbindOutsideClickListener(),this.scrollHandler&&(this.scrollHandler.destroy(),this.scrollHandler=null),this.destroyStyle(),this.unbindResizeListener(),this.target=null,this.container&&this.autoZIndex&&c.clear(this.container),this.overlayEventListener&&(o.off("overlay-click",this.overlayEventListener),this.overlayEventListener=null),this.container=null},mounted(){this.breakpoints&&this.createStyle()},methods:{toggle(e,t){this.visible?this.hide():this.show(e,t)},show(e,t){this.visible=!0,this.eventTarget=e.currentTarget,this.target=t||e.currentTarget},hide(){this.visible=!1},onContentClick(){this.selfClick=!0},onEnter(e){this.container.setAttribute(this.attributeSelector,""),this.alignOverlay(),this.dismissable&&this.bindOutsideClickListener(),this.bindScrollListener(),this.bindResizeListener(),this.autoZIndex&&c.set("overlay",e,this.baseZIndex+this.$primevue.config.zIndex.overlay),this.overlayEventListener=t=>{this.container.contains(t.target)&&(this.selfClick=!0)},o.on("overlay-click",this.overlayEventListener),this.$emit("show")},onLeave(){this.unbindOutsideClickListener(),this.unbindScrollListener(),this.unbindResizeListener(),o.off("overlay-click",this.overlayEventListener),this.overlayEventListener=null,this.$emit("hide")},onAfterLeave(e){this.autoZIndex&&c.clear(e)},alignOverlay(){a.absolutePosition(this.container,this.target);const e=a.getOffset(this.container),t=a.getOffset(this.target);let s=0;e.left<t.left&&(s=t.left-e.left),this.container.style.setProperty("--overlayArrowLeft",`${s}px`),e.top<t.top&&a.addClass(this.container,"p-overlaypanel-flipped")},bindOutsideClickListener(){!this.outsideClickListener&&a.isClient()&&(this.outsideClickListener=e=>{this.visible&&!this.selfClick&&!this.isTargetClicked(e)&&(this.visible=!1),this.selfClick=!1},document.addEventListener("click",this.outsideClickListener))},unbindOutsideClickListener(){this.outsideClickListener&&(document.removeEventListener("click",this.outsideClickListener),this.outsideClickListener=null,this.selfClick=!1)},bindScrollListener(){this.scrollHandler||(this.scrollHandler=new y(this.target,()=>{this.visible&&(this.visible=!1)})),this.scrollHandler.bindScrollListener()},unbindScrollListener(){this.scrollHandler&&this.scrollHandler.unbindScrollListener()},bindResizeListener(){this.resizeListener||(this.resizeListener=()=>{this.visible&&!a.isTouchDevice()&&(this.visible=!1)},window.addEventListener("resize",this.resizeListener))},unbindResizeListener(){this.resizeListener&&(window.removeEventListener("resize",this.resizeListener),this.resizeListener=null)},isTargetClicked(e){return this.eventTarget&&(this.eventTarget===e.target||this.eventTarget.contains(e.target))},containerRef(e){this.container=e},createStyle(){if(!this.styleElement){this.styleElement=document.createElement("style"),this.styleElement.type="text/css",document.head.appendChild(this.styleElement);let e="";for(let t in this.breakpoints)e+=`
                        @media screen and (max-width: ${t}) {
                            .p-overlaypanel[${this.attributeSelector}] {
                                width: ${this.breakpoints[t]} !important;
                            }
                        }
                    `;this.styleElement.innerHTML=e}},destroyStyle(){this.styleElement&&(document.head.removeChild(this.styleElement),this.styleElement=null)},onOverlayClick(e){o.emit("overlay-click",{originalEvent:e,target:this.target})}},computed:{containerClass(){return["p-overlaypanel p-component",{"p-input-filled":this.$primevue.config.inputStyle==="filled","p-ripple-disabled":this.$primevue.config.ripple===!1}]},attributeSelector(){return m()}},directives:{ripple:k},components:{Portal:L}};const H=["aria-label"],A=u("span",{class:"p-overlaypanel-close-icon pi pi-times"},null,-1),I=[A];function B(e,t,s,r,n,i){const v=C("Portal"),b=g("ripple");return d(),w(v,{appendTo:s.appendTo},{default:p(()=>[E(T,{name:"p-overlaypanel",onEnter:i.onEnter,onLeave:i.onLeave,onAfterLeave:i.onAfterLeave},{default:p(()=>[n.visible?(d(),h("div",S({key:0,ref:i.containerRef,class:i.containerClass},e.$attrs,{onClick:t[3]||(t[3]=(...l)=>i.onOverlayClick&&i.onOverlayClick(...l))}),[u("div",{class:"p-overlaypanel-content",onClick:t[0]||(t[0]=(...l)=>i.onContentClick&&i.onContentClick(...l)),onMousedown:t[1]||(t[1]=(...l)=>i.onContentClick&&i.onContentClick(...l))},[x(e.$slots,"default")],32),s.showCloseIcon?z((d(),h("button",{key:0,class:"p-overlaypanel-close p-link",onClick:t[2]||(t[2]=(...l)=>i.hide&&i.hide(...l)),"aria-label":s.ariaCloseLabel,type:"button"},I,8,H)),[[b]]):f("",!0)],16)):f("",!0)]),_:3},8,["onEnter","onLeave","onAfterLeave"])]),_:3},8,["appendTo"])}function R(e,t){t===void 0&&(t={});var s=t.insertAt;if(!(!e||typeof document=="undefined")){var r=document.head||document.getElementsByTagName("head")[0],n=document.createElement("style");n.type="text/css",s==="top"&&r.firstChild?r.insertBefore(n,r.firstChild):r.appendChild(n),n.styleSheet?n.styleSheet.cssText=e:n.appendChild(document.createTextNode(e))}}var P=`
.p-overlaypanel {
    position: absolute;
    margin-top: 10px;
    top: 0;
    left: 0;
}
.p-overlaypanel-flipped {
    margin-top: 0;
    margin-bottom: 10px;
}
.p-overlaypanel-close {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-pack: center;
        -ms-flex-pack: center;
            justify-content: center;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    overflow: hidden;
    position: relative;
}

/* Animation */
.p-overlaypanel-enter-from {
    opacity: 0;
    -webkit-transform: scaleY(0.8);
            transform: scaleY(0.8);
}
.p-overlaypanel-leave-to {
    opacity: 0;
}
.p-overlaypanel-enter-active {
    -webkit-transition: opacity 0.12s cubic-bezier(0, 0, 0.2, 1), -webkit-transform 0.12s cubic-bezier(0, 0, 0.2, 1);
    transition: opacity 0.12s cubic-bezier(0, 0, 0.2, 1), -webkit-transform 0.12s cubic-bezier(0, 0, 0.2, 1);
    transition: transform 0.12s cubic-bezier(0, 0, 0.2, 1), opacity 0.12s cubic-bezier(0, 0, 0.2, 1);
    transition: transform 0.12s cubic-bezier(0, 0, 0.2, 1), opacity 0.12s cubic-bezier(0, 0, 0.2, 1), -webkit-transform 0.12s cubic-bezier(0, 0, 0.2, 1);
}
.p-overlaypanel-leave-active {
    -webkit-transition: opacity 0.1s linear;
    transition: opacity 0.1s linear;
}
.p-overlaypanel:after,
.p-overlaypanel:before {
    bottom: 100%;
    left: calc(var(--overlayArrowLeft, 0) + 1.25rem);
    content: ' ';
    height: 0;
    width: 0;
    position: absolute;
    pointer-events: none;
}
.p-overlaypanel:after {
    border-width: 8px;
    margin-left: -8px;
}
.p-overlaypanel:before {
    border-width: 10px;
    margin-left: -10px;
}
.p-overlaypanel-flipped:after,
.p-overlaypanel-flipped:before {
    bottom: auto;
    top: 100%;
}
.p-overlaypanel.p-overlaypanel-flipped:after {
    border-bottom-color: transparent;
}
.p-overlaypanel.p-overlaypanel-flipped:before {
    border-bottom-color: transparent;
}
`;R(P);O.render=B;export{O as s};
//# sourceMappingURL=overlaypanel.esm-944aa237.js.map
