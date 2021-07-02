<template>
  <Toolbar class="kn-toolbar kn-toolbar--secondary">
    <template #left>
      {{ menuNode.name }}
    </template>
    <template #right>
      <Button
        icon="pi pi-save"
        class="p-button-text p-button-rounded p-button-plain"
        @click="save"
        :disabled="formValid"
      />
      <Button
        class="p-button-text p-button-rounded p-button-plain"
        icon="pi pi-times"
        @click="closeForm"
      />
    </template>
  </Toolbar>
  <ProgressBar
    mode="indeterminate"
    class="kn-progress-bar"
    v-if="loading"
    data-test="progress-bar"
  />

  <div class="p-grid p-m-0 p-fluid">
    <div class="p-col-12">
      <Card>
        <template #content>
          <form ref="menu-configuration-form">
            <div class="p-field">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <InputText
                    id="name"
                    type="text"
                    v-model.trim="v$.menuNode.name.$model"
                    @change="onDataChange(v$.menuNode.name)"
                    class="p-inputtext p-component kn-material-input"
                  />
                  <label for="name"
                    >{{ $t("managers.menuConfigurationManagement.form.name") }} *</label
                  >
                </span>
              </div>
              <KnValidationMessages
                :vComp="v$.menuNode.name"
                :additionalTranslateParams="{
                  fieldName: $t('managers.menuConfigurationManagement.form.name'),
                }"
              ></KnValidationMessages>
            </div>

            <div class="p-field">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <InputText
                    id="descr"
                    type="text"
                    v-model.trim="v$.menuNode.descr.$model"
                    @blur="onDataChange(v$.menuNode.descr)"
                    class="p-inputtext p-component kn-material-input"
                  />
                  <label for="descr">{{ $t("managers.menuConfigurationManagement.description") }} *</label>
                </span>
              </div>
              <KnValidationMessages
                :vComp="v$.menuNode.descr"
                :additionalTranslateParams="{
                  fieldName: $t('managers.menuConfigurationManagement.description'),
                }"
              ></KnValidationMessages>
            </div>

            <div class="p-field">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <Dropdown
                    id="menuNodeContent"
                    v-model="v$.menuNode.menuNodeContent.$model"
                    :options="menuNodeContent"
                    @change="onMenuNodeChange(v$.menuNode.menuNodeContent)"
                    optionLabel="name"
                    optionValue="value"
                    class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                  />
                  <label for="menuNodeContent">{{ $t("managers.menuConfigurationManagement.form.menuNodeContent") }} *</label>
                </span>
              </div>

              <KnValidationMessages
                :vComp="v$.menuNode.menuNodeContent"
                :additionalTranslateParams="{
                  fieldName: $t('managers.menuConfigurationManagement.form.menuNodeContent'),
                }"
              ></KnValidationMessages>
            </div>

            <div class="p-field" :hidden="staticPageHidden">
              <div class="p-field">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <Dropdown
                    id="staticPage"
                    v-model="v$.menuNode.staticPage.$model"
                    :options="staticPageOptions"
                    @change="onStaticPageSelect(v$.menuNode.staticPage)"
                    optionLabel="name"
                    optionValue="value"
                    class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                  />
                  <label for="staticPage">{{ $t("managers.menuConfigurationManagement.form.staticPage") }} *</label>
                </span>
              </div>

              <KnValidationMessages
                :vComp="v$.menuNode.menuNodeContent"
                :additionalTranslateParams="{
                  fieldName: $t(
                    'managers.menuConfigurationManagement.form.menuNodeContent'
                  ),
                }"
              ></KnValidationMessages>
              </div>
            </div>

            <div class="p-field" :hidden="externalAppHidden">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <InputText
                    id="externalApplicationUrl"
                    type="text"
                    v-model.trim="v$.menuNode.externalApplicationUrl.$model"
                    @blur="onDataChange(v$.menuNode.externalApplicationUrl)"
                    class="p-inputtext p-component kn-material-input"
                  />
                  <label for="externalApplicationUrl">{{ $t("managers.menuConfigurationManagement.form.externalApplicationUrl") }} *</label>
                </span>
              </div>
              <KnValidationMessages
                :vComp="v$.menuNode.externalApplicationUrl"
                :additionalTranslateParams="{
                  fieldName: $t('managers.menuConfigurationManagement.form.externalApplicationUrl'),
                }"
              ></KnValidationMessages>
            </div>


            <div :hidden="documentHidden">
              <div class="p-field">
                <div class="p-inputgroup">
                  <span class="p-float-label">

                    <InputText
                      id="selectedDocument"
                      type="text"
                      v-model.trim="v$.menuNode.document.$model"
                      @blur="onDataChange(v$.menuNode.document)"
                      class="p-inputtext p-component kn-material-input"
                    />

                    <InputText
                      :hidden="true"
                      id="objId"
                      type="text"
                      v-model.trim="v$.menuNode.objId.$model"
                      @blur="onDataChange(v$.menuNode.objId)"
                      class="p-inputtext p-component kn-material-input"
                    />
                    <Button
                      icon="pi pi-search"
                      class="p-button"
                      @click="openRelatedDocumentModal()"
                    />
                    <label for="objId">{{ $t("managers.menuConfigurationManagement.form.document") }} *</label
                    >
                  </span>
                </div>
              </div>

              <div class="p-field">
                <div class="p-inputgroup">
                  <span class="p-float-label">
                    <InputText
                      id="objParameters"
                      type="text"
                      v-model.trim="v$.menuNode.objParameters.$model"
                      @blur="onDataChange(v$.menuNode.objParameters)"
                      class="p-inputtext p-component kn-material-input"
                    />
                    <label for="objParameters">{{ $t("managers.menuConfigurationManagement.form.objParameters") }}</label>
                  </span>
                </div>

                <KnValidationMessages
                  :vComp="v$.menuNode.objParameters"
                  :additionalTranslateParams="{
                    fieldName: $t(
                      'managers.menuConfigurationManagement.form.objParameters'
                    ),
                  }"
                ></KnValidationMessages>
              </div>

              <Dialog
                :header="$t('managers.menuConfigurationManagement.selectDocument')"
                v-model:visible="displayModal"
                :style="{ width: '50vw' }"
                :modal="true"
              >
                <RelatedDocumentList
                  :loading="loading"
                  @selectedDocument="onDocumentSelect"
                  data-test="related-documents-list"
                ></RelatedDocumentList>
              </Dialog>
            </div>

            <div class="p-field" :hidden="functionalityHidden">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <Dropdown
                    id="functionality"
                    v-model="v$.menuNode.functionality.$model"
                    :options="menuNodeContentFunctionalies"
                    @change="onFunctionalityTypeChange(v$.menuNode.functionality)"
                    optionLabel="name"
                    optionValue="value"
                    class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                  />
                  <label for="functionality">{{ $t("managers.menuConfigurationManagement.form.functionality") }}*</label>
                </span>
              </div>

              <KnValidationMessages
                :vComp="v$.menuNode.functionality"
                :additionalTranslateParams="{
                  fieldName: $t('managers.menuConfigurationManagement.form.functionality'),
                }"
              ></KnValidationMessages>
            </div>

            <div class="p-field" :hidden="workspaceInitialHidden">
              <div class="p-inputgroup">
                <span class="p-float-label">
                  <Dropdown
                    id="initialPath"
                    v-model="v$.menuNode.initialPath.$model"
                    :options="workspaceOptions"
                    optionLabel="name"
                    optionValue="value"
                    class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                  />
                  <label for="initialPath">{{ $t("managers.menuConfigurationManagement.form.initialPath") }} *</label>
                </span>
              </div>
            </div>

            <div class="p-field" :hidden="documentTreeHidden">
              <p>Open document browser on</p>
              <DocumentBrowserTree
                :selected="v$.menuNode.initialPath.$model"
                @selectedDocumentNode="onSelectedDocumentNode"
                :loading="loading"
              ></DocumentBrowserTree>
            </div>
          </form>
        </template>
      </Card>

      <RolesTab
        :rolesList="roles"
        :selected="selectedMenuNode.roles"
        @changed="setSelectedRoles($event)"
        v-if="!hideForm"
      ></RolesTab>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios, { AxiosResponse } from "axios";
