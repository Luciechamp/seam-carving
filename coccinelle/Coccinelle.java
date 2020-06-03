package coccinelle;


/**
 * Classe coccinelle comprenant la grille des pucerons et contenant une fonction main calculant le chemin optimal et la grille des scores (nombre cumulé de pucerons)
 */
public class Coccinelle
{
    private int[][] grid;
    
    public Coccinelle(int[][] grid)
    {
        this.grid = grid;
    }
    
    public CoccinelleResult computeMaxGrid()
    {
        int[][] M = new int[grid.length][grid[0].length];
        int[][] gridBiggestValues = new int[grid.length][grid[0].length];
        
        // We fill the M array
        // we go through the grid line by line ...
        for (int l = 0; l < grid.length; l++)
        {
            // ... and column by column 
            for (int c = 0; c < grid[0].length; c++)
            {
                // if we're on the first line M[l][c] will be the same as grid[l][c]
                if(l == 0)
                {
                    M[l][c] = grid[l][c];
                    gridBiggestValues[l][c] = 0;
                }
                else
                {
                    //we check the 3 possible values of the previous line
                    // 1) we create a temporary int to store the biggest value on the previous line and we give it 
                    // the value right under the one we're in now
                    int biggestNbPreviousLine = M[l-1][c];
                    int columnOfTheBiggest = c;
                    // 2) we check if the value at the right and at the left (next line) are bigger (BUT careful if we're 
                    // at the first column or the last

                    // if it's not the first column
                    if (c > 0)
                    {
                        // we compare the one below with the below left
                        // l - 1 because it's previous line, c - 1 because it's the column on the left
                        if (M[l-1][c-1] > biggestNbPreviousLine)
                        {
                            // if it's bigger we give it the value and we assume that this is now the column of the biggest figure
                            biggestNbPreviousLine = M[l-1][c-1];
                            columnOfTheBiggest = c-1;
                        }
                    }

                    // if it's not the last column
                    if (c < grid[0].length - 1)
                    {
                        // we compare the one below with the below right
                        if (M[l-1][c+1] > biggestNbPreviousLine)
                        {
                            // if it's bigger we give it the value and we assume that this is now the column of the biggest figure
                            biggestNbPreviousLine = M[l-1][c+1];
                            columnOfTheBiggest = c+1;
                        }
                    }

                    // we assign M[l,c] value and we store in gridBiggestValues the column of the biggest value below us
                    gridBiggestValues[l][c] = columnOfTheBiggest;
                    M[l][c] = grid[l][c] + biggestNbPreviousLine;
                }
            }
        }
        
        // Now we're looking for the biggest number on the last line to see how much it ate
        int indexOfTheBiggestValueCurrentLine = 0;
        int tempBiggest = M[grid.length-1][0];

        // we go through the last line of M
        for (int indexBiggestLastLine = 0; indexBiggestLastLine < M[0].length; indexBiggestLastLine++)
        {
            // if the value is bigger we replace it and store its index
            if(tempBiggest < M[grid.length-1][indexBiggestLastLine])
            {
                tempBiggest = M[grid.length - 1][indexBiggestLastLine];
                indexOfTheBiggestValueCurrentLine = indexBiggestLastLine;
            }
        }
        
        // We want to check the path from where it comes thanks to our gridBiggestValues array that stored the
        // indexes of our movement in each line
        int[] path = new int[grid.length];
       
        for (int i = gridBiggestValues.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < gridBiggestValues[0].length; j++)
            {
                // if it matches the column we're in
                if (j == indexOfTheBiggestValueCurrentLine)
                {
                    // we add the cell to the path
                    path[i] = j;
                    indexOfTheBiggestValueCurrentLine = gridBiggestValues[i][j];
                    // we break because we have found the cell we wanted on that line so we move on
                    break;
                }
            }
        }
        
        CoccinelleResult coccinelleResult = new CoccinelleResult(M, path);
        return coccinelleResult;
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
        
        int[][] grid = defaultGrid;
        
        Coccinelle coccinelle = new Coccinelle(grid);
        
        CoccinelleResult coccinelleResult = coccinelle.computeMaxGrid();
        
        int[][] M = coccinelleResult.getM();
        int[] path = coccinelleResult.getPath();
        int maxScore = coccinelleResult.getMaxScore();
       
        System.out.print("Grille des pucerons :");
        System.out.println();

        for (int i = grid.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < grid[0].length; j++)
            {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
           
         System.out.println();
         System.out.print("Tableau M[L][C] de terme général M[l][c] = m(l,c) :");
         System.out.println();

        // we display our M array
        for (int i = M.length - 1; i >= 0; i--)
        {
            for (int j = 0; j < M[0].length; j++)
            {
                System.out.print(M[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println();

        System.out.print("La coccinelle a mangé " + maxScore + " pucerons.");
        System.out.println();

        System.out.print("Elle a suivi le chemin " + coccinelleResult.getPathString());
        System.out.println();

        System.out.print("Case d'atterrissage = " + coccinelleResult.getPathCellString(0));
        System.out.println();

        System.out.print("Case de l'interview = " + coccinelleResult.getPathCellString(path.length - 1));
        System.out.println();

    }
        
}
    

