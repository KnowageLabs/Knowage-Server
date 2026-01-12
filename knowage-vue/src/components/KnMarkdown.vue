<template>
  <div v-html="renderedHtml"></div>
</template>

<script lang="ts">
import { defineComponent, computed } from "vue";
import MarkdownIt from "markdown-it";
import DOMPurify from "dompurify";

const md = new MarkdownIt({ html: true, linkify: true, typographer: true });

export default defineComponent({
  name: "MarkdownRenderer",
  props: {
    source: { type: String, required: true },
  },
  setup(props) {
    const renderedHtml = computed(() => DOMPurify.sanitize(md.render(props.source || "")));
    return { renderedHtml };
  },
});
</script>
