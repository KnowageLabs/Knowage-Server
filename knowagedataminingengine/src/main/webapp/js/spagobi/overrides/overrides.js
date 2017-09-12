/* =============================================================================
* Bug Fix: It is not possible to see the column hide menu when the sortable property
* is set to false
* See:
* - http://extjs.com/forum/showthread.php?p=124583
* - http://extjs.com/forum/showthread.php?t=17379
============================================================================= */
Ext.override(Ext.grid.ColumnModel, {
  isMenuDisabled : function(col) {
    return !!this.config[col].menuDisabled;
  }
}); 

Ext.override(Ext.grid.GridView, {
  // private
  handleHdOver : function(e, t) {
    var hd = this.findHeaderCell(t);
    if (hd && !this.headersDisabled) {
      this.activeHd = hd;
      this.activeHdIndex = this.getCellIndex(hd);
      var fly = this.fly(hd);
      this.activeHdRegion = fly.getRegion();
      if (!this.cm.isMenuDisabled(this.activeHdIndex)) {
        fly.addClass("x-grid3-hd-over");
        this.activeHdBtn = fly.child('.x-grid3-hd-btn');
        if (this.activeHdBtn) {
          this.activeHdBtn.dom.style.height = (hd.firstChild.offsetHeight-1)+'px';
        }
      }
    }
  }
});


/* =============================================================================
* Not so sure this override is still so useful. BTW it copies attributes passed by
* the server into node attributes property of the node if the node is a leaf. If the node is not
* a leaf attribute are handled normally (i.e. they are copied in attributes property
* of the attributes subobject of the node). This mean that in order to access to attributes
* passed in by the server you have to do like this node.attributes if the node is a leaf
* an like this node.attributes.attributes if the node is not a leaf. This is good in
* general because you do not want to access to attributes on a non leaf node. But
* if this is not the case this difference i n handling node's attributes is boaring and 
* error prone. The obvious fix of applying the same patch also to non leaf node does
* not work unfortunately because in the case of AsyncNode the attributes property of
* the node is used internally in order to handle asynchronous loading and/or asynchronous
* rendering. The best solution to apply in the near future is to move all attribute passed in by 
* the server into a nod first level property named differently (ex. props) both in the case
* of leaf and non-leaf nodes (Andrea Gioia)
============================================================================= */
Ext.override(Ext.tree.TreeLoader, {
    createNode : function(attr){
        // apply baseAttrs, nice idea Corey!
        if(this.baseAttrs){
            Ext.applyIf(attr, this.baseAttrs);
        }
        if(this.applyLoader !== false){
            attr.loader = this;
        }
        if(typeof attr.uiProvider == 'string'){
           attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
        }
        
        var resultNode;
        if(attr.leaf) {
        	resultNode = new Ext.tree.TreeNode(attr);
        	resultNode.attributes = attr.attributes;
        } else {
        	resultNode = new Ext.tree.AsyncTreeNode(attr);
        }
        
        return resultNode;
    }
}) ; 


/* =============================================================================
* Change the behaviour of method setRoot. Thanks to this override everytime the 
* root node change the tree is properly refreshed in order to reflect the 
* modifications  in the structure.
============================================================================= */

