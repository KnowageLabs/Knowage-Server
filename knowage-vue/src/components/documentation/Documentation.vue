<template>
  <q-layout v-if="config" view="hHh lpR fFf" :class="['full-height', { 'no-main-menu': isMainMenuHidden }]">
    <q-drawer v-model="drawer" side="left" bordered class="drawer">
      <div class="q-pa-md">
        <div class="full-width flex justify-center">
          <img v-if="getLogoUrl()" :src="getLogoUrl()" class="logo" @error="onLogoError" />
        </div>

        <h2 v-if="config?.title">{{ config.title }}</h2>

        <q-tree :nodes="treeNodes" :dense="config.dense" node-key="id" no-connectors default-expand-all v-model:selected="selectedKey" no-selection-unset @update:selected="onNodeSelect">
          <template #header-section="prop">
            <span :class="['doc-menu-node', 'doc-menu-section', { active: selectedKey === prop.node.id, 'no-path': !prop.node.path }]">
              {{ prop.node.label }}
            </span>
          </template>
          <template #default-header="prop">
            <span :class="['doc-menu-node', { active: selectedKey === prop.node.id, 'no-path': !prop.node.path }]">
              {{ prop.node.label }}
            </span>
          </template>
        </q-tree>
      </div>
    </q-drawer>

    <q-page-container v-if="config" class="full-height scroll relative-position">
      <router-view @toggle-drawer="toggleDrawer" />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import axios from "axios";
import { onMounted, ref, computed, watch } from "vue";
import { findFoldersWithLabel, mapToQTreeNodes } from "./DocumentationHelper";
import { useStore } from "vuex";
import { useRouter, useRoute } from "vue-router";

const store = useStore();
const router = useRouter();
const route = useRoute();

const drawer = ref(true);
const isMainMenuHidden = computed(() => route.query.menu === "false");
const folderKey = ref<string | null>("");
const config = ref<any | null>(null);
const logoWide = require("@/assets/images/commons/logo_knowage.svg");

const selectedKey = ref<string | null>(null);
const treeNodes = ref<any[]>([]);

watch(
  [() => route.path, treeNodes],
  ([path]) => {
    const docPath = (path as string).replace(/^\/docs/, "") || null;
    if (!docPath || !treeNodes.value.length) {
      selectedKey.value = null;
      return;
    }
    const node = findNodeByPath(treeNodes.value, docPath);
    selectedKey.value = node?.id ?? null;
  },
  { immediate: true },
);

function findNodeByKey(nodes: any[], key: string): any | null {
  for (const node of nodes) {
    if (node.id === key) return node;
    if (node.children) {
      const found = findNodeByKey(node.children, key);
      if (found) return found;
    }
  }
  return null;
}

function findNodeByPath(nodes: any[], path: string): any | null {
  for (const node of nodes) {
    if (node.path === path) return node;
    if (node.children) {
      const found = findNodeByPath(node.children, path);
      if (found) return found;
    }
  }
  return null;
}

function onNodeSelect(nodeId: string | null) {
  if (!nodeId) return;
  const node = findNodeByKey(treeNodes.value, nodeId);
  if (node?.path) {
    const target = "/docs" + node.path;
    const query = config.value?.menu === false ? { ...route.query, menu: "false" } : route.query;
    router.push({ path: target, query });
  } else {
    selectedKey.value = null;
  }
}

