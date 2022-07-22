<template>
    <div class="p-d-flex">
        <AssociationsList class="kn-flex p-m-2" :dashboardAssociationsProp="dashboardAssociationsProp" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import AssociationsList from './DatasetEditorAssociationsList/DatasetEditorAssociationsList.vue'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { AssociationsList },
    props: { dashboardAssociationsProp: { required: true, type: Array } },
    emits: [],
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
