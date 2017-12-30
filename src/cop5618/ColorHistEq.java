package cop5618;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColorHistEq {

    //Use these labels to instantiate you timers.  You will need 8 invocations of now()
	static String[] labels = { "getRGB", "convert to HSB", "create brightness map", "probability array",
			"parallel prefix", "equalize pixels", "setRGB" };
        
        final static int numBins=256;

	static Timer colorHistEq_serial(BufferedImage image, BufferedImage newImage) {
		Timer times = new Timer(labels);
		ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight(); 
                
                times.now();
                int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		
                times.now();
                float [][] objHSBpixels = Arrays.stream(sourcePixelArray)
                                   .mapToObj(pixel -> Color.RGBtoHSB(colorModel.getRed(pixel), 
                                                                     colorModel.getGreen(pixel), 
                                                                     colorModel.getBlue(pixel), new float[3]))
                                   .toArray(float[][] :: new);
                
                times.now();
                Map<Integer, Long> binvaluemap = Arrays.stream(objHSBpixels)
                                    .mapToInt(mapRow -> Math.min((int)(mapRow[2] * numBins),numBins-1) )
                                    .mapToObj(Integer::new)
                                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
               
                times.now();
                double[] probability = IntStream.range(0, numBins)
                                .map(index -> binvaluemap.containsKey(index)? binvaluemap.get(index).intValue() : 0)
                                .mapToDouble( b->b / (double)(w*h))	
                                .toArray();
            
               
                times.now();
                Arrays.parallelPrefix(probability, (x,y)->x+y);
                
                times.now();
                int[] finalArray = Arrays.stream(objHSBpixels)
                                        .mapToInt(
                                                  pixel -> Color.HSBtoRGB(pixel[0], pixel[1],
                                                 (float)probability[Math.min((int)(pixel[2]*numBins),numBins-1)] 
                                         ))
                                        .toArray();
          
                times.now();
                newImage.setRGB(0, 0, w,h,finalArray,0,w);
                times.now();
                return times;
	}



	static Timer colorHistEq_parallel(FJBufferedImage image, FJBufferedImage newImage) {
		Timer times = new Timer(labels);
		ColorModel colorModel = ColorModel.getRGBdefault();
		int w = image.getWidth();
		int h = image.getHeight();
                     
                times.now();
                int[] sourcePixelArray = image.getRGB(0, 0, w, h, new int[w * h], 0, w);
		
                times.now();
                float [][] objHSBpixels = Arrays.stream(sourcePixelArray)
                                   .parallel()
                                   .mapToObj(pixel -> Color.RGBtoHSB(colorModel.getRed(pixel), 
                                                                     colorModel.getGreen(pixel), 
                                                                     colorModel.getBlue(pixel), new float[3]))
                                   .toArray(float[][] :: new);
                
                times.now();
                Map<Integer, Long> binvaluemap = Arrays.stream(objHSBpixels)
                                    .parallel()
                                    .mapToInt(mapRow -> Math.min((int)(mapRow[2] * numBins),numBins-1) )
                                    .mapToObj(Integer::new)
                                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
               
                times.now();
                double[] probability = IntStream.range(0, numBins)
                                .parallel()
				.map(index -> binvaluemap.containsKey(index)? binvaluemap.get(index).intValue() : 0)
                                .mapToDouble( b->b / (double)(w*h))	
                                .toArray();

                             
                times.now();
                Arrays.parallelPrefix(probability, (x,y)->x+y);
                
                times.now();
                int[] finalArray = Arrays.stream(objHSBpixels)
                                    .parallel()        
                                    .mapToInt(pixel -> Color.HSBtoRGB(
                                            pixel[0], pixel[1],
                                            (float)probability[Math.min((int)(pixel[2]*numBins), numBins-1)]
                                    ))
                                    .toArray();
          
                times.now();
                newImage.setRGB(0, 0, w,h,finalArray,0,w);
                
                times.now();
                return times;
	}

}
