package database;

public class DatabaseBuilder {
   protected static final UserHandler userHandler = UserHandler.getInstance();
   protected static final HotelInfoBuilder hotelInfoBuilder = HotelInfoBuilder.getInstance();
   protected static final HotelReviewBuilder hotelReviewBuilder  = HotelReviewBuilder.getInstance();
   protected static final SetHotelRating setHotelRating  = SetHotelRating.getInstance();


    public static void main(String[] args){
        System.out.println("All tables are built");
    }
}
