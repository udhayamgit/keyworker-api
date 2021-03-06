package uk.gov.justice.digital.hmpps.keyworker.controllers;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.hmpps.keyworker.dto.ErrorResponse;
import uk.gov.justice.digital.hmpps.keyworker.dto.KeyworkerStatSummary;
import uk.gov.justice.digital.hmpps.keyworker.dto.KeyworkerStatsDto;
import uk.gov.justice.digital.hmpps.keyworker.dto.Prison;
import uk.gov.justice.digital.hmpps.keyworker.services.KeyworkerStatsService;
import uk.gov.justice.digital.hmpps.keyworker.services.PrisonSupportedService;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"key-worker-stats"})

@RestController
@RequestMapping(
        value="key-worker-stats",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class KeyworkerStatsController {
    private final KeyworkerStatsService keyworkerStatsService;
    private final PrisonSupportedService prisonSupportedService;

    public KeyworkerStatsController(final KeyworkerStatsService keyworkerStatsService, final PrisonSupportedService prisonSupportedService) {
        this.keyworkerStatsService = keyworkerStatsService;
        this.prisonSupportedService = prisonSupportedService;
    }

    @ApiOperation(
            value = "Return staff members stats",
            notes = "Statistic for key workers and the prisoners that they support",
            nickname="getStatsForStaff")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = KeyworkerStatsDto.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping(path = "/{staffId}/prison/{prisonId}")
    public KeyworkerStatsDto getStatsForStaff(
            @ApiParam("staffId") @NotNull @PathVariable("staffId") final
            Long staffId,

            @ApiParam("prisonId") @NotNull @PathVariable("prisonId") final
            String prisonId,

            @ApiParam(value = "Calculate stats for staff on or after this date (in YYYY-MM-DD format).")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final
            LocalDate fromDate,

            @ApiParam(value = "Calculate stats for staff on or before this date (in YYYY-MM-DD format).")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final
            LocalDate toDate)
    {

        return keyworkerStatsService.getStatsForStaff(staffId, prisonId, fromDate, toDate);

    }

    /* --------------------------------------------------------------------------------*/

    @ApiOperation(
            value = "Get Key Worker stats for any prison.",
            nickname="getAllPrisonStats")

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", responseContainer = "Map", response = KeyworkerStatSummary.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class ),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })

    @GetMapping
    public KeyworkerStatSummary getPrisonStats(
            @ApiParam(value = "List of prisonIds", allowMultiple = true, example = "prisonId=MDI&prisonId=LEI")
            @RequestParam(value = "prisonId", required = false) final
            List<String> prisonIdList,
            @ApiParam(value = "Start Date of Stats, optional, will choose one month before toDate (in YYYY-MM-DD format)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final
            LocalDate fromDate,
            @ApiParam(value = "End Date of Stats (inclusive), optional, will choose yesterday if not provided (in YYYY-MM-DD format)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final
            LocalDate toDate) {

        final List<String> prisonIds = new ArrayList<>();
        if (prisonIdList == null || prisonIdList.isEmpty()) {
            final var migratedPrisons = prisonSupportedService.getMigratedPrisons();
            prisonIds.addAll(migratedPrisons.stream().map(Prison::getPrisonId).collect(Collectors.toList()));
        } else {
            prisonIds.addAll(prisonIdList);
        }
        log.debug("getting key-workers stats for prisons {}", prisonIds);
        return keyworkerStatsService.getPrisonStats(prisonIds, fromDate, toDate);
    }



}
