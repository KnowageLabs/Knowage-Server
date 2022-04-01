<template>
    <Card class="p-m-2 p-d-flex p-flex-column">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <i class="fa fa-list p-mr-2"></i>
                    <span>{{ $t('managers.hierarchyManagement.hierarchiesTarget') }}</span>
                </template>
            </Toolbar>
        </template>

        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6 p-lg-4">
                    <Calendar class="kn-material-input" v-model="optionsDate" :manualInput="true" :showIcon="true" @dateSelect="onOptionsDateSelected" />
                </div>
                <div class="p-field p-col-6 p-lg-3">
                    <Button class="kn-button kn-button--primary" :label="$t('common.create')" :disabled="true" @click="createHierarchy" />
                </div>
                <div class="p-field p-col-6 p-lg-3">
                    <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="true" @click="saveHierarchy" />
                </div>
                <div class="p-field-checkbox p-col-6 p-lg-2">
                    <Checkbox v-model="backup" :binary="true" :disabled="true" />
                    <label class="kn-material-input-label p-ml-2"> {{ $t('managers.hierarchyManagement.backup') }}</label>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    name: 'hierarchy-management-target-card',
    components: { Card, Calendar, Checkbox },
    emits: ['optionsDateSelected'],
    data() {
        return {
            optionsDate: new Date(),
            backup: true
        }
    },
    async created() {},
    methods: {
        onOptionsDateSelected() {
            this.$emit('optionsDateSelected', this.optionsDate)
            // await this.loadNodeMetadata()
        },
        createHierarchy() {},
        saveHierarchy() {}
    }
})
</script>
