<template>
    <div v-if="widgetModel" class="dashboard-editor-list-card-container p-m-3">
        <div class="p-col-12 p-d-flex">
            <label class="kn-material-input-label p-as-center p-ml-1"> {{ $t('workspace.gis.dnl.layers') }} </label>
            <Button :label="$t('workspace.gis.dnl.addLayer')" icon="pi pi-plus-circle" class="p-button-outlined p-ml-auto p-mr-1" @click="openLayersDialog" />
        </div>
    </div>

    <LayersDialog :visible="layersDialogVisible" :available-datasets-prop="selectedDatasets" :selected-datasets-prop="[]" @close="closeLayersDialog" />
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IDataset, IWidget } from '../../../Dashboard'

import LayersDialog from './MapWidgetLayersTabDialog.vue'

export default defineComponent({
    name: 'map-widget-layers-list',
    components: { LayersDialog },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
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
    emits: ['layerSelected'],
    setup() {},
    data() {
        return {
            layersDialogVisible: false
        }
    },
    watch: {},
    created() {},
    unmounted() {},
    methods: {
        openLayersDialog() {
            this.layersDialogVisible = true
        },
        closeLayersDialog() {
            this.layersDialogVisible = false
        }
    }
})
</script>

<style lang="scss" scoped></style>
