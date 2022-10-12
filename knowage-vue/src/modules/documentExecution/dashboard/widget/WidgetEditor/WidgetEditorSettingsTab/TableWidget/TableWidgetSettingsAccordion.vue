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
                <WidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></WidgetExport>
                <TableWidgetCustomMessages v-else-if="accordion.type === 'CustomMessages'" :widgetModel="widgetModel"></TableWidgetCustomMessages>
                <TableWidgetVisualizationType v-else-if="accordion.type === 'VisualizationType'" :widgetModel="widgetModel"></TableWidgetVisualizationType>
                <TableWidgetVisibilityConditions v-else-if="accordion.type === 'VisibilityConditions'" :widgetModel="widgetModel" :variables="variables"></TableWidgetVisibilityConditions>
                <WidgetHeaders v-else-if="accordion.type === 'Headers'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetHeaders>
                <TableWidgetColumnStyle v-else-if="accordion.type === 'ColumnStyle'" :widgetModel="widgetModel"></TableWidgetColumnStyle>
                <TableWidgetColumnStyle v-else-if="accordion.type === 'ColumnGroupsStyle'" :widgetModel="widgetModel" mode="columnGroups"></TableWidgetColumnStyle>
                <WidgetRowsStyle v-else-if="accordion.type === 'RowsStyle'" :widgetModel="widgetModel"></WidgetRowsStyle>
                <TableWidgetSummaryStyle v-else-if="accordion.type === 'SummaryStyle'" :widgetModel="widgetModel"></TableWidgetSummaryStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
                <TableWidgetConditions v-else-if="accordion.type === 'Conditions'" :widgetModel="widgetModel" :drivers="drivers" :variables="variables"></TableWidgetConditions>
                <TableWidgetTooltips v-else-if="accordion.type === 'Tooltips'" :widgetModel="widgetModel"></TableWidgetTooltips>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></WidgetResponsive>
                <TableWidgetSelection v-else-if="accordion.type === 'Selection'" :widgetModel="widgetModel"></TableWidgetSelection>
                <TableWidgetCrossNavigation v-else-if="accordion.type === 'CrossNavigation'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets"></TableWidgetCrossNavigation>
                <TableWidgetInteractionsLinks v-else-if="accordion.type === 'Link'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers"></TableWidgetInteractionsLinks>
                <TableWidgetPreview v-else-if="accordion.type === 'Preview'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :dashboardId="dashboardId"></TableWidgetPreview>
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
import settingsTabDescriptor from '../WidgetEditorSettingsTabDescriptor.json'
import TableWidgetRows from './configuration/TableWidgetRows.vue'
import TableWidgetSummaryRows from './configuration/TableWidgetSummaryRows.vue'
import TableWidgetHeader from './configuration/TableWidgetHeader.vue'
import TableWidgetColumnGroups from './configuration/TableWidgetColumnGroups.vue'
import WidgetExport from '../common/configuration/WidgetExport.vue'
import TableWidgetCustomMessages from './configuration/TableWidgetCustomMessages.vue'
import TableWidgetVisualizationType from './visualization/TableWidgetVisualizationType.vue'
import TableWidgetVisibilityConditions from './visualization/TableWidgetVisibilityConditions.vue'
import TableWidgetColumnStyle from './style/TableWidgetColumnStyle.vue'
import WidgetRowsStyle from '../common/style/WidgetRowsStyle.vue'
import TableWidgetSummaryStyle from './style/TableWidgetSummaryStyle.vue'
import WidgetBordersStyle from '../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../common/style/WidgetShadowsStyle.vue'
import TableWidgetConditions from './conditionalStyle/TableWidgetConditions.vue'
import TableWidgetTooltips from './tooltips/TableWidgetTooltips.vue'
import WidgetResponsive from '../common/responsive/WidgetResponsive.vue'
import TableWidgetSelection from './interactions/selection/TableWidgetSelection.vue'
import TableWidgetCrossNavigation from './interactions/crossNavigation/TableWidgetCrossNavigation.vue'
import TableWidgetInteractionsLinks from './interactions/link/TableWidgetInteractionsLinks.vue'
import TableWidgetPreview from './interactions/preview/TableWidgetPreview.vue'
import WidgetHeaders from '../common/style/WidgetHeaders.vue'
import WidgetPaddingStyle from '../common/style/WidgetPaddingStyle.vue'

export default defineComponent({
    name: 'table-widget-configuration-container',
    components: {
        Accordion,
        AccordionTab,
        TableWidgetRows,
        TableWidgetSummaryRows,
        TableWidgetHeader,
        TableWidgetColumnGroups,
        WidgetExport,
        TableWidgetCustomMessages,
        TableWidgetVisualizationType,
        TableWidgetVisibilityConditions,
        WidgetHeaders,
        TableWidgetColumnStyle,
        WidgetRowsStyle,
        TableWidgetSummaryStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        TableWidgetConditions,
        TableWidgetTooltips,
        WidgetResponsive,
        TableWidgetSelection,
        TableWidgetCrossNavigation,
        TableWidgetInteractionsLinks,
        TableWidgetPreview,
        WidgetPaddingStyle
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        variables: { type: Array as PropType<IVariable[]> },
        dashboardId: { type: String, required: true }
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
