<template>
    <KnDashboardTabsPanel label-position="bottom" v-model:sheets="dashboardModel.sheets" @sheet-change="sheetChange">
        <KnDashboardTab v-for="(sheet, index) in dashboardModel.sheets" :key="index" :index="index">
            <grid-layout
                v-model:layout="sheet.widgets['lg']"
                :responsive-layouts="sheet.widgets"
                :responsive="true"
                :cols="{ lg: 50, md: 100, sm: 50, xs: 20, xxs: 10 }"
                :row-height="30"
                :is-draggable="true"
                :is-resizable="true"
                :vertical-compact="false"
                :use-css-transforms="false"
                :margin="[2, 2]"
                @breakpoint-changed="breakpointChangedEvent"
            >
                <WidgetController :activeSheet="activeSheet(index)" :widget="currentWidget(item.id)" :item="item" v-for="item in sheet.widgets['lg']" :key="item.i" :datasets="datasets" :dashboardId="dashboardId" :variables="variables" :model="model"></WidgetController>
            </grid-layout>
        </KnDashboardTab>
    </KnDashboardTabsPanel>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the dashboard visualizazion, specifically to manage responsive structure and sheets.
 */
import { defineComponent, PropType } from 'vue'
import { IDataset, IVariable } from './Dashboard'
import { mapState } from 'pinia'
import WidgetController from './widget/WidgetController.vue'
import KnDashboardTabsPanel from '@/components/UI/KnDashboardTabs/KnDashboardTabsPanel.vue'
import KnDashboardTab from '@/components/UI/KnDashboardTabs/KnDashboardTab.vue'
import dashboardStore from './Dashboard.store'

export default defineComponent({
    name: 'dashboard-manager',
    components: { KnDashboardTab, KnDashboardTabsPanel, WidgetController },
    props: {
        model: { type: Object },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        dashboardId: { type: String, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true }
    },
    inject: ['dHash'],
    data() {
        return {
            dashboardModel: {} as any,
            startingBreakpoint: '' as string
        }
    },
    setup() {
        const store = dashboardStore()
        return { store }
    },
    mounted() {
        this.dashboardModel = this.model
    },
    computed: {
        ...mapState(dashboardStore, {
            dashboard: 'dashboards'
        })
    },
    methods: {
        activeSheet(index) {
            // @ts-ignore
            if ((!this.dashboard[this.dHash] && index === 0) || this.dashboard[this.dHash] === index) return true
            return false
        },
        breakpointChangedEvent: function(newBreakpoint, newLayout) {
            // console.log('BREAKPOINT CHANGED breakpoint=', newBreakpoint, ', layout: ', newLayout)
        },
        currentWidget(id) {
            return this.dashboardModel.widgets.find((item) => item.id === id)
        },
        sheetChange(index) {
            this.store.setSelectedSheetIndex(index)
            this.store.setDashboardSheet({ id: (this as any).dHash as any, sheet: index })
        }
    }
})
</script>
