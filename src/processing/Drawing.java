package processing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Drawing {
	
	public static final Scalar 
	POINT_COLOR = new Scalar(0, 0, 255),
	LINE_COLOR = new Scalar(0, 0, 255), 
	ROTATED_COLOR = new Scalar(0, 100, 255);
	
	private static final int
	CIRCLE_RADIUS = 3,
	CIRCLE_THICKNESS = -1,
	CIRCLE_LINE_TYPE = 8,
	CIRCLE_SHIFT = 0;
	
	// Define constructor to make private
	private Drawing() {
		
	}
	
	public static Mat drawContours(Mat image, List<MatOfPoint> contours) {
		Mat drawn = image.clone();
		Imgproc.drawContours(drawn, contours, -1, LINE_COLOR);
		return drawn;
	}
	
	public static Mat drawRectangles(Mat image, List<Rect> rectangles) {
		Mat boxes = image.clone();
		for (Rect rect : rectangles) {
			Imgproc.rectangle(boxes, rect.br(), rect.tl(), LINE_COLOR, 2);
		}

		return boxes;
	}
	
	public static void drawRotatedRectangles(Mat image, List<RotatedRect> rectangles) {
		drawRotatedRectangles(image, rectangles, false);
	}

	public static void drawRotatedRectangles(Mat image, List<RotatedRect> rectangles, boolean inclDetails) {
		for (RotatedRect rotatedRect : rectangles) {
			// Draw the lines for the rotated rectangle edges
			Point[] vertices = new Point[4];
			rotatedRect.points(vertices);

			List<MatOfPoint> boxContours = new ArrayList<>();
			boxContours.add(new MatOfPoint(vertices));

			Imgproc.drawContours(image, boxContours, 0, ROTATED_COLOR, 2);
			
			if (inclDetails) {
				drawFittingLine(image, rotatedRect);
				drawAllCircles(image, vertices, POINT_COLOR);
				drawCircle(image, rotatedRect.center, ROTATED_COLOR);
			}
		}
	}

	public static void drawText(Mat image, Point ofs, String text) {
		Imgproc.putText(image, text, ofs, Core.FONT_HERSHEY_SIMPLEX, 1, LINE_COLOR);
	}

	public static void drawCircle(Mat image, Point center, Scalar color) {
		Imgproc.circle(image, center, CIRCLE_RADIUS, color, CIRCLE_THICKNESS, CIRCLE_LINE_TYPE, CIRCLE_SHIFT);
	}
	
	public static void drawAllCircles(Mat image, List<Point> centers, Scalar color) {
		for (Point center : centers)
			drawCircle(image, center, color);
	}
	
	public static void drawAllCircles(Mat image, Point[] centers, Scalar color) {
		for (Point center : centers)
			drawCircle(image, center, color);
	}
	
	public static void drawFittingLine(Mat image, RotatedRect rotatedRect) {
//		Mat line = new Mat();
//		Imgproc.fitLine(mPoints, line, Imgproc.CV_DIST_L2, 0, 0.01, 0.01);
	}
	
}
