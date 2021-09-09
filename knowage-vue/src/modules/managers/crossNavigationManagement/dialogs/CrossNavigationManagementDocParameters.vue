<template>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.crossNavigationManagement.availableIO') }}
            </template>
        </Toolbar>
        <div class="p-inputgroup p-mt-3">
            <span class="p-float-label">
                <InputText class="kn-material-input" type="text" v-model.trim="fixedValue" />
                <label class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.fixedValue') }} </label>
            </span>
            <FabButton icon="fas fa-plus" class="fab-button p-mt-3 p-ml-2" @click.stop="addFixedValue" />
        </div>
        <Listbox :options="navigation.fromPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="p-d-flex card" draggable="true">
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
        <Listbox :options="navigation.toPars">
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <template #option="slotProps">
                <div class="p-d-flex card" @drop="link" @dragover.prevent>
                    <div v-if="slotProps.option.links && slotProps.option.links.length > 0">{{ slotProps.option.links[0].name }} <i class="fa fa-link"> </i> {{ slotProps.option.name }}</div>
                    <div v-else>{{ slotProps.option.name }}</div>
                    <i class="fa fa-times-circle p-mr-2 p-ml-auto" v-if="slotProps.option.links && slotProps.option.links.length > 0"> </i>
                    <div class="p-ml-auto" v-else>{{ $t(dialogDescriptor.parType[slotProps.option.type].label) }}</div>
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
        link() {
            console.log('DROP')
        }
    }
})
</script>
