<template>
    <div v-if="model">
        <Card v-for="(card, index) in descriptor[model.type]" :key="index">
            <template #content>
                <div v-if="showCardContent(card)">
                    <span> {{ card.title ? $t(card.title) : '' }}</span>
                    <div :class="card.cardClasses">
                        <template v-for="(container, containerIndex) in card.containers" :key="containerIndex">
                            <div :class="container.containerClasess">
                                <template v-for="(component, componentIndex) in container.components" :key="componentIndex">
                                    <WidgetEditorInputText
                                        v-if="component.type === 'inputText'"
                                        :widgetModel="widgetModel"
                                        :property="component.property"
                                        :label="component.label"
                                        :class="component.cssClass"
                                        :disabled="isDisabled(component)"
                                        :settings="component.settings"
                                        @input="onInputTextInput($event, component)"
                                        @change="onInputTextChange($event, component)"
                                    ></WidgetEditorInputText>
                                    <WidgetEditorInputSwitch
                                        v-else-if="component.type === 'inputSwitch'"
                                        :widgetModel="widgetModel"
                                        :property="component.property"
                                        :class="component.cssClass"
                                        :inputClass="component.inputClass"
                                        :label="component.label"
                                        :settings="component.settings"
                                        @change="onInputSwitchChange($event, component)"
                                    ></WidgetEditorInputSwitch>
                                    <WidgetEditorDropdown
                                        v-else-if="component.type === 'dropdown' && fieldIsVisible(component)"
                                        :widgetModel="widgetModel"
                                        :class="component.cssClass"
                                        :label="component.label"
                                        :property="component.property"
                                        :options="getDropdownOptions(component)"
                                        :settings="component.settings"
                                        @change="onDropdownChange($event, component)"
                                    ></WidgetEditorDropdown>
                                    <WidgetEditorAccordion
                                        v-else-if="component.type === 'accordion'"
                                        :widgetModel="widgetModel"
                                        :settings="component.settings"
                                        :accordions="component.accordions"
                                        @accordionInputSwitchChanged="onInputSwitchChange($event.value, $event.component)"
                                        @accordionDropdownChanged="onDropdownChange($event.value, $event.component)"
                                    ></WidgetEditorAccordion>
                                    <WidgetEditorDataTable
                                        v-else-if="component.type === 'dataTable'"
                                        :widgetModel="widgetModel"
                                        :items="getItems(component.settings.property)"
                                        :columns="component.columns"
                                        :settings="component.settings"
                                        @rowReorder="onRowReorder($event, component)"
                                    ></WidgetEditorDataTable>
                                </template>
                            </div>
                        </template>
                    </div>
                </div>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import { getModelProperty } from './WidgetEditorGenericHelper'
import Card from 'primevue/card'
import descriptor from './WidgetEditorGenericDescriptor.json'
import WidgetEditorInputSwitch from './components/WidgetEditorInputSwitch.vue'
import WidgetEditorInputText from './components/WidgetEditorInputText.vue'
import WidgetEditorDataTable from './components/WidgetEditorDataTable.vue'
import WidgetEditorDropdown from './components/WidgetEditorDropdown.vue'
import WidgetEditorAccordion from './components/WidgetEditorAccordion.vue'

export default defineComponent({
    name: 'widget-editor-generic',
    components: { Card, WidgetEditorInputSwitch, WidgetEditorInputText, WidgetEditorDataTable, WidgetEditorDropdown, WidgetEditorAccordion },
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
            console.log(' >>>>> onInputTextInput')
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onInputTextChange(value: string, component: any) {
            console.log(' >>>>> onInputTextChange')
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onInputSwitchChange(value: string, component: any) {
            console.log(' >>>>> onInputSwitchChange')
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onDropdownChange(value: string, component: any) {
            console.log(' >>>>> onDropdownChange')
            if (component.property) this.updateModelProperty(value, component.property)
            if (component.settings.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, component.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel)
            }
        },
        onRowReorder(value: any[], component: any) {
            console.log(' >>>>> onRowReorder')
            if (component.property) this.updateModelProperty(value, component.property)
        },
        updateModelProperty(value: any, propertyPath: string) {
            console.log(' >>>>> updateModelProperty')
            getModelProperty(this.widgetModel, propertyPath, 'updateValue', value)
            console.log('UPDATED MODEL: ', this.model)
        },
        isDisabled(component: any) {
            console.log(' >>>>> isDisabled')
            if (component.isDisabled === undefined) return false
            return getModelProperty(this.widgetModel, component.disabled, 'getValue', null)()
        },
        getItems(propertyPath: string): any[] {
            console.log(' >>>>> getItems')
            return getModelProperty(this.widgetModel, propertyPath, 'getValue', null)
        },
        getDropdownOptions(component: any) {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, component.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction()
            return temp
        },
        fieldIsVisible(component: any) {
            console.log(' >>>>>> fieldIsVisible 1')
            if (!component.visibilityCondition) return true
            const tempFunction = getModelProperty(this.widgetModel, component.visibilityCondition, 'getValue', null)
            console.log(' >>>>>> fieldIsVisible 2', tempFunction(this.widgetModel))
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel)
        },
        showCardContent(card: any) {
            console.log(' >>>>> showCardContent')
            if (!card.visibilityCondition) return true
            const tempFunction = getModelProperty(this.widgetModel, card.visibilityCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel)
        }
    }
})
</script>
