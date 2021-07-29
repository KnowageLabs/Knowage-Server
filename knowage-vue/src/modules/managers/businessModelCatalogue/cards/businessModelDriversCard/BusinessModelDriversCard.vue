span<template>
    <div class="kn-page-content p-grid p-m-0">
        <Card class="p-col-6 p-p-0">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.businessModelManager.drivers') }}
                    </template>
                    <template #right>
                        <Button class="kn-button p-button-text" @click="showForm">{{ $t('managers.businessModelManager.add') }}</Button>
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <Listbox class="kn-list" :options="businessModelDrivers" listStyle="max-height:calc(100% - 62px)" @change="showForm" data-test="driver-list">
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item">
                            <div class="kn-list-item-text">
                                <span
                                    :class="{
                                        'driver-invalid': slotProps.option.numberOfErrors > 0
                                    }"
                                    >{{ slotProps.option.label }}</span
                                >
                                <span class="kn-list-item-text-secondary kn-truncated">{{ slotProps.option.parameterUrlName }}</span>
                            </div>
                            <Button v-if="slotProps.option.id !== businessModelDrivers[0].id" icon="fa fa-arrow-up" class="p-button-link p-button-sm" @click.stop="movePriority(slotProps.option.id, 'UP')" />
                            <Button v-if="slotProps.option.id !== businessModelDrivers[businessModelDrivers.length - 1].id" icon="fa fa-arrow-down" class="p-button-link p-button-sm" @click.stop="movePriority(slotProps.option.id, 'DOWN')" />
                            <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="deleteDriverConfirm(slotProps.index)" />
                        </div>
                    </template>
                </Listbox>
            </template>
        </Card>

        <div class="p-col-6 p-p-0 p-m-0">
            <BuisnessModelDriverDetail :businessModelId="id" :selectedDriver="selectedDriver" :formVisible="formVisible" :driverOptions="analyticalDrivers" :businessModelDrivers="businessModelDrivers"></BuisnessModelDriverDetail>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModelDriver } from '../../BusinessModelCatalogue'
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
    emits: ['delete'],
    data() {
        return {
            businessModelDrivers: [] as iBusinessModelDriver[],
            driversForDelete: [] as iBusinessModelDriver[],
            analyticalDrivers: [] as any[],
            selectedDriver: null as iBusinessModelDriver | null,
            formVisible: false
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
        this.loadAnalyticalDrivers()
    },
    methods: {
        loadDrivers() {
            this.businessModelDrivers = this.drivers as any[]
        },
        loadAnalyticalDrivers() {
            this.analyticalDrivers = this.driversOptions
        },
        showForm(event: any) {
            if (event.value) {
                this.selectedDriver = event.value
            } else {
                this.selectedDriver = { biMetaModelID: this.id, modifiable: 0, priority: this.businessModelDrivers.length + 1, required: true, visible: true, multivalue: false, numberOfErrors: 1 }
                this.businessModelDrivers.push(this.selectedDriver)
            }

            if (this.selectedDriver && this.selectedDriver.parameter) {
                this.selectedDriver.parameter = this.analyticalDrivers.find((driver) => {
                    return driver.id === this.selectedDriver?.parameter?.id
                })
            }

            this.formVisible = true
        },
        movePriority(driverId: number, direction: 'UP' | 'DOWN') {
            const currentDriverIndex = this.businessModelDrivers.findIndex((driver) => driver.id === driverId)

            if (direction === 'UP') {
                this.businessModelDrivers[currentDriverIndex - 1].priority++
                this.businessModelDrivers[currentDriverIndex - 1].status = 'CHANGED'
                this.businessModelDrivers[currentDriverIndex].priority--
                this.businessModelDrivers[currentDriverIndex].status = 'CHANGED'

                const temp = this.businessModelDrivers[currentDriverIndex - 1]
                this.businessModelDrivers[currentDriverIndex - 1] = this.businessModelDrivers[currentDriverIndex]
                this.businessModelDrivers[currentDriverIndex] = temp
            } else {
                this.businessModelDrivers[currentDriverIndex + 1].priority--
                this.businessModelDrivers[currentDriverIndex + 1].status = 'CHANGED'
                this.businessModelDrivers[currentDriverIndex].priority++
                this.businessModelDrivers[currentDriverIndex].status = 'CHANGED'

                const temp = this.businessModelDrivers[currentDriverIndex + 1]
                this.businessModelDrivers[currentDriverIndex + 1] = this.businessModelDrivers[currentDriverIndex]
                this.businessModelDrivers[currentDriverIndex] = temp
            }
        },
        deleteDriverConfirm(index: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDriver(index)
            })
        },
        deleteDriver(index: number) {
            if (this.businessModelDrivers[index].id) {
                this.driversForDelete.push(this.businessModelDrivers[index])
            }
            this.businessModelDrivers.splice(index, 1)
            this.$emit('delete', this.driversForDelete)
            this.selectedDriver = null
        }
    }
})
</script>

<style lang="scss" scoped>
.driver-invalid {
    color: red;
}
</style>
