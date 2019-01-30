package processing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Targeting {

	protected static final double STRIPE_ASPECT_RATIO = 2.0 / 5.0; // Width / Height of the target stripes
	protected static final double STRIPE_ASPECT_RATIO_TOLERANCE = 0.25;

	protected static final double STRIPE_TOP = 50;
	
	protected static final double STRIPE_WIDTH_MIN = 15;
	protected static final double STRIPE_HEIGHT_MIN = 50;
	
	protected static final double STRIPE_WIDTH = 17;
	protected static final double STRIPE_HEIGHT = 150;
	
	protected static final double STRIPE_WIDTH_MAX = 23;
	protected static final double STRIPE_HEIGHT_MAX = 150;
	
	protected static final double STRIPE_OFFSET = 80;
	
	protected static final Size STRIPE_SIZE = new Size(STRIPE_WIDTH, STRIPE_HEIGHT);
	
	private Targeting() {
		
	}
	
	/**
	 * Get rect, noob
	 * @param boxes - all bounding boxes
	 * @return all target stripes
	 */
	public static List<Rect> filterBoundingBoxes(List<Rect> boxes, int minArea) {
		List<Rect> filtered = new ArrayList<>();
		for (Rect box : boxes) {
			if (isTargetStripe(box, minArea))
				filtered.add(box);
		}
		return filtered;
	}
	
	public static boolean isTargetStripe(Rect rect, double minArea) {
		double width = rect.width;
		double height = rect.height;
		
		double area = width * height;
		System.out.println("Area: " + area);
		
		double rectangleAspectRatio = (width / height);
		
		double tolerance = STRIPE_ASPECT_RATIO * STRIPE_ASPECT_RATIO_TOLERANCE;
		
		double lowerRange = STRIPE_ASPECT_RATIO - tolerance;
		double upperRange = STRIPE_ASPECT_RATIO + tolerance;
		
//		System.out.println(lowerRange + " " + rectangleAspectRatio + " " + upperRange) ;
		
//		return lowerRange < rectangleAspectRatio 
//		    && rectangleAspectRatio < upperRange
//		    && area > minArea;
		
		return area > minArea;
		
	}
	
}