onMounted(async () => {
  await axios
    .get(process.env.VUE_APP_API_PATH + `2.0/resources/folders`)
    .then((response: any) => {
      const root = Array.isArray(response.data.root) ? response.data.root : Array.isArray(response.data) ? response.data : [];
      const folder = findFoldersWithLabel(root, "docs")[0];
      folderKey.value = folder ? folder.key : null;
    })
    .catch(() => {
      folderKey.value = null;
    });

  if (!folderKey.value) {
    push404();
    return;
  }
  await axios
    .post(
      process.env.VUE_APP_API_PATH + `2.0/resources/files/download`,
      { key: folderKey.value, selectedFilesNames: ["config.json"] },
      {
        headers: {
          "Content-Type": "application/json",
          Accept: "application/zip; charset=utf-8",
        },
      },
    )
    .then(async (response: any) => {
      // Attendi che l'utente sia caricato nel store se necessario
      let user = (store.state as any).user;
      let attempts = 0;
      while (!user && attempts < 10) {
        await new Promise((resolve) => setTimeout(resolve, 100));
        user = (store.state as any).user;
        attempts++;
      }

      const sessionRole: string | undefined = user?.sessionRole;
      const defaultRole: string | undefined = user?.defaultRole;
      const userRoles: string[] = user?.roles ?? [];
      const activeRole = sessionRole || defaultRole;

      function filterNode(node: any): any | null {
        if (node == null) return null;
        if (Array.isArray(node)) {
          return node.map(filterNode).filter((n) => n !== null);
        }
        if (typeof node !== "object") return node;

        if (Array.isArray(node.roles) && node.roles.length > 0) {
          const canSee = activeRole ? node.roles.includes(activeRole) : userRoles.some((r: string) => node.roles.includes(r));
          if (!canSee) return null;
        }

        const newNode: any = { ...node };
        if (Array.isArray(node.content)) {
          const filteredContent = node.content.map(filterNode).filter((n) => n !== null);
          if (filteredContent.length) {
            newNode.content = filteredContent;
          } else {
            delete newNode.content;
            // nasconde solo i gruppi che avevano figli ma tutti filtrati per ruolo
            if (node.content.length > 0) return null;
          }
        }
        return newNode;
      }

      const filtered = filterNode(response.data);
      if (!filtered) push404();
      else {
        config.value = filtered;
        treeNodes.value = mapToQTreeNodes(filtered.content ?? []);
      }
    })
    .catch(() => {
      push404();
    });
});

function push404() {
  config.value = null;
  router.push({ name: "404" });
}
function getLogoUrl() {
  if (config.value && config.value.logo) {
    if (typeof config.value.logo === "string" && config.value.logo.startsWith("http")) {
      return config.value.logo;
    } else {
      return logoWide;
    }
  }
  return false;
}

function onLogoError(event: Event) {
  const img = event.target as HTMLImageElement | null;
  if (!img) return;
  if (img.src !== logoWide) img.src = logoWide;
}

function toggleDrawer() {
  drawer.value = !drawer.value;
}
</script>

<style lang="scss" scoped>
.no-main-menu {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  width: 100vw;
  &:deep(.q-drawer .q-drawer--left) {
    left: 0 !important;
  }
}

:deep(.q-drawer) {
  background-color: var(--kn-documentation-drawer-color-background);
  color: var(--kn-documentation-drawer-color);
  font-family: var(--kn-documentation-drawer-font-family);
  font-size: var(--kn-documentation-drawer-font-size);
}
.logo {
  max-width: 100%;
  max-height: 60px;
  margin-bottom: 1rem;
  display: block;
  text-align: center;
}
:deep(.q-tree__node-header) {
  padding: 2px 4px;
}
.doc-menu-node {
  cursor: pointer;
  color: var(--kn-documentation-drawer-color);
  font-family: var(--kn-documentation-drawer-font-family);
  font-size: var(--kn-documentation-drawer-font-size);
  font-weight: 400;
  &.no-path {
    cursor: default;
    pointer-events: none;
  }
  &.active {
    color: var(--kn-documentation-drawer-color-active);
    font-weight: 600;
    border-left: 4px solid var(--kn-documentation-drawer-color-active);
    padding-left: 4px;
  }
  &.doc-menu-section {
    color: var(--kn-documentation-drawer-header-color);
    font-family: var(--kn-documentation-drawer-header-font-family);
    font-size: var(--kn-documentation-drawer-header-font-size);
    font-weight: 600;
  }
}
</style>
