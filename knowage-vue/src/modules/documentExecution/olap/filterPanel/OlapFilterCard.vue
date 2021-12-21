<template>
    <div id="filterCard" class="p-d-flex p-flex-row p-flex-wrap">
        <div id="filter-{{filter.name}}" v-for="(filter, index) in filterCardList" :key="index" :style="panelDescriptor.style.filterCard">
            <Button v-if="filter.hierarchies.length > 1" icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain" />
            <span class="p-ml-1"> {{ filter.caption }} </span>
            <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain p-ml-auto" />
            <!-- TODO: Tooltip for selected filters when hovering on icon and knowage magenta button color if filter is selected -->
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import panelDescriptor from './OlapFilterPanelDescriptor.json'

export default defineComponent({
    components: {},
    props: { filterCardList: { type: Array, required: true } },
    emits: [],
    data() {
        return {
            panelDescriptor
        }
    },
    created() {},
    methods: {
        loadData() {}
    }
})
</script>
