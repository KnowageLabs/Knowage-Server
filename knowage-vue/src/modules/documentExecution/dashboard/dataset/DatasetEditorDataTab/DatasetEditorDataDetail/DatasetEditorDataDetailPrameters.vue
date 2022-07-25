<template>
    <!-- <Card>
        <template #title> Parameters Card </template>
        <template #content>
            Dashboard -------------
            <br />
            {{ dashboardDatasetsProp }}
            <br />
            Selected -----------------
            <br />
            {{ selectedDatasetProp.parameters }}
        </template>
    </Card> -->

    <Accordion>
        <AccordionTab header="Parameters">
            Dashboard -------------
            <br />
            {{ dashboardDatasetsProp }}
            <br />
            Selected -----------------
            <br />
            {{ selectedDatasetProp.parameters }}

            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" :disabled="true" v-model="selectedDatasetProp.label" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.parameter') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-3">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" :options="parameterTypes" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }}</label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.value') }} </label>
                    </span>
                </div>
                <!-- <div class="p-field p-col-12 p-lg-1">
                    <span class="p-float-label">
                        <Button icon="pi pi-plus-circle" class="p-button-outlined"></Button>
                    </span>
                </div> -->
            </form>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card, Accordion, AccordionTab, Dropdown },
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any } },
    emits: [],
    data() {
        return {
            parameterTypes: ['static', 'dynamic']
        }
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
