<template>
    <Breadcrumb class="workspace-breadcrumbs" :home="home" :model="items">
        <template #item="{item}">
            <span class="workspace-breadcrumbs-item" @click="selectBreadcrumb">{{ item.label }}</span>
        </template>
    </Breadcrumb>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Breadcrumb from 'primevue/breadcrumb'

export default defineComponent({
    name: 'workspace-repository-breadcrumb',
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
.workspace-breadcrumbs-item:hover {
    cursor: pointer;
}

.workspace-breadcrumbs.p-breadcrumb ul li:nth-child(2) {
    display: none;
}
</style>
