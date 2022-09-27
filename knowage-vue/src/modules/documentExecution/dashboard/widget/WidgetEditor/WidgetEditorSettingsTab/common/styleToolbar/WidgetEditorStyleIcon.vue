<template>
    <div v-show="model" ref="knowageStyleIcon" class="click-outside icon-container" :class="{ 'icon-disabled': disabled }">
        <div id="color-picker-target" class="p-d-flex p-flex-row p-jc-center p-ai-center" v-tooltip.top="{ value: option.tooltip ? $t(option.tooltip) : getDefaultTooltip() }" @click="openAdditionalComponents">
            <i :class="[getIconClass(), active ? 'active-icon' : '']" class="widget-editor-icon kn-cursor-pointer p-mr-2" @click="onIconClicked"></i>
            <div v-show="showArowDown || showCircleIcon">
                <div v-show="showCircleIcon" class="style-circle-icon" :style="{ 'background-color': newColor }"></div>
                <i v-show="showArowDown" class="fas fa-arrow-down style-arrow-down-icon"></i>
            </div>
            <span v-if="option.type === 'font-size'" class="icon-display-value-span p-ml-1">{{ '(' + displayValue + ')' }}</span>
        </div>
        <ColorPicker class="style-icon-color-picker" v-if="(option.type === 'color' || option.type === 'background-color') && colorPickerVisible" v-model="color" :inline="true" format="rgb" @change="onColorPickerChange" />
        <WidgetEditorToolbarContextMenu
            class="context-menu"
            v-show="(option.type === 'font-size' || option.type === 'justify-content' || option.type === 'font-family') && contextMenuVisible"
            :option="option"
            @selected="onContextItemSelected"
            @inputChanged="onContextInputChanged"
        ></WidgetEditorToolbarContextMenu>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue'
import { IWidgetStyleToolbarModel } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import { getRGBColorFromString } from '../../../helpers/WidgetEditorHelpers'
import { useClickOutside } from './useClickOutside'
import ColorPicker from 'primevue/colorpicker'
import descriptor from './WidgetEditorStyleToolbarDescriptor.json'
import WidgetEditorToolbarContextMenu from './WidgetEditorToolbarContextMenu.vue'

