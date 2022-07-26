<template>
    <div class="icon-container">
        <div class="p-d-flex p-flex-row p-jc-center p-ai-center" @click="changeContextMenuVisibility">
            <i :class="[icon.class, active ? 'active-icon' : '']" class="widget-editor-icon kn-cursor-pointer p-mr-2" @click="onIconClicked(icon)"></i>
            <div v-show="icon.arrowDownIcon || icon.colorCircleIcon">
                <div v-show="icon.colorCircleIcon" class="style-circle-icon"></div>
                <i v-show="icon.arrowDownIcon" class="fas fa-arrow-down style-arrow-down-icon"></i>
            </div>
        </div>

        <WidgetEditorToolbarContextMenu class="context-menu" v-if="icon.contextMenuSettings && contextMenuVisible" :settings="icon.contextMenuSettings" :options="getContextMenuOptions()" @selected="onContextItemSelected" @inputChanged="onContextInputChanged"></WidgetEditorToolbarContextMenu>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { emitter } from '../../../../DashboardHelpers'
import WidgetEditorToolbarContextMenu from './WidgetEditorToolbarContextMenu.vue'

export default defineComponent({
    name: 'name',
    components: { WidgetEditorToolbarContextMenu },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, icon: { type: Object, required: true } },
    data() {
        return {
            active: false,
            contextMenuVisible: false,
            contextMenuInput: ''
        }
    },
    created() {
        emitter.on('toolbarIconContextMenuOpened', (event) => {
            this.closeContextMenu(event)
        })
        this.iconIsActive()
        if (this.icon.watchers) {
            for (let i = 0; i < this.icon.watchers.length; i++) {
                this.$watch(
                    'widgetModel.' + this.icon.watchers[i],
                    () => this.iconIsActive(),

                    { deep: true }
                )
            }
        }
    },
    methods: {
        onIconClicked(icon: any) {
            if (!icon || !icon.function) return

            const tempFunction = getModelProperty(this.widgetModel, icon.function, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel)
        },
        iconIsActive() {
            console.log('iconIsActive')
            if (!this.icon.isActiveFunction) return (this.active = false)

            const tempFunction = getModelProperty(this.widgetModel, this.icon.isActiveFunction, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.active = tempFunction(this.widgetModel))
        },
        changeContextMenuVisibility() {
            this.contextMenuVisible = !this.contextMenuVisible
            if (this.contextMenuVisible) {
                emitter.emit('toolbarIconContextMenuOpened', this.icon)
            } else {
                this.updateValueFromContextInput()
            }
        },
        updateValueFromContextInput() {
            if (this.contextMenuInput) this.callUpdateFunction(this.contextMenuInput)
            this.contextMenuInput = ''
        },
        closeContextMenu(icon: any) {
            if (this.icon.class !== icon.class) {
                this.contextMenuVisible = false
                this.updateValueFromContextInput()
            }
        },
        onContextItemSelected(item: string) {
            console.log('onContextItemSelected: ', item)
            if (item === 'input') return
            this.contextMenuInput = ''
            this.callUpdateFunction(item)
        },
        callUpdateFunction(newValue: string) {
            if (this.icon.contextMenuSettings?.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.icon.contextMenuSettings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(newValue, this.widgetModel)
            }
        },
        onContextInputChanged(item: string) {
            console.log('onContextInputChanged: ', item)
            this.contextMenuInput = item
        },
        getContextMenuOptions() {
            console.log('getContextMenuOptions: ', this.icon.contextMenuSettings.options)
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, this.icon.contextMenuSettings.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction()
            return temp
        }
    }
})
</script>

<style lang="scss" scoped>
.active-icon {
    color: blue;
}

.widget-editor-icon {
    font-size: 1.2rem;
}

.style-circle-icon {
    margin: 0;
    border: 1px solid grey;
    border-radius: 6px;
    height: 10px;
    width: 10px;
    color: red;
}
.style-arrow-down-icon {
    font-size: 0.8rem;
}

.icon-container {
    position: relative;
}

.context-menu {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 999999;
}
</style>
