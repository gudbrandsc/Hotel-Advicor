package database;

public class DatabaseBuilder {
    protected static final UserHandler userHandler = UserHandler.getInstance();
    protected static final HotelInfoBuilder hotelInfoBuilder = HotelInfoBuilder.getInstance();

    public static void main(String[] args){
        System.out.println("Building tables...");
    }
}
