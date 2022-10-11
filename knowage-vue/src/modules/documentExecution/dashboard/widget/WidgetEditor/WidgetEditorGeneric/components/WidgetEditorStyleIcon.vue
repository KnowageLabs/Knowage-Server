<template>
    <div class="icon-container" :class="{ 'icon-disabled': disabled }">
        <div id="color-picker-target" class="p-d-flex p-flex-row p-jc-center p-ai-center" v-tooltip.top="{ value: icon.tooltip ? $t(icon.tooltip) : '', disabled: !icon.tooltip }" @click="openAdditionalComponents">
            <i :class="[icon.class, active ? 'active-icon' : '']" class="widget-editor-icon kn-cursor-pointer p-mr-2" @click="onIconClicked(icon)"></i>
            <div v-show="icon.arrowDownIcon || icon.colorCircleIcon">
                <div v-show="icon.colorCircleIcon" class="style-circle-icon" :style="{ 'background-color': newColor }"></div>
                <i v-show="icon.arrowDownIcon" class="fas fa-arrow-down style-arrow-down-icon"></i>
            </div>
            <span v-if="icon.contextMenuSettings?.displayValue" class="icon-display-value-span p-ml-1">{{ '(' + displayValue + ')' }}</span>
        </div>
        <ColorPicker class="style-icon-color-picker" v-if="icon.colorPickerSettings && colorPickerVisible" v-model="color" :inline="true" format="rgb" @change="onColorPickerChange" />
        <WidgetEditorToolbarContextMenu class="context-menu" v-if="icon.contextMenuSettings && contextMenuVisible" :settings="icon.contextMenuSettings" :options="getContextMenuOptions()" @selected="onContextItemSelected" @inputChanged="onContextInputChanged"></WidgetEditorToolbarContextMenu>
        <WidgetEditorIconPickerDialog v-if="iconPickerDialogVisible" :widgetModel="widgetModel" :settings="icon.iconPickerSettings" :itemIndex="itemIndex" @close="iconPickerDialogVisible = false" @save="onIconSelected"></WidgetEditorIconPickerDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import { emitter } from '../../../../DashboardHelpers'
import ColorPicker from 'primevue/colorpicker'
import WidgetEditorToolbarContextMenu from './WidgetEditorToolbarContextMenu.vue'
import WidgetEditorIconPickerDialog from './WidgetEditorIconPickerDialog.vue'

