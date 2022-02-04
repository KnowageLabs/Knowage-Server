<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('kpi.targetDefinition.applyTargetonKPI') }}
                </template>
            </Toolbar>
        </template>
        <template #footer>
            <div class="p-d-inline-flex">
                <Button class="kn-button kn-button--secondary " @click="addKpiDialog()">{{ $t('kpi.targetDefinition.addKpiBtn') }}</Button>
            </div>
        </template>
        <template #content>
            <DataTable :value="selectedKpi" :loading="loadingKpi" class="editable-cells-table" dataKey="id" editMode="cell" responsiveLayout="stack" breakpoint="960px" data-test="selected-kpi-table">
                <template #empty>
                    {{ $t('common.info.noElementSelected') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>

                <Column field="kpiName" :header="$t('kpi.targetDefinition.kpiName')" key="kpiName" :sortable="true" class="kn-truncated" :headerStyle="targetDefinitionDetailDecriptor.table.column.style"></Column>
                <Column field="value" :header="$t('kpi.targetDefinition.kpiValue')" key="value" :sortable="true" class="kn-truncated" :headerStyle="targetDefinitionDetailDecriptor.table.column.style">
                    <template #body="slotProps"> {{ slotProps.data[slotProps.column.props.field] }} <i class="pi pi-pencil"></i> </template>
                    <template #editor="slotProps">
                        <InputNumber v-model="slotProps.data[slotProps.column.props.field]" showButtons mode="decimal" :minFractionDigits="2" />
                    </template>
                </Column>
                <Column headerStyle="targetDefinitionDetailDecriptor.table.iconColumn.style" :style="targetDefinitionDetailDecriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteKpi(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iValues } from './TargetDefinition'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputNumber from 'primevue/inputnumber'
import targetDefinitionDetailDecriptor from './TargetDefinitionDetailDescriptor.json'
export default defineComponent({
    name: 'apply-target-card',
    components: {
        DataTable,
        Column,
        InputNumber
    },
    props: {
        loadingKpi: {
            type: Boolean,
            default: false
        },
        kpi: {
            type: Array as PropType<iValues[]>,
            required: true
        }
    },
    data() {
        return {
            selectedKpi: [] as iValues[],
            targetDefinitionDetailDecriptor: targetDefinitionDetailDecriptor
        }
    },
    watch: {
        kpi() {
            this.selectedKpi = this.kpi
        }
    },
    methods: {
        addKpiDialog() {
            this.$emit('showDialog')
        },
        deleteKpi(selected: iValues) {
            const index = this.selectedKpi.findIndex((selectedKpi) => {
                return selectedKpi.kpiId === selected.kpiId
            })
            if (index >= 0) {
                this.selectedKpi.splice(index, 1)
                this.$emit('kpiChanged', this.selectedKpi)
            }
        }
    }
})
</script>
<style scoped>
::v-deep(.editable-cells-table td.p-cell-editing) {
    padding-top: 0;
    padding-bottom: 0;
}
</style>
