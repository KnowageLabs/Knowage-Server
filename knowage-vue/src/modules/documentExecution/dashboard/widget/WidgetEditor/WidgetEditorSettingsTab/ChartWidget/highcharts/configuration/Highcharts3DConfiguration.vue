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
            class Greeter {
                public greet() {
                    console.log('hi!')
                }
            }
            const g = new Greeter()
            g.greet()
            console.log('>>>>>>>> blaaaa: ', typeof g)
            console.log('>>>>>>>> widgetModel: ', this.widgetModel)
            console.log('>>>>>>>> typeof: ', typeof this.widgetModel.settings.chartModel)
            const object = this.widgetModel.settings.chartModel
            console.log('>>>>>>>> object', object)
            const test = new HighchartsPieChart({}, this.widgetModel)
            console.log('>>>>>>>> TEEEEEEEEEEEEEEEEEEEEEEST', test)
        }
    }
})
</script>
