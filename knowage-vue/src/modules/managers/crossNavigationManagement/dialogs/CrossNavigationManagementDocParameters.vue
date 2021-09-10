<template>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.crossNavigationManagement.availableIO') }}
            </template>
        </Toolbar>
        <div class="p-inputgroup p-mt-3" v-if="navigation.fromPars">
            <span class="p-float-label">
                <InputText class="kn-material-input" type="text" v-model.trim="fixedValue" />
                <label class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.fixedValue') }} </label>
            </span>
            <FabButton icon="fas fa-plus" class="fab-button p-mt-3 p-ml-2" @click.stop="addFixedValue" />
        </div>
        <Listbox :options="navigation.fromPars" v-if="navigation.fromPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="p-d-flex card" draggable="true" @dragstart="onDragStart($event, slotProps.option)">
                    <i class="pi pi-bars p-mr-2"> </i>
                    <div>{{ slotProps.option.name }}</div>
                    <div class="p-ml-auto">{{ $t(dialogDescriptor.parType[slotProps.option.type].label) }}</div>
                </div>
            </template>
        </Listbox>
    </div>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.crossNavigationManagement.availableInput') }}
            </template>
        </Toolbar>
        <Listbox :options="navigation.toPars" v-if="navigation.toPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="p-d-flex card" v-if="slotProps.option.links && slotProps.option.links.length > 0">
                    <div>{{ slotProps.option.links[0].name }} <i class="fa fa-link"> </i> {{ slotProps.option.name }}</div>
                    <i class="fa fa-times-circle p-mr-2 p-ml-auto" @click="removeLink(slotProps.option.id)"> </i>
                </div>
                <div class="p-d-flex card" @drop="link($event, slotProps.option)" @dragover.prevent v-else>
                    <div>{{ slotProps.option.name }}</div>
                    <div class="p-ml-auto">{{ $t(dialogDescriptor.parType[slotProps.option.type].label) }}</div>
                </div>
            </template>
        </Listbox>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import FabButton from '@/components/UI/KnFabButton.vue'
import dialogDescriptor from './CrossNavigationManagementDialogDescriptor.json'
export default defineComponent({
    name: 'cross-navigation-detail',
    components: { Listbox, FabButton },
    props: {
        selectedNavigation: {
            type: Object
        }
    },
    data() {
        return {
            navigation: {} as any,
            dialogDescriptor,
            fixedValue: ''
        }
    },
    created() {
        if (this.selectedNavigation) {
            this.navigation = this.selectedNavigation
        }
    },
    watch: {
        selectedNavigation() {
            if (this.selectedNavigation) {
                this.navigation = this.selectedNavigation
            }
        }
    },
    methods: {
        addFixedValue() {
            if (this.fixedValue != '') {
                if (!this.navigation.fromPars) this.navigation.fromPars = []
                this.navigation.fromPars.push({ id: this.navigation.simpleNavigation.fromDocId, name: this.fixedValue, type: 2, fixedValue: this.fixedValue })
                this.fixedValue = ''
            }
        },
        onDragStart(event: any, param: any) {
            event.dataTransfer.setData('text/plain', JSON.stringify(param))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        link(event: any, item: any) {
            const param = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (param.type === 2 || param.parType === item.parType) {
                item.links = [param]
            } else {
                this.$store.commit('setInfo', {
                    title: this.$t('managers.crossNavigationManagement.incompatibleTypes'),
                    msg: this.$t('managers.crossNavigationManagement.incompatibleTypesMessage', { originParam: param.name, targetParam: item.name })
                })
            }
        },
        removeLink(id) {
            this.navigation.toPars.forEach((param) => {
                if (param.id === id) {
                    param.links = []
                }
            })
        }
    }
})
</script>
<style lang="scss" scoped>
//::v-deep(.p-listbox) {
// .p-listbox-item {
//     background: #b4b4b4;
// }
//}
</style>
