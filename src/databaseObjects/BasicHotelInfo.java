package databaseObjects;

public class BasicHotelInfo {
    String name;
    String address;
    String hotelId;
    Double rating;

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
