<template>
    <i class="fa-solid fa-grip-vertical drag-handle drag-widget-icon"></i>
    <div v-if="(selectionIsLocked || playSelectionButtonVisible) && inFocus" class="lockButtonContainer" @mouseover="$emit('changeFocus', true)" @mouseleave="$emit('changeFocus', false)">
        <i v-if="selectionIsLocked" class="fas fa-lock kn-cursor-pointer" @click="$emit('unlockSelection')" />
        <i v-if="playSelectionButtonVisible" class="fas fa-play kn-cursor-pointer" @click="$emit('launchSelection')" />
    </div>

    <div class="widgetButtonBarContainer">
        <Button type="button" icon="fa-solid fa-ellipsis-h" @click="toggle" class="p-button-outlined p-button-rounded widgetMenuButton" />
        <Menu label="Toggle" @click="toggle" :model="menuItems" ref="widgetmenu" :popup="true" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget buttons and visibility.
 */
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../Dashboard'
import SpeedDial from 'primevue/speeddial'
import Menu from 'primevue/menu'

export default defineComponent({
    name: 'widget-button-bar',
    components: { Menu, SpeedDial },
    props: {
        widget: { type: Object as PropType<IWidget>, required: true },
        playSelectionButtonVisible: { type: Boolean, required: true },
        selectionIsLocked: { type: Boolean, required: true },
        dashboardId: { type: String, required: true },
        inFocus: { type: Boolean, required: true },
        menuItems: { type: Object as PropType<any> }
    },
    emits: ['editWidget', 'unlockSelection', 'launchSelection', 'changeFocus'],
    methods: {
        toggle(event) {
            const menu = this.$refs.widgetmenu as any
            menu.toggle(event)
        },
        editWidget() {
            this.$emit('editWidget')
        }
    }
})
</script>
<style lang="scss">
.lockButtonContainer {
    width: 32px;
    height: 32px;
    position: absolute;
    right: -32px;
    background-color: #a9c3db;
    color: rgb(82, 82, 82);
    border: 1px solid #ccc;
    display: flex;
    z-index: 99999999 !important;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    align-content: center;
}
.widgetButtonBarContainer {
    display: none;
    position: absolute;
    top: 2px;
    right: 2px;
    .widgetMenuButton.p-button.p-button-outlined:enabled {
        background-color: rgba(256, 256, 256, 0.6);
        &:hover {
            outline: 2px solid #0085f290;
            background-color: rgba(246, 246, 246, 0.8);
        }
        &:active {
            background-color: rgba(216, 216, 216, 0.8);
        }
    }
}
.drag-widget-icon {
    position: absolute;
    display: none;
    top: 0;
    left: 0;
    height: 26px;
    width: 26px;
    text-align: center;
    padding: 6px;
    font-size: 1.1rem;
    cursor: grab;
    color: rgba(0, 0, 0, 0.4);
    &:active {
        cursor: grabbing;
    }
}
.speed-dial-menu {
    position: relative;
    .p-speeddial-button {
        width: 3.5rem !important;
        height: 3.5rem !important;
        span {
            font-size: 1.5rem !important;
        }
    }
}
</style>
