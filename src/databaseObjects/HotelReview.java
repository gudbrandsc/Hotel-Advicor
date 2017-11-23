package databaseObjects;

public class HotelReview {
    private String hotelid;
    private String reviewid;
    private int rating;
    private String title;
    private String review;
    private String date;
    private String username ;

    public HotelReview(String hotelid, String reviewid,int rating,String title, String review,String date, String username){
        this.hotelid = hotelid;
        this.reviewid =reviewid;
        this.rating=rating;
        this.title=title;
        this.review=review;
        this.date=date;
        this.username=username;

    }

    public String getHotelid() { return this.hotelid; }

    public String getReviewid() { return this.reviewid; }

    public int getRating() { return this.rating; }

    public String getReview() { return this.review; }

    public String getDate() { return this.date; }

    public String getUsername() { return this.username; }

    public String getTitle() { return this.title; }
}
