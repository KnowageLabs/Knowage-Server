<template>
    <div id="filterPanel" class="p-d-flex filterPanel p-ai-center p-flex-wrap" @drop="onDrop($event)" @dragover.prevent @dragenter="displayDropzone" @dragleave="hideDropzone">
        <div v-if="filterCardList?.length == 0" class="p-d-flex p-flex-row kn-flex p-jc-center">
            <InlineMessage class="kn-flex p-m-1" :style="panelDescriptor.style.noFilters" severity="info" closable="false">{{ $t('documentExecution.olap.filterPanel.filterPanelEmpty') }}</InlineMessage>
        </div>
        <FilterCard v-else :filterCardList="filterCardList" />
        <div ref="axisDropzone" class="kn-flex kn-truncated p-mr-1" :style="panelDescriptor.style.filterAxisDropzone">{{ $t('documentExecution.olap.filterPanel.drop') }}</div>
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
    watch: {
        olapProp() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },

    methods: {
        loadData() {
            this.filterCardList = this.olapProp?.filters as iOlapFilter[]
        },
        displayDropzone() {
            // @ts-ignore
            this.$refs.axisDropzone.classList.add('display-axis-dropzone')
        },
        hideDropzone() {
            // @ts-ignore
            this.$refs.axisDropzone.classList.remove('display-axis-dropzone')
        },
        onDrop(event) {
            // @ts-ignore
            this.$refs.axisDropzone.classList.remove('display-axis-dropzone')
            var data = JSON.parse(event.dataTransfer.getData('text/plain'))
            console.log('DROP MAIN FILTERS:', data)
        }
    }
})
</script>
<style lang="scss" scoped>
#filterPanel {
    min-height: 45px;
}
.display-axis-dropzone {
    display: flex !important;
}
</style>
