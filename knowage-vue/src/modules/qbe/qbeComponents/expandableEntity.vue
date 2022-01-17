<template>
    <div class="expandableEntities" v-for="(entity, index) in entities" :key="index">
        <h4 class="entity-item-container" :style="{ 'border-left': `10px solid ${entity.color}` }">
            <i :class="getIconCls(entity.attributes.iconCls)" class="p-mx-2" v-tooltip.bottom="$t(`qbe.entities.${entity.attributes.iconCls}`)" />
            <span>{{ entity.text }}</span>
            <Button icon="fas fa-info" class="p-button-text p-button-rounded p-button-plain p-ml-auto" v-tooltip.bottom="$t('qbe.entities.relations')" @click="showRelationDialogue" />
            <Button v-if="entity.expanded" icon="pi pi-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="entity.expanded = false" />
            <Button v-else icon="pi pi-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="entity.expanded = true" />
        </h4>
        <ul v-show="entity.expanded">
            <div v-for="(child, index) in entity.children" :key="index">
                <span>{{ child.text }}</span>
            </div>
        </ul>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'qbe',
    components: {},
    props: { availableEntities: { type: Array } },
    emits: ['close'],
    data() {
        return {
            entities: [] as any,
            colors: ['#D7263D', '#F46036', '#2E294E', '#1B998B', '#C5D86D', '#3F51B5', '#8BC34A', '#009688', '#F44336']
        }
    },
    watch: {
        availableEntities() {
            this.entities = this.availableEntities
            this.setupEntities()
        }
    },
    created() {
        this.entities = this.availableEntities
        this.setupEntities()
    },
    methods: {
        setupEntities() {
            let usedColorIndex = 0
            this.entities.forEach((entity) => {
                //set colors property
                if (!this.colors[usedColorIndex]) usedColorIndex = 0
                var color = this.colors[usedColorIndex]
                usedColorIndex++
                entity.color = color
                if (entity.children) {
                    entity.children.forEach((child) => {
                        child.color = color
                    })
                }

                //set expanded property used for displaying children
                entity.expanded = false
            })
        },

        getIconCls(iconCls) {
            switch (iconCls) {
                case 'measure':
                    return 'fas fa-ruler'
                case 'cube':
                    return 'fas fa-cube'
                case 'calculation':
                    return 'fas fa-calculator'
                case 'dimension':
                    return 'fas fa-ruler-horizontal'
                case 'geographic dimension':
                    return 'fas fa-map-marked-alt'
                case 'attribute':
                    return 'fas fa-font'
                case 'generic':
                    return 'fas fa-layer-group'
                default:
                    return 'fas fa-cube'
            }
        }
    }
})
</script>
<style lang="scss">
.entity-item-container {
    display: flex;
    background-color: #fff;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    height: 24px;
    line-height: 24px;
    margin: 0;
    padding: 4px 8px 4px 8px;
    font-size: 0.8rem;
    border-bottom: 1px solid #ccc;
    outline: none;
    cursor: pointer;
}
</style>
