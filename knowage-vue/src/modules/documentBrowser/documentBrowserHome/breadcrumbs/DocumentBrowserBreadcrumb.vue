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
    name: 'document-browser-breadcrumb',
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
            // console.log('LOADED BREADCRUMBS ITEMS: ', this.items)
        },
        selectBreadcrumb(document: any) {
            console.log('DOCUMENT CLICKED IN BREADCRUMBS: ', document.target.innerText)
            const index = this.items.findIndex((el: any) => el.label === document.target.innerText)
            //  console.log('index', index)
            // console.log('ITEMS: ', this.items)
            if (index !== -1) {
                this.$emit('breadcrumbClicked', this.items[index])
                this.items.splice(index + 1)
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.breadcrumbs-item:hover {
    cursor: pointer;
}
</style>
