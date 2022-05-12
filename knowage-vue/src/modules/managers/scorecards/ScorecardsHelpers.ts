import { iKpi, iScorecardCriterion } from "./Scorecards"

export function getSelectedCriteriaTooltip(option: string, $t: any) {
    switch (option) {
        case 'M':
            return $t('managers.scorecards.majority')
        case 'MP':
            return $t('managers.scorecards.majorityWithPriority')
        case 'P':
            return $t('managers.scorecards.priority')
        default:
            return ''
    }
}

export function getKpiIconColorClass(kpi: iKpi) {
    if (kpi.status) {
        switch (kpi.status) {
            case 'RED':
                return 'scorecard-kpi-icon-red'
            case 'YELLOW':
                return 'scorecard-kpi-icon-yellow'
            case 'GREEN':
                return 'scorecard-kpi-icon-green'
            case 'GREY':
                return 'scorecard-kpi-icon-grey'
        }
    } else {
        return 'scorecard-kpi-icon-light-grey'
    }
}

export function getDefaultCriterion(criterias: iScorecardCriterion[]) {
    let tempCriterion = {} as iScorecardCriterion
    const index = criterias.findIndex((criteria: iScorecardCriterion) => criteria.valueCd === 'MAJORITY')
    if (index !== -1) tempCriterion = criterias[index]
    return tempCriterion
}

export function getSelectedCriteria(criterionValue: string) {
        switch (criterionValue) {
            case 'MAJORITY':
                return 'M'
            case 'MAJORITY_WITH_PRIORITY':
                return 'MP'
            case 'PRIORITY':
                return 'P'
            default:
                return ''
        }
    
}