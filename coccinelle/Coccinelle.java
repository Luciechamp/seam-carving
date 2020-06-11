
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

        // On rempli la grille M
        // On rempli la grille ligne par ligne ...
        for (int l = 0; l < grille.length; l++)
        {
            // ... et colonne pas colonne
            for (int c = 0; c < grille[0].length; c++)
            {
                // si on est sur la première ligne M[l][c] sera identique que la grille[l][c]
                if(l == 0)
                {
                    M[l][c] = grille[l][c];
                    grilleValeursMax[l][c] = 0;
                }
                else
                {
                    // on vérifie les trois valeurs possible de la ligne précédente
                    // 1) on créé un int temporairement pour stocker la plus grande valeur de la ligne précédente et on la retourne
                    // la valeur juste en dessous de celle sur laquelle on se situe maintenant
                    int maxLignePrecedente = M[l-1][c];
                    int colonneMax = c;
                    // 2) On vérifie si les valeurs à gauche et à droite (ligne d'après) sont plus grande (mais attention si  
                    // on est sur la première ou dernière colonne

                    // si ce n'est pas la première colonne
                    if (c > 0)
                    {
                        // on compare la valeur de la case du dessous avec celle en dessous à gauche
                        // l - 1 parce que c'est la ligne précédente, c - 1 parceque c'est la colonne de gauche
                        if (M[l-1][c-1] > maxLignePrecedente)
                        {
                            // si c'est la plus grande on lui attribue la valeur et on assume que c'est maintenant la colonne avec la plus grande valeur
                            maxLignePrecedente = M[l-1][c-1];
                            colonneMax = c-1;
                        }
                    }

                    // si ce n'est pas la dernière colonne
                    if (c < grille[0].length - 1)
                    {
                        // on compare la valeur de la case  en dessous avec celle en dessous à droite
                        if (M[l-1][c+1] > maxLignePrecedente)
                        {
                            // si c'est plus grand on lui attribue la valeur et on assume que c'est maintenant la colonne avec la valeur la plus grande
                            maxLignePrecedente = M[l-1][c+1];
                            colonneMax = c+1;
                        }
                    }

                    //on attribue la valeur M[l,c] et on la stocke dans grilleValeursMax la colonne avec la plus grande valeur en dessous de notre position
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

        // On veut vérifier le chemin par d'où il vient grâce à la grille grilleValeursMax qui stocke les index de nos mouvements dans
        //chaque ligne
      
        cheminMax = new int[grille.length];

        for (int i = grilleValeursMax.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < grilleValeursMax[0].length; j++)
            {
                // si cela correspond au colonne, on est dedans
                if (j == indexMaxLigneCourante)
                {
                    // on ajoute la cellule au chemin
                    cheminMax[i] = j;
                    indexMaxLigneCourante = grilleValeursMax[i][j];
                    // on break car on a trouvé la cellule que l'on voulait donc on continue
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

