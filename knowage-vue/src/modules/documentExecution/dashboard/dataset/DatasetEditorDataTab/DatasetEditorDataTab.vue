<template>
    <div class="p-d-flex">
        <DataList class="kn-flex p-my-2 p-ml-2 p-mr-1" />
        <DataDetail class="kn-flex p-my-2 p-mr-2 p-ml-1" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DataList from './DatasetEditorDataList/DatasetEditorDataList.vue'
import DataDetail from './DatasetEditorDataDetail/DatasetEditorDataDetail.vue'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { DataList, DataDetail },
    props: {},
    emits: [],
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
