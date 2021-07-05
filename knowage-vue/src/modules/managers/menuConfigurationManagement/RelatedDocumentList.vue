<template>
  <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="load" data-test="progress-bar"/>
  <Listbox
    v-if="!load"
    class="kn-list--column"
    :options="relatedDocumentsList"
    :filter="true"
    :filterPlaceholder="$t('common.search')"
    optionLabel="DOCUMENT_ID"
    filterMatchMode="contains"
    :filterFields="menuConfigurationRelatedDocumentsDescriptor.globalFilterFields"
    :emptyFilterMessage="$t('managers.widgetGallery.noResults')"
    @change="onDocumentSelect"
    data-test="related-documents-list"
  >
    <template #empty>{{ $t("common.info.noDataFound") }}</template>
    <template #option="slotProps">
      <div class="kn-list-item" data-test="list-item">
        <div class="kn-list-item-text">
          <span>{{ slotProps.option.DOCUMENT_NAME }}</span>
          <span class="kn-list-item-text-secondary">{{
            slotProps.option.DOCUMENT_DESCR
          }}</span>
        </div>
      </div>
    </template>
  </Listbox>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import ProgressBar from 'primevue/progressbar';
import Listbox from "primevue/listbox";
import MenuConfigurationRelatedDocumentsDescriptor from "./MenuConfigurationRelatedDocumentsDescriptor.json";
import axios from "axios";

export default defineComponent({
  name: "related-documents-list",
  components: {
    Listbox, ProgressBar
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
      relatedDocumentsList: [] as any[],
      selectedDocument: null as any | null,
      menuConfigurationRelatedDocumentsDescriptor: MenuConfigurationRelatedDocumentsDescriptor
    };
  },
  async created() {
    await this.loadRelatedDocuments();
  },
  methods: {
    async loadRelatedDocuments() {
      this.load = true;
      await axios.get(this.apiUrl + "documents/listDocument").then((response) => { this.relatedDocumentsList = response.data.item; }).finally(() => (this.load = false));
    },
    onDocumentSelect(event: any) {
      this.$emit("selectedDocument", event.value);
    },
  },
});
</script>
