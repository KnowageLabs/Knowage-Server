<template>
    <div id="kn-parameter-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                <div id="kn-parameter-sidebar-toolbar-icons-container" class="p-d-flex p-flex-row p-jc-around">
                    <i class="fa fa-eraser kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.resetParametersTooltip')"></i>
                    <i class="pi pi-pencil kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.savedParametersTooltip')"></i>
                    <i class="fas fa-save kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.saveParametersFromStateTooltip')"></i>
                </div>
            </template>
        </Toolbar>

        <div v-if="parameters" class="p-fluid p-m-4">
            <div v-for="(parameter, index) in parameters.filterStatus" :key="index">
                <!-- Manual Text/Number Input -->
                <div class="p-field" v-if="(parameter.type === 'STRING' || parameter.type === 'NUM') && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <InputText
                        class="kn-material-input p-inputtext-sm"
                        :type="parameter.type === 'NUM' ? 'number' : 'text'"
                        v-model="parameter.parameterValue[0].value"
                        :class="{
                            'p-invalid': parameter.mandatory && !parameter.parameterValue[0].value
                        }"
                    />
                </div>

                <!-- Date -->
                <div class="p-field" v-if="parameter.type === 'DATE' && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Calendar
                        v-model="parameter.parameterValue[0].value"
                        :showButtonBar="true"
                        :showIcon="true"
                        :manualInput="true"
                        :class="{
                            'p-invalid': parameter.mandatory && !parameter.parameterValue[0].value
                        }"
                    />
                </div>

                <!-- TODO: NEMA! Date Range -->

                <!-- Lov list radio input -->
                <div class="p-field" v-if="parameter.selectionType == 'LIST' && !parameter.multivalue && parameter.showOnPanel == 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-column">
                        <div class="p-field-radiobutton" v-for="(defaultParameter, index) in parameter.defaultValues" :key="index">
                            <RadioButton v-if="defaultParameter.isEnabled" :value="defaultParameter.value" v-model="parameter.parameterValue" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="p-fluid p-d-flex p-flex-row p-m-2">
            <Button class="kn-button kn-button--primary" @click="$emit('execute')"> {{ $t('common.execute') }}</Button>
            <Button class="kn-button kn-button--primary" icon="fa fa-chevron-down" @click="toggle($event)" />
            <Menu ref="menu" :model="executeMenuItems" :popup="true" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import Menu from 'primevue/menu'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'kn-parameter-sidebar',
    components: { Calendar, Menu, RadioButton },
    props: { filtersData: { type: Object } },
    emits: ['execute', 'exportCSV'],
    data() {
        return {
            parameters: null as any,
            executeMenuItems: [] as any[]
        }
    },
    watch: {
        filtersData() {
            this.loadParameters()
        }
    },
    created() {
        this.loadParameters()
    },
    methods: {
        loadParameters() {
            this.parameters = this.filtersData
            console.log('LOADED PARAMETERS: ', this.parameters)
        },
        resetParameterValue(parameter: any) {
            console.log('RESET PARAMETER VALUE BEFORE: ', parameter)
            parameter.parameterValue[0].value = parameter.driverDefaultValue[0].value
            console.log('RESET PARAMETER VALUE AFTER: ', parameter)
        },
        toggle(event: any) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.executeMenuItems = []
            this.executeMenuItems.push({ label: this.$t('common.exportCSV'), command: () => this.$emit('exportCSV') })
        }
    }
})
</script>

<style lang="scss">
#kn-parameter-sidebar-toolbar .p-toolbar-group-left {
    width: 100%;
}

#kn-parameter-sidebar-toolbar-icons-container {
    width: 100%;
}

#kn-parameter-sidebar {
    z-index: 150;
    background-color: white;
    height: 100%;
    width: 350px;
}

.parameter-clear-icon {
    margin-left: auto;
}
</style>
