<template>
    <div v-if="operatorValue">
        <Dropdown class="kn-material-input" v-model="operatorValue" :options="QBEAdvancedFilterDialogDescriptor.operatorOptions" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dropdown from 'primevue/dropdown'
import QBEAdvancedFilterDialogDescriptor from './QBEAdvancedFilterDialogDescriptor.json'

export default defineComponent({
    name: 'qbe-advanced-filter-operator',
    components: { Dropdown },
    props: { operator: { type: String } },
    data() {
        return {
            QBEAdvancedFilterDialogDescriptor,
            operatorValue: '' as string
        }
    },
    watch: {
        operator() {
            this.loadOperator()
        }
    },
    async created() {
        this.loadOperator()
    },
    methods: {
        loadOperator() {
            this.operatorValue = this.operator as string
        }
    }
})
</script>
