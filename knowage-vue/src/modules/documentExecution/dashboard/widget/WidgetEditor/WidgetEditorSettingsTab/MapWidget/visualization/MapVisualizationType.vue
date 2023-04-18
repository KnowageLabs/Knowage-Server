<template>
    <div v-if="model" class="p-d-flex p-flex-column p-m-2">
        <div class="p-d-flex kn-flex p-ai-center p-mb-2">
            <span class="p-float-label kn-flex">
                <Dropdown id="attributes" v-model="dropdownSelectedLayer" class="kn-material-input kn-width-full" :options="dropdownAvailableLayers" option-label="name" option-value="name" />
                <label for="attributes" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.attribute') }} </label>
            </span>
            <Button icon="fas fa-plus-circle fa-1x" class="p-button-text p-button-plain p-js-center p-ml-2" />
        </div>

        <div class="p-grid gap-1 p-m-0" style="column-gap: 0.5em; row-gap: 0.5em">
            <div v-for="(visType, index) in descriptor.visTypes" :key="index" v-tooltip.bottom="$t(visType.tooltip)" class="visTypeCards">
                <img class="kn-width-full kn-height-full" :src="getImageSource(visType.name)" />
            </div>
        </div>
        <br />
    </div>
</template>

<script lang="ts">
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'

import descriptor from './MapVisualizationTypeDescriptor.json'

import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'image-widget-gallery',
    components: { Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    emits: ['uploadedImage'],
    data() {
        return {
            descriptor,
            model: {} as IWidget,
            dropdownSelectedLayer: null as any,
            dropdownAvailableLayers: [{ name: 'Layer 1' }, { name: 'Layer 2' }]
        }
    },
    watch: {
        widgetModel() {
            this.loadModel()
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel
        },
        getImageSource(visType: string) {
            return `${import.meta.env.VITE_PUBLIC_PATH}images/dashboard/mapVisTypes/${visType}.svg`
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
