<template>
  <q-list style="min-width: 100px" @mouseenter="emitOver" @mouseleave="emitLeave">
    <q-item v-for="(item, i) in items" :key="i" clickable>
      <q-item-section @click.stop="itemClick($event, item)">
        {{ $t(item.label) }}
        <q-tooltip anchor="top middle" :delay="1000">{{ item.descr || item.label }}</q-tooltip>
      </q-item-section>

      <q-item-section side v-if="item.items">
        <q-icon name="keyboard_arrow_right" />
      </q-item-section>
      <q-menu v-model="item.visibleChild" anchor="top right" self="top left">
        <MainMenuTieredMenu :items="item.items" @link="itemClick"></MainMenuTieredMenu>
      </q-menu>
    </q-item>
  </q-list>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "kn-menu-tiered",
  props: ["items"],
  emits: ["link", "over", "leave"],
  methods: {
    itemClick(e, item) {
      if (item) e.item = item;
      this.$emit("link", e);
    },
    emitOver() {
      this.$emit("over");
    },
    emitLeave() {
      this.$emit("leave");
    },
  },
});
</script>
