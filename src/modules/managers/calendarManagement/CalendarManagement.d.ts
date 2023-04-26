export interface iCalendar {
    calendarId?: number
    realDateGenerated?: any[],
    splittedCalendar?: any[],
    calStartDay: number | Date | null,
    calEndDay: number | Date | null,
    calendar: string,
    calType: string,
    recStatus?: string
}

export interface iDomain {
    domainId: number,
    attributeDomain: string,
    attributeDomainDescr: string,
    recStatus: string
}

export interface iCalendarDate {
    idCalComposition: number,
    isHoliday: number,
    pubHoliday: string,
    recStatus: string,
    calendarId: number,
    timeId: number,
    calendar: iCalendar,
    timeByDay: iTimeByDay,
    listOfAttributes: iAttribute[]
}

export interface iTimeByDay {
    timeId: number,
    timeDate: number | Date,
    dayDesc: string,
    dayName: string,
    dayOfWeek: number,
    dayOfMonth: number,
    dayOfYear: number,
    monthId: string,
    monthDesc: string,
    monthOfQuarter: number,
    monthOfYear: number,
    monthName: string,
    endOfMonth: number | Date,
    daysInMonth: string,
    quarterId: string,
    quarterDesc: string,
    quarterOfYear: number,
    endOfQuarter: number | Date,
    daysInQuarter: number,
    semesterId: string,
    semesterDesc: string,
    semesterOfYear: number,
    endOfSemester: number | Date,
    daysInSemester: number,
    yearId: number,
    daysInYear: number,
    endOfYear: number | Date,
    daysInWeek: number,
    calWeekId: string,
    calWeekDesc: string,
    calWeekOfYear: number,
    endOfCalWeek: number | Date,
    isoWeekId: string,
    isoWeekDesc: string,
    isoWeekOfYear: number,
    endOfIsoWeek: number | Date
}

export interface iAttribute {
    attributeId: number,
    domainId: number,
    calendarId: number,
    calendarAttributeDomain: iDomain,
    calendar: iCalendar,
    recStatus: string
}