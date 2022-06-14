<template>
    <div class="widgetEditor-preview">
        Preview
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-editor-preview',
    props: {
        widget: {
            required: true,
            type: Object
        }
    }
})
</script>
<style lang="scss">
.widgetEditor-preview {
    border-left: 1px solid #ccc;
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
