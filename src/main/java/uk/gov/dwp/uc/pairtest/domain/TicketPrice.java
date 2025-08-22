package uk.gov.dwp.uc.pairtest.domain;

public enum TicketPrice {

    INFANT(0),CHILD(15),ADULT(25);

    private final int price;

    TicketPrice(int price){
        this.price= price;
    }

    public int getPrice() {
        return price;
    }
}
