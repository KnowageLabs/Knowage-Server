<template>
    <div v-if="noDataConfiguration" class="p-d-flex p-flex-row kn-flex" :class="wrapperStyle">
        <div :style="innerDivStyle">
            {{ noDataConfiguration.text }}
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import { IVegaChartsNoDataConfiguration } from '../../../interfaces/vega/VegaChartsWidget'

export default defineComponent({
    name: 'vega-container-no-data',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true }
    },
    data() {
        return {
            noDataConfiguration: null as IVegaChartsNoDataConfiguration | null
        }
    },
    computed: {
        wrapperStyle() {
            if (!this.noDataConfiguration || !this.noDataConfiguration.position) return ''
            return `p-jc-${this.noDataConfiguration.position.align} p-ai-${this.noDataConfiguration.position.verticalAlign}`
        },
        innerDivStyle() {
            if (!this.noDataConfiguration || !this.noDataConfiguration.style) return ''
            let style = ''
            const configStyle = this.noDataConfiguration.style
            Object.keys(configStyle).forEach((key: string) => (style += `${key}:${configStyle[key]}; `))
            return style
        }
    },
    created() {
        this.loadConfiguration()
    },
    methods: {
        loadConfiguration() {
            this.noDataConfiguration = this.widgetModel.settings.configuration ? this.widgetModel.settings.configuration.noDataConfiguration : null
        }
    }
})
</script>
