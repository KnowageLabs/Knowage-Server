<template>
    <div>
        <div v-show="dropzoneTopVisible" class="form-list-item-dropzone-active" @drop.stop="onDropComplete($event, 'before')" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div v-show="reorderEnabled" class="form-list-item-dropzone" :class="{ 'form-list-item-dropzone-active': dropzoneTopVisible }" @drop.stop="onDropComplete($event, 'before')" @dragover.prevent @dragenter.prevent="displayDropzone('top')" @dragleave.prevent="hideDropzone('top')"></div>
        <div v-for="(container, containerIndex) in settings.containers" :key="containerIndex" :class="{ 'kn-draggable': reorderEnabled }" :draggable="reorderEnabled" @dragstart.stop="onDragStart">
            <div :class="container.cssClasses">
                <div v-for="(component, index) in container.components" :key="index" :class="component.cssClasess">
                    <i v-if="component.type === 'reorderIcon' && reorderEnabled" class="pi pi-th-large kn-cursor-pointer p-mr-2" @click="$emit('addNewItem', itemIndex)"></i>
                    <WidgetEditorInputText
                        v-if="component.type === 'inputText' && fieldIsVisible(component)"
                        :widgetModel="widgetModel"
                        :property="''"
                        :label="component.label"
                        :class="component.cssClass"
                        :settings="component.settings"
                        :initialValue="item[component.property]"
                        :itemIndex="itemIndex"
                        @input="onChange($event, component, index)"
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
                        :itemIndex="itemIndex"
                        @change="onChange($event, component)"
                    ></WidgetEditorDropdown>
                    <WidgetEditorStyleTooblar v-else-if="component.type === 'styleToolbar'" :widgetModel="widgetModel" :icons="component.icons" :settings="component.settings" :item="item" :itemIndex="itemIndex"></WidgetEditorStyleTooblar>
                    <i v-if="component.type === 'addDeleteIcon'" :class="itemIndex === 0 ? 'pi pi-plus-circle' : 'pi pi-trash'" class="kn-cursor-pointer p-ml-2" @click="$emit('addNewItem', itemIndex)"></i>
                </div>
            </div>
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

export default defineComponent({
    name: 'widget-editor-form-list-item',
    components: { WidgetEditorInputText, WidgetEditorDropdown, WidgetEditorStyleTooblar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Object, required: true }, propItem: { type: Object }, itemIndex: { type: Number }, reorderEnabled: { type: Boolean } },
    emits: ['change', 'addNewItem', 'moveRows'],
    data() {
        return {
            item: null as any,
            dropzoneTopVisible: false,
            dropzoneBottomVisible: false
        }
    },
    watch: {
        propItem() {
            this.loadItem()
        }
    },
    created() {
        this.loadItem()
    },
    methods: {
        loadItem() {
            this.item = this.propItem
        },
        onChange(newValue: any, component: any) {
            if (!component) return
            this.item[component.property] = newValue
            this.$emit('change', { item: this.item, component: component })
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
            console.log('ON DRAG DROP: ', event)
            this.hideDropzone('top')
            this.hideDropzone('bottom')
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            console.log('EVENT DATA: ', eventData)
            this.$emit('moveRows', { sourceRowIndex: eventData, targetRowIndex: this.itemIndex, position: position })
        },
        displayDropzone(position: string) {
            if (position === 'top') {
                this.dropzoneTopVisible = true
            } else {
                this.dropzoneBottomVisible = true
            }
            // console.log('displayDropzone', position, this.dropzoneTopVisible)
        },
        hideDropzone(position: string) {
            if (position === 'top') {
                this.dropzoneTopVisible = false
            } else {
                this.dropzoneBottomVisible = false
            }
            // console.log('hideDropzone', position, this.dropzoneTopVisible)
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
</style>
