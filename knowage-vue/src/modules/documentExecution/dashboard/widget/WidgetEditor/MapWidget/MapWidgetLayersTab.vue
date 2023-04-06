<template>
    <LayersList></LayersList>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IDataset, IWidget } from '../../../Dashboard'
import { mapState } from 'pinia'
import mainStore from '@/App.store'

import LayersList from './MapWidgetLayersTabList.vue'

export default defineComponent({
    name: 'map-widget-layers-tab',
    components: { LayersList },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: {
            type: Array as PropType<IDataset[]>,
            default: function () {
                return []
            }
        },
        selectedDatasets: {
            type: Array as PropType<IDataset[]>,
            default: function () {
                return []
            }
        }
    },
    data() {
        return {
            selectedDataset: null as IDataset | null,
            widget: {} as IWidget
        }
    },
    computed: {
        ...mapState(mainStore, {
            isEnterprise: 'isEnterprise'
        })
    },
    created() {
        this.loadWidget()
    },
    methods: {
        loadWidget() {
            this.widget = this.propWidget
        }
    }
})
</script>
