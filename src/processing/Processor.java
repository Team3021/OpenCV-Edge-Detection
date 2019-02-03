package processing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Processor {
	public static final int HIST_SIZE = 256;
	public static final Scalar LINE_COLOR = new Scalar(0, 0, 255);
	public static final Scalar POINT_COLOR = new Scalar(0, 255, 0);
	
	
	public static Mat canny(Mat image, int thresh1, int thresh2) {
		Mat edges = new Mat();
		Imgproc.Canny(image, edges, thresh1, thresh2);
		return edges;
	}
	
	public static Mat autoCanny(Mat image, double sigma) { // Suggested sigma: 0.33
		double median = median(image);
		
		int lower = (int) Math.max(0, (1.0 - sigma) * median);
		int upper = (int) Math.min(255, (1.0 + sigma) * median);
		Mat edged = canny(image, lower, upper);
		
		System.out.println("Median: " + median + "; Canny thresholds: [" + lower + ", " + upper + "]");
		
		return edged;
	}
	
	public static double median(Mat channel) {
		ArrayList<Mat> listOfMat = new ArrayList<>();
		listOfMat.add(channel);
		MatOfInt channels = new MatOfInt(0);
		Mat mask = new Mat();
		Mat hist = new Mat(256, 1, CvType.CV_8UC1);
		MatOfInt histSize = new MatOfInt(256);
		MatOfFloat ranges = new MatOfFloat(0, 256);

		Imgproc.calcHist(listOfMat, channels, mask, hist, histSize, ranges);

		double t = channel.rows() * channel.cols() / 2;
		double total = 0;
		int med = -1;
		for (int row = 0; row < hist.rows(); row++) {
			double val = hist.get(row, 0)[0];
			if ((total <= t) && (total + val >= t)) {
				med = row;
				break;
			}
			total += val;
		}

		return med;
	}
	
	public static Mat gaussianBlur(Mat image, Size size, int factor) {
		Mat blur = new Mat();
		Imgproc.GaussianBlur(image, blur, size, factor);
		return blur;
	}
	
	public static Mat grayscale(Mat image) {
		Mat grayscale = new Mat();
		Imgproc.cvtColor(image, grayscale, Imgproc.COLOR_BGR2GRAY);
		return grayscale;
	}
	
	public static List<MatOfPoint> getContours(Mat image) {
		List<MatOfPoint> contours = new ArrayList<>();
		Mat contoured = new Mat();
		Imgproc.findContours(image, contours, contoured, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		return contours;
	}
	
	public static Mat drawContours(Mat image, List<MatOfPoint> contours) {
		Mat drawn = image.clone();
		Imgproc.drawContours(drawn, contours, -1, LINE_COLOR);
		return drawn;
	}
	
	public static List<Rect> getBoundingBoxes(List<MatOfPoint> contours, List<MatOfPoint> toKeep, int minArea) {
		List<Rect> rectangles = new ArrayList<>();
		System.out.println("Number of contours: " + contours.size());
		for (MatOfPoint contour : contours) {
				Rect rect =  Imgproc.boundingRect(contour);
				if (Targeting.isTargetStripe(rect, minArea)) {
					rectangles.add(rect);
					toKeep.add(contour);
				}
		}
		return rectangles;
	}
	
	public static Mat drawRectangles(Mat image, List<Rect> rectangles) {
		Mat boxes = image.clone();
		for (Rect rect : rectangles) {
			Imgproc.rectangle(boxes, rect.br(), rect.tl(), LINE_COLOR, 2);
		}
		
		return boxes;
	}
	

	public static void drawText(Mat image, Point ofs, String text) {
	    Imgproc.putText(image, text, ofs, Core.FONT_HERSHEY_SIMPLEX, 1, LINE_COLOR);
	}
	
	public static Mat drawContoursAdvanced(Mat image, List<MatOfPoint> contours) {
		Mat contourFilteredMat = image.clone();

		List<Rect> rectangles = new ArrayList<>();
		System.out.println("Number of contours: " + contours.size());
		int index = 0;
		for (MatOfPoint contour : contours) {
			index += 1;

			/*
			Points: [{282.0, 0.0}, {320.0, 95.0}, {467.0, 74.0}, {348.0, 157.0}, {511.0, 155.0}, {440.0, 115.0}, {516.0, 18.0}, {377.0, 95.0}, {376.0, 13.0}, {350.0, 104.0}, {298.0, 3.0}, {411.0, 0.0}] for index: 489
			*/
			
			double epsilon = 0.015 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);

			List<Point> points = approx.toList();

			// index of right = 475
			
			if (index == 475) {
				System.out.println("Points: " + points + " for index: " + index);

				for (Point point : points) {
					drawCircle(contourFilteredMat, point);
				}

				RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour. toArray()));

				Point[] vertices = new Point[4];
				rotatedRect.points(vertices);
				List<MatOfPoint> boxContours = new ArrayList<>();
				boxContours.add(new MatOfPoint(vertices));
				Imgproc.drawContours(contourFilteredMat, boxContours, 0, new Scalar(255, 255, 0), 2);

				Rect rect = rotatedRect.boundingRect();


				//					if (Targeting.isTargetStripe(rect, minArea)) {
				rectangles.add(rect);
				//					}
			}
		}
		
		System.out.println("contour count: " + index);

		Processor.drawText(contourFilteredMat, new Point(50,50), "Hello");
		
		contourFilteredMat = Processor.drawRectangles(contourFilteredMat, rectangles);

		return contourFilteredMat;
	}
		

    public static void drawCircle(Mat img, Point center) {
    	int radius = 3;
        int thickness = -1;
        int lineType = 8;
        int shift = 0;
        
        Imgproc.circle( img, center, radius, POINT_COLOR, thickness, lineType, shift);
    }
}
