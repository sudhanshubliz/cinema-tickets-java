import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketServiceImplTest {

    private final TicketPaymentService paymentService = mock(TicketPaymentService.class);
    private final SeatReservationService seatService = mock(SeatReservationService.class);
    private final TicketService ticketService = new TicketServiceImpl(paymentService, seatService);

    @Test
    void shouldRejectInvalidAccount() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void shouldRejectIfNoAdultWithChild() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)));
    }

    @Test
    void shouldRejectIfExceeds25Tickets() {
        assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)));
    }

    @Test
    void shouldCalculateCorrectPaymentAndSeats() {
        ticketService.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

       verify(paymentService).makePayment(1L, 2 * 25 + 1 * 15); // £65
       verify(seatService).reserveSeat(1L, 3); // 2 adults + 1 child
    }
}
