<template>
    <div>
        <!-- <h4>QBE Operand</h4> -->
        <!-- {{ node }} -->
        <QBEOperator v-if="node?.type !== 'NODE_CONST' && node?.value !== 'PAR'" :propNode="node"></QBEOperator>
        <QBEGroup v-else-if="node?.value === 'PAR'" :propNode="node" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEGroup>
        <QBEFilter v-else-if="node?.type === 'NODE_CONST'" :propNode="node" @selectedChanged="$emit('selectedChanged')"></QBEFilter>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import QBEAdvancedFilterDialogDescriptor from './QBEAdvancedFilterDialogDescriptor.json'
import QBEGroup from './QBEGroup.vue'
import QBEFilter from './QBEFilter.vue'

export default defineComponent({
    name: 'qbe-operand',
    components: { QBEGroup, QBEFilter },
    props: { propNode: { type: Object } },
    emits: ['selectedChanged'],
    data() {
        return {
            QBEAdvancedFilterDialogDescriptor,
            node: {} as any
        }
    },
    watch: {
        propNode: {
            handler() {
                this.loadNode()
            },
            deep: true
        }
    },
    async created() {
        this.loadNode()
    },
    methods: {
        loadNode() {
            this.node = this.propNode as any
            console.log('QBEOperand Loaded node: ', this.node)
        }
    }
})
</script>
