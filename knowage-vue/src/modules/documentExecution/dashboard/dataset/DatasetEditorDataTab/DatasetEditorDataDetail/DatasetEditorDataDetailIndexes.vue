<template>
    <Accordion>
        <AccordionTab :header="$t('dashboard.datasetEditor.indexesTitle')">
            <div class="p-col-12 p-d-flex">
                <div class="kn-flex">
                    <span class="p-float-label">
                        <MultiSelect v-model="selectedDatasetProp.modelIndexes" class="kn-material-input" :style="descriptor.style.indexMultiselect" :options="selectedDatasetProp.metadata.fieldsMeta" option-label="alias" option-value="alias" />
                        <label for="field" class="kn-material-input-label"> {{ $t('common.field') }}</label>
                    </span>
                </div>
                <i v-tooltip.top="$t('dashboard.datasetEditor.indexHint')" class="fas fa-info-circle p-button-text p-button-plain p-as-center p-ml-2" :style="descriptor.style.indexInfoIcon" />
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import MultiSelect from 'primevue/multiselect'
import descriptor from './DatasetEditorDataDetailDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Accordion, AccordionTab, MultiSelect },
    props: { selectedDatasetProp: { required: true, type: Object } },
    emits: [],
    data() {
        return {
            descriptor,
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
    }
})
</script>
