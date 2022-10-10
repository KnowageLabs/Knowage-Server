import { iExporter } from './DocumentExecution'

export function createToolbarMenuItems(document: any, functions: any, exporters: iExporter[] | null, user: any, isOrganizerEnabled: boolean, mode: string | null, $t: any, newDashboardMode: boolean) {
    const toolbarMenuItems = [] as any[]

    if (mode === 'dashboard') {
        toolbarMenuItems.push({
            label: $t('common.settings'),
            items: [{ icon: 'pi pi-cog', label: $t('common.general') }, { icon: 'fa-brands fa-diaspora', label: $t('common.variables') }, { icon: 'fas fa-paint-roller', label: $t('common.themes') }, { icon: 'fas fa-recycle', label: $t('documentExecution.main.clearCache') }]
        })
    }

    if (!newDashboardMode) {
        toolbarMenuItems.push({
            label: $t('common.file'),
            items: [{ icon: 'pi pi-print', label: $t('common.print'), command: () => functions.print() }]
        })
    }


    if (exporters && exporters.length !== 0 && !newDashboardMode) {
        toolbarMenuItems.push({
            label: $t('common.export'),
            items: []
        })
    }

    if (user.enterprise && !newDashboardMode) {
        toolbarMenuItems.push({
            label: $t('common.info.info'),
            items: [{ icon: 'pi pi-star', label: $t('common.rank'), command: () => functions.openRank() }]
        })
    }

    if (!newDashboardMode) {
        toolbarMenuItems.push({
            label: $t('common.shortcuts'),
            items: []
        })
    }


    if (!newDashboardMode)
        exporters?.forEach((exporter: any) => toolbarMenuItems[1].items.push({ icon: 'fa fa-file-excel', label: exporter.name, command: () => functions.export(exporter.name) }))

    if (user.functionalities.includes('SendMailFunctionality') && document.typeCode === 'REPORT') {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.info.info'))
        if (index !== -1) {
            toolbarMenuItems[index].items.push({ icon: 'pi pi-envelope', label: $t('common.sendByEmail'), command: () => functions.openMailDialog() })
        } else {
            toolbarMenuItems.push({
                label: $t('common.export'),
                items: [{ icon: 'pi pi-envelope', label: $t('common.sendByEmail'), command: () => functions.openMailDialog() }]
            })
        }
    }

    if (user.functionalities.includes('SeeMetadataFunctionality') && !newDashboardMode) {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.info.info'))
        if (index !== -1) toolbarMenuItems[index].items.unshift({ icon: 'pi pi-info-circle', label: $t('common.metadata'), command: () => functions.openMetadata() })
    }

    if (user.functionalities.includes('SeeNotesFunctionality') && !newDashboardMode) {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.info.info'))
        if (index !== -1) toolbarMenuItems[index].items.push({ icon: 'pi pi-file', label: $t('common.notes'), command: () => functions.openNotes() })
    }

    if (user.functionalities.includes('SeeSnapshotsFunctionality') && user.enterprise && !newDashboardMode) {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.shortcuts'))
        if (index !== -1) toolbarMenuItems[index].items.unshift({ icon: '', label: $t('documentExecution.main.showScheduledExecutions'), command: () => functions.showScheduledExecutions() })
    }

    if (isOrganizerEnabled && !newDashboardMode) {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.shortcuts'))
        if (index !== -1) toolbarMenuItems[index].items.unshift({ icon: 'fa fa-suitcase ', label: $t('documentExecution.main.addToWorkspace'), command: () => functions.addToWorkspace() })
    }

    if (mode === 'olap') {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.shortcuts'))
        if (index !== -1) toolbarMenuItems[index].items.unshift({ icon: '', label: $t('documentExecution.main.showOLAPCustomView'), command: () => functions.showOLAPCustomView() })
    }

    if (user.functionalities.includes('EnableToCopyAndEmbed') && !newDashboardMode) {
        const index = toolbarMenuItems.findIndex((item: any) => item.label === $t('common.shortcuts'))
        if (index !== -1) {
            toolbarMenuItems[index].items.push({ icon: 'fa fa-share', label: $t('documentExecution.main.copyLink'), command: () => functions.copyLink(false) })
            toolbarMenuItems[index].items.push({ icon: 'fa fa-share', label: $t('documentExecution.main.embedInHtml'), command: () => functions.copyLink(true) })
        }
    }

    if (mode === 'dashboard' && !newDashboardMode) {
        toolbarMenuItems.push({
            label: $t('common.view'),
            items: [{ icon: 'pi pi-eye', label: $t('documentExecution.main.asFinalUser') }, { icon: 'pi pi-print', label: $t('documentExecution.main.inFullScreen') }]
        })
    }

    return toolbarMenuItems
}