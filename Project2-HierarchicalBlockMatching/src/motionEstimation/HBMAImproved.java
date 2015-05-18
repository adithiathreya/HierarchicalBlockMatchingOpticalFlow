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
//public class HBMAImproved {
//	private static final int blockSize = 8;
//	private static final int searchSize = 64;
//
//	public static void main(String[] args) throws FileNotFoundException {
//		long startTime = System.nanoTime();
//		try {
//			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//			//Load the image
//			Mat frame10 = Highgui.imread("/Users/adithiathreya/Desktop/SCU/COEN296-VideoProcessing/other-data-gray/Beanbags/frame10.png");
//			Mat frame11 = Highgui.imread("/Users/adithiathreya/Desktop/SCU/COEN296-VideoProcessing/other-data-gray/Beanbags/frame11.png");
//
//			//Downsampling
//			Mat frame10NewMV = frame10.clone();
//			Mat frame10By2 = new Mat(frame10.rows()/2, frame10.cols()/2, frame10.type());
//			Mat frame11By2 = new Mat(frame11.rows()/2, frame11.cols()/2, frame11.type());
//			Mat frame10By4 = new Mat(frame10.rows()/4, frame10.cols()/4, frame10.type());
//			Mat frame11By4 = new Mat(frame11.rows()/4, frame11.cols()/4, frame11.type());
//
//			Imgproc.pyrDown(frame10, frame10By2);
//			Imgproc.pyrDown(frame11, frame11By2);
//			Imgproc.pyrDown(frame10By2, frame10By4);
//			Imgproc.pyrDown(frame11By2, frame11By4);
//
//			Mat mVBy4 = new Mat(frame10By4.rows(), frame10By4.cols(), CvType.CV_64FC2);
//			Mat mVBy2 = new Mat(frame10By2.rows(), frame10By2.cols(), CvType.CV_64FC2);
//			Mat mV = new Mat(frame10.rows(), frame10.cols(), CvType.CV_64FC2);
//
//			int lastI = frame10By4.rows()-(frame10By4.rows()%blockSize);
//			int lastJ = frame10By4.cols()-(frame10By4.cols()%blockSize);
//			for(int iBy4=0; iBy4<lastI; iBy4+=blockSize) {
//				for (int jBy4=0; jBy4<lastJ; jBy4+=blockSize) {
//
//					//Block matching for the least resolution frame
//					Mat sourceBlock = frame10By4.submat(iBy4,iBy4+blockSize,jBy4,jBy4+blockSize);
//					Scalar minSAD = new Scalar(65536);
//					Point pt1By4 = new Point(jBy4, iBy4);
//					int startK, startL, endK, endL;
//					startK = (iBy4-((searchSize/2)-(blockSize/2)))<0?0:(iBy4-((searchSize/2)-(blockSize/2)));
//					startL = (jBy4-((searchSize/2)-(blockSize/2)))<0?0:(jBy4-((searchSize/2)-(blockSize/2)));
//					endK   = (iBy4+((searchSize/2)+(blockSize/2)))>frame11By4.rows()?frame11By4.rows():(iBy4+((searchSize/2)+(blockSize/2)));
//					endL   = (jBy4+((searchSize/2)+(blockSize/2)))>frame11By4.cols()?frame11By4.cols():(jBy4+((searchSize/2)+(blockSize/2)));
//					double indicesBy4[] = new double[2];
//					for(int k=startK; k<endK; k++){
//						for(int l=startL; l<endL; l++){
//							if(k+blockSize<endK && l+blockSize<endL) {
//								Mat destinationBlock = frame11By4.submat(k, k+blockSize, l, l+blockSize);
//								Mat currDiffBlock = new Mat(blockSize, blockSize, frame10By4.type());
//								Core.absdiff(destinationBlock, sourceBlock, currDiffBlock);
//								Scalar SAD = Core.sumElems(currDiffBlock);
//								if(SAD.val[0] < minSAD.val[0]){
//									minSAD = SAD;
//									indicesBy4[0] = k;
//									indicesBy4[1] = l;
//								} else {
//									continue;
//								}
//							} else {
//								break;
//							}						
//						}
//					}
//					mVBy4.put(iBy4, jBy4, indicesBy4[0]-iBy4, indicesBy4[1]-jBy4);
//					Point pt2By4 = new Point(jBy4+mVBy4.get(iBy4, jBy4)[1], iBy4+mVBy4.get(iBy4, jBy4)[0]);
//					//Drawing Arrow
//					Core.line(frame10By4, pt1By4, pt2By4, new Scalar(0,0,255), 1,8,0);
//					double angle; 
//					angle = Math.atan2( (double) (pt1By4.y - pt2By4.y), (double) (pt1By4.x - pt2By4.x) ); 
//					double hypotenuse; 
//					hypotenuse = Math.sqrt( Math.pow((pt1By4.y - pt2By4.y), 2) + Math.pow((pt1By4.x - pt2By4.x), 2));
//					hypotenuse = hypotenuse/4;
//					pt1By4.x = (int) (pt2By4.x+hypotenuse*Math.cos(angle+Math.PI/4));
//					pt1By4.y = (int) (pt2By4.y+hypotenuse*Math.sin(angle+Math.PI/4));
//					Core.line(frame10By4, pt2By4, pt1By4, new Scalar(0,0,255), 1,8,0);
//					pt1By4.x = (int) (pt2By4.x+hypotenuse*Math.cos(angle - Math.PI/4));
//					pt1By4.y = (int) (pt2By4.y+hypotenuse*Math.sin(angle - Math.PI/4));
//					Core.line(frame10By4, pt2By4, pt1By4, new Scalar(0,0,255), 1,8,0);
//
//					//Block matching for medium resolution frame
//					for (int iBy2=2*iBy4; iBy2<(2*iBy4+2*blockSize); iBy2+=blockSize) {
//						for (int jBy2=2*jBy4; jBy2<(2*jBy4+2*blockSize); jBy2+=blockSize) {
//							Point pt1By2 = new Point(jBy2, iBy2);
//							int i2 = (int)(iBy2+2*mVBy4.get(iBy4, jBy4)[0]);
//							int j2 = (int)(jBy2+2*mVBy4.get(iBy4, jBy4)[1]);
//							sourceBlock = frame10By2.submat(iBy2,iBy2+blockSize,jBy2,jBy2+blockSize);
//							minSAD = new Scalar(65536);
//							double indicesBy2[] = new double[2];
//							startK = (i2<((searchSize/4)-(blockSize/2)))?0:(i2-((searchSize/4)-(blockSize/2)));
//							startL = (j2<((searchSize/4)-(blockSize/2)))?0:(j2-((searchSize/4)-(blockSize/2)));
//							endK   = (i2+((searchSize/4)+(blockSize/2)))>frame11By2.rows()?frame11By2.rows():(i2+((searchSize/4)+(blockSize/2)));
//							endL   = (j2+((searchSize/4)+(blockSize/2)))>frame11By2.cols()?frame11By2.cols():(j2+((searchSize/4)+(blockSize/2)));
//							for(int k=startK;k<endK;k++){
//								for(int l=startL;l<endL;l++){
//									if(k+blockSize<endK && l+blockSize<endL) {
//										Mat destinationBlock = frame11By2.submat(k, k+blockSize, l, l+blockSize);
//										Mat currDiffBlock = new Mat(blockSize, blockSize, frame10By2.type());
//										Core.absdiff(destinationBlock, sourceBlock, currDiffBlock);
//										Scalar SAD = Core.sumElems(currDiffBlock);
//										if(SAD.val[0] < minSAD.val[0]){
//											minSAD = SAD;
//											indicesBy2[0] = k;
//											indicesBy2[1] = l;
//										} else {
//											continue;
//										}
//									} else {
//										break;
//									}						
//								}
//							}
//							mVBy2.put(iBy2, jBy2, indicesBy2[0]-iBy2, indicesBy2[1]-jBy2);
//							Point pt2By2 = new Point(jBy2+mVBy2.get(iBy2, jBy2)[1], iBy2+mVBy2.get(iBy2, jBy2)[0]);
//							
//							//Drawing Arrow
//							Core.line(frame10By2, pt1By2, pt2By2, new Scalar(0,0,255), 1,8,0);
//							angle = Math.atan2( (double)( pt1By2.y - pt2By2.y), (double) (pt1By2.x - pt2By2.x) ); 
//							hypotenuse = Math.sqrt( Math.pow((pt1By2.y - pt2By2.y), 2) + Math.pow((pt1By2.x - pt2By2.x), 2));
//							hypotenuse = hypotenuse/8;
//							pt1By2.x = (int) (pt2By2.x+hypotenuse*Math.cos(angle+Math.PI/4));
//							pt1By2.y = (int) (pt2By2.y+hypotenuse*Math.sin(angle+Math.PI/4));
//							Core.line(frame10By2, pt1By2, pt2By2, new Scalar(0,0,255), 1,8,0);
//							pt1By2.x = (int) (pt2By2.x+hypotenuse*Math.cos(angle-Math.PI/4));
//							pt1By2.y = (int) (pt2By2.y+hypotenuse*Math.sin(angle-Math.PI/4));
//							Core.line(frame10By2, pt1By2, pt2By2, new Scalar(0,0,255), 1,8,0);
//
//							//Block matching for high resolution frame
//							for (int i=2*iBy2; i<(2*iBy2+2*blockSize); i+=blockSize) {
//								for (int j=2*jBy2; j<(2*jBy2+2*blockSize); j+=blockSize) {
//									Point pt1 = new Point(j, i);
//									int i3 = (int)(i+2*mVBy2.get(iBy2, jBy2)[0]);
//									int j3 = (int)(j+2*mVBy2.get(iBy2, jBy2)[1]);
//									sourceBlock = frame10.submat(i,i+blockSize,j,j+blockSize);
//									minSAD = new Scalar(65536);
//									double indices[] = new double[2];
//									startK = (i3<((searchSize/8)-(blockSize/2)))?0:(i3-((searchSize/8)-(blockSize/2)));
//									startL = (j3<((searchSize/8)-(blockSize/2)))?0:(j3-((searchSize/8)-(blockSize/2)));
//									endK   = (i3+((searchSize/8)+(blockSize/2)))>frame11.rows()?frame11.rows():(i3+((searchSize/8)+(blockSize/2)));
//									endL   = (j3+((searchSize/8)+(blockSize/2)))>frame11.cols()?frame11.cols():(j3+((searchSize/8)+(blockSize/2)));
//									for(int k=startK;k<endK;k++){
//										for(int l=startL;l<endL;l++){
//											if(k+blockSize<endK && l+blockSize<endL) {
//												Mat destinationBlock = frame11.submat(k, k+blockSize, l, l+blockSize);
//												Mat currDiffBlock = new Mat(blockSize, blockSize, frame10.type());
//												Core.absdiff(destinationBlock, sourceBlock, currDiffBlock);
//												Scalar SAD = Core.sumElems(currDiffBlock);
//												if(SAD.val[0] < minSAD.val[0]){
//													minSAD = SAD;
//													indices[0] = k;
//													indices[1] = l;
//												} else {
//													continue;
//												}
//											} else {
//												break;
//											}						
//										}
//									}
//									mV.put(i, j, indices[0]-i, indices[1]-j);
//									Point pt2 = new Point(j+mV.get(i, j)[1], i+mV.get(i, j)[0]);
//									
//									//Drawing Arrow
//									Core.line(frame10NewMV, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//									angle = Math.atan2( (double)( pt1.y - pt2.y), (double)( pt1.x - pt2.x) ); 
//									hypotenuse = Math.sqrt( Math.pow((pt1.y - pt2.y), 2) + Math.pow((pt1.x - pt2.x), 2));
//									hypotenuse = hypotenuse/4;
//									pt1By4.x = (int) (pt2.x+hypotenuse*Math.cos(angle+Math.PI/4));
//									pt1By4.y = (int) (pt2.y+hypotenuse*Math.sin(angle+Math.PI/4));
//									Core.line(frame10NewMV, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//									pt1By4.x = (int) (pt2.x+hypotenuse*Math.cos(angle-Math.PI/4));
//									pt1By4.y = (int) (pt2.y+hypotenuse*Math.sin(angle-Math.PI/4));
//									Core.line(frame10NewMV, pt1, pt2, new Scalar(0,0,255), 1,8,0);
//								}
//							}
//						}
//					}
//				}
//			}
//
//			PrintStream ps = new PrintStream(new FileOutputStream(new File("mVBy4.txt")));
//			ps.println("mVBy4 :" + mVBy4.dump());
//			Highgui.imwrite("frame10By4.jpg",frame10By4);
//			ps.close();
//
//			PrintStream ps2 = new PrintStream(new FileOutputStream(new File("mVBy2.txt")));
//			ps2.println("mVBy2"+mVBy2.dump());
//			Highgui.imwrite("frame10By2.jpg",frame10By2);
//			ps2.close();
//
//			PrintStream ps3 = new PrintStream(new FileOutputStream(new File("mV.txt")));
//			ps3.println("V0"+mV.dump());
//			Highgui.imwrite("frame10NewMV.jpg",frame10NewMV);
//			ps3.close();
//
//			//calculating end point error
//			double avgMVError = 0;
//			Mat endPointError = new Mat(frame10.rows(), frame10.cols(), frame10.type());
//			for (int i=0; i<frame10.rows(); ++i) {
//				for (int j=0; j<frame10.cols(); ++j) {
//					double[] temp = new double[3];
//					temp[0] = frame10.get(i, j)[0] - frame11.get(i+(int)mV.get(i, j)[0], j+(int)mV.get(i, j)[1])[0];
//					temp[1] = frame10.get(i, j)[1] - frame11.get(i+(int)mV.get(i, j)[0], j+(int)mV.get(i, j)[1])[1];
//					temp[2] = frame10.get(i, j)[2] - frame11.get(i+(int)mV.get(i, j)[0], j+(int)mV.get(i, j)[1])[2];
//					endPointError.put(i, j, temp);
//					avgMVError = avgMVError+ endPointError.get(i, j)[0] + endPointError.get(i, j)[1] + endPointError.get(i, j)[2];
//				}
//			}
//			avgMVError = avgMVError/(3*frame10.rows()*frame10.cols());
//			PrintStream ps4 = new PrintStream(new FileOutputStream(new File("endPointError.txt")));
//			ps4.println("ePR"+endPointError.dump());
//			Highgui.imwrite("endPointError.jpg", endPointError);
//			ps4.close();
//
//			System.out.println(avgMVError);	
//		} catch (Exception e) {
//			System.out.println("Error: " + e.getMessage());
//		}
//		long endTime = System.nanoTime();
//		System.out.println("Took "+(endTime - startTime) + " ns");
//	}
//}
