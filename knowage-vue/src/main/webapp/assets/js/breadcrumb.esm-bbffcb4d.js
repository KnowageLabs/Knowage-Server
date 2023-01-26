import{r as y,o as n,c as i,B as u,h as b,w as _,a as h,G as o,A as c,t as f,a0 as g,C as B,f as w}from"./index-1e676509.js";var k={name:"BreadcrumbItem",props:{item:null,template:null,exact:null},methods:{onClick(e,a){this.item.command&&this.item.command({originalEvent:e,item:this.item}),this.item.to&&a&&a(e)},containerClass(e){return[{"p-disabled":this.disabled(e)},this.item.class]},linkClass(e){return["p-menuitem-link",{"router-link-active":e&&e.isActive,"router-link-active-exact":this.exact&&e&&e.isExactActive}]},visible(){return typeof this.item.visible=="function"?this.item.visible():this.item.visible!==!1},disabled(e){return typeof e.disabled=="function"?e.disabled():e.disabled},label(){return typeof this.item.label=="function"?this.item.label():this.item.label}},computed:{iconClass(){return["p-menuitem-icon",this.item.icon]}}};const A=["href","onClick"],E={key:1,class:"p-menuitem-text"},I=["href","target"],N={key:1,class:"p-menuitem-text"};function S(e,a,t,r,s,l){const d=y("router-link");return l.visible()?(n(),i("li",{key:0,class:o(l.containerClass(t.item))},[t.template?(n(),b(g(t.template),{key:1,item:t.item},null,8,["item"])):(n(),i(u,{key:0},[t.item.to?(n(),b(d,{key:0,to:t.item.to,custom:""},{default:_(({navigate:m,href:p,isActive:x,isExactActive:C})=>[h("a",{href:p,class:o(l.linkClass({isActive:x,isExactActive:C})),onClick:v=>l.onClick(v,m)},[t.item.icon?(n(),i("span",{key:0,class:o(l.iconClass)},null,2)):c("",!0),t.item.label?(n(),i("span",E,f(l.label()),1)):c("",!0)],10,A)]),_:1},8,["to"])):(n(),i("a",{key:1,href:t.item.url||"#",class:o(l.linkClass()),onClick:a[0]||(a[0]=(...m)=>l.onClick&&l.onClick(...m)),target:t.item.target},[t.item.icon?(n(),i("span",{key:0,class:o(l.iconClass)},null,2)):c("",!0),t.item.label?(n(),i("span",N,f(l.label()),1)):c("",!0)],10,I))],64))],2)):c("",!0)}k.render=S;var T={name:"Breadcrumb",props:{model:{type:Array,default:null},home:{type:null,default:null},exact:{type:Boolean,default:!0}},components:{BreadcrumbItem:k}};const V={class:"p-breadcrumb p-component","aria-label":"Breadcrumb"},z=h("li",{class:"p-breadcrumb-chevron pi pi-chevron-right"},null,-1);function D(e,a,t,r,s,l){const d=y("BreadcrumbItem");return n(),i("nav",V,[h("ul",null,[t.home?(n(),b(d,{key:0,item:t.home,class:"p-breadcrumb-home",template:e.$slots.item,exact:t.exact},null,8,["item","template","exact"])):c("",!0),(n(!0),i(u,null,B(t.model,m=>(n(),i(u,{key:m.label},[z,w(d,{item:m,template:e.$slots.item,exact:t.exact},null,8,["item","template","exact"])],64))),128))])])}function j(e,a){a===void 0&&(a={});var t=a.insertAt;if(!(!e||typeof document=="undefined")){var r=document.head||document.getElementsByTagName("head")[0],s=document.createElement("style");s.type="text/css",t==="top"&&r.firstChild?r.insertBefore(s,r.firstChild):r.appendChild(s),s.styleSheet?s.styleSheet.cssText=e:s.appendChild(document.createTextNode(e))}}var F=`
.p-breadcrumb {
    overflow-x: auto;
}
.p-breadcrumb ul {
    margin: 0;
    padding: 0;
    list-style-type: none;
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    -ms-flex-wrap: nowrap;
        flex-wrap: nowrap;
}
.p-breadcrumb .p-menuitem-text {
    line-height: 1;
}
.p-breadcrumb .p-menuitem-link {
    text-decoration: none;
}
.p-breadcrumb::-webkit-scrollbar {
    display: none;
}
`;j(F);T.render=D;export{T as s};
//# sourceMappingURL=breadcrumb.esm-bbffcb4d.js.map
