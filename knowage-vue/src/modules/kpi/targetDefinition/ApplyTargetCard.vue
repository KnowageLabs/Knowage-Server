<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
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
            <DataTable :value="kpi" :loading="loadingKpi" class="editable-cells-table" dataKey="id" responsiveLayout="stack" editMode="cell" :scrollable="true" scrollHeight="400px" data-test="selected-kpi-table">
                <template #empty>
                    {{ $t('common.info.noElementSelected') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>

                <Column field="kpiName" :header="$t('kpi.targetDefinition.kpiName')" key="kpiName" :sortable="true" class="kn-truncated" :style="targetDefinitionDetailDecriptor.table.column.style"></Column>
                <Column field="value" :header="$t('kpi.targetDefinition.kpiValue')" key="value" :sortable="true" class="kn-truncated" :style="targetDefinitionDetailDecriptor.table.column.style">
                    <template #body="slotProps">
                        {{ slotProps.data[slotProps.column.props.field] }}
                    </template>
                    <template #editor="slotProps">
                        <InputNumber v-model="slotProps.data[slotProps.column.props.field]" showButtons />
                    </template>
                </Column>
                <Column :style="targetDefinitionDetailDecriptor.table.iconColumn.style">
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
            required: false
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
            this.selectedKpi = this.kpi?.map((o) => ({ ...o })) as iValues[]
        }
    },
    methods: {
        addKpiDialog() {
            this.$emit('showDialog')
        },
        deleteKpi(selected: iValues) {
            console.log(selected)
            this.selectedKpi.splice(this.selectedKpi.indexOf(selected), 1)
            this.$emit('kpiChanged', this.selectedKpi)
        }
    }
})
</script>
