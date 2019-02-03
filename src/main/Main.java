package main;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

import processing.Processor;

public class Main {

	public static void init() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) {
		init();
		
		System.out.println("Reading image");
		Mat image = IO.loadImage("res/Blue Cargo - Simulated Low.png");
		
//		System.out.println("Blurring image");
//		Mat blur = Processor.gaussianBlur(image, new Size(3, 3), 2);
//		IO.writeImage("res/blur.jpg", blur);

		System.out.println("Converting to grayscale");
		Mat gray = Processor.grayscale(image);
		IO.writeImage("out/gray.jpg", gray);
	
		System.out.println("Detecting edges");
		Mat edges = Processor.canny(gray, 45, 50);
		IO.writeImage("out/edges.jpg", edges);
		
		System.out.println("Drawing bounding boxes");
		
		System.out.println("Getting contours");
		List<MatOfPoint> contours = Processor.getContours(edges);
		
		List<MatOfPoint> contoursFiltered = new ArrayList<>();
		List<Rect> rectangles = Processor.getBoundingBoxes(contours, contoursFiltered, 300);
		
		Mat contourMat = Processor.drawContours(image, contours);
		IO.writeImage("out/contours.jpg", contourMat);

		Mat contourFilteredMat = Processor.drawContoursAdvanced(image, contours);
		IO.writeImage("out/contours-filtered.jpg", contourFilteredMat);
		
		Mat stripes = Processor.drawRectangles(image, rectangles);
		IO.writeImage("out/boxes.jpg", stripes);
		
	}
	
}
