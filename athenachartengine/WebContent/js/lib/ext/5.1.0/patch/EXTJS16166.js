Ext.define('Ext.patch.EXTJS16166', {
    override: 'Ext.view.View',
    compatibility: '5.1.0.107',
    handleEvent: function(e) {
        var me = this,
            isKeyEvent = me.keyEventRe.test(e.type),
            nm = me.getNavigationModel();

        e.view = me;
        
        if (isKeyEvent) {
            e.item = nm.getItem();
            e.record = nm.getRecord();
        }

        // If the key event was fired programatically, it will not have triggered the focus
        // so the NavigationModel will not have this information.
        if (!e.item) {
            e.item = e.getTarget(me.itemSelector);
        }
        if (e.item && !e.record) {
            e.record = me.getRecord(e.item);
        }

        if (me.processUIEvent(e) !== false) {
            me.processSpecialEvent(e);
        }
        
        // We need to prevent default action on navigation keys
        // that can cause View element scroll unless the event is from an input field.
        // We MUST prevent browser's default action on SPACE which is to focus the event's target element.
        // Focusing causes the browser to attempt to scroll the element into view.
        
        if (isKeyEvent && !Ext.fly(e.target).isInputField()) {
            if (e.getKey() === e.SPACE || e.isNavKeyPress(true)) {
                e.preventDefault();
            }
        }
    }
});