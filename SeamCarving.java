import javax.imageio.ImageIO; 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import coccinelle.Coccinelle;
import coccinelle.CoccinelleResult;

/**
 * Class SeamCarving contenant une image à redimensionner et la hauteur et la largeur cibles.
 */
public class SeamCarving
{
    // instance variables - replace the example below with your own
    private BufferedImage image;
    private int width;
    private int height;

    /**
     * Constructor for objects of class SeamCarving
     */
    public SeamCarving(String imageName, int heightReductionPercentage, int widthReductionPercentage) throws IOException
    {
        try
        {
            this.image = ImageIO.read(new File(imageName));
            // We want to manipulate integers (number of rows and columns)
            this.width = this.image.getWidth() * (100 - widthReductionPercentage) / 100;
            this.height = this.image.getHeight() * (100 - heightReductionPercentage) / 100;
        }
        catch (IOException e)
        {
            System.out.println("Image " + imageName + " not found.");
            throw(e);
        }
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    private static int[][] transpose(int[][] grid)
    {
        int gridWidth = grid.length;
        int gridHeight = grid[0].length;
        int[][] res = new int[gridHeight][gridWidth];

        for (int i = 0; i < gridHeight; i++)
        {
            for (int j = 0; j < gridWidth; j++)
            {
                res[i][j] = grid[j][i];
            }
        }
        return res;
    }

    private static int getRed(int rgb)
    {
        return (rgb & 0xff0000) >> 16;
    }

    private static int getGreen(int rgb)
    {
        return (rgb & 0xff00) >> 8;
    }

    private static int getBlue(int rgb)
    {
        return rgb & 0xff;
    }

    private static int computeGradient(int rgb1, int rgb2)
    {
        return (Math.abs(getRed(rgb1) - getRed(rgb2)) + Math.abs(getGreen(rgb1) - getGreen(rgb2)) + Math.abs(getBlue(rgb1) - getBlue(rgb2)));
    }

    private static int computeEnergy(BufferedImage image, int i, int j)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        int leftRGB = (i == 0) ? image.getRGB(i, j) : image.getRGB(i - 1, j);
        int rightRGB = (i == imageWidth - 1) ? image.getRGB(i, j) : image.getRGB(i + 1, j);
        int topRGB = (j == 0) ? image.getRGB(i, j) : image.getRGB(i, j - 1);
        int bottomRGB = (j == imageHeight - 1) ? image.getRGB(i, j) : image.getRGB(i, j + 1);

        return (computeGradient(leftRGB, rightRGB) + computeGradient(topRGB, bottomRGB)) / 2;
    }

    private static int[][] computeNegEnergyGrid(BufferedImage image)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int[][] energyGrid = new int[imageWidth][imageHeight];

        for (int i = 0; i < imageWidth; i++)
        {
            for (int j = 0; j < imageHeight; j++)
            {
                // negEnergy = energyMax - energy because Coccinelle finds the max value and we want to find the min one
                // energyMax is 255 * 3
                energyGrid[i][j] = 255 * 3 - computeEnergy(image, i, j);
            }
        }

        return energyGrid;
    }

    private static BufferedImage removeVerticalSeam(BufferedImage image, int[] verticalSeam)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        BufferedImage shiftedImage = image;

        for (int j = 0; j < imageHeight; j++)
        {
            for (int i = verticalSeam[j]; i < imageWidth - 1; i++)
            {
                shiftedImage.setRGB(i, j, shiftedImage.getRGB(i + 1, j));
            }
        }

        return shiftedImage.getSubimage(0, 0, imageWidth - 1, imageHeight);
    }

    private static BufferedImage removeHorizontalSeam(BufferedImage image, int[] horizontalSeam)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        BufferedImage shiftedImage = image;

        for (int i = 0; i < imageWidth; i++)
        {
            for (int j = horizontalSeam[i]; j < imageHeight - 1; j++)
            {
                shiftedImage.setRGB(i, j, shiftedImage.getRGB(i, j + 1));
            }
        }

        return shiftedImage.getSubimage(0, 0, imageWidth, imageHeight - 1);
    }

    public BufferedImage resizeImage()
    {
        BufferedImage resizedImage = this.image;
        int index = 0;
        while (resizedImage.getWidth() > this.width | resizedImage.getHeight() > this.height)
        {
            int[][] energyGrid = computeNegEnergyGrid(resizedImage);
            CoccinelleResult coccinelleResultV = new CoccinelleResult(null, null);
            CoccinelleResult coccinelleResultH = new CoccinelleResult(null, null);
            float averageMaxScoreV = -1;
            float averageMaxScoreH = -1;

            if (resizedImage.getWidth() > this.width)
            {
                Coccinelle coccinelleV = new Coccinelle(transpose(energyGrid));
                coccinelleResultV = coccinelleV.computeMaxGrid();
                averageMaxScoreV = (float)coccinelleResultV.getMaxScore() / resizedImage.getHeight();
            }

            if (resizedImage.getHeight() > this.height)
            {
                Coccinelle coccinelleH = new Coccinelle(energyGrid);
                coccinelleResultH = coccinelleH.computeMaxGrid();
                averageMaxScoreH = (float)coccinelleResultH.getMaxScore() / resizedImage.getWidth();
            }

            if (averageMaxScoreV > averageMaxScoreH)
            {
                int[] verticalSeam = coccinelleResultV.getPath();
                resizedImage = removeVerticalSeam(resizedImage, verticalSeam);
            }
            else
            {
                int[] horizontalSeam = coccinelleResultH.getPath();
                resizedImage = removeHorizontalSeam(resizedImage, horizontalSeam);
            }

            index++;
        }

        return resizedImage;
    }

    public static void main(String[] args) throws IOException
    {   
        // If the user does not call the function with the right number of arguments
        if (args.length != 3)
        {
            System.out.println("Please call SeamCarver with the arguments: imageName heightReductionPercentage widthReductionPercentage");
            return;
        }
        
        String imageName = args[0];
        int heightReductionPercentage = Integer.parseInt(args[1]); // Deals with incorrect typing
        int widthReductionPercentage = Integer.parseInt(args[2]);
        
        // If the user specifies out of bounds reduction percentage(s)
        if (heightReductionPercentage < 0 | heightReductionPercentage >= 100 | widthReductionPercentage < 0 | widthReductionPercentage >= 100)
        {
            System.out.println("Please use reduction percentages between 0 and 99.");
            return;
        }
        
        SeamCarving seamCarving = new SeamCarving(imageName, heightReductionPercentage, widthReductionPercentage);
        
        System.out.println("Dimensions of the new file (height x width): " + seamCarving.getHeight() + " x " + seamCarving.getWidth());

        BufferedImage outputImage = seamCarving.resizeImage();

        String[] imageNameArray = imageName.split("\\.");
        
        if (imageNameArray.length != 2)
        {
            System.out.println("The file name must respect the pattern 'name.extension'.");
            return;
        }
        
        String outputFileName = imageNameArray[0] + "_resized_" + heightReductionPercentage + "_" + widthReductionPercentage + "." + imageNameArray[1];
        File outputfile = new File(outputFileName);
        try
        {
            ImageIO.write(outputImage, imageNameArray[1], outputfile);
            System.out.println("File " + outputFileName + " successfully generated!");
        }
        catch (IOException e)
        {
            System.out.println(e);
            throw(e);
        }
    }
}
