import{o as a,c as r,J as m,G as d,A as c,t as p,N as u}from"./index-1e676509.js";var h={name:"Chip",emits:["remove"],props:{label:{type:String,default:null},icon:{type:String,default:null},image:{type:String,default:null},removable:{type:Boolean,default:!1},removeIcon:{type:String,default:"pi pi-times-circle"}},data(){return{visible:!0}},methods:{close(i){this.visible=!1,this.$emit("remove",i)}},computed:{containerClass(){return["p-chip p-component",{"p-chip-image":this.image!=null}]},iconClass(){return["p-chip-icon",this.icon]},removeIconClass(){return["p-chip-remove-icon",this.removeIcon]}}};const y=["src"],f={key:2,class:"p-chip-text"};function v(i,n,t,s,e,l){return e.visible?(a(),r("div",{key:0,class:d(l.containerClass)},[m(i.$slots,"default",{},()=>[t.image?(a(),r("img",{key:0,src:t.image},null,8,y)):t.icon?(a(),r("span",{key:1,class:d(l.iconClass)},null,2)):c("",!0),t.label?(a(),r("div",f,p(t.label),1)):c("",!0)]),t.removable?(a(),r("span",{key:0,tabindex:"0",class:d(l.removeIconClass),onClick:n[0]||(n[0]=(...o)=>l.close&&l.close(...o)),onKeydown:n[1]||(n[1]=u((...o)=>l.close&&l.close(...o),["enter"]))},null,34)):c("",!0)],2)):c("",!0)}function g(i,n){n===void 0&&(n={});var t=n.insertAt;if(!(!i||typeof document=="undefined")){var s=document.head||document.getElementsByTagName("head")[0],e=document.createElement("style");e.type="text/css",t==="top"&&s.firstChild?s.insertBefore(e,s.firstChild):s.appendChild(e),e.styleSheet?e.styleSheet.cssText=i:e.appendChild(document.createTextNode(i))}}var b=`
.p-chip {
    display: -webkit-inline-box;
    display: -ms-inline-flexbox;
    display: inline-flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
}
.p-chip-text {
    line-height: 1.5;
}
.p-chip-icon.pi {
    line-height: 1.5;
}
.p-chip-remove-icon {
    line-height: 1.5;
    cursor: pointer;
}
.p-chip img {
    border-radius: 50%;
}
`;g(b);h.render=v;export{h as s};
//# sourceMappingURL=chip.esm-057ba1f1.js.map
