<template>
    <Card>
        <template #content>
            <div id="expired-schedule" v-if="showExpired && expired" data-test="expired-warning">
                <p>{{ $t('kpi.kpiScheduler.expiredInterval') }}</p>
                <i class="fa fa-times-circle" @click="showExpired = false"></i>
            </div>
            <DataTable :value="kpisList" class="p-datatable-sm kn-table" dataKey="id" v-model:filters="filters" :globalFilterFields="kpiCardDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="960px" data-test="kpi-table">
                <template #header>
                    <div class="table-header p-d-flex p-ai-center">
                        <span id="search-container" class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                        <Button id="add-kpi-associations-button" class="kn-button kn-button--primary" :label="$t('kpi.kpiScheduler.addKpiAssociation')" @click="addKpiAssociationVisible = true"></Button>
                    </div>
                </template>
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <Column class="kn-truncated" v-for="col of kpiCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
                <Column :style="kpiCardDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteKpiAssociationConfirm(slotProps.data.id)" :data-test="'delete-button-' + slotProps.data.id" />
                    </template>
                </Column>
            </DataTable>

            <Dialog :style="kpiCardDescriptor.dialog.style" :visible="addKpiAssociationVisible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                        <template #start>
                            {{ $t('kpi.kpiScheduler.saveKpiAssociation') }}
                        </template>
                        <template #end>
                            <Button class="kn-button p-button-text p-m-2" :label="$t('common.close')" @click="closeKpiAssociations"></Button>
                            <Button class="kn-button p-button-text" :label="$t('common.save')" @click="addKpiAssociations"></Button>
                        </template>
                    </Toolbar>
                </template>
                <DataTable :value="allKpiList" v-model:selection="selectedKpiAssociations" class="p-datatable-sm kn-table" dataKey="id" v-model:filters="filters" :globalFilterFields="kpiCardDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="960px">
                    <template #header>
                        <div class="table-header p-d-flex ">
                            <span class="p-input-icon-left p-mr-3">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <Column selectionMode="multiple" headerStyle="width: 3em"></Column>
                    <Column class="kn-truncated" field="name" :header="$t('kpi.kpiScheduler.kpiName')" key="name" :sortable="true"> </Column>
                    <Column class="kn-truncated" field="category.valueCd" :header="$t('common.category')" key="category.valueCd" :sortable="true"> </Column>
                    <Column class="kn-truncated" field="dateCreation" :header="$t('kpi.kpiScheduler.kpiName')" key="dateCreation" :sortable="true">
                        <template #body="slotProps">
                            <span>{{ getFormattedDate(slotProps.data.dateCreation) }}</span>
                        </template>
                    </Column>
                    <Column class="kn-truncated" field="author" :header="$t('common.author')" key="author" :sortable="true"> </Column>
                </DataTable>
            </Dialog>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { filterDefault } from '@/helpers/commons/filterHelper'
    import { formatDate } from '@/helpers/commons/localeHelper'
    import { iKpi } from '../../KpiScheduler'
    import Card from 'primevue/card'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import Dialog from 'primevue/dialog'
    import kpiCardDescriptor from './KpiSchedulerKpiCardDescriptor.json'

    export default defineComponent({
        name: 'kpi-scheduler-kpi-card',
        components: { Card, Column, DataTable, Dialog },
        props: { expired: { type: Boolean }, kpis: { type: Array }, allKpiList: { type: Array } },
        emits: ['touched', 'kpiAdded', 'kpiDeleted'],
        data() {
            return {
                kpiCardDescriptor,
                kpisList: [] as iKpi[],
                filters: { global: [filterDefault] } as Object,
                selectedKpiAssociations: [] as iKpi[],
                addKpiAssociationVisible: false,
                showExpired: true
            }
        },
        created() {
            this.loadKpis()
            this.loadSelectedKpiAssociations()
        },
        methods: {
            loadKpis() {
                this.kpisList = this.kpis as iKpi[]
            },
            loadSelectedKpiAssociations() {
                if (this.kpisList) {
                    this.selectedKpiAssociations = [...this.kpisList] as iKpi[]
                }
            },
            deleteKpiAssociationConfirm(id: number) {
                this.$confirm.require({
                    message: this.$t('common.toast.deleteMessage'),
                    header: this.$t('common.toast.deleteTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.deleteKpiAssociation(id)
                })
            },
            deleteKpiAssociation(id: number) {
                const index = this.kpisList.findIndex((kpi: iKpi) => kpi.id === id)
                if (index > -1) {
                    const kpi = this.kpisList[index]
                    this.kpisList.splice(index, 1)
                    this.$emit('kpiDeleted', kpi)
                }
                this.loadSelectedKpiAssociations()
            },
            addKpiAssociations() {
                this.kpisList = [...this.selectedKpiAssociations]
                this.addKpiAssociationVisible = false
                this.$emit('kpiAdded', this.kpisList)
            },
            getFormattedDate(date: any) {
                return formatDate(date, 'YYYY-MM-DD')
            },
            closeKpiAssociations() {
                this.addKpiAssociationVisible = false
                this.loadSelectedKpiAssociations()
            }
        }
    })
</script>

<style lang="scss" scoped>
    ::v-deep(.kn-toolbar) {
        width: 100%;
    }

    ::v-deep(.p-toolbar-group-right) {
        height: 100%;
    }

    #search-container {
        flex: 0.8;
    }

    #add-kpi-associations-button {
        flex: 0.2;
        height: 2.3rem;
    }

    #expired-schedule {
        font-size: 0.8rem;
        text-transform: uppercase;
        display: flex;
        justify-content: center;
        align-items: center;
        border: 1px solid rgba(251, 192, 45, 0.5);
        background-color: #fef5dc;
        p {
            margin: 0.3rem;
        }
    }
</style>
