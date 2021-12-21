<template>
    <div id="filterPanel" class="p-d-flex filterPanel p-ai-center">
        <div v-if="filterCardList?.length == 0" class="p-d-flex p-flex-row kn-flex p-jc-center">
            <InlineMessage class="kn-flex p-m-1" :style="panelDescriptor.style.noFilters" severity="info" closable="false">{{ $t('documentExecution.olap.filterPanel.filterPanelEmpty') }}</InlineMessage>
        </div>
        <FilterCard :filterCardList="filterCardList" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOlapFilter } from '@/modules/documentExecution/olap/Olap'
import panelDescriptor from './OlapFilterPanelDescriptor.json'
import InlineMessage from 'primevue/inlinemessage'
import FilterCard from './OlapFilterCard.vue'

export default defineComponent({
    components: { InlineMessage, FilterCard },
    props: { olapProp: { type: Object, required: true } },
    emits: [],
    data() {
        return {
            panelDescriptor,
            filterCardList: [] as iOlapFilter[]
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.filterCardList = this.olapProp?.filters as iOlapFilter[]
        }
    }
})
</script>
<style lang="scss" scoped>
#filterPanel {
    min-height: 45px;
}
</style>
