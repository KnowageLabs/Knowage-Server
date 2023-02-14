<template>
    <Card v-if="data" class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('kpi.kpiDocumentDesigner.kpiList') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div>
                <DataTable :value="data.kpi" class="p-datatable-sm kn-table" dataKey="name" v-model:filters="filters" :globalFilterFields="KpiDocumentDesignerKpiListCardDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="960px" :scrollable="true" scroll-height="60vh">
                    <template #header>
                        <div class="table-header p-d-flex p-ai-center">
                            <span id="search-container" class="p-input-icon-left p-mr-3">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                            </span>
                            <Button id="kpi-edit-add-kpi-associations-button" class="kn-button kn-button--primary" :label="$t('kpi.kpiScheduler.addKpiAssociation')" @click="addKpiAssociationVisible = true"></Button>
                        </div>
                    </template>

                    <template #empty>{{ $t('common.info.noDataFound') }}</template>

                    <Column class="kn-truncated" v-for="col of KpiDocumentDesignerKpiListCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" :sortable="true"> </Column>

                    <Column v-if="showSaveAsColumn" field="vieweas" :header="$t('kpi.kpiDocumentDesigner.viewAs')" key="vieweas" :sortable="true" :style="KpiDocumentDesignerKpiListCardDescriptor.columnStyle">
                        <template #body="slotProps">
                            <Dropdown class="kpi-edit-kpi-list-card-dropdown" v-model="slotProps.data[slotProps.column.props.field]" :options="KpiDocumentDesignerKpiListCardDescriptor.viewAsOptions" optionValue="value" :placeholder="$t('kpi.kpiDocumentDesigner.viewAsPlaceholder')">
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value === 'speedometer' ? $t('kpi.kpiDocumentDesigner.speedometer') : $t('kpi.kpiDocumentDesigner.kpiCard') }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <span>{{ $t(slotProps.option.label) }}</span>
                                </template>
                            </Dropdown>
                        </template>
                    </Column>

                    <Column field="rangeMinValue" :header="$t('kpi.kpiDocumentDesigner.rangeMinValue')" key="rangeMinValue" :sortable="true" :style="KpiDocumentDesignerKpiListCardDescriptor.columnStyle">
                        <template #body="slotProps">
                            <div class="p-d-flex p-flex-row p-ai-center">
                                <InputText class="kn-material-input p-mr-2 kn-flex" type="number" v-model="slotProps.data[slotProps.column.props.field]" :class="{ 'p-invalid': !slotProps.data[slotProps.column.props.field] }" />
                                <i class="pi pi-pencil edit-icon kn-flex" />
                            </div>
                        </template>
                    </Column>

                    <Column field="rangeMaxValue" :header="$t('kpi.kpiDocumentDesigner.rangeMaxValue')" key="rangeMaxValue" :sortable="true" :style="KpiDocumentDesignerKpiListCardDescriptor.columnStyle">
                        <template #body="slotProps">
                            <div class="p-d-flex p-flex-row p-ai-center">
                                <InputText class="kn-material-input p-mr-2 kn-flex" type="number" v-model="slotProps.data[slotProps.column.props.field]" :class="{ 'p-invalid': !slotProps.data[slotProps.column.props.field] }" />
                                <i class="pi pi-pencil edit-icon kn-flex" />
                            </div>
                        </template>
                    </Column>

                    <Column field="prefixSuffixValue" :header="$t('kpi.kpiDocumentDesigner.prefixSuffixLabel')" key="prefixSuffixValue" :sortable="true" :style="KpiDocumentDesignerKpiListCardDescriptor.columnStyle">
                        <template #body="slotProps">
                            <div class="p-d-flex p-flex-row p-ai-center">
                                <InputText class="kn-material-input p-mr-2 kn-flex" v-model="slotProps.data[slotProps.column.props.field]" maxLength="3" />
                                <i class="pi pi-pencil edit-icon kn-flex" />
                            </div>
                        </template>
                    </Column>

                    <Column field="isSuffix" :header="$t('kpi.kpiDocumentDesigner.showLabelAs')" key="isSuffix" :sortable="true" :style="KpiDocumentDesignerKpiListCardDescriptor.columnStyle">
                        <template #body="slotProps">
                            <Dropdown v-model="slotProps.data[slotProps.column.props.field]" :options="KpiDocumentDesignerKpiListCardDescriptor.prefixSuffixOptions" optionValue="value">
                                <template #value="slotProps">
                                    <div v-if="slotProps.value">
                                        <span>{{ slotProps.value === 'true' ? $t('kpi.kpiDocumentDesigner.suffix') : $t('kpi.kpiDocumentDesigner.prefix') }}</span>
                                    </div>
                                </template>
                                <template #option="slotProps">
                                    <span>{{ $t(slotProps.option.label) }}</span>
                                </template>
                            </Dropdown>
                        </template>
                    </Column>

                    <Column :style="KpiDocumentDesignerKpiListCardDescriptor.iconColumnStyle">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteKpiAssociationConfirm(slotProps.data)" />
                        </template>
                    </Column>
                </DataTable>
            </div>

            <KpiDocumentDesignerKpiSelectDialog :kpiList="kpiList" :visible="addKpiAssociationVisible" :data="data" @close="addKpiAssociationVisible = false" @kpiSelected="onKpiSelected"></KpiDocumentDesignerKpiSelectDialog>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iKpi, iKpiListItem } from '../KpiDocumentDesigner'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import KpiDocumentDesignerKpiListCardDescriptor from './KpiDocumentDesignerKpiListCardDescriptor.json'
