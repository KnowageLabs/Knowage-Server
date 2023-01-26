import{o as s,c as r,J as o,A as c,G as g,U as u,R as b,y,a,t as h,z as f,f as m,w as x,Y as k,T as _}from"./index-1e676509.js";var w={name:"Divider",props:{align:{type:String,default:null},layout:{type:String,default:"horizontal"},type:{type:String,default:"solid"}},computed:{containerClass(){return["p-divider p-component","p-divider-"+this.layout,"p-divider-"+this.type,{"p-divider-left":this.layout==="horizontal"&&(!this.align||this.align==="left")},{"p-divider-center":this.layout==="horizontal"&&this.align==="center"},{"p-divider-right":this.layout==="horizontal"&&this.align==="right"},{"p-divider-top":this.layout==="vertical"&&this.align==="top"},{"p-divider-center":this.layout==="vertical"&&(!this.align||this.align==="center")},{"p-divider-bottom":this.layout==="vertical"&&this.align==="bottom"}]}}};const z=["aria-orientation"],C={key:0,class:"p-divider-content"};function S(e,n,i,l,t,d){return s(),r("div",{class:g(d.containerClass),role:"separator","aria-orientation":i.layout},[e.$slots.default?(s(),r("div",C,[o(e.$slots,"default")])):c("",!0)],10,z)}function B(e,n){n===void 0&&(n={});var i=n.insertAt;if(!(!e||typeof document=="undefined")){var l=document.head||document.getElementsByTagName("head")[0],t=document.createElement("style");t.type="text/css",i==="top"&&l.firstChild?l.insertBefore(t,l.firstChild):l.appendChild(t),t.styleSheet?t.styleSheet.cssText=e:t.appendChild(document.createTextNode(e))}}var I=`
.p-divider-horizontal {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    width: 100%;
    position: relative;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
}
.p-divider-horizontal:before {
    position: absolute;
    display: block;
    top: 50%;
    left: 0;
    width: 100%;
    content: '';
}
.p-divider-horizontal.p-divider-left {
    -webkit-box-pack: start;
        -ms-flex-pack: start;
            justify-content: flex-start;
}
.p-divider-horizontal.p-divider-right {
    -webkit-box-pack: end;
        -ms-flex-pack: end;
            justify-content: flex-end;
}
.p-divider-horizontal.p-divider-center {
    -webkit-box-pack: center;
        -ms-flex-pack: center;
            justify-content: center;
}
.p-divider-content {
    z-index: 1;
}
.p-divider-vertical {
    min-height: 100%;
    margin: 0 1rem;
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    position: relative;
    -webkit-box-pack: center;
        -ms-flex-pack: center;
            justify-content: center;
}
.p-divider-vertical:before {
    position: absolute;
    display: block;
    top: 0;
    left: 50%;
    height: 100%;
    content: '';
}
.p-divider-vertical.p-divider-top {
    -webkit-box-align: start;
        -ms-flex-align: start;
            align-items: flex-start;
}
.p-divider-vertical.p-divider-center {
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
}
.p-divider-vertical.p-divider-bottom {
    -webkit-box-align: end;
        -ms-flex-align: end;
            align-items: flex-end;
}
.p-divider-solid.p-divider-horizontal:before {
    border-top-style: solid;
}
.p-divider-solid.p-divider-vertical:before {
    border-left-style: solid;
}
.p-divider-dashed.p-divider-horizontal:before {
    border-top-style: dashed;
}
.p-divider-dashed.p-divider-vertical:before {
    border-left-style: dashed;
}
.p-divider-dotted.p-divider-horizontal:before {
    border-top-style: dotted;
}
.p-divider-dotted.p-divider-horizontal:before {
    border-left-style: dotted;
}
`;B(I);w.render=S;var D={name:"Fieldset",emits:["update:collapsed","toggle"],props:{legend:String,toggleable:Boolean,collapsed:Boolean,toggleButtonProps:String},data(){return{d_collapsed:this.collapsed}},watch:{collapsed(e){this.d_collapsed=e}},methods:{toggle(e){this.d_collapsed=!this.d_collapsed,this.$emit("update:collapsed",this.d_collapsed),this.$emit("toggle",{originalEvent:e,value:this.d_collapsed})},onKeyDown(e){(e.code==="Enter"||e.code==="Space")&&(this.toggle(e),e.preventDefault())}},computed:{iconClass(){return["p-fieldset-toggler pi ",{"pi-minus":!this.d_collapsed,"pi-plus":this.d_collapsed}]},ariaId(){return u()}},directives:{ripple:b}};const T={class:"p-fieldset-legend"},j=["id"],E=["id","aria-controls","aria-expanded","aria-label"],N={class:"p-fieldset-legend-text"},A=["id","aria-labelledby"],K={class:"p-fieldset-content"};function V(e,n,i,l,t,d){const v=y("ripple");return s(),r("fieldset",{class:g(["p-fieldset p-component",{"p-fieldset-toggleable":i.toggleable}])},[a("legend",T,[i.toggleable?c("",!0):o(e.$slots,"legend",{key:0},()=>[a("span",{id:d.ariaId+"_header",class:"p-fieldset-legend-text"},h(i.legend),9,j)]),i.toggleable?f((s(),r("a",{key:1,id:d.ariaId+"_header",tabindex:"0",role:"button","aria-controls":d.ariaId+"_content","aria-expanded":!t.d_collapsed,"aria-label":i.toggleButtonProps||i.legend,onClick:n[0]||(n[0]=(...p)=>d.toggle&&d.toggle(...p)),onKeydown:n[1]||(n[1]=(...p)=>d.onKeyDown&&d.onKeyDown(...p))},[a("span",{class:g(d.iconClass)},null,2),o(e.$slots,"legend",{},()=>[a("span",N,h(i.legend),1)])],40,E)),[[v]]):c("",!0)]),m(_,{name:"p-toggleable-content"},{default:x(()=>[f(a("div",{id:d.ariaId+"_content",class:"p-toggleable-content",role:"region","aria-labelledby":d.ariaId+"_header"},[a("div",K,[o(e.$slots,"default")])],8,A),[[k,!t.d_collapsed]])]),_:3})],2)}function P(e,n){n===void 0&&(n={});var i=n.insertAt;if(!(!e||typeof document=="undefined")){var l=document.head||document.getElementsByTagName("head")[0],t=document.createElement("style");t.type="text/css",i==="top"&&l.firstChild?l.insertBefore(t,l.firstChild):l.appendChild(t),t.styleSheet?t.styleSheet.cssText=e:t.appendChild(document.createTextNode(e))}}var R=`
.p-fieldset-legend > a,
.p-fieldset-legend > span {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    -webkit-box-pack: center;
        -ms-flex-pack: center;
            justify-content: center;
}
.p-fieldset-toggleable .p-fieldset-legend a {
    cursor: pointer;
    -webkit-user-select: none;
       -moz-user-select: none;
        -ms-user-select: none;
            user-select: none;
    overflow: hidden;
    position: relative;
    text-decoration: none;
}
.p-fieldset-legend-text {
    line-height: 1;
}
`;P(R);D.render=V;export{w as a,D as s};
//# sourceMappingURL=fieldset.esm-61ec4a9f.js.map
