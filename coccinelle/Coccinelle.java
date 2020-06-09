
/**
 * Classe coccinelle comprenant la grille des pucerons et contenant une fonction main calculant le chemin optimal et la grille des scores (nombre cumulé de pucerons)
 */
public class Coccinelle
{
    static int[][] grille;
    static int[][] M;
    static int[] cheminMax;
    static int scoreMax;

    /**
     * Calcule et stocke M[0:l][0:c] de terme général M[l][c] = m(l,c), où m(l,c) est le score maximal d'un chemin allant de la case (0,0) à la case (l,c).
     * Calcule et stocke cheminMax, le chemin maximisant le score, car on en aura besoin dans le Seam carving.
     */
    public static void calculerM()
    {
        M = new int[grille.length][grille[0].length];
        int[][] grilleValeursMax = new int[grille.length][grille[0].length];

        // We fill the M array
        // we go through the grid line by line ...
        for (int l = 0; l < grille.length; l++)
        {
            // ... and column by column 
            for (int c = 0; c < grille[0].length; c++)
            {
                // if we're on the first line M[l][c] will be the same as grille[l][c]
                if(l == 0)
                {
                    M[l][c] = grille[l][c];
                    grilleValeursMax[l][c] = 0;
                }
                else
                {
                    //we check the 3 possible values of the previous line
                    // 1) we create a temporary int to store the biggest value on the previous line and we give it 
                    // the value right under the one we're in now
                    int maxLignePrecedente = M[l-1][c];
                    int colonneMax = c;
                    // 2) we check if the value at the right and at the left (next line) are bigger (BUT careful if we're 
                    // at the first column or the last

                    // if it's not the first column
                    if (c > 0)
                    {
                        // we compare the one below with the below left
                        // l - 1 because it's previous line, c - 1 because it's the column on the left
                        if (M[l-1][c-1] > maxLignePrecedente)
                        {
                            // if it's bigger we give it the value and we assume that this is now the column of the biggest figure
                            maxLignePrecedente = M[l-1][c-1];
                            colonneMax = c-1;
                        }
                    }

                    // if it's not the last column
                    if (c < grille[0].length - 1)
                    {
                        // we compare the one below with the below right
                        if (M[l-1][c+1] > maxLignePrecedente)
                        {
                            // if it's bigger we give it the value and we assume that this is now the column of the biggest figure
                            maxLignePrecedente = M[l-1][c+1];
                            colonneMax = c+1;
                        }
                    }

                    // we assign M[l,c] value and we store in grilleValeursMax the column of the biggest value below us
                    grilleValeursMax[l][c] = colonneMax;
                    M[l][c] = grille[l][c] + maxLignePrecedente;
                }
            }
        }

        // Now we're looking for the biggest number on the last line to see how much it ate
        int indexMaxLigneCourante = 0;
        int tempMax = M[grille.length-1][0];

        // we go through the last line of M
        for (int indexMaxLignePrecedente = 0; indexMaxLignePrecedente < M[0].length; indexMaxLignePrecedente++)
        {
            // if the value is bigger we replace it and store its index
            if(tempMax < M[grille.length-1][indexMaxLignePrecedente])
            {
                tempMax = M[grille.length - 1][indexMaxLignePrecedente];
                indexMaxLigneCourante = indexMaxLignePrecedente;
            }
        }

        // We want to check the path from where it comes thanks to our grilleValeursMax array that stored the
        // indexes of our movement in each line
        cheminMax = new int[grille.length];

        for (int i = grilleValeursMax.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < grilleValeursMax[0].length; j++)
            {
                // if it matches the column we're in
                if (j == indexMaxLigneCourante)
                {
                    // we add the cell to the path
                    cheminMax[i] = j;
                    indexMaxLigneCourante = grilleValeursMax[i][j];
                    // we break because we have found the cell we wanted on that line so we move on
                    break;
                }
            }
        }
    }

    public static void calculerScoreMax()
    {
        int[] derniereLigne = M[M.length - 1];
        int max = derniereLigne[0];
        for (int i = 0; i < derniereLigne.length; i++)
        {
            if(derniereLigne[i] > max)
            {
                max = derniereLigne[i];
            }
        }
        scoreMax = max;
    }

    public static void afficherGrille()
    {
        for (int i = grille.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < grille[0].length; j++)
            {
                System.out.print(grille[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void afficherMatrice()
    {
        for (int i = M.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < M[0].length; j++)
            {
                System.out.print(M[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void afficherCaseCheminMax(int index)
    {
        System.out.print("(" + index + "," + cheminMax[index] + ")");
    }

    public static void afficherCaseAtterrissage()
    {
        afficherCaseCheminMax(0);
    }

    public static void afficherCaseInterview()
    {
        afficherCaseCheminMax(cheminMax.length - 1);
    }

    public static void afficherCheminMax()
    {
        for (int i = 0; i < cheminMax.length; i++)
        {
            afficherCaseCheminMax(i);
        }
    }

    public static void main(String[] args)
    {
        int[][] defaultGrid = new int[][]{ 
                { 2, 4, 3, 9, 6 }, 
                { 1, 10, 15, 1, 2 }, 
                { 2, 4, 11, 26, 66 }, 
                { 36, 34, 1, 13, 30 }, 
                { 46, 2, 8, 7, 15 }, 
                { 89, 27, 10, 12, 3 }, 
                { 1, 72, 3, 6, 6 }, 
                { 3, 1, 2, 4, 5 } 
            };

        grille = defaultGrid;

        calculerM();
        calculerScoreMax();

        System.out.print("Grille des pucerons :");
        System.out.println();

        afficherGrille();

        System.out.println();
        System.out.print("Tableau M[L][C] de terme général M[l][c] = m(l,c) :");
        System.out.println();

        afficherMatrice();

        System.out.println();

        System.out.print("La coccinelle a mangé " + scoreMax + " pucerons.");
        System.out.println();

        System.out.print("Elle a suivi le chemin ");
        afficherCheminMax();
        System.out.println();

        System.out.print("Case d'atterrissage = ");
        afficherCaseAtterrissage();
        System.out.println();

        System.out.print("Case de l'interview = ");
        afficherCaseInterview();
        System.out.println();

    }

}

