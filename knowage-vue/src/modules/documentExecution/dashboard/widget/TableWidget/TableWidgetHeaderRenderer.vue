<template>
    <div class="custom-header-container" style="height: 100%; width: 100%" :style="params.styleString">
        <div class="custom-header-label">{{ params.displayName }}</div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            ascSort: null as any,
            descSort: null as any,
            noSort: null as any
        }
    },
    mounted() {
        console.log('\n \n HEADER RENDERER PARAMS \n', this.params)
    },
    methods: {}
})
</script>
