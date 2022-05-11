<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6 p-d-flex">
                    <div class="kn-flex">
                        <span class="p-float-label">
                            <InputText id="prepDatasetName" class="kn-material-input" v-model="dataset.name" :disabled="true" @change="$emit('touched')" />
                            <label for="prepDatasetName" class="kn-material-input-label"> {{ $t('common.name') }} </label>
                        </span>
                    </div>
                    <Button icon="fas fa-search fa-1x" class="p-button-text p-button-plain" @click="openPrepDatasetList" />
                </div>
                <div class="p-field p-col-6">
                    <Button label="OPEN DATA PREPARATION" class="kn-button kn-button--primary" @click="showDatasetListDialog = true" />
                </div>
            </form>
        </template>
    </Card>

    <KnDatasetList :visibility="showDatasetListDialog" :items="availableDatasets" @selected="newDataPrep" @save="openDataPreparation(selectedDsForDataPrep)" @cancel="showDatasetListDialog = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import descriptor from './DatasetManagementPreparedDataset.json'
import Card from 'primevue/card'
import KnDatasetList from '@/components/functionalities/KnDatasetList/KnDatasetList.vue'

export default defineComponent({
    components: { Card, KnDatasetList },
    props: { selectedDataset: { type: Object as any }, dataSources: { type: Array as any } },
    emits: ['touched'],
    data() {
        return {
            descriptor,
            dataset: {} as any,
            availableDatasets: [] as any,
            showDatasetListDialog: false
        }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    methods: {
        openPrepDatasetList() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/for-dataprep`).then(
                (response: AxiosResponse<any>) => {
                    this.availableDatasets = [...response.data.root]
                    this.showDatasetListDialog = true
                },
                () => {
                    this.$store.commit('setError', { title: 'Error', msg: 'Cannot load dataset list' })
                }
            )
        }
    }
})
</script>
