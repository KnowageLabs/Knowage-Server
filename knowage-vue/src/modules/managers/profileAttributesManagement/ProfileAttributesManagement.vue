<template>
  <div class="kn-page">
    <div class="kn-page-content p-grid p-m-0">
      <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
          <template #left>
            {{ $t("managers.profileAttributesManagement.title") }}
          </template>
          <template #right>
            <KnFabButton icon="fas fa-plus" @click="showForm()" data-test="open-form-button"></KnFabButton>
          </template>
        </Toolbar>
        <ProgressBar
          mode="indeterminate"
          class="kn-progress-bar"
          v-if="loading"
          data-test="progress-bar"
        />
        <AttributesListBox
          :attributes="attributes"
          :loading="loading"
          @deleteAttribute="onAttributeDelete"
          @selectedAttribute="onAttributeSelect"
          data-test="profile-attributes-listbox"
        ></AttributesListBox>
      </div>

      <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0" :hidden="hideForm">
        <ProfileAttributesForm
          :selectedAttribute="attribute"
          @refreshRecordSet="loadAllAttributes"
          @closesForm="closeForm"
        ></ProfileAttributesForm>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import KnFabButton from "@/components/UI/KnFabButton.vue";
import ProfileAttributesManagementDescriptor from "./ProfileAttributesManagementDescriptor.json";
import ProfileAttributesForm from "./ProfileAttributesForm.vue";
import AttributesListBox from "./AttributesListBox.vue";
import { iAttribute } from "./ProfileAttributesManagement";

export default defineComponent({
  name: "profile-attributes",
  components: {
    KnFabButton,
    ProfileAttributesForm,
    AttributesListBox,
  },
  data() {
    return {
      apiUrl: process.env.VUE_APP_RESTFUL_SERVICES_PATH + "2.0/",
      attributes: [] as iAttribute[],
      attribute: {} as iAttribute,
      tempAttribute: {} as iAttribute,
      profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor,
      columns: ProfileAttributesManagementDescriptor.columns,
      loading: false as Boolean,
      hideForm: false as Boolean,
    };
  },
  async created() {
    await this.loadAllAttributes();
  },
  methods: {
    async loadAllAttributes() {
      this.loading = true;
      await axios
        .get(this.apiUrl + "attributes")
        .then((response) => {
          this.attributes = response.data;
        })
        .finally(() => (this.loading = false));
    },
    onAttributeSelect(attribute: iAttribute) {
      this.attribute = { ...attribute };
      if (this.hideForm) {
        this.hideForm = false;
      }
    },
    onAttributeDelete(id: number) {
      this.deleteAttribute(id);
    },
    showForm() {
      this.hideForm = false;
      this.attribute = {
        attributeId: null,
        attributeName: "",
        attributeDescription: "",
        allowUser: null,
        multivalue: null,
        syntax: null,
        lovId: null,
        value: {},
      };
    },
    closeForm() {
      this.hideForm = true;
    },
    async deleteAttribute(id: number) {
      this.$confirm.require({
        message: this.$t(
          "managers.profileAttributesManagement.confirmDeleteMessage",
          {
            item: "attribute",
          }
        ),
        header: this.$t("common.confirmation"),
        icon: "pi pi-exclamation-triangle",
        accept: async () => {
          this.loading = true;
          this.axios
            .delete(this.apiUrl + "attributes/" + id)
            .then(() => {
              this.$store.commit("setInfo", {
                title: this.$t("managers.profileAttributesManagement.info.deleteTitle"),
                msg: this.$t("managers.profileAttributesManagement.info.deleteMessage"),
              });
              this.loadAllAttributes();
            })
            .finally(() => {
              this.loading = false;
            });
        },
      });
    },
  },
});
</script>