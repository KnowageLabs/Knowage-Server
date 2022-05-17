<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <Button :label="$t('managers.datasetManagement.monitoring')" class="kn-button kn-button--primary" @click="showMonitoringDialog = true" />
                </div>
                <div class="p-field p-col-6">
                    <Button :label="$t('managers.datasetManagement.openDP')" class="kn-button kn-button--primary" @click="routeToDataPreparation" />
                </div>
            </form>
        </template>
    </Card>

    <MonitoringDialog :visibility="showMonitoringDialog" :dataset="selectedDataset" @close="showMonitoringDialog = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
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
        }
    }
})
</script>
