<template>
    <div v-show="widgetModel">
        <Accordion class="widget-editor-accordion">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <div>
                        <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                    </div>
                </template>
                <TableWidgetRows v-show="accordion.type === 'Rows'" :widgetModel="widgetModel"></TableWidgetRows>
                <TableWidgetSummaryRows v-show="accordion.type === 'SummaryRows'" :widgetModel="widgetModel"></TableWidgetSummaryRows>
                <TableWidgetHeader v-show="accordion.type === 'Header'" :widgetModel="widgetModel" :drivers="drivers" :variables="variables"></TableWidgetHeader>
                <TableWidgetColumnGroups v-show="accordion.type === 'ColumnGroups'" :widgetModel="widgetModel"></TableWidgetColumnGroups>
                <TableWidgetExport v-show="accordion.type === 'Export'" :widgetModel="widgetModel"></TableWidgetExport>
                <TableWidgetVisualizationType v-show="accordion.type === 'VisualizationType'" :widgetModel="widgetModel"></TableWidgetVisualizationType>
                <TableWidgetVisibilityConditions v-show="accordion.type === 'VisibilityConditions'" :widgetModel="widgetModel" :variables="variables"></TableWidgetVisibilityConditions>
                <TableWidgetHeaders v-show="accordion.type === 'Headers'" :widgetModel="widgetModel"></TableWidgetHeaders>
                <TableWidgetColumnStyle v-show="accordion.type === 'ColumnStyle'" :widgetModel="widgetModel"></TableWidgetColumnStyle>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './TableWidgetSettingsDescriptor.json'
import TableWidgetRows from './configuration/TableWidgetRows.vue'
import TableWidgetSummaryRows from './configuration/TableWidgetSummaryRows.vue'
import TableWidgetHeader from './configuration/TableWidgetHeader.vue'
import TableWidgetColumnGroups from './configuration/TableWidgetColumnGroups.vue'
import TableWidgetExport from './configuration/TableWidgetExport.vue'
import TableWidgetVisualizationType from './visualization/TableWidgetVisualizationType.vue'
import TableWidgetVisibilityConditions from './visualization/TableWidgetVisibilityConditions.vue'
import TableWidgetHeaders from './style/TableWidgetHeaders.vue'
import TableWidgetColumnStyle from './style/TableWidgetColumnStyle.vue'

export default defineComponent({
    name: 'table-widget-configuration-container',
    components: { Accordion, AccordionTab, TableWidgetRows, TableWidgetSummaryRows, TableWidgetHeader, TableWidgetColumnGroups, TableWidgetExport, TableWidgetVisualizationType, TableWidgetVisibilityConditions, TableWidgetHeaders, TableWidgetColumnStyle },
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
