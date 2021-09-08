<template>
    <Dialog :header="$t('managers.driversManagement.useModes.selectLov')" :breakpoints="useModeDescriptor.dialog.breakpoints" :style="useModeDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.driversManagement.useModes.selectLov') }}
                </template>
            </Toolbar>
        </template>
        <DataTable v-if="!detailVisiable" v-model:selection="selectedLov" :value="lovs" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" selectionMode="single">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>

            <Column v-for="col of useModeDescriptor.columnsLov" :field="col.field" :header="$t(col.header)" :key="col.field" class="kn-truncated">
                <template #body="slotProps">
                    <span>{{ slotProps.data[slotProps.column.props.field] }}</span>
                </template>
            </Column>
            <Column headerStyle="useModeDescriptor.table.iconColumn.style" :style="useModeDescriptor.table.iconColumn.style">
                <template #body="slotProps">
                    <Button icon="pi pi-info-circle" class="p-button-link" @click="lovDetail(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
        <LovsDetail v-else :lov="lovDetails" @close="detailVisiable = false"></LovsDetail>
        <template #footer>
            <Button :label="$t('common.cancel')" @click="closeLovDialog" class="kn-button kn-button--secondary" />
            <Button :label="$t('common.apply')" @click="applyLov" class="kn-button kn-button--primary" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import useModeDescriptor from './UseModesDescriptor.json'
import LovsDetail from './DriversManagementLovsDetail.vue'
export default defineComponent({
    name: 'lovs-dialog',
    components: {
        DataTable,
        Column,
        Dialog,
        LovsDetail
    },
    props: {
        dialogVisible: {
            type: Boolean,
            default: false
        },
        lovs: {
            type: Array,
            required: false
        },
        selectedLovProp: {
            type: Array,
            required: false
        }
    },
    emits: ['close', 'apply'],
    data() {
        return {
            selectedLov: {} as any,
            detailVisiable: false,
            lovDetails: {} as any,
            useModeDescriptor
        }
    },
    mounted() {
        this.selectedLov = { ...this.selectedLovProp }
    },
    watch: {
        selectedLovProp() {
            this.selectedLov = { ...this.selectedLovProp }
        }
    },
    methods: {
        applyLov() {
            this.$emit('apply', this.selectedLov)
            this.detailVisiable = false
        },
        closeLovDialog() {
            this.$emit('close')
            this.detailVisiable = false
        },
        lovDetail(lov: any) {
            this.detailVisiable = true
            this.lovDetails = lov
        }
    }
})
</script>
