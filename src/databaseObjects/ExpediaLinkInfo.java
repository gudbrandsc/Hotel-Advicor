package databaseObjects;

public class ExpediaLinkInfo {
    private String hotelid;
    private String username;
    private String link;
    private String name;

    public ExpediaLinkInfo(String hotelid,String name, String username, String link){
        this.hotelid=hotelid;
        this.name=name;
        this.username=username;
        this.link = link;
    }

    public String getHotelid() {
        return this.hotelid;
    }

    public String getLink() {
        return this.link;
    }

    public String getUsername() {
        return this.username;
    }

    public String getName() {
        return this.name;
    }
}
