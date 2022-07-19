<template>
    <div class="datasetEditor-preview">Preview</div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset editing preview.
 */
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'dataset-editor-preview',
    props: {
        dataset: {
            required: true,
            type: Object
        }
    }
})
</script>
<style lang="scss">
.datasetEditor-preview {
    border-left: 1px solid #ccc;
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