export default defineComponent({
    name: 'widget-editor-colo-picker-icon',
    components: { ColorPicker, WidgetEditorToolbarContextMenu },
    props: { option: { type: Object as PropType<any>, required: true }, propModel: { type: Object as PropType<IWidgetStyleToolbarModel | null>, required: true }, disabled: { type: Boolean } },
    emits: ['change', 'openIconPicker'],
    data() {
        return {
            descriptor,
            model: null as IWidgetStyleToolbarModel | null,
            active: false,
            iconPickerDialogVisible: false,
            displayValue: '',
            color: null as { r: number; g: number; b: number } | null,
            newColor: 'rgb(255, 255, 255)',
            colorPickTimer: null as any,
            useClickOutside
        }
    },
    computed: {
        showArowDown() {
            return ['font-size', 'justify-content', 'color', 'background-color', 'font-family'].includes(this.option.type)
        },
        showCircleIcon() {
            return ['color', 'background-color'].includes(this.option.type)
        }
    },
    setup() {
        const knowageStyleIcon = ref(null)
        let colorPickerVisible = ref(false)
        let contextMenuVisible = ref(false)
        useClickOutside(knowageStyleIcon, () => {
            colorPickerVisible.value = false
            contextMenuVisible.value = false
        })
        return { colorPickerVisible, contextMenuVisible, knowageStyleIcon }
    },
    created() {
        this.setEventListeners()
        this.loadModel()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('toolbarIconContextMenuOpened', this.onIconMenuColorPickerOpened)
            emitter.on('toolbarIconColorPickerOpened', this.onIconMenuColorPickerOpened)
        },
        removeEventListeners() {
            emitter.off('toolbarIconContextMenuOpened', this.onIconMenuColorPickerOpened)
            emitter.off('toolbarIconColorPickerOpened', this.onIconMenuColorPickerOpened)
        },
        onIconMenuColorPickerOpened(event: any) {
            this.closePopups(event)
        },
        loadModel() {
            this.model = this.propModel
            if (!this.model) return
            switch (this.option.type) {
                case 'font-weight':
                    this.active = this.model['font-weight'] === 'bold'
                    break
                case 'font-style':
                    this.active = this.model['font-style'] === 'italic'
                    break
                case 'font-size':
                    this.displayValue = this.model['font-size'] ?? ''
                    break
                case 'color':
                    this.color = this.model.color ? getRGBColorFromString(this.model.color) : null
                    this.newColor = this.model.color ?? ''
                    break
                case 'background-color':
                    this.color = this.model['background-color'] ? getRGBColorFromString(this.model['background-color']) : null
                    this.newColor = this.model['background-color'] ?? ''
            }
        },
        getIconClass() {
            return this.option.type ? descriptor.icons[this.option.type] : ''
        },
        getDefaultTooltip() {
            return this.option.type && descriptor.tooltips[this.option.type] ? this.$t(descriptor.tooltips[this.option.type]) : ''
        },
        onColorPickerChange(event: any) {
            if (this.colorPickTimer) {
                clearTimeout(this.colorPickTimer)
                this.colorPickTimer = null
            }
            this.colorPickTimer = setTimeout(() => {
                if (!event.value || !this.model) return
                this.newColor = `rgb(${event.value.r}, ${event.value.g}, ${event.value.b})`
                this.option.type === 'color' ? (this.model.color = this.newColor) : (this.model['background-color'] = this.newColor)
                this.$emit('change')
            }, 200)
        },
        onIconClicked() {
            if (!this.model || this.disabled) return

            switch (this.option.type) {
                case 'font-weight':
                    this.active = !this.active
                    this.model['font-weight'] = this.active ? 'bold' : 'normal'
                    this.$emit('change')
                    break
                case 'font-style':
                    this.active = !this.active
                    this.model['font-style'] = this.active ? 'italic' : ''
                    this.$emit('change')
                    break
                case 'icon':
                    this.$emit('openIconPicker')
            }
        },
        openAdditionalComponents() {
            if (this.disabled) return
            switch (this.option.type) {
                case 'color':
                case 'background-color':
                    this.changeColorPickerVisibility()
                    break
                case 'font-size':
                case 'justify-content':
                case 'font-family':
                    this.changeContextMenuVisibility()
            }
        },
        changeContextMenuVisibility() {
            this.contextMenuVisible = !this.contextMenuVisible
            if (this.contextMenuVisible) {
                emitter.emit('toolbarIconContextMenuOpened', this.option)
            }
        },
        changeColorPickerVisibility() {
            this.colorPickerVisible = !this.colorPickerVisible
            if (this.colorPickerVisible) {
                emitter.emit('toolbarIconColorPickerOpened', this.option)
            }
        },
        closePopups(event: any) {
            this.closeContextMenu(event)
            this.closeColorPicker(event)
        },
        closeContextMenu(option: any) {
            if (this.option.type !== option.type) {
                this.contextMenuVisible = false
            }
        },
        closeColorPicker(option: any) {
            if (this.option.type !== option.type) {
                this.colorPickerVisible = false
            }
        },
        onContextItemSelected(item: string) {
            if (item === 'input') return
            this.contextMenuVisible = false
            this.updateModelAfterContextItemSelected(item)
        },
        updateModelAfterContextItemSelected(item: string) {
            if (!this.model) return
            switch (this.option.type) {
                case 'font-size':
                    this.model['font-size'] = item
                    this.displayValue = item
                    break
                case 'justify-content':
                    this.model['justify-content'] = item
                    break
                case 'font-family':
                    this.model['font-family'] = item
            }
            this.$emit('change')
        },
        onContextInputChanged(item: string) {
            if (!this.model) return
            this.model['font-size'] = item
            this.displayValue = item
            this.$emit('change')
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
