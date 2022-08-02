<template>
    <Accordion class="p-mb-3">
        <AccordionTab :header="$t('common.parameters')">
            <!-- PARAMETERS ---------------- -->
            <div id="parameters" v-for="(parameter, index) of selectedDatasetProp.parameters" :key="index" class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" :disabled="true" v-model="parameter.name" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.parameter') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-4">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" :options="parameterTypes" v-model="parameter.modelType" />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }}</label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-lg-4 p-d-flex">
                    <span class="p-float-label kn-flex">
                        <InputText id="label" class="kn-material-input" type="text" v-model="parameter.value" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.value') }} </label>
                    </span>
                    <Button v-if="parameter.modelType === 'dynamic' && documentDriversProp && documentDriversProp.filterStatus.length > 0" icon="fa-solid fa-link" class="p-button-text p-button-rounded p-button-plain p-as-end" @click.stop="showMenu($event, parameter.name)" />
                </div>
            </div>

            <!-- DRIVERS ---------------- -->
            <!-- <div id="drivers" v-for="(driver, index) of selectedDatasetProp.drivers" :key="index" class="p-field p-col-12">
                <span v-if="driver.showOnPanel == 'true'">
                    {{ driver.label }}
                </span>
            </div> -->
        </AccordionTab>
    </Accordion>

    <Menu id="parameterPickerMenu" ref="parameterPickerMenu" :model="menuButtons" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card, Accordion, AccordionTab, Dropdown, Menu },
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any }, documentDriversProp: { type: Array as any } },
    emits: [],
    data() {
        return {
            parameterTypes: ['static', 'dynamic'],
            menuButtons: [] as any
        }
    },
    setup() {},
    async created() {},
    methods: {
        showMenu(event, parameter) {
            this.createMenuItems(parameter)
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.parameterPickerMenu.toggle(event)
        },
        createMenuItems(paramName) {
            this.menuButtons = this.documentDriversProp.filterStatus.map((driver) => {
                return { label: driver.label, urlName: driver.urlName, command: () => this.addDriverValueToParameter(driver.label, paramName) }
            })
        },
        addDriverValueToParameter(driverUrl, paramName) {
            this.selectedDatasetProp.parameters.find((parameter) => parameter.name === paramName).value = '$P{' + driverUrl + '}'
        }
    }
})
</script>
