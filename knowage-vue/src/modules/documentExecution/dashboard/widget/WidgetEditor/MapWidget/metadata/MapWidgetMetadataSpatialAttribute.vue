<template>
    <div class="p-formgrid p-grid p-p-3">
        <div class="p-col-12 p-mb-4">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.metadata.spatialAttribute') }}</label>
        </div>

        <div class="p-float-label p-col-12 p-lg-4 kn-flex">
            <InputText v-model="spatialAttribute.alias" class="kn-material-input kn-width-full" :disabled="true" />
            <label class="kn-material-input-label">{{ $t('common.column') }}</label>
        </div>

        <span class="p-field p-float-label p-col-12 p-lg-4 p-fluid">
            <Dropdown v-model="spatialAttribute.properties.coordType" class="kn-material-input" :options="descriptor.coordTypes" option-value="value">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.coordTypes, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
            <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.metadata.coordType') }} </label>
        </span>

        <span class="p-field p-float-label p-col-12 p-lg-4 p-fluid">
            <Dropdown v-model="spatialAttribute.properties.coordFormat" class="kn-material-input" :options="descriptor.coordFormats" option-value="value">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.coordFormats, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
            <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.map.metadata.coordFormat') }} </label>
        </span>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from './MapWidgetMetadataDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'map-widget-metadata-spatial-attribute',
    components: { Dropdown },
    props: { propSpatialAttribute: { type: Object as PropType<any>, required: true } },
    data() {
        return {
            descriptor,
            spatialAttribute: null as any,
            getTranslatedLabel
        }
    },
    watch: {
        propSpatialAttribute() {
            this.loadSpatialAttribute()
        }
    },
    created() {
        this.loadSpatialAttribute()
    },
    methods: {
        loadSpatialAttribute() {
            this.spatialAttribute = this.propSpatialAttribute
        }
    }
})
</script>
