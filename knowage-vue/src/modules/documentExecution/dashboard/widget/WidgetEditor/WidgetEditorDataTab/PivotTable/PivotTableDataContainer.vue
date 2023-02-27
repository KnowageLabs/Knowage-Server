<template>
    <div v-if="widgetModel">
        TEST
        <!-- <WidgetEditorColumnTable
            v-if="chartType === 'pie'"
            class="p-m-2"
            :widgetModel="widgetModel"
            :items="columnTableItems['ATTRIBUTES'] ?? []"
            :settings="{ ...commonDescriptor.columnTableSettings, ...highchartDescriptor.pieChartcolumnTableSettings[0] }"
            :chartType="chartType"
            @rowReorder="onColumnsReorder($event, 'ATTRIBUTES')"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <WidgetEditorColumnTable
            class="p-m-2"
            :widgetModel="widgetModel"
            :items="columnTableItems['MEASURES'] ?? []"
            :settings="valuesColumnSettings"
            :chartType="chartType"
            @rowReorder="onColumnsReorder($event, 'MEASURES')"
            @itemAdded="onColumnAdded"
            @itemUpdated="onColumnItemUpdate"
            @itemSelected="setSelectedColumn"
            @itemDeleted="onColumnDelete"
        ></WidgetEditorColumnTable>
        <ChartWidgetColumnForm class="p-m-2" :widgetModel="widgetModel" :selectedColumn="selectedColumn"></ChartWidgetColumnForm> -->
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './PivotTableDataContainerDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorColumnTable from '../../common/WidgetEditorColumnTable.vue'
import WidgetEditorFilterForm from '../../common/WidgetEditorFilterForm.vue'
import ChartWidgetColumnForm from '../common/ChartWidgetColumnForm.vue'

export default defineComponent({
    name: 'highcharts-widget-common-data-container',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            columnTableItems: {} as any,
            selectedColumn: null as IWidgetColumn | null
        }
    },
    computed: {},
    watch: {
        selectedDataset() {
            this.selectedColumn = null
        }
    },
    async created() {},
    methods: {}
})
</script>
