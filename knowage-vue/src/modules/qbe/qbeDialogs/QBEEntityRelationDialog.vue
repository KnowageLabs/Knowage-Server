<template>
    <Dialog id="qbe-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="relationDescriptor.entityRelation.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ propEntity.text }}
                </template>
            </Toolbar>
        </template>

        <DataTable class="p-datatable-sm kn-table p-m-2" :value="propEntity.relation" responsive-layout="scroll" striped-rows>
            <template #empty>
                {{ $t('qbe.entities.relationDialog.noRelations') }}
            </template>
            <Column v-for="column in relationDescriptor.entityRelation.columns" :key="column.field" class="kn-truncated" :field="column.field" :header="$t(column.header)" :sortable="true" />
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.ok') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import relationDescriptor from './QBEDialogsDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Dialog, DataTable, Column },
    props: { propEntity: Object, visible: Boolean },
    data() {
        return {
            relationDescriptor
        }
    }
})
</script>
