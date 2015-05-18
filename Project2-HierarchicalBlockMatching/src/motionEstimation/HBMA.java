//package motionEstimation;
//
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.highgui.*;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintStream;
//
//public class HBMA {
//	private static final int blockSize = 8;
//	private static final int searchSize = 64;
//
//	public static void main(String[] args) throws FileNotFoundException {
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//		//Load the image
//		Mat frame10 = Highgui.imread("frame10.png");
//		Mat frame11 = Highgui.imread("frame11.png");
//
//		//Downsampling
//		Mat frame10By2 = new Mat(frame10.rows()/2, frame10.cols()/2, frame10.type());
//		Mat frame11By2 = new Mat(frame11.rows()/2, frame11.cols()/2, frame11.type());
//		Mat frame10By4 = new Mat(frame10.rows()/4, frame10.cols()/4, frame10.type());
//		Mat frame11By4 = new Mat(frame11.rows()/4, frame11.cols()/4, frame11.type());
//
//		Imgproc.pyrDown(frame10, frame10By2);
//		Imgproc.pyrDown(frame11, frame11By2);
//		Imgproc.pyrDown(frame10By2, frame10By4);
//		Imgproc.pyrDown(frame11By2, frame11By4);
//		
//		Mat V2 = new Mat(frame10.rows()/4, frame10.cols()/4, CvType.CV_64FC2);
//		Mat V1 = new Mat(frame10.rows()/2, frame10.cols()/2, CvType.CV_64FC2);
//		Mat V0 = new Mat(frame10.rows(), frame10.cols(), CvType.CV_64FC2);
//		Mat V1New = new Mat(frame10.rows()/2, frame10.cols()/2, CvType.CV_64FC2);
//		Mat V0New = new Mat(frame10.rows(), frame10.cols(), CvType.CV_64FC2);
//		Mat tempV2 = new Mat(frame10.rows()/2, frame10.cols()/2, CvType.CV_64FC2);
//		Mat tempV1 = new Mat(frame10.rows(), frame10.cols(), CvType.CV_64FC2);
//
//		//Block matching for the level2 resolution frame
//		int lastI = frame10By4.rows()-(frame10By4.rows()%blockSize);
//		int lastJ = frame10By4.cols()-(frame10By4.cols()%blockSize);
//		for(int i=0; i<lastI; i+=blockSize) {
//			for (int j = 0; j<lastJ; j+=blockSize) {
//				Mat sourceBlock = frame10By4.submat(i,i+blockSize,j,j+blockSize);
//
//				//Matching at the destination
//				Scalar minMeanSqrdDiff = new Scalar(0);
//				boolean minUninit = true;
//				Point pt1 = new Point(i, j);
//				Point pt2 = new Point(0, 0);
//				int startK, startL, endK, endL;
//				startK = (i<((searchSize/2)-(blockSize/2)))?0:(i-((searchSize/2)-(blockSize/2)));
//				startL = (j<((searchSize/2)-(blockSize/2)))?0:(j-((searchSize/2)-(blockSize/2)));
//				endK   = (i+((searchSize/2)+(blockSize/2)))>frame11By4.rows()?frame11By4.rows():(i+((searchSize/2)+(blockSize/2)));
//				endL   = (j+((searchSize/2)+(blockSize/2)))>frame11By4.cols()?frame11By4.cols():(j+((searchSize/2)+(blockSize/2)));
//				double indices[] = new double[2];
//				for(int k=startK;k<endK;k++){
//					for(int l=startL;l<endL;l++){
//						if(k+blockSize<endK && l+blockSize<endL) {
//							Mat destinationBlock = frame11By4.submat(k, k+blockSize, l, l+blockSize);
//							Mat currDiffBlock = new Mat(blockSize, blockSize, frame10By4.type());
//							Mat sqrdDiffBlock = new Mat(blockSize, blockSize, frame10By4.type());						
//							Core.absdiff(destinationBlock, sourceBlock, currDiffBlock);
//							Core.multiply(currDiffBlock, currDiffBlock, sqrdDiffBlock);
//							Scalar minSAD = Core.sumElems(sqrdDiffBlock);
//							minSAD.mul(new Scalar(1/(blockSize*blockSize)));
//							if((minSAD.val[0] < minMeanSqrdDiff.val[0]) || minUninit ){
//								minUninit = false;
//								minMeanSqrdDiff = minSAD;
//								indices[0] = k;
//								indices[1] = l;
//								pt2.set(indices);
//								V2.put(k, l, indices);
//								indices[0] = 2*indices[0];
//								indices[1] = 2*indices[1];
//								tempV2.put(2*k, 2*l, indices);
//							}
//						} else {
//							break;
//						}						
//					}
//				}
////				V2.put(i, j, indices);
////				indices[0] = 2*indices[0];
////				indices[1] = 2*indices[1];
////				tempV2.put(2*i, 2*j, indices);
//
//				//Drawing Arrow
//				Core.line(frame10By4, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				double angle; 
//				angle = Math.atan2( (double) pt1.y - pt2.y, (double) pt1.x - pt2.x ); 
//				double hypotenuse; 
//				hypotenuse = Math.sqrt( Math.pow((pt1.y - pt2.y), 2) + Math.pow((pt1.x - pt2.x), 2));
//				hypotenuse = hypotenuse/8;
//				pt1.x = (int) (pt2.x+hypotenuse*Math.cos(angle+Math.PI/4));
//				pt1.y = (int) (pt2.y+hypotenuse*Math.sin(angle+Math.PI/4));
//				Core.line(frame10By4, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt2.x+hypotenuse*Math.cos(angle-Math.PI/4));
//				pt1.y = (int) (pt2.y+hypotenuse*Math.sin(angle-Math.PI/4));
//				Core.line(frame10By4, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//			}
//		}
//
//		File file = new File("V2.txt");
//		FileOutputStream fos = new FileOutputStream(file);
//		PrintStream ps = new PrintStream(fos);
//		System.setOut(ps);
//		System.out.println("V2 :" + V2.dump());
//		Highgui.imwrite("frame10By4.jpg",frame10By4);
//		
//		//Block matching for the level1 resolution frame
//		Mat frame10By2NewMV = frame10.clone();
//		lastI = frame10By2.rows()-(frame10By2.rows()%blockSize);
//		lastJ = frame10By2.cols()-(frame10By2.cols()%blockSize);
//		for(int i=0; i<lastI; i+=blockSize) {
//			for (int j = 0; j<lastJ; j+=blockSize) {
//				Mat sourceBlock = frame10By2.submat(i,i+blockSize,j,j+blockSize);
//				Scalar minMeanSqrdDiff = new Scalar(0);
//				boolean minUninit = true;
//				Point pt1 = new Point(i, j);
//				Point pt2 = new Point(0, 0);
//				Point pt3 = new Point(0, 0);
//				int startK, startL, endK, endL;
//				startK = (i<((searchSize/4)-(blockSize/2)))?0:(i-((searchSize/4)-(blockSize/2)));
//				startL = (j<((searchSize/4)-(blockSize/2)))?0:(j-((searchSize/4)-(blockSize/2)));
//				endK   = (i+((searchSize/4)+(blockSize/2)))>frame11By2.rows()-1?frame11By2.rows()-1:(i+((searchSize/4)+(blockSize/2)));
//				endL   = (j+((searchSize/4)+(blockSize/2)))>frame11By2.cols()-1?frame11By2.cols()-1:(j+((searchSize/4)+(blockSize/2)));
//				double indices[] = new double[2];
//				//Matching at the destination
//				for(int k=startK;k<endK;k+=blockSize){
//					for(int l=startL;l<endL;l+=blockSize){
//						if(k+blockSize<endK && l+blockSize<endL) {
//							Mat destinationBlock = frame11By2.submat(k, k+blockSize, l, l+blockSize);
//							Mat currDiffBlock = new Mat(blockSize, blockSize, frame10By2.type());
//							Mat sqrdDiffBlock = new Mat(blockSize, blockSize, frame10By2.type());						
//							Core.absdiff(destinationBlock, sourceBlock, currDiffBlock);
//							Core.multiply(currDiffBlock, currDiffBlock, sqrdDiffBlock);
//							Scalar minSAD = Core.sumElems(sqrdDiffBlock);
//							minSAD.mul(new Scalar(1/(blockSize*blockSize)));
//							if((minSAD.val[0] < minMeanSqrdDiff.val[0]) || minUninit ){
//								minUninit = false;
//								minMeanSqrdDiff = minSAD;
//								indices[0] = k;
//								indices[1] = l;
//								pt2.set(indices);
//								V1.put(k, l, indices);
//								//Core.absdiff(tempV2, V1, V1New);
//								V1New.get(k, l)[0] = (V1.get(k, l)[0] + tempV2.get(k, l)[0])/2;
//								V1New.get(k, l)[1] = (V1.get(k, l)[1] + tempV2.get(k, l)[1])/2;
//								pt3.set(V1New.get(k, l));
//								V1New.get(k, l)[0] = 2*V1New.get(k, l)[0];
//								V1New.get(k, l)[1] = 2*V1New.get(k, l)[0];
//								tempV1.put(2*k, 2*l, V1New.get(k, l));
//							}						
//						} else {
//							break;
//						}
//					}
//				}
////				V1.put(i, j, indices);
////				//Core.absdiff(tempV2, V1, V1New);
////				V1New.get(i, j)[0] = (V1.get(i, j)[0]+tempV2.get(i, j)[0])/2;
////				V1New.get(i, j)[1] = (V1.get(i, j)[1]+tempV2.get(i, j)[1])/2;
////				pt2.set(V1New.get(i, j));
////				V1New.get(i, j)[0] = 2*V1New.get(i, j)[0];
////				V1New.get(i, j)[1] = 2*V1New.get(i, j)[0];
////				tempV1.put(2*i, 2*j, V1New.get(i, j));
//
//				//Drawing Arrow
//				Core.line(frame10By2, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				double angle; 
//				angle = Math.atan2( (double) pt1.y - pt2.y, (double) pt1.x - pt2.x ); 
//				double hypotenuse; 
//				hypotenuse = Math.sqrt( Math.pow((pt1.y - pt2.y), 2) + Math.pow((pt1.x - pt2.x), 2));
//				hypotenuse = hypotenuse/4;
//				pt1.x = (int) (pt2.x+hypotenuse*Math.cos(angle+Math.PI/4));
//				pt1.y = (int) (pt2.y+hypotenuse*Math.sin(angle+Math.PI/4));
//				Core.line(frame10By2, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt2.x+hypotenuse*Math.cos(angle-Math.PI/4));
//				pt1.y = (int) (pt2.y+hypotenuse*Math.sin(angle-Math.PI/4));
//				Core.line(frame10By2, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				//Drawing Arrow
//				Core.line(frame10By2NewMV, pt1, pt3, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt3.x+hypotenuse*Math.cos(angle+Math.PI/4));
//				pt1.y = (int) (pt3.y+hypotenuse*Math.sin(angle+Math.PI/4));
//				Core.line(frame10By2NewMV, pt1, pt3, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt3.x+hypotenuse*Math.cos(angle-Math.PI/4));
//				pt1.y = (int) (pt3.y+hypotenuse*Math.sin(angle-Math.PI/4));
//				Core.line(frame10By2NewMV, pt1, pt3, new Scalar(0,0,255), 1,8,0);
//			}
//		}
//		
//		File file2 = new File("V1.txt");
//		FileOutputStream fos2 = new FileOutputStream(file2);
//		PrintStream ps2 = new PrintStream(fos2);
//		System.setOut(ps2);
//		System.out.println("V1"+V1.dump());
//
//		Highgui.imwrite("frame10By2.jpg",frame10By2);
//		Highgui.imwrite("frame10By2NewMV.jpg",frame10By2NewMV);
//		
//		//Block matching for the level0 resolution frame
//		Mat frame10NewMV = frame10.clone();
//		lastI = frame10.rows()-(frame10.rows()%blockSize);
//		lastJ = frame10.cols()-(frame10.cols()%blockSize);
//		for(int i=0; i<lastI; i+=blockSize) {
//			for (int j = 0; j<lastJ; j+=blockSize) {
//				Mat sourceBlock = frame10.submat(i,i+blockSize,j,j+blockSize);
//				Scalar minMeanSqrdDiff = new Scalar(0);
//				boolean minUninit = true;
//				Point pt1 = new Point(i, j);
//				Point pt2 = new Point(0, 0);
//				Point pt3 = new Point(0, 0);
//				int startK, startL, endK, endL;
//				startK = (i<((searchSize/8)-(blockSize/2)))?0:(i-((searchSize/8)-(blockSize/2)));
//				startL = (j<((searchSize/8)-(blockSize/2)))?0:(j-((searchSize/8)-(blockSize/2)));
//				endK   = (i+((searchSize/8)+(blockSize/2)))>frame11.rows()-1?frame11.rows()-1:(i+((searchSize/8)+(blockSize/2)));
//				endL   = (j+((searchSize/8)+(blockSize/2)))>frame11.cols()-1?frame11.cols()-1:(j+((searchSize/8)+(blockSize/2)));
//				double indices[] = new double[2];
//				//Matching at the destination
//				for(int k=startK;k<endK;k+=blockSize){
//					for(int l=startL;l<endL;l+=blockSize){
//						if(k+blockSize<endK && l+blockSize<endL) {
//							Mat destinationBlock = frame11.submat(k, k+blockSize, l, l+blockSize);
//							Mat currDiffBlock = new Mat(blockSize, blockSize, frame10.type());
//							Mat sqrdDiffBlock = new Mat(blockSize, blockSize, frame10.type());						
//							Core.absdiff(destinationBlock, sourceBlock, currDiffBlock);
//							Core.multiply(currDiffBlock, currDiffBlock, sqrdDiffBlock);
//							Scalar minSAD = Core.sumElems(sqrdDiffBlock);
//							minSAD.mul(new Scalar(1/(blockSize*blockSize)));
//							if((minSAD.val[0] < minMeanSqrdDiff.val[0]) || minUninit ){
//								minUninit = false;
//								minMeanSqrdDiff = minSAD;
//								indices[0] = k;
//								indices[1] = l;
//								pt2.set(indices);
//								V0.put(k, l, indices);
//								//Core.absdiff(tempV2, V1, V1New);
//								V0New.get(k, l)[0] = (V0.get(k, l)[0] + tempV1.get(k, l)[0])/2;
//								V0New.get(k, l)[1] = (V0.get(k, l)[1] + tempV1.get(k, l)[1])/2;
//								pt3.set(V0New.get(k, l));
//							}
//						} else {
//							break;
//						}						
//					}
//				}
//
//				//Drawing Arrow
//				Core.line(frame10, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				double angle; 
//				angle = Math.atan2( (double) pt1.y - pt2.y, (double) pt1.x - pt2.x ); 
//				double hypotenuse; 
//				hypotenuse = Math.sqrt( Math.pow((pt1.y - pt2.y), 2) + Math.pow((pt1.x - pt2.x), 2));
//				hypotenuse = hypotenuse/4;
//				pt1.x = (int) (pt2.x+hypotenuse*Math.cos(angle+Math.PI/4));
//				pt1.y = (int) (pt2.y+hypotenuse*Math.sin(angle+Math.PI/4));
//				Core.line(frame10, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt2.x+hypotenuse*Math.cos(angle-Math.PI/4));
//				pt1.y = (int) (pt2.y+hypotenuse*Math.sin(angle-Math.PI/4));
//				Core.line(frame10, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//				
//				//Drawing Arrow
//				Core.line(frame10NewMV, pt1, pt3, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt3.x+hypotenuse*Math.cos(angle+Math.PI/4));
//				pt1.y = (int) (pt3.y+hypotenuse*Math.sin(angle+Math.PI/4));
//				Core.line(frame10NewMV, pt1, pt3, new Scalar(0,0,255), 1,8,0);
//				pt1.x = (int) (pt3.x+hypotenuse*Math.cos(angle-Math.PI/4));
//				pt1.y = (int) (pt3.y+hypotenuse*Math.sin(angle-Math.PI/4));
//				Core.line(frame10NewMV, pt1, pt3, new Scalar(0,0,255), 1,8,0);
//			}
//		}
//		
//		File file3 = new File("V0.txt");
//		FileOutputStream fos3 = new FileOutputStream(file3);
//		PrintStream ps3 = new PrintStream(fos3);
//		System.setOut(ps3);
//		System.out.println("V0"+V0.dump());
//		
//		Highgui.imwrite("frame10New.jpg",frame10);
//		Highgui.imwrite("frame10NewMV.jpg",frame10NewMV);
//	}
//
//}