Ext.override(Ext.tree.TreePanel, {
    initComponent : function(){
        Ext.tree.TreePanel.superclass.initComponent.call(this);

        if(!this.eventModel){
            this.eventModel = new Ext.tree.TreeEventModel(this);
        }

        // initialize the loader
        var l = this.loader;
        if(!l){
            l = new Ext.tree.TreeLoader({
                dataUrl: this.dataUrl
            });
        }else if(typeof l == 'object' && !l.load){
            l = new Ext.tree.TreeLoader(l);
        }
        this.loader = l;
        
        this.nodeHash = {};

        /**
        * The root node of this tree.
        * @type Ext.tree.TreeNode
        * @property root
        */
        // setRootNode destroys the existing root, so remove it first.
        if(this.root){
            var r = this.root;
            delete this.root;
            this.setRootNode(r);
        }

        this.addEvents(

            /**
            * @event append
            * Fires when a new child node is appended to a node in this tree.
            * @param {Tree} tree The owner tree
            * @param {Node} parent The parent node
            * @param {Node} node The newly appended node
            * @param {Number} index The index of the newly appended node
            */
           "append",
           /**
            * @event remove
            * Fires when a child node is removed from a node in this tree.
            * @param {Tree} tree The owner tree
            * @param {Node} parent The parent node
            * @param {Node} node The child node removed
            */
           "remove",
           /**
            * @event movenode
            * Fires when a node is moved to a new location in the tree
            * @param {Tree} tree The owner tree
            * @param {Node} node The node moved
            * @param {Node} oldParent The old parent of this node
            * @param {Node} newParent The new parent of this node
            * @param {Number} index The index it was moved to
            */
           "movenode",
           /**
            * @event insert
            * Fires when a new child node is inserted in a node in this tree.
            * @param {Tree} tree The owner tree
            * @param {Node} parent The parent node
            * @param {Node} node The child node inserted
            * @param {Node} refNode The child node the node was inserted before
            */
           "insert",
           /**
            * @event beforeappend
            * Fires before a new child is appended to a node in this tree, return false to cancel the append.
            * @param {Tree} tree The owner tree
            * @param {Node} parent The parent node
            * @param {Node} node The child node to be appended
            */
           "beforeappend",
           /**
            * @event beforeremove
            * Fires before a child is removed from a node in this tree, return false to cancel the remove.
            * @param {Tree} tree The owner tree
            * @param {Node} parent The parent node
            * @param {Node} node The child node to be removed
            */
           "beforeremove",
           /**
            * @event beforemovenode
            * Fires before a node is moved to a new location in the tree. Return false to cancel the move.
            * @param {Tree} tree The owner tree
            * @param {Node} node The node being moved
            * @param {Node} oldParent The parent of the node
            * @param {Node} newParent The new parent the node is moving to
            * @param {Number} index The index it is being moved to
            */
           "beforemovenode",
           /**
            * @event beforeinsert
            * Fires before a new child is inserted in a node in this tree, return false to cancel the insert.
            * @param {Tree} tree The owner tree
            * @param {Node} parent The parent node
            * @param {Node} node The child node to be inserted
            * @param {Node} refNode The child node the node is being inserted before
            */
            "beforeinsert",

            /**
            * @event beforeload
            * Fires before a node is loaded, return false to cancel
            * @param {Node} node The node being loaded
            */
            "beforeload",
            /**
            * @event load
            * Fires when a node is loaded
            * @param {Node} node The node that was loaded
            */
            "load",
            /**
            * @event textchange
            * Fires when the text for a node is changed
            * @param {Node} node The node
            * @param {String} text The new text
            * @param {String} oldText The old text
            */
            "textchange",
            /**
            * @event beforeexpandnode
            * Fires before a node is expanded, return false to cancel.
            * @param {Node} node The node
            * @param {Boolean} deep
            * @param {Boolean} anim
            */
            "beforeexpandnode",
            /**
            * @event beforecollapsenode
            * Fires before a node is collapsed, return false to cancel.
            * @param {Node} node The node
            * @param {Boolean} deep
            * @param {Boolean} anim
            */
            "beforecollapsenode",
            /**
            * @event expandnode
            * Fires when a node is expanded
            * @param {Node} node The node
            */
            "expandnode",
            /**
            * @event disabledchange
            * Fires when the disabled status of a node changes
            * @param {Node} node The node
            * @param {Boolean} disabled
            */
            "disabledchange",
            /**
            * @event collapsenode
            * Fires when a node is collapsed
            * @param {Node} node The node
            */
            "collapsenode",
            /**
            * @event beforeclick
            * Fires before click processing on a node. Return false to cancel the default action.
            * @param {Node} node The node
            * @param {Ext.EventObject} e The event object
            */
            "beforeclick",
            /**
            * @event click
            * Fires when a node is clicked
            * @param {Node} node The node
            * @param {Ext.EventObject} e The event object
            */
            "click",
            /**
            * @event checkchange
            * Fires when a node with a checkbox's checked property changes
            * @param {Node} this This node
            * @param {Boolean} checked
            */
            "checkchange",
            /**
            * @event dblclick
            * Fires when a node is double clicked
            * @param {Node} node The node
            * @param {Ext.EventObject} e The event object
            */
            "dblclick",
            /**
            * @event contextmenu
            * Fires when a node is right clicked. To display a context menu in response to this
            * event, first create a Menu object (see {@link Ext.menu.Menu} for details), then add
            * a handler for this event:<code><pre>
new Ext.tree.TreePanel({
    title: 'My TreePanel',
    root: new Ext.tree.AsyncTreeNode({
        text: 'The Root',
        children: [
            { text: 'Child node 1', leaf: true },
            { text: 'Child node 2', leaf: true }
        ]
    }),
    contextMenu: new Ext.menu.Menu({
        items: [{
            id: 'delete-node',
            text: 'Delete Node'
        }],
        listeners: {
            itemclick: function(item) {
                switch (item.id) {
                    case 'delete-node':
                        var n = item.parentMenu.contextNode;
                        if (n.parentNode) {
                            n.remove();
                        }
                        break;
                }
            }
        }
    }),
    listeners: {
        contextmenu: function(node, e) {
//          Register the context node with the menu so that a Menu Item's handler function can access
//          it via its {@link Ext.menu.BaseItem#parentMenu parentMenu} property.
            node.select();
            var c = node.getOwnerTree().contextMenu;
            c.contextNode = node;
            c.showAt(e.getXY());
        }
    }
});
</pre></code>
            * @param {Node} node The node
            * @param {Ext.EventObject} e The event object
            */
            "contextmenu",
            /**
            * @event beforechildrenrendered
            * Fires right before the child nodes for a node are rendered
            * @param {Node} node The node
            */
            "beforechildrenrendered",
           /**
             * @event startdrag
             * Fires when a node starts being dragged
             * @param {Ext.tree.TreePanel} this
             * @param {Ext.tree.TreeNode} node
             * @param {event} e The raw browser event
             */
            "startdrag",
            /**
             * @event enddrag
             * Fires when a drag operation is complete
             * @param {Ext.tree.TreePanel} this
             * @param {Ext.tree.TreeNode} node
             * @param {event} e The raw browser event
             */
            "enddrag",
            /**
             * @event dragdrop
             * Fires when a dragged node is dropped on a valid DD target
             * @param {Ext.tree.TreePanel} this
             * @param {Ext.tree.TreeNode} node
             * @param {DD} dd The dd it was dropped on
             * @param {event} e The raw browser event
             */
            "dragdrop",
            /**
             * @event beforenodedrop
             * Fires when a DD object is dropped on a node in this tree for preprocessing. Return false to cancel the drop. The dropEvent
             * passed to handlers has the following properties:<br />
             * <ul style="padding:5px;padding-left:16px;">
             * <li>tree - The TreePanel</li>
             * <li>target - The node being targeted for the drop</li>
             * <li>data - The drag data from the drag source</li>
             * <li>point - The point of the drop - append, above or below</li>
             * <li>source - The drag source</li>
             * <li>rawEvent - Raw mouse event</li>
             * <li>dropNode - Drop node(s) provided by the source <b>OR</b> you can supply node(s)
             * to be inserted by setting them on this object.</li>
             * <li>cancel - Set this to true to cancel the drop.</li>
             * <li>dropStatus - If the default drop action is cancelled but the drop is valid, setting this to true
             * will prevent the animated "repair" from appearing.</li>
             * </ul>
             * @param {Object} dropEvent
             */
            "beforenodedrop",
            /**
             * @event nodedrop
             * Fires after a DD object is dropped on a node in this tree. The dropEvent
             * passed to handlers has the following properties:<br />
             * <ul style="padding:5px;padding-left:16px;">
             * <li>tree - The TreePanel</li>
             * <li>target - The node being targeted for the drop</li>
             * <li>data - The drag data from the drag source</li>
             * <li>point - The point of the drop - append, above or below</li>
             * <li>source - The drag source</li>
             * <li>rawEvent - Raw mouse event</li>
             * <li>dropNode - Dropped node(s).</li>
             * </ul>
             * @param {Object} dropEvent
             */
            "nodedrop",
             /**
             * @event nodedragover
             * Fires when a tree node is being targeted for a drag drop, return false to signal drop not allowed. The dragOverEvent
             * passed to handlers has the following properties:<br />
             * <ul style="padding:5px;padding-left:16px;">
             * <li>tree - The TreePanel</li>
             * <li>target - The node being targeted for the drop</li>
             * <li>data - The drag data from the drag source</li>
             * <li>point - The point of the drop - append, above or below</li>
             * <li>source - The drag source</li>
             * <li>rawEvent - Raw mouse event</li>
             * <li>dropNode - Drop node(s) provided by the source.</li>
             * <li>cancel - Set this to true to signal drop not allowed.</li>
             * </ul>
             * @param {Object} dragOverEvent
             */
            "nodedragover"
        );
        if(this.singleExpand){
            this.on("beforeexpandnode", this.restrictExpand, this);
        }
    },

    setRootNode: function(node, load) {
    	//      Already had one; destroy it.    	
    	if (this.root) {
            this.root.destroy();
        }
    	
        if(!node.render){ // attributes passed
            node = this.loader.createNode(node);
        }
        this.root = node;
        node.ownerTree = this;
        node.isRoot = true;
        this.registerNode(node);
        if(!this.rootVisible){
            var uiP = node.attributes.uiProvider;
            node.ui = uiP ? new uiP(node) : new Ext.tree.RootTreeNodeUI(node); 
        }

//      If we had previously rendered a tree, rerender it.
        if (this.innerCt) {
            this.innerCt.update('');
            this.afterRender();
        }
        return node;
    }
});