<template>
    <Card class="p-m-2">
        <template #content>
            <form v-if="timespan" class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <InputText class="kn-material-input" v-model="timespan.name" maxLength="100" @input="$emit('touched')" />
                        <label class="kn-material-input-label"> {{ $t('common.name') }} *</label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="timespan.type" :options="timespanDescriptor.typeValues" optionValue="value" optionLabel="label" @change="onTypeChange">
                            <template #value="slotProps">
                                <div v-if="slotProps.value">
                                    <span class="timespan-type-value">{{ slotProps.value }}</span>
                                </div>
                            </template>
                            <template #option="slotProps">
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </template>
                        </Dropdown>
                        <label class="kn-material-input-label"> {{ $t('common.type') }} *</label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown class="kn-material-input" v-model="timespan.category" :options="categories" optionValue="VALUE_ID" optionLabel="VALUE_NM" @change="$emit('touched')"> </Dropdown>
                        <label class="kn-material-input-label"> {{ $t('common.category') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iCategory } from './Timespan'
import Dropdown from 'primevue/dropdown'
import timespanDescriptor from './TimespanDescriptor.json'
import Card from 'primevue/card'

export default defineComponent({
    name: 'timespan-form',
    components: { Dropdown, Card },
    props: { propTimespan: { type: Object as PropType<iTimespan | null> }, categories: { type: Array as PropType<iCategory[]> } },
    emits: ['touched'],
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
        },
        onTypeChange() {
            if (this.timespan) {
                this.$emit('touched')
                this.timespan.definition = []
            }
        }
    }
})
</script>

<style lang="scss">
.timespan-type-value {
    text-transform: capitalize;
}
</style>
