<template>
  <div
    class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow"
    :style="descriptor.style.preview"
  >
    <!-- <div :style="descriptor.style.preview" class="kn-overflow"> -->
    <div style="overflow: auto; height: 500px; width: 400px">
      {{ propWidget }}
    </div>
    <Button
      icon="pi pi-save"
      class="p-button-text p-button-rounded p-button-plain"
      @click="populate"
    />

    <div ref="tabulator"></div>
  </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent } from "vue";
import { IWidgetColumn } from "../../Dashboard";
import { emitter } from "../../DashboardHelpers";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import mock from "../../dataset/DatasetEditorTestMocks.json";
import descriptor from "../../dataset/DatasetEditorDescriptor.json";
import { TabulatorFull as Tabulator } from "tabulator-tables";
import "tabulator-tables/dist/css/tabulator.min.css";

export default defineComponent({
  name: "widget-editor-preview",
  components: { Column, DataTable, Tabulator },
  props: {
    propWidget: {
      required: true,
      type: Object,
    },
  },
  data() {
    return {
      descriptor,
      mock,
      tabulator: null as any,
      columns: [] as any,
      tableData: [] as any,
    };
  },
  mounted() {
    this.setDatatableData();
  },
  created() {
    this.setEventListeners();
  },
  methods: {
    populate() {
      console.log("PROPERTY WITH DATA", this.propWidget);
      var formattedColumns = this.propWidget.columns.map((column) => {
        return { title: column.alias, field: column.id };
      });
      this.tabulator.setColumns(formattedColumns);
      console.log(this.tabulator.getColumnDefinitions());
      console.log("FOR COLUMNS", formattedColumns);
    },
    setDatatableData() {
      this.tableData = [];
      this.columns = this.propWidget.columns.map((column) => {
        return { title: column.alias, field: column.id };
      });
      this.tabulator = new Tabulator(this.$refs.tabulator, {
        data: this.tableData,
        columns: this.columns,
      });
      setTimeout(() => {
        console.log("DEFINIUTONS", this.tabulator.getColumnDefinitions());
      }, 300);
    },
    setEventListeners() {
      emitter.on("paginationChanged", (pagination) =>
        console.log("WidgetEditorPreview - PAGINATION CHANGED!", pagination)
      ); //  { enabled: this.paginationEnabled, itemsNumber: +this.itemsNumber }
      emitter.on("sortingChanged", (sorting) =>
        console.log("WidgetEditorPreview  - SORTING CHANGED!", sorting)
      ); // { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder }
      emitter.on("collumnAdded", (column) => this.onColumnAdd(column));
      emitter.on("collumnRemoved", (column) =>
        console.log(
          "WidgetEditorPreview  - collumnRemoved!",
          column,
          this.propWidget
        )
      );
      emitter.on("collumnUpdated", (column) => this.onColumnUpdate(column));
      emitter.on("columnsReordered", () =>
        console.log("WidgetEditorPreview  - columnsReordered!")
      );
      emitter.on("indexColumnChanged", (rows) =>
        console.log("WidgetEditorPreview  - indexColumnChanged!", rows)
      );
      emitter.on("rowSpanChanged", (rows) =>
        console.log("WidgetEditorPreview  - rowSpanChanged!", rows)
      );
      emitter.on("summaryRowsChanged", () =>
        console.log("WidgetEditorPreview  - summaryRowsChanged!")
      );
    },
    onColumnAdd(column) {
      // console.log('WidgetEditorPreview  - collumnAdded!', this.propWidget)
      let test = {
        alias: "FID",
        dataset: 166,
        decript: false,
        fieldType: "ATTRIBUTE",
        multiValue: false,
        name: "FID",
        personal: false,
        precision: 0,
        properties: {},
        scale: 0,
        subjectId: false,
        type: "java.lang.String",
      } as any;
      console.log("WidgetEditorPreview  - collumnAdded!", column);
      this.tabulator.addColumn({
        title: column.alias,
        field: column.name,
        width: 150,
      });
    },
    onColumnUpdate(column) {
      // console.log('WidgetEditorPreview  - collumnAdded!', this.propWidget)
      console.log("WidgetEditorPreview  - columnEdited!", column);
      this.tabulator.updateColumnDefinition(column.id, { title: column.alias });
    },
  },
});
</script>
