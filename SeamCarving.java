import javax.imageio.ImageIO; 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Classe SeamCarving contenant une image à redimensionner et la hauteur et la largeur cibles.
 */
public class SeamCarving
{
    static BufferedImage image;
    static int largeurCible;
    static int hauteurCible;

    private static int[][] transposer(int[][] grille)
    {
        int largeurGrille = grille.length;
        int hauteurGrille = grille[0].length;
        int[][] transposee = new int[hauteurGrille][largeurGrille];

        for (int i = 0; i < hauteurGrille; i++)
        {
            for (int j = 0; j < largeurGrille; j++)
            {
                transposee[i][j] = grille[j][i];
            }
        }
        return transposee;
    }

    private static int extraireRouge(int rgb)
    {
        return (rgb & 0xff0000) >> 16;
    }

    private static int extraireVert(int rgb)
    {
        return (rgb & 0xff00) >> 8;
    }

    private static int extraireBleu(int rgb)
    {
        return rgb & 0xff;
    }

    /**
     * Calcule la valeur du gradient entre deux RGB (en norme 1). Renvoie une valeur comprise entre 0 et 255 x 3.
     */
    private static int calculerGradient(int rgb1, int rgb2)
    {
        return (Math.abs(extraireRouge(rgb1) - extraireRouge(rgb2)) + Math.abs(extraireVert(rgb1) - extraireVert(rgb2)) + Math.abs(extraireBleu(rgb1) - extraireBleu(rgb2)));
    }

    /**
     * Calcule l'énergie d'un pixel de l'image en position (i, j).
     * Elle correspond à la moyenne entre :
     *  - le gradient des RGB du pixel au-dessus et du pixel en dessous ;
     *  - le gradient des RGB du pixel à gauche et du pixel à droite.
     * Dans le cas où l'un de ces pixels n'existe pas (bord de l'image), on le remplace par le pixel en position courante.
     */
    private static int calculerEnergie(int i, int j)
    {
        int largeurImage = image.getWidth();
        int hauteurImage = image.getHeight();

        int rgbGauche = (i == 0) ? image.getRGB(i, j) : image.getRGB(i - 1, j);
        int rgbDroite = (i == largeurImage - 1) ? image.getRGB(i, j) : image.getRGB(i + 1, j);
        int rgbHaut = (j == 0) ? image.getRGB(i, j) : image.getRGB(i, j - 1);
        int rgbBas = (j == hauteurImage - 1) ? image.getRGB(i, j) : image.getRGB(i, j + 1);

        return (calculerGradient(rgbGauche, rgbDroite) + calculerGradient(rgbHaut, rgbBas)) / 2;
    }

    /**
     * Calcule la grille des scores d'énergie à maximiser.
     */
    private static int[][] calculerGrilleNegEnergie()
    {
        int largeurImage = image.getWidth();
        int hauteurImage = image.getHeight();
        int[][] grilleEnergie = new int[largeurImage][hauteurImage];

        for (int i = 0; i < largeurImage; i++)
        {
            for (int j = 0; j < hauteurImage; j++)
            {
                // negEnergie = energieMax - energie car Coccinelle cherche le chemin de score maximal alors qu'on veut minimiser l'énergie
                // Ici, energieMax vaut 255 * 3
                grilleEnergie[i][j] = 255 * 3 - calculerEnergie(i, j);
            }
        }

        return grilleEnergie;
    }

    /**
     * Renvoie l'image privée d'un filon vertical.
     * 
     * @param image 
     * @param filonVertical tableau contenant les numéros des colonnes des pixels du filon de haut en bas
     */
    private static BufferedImage enleverFilonVertical(int[] filonVertical)
    {
        int largeurImage = image.getWidth();
        int hauteurImage = image.getHeight();
        BufferedImage nouvelleImage = image;

        for (int j = 0; j < hauteurImage; j++)
        {
            // On décale tous les pixels à droite du filon d'un cran vers la gauche
            for (int i = filonVertical[j]; i < largeurImage - 1; i++)
            {
                nouvelleImage.setRGB(i, j, nouvelleImage.getRGB(i + 1, j));
            }
        }

        return nouvelleImage.getSubimage(0, 0, largeurImage - 1, hauteurImage);
    }

    /**
     * Renvoie l'image privée d'un filon horizontal.
     * 
     * @param image 
     * @param filonHorizontal tableau contenant les numéros des lignes des pixels du filon de gauche à droite
     */
    private static BufferedImage enleverFilonHorizontal(int[] filonHorizontal)
    {
        int largeurImage = image.getWidth();
        int hauteurImage = image.getHeight();
        BufferedImage nouvelleImage = image;

        for (int i = 0; i < largeurImage; i++)
        {
            // On décale tous les pixels en dessous du filon d'un cran vers le haut
            for (int j = filonHorizontal[i]; j < hauteurImage - 1; j++)
            {
                nouvelleImage.setRGB(i, j, nouvelleImage.getRGB(i, j + 1));
            }
        }

        return nouvelleImage.getSubimage(0, 0, largeurImage, hauteurImage - 1);
    }

