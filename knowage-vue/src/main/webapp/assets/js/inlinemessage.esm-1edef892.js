import{o,c,a,G as r,J as p,g as d}from"./index-1e676509.js";var m={name:"InlineMessage",props:{severity:{type:String,default:"error"}},timeout:null,data(){return{visible:!0}},mounted(){this.sticky||setTimeout(()=>{this.visible=!1},this.life)},computed:{containerClass(){return["p-inline-message p-component p-inline-message-"+this.severity,{"p-inline-message-icon-only":!this.$slots.default}]},iconClass(){return["p-inline-message-icon pi",{"pi-info-circle":this.severity==="info","pi-check":this.severity==="success","pi-exclamation-triangle":this.severity==="warn","pi-times-circle":this.severity==="error"}]}}};const f={class:"p-inline-message-text"},u=d("\xA0");function y(n,t,s,i,e,l){return o(),c("div",{"aria-live":"polite",class:r(l.containerClass)},[a("span",{class:r(l.iconClass)},null,2),a("span",f,[p(n.$slots,"default",{},()=>[u])])],2)}function h(n,t){t===void 0&&(t={});var s=t.insertAt;if(!(!n||typeof document=="undefined")){var i=document.head||document.getElementsByTagName("head")[0],e=document.createElement("style");e.type="text/css",s==="top"&&i.firstChild?i.insertBefore(e,i.firstChild):i.appendChild(e),e.styleSheet?e.styleSheet.cssText=n:e.appendChild(document.createTextNode(n))}}var x=`
.p-inline-message {
    display: -webkit-inline-box;
    display: -ms-inline-flexbox;
    display: inline-flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    -webkit-box-pack: center;
        -ms-flex-pack: center;
            justify-content: center;
    vertical-align: top;
}
.p-inline-message-icon-only .p-inline-message-text {
    visibility: hidden;
    width: 0;
}
.p-fluid .p-inline-message {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
}
`;h(x);m.render=y;export{m as s};
//# sourceMappingURL=inlinemessage.esm-1edef892.js.map
