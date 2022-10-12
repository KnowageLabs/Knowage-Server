<template>
    <div v-show="widgetModel">
        <Accordion class="selectorAccordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                </template>
                {{ 'test' }}

                <!-- <SelectorWidgetType v-if="accordion.type === 'SelectorType'" :widgetModel="widgetModel"></SelectorWidgetType> -->
                <WidgetHeaders v-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetHeaders>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './SelectionsWidgetSettingsDescriptor.json'
import settingsTabDescriptor from '../WidgetEditorSettingsTabDescriptor.json'
import SelectorWidgetType from '../SelectorWidget/configuration/SelectorWidgetType.vue'
import WidgetHeaders from '../common/style/WidgetHeaders.vue'

export default defineComponent({
    name: 'selections-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        SelectorWidgetType,
        WidgetHeaders
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        variables: { type: Array as PropType<IVariable[]> }
    },
    watch: {
        settings() {
            this.activeIndex = -1
        }
    },
    data() {
        return {
            descriptor,
            settingsTabDescriptor,
            activeIndex: -1
        }
    },
    created() {},
    methods: {}
})
</script>

<style lang="scss">
.selectorAccordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
    .p-accordion-content {
        display: flex;
    }
}
</style>
