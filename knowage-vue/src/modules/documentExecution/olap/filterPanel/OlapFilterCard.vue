<template>
    <div id="filterCard" class="p-d-flex p-flex-row p-flex-wrap">
        <div id="filter-{{filter.name}}" v-for="(filter, index) in filterCardList" :key="index" :style="panelDescriptor.style.filterCard">
            <Button icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain" />
            <!-- <span v-if="filter.hierarchies.length > 1">
                <i class="fas fa-sitemap" />
            </span> -->
            <span class="p-ml-1"> {{ filter.caption }} </span>
            <!-- <span class="md-icon-button noMargin" id="{{filter.name}}-filter">
                <i class="fas fa-filter" />
            </span> -->
            <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain p-ml-auto" />
            <!-- TODO: Tooltip for selected filters when hovering on icon -->
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
