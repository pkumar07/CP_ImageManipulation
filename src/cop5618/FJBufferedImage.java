package cop5618;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

import javax.imageio.ImageIO;

//import org.junit.BeforeClass;

public class FJBufferedImage extends BufferedImage {
    
	
   /**Constructors*/
        static ForkJoinPool pool= new ForkJoinPool();
    
	public FJBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public FJBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public FJBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
			Hashtable<?, ?> properties) {
		super(cm, raster, isRasterPremultiplied, properties);
	}
	

	/**
	 * Creates a new FJBufferedImage with the same fields as source.
	 * @param source
	 * @return
	 */
	public static FJBufferedImage BufferedImageToFJBufferedImage(BufferedImage source){
	       Hashtable<String,Object> properties=null; 
	       String[] propertyNames = source.getPropertyNames();
	       if (propertyNames != null) {
	    	   properties = new Hashtable<String,Object>();
	    	   for (String name: propertyNames){properties.put(name, source.getProperty(name));}
	    	   }
	 	   return new FJBufferedImage(source.getColorModel(), source.getRaster(), source.isAlphaPremultiplied(), properties);		
	}
        
        private class GetTask extends RecursiveAction {
            int xStart, yStart, w, h, rgbArray[], offset, scansize, nthreads;
            GetTask(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize, int nthreads){
                this.xStart= xStart;
                this.yStart=yStart;
                this.w= w;
                this.h=h;
                this.rgbArray=rgbArray;
                this.offset=offset;
                this.scansize=scansize;
                this.nthreads=nthreads;    
            }
            
            @Override
            protected void compute(){
                if(nthreads <= 1){
                    FJBufferedImage.super.getRGB(xStart,yStart, w, h, rgbArray,offset,scansize);
                }
                else{
                    invokeAll(
                        new GetTask(xStart, yStart, w , h/2, rgbArray, offset, scansize, nthreads/2 ),
                        new GetTask(xStart, yStart+ (h/2) , w , h-(h/2), rgbArray, offset + (scansize * (h/2)) ,scansize,nthreads-nthreads/2 )
                    ); 
                }
            }
        }
  
        private class SetTask extends RecursiveAction {
            int xStart, yStart, w, h, rgbArray[], offset, scansize, nthreads;
            SetTask(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize, int nthreads){
                this.xStart= xStart;
                this.yStart=yStart;
                this.w= w;
                this.h=h;
                this.rgbArray=rgbArray;
                this.offset=offset;
                this.scansize=scansize;
                this.nthreads=nthreads;   
            }
            
            @Override
            protected void compute(){
                if(nthreads <= 1){
                    FJBufferedImage.super.setRGB(xStart, yStart, w, h, rgbArray,offset,scansize);
                }
                else{
                    invokeAll(
                            new SetTask(xStart, yStart, w, (h/2), rgbArray, offset,scansize, nthreads/2 ),
                            new SetTask(xStart, yStart + (h/2) , w , h-(h/2), rgbArray, offset + ((h/2) * scansize) ,scansize, nthreads-nthreads/2 )
                    );
                }
            }
        }
                
	
	@Override
	public void setRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
        /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
            pool.invoke(new SetTask( xStart, yStart,w,h,rgbArray, offset, scansize, pool.getParallelism() * 8));
	}
	

	@Override
	public int[] getRGB(int xStart, int yStart, int w, int h, int[] rgbArray, int offset, int scansize){
	       /****IMPLEMENT THIS METHOD USING PARALLEL DIVIDE AND CONQUER*****/
               
            pool.invoke(new GetTask( xStart, yStart,w,h,rgbArray, offset, scansize, pool.getParallelism() * 8));
            return rgbArray; 
	}
	
}
