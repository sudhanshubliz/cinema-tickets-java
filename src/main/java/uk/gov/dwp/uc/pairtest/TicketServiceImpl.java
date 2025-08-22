package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static uk.gov.dwp.uc.pairtest.validation.TicketServiceValidation.*;

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
        validateAdultTicket(ticketTypeRequests);
        int totalTickets = 0;
        int totalPayment = 0;
        int seatsToReserve = 0;

        for(TicketTypeRequest request: ticketTypeRequests) {
            int noOfTickets = request.getNoOfTickets();
            totalTickets += noOfTickets;
            switch (request.getTicketType()) {
                case ADULT -> {
                    totalPayment += noOfTickets * TicketPrice.ADULT.getPrice();
                    seatsToReserve += noOfTickets;
                }
                case CHILD -> {
                    totalPayment += noOfTickets * TicketPrice.CHILD.getPrice();
                    seatsToReserve += noOfTickets;
                }
                case INFANT -> {
                    // Infants do not pay and do not require a seat
                }
            }
        }
        validateTotalTickets(totalTickets);
        try{
            ticketPaymentService.makePayment(accountId, totalPayment);
            seatReservationService.reserveSeat(accountId, seatsToReserve);
        }catch(Exception e){
            throw new InvalidPurchaseException("An error occurred while processing the payment or reserving seats: " + e.getMessage());
        }
    }
}
