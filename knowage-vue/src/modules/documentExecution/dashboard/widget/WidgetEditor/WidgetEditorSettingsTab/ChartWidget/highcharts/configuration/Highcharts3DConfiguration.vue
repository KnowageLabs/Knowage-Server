<template>
    <div class="widget-editor-card p-p-2">
        {{ model }}

        <button @click="loadModel">test</button>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'
import { HighchartsPieChart } from '../../../../../ChartWidget/classes/highcharts/KnowageHighchartsPieChart'

export default defineComponent({
    name: 'hihgcharts-3d-configuration',
    components: { InputSwitch, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            model: {} as any
        }
    },
    computed: {},
    watch: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            const object = this.widgetModel.settings.chartModel
            console.log('>>>>>>>> object', object.getModel())
        }
    }
})
</script>
