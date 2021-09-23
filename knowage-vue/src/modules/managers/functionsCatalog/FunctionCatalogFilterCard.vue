<template>
    <Card class="filter-card">
        <template #content>
            <!-- <div class="p-d-flex p-flex-column p-ai-center filter-container p-p-0" :style="{ 'background-image': 'url(' + require(filter.valueDescription) + ')' }"> -->
            <div class="p-d-flex p-flex-column p-ai-center filter-container p-p-0" :class="{ active: filter.active }" :style="{ 'background-image': 'url(' + require('@/assets/images/functionCatalog/text.png') + ')' }" @click="$emit('selected', filter)">
                <span class="filter-value">{{ filter.valueCd }}</span>
                <span class="filter-domain-name">{{ filter.domainName }}</span>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFilter } from './FunctionsCatalog'
import Card from 'primevue/card'
import functionsCatalogFilterCardDescriptor from './FunctionsCatalogFilterCardDescriptor.json'

export default defineComponent({
    name: 'functions-catalog-filter-card',
    components: { Card },
    props: { propFilter: { type: Object } },
    emits: ['selected'],
    data() {
        return {
            functionsCatalogFilterCardDescriptor,
            filter: {} as iFilter
        }
    },
    watch: {
        propFilter: {
            handler() {
                this.loadFilter()
            },
            deep: true
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = { ...this.propFilter } as iFilter
            this.filter.valueDescription = this.functionsCatalogFilterCardDescriptor.filterImagesMap[this.filter.valueDescription]
            console.log('LOADED FILTER: ', this.filter)
        }
    }
})
</script>

<style lang="scss">
.filter-value {
    text-transform: uppercase;
}

.filter-domain-name {
    margin-top: 1rem;
    color: #999;
    font-size: 0.8rem;
}

.filter-container {
    cursor: pointer;
    background-repeat: no-repeat;
    background-position: right -20px top -10px;
    background-color: #fff;
}

.active {
    outline: none;
    background-color: #b4cbdf;
}

.filter-card .p-card-body,
.filter-card .p-card-body .p-card-content {
    padding: 0;
}
</style>
