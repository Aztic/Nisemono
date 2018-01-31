package io.wark.nisemono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;


public class Nisemono {
    private static final String[] validExtensions = {"png","jpg","jpeg","bmp"};

    /** Return the coordinates of the desired position **/
    private enum Position{
        CENTER,
        CENTER_LEFT,
        CENTER_RIGHT,
        TOP_CENTER,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;

        public int[] calculatePosition(int width1,int height1,int width2,int height2){
            int[] tuple = new int[2];
            switch (this){
                case CENTER:
                    tuple[0] = (width1/2) - width2/2;
                    tuple[1] = (height1/2) - height2/2;
                    break;
                case CENTER_LEFT:
                    tuple[0] = 0;
                    tuple[1] = (height1/2) - height2/2;
                    break;
                case CENTER_RIGHT:
                    tuple[0] = width1 - width2;
                    tuple[1] = (height1/2) - height2/2;
                    break;
                case TOP_CENTER:
                    tuple[0] = (width1/2) - width2/2;
                    tuple[1] = 0;
                    break;
                case TOP_LEFT:
                    tuple[0] = 0;
                    tuple[1] = 0;
                    break;
                case TOP_RIGHT:
                    tuple[0] = width1 - width2;
                    tuple[1] = 0;
                    break;
                case BOTTOM_CENTER:
                    tuple[0] = (width1/2) - width2/2;
                    tuple[1] = height1 - height2;
                    break;
                case BOTTOM_LEFT:
                    tuple[0] = 0;
                    tuple[1] = height1 - height2;
                    break;
                case BOTTOM_RIGHT:
                    tuple[0] = width1 - width2;
                    tuple[1] = height1 - height2;
                    break;
                default:
                    tuple[0] = 0;
                    tuple[1] = 0;
            }
            return tuple;
        }
    };

    public static void main(String[] args){
        File[] currentFolder = new File(".").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        int option;
        System.out.println("Which folder? (choose a number)");
        for(int i=0;i<currentFolder.length;i++){
            System.out.println(String.format("%d - %s",i+1,currentFolder[i].getName()));
        }
        Scanner inn = new Scanner(System.in);
        do{
            option = inn.nextInt();
        }while(option-1 >= currentFolder.length || option < 0);
        System.out.println("Which position?");
        int i = 0;
        for(Position p: Position.values()){
            System.out.println(String.format("%d - %s",i,p));
            i++;
        }
        do{
           i = inn.nextInt();
        }while(i < 0 || i>=Position.values().length);
        Position p = Position.values()[i];

        try{
            File watermark = new File("watermark.png");
            Files.walk(Paths.get(currentFolder[option-1].getAbsolutePath()))
                    .filter(Files::isRegularFile)
                    .forEach(file ->{
                        String absolutePath = file.toAbsolutePath().toString();
                        String[] splittedFilename = absolutePath.split("\\.");
                        String extension = splittedFilename[splittedFilename.length -1].toLowerCase();
                        if(Arrays.asList(validExtensions).contains(extension) && splittedFilename.length > 1 && absolutePath.split("-watermark").length == 1){
                            try {
                                splittedFilename[splittedFilename.length -2] += "-watermark";
                                absolutePath = String.join(".",splittedFilename);
                                BufferedImage watermarked = addWatermarkToImage(file.toFile(),watermark,0.5f,p);
                                File output = new File(absolutePath);
                                ImageIO.write(watermarked, extension, output);
                                System.out.println(String.format("Saved %s.%s",splittedFilename[splittedFilename.length -2],extension));
                            }catch (IOException io){
                                System.out.println(String.format("Cannot save image because %s",io.toString()));
                            }
                        }
                    });
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    /**
     * Resize the image
     * @param img
     * @param height
     * @param width
     * @return
     */
    private static BufferedImage resize(BufferedImage img, int height, int width){
        Image tmp = img.getScaledInstance(width,height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(tmp,0,0,null);
        g.dispose();
        return resized;
    }

    /**
     * Add the desired watermark to the image, with desired opacity and return the BufferedImage element
     * @param ori
     * @param overl
     * @param opacity
     * @param p
     * @return
     * @throws IOException
     */
    private static BufferedImage addWatermarkToImage(File ori, File overl,float opacity,Position p) throws IOException{
        //Pass the files to buffered image
        BufferedImage original = ImageIO.read(ori);
        BufferedImage overlap = ImageIO.read(overl);

        //Create a watermarked image with the original and the overlay
        BufferedImage watermarked = new BufferedImage(original.getWidth(),original.getHeight(),original.getType());
        Graphics2D g = watermarked.createGraphics();
        //Draw the image
        g.drawImage(original,0,0,null); //Draw the original image
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity);
        //Next image will be with less opacity
        g.setComposite(alpha);
        int image1Width = original.getWidth()/6;
        int image1Height = original.getHeight()/6;
        int fixedWidth = overlap.getWidth();
        int fixedHeight = overlap.getHeight();

        while(fixedWidth > image1Width && fixedHeight > image1Height){
            fixedWidth /= 2;
            fixedHeight /= 2;
        }
        overlap = resize(overlap,fixedHeight,fixedWidth);
        int position[] = p.calculatePosition(original.getWidth(),original.getHeight(),overlap.getWidth(),overlap.getHeight());

        //Draw the watermark image
        g.drawImage(overlap,position[0],position[1],null);
        g.dispose();

        return watermarked;
    }


}
