<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.cacheManagement.addRemoveDataset') }}
                </template>
                <template #end>
                    <Button class="kn-button p-button-text p-button-rounded" :disabled="cleanAllDisabled" @click="cleanAllConfirm" data-test="clean-all-button">{{ $t('managers.cacheManagement.cleanAll') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable :value="datasets" :loading="loading" class="p-datatable-sm kn-table" dataKey="signature" responsiveLayout="stack" breakpoint="960px" data-test="dataset-table">
                <template #empty>
                    {{ $t('managers.cacheManagement.metadataUnavailable') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>
                <Column v-for="col of datasetTableCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" class="kn-truncated"> </Column>
                <Column :style="datasetTableCardDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteDatasetConfirm(slotProps.data.signature)" data-test="delete-button" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMeta } from '../../CacheManagement'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import datasetTableCardDescriptor from './DatasetTableCardDescriptor.json'

export default defineComponent({
    name: 'dataset-table-card',
    components: {
        Card,
        Column,
        DataTable
    },
    props: {
        datasetMetadataList: {
            type: Array,
            required: true
        },
        loading: {
            type: Boolean
        }
    },
    emits: ['deleted'],
    data() {
        return {
            datasetTableCardDescriptor,
            datasets: [] as iMeta[]
        }
    },
    computed: {
        cleanAllDisabled(): boolean {
            return this.datasets.length === 0
        }
    },
    watch: {
        datasetMetadataList() {
            this.loadDatasets()
        }
    },
    created() {
        this.loadDatasets()
    },
    methods: {
        loadDatasets() {
            this.datasets = this.datasetMetadataList as iMeta[]
        },
        cleanAllConfirm() {
            this.$confirm.require({
                message: this.$t('managers.cacheManagement.cleanAllMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.cleanAll()
            })
        },
        async cleanAll() {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/cacheee').then(() => this.emitDeleteSuccess())
        },
        deleteDatasetConfirm(signature: string) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDataset(signature)
            })
        },
        async deleteDataset(signature: string) {
            await this.$http.put(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/cacheee/deleteItems', { namesArray: [signature] }).then(() => this.emitDeleteSuccess())
        },
        emitDeleteSuccess() {
            this.$store.commit('setInfo', {
                title: this.$t('common.toast.deleteTitle'),
                msg: this.$t('common.toast.deleteSuccess')
            })
            this.$emit('deleted')
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
