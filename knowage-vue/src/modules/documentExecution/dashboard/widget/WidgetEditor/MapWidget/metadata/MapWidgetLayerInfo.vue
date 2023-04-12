<template>
    <div v-if="layer" class="widget-editor-card p-p-2">
        <h2 class="p-ml-3">{{ $t('common.layer') }}</h2>
        <div class="p-formgrid p-grid p-p-3">
            <div class="p-float-label p-col-12 p-lg-6 kn-flex">
                <InputText v-model="layer.name" class="kn-material-input kn-width-full" :disabled="true" />
                <label class="kn-material-input-label">{{ $t('common.layer') }}</label>
            </div>

            <span class="p-field p-float-label p-col-12 p-lg-6 p-fluid">
                <Dropdown v-model="layer.type" class="kn-material-input" :options="descriptor.layerTypes"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.type') }} </label>
            </span>
        </div>

        <hr />

        <div class="p-formgrid p-grid p-p-3 p-mt-2">
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
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { IMapWidgetLayer } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetMetadataDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'map-widget-layer-info',
    components: { Dropdown, InputSwitch },
    props: { selectedLayer: { type: Object as PropType<IMapWidgetLayer | null>, required: true } },
    data() {
        return {
            descriptor,
            layer: null as IMapWidgetLayer | null,
            getTranslatedLabel
        }
    },
    watch: {
        selectedLayer() {
            this.loadLayer()
        }
    },
    created() {
        this.loadLayer()
    },
    methods: {
        loadLayer() {
            this.layer = this.selectedLayer
            console.log('---- loadedLayer', this.layer)
        },
        onStaticChange() {
            if (!this.layer) return
            this.layer.targetDefault = false
            this.layer.defaultVisible = true
        }
    }
})
</script>
