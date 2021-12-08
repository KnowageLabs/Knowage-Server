<template>
    <div class="p-grid p-m-0 kn-flex">
        <div id="CONTAINER ELEMENT 1 " class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left> {{ $t('metaweb.businessModel.businessClass') }}/{{ $t('metaweb.businessModel.businessView') }} </template>
                <template #right>
                    <KnFabButton icon="fas fa-plus" />
                </template>
            </Toolbar>
            <div class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    {{ mainDescriptor.test }}
                </div>
            </div>
        </div>
        <div id="CONTAINER ELEMENT 2 " class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 p-d-flex p-flex-column" :style="mainDescriptor.style.flex3">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    test
                </template>
                <template #right>
                    <Button label="Delete" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" />
                </template>
            </Toolbar>
            <div class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    {{ mainDescriptor.test }}
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import mainDescriptor from '../MetawebDescriptor.json'
import KnFabButton from '@/components/UI/KnFabButton.vue'

export default defineComponent({
    name: 'metaweb-business-model',
    components: { KnFabButton },
    props: {},
    computed: {},
    data() {
        return {
            mainDescriptor
        }
    },
    created() {},
    methods: {}
})
</script>
