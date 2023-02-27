<template>
    <div class="pivot-widget-container p-d-flex p-d-row kn-flex">
        <DxPivotGrid id="pivotgrid" ref="grid" :data-source="dataSource" :allow-sorting-by-summary="true" :allow-filtering="true" :show-borders="true" :show-column-grand-totals="false" :show-row-grand-totals="false" :show-row-totals="false" :show-column-totals="false" @contentReady="onContentReady">
            <DxFieldChooser :enabled="true" :height="400" />
        </DxPivotGrid>
    </div>
</template>

<script lang="ts">
import { DxPivotGrid, DxFieldChooser } from 'devextreme-vue/pivot-grid'
import { IDashboardDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'

import { sales } from './MockData.js'

export default defineComponent({
    name: 'table-widget',
    components: { DxPivotGrid, DxFieldChooser },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        editorMode: { type: Boolean, required: false },
        datasets: { type: Array as PropType<IDashboardDataset[]>, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['pageChanged', 'sortingChanged', 'launchSelection'],
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    data() {
        return {
            tableData: [] as any,
            dataSource: {
                fields: [
                    {
                        caption: 'Region',
                        width: 120,
                        dataField: 'region',
                        area: 'row',
                        sortBySummaryField: 'Total'
                    },
                    {
                        caption: 'City',
                        dataField: 'city',
                        width: 150,
                        area: 'row'
                    },
                    {
                        dataField: 'date',
                        dataType: 'date',
                        area: 'column'
                    },
                    {
                        groupName: 'date',
                        groupInterval: 'month',
                        visible: false
                    },
                    {
                        caption: 'Total',
                        dataField: 'amount',
                        dataType: 'number',
                        summaryType: 'sum',
                        format: 'currency',
                        area: 'data'
                    }
                ],
                store: sales
            }
        }
    },
    watch: {
        propWidget: {
            handler() {},
            deep: true
        },
        dataToShow: {
            handler() {
                this.tableData = this.dataToShow
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    beforeMount() {},
    created() {},
    unmounted() {},
    mounted() {},

    methods: {
        onContentReady() {
            console.log('CONTENT READY', this.dataSource.fields)
        }
    }
})
</script>
<style lang="scss">
.pivot-widget-container {
    overflow: auto;
}
</style>