import KpiDocumentDesignerKpiSelectDialog from './KpiDocumentDesignerKpiSelectDialog.vue'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'kpi-edit-kpi-list-card',
    components: { Card, Column, DataTable, Dropdown, KpiDocumentDesignerKpiSelectDialog },
    props: { propData: { type: Object }, kpiList: { type: Array as PropType<iKpi[]> }, documentType: { type: String } },
    data() {
        return {
            KpiDocumentDesignerKpiListCardDescriptor,
            data: { kpi: [] } as { kpi: iKpiListItem[] },
            filters: { global: [filterDefault] } as Object,
            addKpiAssociationVisible: false,
            showSaveAsColumn: false
        }
    },
    watch: {
        propData() {
            this.loadData()
        },
        documentType() {
            this.setShowSaveAsColumn()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.data = { kpi: [] }
            if (!this.propData) return
            this.data = Array.isArray(this.propData.kpi) ? (this.propData as { kpi: iKpiListItem[] }) : { kpi: [this.propData.kpi] }
            this.setShowSaveAsColumn()
        },
        setShowSaveAsColumn() {
            this.showSaveAsColumn = this.documentType === 'widget'
        },
        onKpiSelected(selectedKpi: iKpi[]) {
            const temp = deepcopy(this.data.kpi) as any[]
            this.data.kpi = []
            selectedKpi.forEach((kpi: iKpi) => {
                const index = temp.findIndex((tempKpi: iKpi) => tempKpi.name === kpi.name)
                if (index !== -1) {
                    this.data.kpi.push(temp[index])
                } else {
                    this.data.kpi.push({
                        isSuffix: 'false',
                        name: kpi.name,
                        prefixSuffixValue: '',
                        rangeMaxValue: '',
                        rangeMinValue: '',
                        vieweas: 'Speedometer',
                        category: kpi.category ? kpi.category.valueName : ''
                    })
                }
            })
            this.addKpiAssociationVisible = false
        },
        deleteKpiAssociationConfirm(kpi: iKpiListItem) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.deleteKpiAssociation(kpi)
                }
            })
        },
        deleteKpiAssociation(kpi: iKpiListItem) {
            const index = this.data.kpi.findIndex((tempKpi: iKpiListItem) => tempKpi.name === kpi.name)
            if (index !== -1) this.data.kpi.splice(index, 1)
        }
    }
})
</script>

<style lang="scss" scoped>
#kpi-edit-add-kpi-associations-button {
    flex: 0.15;
    height: 2.3rem;
    margin-left: auto;
    min-width: 150px;
}

.kpi-edit-kpi-list-card-dropdown {
    min-width: 150px;
    max-width: 200px;
}
</style>
