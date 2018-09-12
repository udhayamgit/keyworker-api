package uk.gov.justice.digital.hmpps.keyworker.services;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
class GenerateDateRange {
    private final LocalDate toDate;
    private final LocalDate fromDate;

    public GenerateDateRange(int numWeeks, final LocalDate startDate) {

        final LocalDate workingDate = startDate.with(DayOfWeek.SUNDAY);
        //workingDate will be next sunday (unless the startDate fell on a Sunday) -  need to adjust to last sunday
        if (workingDate.isAfter(startDate)) {
            toDate = workingDate.minusWeeks(1);
        } else {
            toDate = workingDate;
        }
        fromDate = toDate.minusWeeks(numWeeks);
    }
}
