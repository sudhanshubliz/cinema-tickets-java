package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.List;
import java.util.Objects;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccount(accountId);

        validateRequests(ticketTypeRequests);
        int totalTickets = 0;
        int totalPayment = 0;
        int seatsToReserve = 0;

        for(TicketTypeRequest request: ticketTypeRequests) {
            int noOfTickets = request.getNoOfTickets();


            totalTickets += noOfTickets;
            switch (request.getTicketType()) {
                case ADULT -> {
                    totalPayment += noOfTickets * 25;
                    seatsToReserve += noOfTickets;
                }
                case CHILD -> {
                    totalPayment += noOfTickets * 15;
                    seatsToReserve += noOfTickets;
                }
                case INFANT -> {
                    // Infants do not pay and do not require a seat
                }
            }
        }
        validateTotalTickets(totalTickets);
        validateAdultTicket(ticketTypeRequests);
        ticketPaymentService.makePayment(accountId, totalPayment);
        seatReservationService.reserveSeat(accountId, seatsToReserve);

    }

    private static void validateAdultTicket(TicketTypeRequest... ticketTypeRequests) {

        // Ensure at least 1 Adult if Child/Infant present
        long adultCount = List.of(ticketTypeRequests).stream()
                .filter(req -> req.getTicketType() == TicketTypeRequest.Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
        if (adultCount == 0) {
            throw new InvalidPurchaseException("Child or Infant tickets require at least 1 Adult ticket.");
        }
    }

    private static void validateTotalTickets(int totalTickets) {
        if(totalTickets >25){
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time.");
        }
    }

    private static void validateRequests(TicketTypeRequest[] ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket must be requested.");
        }
    }

    private static void validateAccount(Long accountId) {
        if(accountId == null || accountId <=0){
            throw new InvalidPurchaseException("Invalid account Id.");
        }
    }

}
