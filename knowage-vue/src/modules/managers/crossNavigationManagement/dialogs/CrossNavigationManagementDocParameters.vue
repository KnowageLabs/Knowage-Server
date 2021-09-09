<template>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.crossNavigationManagement.availableIO') }}
            </template>
        </Toolbar>
        <div class="p-inputgroup p-mt-3">
            <span class="p-float-label">
                <InputText class="kn-material-input" type="text" v-model="value" />
                <label class="kn-material-input-label">{{ $t('managers.crossNavigationManagement.fixedValue') }} </label>
            </span>
            <FabButton icon="fas fa-plus" class="fab-button p-mt-3 p-ml-2" />
        </div>
        <Listbox :options="navigation.fromPars" optionLabel="name"></Listbox>
        {{ navigation.fromPars }}
    </div>
    <div class="p-field p-col-6 p-mb-3">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.crossNavigationManagement.availableInput') }}
            </template>
        </Toolbar>
        <Listbox :options="navigation.toPars" optionLabel="name"></Listbox>
        {{ navigation.toPars }}
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import FabButton from '@/components/UI/KnFabButton.vue'
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
            navigation: {} as any
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
        handleIOparams() {}
    }
})
</script>
