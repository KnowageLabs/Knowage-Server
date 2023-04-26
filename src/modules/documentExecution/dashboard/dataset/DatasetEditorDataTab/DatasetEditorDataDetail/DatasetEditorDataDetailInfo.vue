<template>
    <Card class="p-mb-3 p-mr-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="label" v-model="selectedDataset.label" class="kn-material-input" type="text" :disabled="true" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="name" v-model="selectedDataset.name" class="kn-material-input" type="text" :disabled="true" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText id="description" v-model="selectedDataset.description" class="kn-material-input" type="text" :disabled="true" />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="type" v-model="dataDialogDescriptor.datasetTypes[selectedDataset.type]" class="kn-material-input" type="text" :disabled="true" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6 p-as-center">
                    <span class="p-float-label">
                        <InputSwitch v-model="selectedDataset.modelCache" class="p-mr-2" :disabled="cacheDisabled" />
                        <span>{{ $t('dashboard.datasetEditor.cached') }}</span>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import InputSwitch from 'primevue/inputswitch'
import dataDialogDescriptor from '../DatasetEditorDataDialog/DatasetEditorDataDialogDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card, InputSwitch },
    props: { selectedDatasetProp: { required: true, type: Object } },
    emits: [],
    setup() {},
    data() {
        return {
            dataDialogDescriptor,
            selectedDataset: {} as any
        }
    },
    watch: {
        selectedDatasetProp: {
            handler() {
                this.selectedDataset = this.selectedDatasetProp
            },
            deep: true
        }
    },
    computed: {
        cacheDisabled(): boolean {
            return (this.selectedDatasetProp.isCachingSupported && !this.selectedDatasetProp.isNearRealtimeSupported) || (!this.selectedDatasetProp.isCachingSupported && this.selectedDatasetProp.isNearRealtimeSupported) || this.selectedDatasetProp.isRealtime
        }
    },
    async created() {
        this.selectedDataset = this.selectedDatasetProp
    },
    methods: {}
})
</script>
