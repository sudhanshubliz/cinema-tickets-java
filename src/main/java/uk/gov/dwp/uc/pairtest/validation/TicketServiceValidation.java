package uk.gov.dwp.uc.pairtest.validation;

import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.List;

public class TicketServiceValidation {
    public static void validateTotalTickets(int totalTickets) {
        if(totalTickets > TicketPrice.ADULT.getPrice()){
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time.");
        }
    }

    public static void validateRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket must be requested.");
        }
    }

    public static void validateAccount(Long accountId) {
        if(accountId == null || accountId <=0){
            throw new InvalidPurchaseException("Invalid account Id.");
        }
    }

    public static void validateAdultTicket(TicketTypeRequest... ticketTypeRequests) {
        // Ensure at least 1 Adult if Child/Infant present
        long adultCount = List.of(ticketTypeRequests).stream()
                .filter(req -> req.getTicketType() == TicketTypeRequest.Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
        if (adultCount == 0) {
            throw new InvalidPurchaseException("Child or Infant tickets require at least 1 Adult ticket.");
        }
    }

}
