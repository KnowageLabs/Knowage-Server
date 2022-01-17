<template>
    {{ filter }}
    <div v-if="filter" class="p-grid p-m-2">
        <div class="p-field p-col-3">
            <label class="kn-material-input-label"> {{ $t('common.field') }} </label>
            <InputText class="kn-material-input" v-model="filter.leftOperandDescription" :disabled="true" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iFilter } from '../../QBE'

export default defineComponent({
    name: 'qbe-filter-card',
    components: {},
    props: { propFilter: { type: Object as PropType<iFilter> } },
    data() {
        return {
            filter: null as iFilter | null
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter as iFilter
        }
    }
})
</script>
