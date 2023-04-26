<template>
    <div class="active-selections-chip">
        <span v-if="showDataset">{{ activeSelection.datasetLabel }}</span>
        <span v-if="showColumn" class="p-mx-2">{{ activeSelection.columnName }}</span>
        <span>{{ formatSelectionForDisplay(activeSelection) }}</span>
        <Button v-tooltip.left="$t('common.delete')" icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" :disabled="editorMode" @click.stop="deleteSelection()" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ISelection } from '../../Dashboard'
import { formatSelectionForDisplay } from './ActiveSelectionsWidgetHelpers'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: {},
    props: {
        showDataset: Boolean,
        showColumn: Boolean,
        activeSelection: { type: Object as PropType<ISelection>, required: true },
        editorMode: { type: Boolean }
    },
    emits: ['deleteSelection'],
    setup() {},
    data() {
        return {
            formatSelectionForDisplay
        }
    },
    computed: {},
    created() {},
    updated() {},
    methods: {
        deleteSelection() {
            this.$emit('deleteSelection', this.activeSelection)
        }
    }
})
</script>
<style lang="scss" scoped>
.active-selections-chip {
    display: flex;
    flex-direction: row;
    align-items: center;
    flex-wrap: nowrap;
    padding: 2px 8px;
    background-color: #ccc;
    margin-right: 4px;
    margin-bottom: 4px;
    line-height: 30px;
    border-radius: 50px;
    outline: none;
}
</style>
