<template>
    <div class="qbe-advanced-filter-node p-d-flex p-flex-row">
        <template v-if="expression.type === 'NODE_OP' && expression.value !== 'PAR'">
            <div v-for="(childExpression, index) in expression.childNodes" :key="index" class="p-d-flex p-flex-row">
                {{ childExpression.type }}
                <QBEAdvancedFilterNode :propExpression="childExpression" v-if="childExpression.type === 'NODE_OP'"></QBEAdvancedFilterNode>
                <QBEAdvancedFilterCard :propFilter="childExpression.details" v-else-if="childExpression.type === 'NODE_CONST'"></QBEAdvancedFilterCard>
                <QBEAdvancedFilterOperator v-if="index !== expression.childNodes.length - 1" :operator="expression.value"></QBEAdvancedFilterOperator>
            </div>
        </template>
        <template v-else-if="expression.type === 'NODE_OP' && expression.value === 'PAR'">
            <QBEAdvancedFilterGroup :propExpression="expression.childNodes[0]"></QBEAdvancedFilterGroup>
        </template>
        <template v-else-if="expression.type === 'NODE_CONST'">
            <div class="p-d-flex p-flex-row">
                <QBEAdvancedFilterCard :propFilter="expression.details"></QBEAdvancedFilterCard>
                <QBEAdvancedFilterOperator></QBEAdvancedFilterOperator>
            </div>
        </template>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import QBEAdvancedFilterCard from './QBEAdvancedFilterCard.vue'
import QBEAdvancedFilterOperator from './QBEAdvancedFilterOperator.vue'
import QBEAdvancedFilterGroup from './QBEAdvancedFilterGroup.vue'
import QBEAdvancedFilterNode from './QBEAdvancedFilterNode.vue'

export default defineComponent({
    name: 'qbe-advanced-filter-node',
    components: { QBEAdvancedFilterCard, QBEAdvancedFilterOperator, QBEAdvancedFilterGroup, QBEAdvancedFilterNode },
    props: { propExpression: { type: Object } },
    data() {
        return {
            expression: {} as any
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
            console.log('QBEAdvancedFilterNode- loadExpression() - Loaded expression: ', this.expression)
        }
    }
})
</script>

<style lang="scss">
.qbe-advanced-filter-node {
    border: 1px solid green;
    padding: 2px;
    margin: 2px;
}
</style>
