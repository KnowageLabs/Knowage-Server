<template>
    <div class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-d-flex p-flex-row p-col-12 p-mb-4">
            <label class="kn-material-input-label">{{ $t('common.fields') }}</label>
            <i class="pi pi-plus-circle kn-cursor-pointer p-ml-auto" @click="addField"></i>
        </div>

        <div v-for="field in layerFields" :key="field.name" class="dynamic-form-item p-grid p-col-12 p-ai-center">
            <div class="p-grid p-ai-center p-mt-2">
                <div class="p-col-12 p-d-flex p-flex-row">
                    <div class="p-float-label kn-flex">
                        <InputText v-model="field.alias" class="kn-material-input kn-width-full" />
                        <label class="kn-material-input-label">{{ $t('common.column') }}</label>
                    </div>
                    <div class="p-field p-float-label p-fluid kn-flex p-ml-2">
                        <Dropdown v-model="field.fieldType" class="kn-material-input" :options="descriptor.columnTypeOptions" :disabled="true"> </Dropdown>
                        <label class="kn-material-input-label">{{ $t('common.type') }}</label>
                    </div>
                    <div v-if="field.fieldType === 'MEASURE'" class="p-field p-float-label p-fluid kn-flex p-ml-2">
                        <Dropdown v-model="field.aggregationSelected" class="kn-material-input" :options="descriptor.columnAggregationOptions" option-value="value" option-label="label"> </Dropdown>
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.aggregation') }}</label>
                    </div>
                    <div v-if="field.fieldType !== 'SPATIAL_ATTRIBUTE'" class="p-d-flex p-flex-row p-jc-center p-ai-center p-ml-3 p-mb-2">
                        <i class="pi pi-trash kn-cursor-pointer" @click="removeField(field)"></i>
                    </div>
                </div>

                <div class="p-col-12 p-d-flex p-flex-row">
                    <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3 p-pl-3">
                        <InputSwitch v-model="field.properties.aggregateBy" @change="onAggregateByChanged(field)" />
                        <label class="kn-material-input-label p-ml-3">
                            {{ $t('dashboard.widgetEditor.map.metadata.aggregateBy') }}
                        </label>
                        <i v-tooltip.top="$t('dashboard.widgetEditor.map.metadata.aggregateByHint')" class="pi pi-question-circle kn-cursor-pointer p-mx-3"></i>
                    </span>

                    <span v-if="field.fieldType === 'MEASURE'" class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3 p-pl-3">
                        <InputSwitch v-model="field.properties.showMap" />
                        <label class="kn-material-i47nput-label p-ml-3">
                            {{ $t('dashboard.widgetEditor.map.metadata.showOnMap') }}
                        </label>
                    </span>

                    <span v-if="field.fieldType === 'ATTRIBUTE'" class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3 p-pl-3">
                        <InputSwitch v-model="field.properties.showFilter" :disabled="!field.properties.aggregateBy" />
                        <label class="kn-material-input-label p-ml-3">
                            {{ $t('dashboard.widgetEditor.map.metadata.showOnFilters') }}
                        </label>
                    </span>

                    <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3 p-pl-3">
                        <InputSwitch v-model="field.properties.modal" :disabled="!field.properties.aggregateBy" />
                        <label class="kn-material-input-label p-ml-3">
                            {{ $t('common.modal') }}
                        </label>
                        <i v-tooltip.top="$t('dashboard.widgetEditor.map.metadata.modalHint')" class="pi pi-question-circle kn-cursor-pointer p-mx-3"></i>
                    </span>
                </div>
            </div>
        </div>

        <MapWidgetMetadataNewFieldDialog v-if="addNewFieldDialogVisible" :visible="addNewFieldDialogVisible" :prop-fields="fields" @addSelectedFields="onAddSelectedFields" @close="addNewFieldDialogVisible = false"></MapWidgetMetadataNewFieldDialog>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IWidgetMapLayerColumn } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import descriptor from './MapWidgetMetadataDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import MapWidgetMetadataNewFieldDialog from './MapWidgetMetadataNewFieldDialog.vue'

export default defineComponent({
    name: 'map-widget-metadata-fields',
    components: { Dropdown, InputSwitch, MapWidgetMetadataNewFieldDialog },
    props: {
        propFields: {
            type: Array as PropType<IWidgetMapLayerColumn[]>,
            required: true
        }
    },
    data() {
        return {
            descriptor,
            fields: [] as IWidgetMapLayerColumn[],
            addNewFieldDialogVisible: false
        }
    },
    computed: {
        layerFields() {
            return this.fields.filter((field: IWidgetMapLayerColumn) => !field.deleted)
        }
    },
    watch: {
        propFields() {
            this.loadFields()
        }
    },
    created() {
        this.loadFields()
    },
    methods: {
        loadFields() {
            this.fields = this.propFields
        },
        onAggregateByChanged(field: IWidgetMapLayerColumn) {
            if (!field.properties.aggregateBy) {
                field.properties.modal = false
                field.properties.showFilter = false
            }
        },
        addField() {
            this.addNewFieldDialogVisible = true
        },
        removeField(field: IWidgetMapLayerColumn) {
            const index = this.fields.findIndex((tempField: IWidgetMapLayerColumn) => tempField.name === field.name)
            if (index !== -1) this.fields[index].deleted = true
        },
        onAddSelectedFields(fields: IWidgetMapLayerColumn[]) {
            fields.forEach((field: IWidgetMapLayerColumn) => {
                const index = this.fields.findIndex((tempField: IWidgetMapLayerColumn) => tempField.name === field.name)
                if (index !== -1) this.fields[index].deleted = false
            })
            this.addNewFieldDialogVisible = false
        }
    }
})
</script>
