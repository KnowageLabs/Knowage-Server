<template>
  <Dialog :style="workspaceDataPreviewDialogDescriptor.dialog.style" :content-style="workspaceDataPreviewDialogDescriptor.dialog.contentStyle" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
    <template #header>
      <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12" :style="mainDescriptor.style.maxWidth">
        <template #start>
          <span>{{ dataset?.label }}</span>
        </template>
        <template #end>
          <Button class="kn-button p-button-text p-button-plain" :label="$t('common.close')" @click="closeDialog"></Button>
        </template>
      </Toolbar>
    </template>

    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar p-ml-2" data-test="progress-bar" />

    <div class="p-d-flex p-flex-column kn-flex workspace-scrollable-table">
      <Message v-if="errorMessageVisible" class="p-m-2" severity="warn" :closable="false" :style="mainDescriptor.style.message">
        {{ errorMessage }}
      </Message>

      <DatasetPreviewTable v-else class="p-d-flex p-flex-column kn-flex p-m-2" :preview-columns="columns" :preview-rows="rows" :pagination="pagination" :preview-type="previewType" @pageChanged="updatePagination($event)" @sort="onSort" @filter="onFilter"></DatasetPreviewTable>
    </div>
  </Dialog>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { AxiosResponse } from "axios";
import Dialog from "primevue/dialog";
import DatasetPreviewTable from "@/modules/workspace/views/dataView/tables/DatasetPreviewTable.vue";
import Message from "primevue/message";
import mainDescriptor from "@/modules/workspace/WorkspaceDescriptor.json";
import workspaceDataPreviewDialogDescriptor from "@/modules/workspace/views/dataView/dialogs/WorkspaceDataPreviewDialogDescriptor.json";

import deepcopy from "deepcopy";

export default defineComponent({
  name: "kpi-scheduler-save-dialog",
  components: { Dialog, DatasetPreviewTable, Message },
  props: { visible: { type: Boolean }, propDataset: { type: Object }, previewType: String, dashboardId: { type: String, required: true } },
  emits: ["close"],
  data() {
    return {
      mainDescriptor,
      workspaceDataPreviewDialogDescriptor,
      dataset: null as any,
      columns: [] as any[],
      rows: [] as any[],
      pagination: { start: 0, limit: 15 } as any,
      sort: null as any,
      filter: null as any,
      errorMessageVisible: false,
      errorMessage: "",
      loading: false,
      filtersData: {} as any,
      correctRolesForExecution: null,
    };
  },
  computed: {},
  watch: {
    async propDataset() {
      if (this.visible) await this.loadPreview();
    },
    async visible(value) {
      if (value) await this.loadPreview();
    },
    previewType() {},
  },
  async created() {
    await this.loadPreview();
  },
  methods: {
    async loadPreview() {
      this.loadDataset();
      await this.loadPreviewData();
    },
    loadDataset() {
      this.dataset = this.propDataset as any;
    },
    async loadPreviewData() {
      this.loading = true;
      const postData = { ...this.pagination };

      if (this.sort) postData.sorting = this.sort;
      if (this.filter) postData.filters = this.filter;

      if (this.dataset.pars && this.dataset.pars.length > 0) {
        postData.pars = deepcopy(this.dataset.pars);
        postData.pars.forEach((param: any) => {
          param.value = param.value ?? param.defaultValue;
        });
      }

      if (this.dataset.drivers?.length > 0) {
        const formattedDrivers = {};
        this.dataset.drivers.forEach((filter: any) => {
          formattedDrivers[filter.urlName] = filter.parameterValue;
        });
        postData.drivers = formattedDrivers;
      }

      await this.$http
        .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${this.dataset.label}/preview`, postData, { headers: { "X-Disable-Errors": "true" } })
        .then((response: AxiosResponse<any>) => {
          this.setPreviewColumns(response.data);
          this.rows = response.data.rows;
          this.pagination.size = response.data.results;
        })
        .catch((error) => {
          this.errorMessage = error.message;
          this.errorMessageVisible = true;
        });
      this.loading = false;
    },
    async updatePagination(lazyParams: any) {
      this.pagination.start = lazyParams.paginationStart;
      this.pagination.limit = lazyParams.paginationLimit;
      await this.loadPreviewData();
    },
    async onSort(event: any) {
      this.sort = event;
      await this.loadPreviewData();
    },
    async onFilter(event: any) {
      this.filter = event;
      await this.loadPreviewData();
    },
    setPreviewColumns(data: any) {
      this.columns = [];
      for (let i = 1; i < data.metaData.fields.length; i++) {
        this.columns.push({ header: data.metaData.fields[i].header, field: data.metaData.fields[i].name, type: data.metaData.fields[i].type });
      }
    },
    closeDialog() {
      this.dataset = null;
      this.rows = [];
      this.columns = [];
      this.pagination = { start: 0, limit: 15 };
      this.sort = null;
      this.filter = null;
      this.errorMessageVisible = false;
      this.errorMessage = "";
      this.$emit("close");
    },
  },
});
</script>

<style lang="scss">
.workspace-full-screen-dialog.p-dialog {
  max-height: 100%;
}
.workspace-full-screen-dialog .p-dialog .p-dialog-content {
  padding: 0;
}
.workspace-scrollable-table {
  height: 100%;
  .p-datatable-wrapper {
    position: relative;
    flex: 1;
    max-width: 96vw;
    overflow-x: auto;
  }
  .p-datatable {
    max-width: 96vw;
  }
}

.workspace-parameter-sidebar {
  top: 35px !important;
}
.workspace-parameter-sidebar .kn-parameter-sidebar-buttons {
  margin-bottom: 45px !important;
}
</style>