export default defineComponent({
    name: 'name',
    components: { ColorPicker, WidgetEditorToolbarContextMenu, WidgetEditorIconPickerDialog },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, icon: { type: Object, required: true }, item: { type: Object }, itemIndex: { type: Number } },
    data() {
        return {
            active: false,
            contextMenuVisible: false,
            contextMenuInput: '',
            displayValue: '',
            color: null,
            newColor: 'rgb(255, 255, 255)',
            colorPickerVisible: false,
            iconPickerDialogVisible: false,
            disabled: false
        }
    },
    created() {
        emitter.on('toolbarIconContextMenuOpened', (event) => {
            this.closePopups(event)
        })
        emitter.on('toolbarIconColorPickerOpened', (event) => {
            this.closePopups(event)
        })
        this.iconIsActive()
        this.updateDisplayValue()
        this.loadInitialColorValue()
        this.iconIsDisabled(this.itemIndex)
        if (this.icon.watchers) {
            for (let i = 0; i < this.icon.watchers.length; i++) {
                this.$watch(
                    'widgetModel.' + this.icon.watchers[i],
                    () => {
                        this.iconIsActive(), this.updateDisplayValue(), this.iconIsDisabled(this.itemIndex)
                    },

                    { deep: true }
                )
            }
        }
    },
    methods: {
        onIconClicked(icon: any) {
            if (!icon || !icon.function || this.disabled) return

            const tempFunction = getModelProperty(this.widgetModel, icon.function, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel, this.item, this.itemIndex)
        },
        iconIsActive() {
            if (!this.icon.isActiveFunction) return (this.active = false)

            const tempFunction = getModelProperty(this.widgetModel, this.icon.isActiveFunction, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.active = tempFunction(this.widgetModel, this.item, this.itemIndex))
        },
        loadInitialColorValue() {
            if (!this.icon.colorPickerSettings) return
            const tempFunction = getModelProperty(this.widgetModel, this.icon.colorPickerSettings.initialValue, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') this.newColor = tempFunction(this.widgetModel, this.item, this.itemIndex)
        },
        openAdditionalComponents() {
            if (this.disabled) return
            this.changeColorPickerVisibility()
            this.changeContextMenuVisibility()
            this.changeIconPickerVisibility()
        },
        changeColorPickerVisibility() {
            this.colorPickerVisible = !this.colorPickerVisible
            if (this.colorPickerVisible) {
                emitter.emit('toolbarIconColorPickerOpened', this.icon)
            }
        },
        changeContextMenuVisibility() {
            this.contextMenuVisible = !this.contextMenuVisible
            if (this.contextMenuVisible) {
                emitter.emit('toolbarIconContextMenuOpened', this.icon)
            } else {
                this.updateValueFromContextInput()
            }
        },
        changeIconPickerVisibility() {
            if (this.icon.iconPickerSettings) this.iconPickerDialogVisible = !this.iconPickerDialogVisible
        },
        updateValueFromContextInput() {
            if (this.contextMenuInput) this.callUpdateFunction(this.contextMenuInput)
            this.contextMenuInput = ''
        },
        closePopups(event: any) {
            this.closeContextMenu(event)
            this.closeColorPicker(event)
        },
        closeContextMenu(icon: any) {
            if (this.icon.class !== icon.class) {
                this.contextMenuVisible = false
                this.updateValueFromContextInput()
            }
        },
        closeColorPicker(icon: any) {
            if (this.icon.class !== icon.class) {
                this.colorPickerVisible = false
                this.updateColorInModel()
            }
        },
        onContextItemSelected(item: string) {
            if (item === 'input') return
            this.contextMenuInput = ''
            this.contextMenuVisible = false
            this.callUpdateFunction(item)
        },
        callUpdateFunction(newValue: string) {
            if (this.icon.contextMenuSettings?.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.icon.contextMenuSettings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(newValue, this.widgetModel, this.item, this.itemIndex)
            }
        },
        onContextInputChanged(item: string) {
            this.contextMenuInput = item
        },
        getContextMenuOptions() {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, this.icon.contextMenuSettings.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction()
            return temp
        },
        updateDisplayValue() {
            if (!this.icon.contextMenuSettings?.displayValue) return
            const tempFunction = getModelProperty(this.widgetModel, this.icon.contextMenuSettings.displayValue, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') this.displayValue = tempFunction(this.widgetModel, this.item, this.itemIndex)
        },
        onColorPickerChange(event: any) {
            if (!event.value) return
            this.newColor = `rgb(${event.value.r}, ${event.value.g}, ${event.value.b})`
            this.updateColorInModel()
        },
        updateColorInModel() {
            if (this.icon.colorPickerSettings?.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, this.icon.colorPickerSettings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.newColor, this.widgetModel, this.item, this.itemIndex)
            }
        },
        iconIsDisabled(itemIndex: number | undefined) {
            if (!this.icon.disabledCondition) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.icon.disabledCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel, itemIndex))
        },
        onIconSelected(icon: any) {
            this.iconPickerDialogVisible = false

            if (!this.icon.iconPickerSettings.onSelect) return (this.disabled = false)
            const tempFunction = getModelProperty(this.widgetModel, this.icon.iconPickerSettings.onSelect, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(icon, this.widgetModel, this.item, this.itemIndex))
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

.icon-display-value-span {
    font-size: 0.7rem;
}

.style-icon-color-picker {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 100000;
}

.icon-disabled {
    color: #c2c2c2;
}
</style>
