package databaseObjects;

public class BasicHotelInfo {
    private String name;
    private String city;
    private String address;
    private String hotelId;
    private Double rating;
    private String username;

    public BasicHotelInfo(String hotelId,String name, String city, String address, Double rating){
        this.hotelId=hotelId;
        this.name=name;
        this.city=city;
        this.address=address;
        this.rating = rating;

    }
    public BasicHotelInfo(String hotelId,String name, String city, String address, Double rating, String username){
        this.hotelId=hotelId;
        this.name=name;
        this.city=city;
        this.address=address;
        this.rating = rating;
        this.username=username;

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

    public String getCity() { return this.city; }
}
