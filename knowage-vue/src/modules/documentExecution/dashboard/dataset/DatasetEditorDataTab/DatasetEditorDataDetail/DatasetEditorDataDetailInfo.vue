<template>
    <Card>
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" :disabled="true" v-model="selectedDatasetProp.label" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" :disabled="true" v-model="selectedDatasetProp.name" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" :disabled="true" v-model="selectedDatasetProp.description" />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <InputText id="type" class="kn-material-input" type="text" :disabled="true" v-model="dataDialogDescriptor.datasetTypes[selectedDatasetProp.type]" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-6 p-as-center">
                    <span class="p-float-label">
                        <InputSwitch class="p-mr-2" v-model="selectedDatasetProp.cache" />
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
    data() {
        return {
            dataDialogDescriptor
        }
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
