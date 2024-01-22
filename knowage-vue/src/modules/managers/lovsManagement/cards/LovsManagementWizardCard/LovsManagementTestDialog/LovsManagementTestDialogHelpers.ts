export const removeUnusedVisibleColumnsFromModel = (treeListTypeModel: any, tableModelForTest: any[]) => {
    if (!tableModelForTest || !treeListTypeModel || !treeListTypeModel['VISIBLE-COLUMNS'] || !treeListTypeModel.STMT) return
    const visibleColumns = treeListTypeModel['VISIBLE-COLUMNS'].split(',')
    const filteredColumns = visibleColumns.filter((column: string) => {
        const index = tableModelForTest.findIndex((columnFromPreviewService: any) => columnFromPreviewService.name === column)
        return index !== -1
    })
    treeListTypeModel['VISIBLE-COLUMNS'] = filteredColumns.join(',')
}