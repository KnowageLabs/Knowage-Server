<template>
    <div v-show="widgetModel">
        <Accordion class="widget-editor-accordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <div>
                        <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                    </div>
                </template>
                <TableWidgetRows v-if="accordion.type === 'Rows'" :widgetModel="widgetModel"></TableWidgetRows>
                <TableWidgetSummaryRows v-else-if="accordion.type === 'SummaryRows'" :widgetModel="widgetModel"></TableWidgetSummaryRows>
                <TableWidgetHeader v-else-if="accordion.type === 'Header'" :widgetModel="widgetModel" :drivers="drivers" :variables="variables"></TableWidgetHeader>
                <TableWidgetColumnGroups v-else-if="accordion.type === 'ColumnGroups'" :widgetModel="widgetModel"></TableWidgetColumnGroups>
                <TableWidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></TableWidgetExport>
                <TableWidgetCustomMessages v-else-if="accordion.type === 'CustomMessages'" :widgetModel="widgetModel"></TableWidgetCustomMessages>
                <TableWidgetVisualizationType v-else-if="accordion.type === 'VisualizationType'" :widgetModel="widgetModel"></TableWidgetVisualizationType>
                <TableWidgetVisibilityConditions v-else-if="accordion.type === 'VisibilityConditions'" :widgetModel="widgetModel" :variables="variables"></TableWidgetVisibilityConditions>
                <TableWidgetHeaders v-else-if="accordion.type === 'Headers'" :widgetModel="widgetModel"></TableWidgetHeaders>
                <TableWidgetColumnStyle v-else-if="accordion.type === 'ColumnStyle'" :widgetModel="widgetModel"></TableWidgetColumnStyle>
                <TableWidgetColumnStyle v-else-if="accordion.type === 'ColumnGroupsStyle'" :widgetModel="widgetModel" mode="columnGroups"></TableWidgetColumnStyle>
                <TableWidgetRowsStyle v-else-if="accordion.type === 'RowsStyle'" :widgetModel="widgetModel"></TableWidgetRowsStyle>
                <TableWidgetSummaryStyle v-else-if="accordion.type === 'SummaryStyle'" :widgetModel="widgetModel"></TableWidgetSummaryStyle>
                <TableWidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></TableWidgetBordersStyle>
                <TableWidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></TableWidgetPaddingStyle>
                <TableWidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></TableWidgetShadowsStyle>
                <TableWidgetConditions v-else-if="accordion.type === 'Conditions'" :widgetModel="widgetModel" :drivers="drivers" :variables="variables"></TableWidgetConditions>
                <TableWidgetTooltips v-else-if="accordion.type === 'Tooltips'" :widgetModel="widgetModel"></TableWidgetTooltips>
                <TableWidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></TableWidgetResponsive>
                <TableWidgetSelection v-else-if="accordion.type === 'Selection'" :widgetModel="widgetModel"></TableWidgetSelection>
                <TableWidgetCrossNavigation v-else-if="accordion.type === 'CrossNavigation'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets"></TableWidgetCrossNavigation>
                <TableWidgetInteractionsLinks v-else-if="accordion.type === 'Link'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers"></TableWidgetInteractionsLinks>
                <TableWidgetPreview v-else-if="accordion.type === 'Preview'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers"></TableWidgetPreview>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './TableWidgetSettingsDescriptor.json'
import TableWidgetRows from './configuration/TableWidgetRows.vue'
import TableWidgetSummaryRows from './configuration/TableWidgetSummaryRows.vue'
import TableWidgetHeader from './configuration/TableWidgetHeader.vue'
import TableWidgetColumnGroups from './configuration/TableWidgetColumnGroups.vue'
import TableWidgetExport from './configuration/TableWidgetExport.vue'
import TableWidgetCustomMessages from './configuration/TableWidgetCustomMessages.vue'
import TableWidgetVisualizationType from './visualization/TableWidgetVisualizationType.vue'
import TableWidgetVisibilityConditions from './visualization/TableWidgetVisibilityConditions.vue'
import TableWidgetHeaders from './style/TableWidgetHeaders.vue'
import TableWidgetColumnStyle from './style/TableWidgetColumnStyle.vue'
import TableWidgetRowsStyle from './style/TableWidgetRowsStyle.vue'
import TableWidgetSummaryStyle from './style/TableWidgetSummaryStyle.vue'
import TableWidgetBordersStyle from './style/TableWidgetBordersStyle.vue'
import TableWidgetPaddingStyle from './style/TableWidgetPaddingStyle.vue'
import TableWidgetShadowsStyle from './style/TableWidgetShadowsStyle.vue'
import TableWidgetConditions from './conditionalStyle/TableWidgetConditions.vue'
import TableWidgetTooltips from './tooltips/TableWidgetTooltips.vue'
import TableWidgetResponsive from './responsive/TableWidgetResponsive.vue'
import TableWidgetSelection from './interactions/selection/TableWidgetSelection.vue'
import TableWidgetCrossNavigation from './interactions/crossNavigation/TableWidgetCrossNavigation.vue'
import TableWidgetInteractionsLinks from './interactions/link/TableWidgetInteractionsLinks.vue'
import TableWidgetPreview from './interactions/preview/TableWidgetPreview.vue'

export default defineComponent({
    name: 'table-widget-configuration-container',
    components: {
        Accordion,
        AccordionTab,
        TableWidgetRows,
        TableWidgetSummaryRows,
        TableWidgetHeader,
        TableWidgetColumnGroups,
        TableWidgetExport,
        TableWidgetCustomMessages,
        TableWidgetVisualizationType,
        TableWidgetVisibilityConditions,
        TableWidgetHeaders,
        TableWidgetColumnStyle,
        TableWidgetRowsStyle,
        TableWidgetSummaryStyle,
        TableWidgetBordersStyle,
        TableWidgetPaddingStyle,
        TableWidgetShadowsStyle,
        TableWidgetConditions,
        TableWidgetTooltips,
        TableWidgetResponsive,
        TableWidgetSelection,
        TableWidgetCrossNavigation,
        TableWidgetInteractionsLinks,
        TableWidgetPreview
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
            this.setActiveAccordion()
        }
    },
    data() {
        return {
            descriptor,
            activeIndex: -1
        }
    },
    created() {
        this.setActiveAccordion()
    },
    methods: {
        setActiveAccordion() {
            if (this.settings?.length === 1) this.activeIndex = 0
        }
    }
})
</script>

<style lang="scss">
.widget-editor-accordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
}

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
