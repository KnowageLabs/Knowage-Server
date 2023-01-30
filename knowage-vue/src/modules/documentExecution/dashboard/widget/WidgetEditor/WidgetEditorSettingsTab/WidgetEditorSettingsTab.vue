<template>
    <WidgetEditorSettingsList v-if="descriptor" :widgetModel="propWidget" :options="descriptor.settingsListOptions" @itemClicked="onItemClicked"></WidgetEditorSettingsList>
    <div v-if="propWidget" class="p-d-flex kn-flex kn-overflow">
        <TableWidgetSettingsContainer
            v-if="propWidget.type === 'table'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
        ></TableWidgetSettingsContainer>
        <SelectorWidgetSettingsContainer
            v-else-if="propWidget.type === 'selector'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
        ></SelectorWidgetSettingsContainer>
        <SelectionsWidgetSettingsContainer
            v-else-if="propWidget.type === 'selection'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
        ></SelectionsWidgetSettingsContainer>
        <HTMLWidgetSettingsContainer
            v-else-if="propWidget.type === 'html'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
            :htmlGalleryProp="htmlGalleryProp"
            @galleryItemSelected="onGalleryItemSelected"
        ></HTMLWidgetSettingsContainer>
        <TextWidgetSettingsContainer
            v-else-if="propWidget.type === 'text'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
        ></TextWidgetSettingsContainer>
        <HighchartsWidgetSettingsContainer
            v-else-if="propWidget.type === 'highcharts'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
            :descriptor="descriptor"
        >
        </HighchartsWidgetSettingsContainer>
        <ChartJSWidgetSettingsContainer
            v-else-if="propWidget.type === 'chartJS'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
        >
        </ChartJSWidgetSettingsContainer>
        <ImageWidgetSettingsContainer
            v-else-if="propWidget.type === 'image'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
            @settingSelected="$emit('settingChanged', $event)"
        >
        </ImageWidgetSettingsContainer>
        <CustomChartWidgetSettingsContainer
            v-else-if="propWidget.type === 'customchart'"
            class="model-div kn-flex kn-overflow p-py-3 p-pr-3"
            :widgetModel="propWidget"
            :selectedSetting="selectedSetting"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
            :customChartGalleryProp="customChartGalleryProp"
        ></CustomChartWidgetSettingsContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IGalleryItem } from '../../../Dashboard'
import tableDescriptor from './TableWidget/TableWidgetSettingsDescriptor.json'
import TableWidgetSettingsContainer from './TableWidget/TableWidgetSettingsContainer.vue'
import SelectorWidgetSettingsContainer from './SelectorWidget/SelectorWidgetSettingsContainer.vue'
import SelectionsWidgetSettingsContainer from './SelectionsWidget/SelectionsWidgetSettingsContainer.vue'
import HTMLWidgetSettingsContainer from './HTMLWidget/HTMLWidgetSettingsContainer.vue'
import TextWidgetSettingsContainer from './TextWidget/TextWidgetSettingsContainer.vue'
import HighchartsWidgetSettingsContainer from './ChartWidget/highcharts/HighchartsWidgetSettingsContainer.vue'
import ChartJSWidgetSettingsContainer from './ChartWidget/chartJS/ChartJSWidgetSettingsContainer.vue'
import ImageWidgetSettingsContainer from './ImageWidget/ImageWidgetSettingsContainer.vue'
import CustomChartWidgetSettingsContainer from './CustomChartWidget/CustomChartWidgetSettingsContainer.vue'
import selectorDescriptor from './SelectorWidget/SelectorWidgetSettingsDescriptor.json'
import selectionsDescriptor from './SelectionsWidget/SelectionsWidgetSettingsDescriptor.json'
import WidgetEditorSettingsList from './WidgetEditorSettingsList.vue'
import htmlDescriptor from './HTMLWidget/HTMLWidgetSettingsDescriptor.json'
import textDescriptor from './TextWidget/TextWidgetSettingsDescriptor.json'
import chartJSDescriptor from './ChartWidget/chartJS/ChartJSWidgetSettingsDescriptor.json'
import HighchartsPieSettingsDescriptor from './ChartWidget/highcharts/descriptors/HighchartsPieSettingsDescriptor.json'
import HighchartsGaugeSettingsDescriptor from './ChartWidget/highcharts/descriptors/HighchartsGaugeSettingsDescriptor.json'
import HighchartsActivityGaugeSettingsDescriptor from './ChartWidget/highcharts/descriptors/HighchartsActivityGaugeSettingsDescriptor.json'
import HighchartsSolidGaugeSettingsDescriptor from './ChartWidget/highcharts/descriptors/HighchartsSolidGaugeSettingsDescriptor.json'
import imageDescriptor from './ImageWidget/ImageWidgetSettingsDescriptor.json'
import customChartDescriptor from './CustomChartWidget/CustomChartWidgetSettingsDescriptor.json'

