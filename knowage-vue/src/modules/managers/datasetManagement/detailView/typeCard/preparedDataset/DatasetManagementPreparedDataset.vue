<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <Button :label="$t('managers.datasetManagement.monitoring')" class="kn-button kn-button--primary" @click="showMonitoringDialog = true" />
                </div>
                <div class="p-field p-col-6">
                    <Button :label="$t('managers.datasetManagement.openDP')" class="kn-button kn-button--primary" @click="prepareForDataPreparation" />
                </div>
            </form>
        </template>
    </Card>

    <MonitoringDialog :visibility="showMonitoringDialog" :dataset="selectedDataset" @close="showMonitoringDialog = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import descriptor from './DatasetManagementPreparedDataset.json'
import Card from 'primevue/card'
import MonitoringDialog from '@/modules/workspace/dataPreparation/DataPreparationMonitoring/DataPreparationMonitoringDialog.vue'

export default defineComponent({
    components: { Card, MonitoringDialog },
    props: { selectedDataset: { type: Object as any }, dataSources: { type: Array as any } },
    emits: ['touched'],
    data() {
        return {
            descriptor,
            dataset: {} as any,
            availableDatasets: [] as any,
            avroDatasets: [] as any,
            showMonitoringDialog: false
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
        routeToDataPreparation() {
            let path = ''
            this.$confirm.require({
                header: this.$t('managers.datasetManagement.openDP'),
                message: this.$t('managers.datasetManagement.confirmMsg'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.$router.push(path)
                }
            })
        },

        async getAllAvroDataSets() {
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/avro`)
                .then((response: AxiosResponse<any>) => {
                    this.avroDatasets = response.data
                })
                .catch(() => {})
        },

        async prepareForDataPreparation() {
            await this.getAllAvroDataSets()
            await this.openDataPreparation(this.selectedDataset)
        },

        isAvroReady(dsLabel: String) {
            if (this.avroDatasets.indexOf(dsLabel) >= 0) return true
            else return false
        },

        openDataPreparation(dataset: any) {
            if (dataset.dsTypeCd == 'Prepared') {
                //edit existing data prep
                this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/datasets/advanced/${dataset.label}`).then(
                    (response: AxiosResponse<any>) => {
                        let instanceId = response.data.configuration.dataPrepInstanceId
                        this.$http.get(process.env.VUE_APP_DATA_PREPARATION_PATH + `1.0/process/by-instance-id/${instanceId}`).then(
                            (response: AxiosResponse<any>) => {
                                let transformations = response.data.definition
                                let processId = response.data.id
                                let datasetLabel = response.data.instance.dataSetLabel
                                if (this.isAvroReady(datasetLabel))
                                    // check if Avro file has been deleted or not
                                    this.$router.push({ name: 'data-preparation', params: { id: datasetLabel, transformations: JSON.stringify(transformations), processId: processId, instanceId: instanceId, dataset: JSON.stringify(dataset) } })
                                else {
                                    this.$store.commit('setInfo', {
                                        title: 'Avro file is missing',
                                        msg: 'Generate it again and then retry'
                                    })
                                }
                            },
                            () => {
                                this.$store.commit('setError', { title: 'Save error', msg: 'Cannot create process' })
                            }
                        )
                    },
                    () => {
                        this.$store.commit('setError', {
                            title: 'Cannot open data preparation'
                        })
                    }
                )
            } else if (this.isAvroReady(dataset.label)) {
                // original dataset already exported in Avro
                this.$router.push({ name: 'data-preparation', params: { id: dataset.label } })
            } else {
                this.$store.commit('setInfo', {
                    title: 'Avro file is missing',
                    msg: 'Generate it again and then retry'
                })
            }
        }
    }
})
</script>