    static void redimensionnerImage()
    {
        image = image;

        while (image.getWidth() > largeurCible | image.getHeight() > hauteurCible)
        {
            // L'image doit etre réduite
            int[][] energyGrid = calculerGrilleNegEnergie();

            // Score par défaut, inférieur au score calculable minimum
            float averageMaxScoreV = -1;
            float averageMaxScoreH = -1;

            int[] cheminMaxV = new int[image.getHeight()];
            int[] cheminMaxH = new int[image.getWidth()];

            if (image.getWidth() > largeurCible)
            {
                // L'image est trop large
                // On appelle Coccinelle pour déterminer le filon vertical d'énergie minimale (score maximal)
                // On utilise transposer ici car la recherche d'un filon vertical dans l'image correspond à la recherche d'un chemin
                // horizontal pour Coccinelle de par la permutation de l'ordre des dimensions. On transpose donc l'image pour se
                // ramener au cas de Coccinelle.
                Coccinelle.grille = transposer(energyGrid);
                Coccinelle.calculerM();
                cheminMaxV = Coccinelle.cheminMax;
                Coccinelle.calculerScoreMax();
                averageMaxScoreV = (float)Coccinelle.scoreMax / image.getHeight(); // moyenne des scores par pixel
            }

            if (image.getHeight() > hauteurCible)
            {
                // L'image est trop haute
                // On appelle Coccinelle pour déterminer le filon horizontal d'énergie minimale (score maximal)
                Coccinelle.grille = energyGrid;
                Coccinelle.calculerM();
                cheminMaxH = Coccinelle.cheminMax;
                Coccinelle.calculerScoreMax();
                averageMaxScoreH = (float)Coccinelle.scoreMax / image.getWidth(); // moyenne des scores par pixel
            }

            // On détermine si on doit enlever un filon vertical ou horizontal
            if (averageMaxScoreV > averageMaxScoreH)
            {
                // On enlève un filon vertical
                image = enleverFilonVertical(cheminMaxV);
            }
            else
            {
                // On enlève un filon horizontal
                image = enleverFilonHorizontal(cheminMaxH);
            }
        }
    }

    public static void main(String[] args) throws IOException
    {   
        if (args.length != 3)
        {
            // L'utilisateur n'a pas entré le bon nombre d'arguments
            System.out.println("Appelez SeamCarver avec les arguments suivants : nomImage pourcentageReductionHauteur pourcentageReductionLargeur");
            return;
        }

        String imageName = args[0];
        int heightReductionPercentage = Integer.parseInt(args[1]); // parseInt gère le typage incorrect
        int widthReductionPercentage = Integer.parseInt(args[2]);

        if (heightReductionPercentage < 0 | heightReductionPercentage >= 100 | widthReductionPercentage < 0 | widthReductionPercentage >= 100)
        {
            // L'utilisateur a entré des pourcentages de redimensionnement hors limites
            System.out.println("Les pourcentages de réduction doivent etre compris entre 0 et 99.");
            return;
        }

        try
        {
            image = ImageIO.read(new File(imageName));
        }
        catch (IOException e)
        {
            System.out.println("Image " + imageName + " non trouvée.");
            throw(e);
        }

        // On calcule et stocke les dimensions cibles de l'image en nombres de pixels (entiers)
        largeurCible = image.getWidth() * (100 - widthReductionPercentage) / 100;
        hauteurCible = image.getHeight() * (100 - heightReductionPercentage) / 100;

        System.out.println("Dimensions de la nouvelle image (hauteur x largeur): " + hauteurCible + " x " + largeurCible);

        redimensionnerImage();

        String[] imageNameArray = imageName.split("\\.");

        if (imageNameArray.length != 2)
        {
            System.out.println("Le nom du fichier doit respecter le format 'nom.extension'.");
            return;
        }

        String outputFileName = imageNameArray[0] + "_resized_" + heightReductionPercentage + "_" + widthReductionPercentage + "." + imageNameArray[1];
        File outputfile = new File(outputFileName);
        try
        {
            // Enregistrement de l'image
            ImageIO.write(image, imageNameArray[1], outputfile);
            System.out.println("Le fichier " + outputFileName + " a été généré !");
        }
        catch (IOException e)
        {
            System.out.println(e);
            throw(e);
        }
    }
}
