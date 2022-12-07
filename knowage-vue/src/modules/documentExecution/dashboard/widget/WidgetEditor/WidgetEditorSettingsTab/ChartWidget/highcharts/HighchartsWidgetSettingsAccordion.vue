<template>
    <div v-show="widgetModel">
        <Accordion class="widget-editor-accordion" v-model:activeIndex="activeIndex">
            <AccordionTab v-for="(accordion, index) in settings" :key="index">
                <template #header>
                    <div>
                        <label class="kn-material-input-label">{{ $t(accordion.title) }}</label>
                    </div>
                </template>

                <Highcharts3DConfiguration v-if="accordion.type === 'ConfigurationOf3D'" :widgetModel="widgetModel"></Highcharts3DConfiguration>
                <HighchartsNoDataMessageConfiguration v-else-if="accordion.type === 'NoDataMessageConfiguration'" :widgetModel="widgetModel"></HighchartsNoDataMessageConfiguration>
                <HighchartsAccessibilitySettings v-else-if="accordion.type === 'AccessibilitySettings'" :widgetModel="widgetModel"></HighchartsAccessibilitySettings>
                <HighchartsLabelsSettings v-else-if="accordion.type === 'Labels'" :widgetModel="widgetModel"></HighchartsLabelsSettings>
                <HighchartsLegendSettings v-else-if="accordion.type === 'Legend'" :widgetModel="widgetModel"></HighchartsLegendSettings>
                <HighchartsSeriesAccessibilitySettings v-else-if="accordion.type === 'SeriesAccessibilitySettings'" :widgetModel="widgetModel"></HighchartsSeriesAccessibilitySettings>
                <WidgetExport v-else-if="accordion.type === 'Export'" :widgetModel="widgetModel"></WidgetExport>
                <WidgetTitleStyle v-else-if="accordion.type === 'Title'" :widgetModel="widgetModel" :toolbarStyleSettings="settingsTabDescriptor.defaultToolbarStyleOptions"></WidgetTitleStyle>
                <WidgetRowsStyle v-else-if="accordion.type === 'RowsStyle'" :widgetModel="widgetModel"></WidgetRowsStyle>
                <WidgetBackgroundColorStyle v-else-if="accordion.type === 'BackgroundColorStyle'" :widgetModel="widgetModel"></WidgetBackgroundColorStyle>
                <WidgetBordersStyle v-else-if="accordion.type === 'BordersStyle'" :widgetModel="widgetModel"></WidgetBordersStyle>
                <WidgetPaddingStyle v-else-if="accordion.type === 'PaddingStyle'" :widgetModel="widgetModel"></WidgetPaddingStyle>
                <WidgetShadowsStyle v-else-if="accordion.type === 'ShadowsStyle'" :widgetModel="widgetModel"></WidgetShadowsStyle>
                <WidgetResponsive v-else-if="accordion.type === 'Responsive'" :widgetModel="widgetModel"></WidgetResponsive>
                <WidgetCrossNavigation v-else-if="accordion.type === 'CrossNavigation'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :dashboardId="dashboardId"></WidgetCrossNavigation>
                <WidgetInteractionsLinks v-else-if="accordion.type === 'Link'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers"></WidgetInteractionsLinks>
                <WidgetPreview v-else-if="accordion.type === 'Preview'" :widgetModel="widgetModel" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :dashboardId="dashboardId"></WidgetPreview>
            </AccordionTab>
        </Accordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import descriptor from './HighchartsWidgetSettingsDescriptor.json'
import settingsTabDescriptor from '../../WidgetEditorSettingsTabDescriptor.json'
import WidgetExport from '../../common/configuration/WidgetExport.vue'
import WidgetRowsStyle from '../../common/style/WidgetRowsStyle.vue'
import WidgetBordersStyle from '../../common/style/WidgetBordersStyle.vue'
import WidgetShadowsStyle from '../../common/style/WidgetShadowsStyle.vue'
import WidgetResponsive from '../../common/responsive/WidgetResponsive.vue'
import WidgetCrossNavigation from '../../common/interactions/crossNavigation/WidgetCrossNavigation.vue'
import WidgetInteractionsLinks from '../../common/interactions/link/WidgetInteractionsLinks.vue'
import WidgetPreview from '../../common/interactions/preview/WidgetPreview.vue'
import WidgetTitleStyle from '../../common/style/WidgetTitleStyle.vue'
import WidgetPaddingStyle from '../../common/style/WidgetPaddingStyle.vue'
import WidgetBackgroundColorStyle from '../../common/style/WidgetBackgroundColorStyle.vue'
import Highcharts3DConfiguration from '../highcharts/configuration/Highcharts3DConfiguration.vue'
import HighchartsNoDataMessageConfiguration from '../highcharts/configuration/HighchartsNoDataMessageConfiguration.vue'
import HighchartsAccessibilitySettings from '../highcharts/accessibility/HighchartsAccessibilitySettings.vue'
import HighchartsSeriesAccessibilitySettings from '../highcharts/accessibility/HighchartsSeriesAccessibilitySettings.vue'
import HighchartsLabelsSettings from '../highcharts/labels/HighchartsLabelsSettings.vue'
import HighchartsLegendSettings from '../highcharts/legend/HighchartsLegendSettings.vue'

export default defineComponent({
    name: 'hihgcharts-widget-configuration-container',
    components: {
        Accordion,
        AccordionTab,
        WidgetExport,
        WidgetTitleStyle,
        WidgetRowsStyle,
        WidgetBordersStyle,
        WidgetShadowsStyle,
        WidgetResponsive,
        WidgetPaddingStyle,
        WidgetBackgroundColorStyle,
        WidgetCrossNavigation,
        WidgetInteractionsLinks,
        WidgetPreview,
        Highcharts3DConfiguration,
        HighchartsNoDataMessageConfiguration,
        HighchartsAccessibilitySettings,
        HighchartsSeriesAccessibilitySettings,
        HighchartsLabelsSettings,
        HighchartsLegendSettings
    },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        settings: { type: Array as PropType<{ title: string; type: string }[]> },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array as PropType<IDashboardDriver[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true }
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
            settingsTabDescriptor,
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
