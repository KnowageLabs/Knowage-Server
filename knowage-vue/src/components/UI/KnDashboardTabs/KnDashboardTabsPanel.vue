<template>
    <div class="sheets-container">
        <!-- <div class="sheets-list" :class="labelPosition" role="tablist" v-if="sheets && sheets.length > 1"> -->
        <div v-if="sheets && sheets.length >= 1" class="sheets-list" :class="labelPosition" role="tablist">
            <a v-for="(sheet, index) in sheets" :key="index" class="sheet-label" :class="{ active: currentPage === index }" @touchstart.passive="setPage(index)" @click="setPage(index)">
                <slot name="label" v-bind="sheet">
                    <i v-if="sheet.icon" :class="sheet.icon" class="p-mr-1"></i>
                    <span>{{ sheet.label }} </span>
                    <Button icon="fa-solid fa-ellipsis-vertical" class="p-button-text p-button-rounded p-button-plain" @click="toggleMenu" />
                    <Menu id="buttons_menu" ref="buttons_menu" :model="menuButtons" :popup="true"> </Menu>
                </slot>
            </a>
            <a class="sheet-label" @click="addSheet"><i class="fa-solid fa-circle-plus"></i></a>
        </div>

        <div class="sheets-wrapper" @touchstart.passive="onTouchStart($event)" @touchmove.passive="onTouchMove($event)" @touchend.passive="onTouchEnd($event)">
            <div class="sheet-content" :style="{ transform: `translate3d(${translateX}px, 0, 0)` }">
                <slot />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { PropType } from 'vue'
import Menu from 'primevue/menu'
import type { ISheet } from '@/modules/documentExecution/dashboard/Dashboard'

export default defineComponent({
    name: 'kn-dashboard-tabs-panel',
    components: { Menu },
    props: {
        sheets: {
            type: Array as PropType<Array<ISheet>>,
            required: true
        },
        indexTab: {
            type: String
        },
        labelPosition: {
            type: String
        }
    },
    emits: ['sheetChange', 'update:sheets'],
    data() {
        return {
            currentPage: 0,
            translateX: 0,
            dpr: 1,
            distance: {
                left: 0,
                top: 0
            },
            menuButtons: [
                {
                    label: 'Options',
                    items: [
                        { label: 'New', icon: 'pi pi-fw pi-plus', command: () => {} },
                        { label: 'Delete', icon: 'pi pi-fw pi-trash', url: 'http://primetek.com.tr' }
                    ]
                },
                {
                    label: 'Account',
                    items: [
                        { label: 'Options', icon: 'pi pi-fw pi-cog', to: '/options' },
                        { label: 'Sign Out', icon: 'pi pi-fw pi-power-off', to: '/logout' }
                    ]
                }
            ],
            touchPoint: {
                startLeft: 0,
                startTop: 0,
                startTime: 0
            },
            startTranslateX: 0,
            startTime: 0,
            swipeType: 'init'
        }
    },
    mounted() {
        this.initDPR()
    },
    methods: {
        addSheet(): void {
            this.$emit('update:sheets', [...this.sheets, { label: 'new sheet', widgets: { lg: [] } }])
        },
        setPage(index): void {
            this.$refs
            this.currentPage = index
            this.$emit('sheetChange', index)
            this.translateX = -this.sheets.reduce((total, item, i) => {
                return i > index - 1 ? total : total + document.querySelectorAll('#sheet_' + index)[0].clientWidth
            }, 0)
        },
        next() {
            let currentpage = this.currentPage
            currentpage < this.sheets.length - 1 && currentpage++
            this.setPage(currentpage)
        },
        prev() {
            let currentpage = this.currentPage
            currentpage > 0 && currentpage--
            this.setPage(currentpage)
        },
        reset() {
            this.setPage(this.currentPage)
        },
        onTouchStart(event) {
            if (event.target.classList.contains('drag-handle') || event.target.classList.contains('vue-resizable-handle')) return
            const touchPoint = event.changedTouches[0] || event.touches[0]
            const startLeft = touchPoint.pageX
            this.touchPoint.startLeft = startLeft
            const startTop = touchPoint.pageY
            this.touchPoint.startTop = startTop
            const startTranslateX = this.translateX
            this.startTranslateX = startTranslateX
            const touchTime = new Date().getTime()
            this.touchPoint.startTime = touchTime
        },
        onTouchMove(event) {
            if (event.target.classList.contains('drag-handle') || event.target.classList.contains('vue-resizable-handle')) return
            const touchPoint = event.changedTouches[0] || event.touches[0]
            const distanceLeft = touchPoint.pageX - this.touchPoint.startLeft
            this.distance.left = distanceLeft
            const distanceTop = Math.abs(touchPoint.pageY - this.touchPoint.startTop)
            this.distance.top = distanceTop
            switch (this.swipeType) {
                case 'init':
                    if (Math.abs(distanceLeft) / distanceTop > 1.5) {
                        this.swipeType = 'swipe'
                    } else {
                        this.swipeType = 'scroll'
                    }
                    break
                case 'scroll':
                    break
                case 'swipe':
                    this.translateX = this.startTranslateX + distanceLeft
                    break
            }
        },
        onTouchEnd() {
            const quick = new Date().getTime() - this.startTime < 1000
            if ((this.distance.left < -(200 * this.dpr) && this.distance.top < 100 * this.dpr) || (quick && this.distance.left < -15 && this.distance.top / this.distance.left > -6)) {
                this.next()
            } else if ((this.distance.left > 200 * this.dpr && this.distance.top < 100 * this.dpr) || (quick && this.distance.left > 15 && this.distance.top / this.distance.left < 6)) {
                this.prev()
            } else {
                this.reset()
            }
            this.distance.left = 0
            this.distance.top = 0
        },
        initDPR() {
            const win = window
            const isIPhone = win.navigator.appVersion.match(/iphone/gi)
            const devicePixelRatio = win.devicePixelRatio
            if (isIPhone) {
                if (devicePixelRatio >= 3 && this.dpr) {
                    this.dpr = 3
                } else if (devicePixelRatio >= 2 && this.dpr) {
                    this.dpr = 2
                } else {
                    this.dpr = 1
                }
            } else {
                this.dpr = 1
            }
        },
        toggleMenu(e) {
            e.preventDefault()
            e.stopImmediatePropagation()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.buttons_menu.toggle(e)
        }
    }
})
</script>

<style lang="scss" scoped>
.sheets-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    .sheets-wrapper {
        width: 100%;
        flex: 1 0 0;
        order: 1;
        overflow: hidden;
        .sheet-content {
            width: 100%;
            height: 100%;
            display: flex;
            white-space: nowrap;
            transition: all 0.2s ease;
        }
    }

    .sheets-list {
        position: relative;
        height: 35px;
        margin: 0;
        padding: 0;
        border-bottom: 1px solid #ccc;
        list-style: none;
        display: inline-flex;
        order: 0;
        &.bottom {
            order: 2;
            border-top: 1px solid #ccc;
            border-bottom: 0;
        }
        .sheet-label {
            position: relative;
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            position: relative;
            color: #999;
            cursor: pointer;
            text-decoration: none;
            &:hover {
                background-color: #999;
                color: #000;
            }
            &:focus {
                outline: none;
            }
            &.active {
                color: #000;
                font-weight: 900;
            }
        }
    }
}

@media all and (max-width: 600px) {
    .sheets-container {
        .sheet-label {
            span {
                display: none;
            }
        }
    }
}
</style>
