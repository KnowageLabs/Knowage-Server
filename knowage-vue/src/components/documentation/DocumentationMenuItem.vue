<template>
  <ul class="documentation-menu-level" :data-level="localLevel">
    <li v-for="(item, idx) in items || []" :key="idx" @click.stop.prevent="navigate(item.path)" :class="{ active: isActive(item) }">
      <span>{{ item.label }}</span>

      <DocumentationMenuItem v-if="item.content && item.content.length && localLevel < localMaxLevel" :items="item.content" :level="localLevel + 1" :maxLevel="localMaxLevel" />
    </li>
  </ul>
</template>

<script setup lang="ts">
import { defineProps } from "vue";
import { useRoute, useRouter } from "vue-router";

const props = defineProps<{
  items?: any[];
  level?: number;
  maxLevel?: number;
}>();

const router = useRouter();
const route = useRoute();
const localLevel = props.level ?? 1;
const localMaxLevel = props.maxLevel ?? 3;

function navigate(path?: string) {
  if (!path) return;
  router.push("/docs" + path);
}

// return true se il route corrente coincide con il path dell'item
function isActive(item: any) {
  const itemPath = item?.path ?? "";
  const full = "/docs" + itemPath;
  return route.path === full;
}
</script>

<style lang="scss" scoped>
.documentation-menu-level {
  list-style: none;
  padding-left: 0.5rem;
  margin: 0;
  li {
    &.active {
      border-left: 4px solid var(--kn-documentation-drawer-color-active);
    }
  }
}
.documentation-menu-level > li {
  cursor: pointer;
  padding: 0.25rem 0.5rem;
}
.documentation-menu-level > li.active > span {
  color: var(--kn-documentation-drawer-color-active);
  font-weight: 600;
}

.documentation-menu-level li:hover:not(:has(li:hover)) > span {
  color: var(--kn-documentation-drawer-color-active);
}

.documentation-menu-level[data-level="1"] {
  font-weight: 600;
}
.documentation-menu-level[data-level="2"] {
  padding-left: 0.5rem;
  font-weight: 400;
}
.documentation-menu-level[data-level="3"] {
  padding-left: 1rem;
  font-size: 0.95rem;
  font-weight: 400;
}
</style>
