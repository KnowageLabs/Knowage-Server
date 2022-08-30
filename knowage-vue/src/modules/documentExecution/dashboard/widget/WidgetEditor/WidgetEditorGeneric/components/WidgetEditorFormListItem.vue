<template>
    <div :class="{ 'kn-draggable': reorderEnabled }">
        <div v-show="dropzoneTopVisible" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before')" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div v-show="reorderEnabled" class="form-list-item-dropzone" :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible }" @drop.stop="onDropComplete($event, 'before')" @dragover.prevent @dragenter.prevent="displayDropzone('top')" @dragleave.prevent="hideDropzone('top')"></div>
        <div v-for="container in settings.containers" :key="cryptoRandomString({ length: 16, type: 'base64' })" :class="container.cssClasses" :draggable="reorderEnabled" @dragstart.stop="onDragStart">
            <template v-for="component in container.components" :key="cryptoRandomString({ length: 16, type: 'base64' })">
                <i v-if="component.type === 'reorderIcon' && reorderEnabled" :class="{ 'icon-disabled': disabled }" class="pi pi-th-large kn-cursor-pointer p-mr-2" @click="$emit('addNewItem', itemIndex)"></i>
                <WidgetEditorInputText
                    v-if="component.type === 'inputText' && fieldIsVisible(component)"
                    :widgetModel="widgetModel"
                    :property="''"
                    :label="component.label"
                    :class="component.cssClass"
                    :settings="component.settings"
                    :initialValue="item[component.property]"
                    :item="item"
                    :itemIndex="itemIndex"
                    @input="onChange($event, component, itemIndex)"
                ></WidgetEditorInputText>
                <WidgetEditorDropdown
                    v-else-if="component.type === 'dropdown' && fieldIsVisible(component)"
                    :widgetModel="widgetModel"
                    :class="component.cssClass"
                    :style="component.style"
                    :label="component.label"
                    :property="''"
                    :options="getDropdownOptions(component)"
                    :settings="component.settings"
                    :initialValue="item[component.property]"
                    :item="item"
                    :itemIndex="itemIndex"
                    @change="onChange($event, component, itemIndex)"
                ></WidgetEditorDropdown>
                <WidgetEditorStyleTooblar v-else-if="component.type === 'styleToolbar'" :widgetModel="widgetModel" :icons="component.icons" :settings="component.settings" :item="item" :itemIndex="itemIndex"></WidgetEditorStyleTooblar>
                <i v-if="component.type === 'addDeleteIcon'" :class="[itemIndex === 0 ? 'pi pi-plus-circle' : 'pi pi-trash', disabled ? 'icon-disabled' : '']" class="kn-cursor-pointer p-ml-2" @click="$emit('addNewItem', itemIndex)"></i>
            </template>
        </div>
        <div v-show="reorderEnabled" class="form-list-item-dropzone" :class="{ 'form-list-item-dropzone-active': dropzoneBottomVisible }" @drop.stop="onDropComplete($event, 'after')" @dragover.prevent @dragenter.prevent="displayDropzone('bottom')" @dragleave.prevent="hideDropzone('bottom')"></div>
        <div v-show="dropzoneBottomVisible" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'after')" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import WidgetEditorInputText from './WidgetEditorInputText.vue'
import WidgetEditorDropdown from './WidgetEditorDropdown.vue'
import WidgetEditorStyleTooblar from './WidgetEditorStyleTooblar.vue'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'widget-editor-form-list-item',
    components: { WidgetEditorInputText, WidgetEditorDropdown, WidgetEditorStyleTooblar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Object, required: true }, propItem: { type: Object }, itemIndex: { type: Number }, reorderEnabled: { type: Boolean } },
    emits: ['change', 'addNewItem', 'moveRows'],
    data() {
        return {
            item: null as any,
            disabled: false,
            dropzoneTopVisible: false,
            dropzoneBottomVisible: false,
            cryptoRandomString
        }
    },
    watch: {
        propItem() {
            this.loadItem()
        }
    },
    created() {
        this.loadItem()
        this.setWatchers()
    },
    methods: {
        loadItem() {
            this.item = this.propItem
            this.itemIsDisabled()
        },
        onChange(newValue: any, component: any) {
            if (!component) return
            this.item[component.property] = newValue
        },
        getDropdownOptions(component: any) {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, component.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction(this.item)
            return temp
        },
        fieldIsVisible(component: any) {
            if (!component.settings.visible) return true
            const tempFunction = getModelProperty(this.widgetModel, component.settings.visible, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel, this.item)
        },
        onDragStart(event: any) {
            if (!this.itemIndex && this.itemIndex !== 0) return
            event.dataTransfer.setData('text/plain', JSON.stringify(this.itemIndex))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        onDropComplete(event: any, position: 'before' | 'after') {
            if (this.disabled) return
            this.hideDropzone('top')
            this.hideDropzone('bottom')
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))

            this.$emit('moveRows', { sourceRowIndex: eventData, targetRowIndex: this.itemIndex, position: position })
        },
        displayDropzone(position: string) {
            if (position === 'top') {
                this.dropzoneTopVisible = true
            } else {
                this.dropzoneBottomVisible = true
            }
            //
        },
        hideDropzone(position: string) {
            if (position === 'top') {
                this.dropzoneTopVisible = false
            } else {
                this.dropzoneBottomVisible = false
            }
            //
        },
        itemIsDisabled() {
            if (!this.settings.disabledCondition) return
            const tempFunction = getModelProperty(this.widgetModel, this.settings.disabledCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return (this.disabled = tempFunction(this.widgetModel, this.itemIndex))
        },
        setWatchers() {
            if (this.settings.watchers) {
                for (let i = 0; i < this.settings.watchers.length; i++) {
                    this.$watch(
                        'widgetModel.' + this.settings.watchers[i],
                        () => {
                            this.itemIsDisabled()
                        },
                        { deep: true }
                    )
                }
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.form-list-item-dropzone {
    height: 20px;
    width: 100%;
    background-color: white;
}

.form-list-item-dropzone-active {
    height: 10px;
    background-color: #aec1d3;
}

.icon-disabled {
    color: #c2c2c2;
}
</style>
