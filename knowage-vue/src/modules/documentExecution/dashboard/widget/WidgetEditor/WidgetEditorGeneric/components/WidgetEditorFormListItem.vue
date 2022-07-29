<template>
    <div>
        <div v-for="(container, containerIndex) in settings.containers" :key="containerIndex">
            <div :class="container.cssClasses">
                <div v-for="(component, index) in container.components" :key="index" :class="component.cssClasess">
                    <WidgetEditorInputText
                        v-if="component.type === 'inputText' && fieldIsVisible(component)"
                        :widgetModel="widgetModel"
                        :property="''"
                        :label="component.label"
                        :class="component.cssClass"
                        :settings="component.settings"
                        :initialValue="item[component.property]"
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
                        @change="onChange($event, component)"
                    ></WidgetEditorDropdown>
                    <WidgetEditorStyleTooblar v-else-if="component.type === 'styleToolbar'" :widgetModel="widgetModel" :icons="component.icons" :settings="component.settings" :item="item" :itemIndex="itemIndex"></WidgetEditorStyleTooblar>
                    <i v-if="component.type === 'addDeleteIcon'" :class="itemIndex === 0 ? 'pi pi-plus-circle' : 'pi pi-trash'" class="kn-cursor-pointer p-ml-2" @click="$emit('addNewItem', itemIndex)"></i>
                </div>
            </div>
        </div>
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
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Object, required: true }, propItem: { type: Object }, itemIndex: { type: Number } },
    emits: ['change', 'addNewItem'],
    data() {
        return {
            item: null as any
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
        }
    }
})
</script>
