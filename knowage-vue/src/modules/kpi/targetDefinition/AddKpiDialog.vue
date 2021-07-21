<template>
    <Dialog :header="$t('kpi.targetDefinition.addKpiBtn')" :breakpoints="targetDefinitionDetailDescriptor.dialog.breakpoints" :style="targetDefinitionDetailDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('kpi.targetDefinition.addKpiBtn') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="addKpi" />
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeKpiDialog" />
                </template>
            </Toolbar>
        </template>
        <DataTable
            :paginator="true"
            :rows="15"
            :rowsPerPageOptions="[10, 15, 20]"
            v-model:selection="selectedKpi"
            :value="kpi"
            :loading="loadingKpi"
            class="p-datatable-sm kn-table"
            dataKey="kpiId"
            responsiveLayout="stack"
            v-model:filters="filters"
            filterDisplay="menu"
            :globalFilterFields="targetDefinitionDetailDescriptor.globalFilterFields"
        >
            <template #header>
                <div class="table-header">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>

            <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
            <Column v-for="col of targetDefinitionDetailDescriptor.columnsAllKPI" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" class="kn-truncated">
                <template #body="slotProps">
                    <span v-if="!col.dateField">{{ slotProps.data[slotProps.column.props.field] }}</span>
                    <span v-else>{{ formatDate(slotProps.data[slotProps.column.props.field]) }}</span>
                </template>
            </Column>
        </DataTable>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import { iValues } from './TargetDefinition'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import targetDefinitionDetailDescriptor from './TargetDefinitionDetailDescriptor.json'
import { formatDate } from '@/helpers/commons/localeHelper'

export default defineComponent({
    name: 'add-kpi-dialog',
    components: {
        DataTable,
        Column,
        Dialog
    },
    props: {
        dialogVisible: {
            type: Boolean,
            default: false
        },
        kpi: {
            type: Array as PropType<iValues[]>,
            required: false
        },
        loadingKpi: {
            type: Boolean,
            default: false
        }
    },
    emits: ['close', 'add'],
    data() {
        return {
            selectedKpi: [] as iValues[],
            targetDefinitionDetailDescriptor: targetDefinitionDetailDescriptor,
            filters: {
                global: [filterDefault],
                kpiName: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                kpiCategory: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                domainCode: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                kpiDate: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                kpiAuthor: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object,
            formatDate: formatDate
        }
    },
    methods: {
        addKpi() {
            this.$emit('add', this.selectedKpi)
            this.selectedKpi = []
        },
        closeKpiDialog() {
            this.$emit('close')
        }
    }
})
</script>
