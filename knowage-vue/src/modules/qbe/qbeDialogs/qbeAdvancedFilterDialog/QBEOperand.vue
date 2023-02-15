<template>
    <div>
        <QBEOperator v-if="node?.type !== 'NODE_CONST' && node?.value !== 'PAR'" :prop-node="node" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEOperator>
        <QBEGroup v-else-if="node?.value === 'PAR'" :prop-node="node" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEGroup>
        <QBEFilter v-else-if="node?.type === 'NODE_CONST'" :prop-node="node" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEFilter>
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
    emits: ['selectedChanged', 'treeUpdated'],
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
        }
    }
})
</script>
