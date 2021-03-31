<template>
  <Dialog class="kn-dialog--toolbar--primary roleDialog" v-bind:visible="visibility" footer="footer" :header="$t('downloadsDialog.title')" :closable="false" modal>
    <DataTable :value="rowData" style="width:800px" :resizableColumns="true" columnResizeMode="fit | expand">
      <Column v-for="(column, index) in columnDefs" v-bind:key="index" :field="column.field" :header="$t(column.headerName)" :bodyStyle="column.bodyStyle">
        <template v-if="column.template" #body="slotProps">
          <Button icon="pi pi-download" class="p-button-text p-button-rounded p-button-plain" @click="downloadTemplate(slotProps.data.id)" />
        </template>
        <template v-else #body="slotProps"> {{ slotProps.data[column.field] }} </template>
      </Column>
    </DataTable>
    <template #footer>
      <Button class="kn-button kn-button--primary" @click="closeDialog">{{ $t('common.close') }}</Button>
    </template>
  </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import descriptor from './DownloadsDialogDescriptor.json'

export default defineComponent({
  name: 'role-dialog',
  components: {
    Column,
    DataTable,
    Dialog
  },
  props: {
    visibility: Boolean
  },
  data() {
    return {
      columnDefs: {},
      rowData: new Array(),
      gridOptions: {}
    }
  },
  beforeMount() {
    this.gridOptions = { headerHeight: 30 }
    this.columnDefs = descriptor.columnDefs

    this.rowData = [
      { id: 0, filename: 'Test File', model: 'Celica', filesize: 35000 },
      { id: 1, filename: 'Ford', model: 'Mondeo', filesize: 32000 },
      { id: 2, filename: 'Porsche', model: 'Boxter', filesize: 72000 }
    ]
  },
  emits: ['update:visibility'],
  methods: {
    closeDialog() {
      this.$emit('update:visibility', false)
    },
    downloadTemplate(index) {
      alert(index)
    }
  }
})
</script>
