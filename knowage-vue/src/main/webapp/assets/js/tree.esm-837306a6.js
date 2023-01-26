import{a2 as K,L as p,R as N,r as k,y as w,o,c as a,a as c,z as m,G as u,A as f,h as y,a0 as S,B as g,g as v,t as M,D as x,C as b,a3 as _}from"./index-1e676509.js";var C={name:"TreeNode",emits:["node-toggle","node-click","checkbox-change"],props:{node:{type:null,default:null},expandedKeys:{type:null,default:null},selectionKeys:{type:null,default:null},selectionMode:{type:String,default:null},templates:{type:null,default:null},level:{type:Number,default:null},index:{type:Number,default:null}},nodeTouched:!1,methods:{toggle(){this.$emit("node-toggle",this.node)},label(e){return typeof e.label=="function"?e.label():e.label},onChildNodeToggle(e){this.$emit("node-toggle",e)},onClick(e){p.hasClass(e.target,"p-tree-toggler")||p.hasClass(e.target.parentElement,"p-tree-toggler")||(this.isCheckboxSelectionMode()?this.toggleCheckbox():this.$emit("node-click",{originalEvent:e,nodeTouched:this.nodeTouched,node:this.node}),this.nodeTouched=!1)},onChildNodeClick(e){this.$emit("node-click",e)},onTouchEnd(){this.nodeTouched=!0},onKeyDown(e){const t=e.target.parentElement;switch(e.code){case"ArrowDown":var n=t.children[1];if(n)this.focusNode(n.children[0]);else{const i=t.nextElementSibling;if(i)this.focusNode(i);else{let s=this.findNextSiblingOfAncestor(t);s&&this.focusNode(s)}}break;case"ArrowUp":if(t.previousElementSibling)this.focusNode(this.findLastVisibleDescendant(t.previousElementSibling));else{let i=this.getParentNodeElement(t);i&&this.focusNode(i)}break;case"ArrowRight":case"ArrowLeft":this.$emit("node-toggle",this.node);break;case"Enter":case"Space":this.onClick(e);break}e.preventDefault()},toggleCheckbox(){let e=this.selectionKeys?{...this.selectionKeys}:{};const t=!this.checked;this.propagateDown(this.node,t,e),this.$emit("checkbox-change",{node:this.node,check:t,selectionKeys:e})},propagateDown(e,t,n){if(t?n[e.key]={checked:!0,partialChecked:!1}:delete n[e.key],e.children&&e.children.length)for(let i of e.children)this.propagateDown(i,t,n)},propagateUp(e){let t=e.check,n={...e.selectionKeys},i=0,s=!1;for(let l of this.node.children)n[l.key]&&n[l.key].checked?i++:n[l.key]&&n[l.key].partialChecked&&(s=!0);t&&i===this.node.children.length?n[this.node.key]={checked:!0,partialChecked:!1}:(t||delete n[this.node.key],s||i>0&&i!==this.node.children.length?n[this.node.key]={checked:!1,partialChecked:!0}:delete n[this.node.key]),this.$emit("checkbox-change",{node:e.node,check:e.check,selectionKeys:n})},onChildCheckboxChange(e){this.$emit("checkbox-change",e)},findNextSiblingOfAncestor(e){let t=this.getParentNodeElement(e);return t?t.nextElementSibling?t.nextElementSibling:this.findNextSiblingOfAncestor(t):null},findLastVisibleDescendant(e){const t=e.children[1];if(t){const n=t.children[t.children.length-1];return this.findLastVisibleDescendant(n)}else return e},getParentNodeElement(e){const t=e.parentElement.parentElement;return p.hasClass(t,"p-treenode")?t:null},focusNode(e){e.children[0].focus()},isCheckboxSelectionMode(){return this.selectionMode==="checkbox"}},computed:{hasChildren(){return this.node.children&&this.node.children.length>0},expanded(){return this.expandedKeys&&this.expandedKeys[this.node.key]===!0},leaf(){return this.node.leaf===!1?!1:!(this.node.children&&this.node.children.length)},selectable(){return this.node.selectable===!1?!1:this.selectionMode!=null},selected(){return this.selectionMode&&this.selectionKeys?this.selectionKeys[this.node.key]===!0:!1},containerClass(){return["p-treenode",{"p-treenode-leaf":this.leaf}]},contentClass(){return["p-treenode-content",this.node.styleClass,{"p-treenode-selectable":this.selectable,"p-highlight":this.checkboxMode?this.checked:this.selected}]},icon(){return["p-treenode-icon",this.node.icon]},toggleIcon(){return["p-tree-toggler-icon pi pi-fw",this.expanded?this.node.expandedIcon||"pi-chevron-down":this.node.collapsedIcon||"pi-chevron-right"]},checkboxClass(){return["p-checkbox-box",{"p-highlight":this.checked,"p-indeterminate":this.partialChecked}]},checkboxIcon(){return["p-checkbox-icon",{"pi pi-check":this.checked,"pi pi-minus":this.partialChecked}]},checkboxMode(){return this.selectionMode==="checkbox"&&this.node.selectable!==!1},checked(){return this.selectionKeys?this.selectionKeys[this.node.key]&&this.selectionKeys[this.node.key].checked:!1},partialChecked(){return this.selectionKeys?this.selectionKeys[this.node.key]&&this.selectionKeys[this.node.key].partialChecked:!1}},directives:{ripple:N}};const T=["aria-label","aria-selected","aria-expanded","aria-setsize","aria-posinset","aria-level"],E=["aria-expanded"],D={key:0,class:"p-checkbox p-component"},V=["aria-checked"],L={class:"p-treenode-label"},F={key:0,class:"p-treenode-children",role:"group"};function B(e,t,n,i,s,l){const r=k("TreeNode",!0),h=w("ripple");return o(),a("li",{class:u(l.containerClass),role:"treeitem","aria-label":l.label(n.node),"aria-selected":l.selected,"aria-expanded":l.expanded,"aria-setsize":n.node.children?n.node.children.length:0,"aria-posinset":n.index+1,"aria-level":n.level},[c("div",{class:u(l.contentClass),tabindex:"0",role:"treeitem","aria-expanded":l.expanded,onClick:t[1]||(t[1]=(...d)=>l.onClick&&l.onClick(...d)),onKeydown:t[2]||(t[2]=(...d)=>l.onKeyDown&&l.onKeyDown(...d)),onTouchend:t[3]||(t[3]=(...d)=>l.onTouchEnd&&l.onTouchEnd(...d)),style:x(n.node.style)},[m((o(),a("button",{type:"button",class:"p-tree-toggler p-link",onClick:t[0]||(t[0]=(...d)=>l.toggle&&l.toggle(...d)),tabindex:"-1"},[c("span",{class:u(l.toggleIcon)},null,2)])),[[h]]),l.checkboxMode?(o(),a("div",D,[c("div",{class:u(l.checkboxClass),role:"checkbox","aria-checked":l.checked},[c("span",{class:u(l.checkboxIcon)},null,2)],10,V)])):f("",!0),c("span",{class:u(l.icon)},null,2),c("span",L,[n.templates[n.node.type]||n.templates.default?(o(),y(S(n.templates[n.node.type]||n.templates.default),{key:0,node:n.node},null,8,["node"])):(o(),a(g,{key:1},[v(M(l.label(n.node)),1)],64))])],46,E),l.hasChildren&&l.expanded?(o(),a("ul",F,[(o(!0),a(g,null,b(n.node.children,d=>(o(),y(r,{key:d.key,node:d,templates:n.templates,level:n.level+1,expandedKeys:n.expandedKeys,onNodeToggle:l.onChildNodeToggle,onNodeClick:l.onChildNodeClick,selectionMode:n.selectionMode,selectionKeys:n.selectionKeys,onCheckboxChange:l.propagateUp},null,8,["node","templates","level","expandedKeys","onNodeToggle","onNodeClick","selectionMode","selectionKeys","onCheckboxChange"]))),128))])):f("",!0)],10,T)}C.render=B;var A={name:"Tree",emits:["node-expand","node-collapse","update:expandedKeys","update:selectionKeys","node-select","node-unselect"],props:{value:{type:null,default:null},expandedKeys:{type:null,default:null},selectionKeys:{type:null,default:null},selectionMode:{type:String,default:null},metaKeySelection:{type:Boolean,default:!0},loading:{type:Boolean,default:!1},loadingIcon:{type:String,default:"pi pi-spinner"},filter:{type:Boolean,default:!1},filterBy:{type:String,default:"label"},filterMode:{type:String,default:"lenient"},filterPlaceholder:{type:String,default:null},filterLocale:{type:String,default:void 0},scrollHeight:{type:String,default:null},level:{type:Number,default:0}},data(){return{d_expandedKeys:this.expandedKeys||{},filterValue:null}},watch:{expandedKeys(e){this.d_expandedKeys=e}},methods:{onNodeToggle(e){const t=e.key;this.d_expandedKeys[t]?(delete this.d_expandedKeys[t],this.$emit("node-collapse",e)):(this.d_expandedKeys[t]=!0,this.$emit("node-expand",e)),this.d_expandedKeys={...this.d_expandedKeys},this.$emit("update:expandedKeys",this.d_expandedKeys)},onNodeClick(e){if(this.selectionMode!=null&&e.node.selectable!==!1){const n=(e.nodeTouched?!1:this.metaKeySelection)?this.handleSelectionWithMetaKey(e):this.handleSelectionWithoutMetaKey(e);this.$emit("update:selectionKeys",n)}},onCheckboxChange(e){this.$emit("update:selectionKeys",e.selectionKeys),e.check?this.$emit("node-select",e.node):this.$emit("node-unselect",e.node)},handleSelectionWithMetaKey(e){const t=e.originalEvent,n=e.node,i=t.metaKey||t.ctrlKey,s=this.isNodeSelected(n);let l;return s&&i?(this.isSingleSelectionMode()?l={}:(l={...this.selectionKeys},delete l[n.key]),this.$emit("node-unselect",n)):(this.isSingleSelectionMode()?l={}:this.isMultipleSelectionMode()&&(l=i?this.selectionKeys?{...this.selectionKeys}:{}:{}),l[n.key]=!0,this.$emit("node-select",n)),l},handleSelectionWithoutMetaKey(e){const t=e.node,n=this.isNodeSelected(t);let i;return this.isSingleSelectionMode()?n?(i={},this.$emit("node-unselect",t)):(i={},i[t.key]=!0,this.$emit("node-select",t)):n?(i={...this.selectionKeys},delete i[t.key],this.$emit("node-unselect",t)):(i=this.selectionKeys?{...this.selectionKeys}:{},i[t.key]=!0,this.$emit("node-select",t)),i},isSingleSelectionMode(){return this.selectionMode==="single"},isMultipleSelectionMode(){return this.selectionMode==="multiple"},isNodeSelected(e){return this.selectionMode&&this.selectionKeys?this.selectionKeys[e.key]===!0:!1},isChecked(e){return this.selectionKeys?this.selectionKeys[e.key]&&this.selectionKeys[e.key].checked:!1},isNodeLeaf(e){return e.leaf===!1?!1:!(e.children&&e.children.length)},onFilterKeydown(e){e.which===13&&e.preventDefault()},findFilteredNodes(e,t){if(e){let n=!1;if(e.children){let i=[...e.children];e.children=[];for(let s of i){let l={...s};this.isFilterMatched(l,t)&&(n=!0,e.children.push(l))}}if(n)return!0}},isFilterMatched(e,{searchFields:t,filterText:n,strict:i}){let s=!1;for(let l of t)String(K.resolveFieldData(e,l)).toLocaleLowerCase(this.filterLocale).indexOf(n)>-1&&(s=!0);return(!s||i&&!this.isNodeLeaf(e))&&(s=this.findFilteredNodes(e,{searchFields:t,filterText:n,strict:i})||s),s}},computed:{containerClass(){return["p-tree p-component",{"p-tree-selectable":this.selectionMode!=null,"p-tree-loading":this.loading,"p-tree-flex-scrollable":this.scrollHeight==="flex"}]},loadingIconClass(){return["p-tree-loading-icon pi-spin",this.loadingIcon]},filteredValue(){let e=[];const t=this.filterBy.split(","),n=this.filterValue.trim().toLocaleLowerCase(this.filterLocale),i=this.filterMode==="strict";for(let s of this.value){let l={...s},r={searchFields:t,filterText:n,strict:i};(i&&(this.findFilteredNodes(l,r)||this.isFilterMatched(l,r))||!i&&(this.isFilterMatched(l,r)||this.findFilteredNodes(l,r)))&&e.push(l)}return e},valueToRender(){return this.filterValue&&this.filterValue.trim().length>0?this.filteredValue:this.value}},components:{TreeNode:C}};const I={key:0,class:"p-tree-loading-overlay p-component-overlay"},z={key:1,class:"p-tree-filter-container"},P=["placeholder"],H=c("span",{class:"p-tree-filter-icon pi pi-search"},null,-1),O={class:"p-tree-container",role:"tree"};function R(e,t,n,i,s,l){const r=k("TreeNode");return o(),a("div",{class:u(l.containerClass)},[n.loading?(o(),a("div",I,[c("i",{class:u(l.loadingIconClass)},null,2)])):f("",!0),n.filter?(o(),a("div",z,[m(c("input",{"onUpdate:modelValue":t[0]||(t[0]=h=>s.filterValue=h),type:"text",autocomplete:"off",class:"p-tree-filter p-inputtext p-component",placeholder:n.filterPlaceholder,onKeydown:t[1]||(t[1]=(...h)=>l.onFilterKeydown&&l.onFilterKeydown(...h))},null,40,P),[[_,s.filterValue]]),H])):f("",!0),c("div",{class:"p-tree-wrapper",style:x({maxHeight:n.scrollHeight})},[c("ul",O,[(o(!0),a(g,null,b(l.valueToRender,(h,d)=>(o(),y(r,{key:h.key,node:h,templates:e.$slots,level:n.level+1,index:d,expandedKeys:s.d_expandedKeys,onNodeToggle:l.onNodeToggle,onNodeClick:l.onNodeClick,selectionMode:n.selectionMode,selectionKeys:n.selectionKeys,onCheckboxChange:l.onCheckboxChange},null,8,["node","templates","level","index","expandedKeys","onNodeToggle","onNodeClick","selectionMode","selectionKeys","onCheckboxChange"]))),128))])],4)],2)}function U(e,t){t===void 0&&(t={});var n=t.insertAt;if(!(!e||typeof document=="undefined")){var i=document.head||document.getElementsByTagName("head")[0],s=document.createElement("style");s.type="text/css",n==="top"&&i.firstChild?i.insertBefore(s,i.firstChild):i.appendChild(s),s.styleSheet?s.styleSheet.cssText=e:s.appendChild(document.createTextNode(e))}}var W=`
.p-tree-container {
    margin: 0;
    padding: 0;
    list-style-type: none;
    overflow: auto;
}
.p-treenode-children {
    margin: 0;
    padding: 0;
    list-style-type: none;
}
.p-tree-wrapper {
    overflow: auto;
}
.p-treenode-selectable {
    cursor: pointer;
    -webkit-user-select: none;
       -moz-user-select: none;
        -ms-user-select: none;
            user-select: none;
}
.p-tree-toggler {
    cursor: pointer;
    -webkit-user-select: none;
       -moz-user-select: none;
        -ms-user-select: none;
            user-select: none;
    display: -webkit-inline-box;
    display: -ms-inline-flexbox;
    display: inline-flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    -webkit-box-pack: center;
        -ms-flex-pack: center;
            justify-content: center;
    overflow: hidden;
    position: relative;
    -ms-flex-negative: 0;
        flex-shrink: 0;
}
.p-treenode-leaf > .p-treenode-content .p-tree-toggler {
    visibility: hidden;
}
.p-treenode-content {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
}
.p-tree-filter {
    width: 100%;
}
.p-tree-filter-container {
    position: relative;
    display: block;
    width: 100%;
}
.p-tree-filter-icon {
    position: absolute;
    top: 50%;
    margin-top: -0.5rem;
}
.p-tree-loading {
    position: relative;
    min-height: 4rem;
}
.p-tree .p-tree-loading-overlay {
    position: absolute;
    z-index: 1;
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
.p-tree-flex-scrollable {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-flex: 1;
        -ms-flex: 1;
            flex: 1;
    height: 100%;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
        -ms-flex-direction: column;
            flex-direction: column;
}
.p-tree-flex-scrollable .p-tree-wrapper {
    -webkit-box-flex: 1;
        -ms-flex: 1;
            flex: 1;
}
`;U(W);A.render=R;export{A as s};
//# sourceMappingURL=tree.esm-837306a6.js.map
