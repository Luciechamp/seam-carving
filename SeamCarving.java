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
    private BufferedImage image;
    private int targetWidth;
    private int targetHeight;

    /**
     * Constructeur de la classe SeamCarving.
     * @param imageName le nom de l'image
     * @param heightReductionPercentage le pourcentage de réduction de la hauteur 
     * @param widthReductionPercentage le pourcentage de réduction de la largeur
     */
    public SeamCarving(String imageName, int heightReductionPercentage, int widthReductionPercentage) throws IOException
    {
        try
        {
            this.image = ImageIO.read(new File(imageName));
            // On calcule et stocke les dimensions cibles de l'image en nombres de pixels (entiers)
            this.targetWidth = this.image.getWidth() * (100 - widthReductionPercentage) / 100;
            this.targetHeight = this.image.getHeight() * (100 - heightReductionPercentage) / 100;
        }
        catch (IOException e)
        {
            System.out.println("Image " + imageName + " not found.");
            throw(e);
        }
    }

    public int getTargetWidth()
    {
        return this.targetWidth;
    }

    public int getTargetHeight()
    {
        return this.targetHeight;
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

    /**
     * Calcule la valeur du gradient entre deux RGB (en norme 1). Renvoie une valeur comprise entre 0 et 255 x 3.
     */
    private static int computeGradient(int rgb1, int rgb2)
    {
        return (Math.abs(getRed(rgb1) - getRed(rgb2)) + Math.abs(getGreen(rgb1) - getGreen(rgb2)) + Math.abs(getBlue(rgb1) - getBlue(rgb2)));
    }

    /**
     * Calcule l'énergie d'un pixel de l'image en position (i, j).
     * Elle correspond à la moyenne entre :
     *  - le gradient des RGB du pixel au-dessus et du pixel en dessous ;
     *  - le gradient des RGB du pixel à gauche et du pixel à droite.
     * Dans le cas où l'un de ces pixels n'existe pas (bord de l'image), on le remplace par le pixel en position courante.
     */
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

    /**
     * Calcule la grille des scores d'énergie à maximiser.
     */
    private static int[][] computeNegEnergyGrid(BufferedImage image)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int[][] energyGrid = new int[imageWidth][imageHeight];

        for (int i = 0; i < imageWidth; i++)
        {
            for (int j = 0; j < imageHeight; j++)
            {
                // negEnergy = energyMax - energy car Coccinelle cherche le chemin de score maximal alors qu'on veut minimiser l'énergie
                // Ici, energyMax vaut 255 * 3
                energyGrid[i][j] = 255 * 3 - computeEnergy(image, i, j);
            }
        }

        return energyGrid;
    }

    /**
     * Renvoie l'image privée d'un filon vertical verticalSeam.
     * 
     * @param image 
     * @param verticalSeam tableau contenant les numéros des colonnes des pixels du filon de haut en bas
     */
    private static BufferedImage removeVerticalSeam(BufferedImage image, int[] verticalSeam)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        BufferedImage shiftedImage = image;

        for (int j = 0; j < imageHeight; j++)
        {
            // On décale tous les pixels à droite du filon d'un cran vers la gauche
            for (int i = verticalSeam[j]; i < imageWidth - 1; i++)
            {
                shiftedImage.setRGB(i, j, shiftedImage.getRGB(i + 1, j));
            }
        }

        return shiftedImage.getSubimage(0, 0, imageWidth - 1, imageHeight);
    }

    /**
     * Renvoie l'image privée d'un filon horizontal horizontalSeam.
     * 
     * @param image 
     * @param horizontalSeam tableau contenant les numéros des lignes des pixels du filon de gauche à droite
     */
    private static BufferedImage removeHorizontalSeam(BufferedImage image, int[] horizontalSeam)
    {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        BufferedImage shiftedImage = image;

        for (int i = 0; i < imageWidth; i++)
        {
            // On décale tous les pixels en dessous du filon d'un cran vers le haut
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
        
        while (resizedImage.getWidth() > this.targetWidth | resizedImage.getHeight() > this.targetHeight)
        {
            // L'image doit etre réduite
            int[][] energyGrid = computeNegEnergyGrid(resizedImage);
            CoccinelleResult coccinelleResultV = new CoccinelleResult(null, null);
            CoccinelleResult coccinelleResultH = new CoccinelleResult(null, null);
            
            // Score par défaut, inférieur au score calculable minimum
            float averageMaxScoreV = -1;
            float averageMaxScoreH = -1;

            if (resizedImage.getWidth() > this.targetWidth)
            {
                // L'image est trop large
                // On appelle Coccinelle pour déterminer le filon vertical d'énergie minimale (score maximal)
                // On utilise transpose ici car la recherche d'un filon vertical dans l'image correspond à la recherche d'un chemin
                // horizontal pour Coccinelle de par la permutation de l'ordre des dimensions. On transpose donc l'image pour se
                // ramener au cas de Coccinelle.
                Coccinelle coccinelleV = new Coccinelle(transpose(energyGrid));
                coccinelleResultV = coccinelleV.computeMaxGrid();
                averageMaxScoreV = (float)coccinelleResultV.getMaxScore() / resizedImage.getHeight(); // moyenne des scores par pixel
            }

            if (resizedImage.getHeight() > this.targetHeight)
            {
                // L'image est trop haute
                // On appelle Coccinelle pour déterminer le filon horizontal d'énergie minimale (score maximal)
                Coccinelle coccinelleH = new Coccinelle(energyGrid);
                coccinelleResultH = coccinelleH.computeMaxGrid();
                averageMaxScoreH = (float)coccinelleResultH.getMaxScore() / resizedImage.getWidth(); // moyenne des scores par pixel
            }

            // On détermine si on doit enlever un filon vertical ou horizontal
            if (averageMaxScoreV > averageMaxScoreH)
            {
                // On enlève un filon vertical
                int[] verticalSeam = coccinelleResultV.getPath();
                resizedImage = removeVerticalSeam(resizedImage, verticalSeam);
            }
            else
            {
                // On enlève un filon horizontal
                int[] horizontalSeam = coccinelleResultH.getPath();
                resizedImage = removeHorizontalSeam(resizedImage, horizontalSeam);
            }
        }

        return resizedImage;
    }

    public static void main(String[] args) throws IOException
    {   
        if (args.length != 3)
        {
            // L'utilisateur n'a pas entré le bon nombre d'arguments
            System.out.println("Please call SeamCarver with the arguments: imageName heightReductionPercentage widthReductionPercentage");
            return;
        }

        String imageName = args[0];
        int heightReductionPercentage = Integer.parseInt(args[1]); // parseInt gère le typage incorrect
        int widthReductionPercentage = Integer.parseInt(args[2]);

        if (heightReductionPercentage < 0 | heightReductionPercentage >= 100 | widthReductionPercentage < 0 | widthReductionPercentage >= 100)
        {
            // L'utilisateur a entré des pourcentages de redimensionnement hors limites
            System.out.println("Please use reduction percentages between 0 and 99.");
            return;
        }

        SeamCarving seamCarving = new SeamCarving(imageName, heightReductionPercentage, widthReductionPercentage);

        System.out.println("Dimensions of the new file (height x width): " + seamCarving.getTargetHeight() + " x " + seamCarving.getTargetWidth());

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
            // Enregistrement de l'image
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
