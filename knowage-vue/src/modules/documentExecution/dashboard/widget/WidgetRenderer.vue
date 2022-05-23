<template>
    <div class="widget-renderer">
        {{ widget.type }}
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'widget-renderer',
    props: {
        widget: {
            required: true,
            type: Object
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-renderer {
    width: 100%;
    height: 100%;
    background-color: #ccc;
    overflow: hidden;
}
</style>
