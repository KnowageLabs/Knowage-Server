export const removeUnusedVisibleColumnsFromModel = (treeListTypeModel) => {
    if (!treeListTypeModel || !treeListTypeModel['VISIBLE-COLUMNS']) return
    const visibleColumns = treeListTypeModel['VISIBLE-COLUMNS'].split(',')
    const filteredColumns = visibleColumns.filter((column: string) => treeListTypeModel.STMT.includes(column.trim()))
    treeListTypeModel['VISIBLE-COLUMNS'] = filteredColumns.join(',')
}