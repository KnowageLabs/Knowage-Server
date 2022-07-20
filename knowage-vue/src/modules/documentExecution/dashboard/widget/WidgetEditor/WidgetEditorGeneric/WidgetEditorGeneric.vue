<template>
    <div v-if="model">
        <div v-for="(row, index) in descriptor[model.type]" :key="index" :class="row.cssClasses">
            <template v-for="(component, tempIndex) in row.components" :key="tempIndex">
                <WidgetEditorInputText v-if="component.type === 'inputText'" :label="component.label" :class="component.cssClass" :disabled="isDisabled(component)" @input="onInputTextInput($event, component)" @change="onInputTextChange($event, component)"></WidgetEditorInputText>
                <WidgetEditorInputSwitch v-else-if="component.type === 'inputSwitch'" :class="component.cssClass" :inputClass="component.inputClass" :label="component.label" @change="onInputSwitchChange($event, component)"></WidgetEditorInputSwitch>
                <WidgetEditorDataTable v-else-if="component.type === 'dataTable'" :widgetModel="widgetModel" :items="getItems(component.property)" :columns="component.columns" :settings="component.settings" @rowReorder="onRowReorder($event, component)"></WidgetEditorDataTable>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import descriptor from './WidgetEditorGenericDescriptor.json'
import WidgetEditorInputSwitch from './components/WidgetEditorInputSwitch.vue'
import WidgetEditorInputText from './components/WidgetEditorInputText.vue'
import WidgetEditorDataTable from './components/WidgetEditorDataTable.vue'

export default defineComponent({
    name: 'widget-editor-generic',
    components: { WidgetEditorInputSwitch, WidgetEditorInputText, WidgetEditorDataTable },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IWidget | null
        }
    },
    watch: {
        widgetModel() {
            this.loadModel()
        }
    },
    async created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel
            console.log('LOADED MODEL: ', this.model)
        },
        onInputTextInput(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onInputTextChange(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onInputSwitchChange(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onRowReorder(value: any[], component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        updateModelProperty(value: any, propertyPath: string) {
            this.getModelProperty(propertyPath, 'updateValue', value)

            console.log('UPDATED MODEL: ', this.model)
        },
        isDisabled(component: any) {
            console.log('TEEEEEEEEEST: ', this.getModelProperty(component.disabled, 'callFunction', null))
            return this.getModelProperty(component.disabled, 'callFunction', null)
        },
        getItems(propertyPath: string): any[] {
            return this.getModelProperty(propertyPath, 'getValue', null)
        },
        getModelProperty(propertyPath: string, action: string, newValue: any) {
            if (!this.model) return
            const stack = propertyPath?.split('.')
            if (!stack || stack.length === 0) return

            let property = null as any
            let tempModel = this.model
            while (stack.length > 1) {
                property = stack.shift()
                if (property && this.model) tempModel = tempModel[property]
            }
            property = stack.shift()
            if (action === 'updateValue') tempModel[property] = newValue
            else if (action === 'callFunction') return tempModel[property]()
            else if (action === 'getValue') return tempModel[property]
        }
    }
})
</script>
