<template>
    <Accordion>
        <AccordionTab header="Indexes">
            <div class="p-col-12 p-d-flex">
                <div class="kn-flex">
                    <span class="p-float-label">
                        <MultiSelect class="kn-material-input" :style="descriptor.style.indexMultiselect" v-model="selectedDatasetProp.modelIndexes" :options="selectedDatasetProp.metadata.fieldsMeta" optionLabel="alias" optionValue="alias" />
                        <label for="field" class="kn-material-input-label"> {{ $t('common.field') }}</label>
                    </span>
                </div>
                <i class="fas fa-info-circle p-button-text p-button-plain p-as-center p-ml-2" :style="descriptor.style.indexInfoIcon" v-tooltip.top="$t('dashboard.datasetEditor.indexHint')" />
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import MultiSelect from 'primevue/multiselect'
import descriptor from './DatasetEditorDataDetailDescriptor.json'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card, Accordion, AccordionTab, MultiSelect },
    props: { selectedDatasetProp: { required: true, type: Object } },
    emits: [],
    data() {
        return {
            descriptor
        }
    }
})
</script>
