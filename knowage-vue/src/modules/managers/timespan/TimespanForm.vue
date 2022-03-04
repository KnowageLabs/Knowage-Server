<template>
    <div v-if="timespan" class="p-fluid p-formgrid  p-grid p-ai-center p-m-2">
        <div class="p-field p-float-label p-col-6 p-mt-4">
            <InputText class="kn-material-input" v-model="timespan.name" maxLength="100" />
            <label class="kn-material-input-label"> {{ $t('common.name') }} </label>
        </div>
        <div class="p-field  p-col-6">
            <label class="kn-material-input-label"> {{ $t('common.type') }} </label>
            <Dropdown class="kn-material-input" v-model="timespan.type" :options="timespanDescriptor.typeValues" optionValue="value" optionLabel="label">
                <template #value="slotProps">
                    <div v-if="slotProps.value">
                        <span class="timespan-type-value">{{ slotProps.value }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template></Dropdown
            >
        </div>
        <div class="p-col-6">
            <label class="kn-material-input-label"> {{ $t('common.category') }} </label>
            <Dropdown class="kn-material-input" v-model="timespan.category" :options="categories" optionValue="VALUE_ID" optionLabel="VALUE_NM"> </Dropdown>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iCategory } from './Timespan'
import Dropdown from 'primevue/dropdown'
import timespanDescriptor from './TimespanDescriptor.json'

export default defineComponent({
    name: 'timespan-form',
    components: { Dropdown },
    props: { propTimespan: { type: Object as PropType<iTimespan | null> }, categories: { type: Array as PropType<iCategory[]> } },
    data() {
        return {
            timespanDescriptor,
            timespan: null as iTimespan | null
        }
    },
    watch: {
        propTimespan() {
            this.loadTimespan()
        }
    },
    created() {
        this.loadTimespan()
    },
    methods: {
        loadTimespan() {
            this.timespan = this.propTimespan as iTimespan
            console.log('loadTimespan() - LOADED TIMESPAN: ', this.timespan)
        }
    }
})
</script>

<style lang="scss">
.timespan-type-value {
    text-transform: capitalize;
}
</style>
