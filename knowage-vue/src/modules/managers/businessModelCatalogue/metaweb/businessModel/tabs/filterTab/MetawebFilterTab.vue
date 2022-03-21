<template>
    <div v-if="businessModel">
        <div v-for="(property, index) in businessModel.properties" :key="index">
            <div class="p-d-flex p-flex-row p-m-4" v-if="property['structural.sqlFilter']">
                <div class="p-mr-2">
                    <i class="fa fa-question-circle" v-tooltip.top="$t('metaweb.filterHelpMessage')"></i>
                </div>
                <div class=" p-fluid p-field kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.sqlExpression') }} </label>
                    <Textarea class="kn-material-input" v-model="property['structural.sqlFilter'].value" @blur="updateMeta" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '../../../Metaweb'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'metaweb-filter-tab',
    components: { Textarea },
    props: { selectedBusinessModel: { type: Object as PropType<iBusinessModel | null> }, propMeta: { type: Object } },
    emits: ['metaUpdated'],
    data() {
        return {
            meta: null as any,
            businessModel: null as iBusinessModel | null
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadMeta()
            this.loadBusinessModel()
        }
    },
    created() {
        this.loadMeta()
        this.loadBusinessModel()
    },
    methods: {
        loadMeta() {
            this.meta = this.propMeta as any
        },
        loadBusinessModel() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
        },
        updateMeta() {
            setTimeout(() => {
                this.$emit('metaUpdated')
            }, 250)
        }
    }
})
</script>
