<template>
  <ProgressBar mode="indeterminate" class="kn-progress-bar" :hidden="!load" data-test="related-docs-progress-bar"/>

  <DataTable
    :value="relatedDocumentsList"
    v-model:selection="selectedProduct1" 
    selectionMode="single"
    @rowSelect="onDocumentSelect"
    :paginator="true"
    :loading="load"
    :rows="20"
    class="p-datatable-sm kn-table"
    dataKey="id"
    v-model:filters="filters"
    filterDisplay="menu"
    :globalFilterFields="menuConfigurationRelatedDocumentsDescriptor.globalFilterFields"
    :rowsPerPageOptions="[10, 15, 20]"
    responsiveLayout="stack"
    breakpoint="960px"
    :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
    data-test="related-documents-list"
  >
    <template #header>
      <div class="table-header">
        <span class="p-input-icon-left">
          <i class="pi pi-search" />
          <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input"/>
        </span>
      </div>
    </template>
    <template #empty>
      {{ $t("common.info.noDataFound") }}
    </template>
    <template #loading>
      {{ $t("common.info.dataLoading") }}
    </template>

    <Column
      v-for="col of menuConfigurationRelatedDocumentsDescriptor.columns"
      :field="col.field"
      :header="$t(col.header)"
      :key="col.field"
      :style="menuConfigurationRelatedDocumentsDescriptor.table.column.style"
      :sortable="true"
      class="kn-truncated"
    >
      <template #filter="{ filterModel }">
        <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
      </template>
      <template #body="slotProps">
        <span :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
      </template>
    </Column>
  </DataTable>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FilterOperator } from "primevue/api";
import { filterDefault } from "@/helpers/commons/filterHelper";
import ProgressBar from "primevue/progressbar";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import MenuConfigurationRelatedDocumentsDescriptor from "./RelatedDocumentsDescriptor.json";
import axios from "axios";
import { iDocument } from "../MenuConfiguration";

export default defineComponent({
  name: "related-documents-list",
  components: {
    DataTable,
    Column,
    ProgressBar,
  },
  emits: ["selectedDocument"],
  props: {
    documents: Object,
    loading: Boolean,
  },
  watch: {
    loading: {
      handler: function (l) {
        this.load = l;
      },
    },
  },
  data() {
    return {
      apiUrl: process.env.VUE_APP_RESTFUL_SERVICES_PATH + "2.0/",
      load: false as Boolean,
      relatedDocumentsList: [] as iDocument[],
      filters: {
        global: [filterDefault],
        DOCUMENT_NAME: {
          operator: FilterOperator.AND,
          constraints: [filterDefault],
        },
        DOCUMENT_DESCR: {
          operator: FilterOperator.AND,
          constraints: [filterDefault],
        },
      } as Object,
      selectedDocument: null as iDocument | null,
      menuConfigurationRelatedDocumentsDescriptor:
        MenuConfigurationRelatedDocumentsDescriptor,
    };
  },
  async created() {
    await this.loadRelatedDocuments();
  },
  methods: {
    async loadRelatedDocuments() {
      this.load = true;
      await axios
        .get(this.apiUrl + "documents/listDocument")
        .then((response) => {
          this.relatedDocumentsList = response.data.item;
        })
        .finally(() => (this.load = false));
    },
    onDocumentSelect(event: any) {
      this.$emit("selectedDocument", event.data);
    },
  },
});
</script>
