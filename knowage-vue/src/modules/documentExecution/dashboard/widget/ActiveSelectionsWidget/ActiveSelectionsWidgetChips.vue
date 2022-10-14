<template>
    <div class="active-selections-chip">
        <span v-if="showDataset">{{ activeSelection.datasetLabel }}</span>
        <span class="p-mx-2" v-if="showColumn">{{ activeSelection.columnName }}</span>
        <span>{{ activeSelection.value?.toString() }}</span>
        <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" v-tooltip.left="$t('common.delete')" @click.stop="logMe()" />
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ISelection } from '../../Dashboard'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: {},
    props: {
        showDataset: Boolean,
        showColumn: Boolean,
        activeSelection: { type: Object as PropType<ISelection>, required: true }
    },
    emits: ['selectionDeleted'],
    computed: {},
    data() {
        return {}
    },
    setup() {},
    created() {},
    updated() {},
    methods: {
        logMe() {
            console.log('ACTIVE SELECTION', this.activeSelection)
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
