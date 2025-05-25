package id.ac.ui.cs.advprog.authprofile.dto.response;

public class RatingSummaryResponse {
    private double averageRating;
    private int totalRatings;

    public RatingSummaryResponse() {}

    public RatingSummaryResponse(double averageRating, int totalRatings) {
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
}
