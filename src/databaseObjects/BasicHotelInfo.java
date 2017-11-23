package databaseObjects;

public class BasicHotelInfo {
    private String name;
    private String address;
    private String hotelId;
    private Double rating;

    public BasicHotelInfo(String name, String address, Double rating,String hotelId){
        this.name=name;
        this.address=address;
        this.rating = rating;
        this.hotelId=hotelId;

    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public Double getRating() {
        return this.rating;
    }

    public String getHotelId() {
        return this.hotelId;
    }
}
