<template>
    <Accordion :multiple="settings.multiple" :activeIndex="settings.activeIndex ?? []">
        <AccordionTab v-for="(accordion, index) in accordions" :key="index">
            <template #header>
                <div>
                    <i v-if="accordion.icon" :class="accordion.icon" class="p-mr-2"></i>
                    <span>{{ accordion.title ? $t(accordion.title) : '' }}</span>
                </div>
            </template>

            <div :class="accordion.cssClasses">
                <template v-for="(container, containerIndex) in accordion.containers" :key="containerIndex">
                    <div :class="container.cssClasses">
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

export default defineComponent({
    name: 'widget-editor-accordion',
    components: { Accordion, AccordionTab, WidgetEditorInputSwitch, WidgetEditorDropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Object, required: true }, accordions: { type: Array as PropType<any[]>, requried: true } },
    emits: ['accordionInputSwitchChanged', 'accordionDropdownChanged'],
    data() {
        return {}
    },
    async created() {},
    methods: {
        getDropdownOptions(component: any) {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, component.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction()
            return temp
        }
    }
})
</script>
