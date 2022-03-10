<template>
    <div class="p-m-2">
        <div class="p-d-flex p-flex-row p-ai-center">
            <QBEOperand v-if="node?.childNodes" :propNode="node.childNodes[0]" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEOperand>
            <div v-if="node">
                <Dropdown v-if="node?.childNodes && node.childNodes.length > 0" class="kn-material-input p-mx-2" :style="{ maxWidth: '200px' }" :class="{ 'dropdown-test': selected }" v-model="node.value" :options="QBEAdvancedFilterDialogDescriptor.operatorOptions" />
            </div>
            <QBEOperand v-if="node?.childNodes" :propNode="node?.childNodes[1]" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEOperand>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dropdown from 'primevue/dropdown'
import QBEAdvancedFilterDialogDescriptor from './QBEAdvancedFilterDialogDescriptor.json'
import QBEOperand from './QBEOperand.vue'

export default defineComponent({
    name: 'qbe-operator',
    components: { Dropdown, QBEOperand },
    props: { propNode: { type: String }, selected: { type: Boolean } },
    emits: ['selectedChanged', 'treeUpdated'],
    data() {
        return {
            QBEAdvancedFilterDialogDescriptor,
            node: { value: '' } as any
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
            this.node = this.propNode as string
        }
    }
})
</script>

<style lang="scss">
.dropdown-test {
    background-color: #a9c3db;
}
</style>
