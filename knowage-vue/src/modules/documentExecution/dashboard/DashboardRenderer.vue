<template>
    <KnDashboardTabsPanel label-position="bottom" :sheets="dashboardModel.sheets" @sheet-change="sheetChange">
        <KnDashboardTab v-for="(sheet, index) in dashboardModel.sheets" :key="index" :index="index">
            <grid-layout v-model:layout="dashboardModel.sheets[index].widgets" :col-num="100" :row-height="30" :is-draggable="true" :is-resizable="true" :vertical-compact="false" :use-css-transforms="true" :margin="[2, 2]">
                <WidgetController :sheet="index" :widget="currentWidget(item.id)" :item="item" v-for="item in dashboardModel.sheets[index].widgets" :key="item.i"></WidgetController>
            </grid-layout>
        </KnDashboardTab>
    </KnDashboardTabsPanel>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import WidgetController from './widget/WidgetController.vue'
import KnDashboardTabsPanel from '@/components/UI/KnDashboardTabs/KnDashboardTabsPanel.vue'
import KnDashboardTab from '@/components/UI/KnDashboardTabs/KnDashboardTab.vue'

export default defineComponent({
    name: 'dashboard-manager',
    components: { KnDashboardTab, KnDashboardTabsPanel, WidgetController },
    props: ['model'],
    data() {
        return {
            dashboardModel: {} as any
        }
    },
    mounted() {
        this.dashboardModel = this.model
    },
    methods: {
        currentWidget(id) {
            return this.dashboardModel.widgets.filter((item) => item.id === id)
        },
        sheetChange(index) {
            this.$store.commit('dashboard/setDashboardSheet', { id: this.model.configuration.id, sheet: index })
        }
    }
})
</script>
