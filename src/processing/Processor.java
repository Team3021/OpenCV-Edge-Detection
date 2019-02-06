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
	public static final Scalar POINT_COLOR = new Scalar(0, 0, 255);
	public static final Scalar ROTATED_COLOR = new Scalar(0, 156, 255);
	
	
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
		Imgproc.findContours(image, contours, contoured, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
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
			double epsilon = 0.015 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);

			
			int points = approx.toList().size();

			if (points == 4) {
				RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour. toArray()));
				Rect rect = rotatedRect.boundingRect();

				if (rotatedRect.size.width * rotatedRect.size.height > minArea) {
//					System.out.println("Area: " + rotatedRect.size.width * rotatedRect.size.height + " Points: " + points);

					rectangles.add(rect);
					toKeep.add(contour);
				}
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

		for (MatOfPoint contour : contours) {
			
			double epsilon = 0.015 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);

			List<Point> points = approx.toList();

			if (points.size() == 4) {

				RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour. toArray()));
				
				Rect rect = rotatedRect.boundingRect();

				if (rotatedRect.size.width * rotatedRect.size.height > 1000) {
					System.out.println("Area: " + rotatedRect.size.width * rotatedRect.size.height + " Points: " + points + " Angle: " + rotatedRect.angle);
					
					// Draw the lines for the rotated rectangle edges
					Point[] vertices = new Point[4];
					rotatedRect.points(vertices);
					
					MatOfPoint matOfVertices = new MatOfPoint(vertices);
					
					List<MatOfPoint> boxContours = new ArrayList<>();
					boxContours.add(matOfVertices);
					
					Imgproc.drawContours(contourFilteredMat, boxContours, 0, ROTATED_COLOR, 2);
					
					// Draw the fitting line of the rotated rectangle
					drawFittedLine(contourFilteredMat, matOfVertices, 2, ROTATED_COLOR);
	
					// Draw the four points of the rotated rectangle
					for (Point point : points) {
						drawCircle(contourFilteredMat, point, POINT_COLOR);
					}
					
					// Draw the center of the rotated rectangle
					drawCircle(contourFilteredMat, rotatedRect.center, ROTATED_COLOR);
					
					// keep the bounding rectangle
					rectangles.add(rect);
				}

			}
		}
		
		Processor.drawText(contourFilteredMat, new Point(50,50), "Hello");
		
//		contourFilteredMat = Processor.drawRectangles(contourFilteredMat, rectangles);

		return contourFilteredMat;
	}

	public static void drawFittedLine(Mat img, MatOfPoint points, int thickness, Scalar color) {
		Mat line = new Mat();
		
		// Fit a line to the set of points
		Imgproc.fitLine(points, line, Imgproc.CV_DIST_L2, 0, 0.01, 0.01);
		
		// Get the line vector and starting point data from the mat of the line
		float[] vx = new float[1];
		float[] vy = new float[1];
		float[] x0 = new float[1];
		float[] x1 = new float[1];
		
		line.get(0, 0, vx);
		line.get(1, 0, vy);
		line.get(2, 0, x0);
		line.get(3, 0, x1);

		float[] lineData = new float[]{vx[0], vy[0], x0[0], x1[0]};

		// get the image size to limit the length of the resulting line
	    double theMult = Math.max(img.height(), img.width());
	    
	    // calculate start point
	    Point startPoint = new Point();
	    startPoint.x = lineData[2]- theMult*lineData[0]; // x[0]
	    startPoint.y = lineData[3] - theMult*lineData[1]; // y[0]
	    
	    // calculate end point
	    Point endPoint = new Point();
	    endPoint.x = lineData[2]+ theMult*lineData[0]; //x[1]
	    endPoint.y = lineData[3] + theMult*lineData[1]; //y[1]

	    // draw overlay of bottom lines on image
//	    cvClipLine(cvGetSize(img), &startPoint, &endPoint);
	    Imgproc.line(img, startPoint, endPoint, color, thickness, 8, 0);
	}

    public static void drawCircle(Mat img, Point center, Scalar color) {
    	int radius = 3;
        int thickness = -1;
        int lineType = 8;
        int shift = 0;
        
        Imgproc.circle( img, center, radius, color, thickness, lineType, shift);
    }
}
