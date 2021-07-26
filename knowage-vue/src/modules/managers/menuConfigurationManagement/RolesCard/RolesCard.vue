<template>
  <Card class="p-mx-auto">
    <template #header>
      <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #left>
          {{ $t("managers.menuConfigurationManagement.roles") }}
        </template>
      </Toolbar>
    </template>
    <template #content>
      <DataTable
        :value="rolesList"
        v-model:selection="selectedRoles"
        class="p-datatable-sm kn-table"
        dataKey="id"
        responsiveLayout="stack"
        breakpoint="960px"
        @rowSelect="onRowSelect"
        @rowUnselect="onRowUnselect"
      >
        <template #empty>
          {{ $t("common.info.noDataFound") }}
        </template>
        <Column field="name" :header="$t('common.name')"></Column>
        <Column selectionMode="multiple" dataKey="id"></Column>
      </DataTable>
    </template>
  </Card>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Card from "primevue/card";
import Column from "primevue/column";
import DataTable from "primevue/datatable";
import { iRole } from "../MenuConfiguration";

export default defineComponent({
  name: "roles-tab",
  components: {
    Card,
    Column,
    DataTable,
  },
  props: {
    rolesList: Array,
    selected: Array,
  },
  emits: ["changed"],
  data() {
    return {
      selectedRoles: [] as iRole[],
    };
  },
  watch: {
    selected: {
      handler: function (selected: iRole[]) {
        this.selectedRoles = selected;
      },
    },
  },
  methods: {
    onRowSelect() {
      this.$emit("changed", this.selectedRoles);
    },
    onRowUnselect() {
      this.$emit("changed", this.selectedRoles);
    },
  },
});
</script>
