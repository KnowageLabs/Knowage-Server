<template>
  <q-layout v-if="config" view="hHh lpR fFf" class="full-height">
    <q-drawer v-model="drawer" side="left" bordered class="drawer">
      <div class="q-pa-md">
        <div class="full-width flex justify-center">
          <img v-if="getLogoUrl()" :src="getLogoUrl()" class="logo" @error="onLogoError" />
        </div>

        <h2 v-if="config?.title">{{ config.title }}</h2>

        <DocumentationMenuItem :items="config?.content" />
      </div>
    </q-drawer>

    <q-page-container v-if="config" class="full-height scroll relative-position">
      <router-view @toggle-drawer="toggleDrawer" />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import axios from "axios";
import { onMounted, ref } from "vue";
import { findFoldersWithLabel } from "./DocumentationHelper";
import { useStore } from "vuex";
import DocumentationMenuItem from "./DocumentationMenuItem.vue"; // <- import nuovo componente
import { useRouter } from "vue-router";

const store = useStore();
const router = useRouter();

const drawer = ref(true);
const folderKey = ref<string | null>("");
const config = ref<any | null>(null);
const logoWide = process.env.VUE_APP_PUBLIC_PATH + "/images/commons/logo_knowage.svg";

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
      }
    )
    .then((response: any) => {
      const user = (store.state as any).user;
      const userRole = user?.sessionRole || user?.defaultRole;

      function filterNode(node: any): any | null {
        if (node == null) return null;
        if (Array.isArray(node)) {
          return node.map(filterNode).filter((n) => n !== null);
        }
        if (typeof node !== "object") return node;

        if (Array.isArray(node.roles) && userRole && !node.roles.includes(userRole)) {
          return null;
        }

        const newNode: any = { ...node };
        if (Array.isArray(node.content)) {
          const filteredContent = node.content.map(filterNode).filter((n) => n !== null);
          if (filteredContent.length) {
            newNode.content = filteredContent;
          } else {
            delete newNode.content;
          }
        }
        return newNode;
      }

      const filtered = filterNode(response.data);
      if (!filtered) push404();
      else {
        config.value = filtered;
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
</style>
