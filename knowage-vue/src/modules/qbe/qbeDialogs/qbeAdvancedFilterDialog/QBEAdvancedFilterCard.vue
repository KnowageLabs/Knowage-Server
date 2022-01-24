<template>
    <div v-if="filter" class="qbe-advanced-filter-card">
        <!-- {{ filter }} -->
        <h4>{{ filter.leftOperandAlias }}</h4>
        <p>
            <span class="qbe-advanced-filter-lowercase">{{ filter.operator + ' ' }}</span
            ><span class="qbe-advanced-filter-lowercase">{{ filter.rightOperandValue }}</span>
        </p>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'qbe-advanced-filter-card',
    components: {},
    props: { propFilter: { type: Object } },
    data() {
        return {
            filter: null as any
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        }
    },
    async created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter
            console.log('QBEAdvancedFilterCard - loadFilter() - loaded filter: ', this.filter)
        }
    }
})
</script>

<style lang="scss">
.qbe-advanced-filter-lowercase {
    text-transform: lowercase;
}

.qbe-advanced-filter-card {
    max-width: 200px;
    background-color: #d9d9d9;
}

.qbe-advanced-filter-card:hover {
    cursor: pointer;
    background-color: rgb(123, 123, 197);
}
</style>
