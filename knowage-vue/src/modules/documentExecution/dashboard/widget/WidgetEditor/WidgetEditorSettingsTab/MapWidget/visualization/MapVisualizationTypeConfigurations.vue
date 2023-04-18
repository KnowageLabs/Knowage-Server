<template>
    <div class="p-d-flex p-flex-column p-m-2">
        <div v-if="visType.type === 'markers'">markers component</div>
        <div v-else-if="visType.type === 'balloons'">balloons component</div>
        <div v-else-if="visType.type === 'pies'">pies component</div>
        <div v-else-if="visType.type === 'clusters'">clusters component</div>
        <div v-else-if="visType.type === 'heatmap'">heatmap component</div>
        <div v-else-if="visType.type === 'choropleth'">choropleth component</div>
    </div>
</template>

<script lang="ts">
import { IMapWidgetVisualizationType } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'

import descriptor from './MapVisualizationTypeDescriptor.json'

export default defineComponent({
    name: 'map-visualization-type',
    components: {},
    props: { visTypeProp: { type: Object as PropType<IMapWidgetVisualizationType>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            visType: {} as IMapWidgetVisualizationType
        }
    },
    watch: {
        widgetModel() {
            this.loadVisType()
        }
    },
    created() {
        this.loadVisType()
    },
    methods: {
        loadVisType() {
            this.visType = this.visTypeProp as IMapWidgetVisualizationType
        }
    }
})
</script>

<style lang="scss" scoped></style>
