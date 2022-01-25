<template>
    <div>
        <!-- <h4>QBE Operator</h4> -->
        <!-- <operand node="node.childNodes[0]" layout="row" layout-align="center center"></operand> -->
        <div class="p-d-flex p-flex-row">
            <QBEOperand :propNode="node?.childNodes[0]"></QBEOperand>
            <div v-if="node">
                <Dropdown class="kn-material-input" :style="{ maxWidth: '200px' }" v-model="node.value" :options="QBEAdvancedFilterDialogDescriptor.operatorOptions" />
            </div>
            <QBEOperand :propNode="node?.childNodes[1]"></QBEOperand>
        </div>
        <!-- <operand node="node.childNodes[1]" layout="row" layout-align="center center"></operand> -->
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
    props: { propNode: { type: String } },
    data() {
        return {
            QBEAdvancedFilterDialogDescriptor,
            node: { value: '' } as any
        }
    },
    watch: {
        propNode() {
            this.loadNode()
        }
    },
    async created() {
        this.loadNode()
    },
    methods: {
        loadNode() {
            this.node = this.propNode as string
            console.log('QBEOperator Loaded node: ', this.node)
        }
    }
})
</script>
