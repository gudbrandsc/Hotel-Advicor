package databaseObjects;

public class HotelAttractions {
    private String name;
    private String address;
    private double rating;

    public HotelAttractions(String name, String address, double rating){
        this.name=name;
        this.address=address;
        this.rating=rating;
    }

    public String getAddress() {
        return this.address;
    }

    public String getName() {
        return this.name;
    }

    public double getRating() {
        return this.rating;
    }
}
