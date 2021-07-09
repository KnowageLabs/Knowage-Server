<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('kpi.measureDefinition.title') }}
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showForm" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <KnHint v-if="measuresList.length === 0" :title="'kpi.measureDefinition.title'" :hint="'kpi.measureDefinition.hint'" data-test="measure-hint"></KnHint>
                <DataTable v-else :value="measuresList" :loading="loading" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="measures-table">
                    <template #loading>
                        {{ $t('common.info.dataLoading') }}
                    </template>
                    <Column v-for="col of measureDefintionDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" class="kn-truncated"> </Column>
                    <Column :style="datasetTableCardDescriptor.table.iconColumn.style">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteDatasetConfirm(slotProps.data.signature)" data-test="delete-button" />
                        </template>
                    </Column>
                </DataTable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMeasure } from './MeasureDefinition'
import axios from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'
import measureDefintionDescriptor from './MeasureDefintionDescriptor.json'

export default defineComponent({
    name: 'measure-definition',
    components: {
        Column,
        DataTable,
        KnFabButton,
        KnHint
    },
    data() {
        return {
            measureDefintionDescriptor,
            measuresList: [] as iMeasure[],
            loading: false
        }
    },
    async created() {
        await this.loadMeasures()
        console.log('MEASURES: ', this.measuresList)
    },
    methods: {
        async loadMeasures() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listMeasure')
                .then((response) => (this.measuresList = response.data))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
