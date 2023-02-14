<template>
    <div class="p-col-12 p-md-6 p-lg-4" tabindex="0">
        <div class="card-container">
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start> {{ htmlGalleryItemProp.label }} </template>
            </Toolbar>
            <span class="details-container">
                <img class="card-image" :src="htmlGalleryItemProp.image" />
                <div class="detail-chips-container">
                    <template v-for="(tag, index) of htmlGalleryItemProp.tags" :key="index">
                        <span class="detail-chips"> {{ tag }}</span>
                    </template>
                </div>
            </span>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IGalleryItem } from '@/modules/documentExecution/Dashboard/Dashboard'

export default defineComponent({
    name: 'workspace-sidebar',
    components: {},
    emits: [],
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, htmlGalleryItemProp: { type: Object as PropType<IGalleryItem>, required: true } },
    computed: {},
    data() {
        return {}
    },
    setup() {},
    methods: {}
})
</script>
<style lang="scss" scoped>
.card-container {
    display: flex;
    flex-direction: column;
    position: relative;
    border-radius: 0;
    overflow: hidden;
    height: 200px;
    padding: 0;
    border: 1px solid var(--kn-color-borders);
    &:hover {
        background-color: var(--kn-color-secondary);
        transition: 0.3s;
    }

    .details-container {
        display: flex;
        flex-direction: column;
        justify-content: center;
        flex: 1;
        .card-image {
            box-sizing: border-box;
            max-width: 100%;
            max-height: 150px;
        }
        .detail-chips-container {
            display: flex;
            flex-wrap: wrap-reverse;
            position: absolute;
            justify-content: flex-end;
            right: 0;
            bottom: 0;
            .detail-chips {
                color: rgba(0, 0, 0, 0.87);
                white-space: nowrap;
                text-overflow: ellipsis;
                overflow: hidden;
                background-color: #ccc;
                padding: 0 8px;
                border-radius: 20px;
                margin: 0 2px;
                font-size: 0.8rem;
                margin-bottom: 2px;
            }
        }
    }
}
</style>
