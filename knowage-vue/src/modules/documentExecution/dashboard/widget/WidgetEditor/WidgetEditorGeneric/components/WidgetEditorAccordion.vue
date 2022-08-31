<template>
    <Accordion class="widget-editor-accordion" :multiple="settings.multiple" :activeIndex="settings.activeIndex ?? []">
        <AccordionTab v-for="(accordion, index) in accordions" :key="index">
            <template #header>
                <div>
                    <i v-if="accordion.icon" :class="accordion.icon" class="p-mr-2"></i>
                    <label class="kn-material-input-label">{{ accordion.title ? $t(accordion.title) : '' }}</label>
                </div>
            </template>

            <div :class="accordion.cssClasses" :style="accordion.cssStyle">
                <template v-for="(container, containerIndex) in accordion.containers" :key="containerIndex">
                    <div :class="container.cssClasses" :style="container.cssStyle">
                        <template v-for="(component, componentIndex) in container.components" :key="componentIndex">
                            <WidgetEditorInputSwitch
                                v-if="component.type === 'inputSwitch'"
                                :widgetModel="widgetModel"
                                :property="component.property"
                                :class="component.cssClass"
                                :label="component.label"
                                :settings="component.settings"
                                @change="$emit('accordionInputSwitchChanged', { value: $event, component: component })"
                            ></WidgetEditorInputSwitch>
                            <WidgetEditorInputText
                                v-if="component.type === 'inputText'"
                                :widgetModel="widgetModel"
                                :property="component.property"
                                :label="component.label"
                                :class="component.cssClass"
                                :settings="component.settings"
                                @change="$emit('accordionInputTextChanged', { value: $event, component: component })"
                            ></WidgetEditorInputText>
                            <WidgetEditorDropdown
                                v-else-if="component.type === 'dropdown'"
                                :widgetModel="widgetModel"
                                :class="component.cssClass"
                                :label="component.label"
                                :property="component.property"
                                :options="getDropdownOptions(component)"
                                :settings="component.settings"
                                @change="$emit('accordionDropdownChanged', { value: $event, component: component })"
                            ></WidgetEditorDropdown>
                            <WidgetEditorCheckbox
                                v-else-if="component.type === 'checkbox'"
                                :widgetModel="widgetModel"
                                :class="component.cssClass"
                                :label="component.label"
                                :property="component.property"
                                :settings="component.settings"
                                @change="$emit('accordionCheckboxChanged', { value: $event, component: component })"
                            ></WidgetEditorCheckbox>
                            <WidgetEditorStyleTooblar v-else-if="component.type === 'styleToolbar'" :widgetModel="widgetModel" :icons="component.icons" :settings="component.settings" data-test="widget-editor-style-toolbar"></WidgetEditorStyleTooblar>
                            <WidgetEditorFormList v-else-if="component.type === 'formList'" :widgetModel="widgetModel" :settings="component.settings"></WidgetEditorFormList>
                            <WidgetEditorColorPicker v-else-if="component.type === 'colorPicker'" :widgetModel="widgetModel" :class="component.cssClass" :label="component.label" :property="component.property" :settings="component.settings"></WidgetEditorColorPicker>
                            <WidgetEditorHintIcon v-else-if="component.type === 'hintIcon'" :class="component.cssClass" :settings="component.settings"></WidgetEditorHintIcon>
                            <WidgetEditorMultiselect
                                v-else-if="component.type === 'multiselect'"
                                :widgetModel="widgetModel"
                                :class="component.cssClass"
                                :label="component.label"
                                :property="component.property"
                                :options="getDropdownOptions(component)"
                                :settings="component.settings"
                                @change="$emit('accordionMultiselectChanged', { value: $event, component: component })"
                            ></WidgetEditorMultiselect>
                        </template>
                    </div>
                </template>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import WidgetEditorInputSwitch from './WidgetEditorInputSwitch.vue'
import WidgetEditorDropdown from './WidgetEditorDropdown.vue'
import WidgetEditorInputText from './WidgetEditorInputText.vue'
import WidgetEditorCheckbox from './WidgetEditorCheckbox.vue'
import WidgetEditorStyleTooblar from './WidgetEditorStyleTooblar.vue'
import WidgetEditorFormList from './WidgetEditorFormList.vue'
import WidgetEditorColorPicker from './WidgetEditorColorPicker.vue'
import WidgetEditorHintIcon from './WidgetEditorHintIcon.vue'
import WidgetEditorMultiselect from './WidgetEditorMultiselect.vue'

export default defineComponent({
    name: 'widget-editor-accordion',
    components: { Accordion, AccordionTab, WidgetEditorInputSwitch, WidgetEditorDropdown, WidgetEditorInputText, WidgetEditorCheckbox, WidgetEditorStyleTooblar, WidgetEditorFormList, WidgetEditorColorPicker, WidgetEditorHintIcon, WidgetEditorMultiselect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Object, required: true }, accordions: { type: Array as PropType<any[]>, requried: true } },
    emits: ['accordionInputSwitchChanged', 'accordionDropdownChanged', 'accordionInputTextChanged', 'accordionCheckboxChanged', 'accordionMultiselectChanged'],
    data() {
        return {}
    },
    async created() {},
    methods: {
        getDropdownOptions(component: any) {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, component.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction(this.widgetModel)
            return temp
        }
    }
})
</script>

<style lang="scss" scoped>
.widget-editor-accordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
}
</style>
