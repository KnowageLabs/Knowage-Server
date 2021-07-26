<template>
  <div style="margin-bottom: 1em">
    <ToggleButton
      :onLabel="$t('common.expand')"
      :offLabel="$t('common.collapse')"
      onIcon="pi pi-plus"
      offIcon="pi pi-minus"
      style="width: 10em"
      @click="toggleExpandCollapse"
    />
  </div>
  <Tree
    :value="nodes"
    :expandedKeys="expandedKeys"
    selectionMode="single"
    v-model:selectionKeys="preselectedNodeKey"
    :metaKeySelection="false"
    @node-select="onNodeSelect"
    :data-test="document-browser-tree"
  >
    <template #empty>{{ $t("common.info.noDataFound") }}</template>
    <template #default="slotProps">
      <div class="kn-list-item">
        <div class="kn-list-item-text" :data-test="'document-browser-tree-item-' + slotProps.node.id">
          <span>{{ slotProps.node.name }}</span>
        </div>
      </div>
    </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import ToggleButton from "primevue/togglebutton";
import Tree from "primevue/tree";
import axios from "axios";
export default defineComponent({
  name: "document-browser-tree",
  components: {
    Tree,
    ToggleButton,
  },
  emits: ["selectedDocumentNode"],
  props: {
    selected: null,
    loading: Boolean,
  },
  watch: {
    selected: {
      handler: function (select) {
        if (this.checkValueIsPath(select)) {
          let flattenTree = this.flattenTree(this.nodes[0], "childs");
          this.preselectNodeKey(flattenTree, select);
        }
      },
    },
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
      preselectedNodeKey: null as any | null,
      nodes: [] as any[],
      expandedKeys: {},
    };
  },
  async created() {
    await this.loadFunctionalities();
    if (this.checkValueIsPath(this.selected)) {
      let flattenTree = this.flattenTree(this.nodes[0], "childs");
      this.preselectNodeKey(flattenTree, this.selected);
    }
  },
  methods: {
    flattenTree(root, key) {
      let flatten = [Object.assign({}, root)];
      delete flatten[0][key];
      if (root[key] && root[key].length > 0) {
        return flatten.concat(root[key].map((child) => this.flattenTree(child, key)).reduce((a, b) => a.concat(b), []));
      }
      return flatten;
    },
    checkValueIsPath(select) {
      if (select && this.nodes[0]) {
        var pos = select.indexOf("/");
        if (pos != -1) {
          return true;
        } else {
          return false;
        }
      }
    },
    preselectNodeKey(flatArray, select) {
      for (let node of flatArray) {
        if (node.path == select) {
          let selectionObj: any = {};
          selectionObj[node.id] = true;
          this.preselectedNodeKey = selectionObj;
        }
      }
    },
    toggleExpandCollapse() {
      if (Object.keys(this.expandedKeys).length === 0) { this.expandAll(); } 
      else { this.collapseAll(); }
    },
    expandAll() {
      for (let node of this.nodes) {
        this.expandNode(node);
      }
     
      this.expandedKeys = { ...this.expandedKeys };
       console.log(this.expandedKeys)
    },
    collapseAll() {
      this.expandedKeys = {};
    },
    expandNode(node) {
      if (node.children && node.children.length) {
        this.expandedKeys[node.key] = true;
        for (let child of node.children) {
          this.expandNode(child);
        }
      }
    },
    onNodeSelect(node) {
      this.$emit("selectedDocumentNode", node.path);
    },
    async loadFunctionalities() {
      this.load = true;
      await axios
        .get(this.apiUrl + "menu/functionalities")
        .then((response) => {
          this.nodes = response.data.functionality.map((item) => {
            item.label = item.name;
            item.key = item.id;
            item.children = item.childs;
            item.icon = "pi pi-fw pi-folder";

            if (item.childs) {
              item.childs.forEach(function (element) {
                element.label = element.name;
                element.key = element.id;
                element.children = element.childs;
                element.icon = "pi pi-fw pi-folder";

                if (element.childs) {
                  element.childs.forEach(function (element) {
                    element.label = element.name;
                    element.key = element.id;
                    element.children = element.childs;
                    element.icon = "pi pi-fw pi-folder";
                  });
                }
              });
            }
            return item;
          });
        })
        .finally(() => {
          this.load = false;
          this.expandAll();
        });
    },
  },
});
</script>
