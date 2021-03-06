package uk.gov.justice.digital.hmpps.keyworker.batch;

import com.microsoft.applicationinsights.TelemetryClient;
import groovy.util.logging.Slf4j;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.keyworker.dto.CaseloadUpdate;
import uk.gov.justice.digital.hmpps.keyworker.dto.Prison;
import uk.gov.justice.digital.hmpps.keyworker.services.NomisService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class EnableNewNomisRouteTest extends CamelTestSupport {

    private final static String MOCK_PRISONS_ENDPOINT ="mock:getAllPrisons";
    private final static String MOCK_ENABLE_ENDPOINT = "mock:enableNewNomis";
    private final static String MOCK_DLQ_ENDPOINT = "mock:dlq";

    private static final Prison MDI = Prison.builder().prisonId("MDI").build();
    private static final Prison LEI = Prison.builder().prisonId("LEI").build();
    private static final Prison LPI = Prison.builder().prisonId("LPI").build();

    @Mock
    private NomisService nomisService;

    @Mock
    private TelemetryClient telemetryClient;

    @Override
    public RouteBuilder[] createRouteBuilders() {
        MockitoAnnotations.initMocks(this);
        final var route = new EnableNewNomisRoute(nomisService, telemetryClient);
        return new RouteBuilder[]{route};
    }

    @Before
    public void mockEndpoints() throws Exception {
        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                weaveAddLast().to(MOCK_PRISONS_ENDPOINT);
            }
        });

        context.getRouteDefinitions().get(1).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                weaveAddLast().to(MOCK_ENABLE_ENDPOINT);
            }
        });

        context.getRouteDefinitions().get(2).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                weaveAddLast().to(MOCK_DLQ_ENDPOINT);
            }
        });
    }


    @Test
    public void testEnabledNewNomisCamelRoute() throws Exception {

        final var prisons = List.of(MDI, LEI, LPI);

        when(nomisService.getAllPrisons()).thenReturn(prisons);
        final var MDIResponse = CaseloadUpdate.builder().caseload(MDI.getPrisonId()).numUsersEnabled(2).build();
        when(nomisService.enableNewNomisForCaseload(eq(MDI.getPrisonId()))).thenReturn(MDIResponse);
        final var LEIResponse = CaseloadUpdate.builder().caseload(LEI.getPrisonId()).numUsersEnabled(0).build();
        when(nomisService.enableNewNomisForCaseload(eq(LEI.getPrisonId()))).thenReturn(LEIResponse);
        final var LPIResponse = CaseloadUpdate.builder().caseload(LPI.getPrisonId()).numUsersEnabled(14).build();
        when(nomisService.enableNewNomisForCaseload(eq(LPI.getPrisonId()))).thenReturn(LPIResponse);


        template.send(EnableNewNomisRoute.ENABLE_NEW_NOMIS, exchange -> {
        });

        assertMockEndpointsSatisfied();
        final var mockEndpoint = getMockEndpoint(MOCK_PRISONS_ENDPOINT);
        mockEndpoint.assertIsSatisfied();

        final var receivedExchanges = mockEndpoint.getReceivedExchanges();
        assertEquals(1, receivedExchanges.size());
        final List<Prison> exchangeData = receivedExchanges.get(0).getIn().getBody(List.class);
        assertEquals(prisons, exchangeData);

        final var mockEndpoint2 = getMockEndpoint(MOCK_ENABLE_ENDPOINT);
        mockEndpoint2.assertIsSatisfied();

        final var receivedExchanges2 = mockEndpoint2.getReceivedExchanges();
        assertEquals(3, receivedExchanges2.size());

        assertEquals(receivedExchanges2.get(0).getIn().getBody(CaseloadUpdate.class), MDIResponse);
        assertEquals(receivedExchanges2.get(1).getIn().getBody(CaseloadUpdate.class), LEIResponse);
        assertEquals(receivedExchanges2.get(2).getIn().getBody(CaseloadUpdate.class), LPIResponse);

        verify(nomisService).getAllPrisons();
        verify(nomisService).enableNewNomisForCaseload(eq(MDI.getPrisonId()));
        verify(nomisService).enableNewNomisForCaseload(eq(LEI.getPrisonId()));
        verify(nomisService).enableNewNomisForCaseload(eq(LPI.getPrisonId()));
        verify(telemetryClient, times(2)).trackEvent(eq("ApiUsersEnabled"), isA(Map.class), isNull());
    }


}
