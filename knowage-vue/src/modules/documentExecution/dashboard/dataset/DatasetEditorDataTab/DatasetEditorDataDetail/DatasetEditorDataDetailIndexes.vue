<template>
    <Accordion>
        <AccordionTab header="Indexes">
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-d-flex">
                    <div class="kn-flex">
                        <span class="p-float-label">
                            <Dropdown id="field" class="kn-material-input" v-model="selectedField" :options="selectedDatasetProp.metadata.fieldsMeta" optionLabel="alias" optionValue="alias" />
                            <label for="field" class="kn-material-input-label"> {{ $t('common.field') }}</label>
                        </span>
                    </div>
                    <Button icon="pi pi-plus-circle" class="p-button-text p-button-rounded p-button-plain p-ml-2 p-as-end" @click="checkFieldValidity" />
                </div>
                <div v-for="(field, index) of datasetIndexes" :key="index" class="p-field p-col-12 p-d-flex p-ai-baseline">
                    <div class="kn-flex">
                        {{ field }}
                    </div>
                    <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" @click="removeFieldFromIndexes(field)" />
                </div>
            </form>

            <!-- MULTIVALUE SOLUTION -----------------------------------
            <div class="p-col-12 p-d-flex">
                <div class="kn-flex">
                    <span class="p-float-label">
                        <MultiSelect class="kn-material-input" :style="descriptor.style.indexMultiselect" v-model="datasetIndexes" :options="selectedDatasetProp.metadata.fieldsMeta" optionLabel="alias" optionValue="alias" @change="logMulti" />
                        <label for="field" class="kn-material-input-label"> {{ $t('common.field') }}</label>
                    </span>
                </div>
                <i class="fas fa-info-circle p-button-text p-button-plain p-as-center p-ml-2" :style="descriptor.style.indexInfoIcon" v-tooltip.top="$t('dashboard.datasetEditor.indexHint')" />
            </div> -->
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Dropdown from 'primevue/dropdown'
import descriptor from './DatasetEditorDataDetailDescriptor.json'
import mainStore from '../../../../../../App.store'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card, Accordion, AccordionTab, Dropdown },
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any } },
    emits: [],
    data() {
        return {
            descriptor,
            datasetIndexes: [] as any,
            selectedField: ''
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.getIndexesFromModel()
    },
    methods: {
        getIndexesFromModel() {
            this.datasetIndexes = this.dashboardDatasetsProp?.find((dataset) => dataset.id === this.selectedDatasetProp.id.dsId).indexes
            console.log(this.datasetIndexes)
        },
        checkFieldValidity() {
            if (this.selectedField != '') {
                if (this.datasetIndexes.indexOf(this.selectedField) > -1) {
                    this.store.setInfo({
                        title: this.$t('common.toast.warning'),
                        msg: this.$t('dashboard.datasetEditor.indexPresentMsg')
                    })
                } else {
                    this.addFieldToIndexes()
                    this.selectedField = ''
                    this.getIndexesFromModel()
                }
            } else return
        },
        addFieldToIndexes() {
            this.dashboardDatasetsProp.find((dataset) => dataset.id === this.selectedDatasetProp.id.dsId).indexes.push(this.selectedField)
        },
        removeFieldFromIndexes(fieldToRemove) {
            this.datasetIndexes = this.datasetIndexes.filter((field) => field !== fieldToRemove)
            this.dashboardDatasetsProp.find((dataset) => dataset.id === this.selectedDatasetProp.id.dsId).indexes = this.datasetIndexes
        }
        // MULTIVALUE SOLUTION ------------------------------------
        // saveMultivalueChanges(event) {
        //     console.log(event.value)
        //     this.dashboardDatasetsProp.find((dataset) => dataset.id === this.selectedDatasetProp.id.dsId).indexes = event.value
        // }
    }
})
</script>
