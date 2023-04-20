<template>
    <div v-if="layer" class="widget-editor-card">
        <h3 class="p-ml-3">{{ $t('common.layer') }}</h3>
        <div class="p-formgrid p-grid p-fluid p-mx-3">
            <div class="p-float-label p-col-12 p-lg-6 kn-flex">
                <InputText v-model="layer.alias" class="kn-material-input" :disabled="true" />
                <label class="kn-material-input-label">{{ $t('common.layer') }}</label>
            </div>

            <span class="p-field p-float-label p-col-12 p-lg-6">
                <Dropdown v-model="layer.type" class="kn-material-input" :options="descriptor.layerTypes"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.type') }} </label>
            </span>
        </div>

        <hr />

        <div class="p-formgrid p-grid p-m-3 p-pt-3">
            <span class="p-field p-col-12 p-lg-4 p-jc-center p-pl-3">
                <InputSwitch v-model="layer.isStatic" @change="onStaticChange" />
                <label class="kn-material-input-label p-ml-3"> {{ $t('common.static') }} </label>
                <i v-tooltip.top="$t('dashboard.widgetEditor.map.layerInfo.staticHint')" class="pi pi-question-circle kn-cursor-pointer p-mx-3"></i>
            </span>
            <span class="p-field p-col-12 p-lg-4 p-jc-center p-pl-3">
                <InputSwitch v-model="layer.targetDefault" :disabled="layer.isStatic" />
                <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.map.layerInfo.target') }} </label>
                <i v-tooltip.top="$t('dashboard.widgetEditor.map.layerInfo.targetHint')" class="pi pi-question-circle kn-cursor-pointer p-mx-3"></i>
            </span>
            <span class="p-field p-col-12 p-lg-4 p-jc-center p-pl-3">
                <InputSwitch v-model="layer.defaultVisible" :disabled="layer.isStatic" />
                <label class="kn-material-input-label p-ml-3"> {{ $t('dashboard.widgetEditor.map.layerInfo.defaultVisible') }} </label>
                <i v-tooltip.top="$t('dashboard.widgetEditor.map.layerInfo.defaultVisibleHint')" class="pi pi-question-circle kn-cursor-pointer p-mx-3"></i>
            </span>
        </div>

        <Message class="p-mx-4" severity="info" :closable="false">
            {{ $t('dashboard.widgetEditor.map.layerInfo.linkHint') }}
        </Message>

        <div class="p-formgrid p-grid p-p-3 p-mt-2">
            <div class="p-col-5">
                <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid kn-width-full">
                    <Dropdown v-model="layer.datasetLink" class="kn-material-input" :options="datasets" option-value="id.dsId" option-label="name"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.dataset') }} </label>
                </span>
                <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid kn-width-full">
                    <Dropdown v-model="layer.datasetColumnLink" class="kn-material-input" :options="datasetColumnsLinkOptions" option-value="name" option-label="alias"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.layerInfo.datasetColumn') }} </label>
                </span>
            </div>
            <div class="p-d-flex p-flex-column p-jc-center p-ai-center p-col-2">
                <i class="fa fa-link"></i>
            </div>
            <div class="p-col-5">
                <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid kn-width-full">
                    <Dropdown v-model="layer.catalogLayerLink" class="kn-material-input" :options="layers" option-value="name" option-label="name"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.layerInfo.catalogLayer') }} </label>
                </span>
                <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid kn-width-full">
                    <Dropdown v-model="layer.catalogLayerColumnLink" class="kn-material-input" :options="[]"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.layerInfo.catalogLayerColumn') }} </label>
                </span>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { ILayer, IMapWidgetLayer } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { IDataset } from '@/modules/documentExecution/dashboard/Dashboard'
import { mapActions } from 'pinia'
import descriptor from './MapWidgetLayerInfoDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import Message from 'primevue/message'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'

export default defineComponent({
    name: 'map-widget-layer-info',
    components: { Dropdown, InputSwitch, Message },
    props: { selectedLayer: { type: Object as PropType<IMapWidgetLayer | null>, required: true }, layers: { type: Array as PropType<ILayer[]>, required: true } },
    data() {
        return {
            descriptor,
            layer: null as IMapWidgetLayer | null,
            datasets: [] as IDataset[],
            getTranslatedLabel
        }
    },
    computed: {
        datasetColumnsLinkOptions() {
            if (!this.layer || !this.layer.datasetLink) return []
            const index = this.datasets.findIndex((dataset: IDataset) => dataset.id.dsId === this.layer?.datasetLink)
            return index !== -1 ? this.datasets[index].metadata.fieldsMeta : []
        }
    },
    watch: {
        selectedLayer() {
            this.loadLayer()
        }
    },

    created() {
        this.loadLayer()
        this.loadDatasets()
    },
    methods: {
        ...mapActions(dashboardStore, ['getAllDatasets']),
        loadLayer() {
            this.layer = this.selectedLayer
        },
        loadDatasets() {
            this.datasets = this.getAllDatasets()
        },
        onStaticChange() {
            if (!this.layer) return
            if (this.layer.isStatic) {
                this.layer.targetDefault = false
                this.layer.defaultVisible = true
            }
        }
    }
})
</script>
