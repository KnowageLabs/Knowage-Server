<template>
    <Dialog :header="$t('managers.driversManagement.useModes.selectLov')" :breakpoints="useModeDescriptor.dialog.breakpoints" :style="useModeDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.driversManagement.useModes.selectLov') }}
                </template>
            </Toolbar>
        </template>
        <DataTable v-model:selection="selectedLov" :value="lovs" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" selectionMode="single">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #loading>
                {{ $t('common.info.dataLoading') }}
            </template>

            <Column v-for="col of useModeDescriptor.columnsLov" :field="col.field" :header="$t(col.header)" :key="col.field" class="kn-truncated">
                <template #body="slotProps">
                    <span v-if="!col.dateField">{{ slotProps.data[slotProps.column.props.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <Button :label="$t('common.cancel')" @click="closeLovDialog" class="kn-button kn-button--secondary" />
            <Button :label="$t('common.apply')" class="kn-button kn-button--primary" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import useModeDescriptor from './UseModesDescriptor.json'
export default defineComponent({
    name: 'lovs-dialog',
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
        lovs: {
            type: Array,
            required: false
        },
        selectedLovProp: {
            type: Array,
            required: false
        }
    },
    emits: ['close', 'add'],
    data() {
        return {
            selectedLov: {} as any,
            useModeDescriptor
        }
    },
    methods: {
        addLov() {
            this.$emit('add', this.selectedLov)
        },
        closeLovDialog() {
            this.$emit('close')
        }
    }
})
</script>
