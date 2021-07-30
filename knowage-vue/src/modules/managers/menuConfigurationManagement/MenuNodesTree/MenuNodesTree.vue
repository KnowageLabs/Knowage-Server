<template>
  <Tree
    :loading="load"
    :value="menuElements"
    :expandedKeys="expandedKeys"
    selectionMode="single"
    v-model:selectionKeys="selectedMenuNode"
    :metaKeySelection="false"
    @node-select="onNodeSelect"
    @nodeUnselect="onNodeUnselect"
    data-test="menu-nodes-tree"
  >
    <template #empty>{{ $t("common.info.noDataFound") }}</template>
    <template #default="slotProps">
      <div class="kn-list-item">
        <div class="kn-list-item-text" :data-test="'menu-nodes-tree-item-' + slotProps.node.menuId">
          <span>{{ slotProps.node.name }}</span>
        </div>

        <Button
          v-if="canBeDeleted(slotProps.node)"
          icon="far fa-trash-alt"
            class="p-button-link p-button-sm p-p-0"
          @click="deleteMenuNode(slotProps.node.menuId)"
          :data-test="'delete-button-' + slotProps.node.menuId"
        />

        <div v-if="slotProps.node.parentId != null">
          <Button
            icon="pi pi-sort-alt"
              class="p-button-link p-button-sm p-p-0"
            @click="changeWithFather(slotProps.node.menuId)"
            :data-test="'change-with-father-button-' + slotProps.node.menuId"
          />
        </div>

        <Button
          v-if="canBeMovedUp(slotProps.node)"
          icon="pi pi-arrow-up"
            class="p-button-link p-button-sm p-p-0"
          @click="moveUp(slotProps.node.menuId)"
          :data-test="'move-up-button-' + slotProps.node.menuId"
        />

        <Button
          v-if="canBeMovedDown(slotProps.node)"
          icon="pi pi-arrow-down"
            class="p-button-link p-button-sm p-p-0"
          @click="moveDown(slotProps.node.menuId)"
          :data-test="'move-down-button-' + slotProps.node.menuId"
        />
      </div>
    </template>
  </Tree>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Tree from "primevue/tree";
import { iMenuNode } from "../MenuConfiguration";
import { arrayToTree } from "@/helpers/commons/arrayToTreeHelper";
export default defineComponent({
  name: "menu-nodes-tree",
  components: {
    Tree,
  },
  emits: [
    "selectedMenuNode",
    "unselectedMenuNode",
    "deleteMenuNode",
    "changeWithFather",
    "moveUp",
    "moveDown",
  ],
  props: {
    elements: Array,
    loading: Boolean,
  },
  watch: {
    elements: {
      handler: function (element) {
        element = element.map((item) => {
          item.label = item.name;
          item.id = item.menuId;
          item.key = item.menuId;
          return item;
        });

        this.menuElements = arrayToTree(element, { dataField: null });
        this.expandAll();
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
      load: false as Boolean,
      menuElements: [] as any[],
      expandedKeys: [] as any[],
      selectedMenuNode: null as iMenuNode | null,
    };
  },
  methods: {
    expandAll() {
      for (let node of this.menuElements) {
        this.expandNode(node);
      }
      this.expandedKeys = { ...this.expandedKeys };
    },
    expandNode(node) {
      if (node.children && node.children.length) {
        this.expandedKeys[node.key] = true;

        for (let child of node.children) {
          this.expandNode(child);
        }
      }
    },
    canBeMovedUp(node: iMenuNode) {
      return node.prog !== 1;
    },
    canBeMovedDown(node: iMenuNode) {
      let canBeMoved = false;
      this.menuElements.forEach((currentNode) => {
        if (
          node.parentId === currentNode.parentId &&
          node.prog < currentNode.prog
        ) {
          canBeMoved = true;
        }
      });
      return canBeMoved;
    },
    canBeDeleted(node: iMenuNode) {
      return node.parentId;
    },
    deleteMenuNode(elementID: number) {
      this.$emit("deleteMenuNode", elementID);
    },
    changeWithFather(elementID: number) {
      this.$emit("changeWithFather", elementID);
    },
    moveUp(elementID: number) {
      this.$emit("moveUp", elementID);
    },
    moveDown(elementID: number) {
      this.$emit("moveDown", elementID);
    },
    onElementSelect(event: any) {
      this.$emit("selectedMenuNode", event.value);
    },
    onNodeSelect(node: iMenuNode) {
      this.$emit("selectedMenuNode", node);
    },
    onNodeUnselect(node: iMenuNode) {
      this.$emit("unselectedMenuNode", node);
    },
  },
});
</script>
