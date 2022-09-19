<template>
    <div class="custom-cell-container p-d-flex" :style="params.styleString">
        <div class="custom-cell-label">{{ params.value }}</div>
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
        return {}
    },
    mounted() {
        // console.log('\n \n cell RENDERER PARAMS \n', this.params)
    },
    methods: {}
})
</script>
