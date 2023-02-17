<template>
    <Button v-tooltip.left="$t('common.delete')" icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click.stop="deleteSelection()" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import helpersDecriptor from '../WidgetEditor/helpers/tableWidget/TableWidgetHelpersDescriptor.json'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return { helpersDecriptor }
    },
    created() {},
    methods: {
        deleteSelection() {
            this.params.context.componentParent.deleteSelection(this.params.node.data)
        }
    }
})
</script>
