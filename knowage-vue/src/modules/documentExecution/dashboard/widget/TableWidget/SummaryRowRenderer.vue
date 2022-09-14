<template>
    <div class="custom-header-group-container" style="height: 100%; width: 100%" :style="params.styleString">
        <span class="custom-header-group-label">
            <b style="margin-right: 4px">{{ params.value ? params.summaryRows[params.rowIndex] : '' }} </b>
            {{ params.value ?? '' }}
        </span>
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
    mounted() {},
    methods: {}
})
</script>
