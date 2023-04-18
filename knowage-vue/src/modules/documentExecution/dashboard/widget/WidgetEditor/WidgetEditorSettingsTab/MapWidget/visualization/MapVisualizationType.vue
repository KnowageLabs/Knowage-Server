<template>
    <div v-for="(visType, visTypeIndex) in visualizationTypeModel" :key="visTypeIndex" class="p-d-flex p-flex-column p-m-2">
        <div class="p-d-flex kn-flex p-ai-center p-mb-2">
            <span class="p-float-label kn-flex">
                <Dropdown id="attributes" v-model="dropdownSelectedLayer" class="kn-material-input kn-width-full" :options="dropdownAvailableLayers" option-label="name" option-value="name" />
                <label for="attributes" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.attribute') }} </label>
            </span>
            <Button v-if="visTypeIndex == 0" icon="fas fa-plus-circle fa-1x" class="p-button-text p-button-plain p-js-center p-ml-2" />
        </div>

        <div class="p-grid gap-1 p-m-0" style="column-gap: 0.5em; row-gap: 0.5em">
            <div v-for="(visTypeConfig, visTypeConfigIndex) in descriptor.visTypes" :key="visTypeConfigIndex" v-tooltip.bottom="$t(visTypeConfig.tooltip)" class="visTypeCards" :class="{ selected: visType.type === visTypeConfig.name }" @click="selectVisTypeConfig(visTypeIndex, visTypeConfig.name)">
                <img class="kn-width-full kn-height-full" :src="getImageSource(visTypeConfig.name)" />
            </div>
        </div>

        <hr class="kn-width-full p-my-2" />

        <VisTypeConfig :vis-type-prop="visType" />

        <br />
        <br />
        <br />
    </div>
</template>

<script lang="ts">
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IMapWidgetVisualizationType } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'

import descriptor from './MapVisualizationTypeDescriptor.json'

import Dropdown from 'primevue/dropdown'
import VisTypeConfig from './MapVisualizationTypeConfigurations.vue'

export default defineComponent({
    name: 'map-visualization-type',
    components: { Dropdown, VisTypeConfig },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            visualizationTypeModel: [] as IMapWidgetVisualizationType[],
            dropdownSelectedLayer: null as any,
            //TODO: hardcoded, implement real layers from widget
            dropdownAvailableLayers: [{ name: 'Layer 1' }, { name: 'Layer 2' }]
        }
    },
    watch: {
        widgetModel() {
            this.loadVisTypeModel()
        }
    },
    created() {
        this.loadVisTypeModel()
    },
    methods: {
        loadVisTypeModel() {
            if (this.widgetModel.settings?.visualization?.types) this.visualizationTypeModel = this.widgetModel.settings?.visualization?.types as IMapWidgetVisualizationType[]
            console.log('visualizationTypeModel', this.visualizationTypeModel)
        },
        getImageSource(visType: string) {
            return `${import.meta.env.VITE_PUBLIC_PATH}images/dashboard/mapVisTypes/${visType}.svg`
        },
        selectVisTypeConfig(visTypeIndex, visTypeConfigName) {
            this.visualizationTypeModel[visTypeIndex].type = visTypeConfigName
        }
    }
})
</script>

<style lang="scss" scoped>
.visTypeCards {
    cursor: pointer;
    border: 1px solid #cccccc;
    height: 80px;
    width: 140px;
    &.selected {
        background-color: #bbd6ed;
    }
    &:hover {
        background-color: darken(#bbd6ed, 15%);
    }
    &:hover,
    &.selected {
        .visTypeIcon {
            background-color: #deecf8;
        }
    }
}
</style>
