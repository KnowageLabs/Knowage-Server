<template>
    <div v-if="expression" class="qbe-advanced-filter-group p-d-flex p-flex-row">
        {{ expression.type }}
        <QBEAdvancedFilterNode :propExpression="expression"></QBEAdvancedFilterNode>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import QBEAdvancedFilterNode from './QBEAdvancedFilterNode.vue'

export default defineComponent({
    name: 'qbe-advanced-filter-group',
    components: { QBEAdvancedFilterNode },
    props: { propExpression: { type: Object } },
    data() {
        return {
            expression: null as any
        }
    },
    watch: {
        propExpression() {
            this.loadExpression()
        }
    },
    async created() {
        this.loadExpression()
    },
    methods: {
        loadExpression() {
            this.expression = this.propExpression
            console.log('QBEAdvancedFilterGroup - loadExpression() - Loaded expression: ', this.expression)
        }
    }
})
</script>

<style lang="scss">
.qbe-advanced-filter-group {
    border: 1px solid red;
    padding: 2px;
    margin: 2px;
}
</style>
