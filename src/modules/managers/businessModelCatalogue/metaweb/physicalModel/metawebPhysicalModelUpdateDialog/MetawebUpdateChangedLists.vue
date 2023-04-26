<template>
    <div v-if="data">
        <div class="p-m-2 p-my-4">
            <label class="kn-material-input-label">{{ data.missingColumns.length > 0 ? $t('metaweb.updatePhysicalModel.addNewColumns') : $t('metaweb.updatePhysicalModel.noNewColumns') }}</label>
            <Listbox v-if="data.missingColumns.length > 0" class="metaweb-update-changed-list " :options="data.missingColumns" :disabled="true"></Listbox>
        </div>

        <div class="p-mx-2 p-my-4">
            <label class="kn-material-input-label">{{ data.removingItems.length > 0 ? $t('metaweb.updatePhysicalModel.deletedColumns') : $t('metaweb.updatePhysicalModel.noDeletedColumns') }}</label>
            <Listbox v-if="data.removingItems.length > 0" class="metaweb-update-changed-list " :options="data.removingItems" :disabled="true"></Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iChangedData } from '../../Metaweb'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'metaweb-update-changed-lists',
    components: { Listbox },
    props: { changedItem: { type: Object as PropType<iChangedData | null> } },
    data() {
        return {
            data: null as iChangedData | null
        }
    },
    watch: {
        changedItem() {
            this.loadData()
        }
    },
    async created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.data = this.changedItem as iChangedData
        }
    }
})
</script>

<style lang="scss">
.metaweb-update-changed-list {
    border: none;
}

.metaweb-update-changed-list .p-listbox-item {
    border-bottom: 1px solid #c2c2c2 !important;
}
</style>
