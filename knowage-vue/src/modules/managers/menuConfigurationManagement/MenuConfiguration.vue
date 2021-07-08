<template>
  <div class="kn-page">
    <div class="kn-page-content p-grid p-m-0">
      <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
          <template #left>
            {{ $t("managers.menuConfigurationManagement.title") }}
          </template>
          <template #right>
            <KnFabButton icon="fas fa-plus" @click="showForm()" data-test="open-form-button"></KnFabButton>
          </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar"/>
        <MenuNodesTree
          :elements="menuNodes"
          :loading="loading"
          @deleteMenuNode="onNodeDelete"
          @selectedMenuNode="onNodeSelect"
          @unselectedMenuNode="onNodeUnselect"
          @changeWithFather="onChangeWithFather"
          @moveUp="onMoveUp"
          @moveDown="onMoveDown"
          data-test="menu-nodes-tree"
        ></MenuNodesTree>
      </div>

      <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
        <KnHint :title="'managers.menuConfigurationManagement.title'" :hint="'managers.menuConfigurationManagement.hint'" v-if="hideForm"></KnHint>
        <MenuElementsDetail
          :selectedRoles="selectedMenuNode.roles"
          :selectedMenuNode="selectedMenuNode"
          @refreshRecordSet="loadMenuNodes"
          @closesForm="closeForm"
          @dataChanged="dirty = true"
          :hidden="hideForm"
        ></MenuElementsDetail>

         <RolesCard :hidden="hideForm" :rolesList="roles" :selected="selectedMenuNode.roles" @changed="setSelectedRoles($event)"></RolesCard>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import KnFabButton from "@/components/UI/KnFabButton.vue";
import KnHint from "@/components/UI/KnHint.vue";
import { iMenuNode } from "./MenuConfiguration";
import MenuNodesTree from "./MenuNodesTree/MenuNodesTree.vue";
import MenuElementsDetail from "./ElementDetailsCard/MenuElementsDetail.vue";
import RolesCard from "./RolesCard/RolesCard.vue";
import { iRole } from "../usersManagement/UsersManagement";
export default defineComponent({
  name: "menu-configuration",
  components: {
    MenuNodesTree,
    MenuElementsDetail,
    KnFabButton,
    KnHint,
    RolesCard
  },
  data() {
    return {
      apiUrl: process.env.VUE_APP_RESTFUL_SERVICES_PATH + "2.0/",
      menuNodes: [] as iMenuNode[],
      selectedMenuNode: {} as any,
      loading: false as Boolean,
      hideForm: true as Boolean,
      dirty: false as Boolean,
      roles: [] as iRole[]
    };
  },
  async created() {
    await this.loadMenuNodes();
    await this.loadRoles();
  },
  methods: {
    async loadRoles() {
      this.loading = this.hideForm = true;
      this.dirty = false;
      await axios.get(this.apiUrl + "roles").then((response) => { this.roles = response.data; }).finally(() => (this.loading = false));
    },
    showForm() {
      this.hideForm = false;
      if(Object.keys(this.selectedMenuNode).length === 0 && this.selectedMenuNode.constructor === Object){
      this.selectedMenuNode = {};
      this.initMenuNode();
      }else{
        let selectedNode = this.selectedMenuNode;
        this.selectedMenuNode = {};
        this.initMenuNode();
        this.selectedMenuNode.parentId = selectedNode.id;
      }
    },
    initMenuNode(){
      this.selectedMenuNode.level=0;
      this.selectedMenuNode.icon = {};
      this.selectedMenuNode.roles = [];
      this.selectedMenuNode.custIcon = this.selectedMenuNode.externalApplicationUrl = this.selectedMenuNode.functionality= this.selectedMenuNode.initialPath = 
      this.selectedMenuNode.objId = this.selectedMenuNode.objParameters = this.selectedMenuNode.staticPage = this.selectedMenuNode.parentId = null;
      this.selectedMenuNode.hideSliders = this.selectedMenuNode.hideToolbar = this.selectedMenuNode.viewIcons = false;
    },
    closeForm() {
      this.hideForm = true;
    },
    async loadMenuNodes() {
      this.loading = true;
      this.hideForm = true;
      this.dirty = false;
      await axios
        .get(this.apiUrl + "menu")
        .then((response) => {
          this.menuNodes = response.data;
        })
        .finally(() => (this.loading = false));
    },
    onNodeSelect(menuNode?: iMenuNode) {
      if (this.dirty) {
        this.$confirm.require({
          message: this.$t("common.toast.unsavedChangesMessage"),
          header: this.$t("common.toast.unsavedChangesHeader"),
          icon: "pi pi-exclamation-triangle",
          accept: () => {
            this.dirty = false;
            if (menuNode) this.prepareFormData(menuNode);
            else this.hideForm = true;
          },
        });
      } else {
        if (menuNode) this.prepareFormData(menuNode);
        else this.hideForm = true;
      }
    },
    onNodeUnselect(){
      this.selectedMenuNode = {};
      this.initMenuNode();
    },
    onNodeDelete(id: number) {
      this.deleteNode(id);
    },
    onChangeWithFather(id: number) {
      this.changeWithFather(id);
    },
     onMoveUp(id: number) {
      this.moveUp(id);
    },
    onMoveDown(id: number) {
      this.moveDown(id);
    },
    async changeWithFather(id: number) {
      this.loading = true;
      this.axios
        .get(this.apiUrl + "menu/changeWithFather/" + id)
        .then(() => {
          this.loadMenuNodes();
        })
        .finally(() => {
          this.loading = false;
        });
    },
    async moveUp(id: number) {
      this.loading = true;
      this.axios
        .get(this.apiUrl + "menu/moveUp/" + id)
        .then(() => {
          this.loadMenuNodes();
        })
        .finally(() => {
          this.loading = false;
        });
    },
    async moveDown(id: number) {
      this.loading = true;
      this.axios
        .get(this.apiUrl + "menu/moveDown/" + id)
        .then(() => {
          this.loadMenuNodes();
        })
        .finally(() => {
          this.loading = false;
        });
    },
    setSelectedRoles(roles: iRole[]) {
      this.selectedMenuNode.roles = roles;
    },
    async deleteNode(id: number) {
      this.$confirm.require({
        message: this.$t("common.toast.deleteMessage"),
        header: this.$t("common.toast.deleteConfirmTitle"),
        icon: "pi pi-exclamation-triangle",
        accept: async () => {
          this.loading = true;
          this.axios
            .delete(this.apiUrl + "menu/" + id)
            .then(() => {
              this.$store.commit("setInfo", {
                title: this.$t("managers.menuConfigurationManagement.info.deleteTitle"),
                msg: this.$t("managers.menuConfigurationManagement.info.deleteMessage"),
              });
              this.loadMenuNodes();
            })
            .finally(() => {
              this.loading = false;
            });
        },
      });
    },
    prepareFormData(menuNode: iMenuNode) {
      if (this.hideForm) {
        this.hideForm = false;
      }
      this.selectedMenuNode = { ...menuNode };
    },
  },
});
</script>
