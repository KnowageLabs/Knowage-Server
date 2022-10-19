<template>
    <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-ml-auto" v-tooltip.left="$t('common.delete')" @click.stop="logMe()" />
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
        logMe() {
            console.log(this.params)
            this.params.context.componentParent.methodFromParent(this.params.node.data)
        }
    }
})
</script>
