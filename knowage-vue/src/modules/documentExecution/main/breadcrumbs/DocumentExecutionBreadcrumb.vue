<template>
    <Breadcrumb :home="home" :model="items">
        <template #item="{item}">
            <span class="breadcrumbs-item" @click="selectBreadcrumb">{{ item.label }}</span>
        </template>
    </Breadcrumb>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Breadcrumb from 'primevue/breadcrumb'

export default defineComponent({
    name: 'document-execution-breadcrumb',
    components: { Breadcrumb },
    props: { breadcrumbs: { type: Array } },
    emits: ['breadcrumbClicked'],
    data() {
        return {
            home: { icon: 'pi pi-home' },
            items: [] as any[]
        }
    },
    watch: {
        breadcrumbs() {
            this.loadBreadcrumbs()
        }
    },
    created() {
        this.loadBreadcrumbs()
    },
    methods: {
        loadBreadcrumbs() {
            this.items = this.breadcrumbs as any[]
            console.log('LOADED BREADCRUMB ITEMS: ', this.breadcrumbs)
        },
        selectBreadcrumb(event: any) {
            const index = this.items.findIndex((el: any) => el.label === event.target.textContent)
            if (index !== -1) {
                this.$emit('breadcrumbClicked', this.items[index])
                this.items.splice(index + 1)
            }
        }
    }
})
</script>

<style lang="scss">
.breadcrumbs-item:hover {
    cursor: pointer;
}
.p-breadcrumb ul li:nth-child(2) {
    display: none;
}
</style>
