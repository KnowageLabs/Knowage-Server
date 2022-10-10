<template>
    <div v-show="widgetModel">
        <Accordion v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <div>
                        <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                        <Button icon="fas fa-square-check" class="p-button-rounded p-button-text p-button-plain" @click="logWidget" />
                    </div>
                </template>
                <SelectorWidgetType v-if="accordion.type === 'SelectorType'" :widgetModel="widgetModel"></SelectorWidgetType>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './SelectorWidgetSettingsDescriptor.json'
import SelectorWidgetType from './configuration/SelectorWidgetType.vue'

export default defineComponent({
    name: 'selector-widget-settings-container',
    components: {
        Accordion,
        AccordionTab,
        SelectorWidgetType
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
            activeIndex: -1
        }
    },
    created() {},
    methods: {
        logWidget() {
            console.log('widget ----------------- \n', this.widgetModel)
        }
    }
})
</script>

<style lang="scss">
// .widget-editor-accordion {
//     ::v-deep(.p-accordion-tab-active) {
//         margin: 0;
//     }
// }

.p-accordion-content {
    padding: 0 !important;
}

.dynamic-form-item {
    border-bottom: 1px solid #c2c2c2;
}

.dynamic-form-item:last-child {
    border-bottom: none;
}
</style>
