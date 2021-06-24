<template>
    <div class="kn-page-content p-grid p-m-0">
        <Card class="p-col-6 p-p-0">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.buisnessModelCatalogue.drivers') }}
                    </template>
                    <template #right>
                        <Button class="kn-button p-button-text" @click="showForm">{{ $t('managers.buisnessModelCatalogue.add') }}</Button>
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <div class="kn-list--column">
                    <div class="p-col">
                        <Listbox class="kn-list" :options="businessModelDrivers" listStyle="max-height:calc(100% - 62px)" @change="showForm">
                            <template #empty>{{ $t('common.info.noDataFound') }}</template>
                            <template #option="slotProps">
                                <div class="kn-list-item">
                                    <div class="kn-list-item-text">
                                        <span>{{ slotProps.option.label }}</span>
                                        <span class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option.parameterUrlName }}</span>
                                    </div>
                                    <Button v-if="slotProps.option.id !== businessModelDrivers[0].id" icon="fa fa-arrow-up" class="p-button-link p-button-sm" @click.stop="movePriority(slotProps.option.id, 'UP')" />
                                    <Button v-if="slotProps.option.id !== businessModelDrivers[businessModelDrivers.length - 1].id" icon="fa fa-arrow-down" class="p-button-link p-button-sm" @click.stop="movePriority(slotProps.option.id, 'DOWN')" />
                                    <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="deleteDriver(slotProps.option.id)" />
                                </div>
                            </template>
                        </Listbox>
                    </div>
                </div>
            </template>
        </Card>

        <div class="p-col-6 p-p-0 p-m-0">
            <BuisnessModelDriverDetail :businessModelId="id" :selectedDriver="selectedDriver" :formVisible="formVisible" :driverOptions="analyticalDrivers" :businessModelDrivers="businessModelDrivers"></BuisnessModelDriverDetail>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import BuisnessModelDriverDetail from './BusinessModelDriverDetail.vue'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'business-model-drivers-card',
    components: {
        BuisnessModelDriverDetail,
        Card,
        Listbox
    },
    props: {
        id: {
            type: Number,
            required: true
        },
        drivers: {
            type: Array,
            required: true
        },
        driversOptions: {
            type: Array,
            required: true
        }
    },
    data() {
        return {
            businessModelDrivers: [] as any[],
            analyticalDrivers: [] as any[],
            selectedDriver: null as any,
            formVisible: false,
            touched: false
        }
    },
    watch: {
        drivers() {
            this.selectedDriver = null
            this.loadDrivers()
        },
        driversOptions() {
            this.loadAnalyticalDrivers()
        }
    },
    created() {
        this.loadDrivers()
    },
    methods: {
        loadDrivers() {
            this.businessModelDrivers = this.drivers as any[]
            console.log(this.businessModelDrivers)
        },
        loadAnalyticalDrivers() {
            this.analyticalDrivers = this.driversOptions
        },
        showForm(event: any) {
            this.selectedDriver = event.value ?? {}

            this.selectedDriver.parameter = this.analyticalDrivers.find((driver) => driver.id === this.selectedDriver.parameter.id)

            if (!this.touched) {
                this.formVisible = true
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.formVisible = true
                    }
                })
            }
        },
        movePriority(driverId: number, direction: 'UP' | 'DOWN') {
            const currentDriverIndex = this.businessModelDrivers.findIndex((driver) => driver.id === driverId)

            if (direction === 'UP') {
                this.businessModelDrivers[currentDriverIndex - 1].priority++
                this.businessModelDrivers[currentDriverIndex].priority--

                const temp = this.businessModelDrivers[currentDriverIndex - 1]
                this.businessModelDrivers[currentDriverIndex - 1] = this.businessModelDrivers[currentDriverIndex]
                this.businessModelDrivers[currentDriverIndex] = temp
            } else {
                this.businessModelDrivers[currentDriverIndex + 1].priority--
                this.businessModelDrivers[currentDriverIndex].priority++

                const temp = this.businessModelDrivers[currentDriverIndex + 1]
                this.businessModelDrivers[currentDriverIndex + 1] = this.businessModelDrivers[currentDriverIndex]
                this.businessModelDrivers[currentDriverIndex] = temp
            }
        },

        deleteDriver(driverId: number) {
            const currentDriverIndex = this.businessModelDrivers.findIndex((driver) => driver.id === driverId)
            this.businessModelDrivers.splice(currentDriverIndex, 1)
        }
    }
})
</script>