import { iMenuNode, iRole } from "./MenuConfiguration";
import useValidate from "@vuelidate/core";
import { createValidations } from "@/helpers/commons/validationHelper";
import Dropdown from "primevue/dropdown";
import Dialog from "primevue/dialog";
import RelatedDocumentList from "./RelatedDocumentList.vue";
import DocumentBrowserTree from "./DocumentBrowserTree.vue";
import RolesTab from "./RolesTab.vue";
import KnValidationMessages from "@/components/UI/KnValidatonMessages.vue";
import MenuConfigurationDescriptor from "./MenuConfigurationDescriptor.json";
import MenuConfigurationValidationDescriptor from "./MenuConfigurationValidationDescriptor.json";
export default defineComponent({
  name: "profile-attributes-detail",
  components: {
    Dropdown,
    DocumentBrowserTree,
    RolesTab,
    RelatedDocumentList,
    KnValidationMessages,
    Dialog,
  },
  props: {
    selectedMenuNode: {
      type: Object,
      required: true,
    },
  },
  computed: {
    formValid(): any {
      return this.v$.$invalid;
    },
  },
  watch: {
    selectedMenuNode: {
      handler: function (node) {
        this.v$.$reset();
        this.loadNode(node);
      },
    },
  },
  emits: ["refreshRecordSet", "closesForm", "dataChanged"],
  data() {
    return {
      v$: useValidate() as any,
      apiUrl: process.env.VUE_APP_RESTFUL_SERVICES_PATH + "2.0/",
      menuNode: {} as iMenuNode,
      loading: false as Boolean,
      hideForm: false as Boolean,
      documentHidden: true as Boolean,
      staticPageHidden: true as Boolean,
      externalAppHidden: true as Boolean,
      functionalityHidden: true as Boolean,
      workspaceInitialHidden: true as Boolean,
      documentTreeHidden: true as Boolean,
      dirty: false as Boolean,
      displayModal: false as Boolean,
      roles: [] as iRole[],
      selectedRoles: [] as iRole[],
      relatedDocuments: [],
      selectedRelatedDocument: "",
      selectedFunctionality: {},
      menuNodeContent: MenuConfigurationDescriptor.menuNodeContent,
      workspaceOptions: MenuConfigurationDescriptor.workspaceOptions,
      staticPageOptions: MenuConfigurationDescriptor.staticPageOptions,
      menuNodeContentFunctionalies: MenuConfigurationDescriptor.menuNodeContentFunctionalies,
    };
  },
  validations() {
    return {
      menuNode: createValidations(
        "menuNode",
        MenuConfigurationValidationDescriptor.validations.menuNode
      ),
    };
  },
  async created() {
    await this.loadRoles();

    if (this.selectedMenuNode) {
      this.loadNode(this.selectedMenuNode);
    }
  },
  methods: {
    resetForm() {
      Object.keys(this.menuNode).forEach((k) => delete this.menuNode[k]);
    },
    openRelatedDocumentModal() {
      this.displayModal = true;
    },
    closeRelatedDocumentModal() {
      this.displayModal = false;
    },
    showForm() {
      this.resetForm();
      this.hideForm = false;
    },
    toggleDocument() {
      this.documentHidden = false;
      this.staticPageHidden = true;
      this.externalAppHidden = true;
      this.functionalityHidden = true;
      this.documentTreeHidden = true;
      this.workspaceInitialHidden = true;
    },
    toggleStaticPage() {
      this.staticPageHidden = false;
      this.documentHidden = true;
      this.externalAppHidden = true;
      this.functionalityHidden = true;
      this.documentTreeHidden = true;
      this.workspaceInitialHidden = true;
    },
    toggleExternalApp() {
      this.externalAppHidden = false;
      this.documentHidden = true;
      this.staticPageHidden = true;
      this.functionalityHidden = true;
      this.documentTreeHidden = true;
      this.workspaceInitialHidden = true;
    },
    toggleFunctionality() {
      this.functionalityHidden = false;
      this.externalAppHidden = true;
      this.documentHidden = true;
      this.staticPageHidden = true;
      if (this.menuNode.functionality == "WorkspaceManagement") {
        this.toggleWorkspaceInitial();
      } else if (this.menuNode.functionality == "DocumentUserBrowser") {
        this.toggleDocumentTreeSelect();
      }
    },
    toggleEmpty() {
      this.functionalityHidden = true;
      this.externalAppHidden = true;
      this.documentHidden = true;
      this.staticPageHidden = true;
      this.documentTreeHidden = true;
      this.workspaceInitialHidden = true;
    },
    toggleWorkspaceInitial() {
      this.workspaceInitialHidden = false;
      this.documentTreeHidden = true;
    },
    toggleDocumentTreeSelect() {
      this.documentTreeHidden = false;
      this.workspaceInitialHidden = true;
    },
    onMenuNodeChange(menuNodeContent) {
      if (menuNodeContent.$model == 1) {
        this.toggleDocument();
      } else if (menuNodeContent.$model == 3) {
        this.toggleStaticPage();
      } else if (menuNodeContent.$model == 2) {
        this.toggleExternalApp();
      } else if (menuNodeContent.$model == 4) {
        this.toggleFunctionality();
      } else {
        this.toggleEmpty();
      }
    },
    onFunctionalityTypeChange(functionality) {
      if (functionality.$model == "WorkspaceManagement") {
        this.toggleWorkspaceInitial();
      } else if (functionality.$model == "DocumentUserBrowser") {
        this.toggleDocumentTreeSelect();
      }
    },
    onDocumentSelect(document) {
      this.menuNode.objId = document.DOCUMENT_ID;
      this.menuNode.document = document.DOCUMENT_NAME;
      this.closeRelatedDocumentModal();
    },
    async save() {
      let response: AxiosResponse;

      if (this.menuNode.menuId != null) {
        response = await axios.put(
          this.apiUrl + "menu/" + this.menuNode.menuId,
          this.menuNode,
          MenuConfigurationDescriptor.headers
        );
      } else {
        response = await axios.post(
          this.apiUrl + "menu/",
          this.menuNode,
          MenuConfigurationDescriptor.headers
        );
      }
      if (response.status == 200) {
        if (response.data.errors) {
          console.log(response.data.errors);
        } else {
          this.$store.commit("setInfo", {
            title: this.$t("managers.menuConfigurationManagement.info.saveTitle"),
            msg: this.$t("managers.menuConfigurationManagement.info.saveMessage"),
          });
        }
      }
      this.$emit("refreshRecordSet");
      this.resetForm();
    },
    async loadRoles() {
      this.loading = true;
      this.hideForm = true;
      this.dirty = false;
      await axios.get(this.apiUrl + "roles").then((response) => { this.roles = response.data; }).finally(() => (this.loading = false));
    },
    async getDocumentNameByID(id : any) {
      await axios.get(this.apiUrl + "documents/" + id).then((response) => { this.menuNode.document = response.data.name; });
    },
    closeForm() {
      this.$emit("closesForm");
    },
    onAttributeSelect(event: any) {
      this.populateForm(event.data);
    },
    populateForm(menuNode: iMenuNode) {
      this.hideForm = false;
      this.menuNode = { ...menuNode };
      if(menuNode.objId){
      this.getDocumentNameByID(menuNode.objId);
      }
      
      if (this.menuNode.functionality != null) {
        this.menuNode.menuNodeContent = 4;
        this.toggleFunctionality();
      } else if (this.menuNode.externalApplicationUrl != null) {
        this.menuNode.menuNodeContent = 2;
        this.toggleExternalApp();
      } else if (this.menuNode.objId != null) {
        this.menuNode.menuNodeContent = 1;
        this.toggleDocument();
      } else if (this.menuNode.staticPage != null) {
        this.menuNode.menuNodeContent = 3;
        this.toggleStaticPage();
      } else {
        this.menuNode.menuNodeContent = 0;
        this.toggleEmpty();
      }
    },
    onStaticPageSelect(){
      this.menuNode.initialPath = null;
      this.menuNode.functionality= null;
      this.menuNode.objParameters= null;
      this.menuNode.objId= null;
      this.menuNode.externalApplicationUrl= null;
    },
    onDataChange(v$Comp) {
      v$Comp.$touch();
      this.$emit("dataChanged");
    },
    loadNode(menuNode) {
      if (menuNode.menuId === null) {
        this.resetForm();
        return;
      }
      this.populateForm(menuNode);
    },
    setSelectedRoles(roles: iRole[]) {
      this.selectedRoles = roles;
      this.menuNode.roles = roles;
    },
    onSelectedDocumentNode(documentInitialPath) {
      this.menuNode.initialPath = documentInitialPath;
    },
  },
});
</script>

<style lang="scss" scoped>
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  @media screen and (max-width: 960px) {
    align-items: start;
  }
}

.record-image {
  width: 50px;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23);
}

.p-dialog .record-image {
  width: 50px;
  margin: 0 auto 2rem auto;
  display: block;
}

.confirmation-content {
  display: flex;
  align-items: center;
  justify-content: center;
}
@media screen and (max-width: 960px) {
  ::v-deep(.p-toolbar) {
    flex-wrap: wrap;

    .p-button {
      margin-bottom: 0.25rem;
    }
  }
}
</style>