export default defineComponent({
    name: 'widget-editor-settings-tab',
    components: {
        TableWidgetSettingsContainer,
        WidgetEditorSettingsList,
        SelectorWidgetSettingsContainer,
        SelectionsWidgetSettingsContainer,
        HTMLWidgetSettingsContainer,
        TextWidgetSettingsContainer,
        HighchartsWidgetSettingsContainer,
        ChartJSWidgetSettingsContainer,
        ImageWidgetSettingsContainer,
        CustomChartWidgetSettingsContainer
    },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        htmlGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true },
        customChartGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['settingChanged'],
    data() {
        return {
            descriptor: null as any,
            selectedDescriptor: {},
            selectedSetting: ''
        }
    },
    computed: {
        chartType() {
            return this.propWidget?.settings.chartModel?.model?.chart.type
        }
    },
    watch: {
        chartType() {
            this.loadDescriptor()
        }
    },
    created() {
        this.loadDescriptor()
    },
    methods: {
        loadDescriptor() {
            switch (this.propWidget.type) {
                case 'table':
                    this.descriptor = tableDescriptor
                    break
                case 'selector':
                    this.descriptor = selectorDescriptor
                    break
                case 'selection':
                    this.descriptor = selectionsDescriptor
                    break
                case 'html':
                    this.descriptor = { ...htmlDescriptor }
                    this.checkIfHtmlWidgetGalleryOptionIsDisabled()
                    break
                case 'text':
                    this.descriptor = textDescriptor
                    break
                case 'highcharts':
                    this.descriptor = this.getHighchartsDescriptor()
                    break
                case 'chartJS':
                    this.descriptor = chartJSDescriptor
                    break
                case 'image':
                    this.descriptor = imageDescriptor
                    break
                case 'customchart':
                    this.descriptor = customChartDescriptor
                    break
            }
        },
        getHighchartsDescriptor() {
            switch (this.chartType) {
                case 'pie':
                    return HighchartsPieSettingsDescriptor
                case 'gauge':
                    return HighchartsGaugeSettingsDescriptor
                case 'activitygauge':
                    return HighchartsActivityGaugeSettingsDescriptor
                case 'solidgauge':
                    return HighchartsSolidGaugeSettingsDescriptor
            }
        },
        onItemClicked(item: any) {
            this.selectedSetting = item.value
            this.$emit('settingChanged', item.value)
            this.selectedDescriptor = { table: item.descriptor }
        },
        checkIfHtmlWidgetGalleryOptionIsDisabled() {
            if (this.htmlGalleryProp.length > 0) return
            const index = this.descriptor.settingsListOptions.findIndex((option: any) => option.value === 'Gallery')
            if (index !== -1) this.descriptor.settingsListOptions[index].disabled = true
        },
        onGalleryItemSelected() {
            this.selectedSetting = 'Editor'
            this.$emit('settingChanged', 'Editor')
        }
    }
})
</script>
