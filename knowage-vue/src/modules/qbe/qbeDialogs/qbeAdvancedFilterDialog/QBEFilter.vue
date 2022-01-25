<template>
    <div class="drop-zone" style="width: 10px;"></div>
    <div>
        <QBEFilterDetail :details="node.details"></QBEFilterDetail>
    </div>
    <div class="drop-zone" style="width: 10px;"></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import QBEFilterDetail from './QBEFilterDetail.vue'

export default defineComponent({
    name: 'qbe-filter',
    components: { QBEFilterDetail },
    props: { node: { type: Object } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
