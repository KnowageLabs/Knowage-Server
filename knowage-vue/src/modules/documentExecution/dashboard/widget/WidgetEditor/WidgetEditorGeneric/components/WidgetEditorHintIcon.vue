<template>
  <i
    class="pi pi-question-circle kn-cursor-pointer"
    :class="class"
    v-tooltip.top="settings.hint ? $t(settings.hint) : ''"
  ></i>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "widget-editor-hint-icon",
  components: {},
  props: {
    class: { type: String },
    settings: { type: Object, required: true },
  },
  data() {
    return {};
  },
  emits: [],
  mounted() {},
  methods: {},
});
</script>
