<template>
    <div v-if="model">
        <div v-for="(card, index) in descriptor[model.type]" :key="index">
            <div v-if="showCardContent(card)">
                <span> {{ card.title ? $t(card.title) : '' }}</span>
                <div :class="card.cardClasses">
                    <template v-for="(container, containerIndex) in card.containers" :key="containerIndex">
                        <div :class="container.containerClasess" :style="container.containerStyle">
                            <template v-for="(component, componentIndex) in container.components" :key="componentIndex">
                                <WidgetEditorInputText
                                    v-if="component.type === 'inputText'"
                                    :widgetModel="widgetModel"
                                    :property="component.property"
                                    :label="component.label"
                                    :class="component.cssClass"
                                    :settings="component.settings"
                                    @input="onInputTextInput($event, component)"
                                    @change="onInputTextChange($event, component)"
                                    data-test="widget-editor-input-text"
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
                                    data-test="widget-editor-input-switch"
                                ></WidgetEditorInputSwitch>
                                <WidgetEditorDropdown
                                    v-else-if="component.type === 'dropdown'"
                                    :widgetModel="widgetModel"
                                    :class="component.cssClass"
                                    :label="component.label"
                                    :property="component.property"
                                    :options="getDropdownOptions(component)"
                                    :settings="component.settings"
                                    @change="onDropdownChange($event, component)"
                                    data-test="widget-editor-dropdown"
                                ></WidgetEditorDropdown>
                                <WidgetEditorCheckbox
                                    v-else-if="component.type === 'checkbox'"
                                    :widgetModel="widgetModel"
                                    :class="component.cssClass"
                                    :label="component.label"
                                    :property="component.property"
                                    :settings="component.settings"
                                    @change="onCheckboxChanged($event, component)"
                                    data-test="widget-editor-input-checkbox"
                                ></WidgetEditorCheckbox>
                                <WidgetEditorAccordion
                                    v-else-if="component.type === 'accordion'"
                                    :widgetModel="widgetModel"
                                    :settings="component.settings"
                                    :accordions="component.accordions"
                                    @accordionInputSwitchChanged="onInputSwitchChange($event.value, $event.component)"
                                    @accordionDropdownChanged="onDropdownChange($event.value, $event.component)"
                                    @accordionInputTextChanged="onInputTextChange($event.value, $event.component)"
                                    @accordionCheckboxChanged="onCheckboxChanged($event.value, $event.component)"
                                    data-test="widget-editor-accordion"
                                ></WidgetEditorAccordion>
                                <WidgetEditorDataTable
                                    v-else-if="component.type === 'dataTable'"
                                    :widgetModel="widgetModel"
                                    :items="getItems(component.settings.property)"
                                    :columns="component.columns"
                                    :settings="component.settings"
                                    @rowReorder="onRowReorder($event, component)"
                                    data-test="widget-editor-data-table"
                                ></WidgetEditorDataTable>
                                <WidgetEditorHintIcon v-else-if="component.type === 'hintIcon'" :class="component.cssClass" :settings="component.settings"></WidgetEditorHintIcon>
                            </template>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import { getModelProperty } from './WidgetEditorGenericHelper'
import { emitter } from '../../../DashboardHelpers'
import WidgetEditorInputSwitch from './components/WidgetEditorInputSwitch.vue'
import WidgetEditorInputText from './components/WidgetEditorInputText.vue'
import WidgetEditorDataTable from './components/WidgetEditorDataTable.vue'
import WidgetEditorDropdown from './components/WidgetEditorDropdown.vue'
import WidgetEditorAccordion from './components/WidgetEditorAccordion.vue'
import WidgetEditorCheckbox from './components/WidgetEditorCheckbox.vue'
import WidgetEditorHintIcon from './components/WidgetEditorHintIcon.vue'

export default defineComponent({
    name: 'widget-editor-generic',
    components: { WidgetEditorInputSwitch, WidgetEditorInputText, WidgetEditorDataTable, WidgetEditorDropdown, WidgetEditorAccordion, WidgetEditorCheckbox, WidgetEditorHintIcon },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, propDescriptor: { type: Object, required: true } },
    data() {
        return {
            descriptor: {},
            model: null as IWidget | null
        }
    },
    watch: {
        propDescriptor() {
            this.loadDescriptor()
        },
        widgetModel() {
            this.loadModel()
        }
    },
    async created() {
        this.loadDescriptor()
        this.loadModel()
    },
    methods: {
        loadDescriptor() {
            this.descriptor = this.propDescriptor as any
        },
        loadModel() {
            this.model = this.widgetModel
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
        onDropdownChange(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onCheckboxChanged(value: string, component: any) {
            if (component.property) this.updateModelProperty(value, component.property)
        },
        onRowReorder(value: any[], component: any) {
            if (component.settings.property) {
                this.updateModelProperty(value, component.settings.property)
                emitter.emit('columnsReordered')
            }
        },
        updateModelProperty(value: any, propertyPath: string) {
            getModelProperty(this.widgetModel, propertyPath, 'updateValue', value)
        },
        getItems(propertyPath: string): any[] {
            return getModelProperty(this.widgetModel, propertyPath, 'getValue', null)
        },
        getDropdownOptions(component: any) {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, component.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction(this.widgetModel)
            return temp
        },
        showCardContent(card: any) {
            if (!card.visibilityCondition) return true
            const tempFunction = getModelProperty(this.widgetModel, card.visibilityCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(this.widgetModel)
        }
    }
})
</script>

<style lang="scss" scoped>
.dropzone-active {
    border: 1.5px blue dotted;
    padding: 0.5rem;
}

.table-headers-hidden {
    ::v-deep(.p-datatable-header) {
        display: none;
    }
}

#drag-columns-hint {
    min-height: 200px;
    min-width: 200px;
}
</style>
