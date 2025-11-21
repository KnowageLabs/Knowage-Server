<template>
  <q-btn round flat icon="menu" @click="toggleDrawer" class="drawerButton" />
  <q-btn round flat icon="link" @click="copyLink" class="copyButton">
    <q-tooltip :delay="500">{{ $t("documentExecution.main.copyLink") }}</q-tooltip>
  </q-btn>
  <KnMarkdown v-if="markdown" :source="markdown" class="markdownContent"> </KnMarkdown>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, defineProps, defineEmit } from "vue";
import KnMarkdown from "@/components/KnMarkdown.vue";
import { findNodesByLabelPath } from "./DocumentationHelper";
import axios from "axios";
import { useQuasar } from "quasar";
import i18n from "@/App.i18n";

const { t } = i18n.global;
const $q = useQuasar();

const markdown = ref<string | null>(null);
const folderKey = ref<string | null>(null);

const props = defineProps<{ path?: string[] }>();

const emit = defineEmit<{
  (e: "toggle-drawer"): void;
}>();

onMounted(async () => {
  await loadMarkdown();
});

watch(
  () => props.path,
  async (newPath, oldPath) => {
    if (newPath !== oldPath) await loadMarkdown();
  }
);

async function loadMarkdown() {
  const folders = ["docs", ...(props.path ? props.path.slice(0, -1) : [])];
  await axios
    .get(process.env.VUE_APP_API_PATH + `2.0/resources/folders`)
    .then((response: any) => {
      const root = Array.isArray(response.data.root) ? response.data.root : Array.isArray(response.data) ? response.data : [];
      const folder = findNodesByLabelPath(root, folders)[0];
      folderKey.value = folder ? folder.key : null;
    })
    .catch(() => {
      folderKey.value = null;
    });

  const fileName = `${props.path ? props.path[props.path.length - 1] : "index.md"}`;
  await axios
    .post(
      process.env.VUE_APP_API_PATH + `2.0/resources/files/download`,
      { key: folderKey.value, selectedFilesNames: [fileName] },
      {
        headers: {
          "Content-Type": "application/json",
          Accept: "application/zip; charset=utf-8",
        },
      }
    )
    .then((response: any) => {
      markdown.value = response.data;
    });
}

function copyLink() {
  const url = window.location.href;

  navigator.clipboard.writeText(url);
  $q.notify({
    position: "top",
    type: "positive",
    message: t("documentExecution.main.urlCopiedToClipboard"),
  });
}

function toggleDrawer() {
  emit("toggle-drawer");
}
</script>

<style lang="scss" scoped>
.drawerButton {
  position: absolute;
  top: 4px;
  left: 4px;
  z-index: 1000;
  display: none;
}
.copyButton {
  position: absolute;
  right: 4px;
  top: 4px;
  z-index: 1000;
}
.markdownContent {
  position: relative;
  width: 100%;
  top: 0;
  background: var(--kn-documentation-content-background);
  color: var(--kn-documentation-content-color);
  font-family: var(--kn-documentation-content-font-family);
  font-size: var(--kn-documentation-content-font-size);
  margin-top: 0;
  padding-left: 20%;
  padding-right: 20%;
  &:deep(h1),
  &:deep(h2),
  &:deep(h3) {
    color: var(--kn-documentation-content-heading-color);
  }
  &:deep(a) {
    color: var(--kn-documentation-content-link-color);
  }
  &:deep(pre) {
    border-radius: var(--kn-documentation-content-code-border-radius);
    border: 1px solid #ccc;
    padding: 4px;
    background-color: var(--kn-documentation-content-code-background);
    color: var(--kn-documentation-content-code-color);
  }
  &:deep(table) {
    border-collapse: collapse;
    td,
    th {
      border: 1px solid #ccc;
      padding: 6px 8px;
    }
  }
  &:deep(h1:first-of-type) {
    padding-top: 10px;
    margin-top: 0;
  }
}

@media screen and (max-width: 1200px) {
  .markdownContent {
    padding-left: 10%;
    padding-right: 10%;
  }
}
@media screen and (max-width: 1066px) {
  .drawerButton {
    display: inline-flex;
  }
}
@media screen and (max-width: 900px) {
  .markdownContent {
    padding-left: 50px;
    padding-right: 50px;
  }
}
</style>
