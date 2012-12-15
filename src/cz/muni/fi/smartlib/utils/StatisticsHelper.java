package cz.muni.fi.smartlib.utils;


/**
 * Helper class for statistics of a book
 * 
 * */
public class StatisticsHelper {
	public static final int RATING_STARS_ONE_COUNT = 0;
	public static final int RATING_STARS_TWO_COUNT = 1;
	public static final int RATING_STARS_THREE_COUNT = 2;
	public static final int RATING_STARS_FOUR_COUNT = 3;
	public static final int RATING_STARS_FIVE_COUNT = 4;
	
	public static final int MAX_WIDTH = 100;
	public static final int ZERO_RATING_WIDTH = 5;
	
	private int[] ratingStarsArray;
	private int highestCountStarType;
	
	public StatisticsHelper(int[] ratingStarsArray) {
		this.ratingStarsArray = ratingStarsArray;
		highestCountStarType = getHighestCountStar();
	}
	
	public int getWidthForView(int starType) {
		if (ratingStarsArray[highestCountStarType] > 0) {
			return ZERO_RATING_WIDTH + ((MAX_WIDTH / ratingStarsArray[highestCountStarType]) * ratingStarsArray[starType]);
		} 
		return ZERO_RATING_WIDTH;
	}
	
	public int getRatingsCount(int starType) {
		return ratingStarsArray[starType];
	}
	
	public int getOveralRatingsCount() {
		int sum = 0;
		for (int i = 0; i < ratingStarsArray.length; i++) {
			sum += ratingStarsArray[i];
		}
		return sum;
	}
	
	public double getAverageRating() {
		double sum = 0;
		for (int i = 0; i < ratingStarsArray.length; i++) {
			sum += (i + 1) * ratingStarsArray[i];
		}
		if (getOveralRatingsCount() != 0) {
			return (sum / getOveralRatingsCount());
		}
		return 0;
	}
	
	/**
	 * Get rating type whose count is the biggest 
	 * 
	 * */
	private int getHighestCountStar () {
		int max = ratingStarsArray[0];
		int ratingType = RATING_STARS_ONE_COUNT;
		
		for (int i = 0; i < ratingStarsArray.length; i++) {
			if (ratingStarsArray[i] > max) {
				max = ratingStarsArray[i];
				ratingType = i;
			}
		}
		
		return ratingType;
	}
	
	
	
}
