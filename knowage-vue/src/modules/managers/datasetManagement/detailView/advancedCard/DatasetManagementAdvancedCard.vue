<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>
            <InputSwitch v-model="isTransformable" @change="setTransformationType" class="p-mr-2" />
            <span>Pivot Transformer</span>
        </template>
    </Toolbar>
    <Card v-if="isTransformable">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <span class="p-float-label p-col-3">
                    <InputText id="label" v-model="dataset.pivotColName" class="kn-material-input" type="text" />
                    <label for="label" class="kn-material-input-label"> Name of category column to be pivoted * </label>
                </span>
                <span class="p-float-label p-col-3">
                    <InputText id="label" v-model="dataset.pivotColValue" class="kn-material-input" type="text" />
                    <label for="label" class="kn-material-input-label"> Name of value column to be pivoted * </label>
                </span>
                <span class="p-float-label p-col-3">
                    <InputText id="label" v-model="dataset.pivotRowName" class="kn-material-input" type="text" />
                    <label for="label" class="kn-material-input-label"> Name of the column NOT to be pivoted *</label>
                </span>
                <span class="p-field-checkbox p-col-3">
                    <label for="binary">Automatic Columns numeration</label>
                    <Checkbox id="binary" class="p-ml-2" v-model="dataset.pivotIsNumRows" :binary="true" />
                </span>
            </form>
        </template>
    </Card>

    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
        <template #left>
            <InputSwitch v-model="dataset.isPersistedHDFS" class="p-mr-2" />
            <span>Exportable in HDFS</span>
        </template>
    </Toolbar>

    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
        <template #left>
            <InputSwitch v-model="dataset.isPersisted" class="p-mr-2" />
            <span>Persist</span>
        </template>
    </Toolbar>
    <Card v-if="dataset.isPersisted">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <span class="p-float-label p-col-3">
                    <InputText id="label" v-model="dataset.pivotColName" class="kn-material-input" type="text" />
                    <label for="label" class="kn-material-input-label"> Table name * </label>
                </span>
            </form>
        </template>
    </Card>

    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3" v-if="dataset.isPersistedHDFS || dataset.isPersisted">
        <template #left>
            <InputSwitch v-model="dataset.isScheduled" class="p-mr-2" />
            <span>Schedule</span>
        </template>
    </Toolbar>
    <KpiCron v-if="dataset.isScheduled" :frequency="frequency" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import advancedCardDescriptor from './DatasetManagementAdvancedCardDescriptor.json'
import KpiCron from '@/modules/kpi/kpiCron/KpiCron.vue'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    components: { Card, InputSwitch, Checkbox, KpiCron },
    props: {
        selectedDataset: { type: Object as any },
        transformationDataset: { type: Object as any }
    },
    computed: {},
    emits: ['touched'],
    data() {
        return {
            advancedCardDescriptor,
            dataset: {} as any,
            frequency: {} as any,
            testInput: 'testinput',
            testCheckbox: true,
            isTransformable: false
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.isDatasetTransformable()
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.isDatasetTransformable()
        }
    },
    validations() {},
    methods: {
        isDatasetTransformable() {
            if (this.dataset.trasfTypeCd && this.dataset.trasfTypeCd == this.transformationDataset.VALUE_CD) {
                this.isTransformable = true
            } else {
                this.isTransformable = false
            }
        },
        setTransformationType() {
            this.dataset.trasfTypeCd = this.isTransformable ? this.transformationDataset.VALUE_CD : ''
        }
    }
})
</script>
<style>
.custom-accordion-tab .p-accordion-header-link {
    background-color: #bbd6ed;
}
</style>
