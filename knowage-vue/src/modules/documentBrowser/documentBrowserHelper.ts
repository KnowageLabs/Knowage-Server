export function getRouteDocumentType(item: any) {
    let routeDocumentType = ''

    switch (item.typeCode) {
        case 'DATAMART':
            routeDocumentType = 'registry'
            break
        case 'DOCUMENT_COMPOSITE':
            routeDocumentType = 'document-composite'
            break
        case 'OFFICE_DOC':
            routeDocumentType = 'office-doc'
            break
        case 'OLAP':
            routeDocumentType = 'olap'
            break
        case 'MAP':
            routeDocumentType = 'map'
            break
        case 'REPORT':
            routeDocumentType = 'report'
            break
        case 'KPI':
            routeDocumentType = 'kpi'
            break
        case 'DOSSIER':
            routeDocumentType = 'dossier'
            break
        case 'ETL':
            routeDocumentType = 'etl'
            break
    }

    return routeDocumentType
}