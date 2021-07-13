<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <div class="p-col-9">
            <Card>
                <template #content>
                    <form class="p-fluid p-m-5">
                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText id="name" class="kn-material-input" type="text" v-model="target.name" @change="setDirty" />
                                <label for="name" class="kn-material-input-label">Name * </label>
                            </span>
                        </div>
                        <div class="kn-flex">
                            <div class="p-d-flex p-jc-between">
                                <span class="p-float-label">
                                    <Calendar id="startDate" class="kn-material-input" v-model="target.startValidity" :showIcon="true" :manualInput="false" @change="setDirty" />
                                    <label for="startnDate" class="kn-material-input-label"> Start Validity Date * </label>
                                </span>
                                <div class="p-d-flex">
                                    <span class="p-float-label">
                                        <Calendar id="endDate" class="kn-material-input" v-model="target.endValidity" :showIcon="true" :manualInput="false" @change="setDirty" />
                                        <label for="endDate" class="kn-material-input-label"> End Validity Date * </label>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </form>
                </template>
            </Card>
            <Card>
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('kpi.targetDefinition.applyTargetonKPI') }}
                        </template>
                    </Toolbar>
                </template>
                <template #footer>
                    <div class="table-footer">
                        <span class="p-input-icon-left">
                            <Button class="kn-button kn-button--secondary" @click="addKpiDialog()">{{ $t('kpi.targetDefinition.addKpiBtn') }}</Button>
                        </span>
                    </div>
                </template>
                <template #content>
                    <DataTable :value="kpi" :loading="loading" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack">
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <Column v-for="col of targetDefinitionDetailDecriptor.columns" :field="col.field" :header="col.header" :key="col.field" class="kn-truncated"> </Column
                        ><Column>
                            <template #body="slotProps">
                                <Button icon="pi pi-trash" class="p-button-link" @click="deleteKpi(slotProps.data)" />
                            </template>
                        </Column>
                    </DataTable>
                </template>
            </Card>
        </div>
    </div>
    <Dialog :visible="kpiDialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0">
                <template #right>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="addKpi" />
                    <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
                </template>
            </Toolbar>
        </template>
        <DataTable :paginator="true" :rows="20" :rowsPerPageOptions="[10, 15, 20]" v-model:selection="selectedKpi" :value="allKpi" :loading="loadingAllKpi" class="p-datatable-sm kn-table" dataKey="kpiId" responsiveLayout="stack">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>

            <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
            <Column v-for="col of targetDefinitionDetailDecriptor.columnsAllKPI" :field="col.field" :header="col.header" :key="col.field" class="kn-truncated"> </Column>
        </DataTable>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iTargetDefinition } from './TargetDefinition'
import { formatDate } from '@/helpers/commons/localeHelper'
import targetDefinitionDetailDecriptor from './TargetDefinitionDetailDescriptor.json'
import Calendar from 'primevue/calendar'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import axios from 'axios'
export default defineComponent({
    name: 'target-definition-detail',
    components: {
        Calendar,
        DataTable,
        Column,
        Dialog
    },
    props: {
        model: {
            type: Object,
            required: false
        }
    },
    data() {
        return {
            target: {} as iTargetDefinition,
            formatDate: formatDate,
            targetDefinitionDetailDecriptor: targetDefinitionDetailDecriptor,
            kpi: [] as any,
            allKpi: [] as any,
            selectedKpi: [] as any,
            loading: false,
            loadingAllKpi: false,
            kpiDialogVisible: false
        }
    },
    watch: {
        model() {
            this.target = { ...this.model } as iTargetDefinition
            this.loadKPI(this.target.id)
        }
    },
    mounted() {
        if (this.model) {
            this.target = { ...this.model } as iTargetDefinition
            this.loadKPI(this.target.id)
        }
    },
    methods: {
        async loadKPI(id: any) {
            this.loading = true
            this.kpi = []
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpiee/' + id + '/listKpiWithTarget')
                .then((response) =>
                    response.data.map((kpi: any) => {
                        this.kpi.push({
                            kpiId: kpi.kpiId,
                            kpiName: kpi.kpi.name,
                            kpiVersion: kpi.kpi.version,
                            targetId: kpi.targetId,
                            value: kpi.value
                        })
                    })
                )
                .finally(() => (this.loading = false))
        },
        async loadKpi() {
            this.loadingAllKpi = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listKpi')
                .then((response) =>
                    response.data.map((kpi: any) => {
                        this.allKpi.push({
                            kpiId: kpi.id,
                            kpiName: kpi.name,
                            kpiVersion: kpi.version,
                            kpiCategory: kpi.category?.valueName,
                            kpiDate: new Date(kpi.dateCreation),
                            kpiAuthor: kpi.author
                        })
                    })
                )
                .finally(() => (this.loadingAllKpi = false))
        },
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
            console.log('dirty')
        },
        deleteKpi(kpiSelected: any) {
            this.kpi.splice(this.kpi.indexOf(kpiSelected), 1)
        },
        addKpiDialog() {
            console.log('addKpiModal')
            this.loadKpi()
            this.kpiDialogVisible = true
        },
        addKpi() {
            this.selectedKpi.map((kpi: any) => {
                this.kpi.push({
                    kpiId: kpi.kpiId,
                    kpiName: kpi.kpiName,
                    kpiVersion: kpi.kpiVersion,
                    targetId: this.target.id,
                    value: 0
                })
            })
            this.kpiDialogVisible = false
            console.log(this.kpi)
        },
        kpiSelected() {
            console.log(this.selectedKpi)
        }
    }
})
</script>
