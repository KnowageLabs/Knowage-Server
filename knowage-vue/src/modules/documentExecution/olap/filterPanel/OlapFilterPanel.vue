<template>
    <div id="filterpanel" class="p-d-flex "></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import panelDescriptor from './OlapFilterPanelDescriptor.json'

export default defineComponent({
    components: {},
    props: {},
    emits: [],
    data() {
        return {
            panelDescriptor
        }
    },
    created() {},
    methods: {}
})
</script>
