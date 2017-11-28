package databaseObjects;

public class HotelAttractions {
    private String name;
    private String address;

    public HotelAttractions(String name, String address){
        this.name=name;
        this.address=address;
    }

    public String getAddress() {
        return this.address;
    }

    public String getName() {
        return this.name;
    }
}
