<template>
    <div :id="`sheet_${index}`" class="sheet">
        <slot />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-dashboard-tab',
    props: {
        index: {
            type: Number
        }
    },
    data() {
        return {
            isActive: false
        }
    }
})
</script>
<style lang="scss" scoped>
.sheet {
    height: 100%;
    min-width: 100%;
    overflow-y: auto;
    overflow-x: clip;
}
</style>
