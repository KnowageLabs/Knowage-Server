<template>
    <div v-if="widgetModel">
        <Accordion class="widget-editor-accordion">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <div>
                        <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                    </div>
                </template>
                <TableWidgetRows v-if="accordion.type === 'Rows'" :widgetModel="widgetModel"></TableWidgetRows>
                <TableWidgetSummaryRows v-else-if="accordion.type === 'SummaryRows'" :widgetModel="widgetModel"></TableWidgetSummaryRows>
                <TableWidgetHeaders v-else-if="accordion.type === 'Headers'" :widgetModel="widgetModel" :drivers="drivers" :variables="variables"></TableWidgetHeaders>
                <TableWidgetColumnGroups v-else-if="accordion.type === 'ColumnGroups'" :widgetModel="widgetModel"></TableWidgetColumnGroups>
                <TableWidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></TableWidgetExport>
                <TableWidgetVisualizationType v-else-if="accordion.type === 'VisualizationType'" :widgetModel="widgetModel"></TableWidgetVisualizationType>
                <TableWidgetVisibilityConditions v-else-if="accordion.type === 'VisibilityConditions'" :widgetModel="widgetModel" :variables="variables"></TableWidgetVisibilityConditions>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './TableWidgetSettingsDescriptor.json'
import TableWidgetRows from './configuration/TableWidgetRows.vue'
import TableWidgetSummaryRows from './configuration/TableWidgetSummaryRows.vue'
import TableWidgetHeaders from './configuration/TableWidgetHeaders.vue'
import TableWidgetColumnGroups from './configuration/TableWidgetColumnGroups.vue'
import TableWidgetExport from './configuration/TableWidgetExport.vue'
import TableWidgetVisualizationType from './visualization/TableWidgetVisualizationType.vue'
import TableWidgetVisibilityConditions from './visualization/TableWidgetVisibilityConditions.vue'

export default defineComponent({
    name: 'table-widget-configuration-container',
    components: { Accordion, AccordionTab, TableWidgetRows, TableWidgetSummaryRows, TableWidgetHeaders, TableWidgetColumnGroups, TableWidgetExport, TableWidgetVisualizationType, TableWidgetVisibilityConditions },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, settings: { type: Array as PropType<{ title: string; type: string }[]> }, drivers: { type: Array }, variables: { type: Array } },
    data() {
        return {
            descriptor
        }
    },
    created() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
.widget-editor-accordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
}
</style>
