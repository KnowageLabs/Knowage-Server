<template>
    <div v-if="widgetModel" class="dashboard-editor-list-card-container p-my-3 p-ml-3">
        <div class="p-col-12 p-d-flex">
            <label class="kn-material-input-label p-as-center p-ml-1"> {{ $t('workspace.gis.dnl.layers') }} </label>
            <Button :label="$t('workspace.gis.dnl.addLayer')" icon="pi pi-plus-circle" class="p-button-outlined p-ml-auto p-mr-1" @click="openLayersDialog" />
        </div>

        <Listbox class="kn-list kn-list-no-border-right dashboard-editor-list" :options="layers" :filter="true" :filter-placeholder="$t('common.search')" :filter-fields="dataListDescriptor.filterFields" :empty-filter-message="$t('common.info.noDataFound')">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div
                    class="kn-list-item kn-draggable"
                    draggable="true"
                    :style="dataListDescriptor.style.list.listItem"
                    @dragstart="onDragStart($event, slotProps.index)"
                    @drop.stop="onDropComplete($event, slotProps.index)"
                    @dragover.prevent
                    @dragenter.prevent
                    @dragleave.prevent
                    @click="$emit('layerSelected', slotProps.option)"
                >
                    <i class="pi pi-bars" :style="dataListDescriptor.style.list.listIcon"></i>
                    <i :style="dataListDescriptor.style.list.listIcon" :class="slotProps.option.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'" class="p-ml-2"></i>
                    <div class="kn-list-item-text">
                        <span v-tooltip.top="slotProps.option.alias" class="dashboard-editor-list-alias-container">{{ slotProps.option.alias }}</span>
                    </div>
                </div>
            </template>
        </Listbox>
    </div>

    <LayersDialog :visible="layersDialogVisible" :available-datasets-prop="selectedDatasets" :selected-datasets-prop="widgetModel.layers" @close="closeLayersDialog" />
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IDataset, IWidget } from '../../../Dashboard'
import { IMapWidgetLayer } from '../../../interfaces/mapWidget/DashboardMapWidget'
import LayersDialog from './MapWidgetLayersTabDialog.vue'
import Listbox from 'primevue/listbox'
import dataListDescriptor from './MapWidgetLayersTabListDescriptor.json'

export default defineComponent({
    name: 'map-widget-layers-list',
    components: { LayersDialog, Listbox },
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
            dataListDescriptor,
            layers: [] as IMapWidgetLayer[],
            layersDialogVisible: false
        }
    },
    created() {
        this.loadLayers()
    },
    methods: {
        loadLayers() {
            this.layers = this.widgetModel.layers
        },
        openLayersDialog() {
            this.layersDialogVisible = true
        },
        closeLayersDialog() {
            this.layersDialogVisible = false
        },
        onDragStart(event: any, startIndex: number) {
            event.dataTransfer.setData('text/plain', JSON.stringify(startIndex))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        onDropComplete(event: any, dropIndex: number) {
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            const temp = this.widgetModel.layers[eventData]
            this.layers.splice(eventData, 1)
            this.layers.splice(dropIndex, 0, temp)
        }
    }
})
</script>

<style lang="scss" scoped></style>
