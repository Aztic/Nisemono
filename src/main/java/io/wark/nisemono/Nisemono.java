package io.wark.nisemono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Nisemono {
    private static final String[] validExtensions = {"png","jpg","jpeg","bmp"};

    public static void main(String[] args){
        File[] currentFolder = new File(".").listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        int option;
        System.out.println("¿Cuál carpeta? (elige un número)");
        for(int i=0;i<currentFolder.length;i++){
            System.out.println(String.format("%d - %s",i+1,currentFolder[i].getName()));
        }
        do{
            Scanner inn = new Scanner(System.in);
            option = inn.nextInt();
        }while(option-1 >= currentFolder.length || option < 0);

        try{
            File watermark = new File("watermark.png");
            //File[] dirs = new File(folder).listFiles(); //List all files
            Queue<File> pendingDirectories = new LinkedList<>();
            //Let's do a BFS
            pendingDirectories.add(currentFolder[option-1]);
            try{
                do{
                    File[] folderFiles= pendingDirectories.remove().listFiles();
                    for(File f: folderFiles){
                        //If it's a directory, add it to te queue
                        if(f.isDirectory()){
                            pendingDirectories.add(f); //Let's add it to the queue
                        }
                        else{
                            String absolutePath = f.getAbsolutePath();
                            String[] splittedFilename = absolutePath.split("\\.");
                            String extension = splittedFilename[splittedFilename.length -1].toLowerCase();
                            //Check if it's a valid file
                            if(Arrays.asList(validExtensions).contains(extension) && splittedFilename.length > 1){
                                splittedFilename[splittedFilename.length -2] += "-watermark";
                                absolutePath = String.join(".",splittedFilename);
                                BufferedImage watermarked = addWatermarkToImage(f,watermark,0.5f);
                                File output = new File(absolutePath);
                                ImageIO.write(watermarked,extension,output);
                            }
                        }

                    }
                }while(pendingDirectories.size() > 0);

            }catch (NullPointerException n){
                System.out.println(String.format("%s happened",n.toString()));
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
    public static BufferedImage changeOpacity(BufferedImage original){
        float alpha = 0.2f;
        BufferedImage another = new BufferedImage(original.getWidth(),original.getHeight(),original.getType());
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);
        Graphics2D g = another.createGraphics();
        g.setComposite(ac);
        g.drawImage(original,0,0,original.getWidth(),original.getHeight(),null);
        g.dispose();
        return another;
    }

    /*
    private static BufferedImage resize(BufferedImage img, int height, int width){
        Image tmp = img.getScaledInstance(width,height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(tmp,0,0,null);
        g.dispose();
        return resized;
    }*/

    private static BufferedImage addWatermarkToImage(File ori, File overl,float opacity) throws IOException{
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

        //Get the middle of the original image
        int centerX = (original.getWidth()/2) - overlap.getWidth()/2;
        int centerY = (original.getHeight()/2) - overlap.getHeight()/2;

        //Draw the watermark image
        g.drawImage(overlap,centerX,centerY,null);
        g.dispose();

        return watermarked;
    }


}
