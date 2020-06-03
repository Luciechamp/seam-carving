package coccinelle;


/**
 * Contient le tableau M contenant les nombres maximums de pucerons et le chemin optimal
 */
public class CoccinelleResult
{
    private int[][] M;
    private int[] path;
    
    public CoccinelleResult(int[][] M, int[] path)
    {
        this.M = M;
        this.path = path;
    }
    
    public int[][] getM()
    {
        return this.M;
    }
    
    public int[] getPath()
    {
        return this.path;
    }
    
    public String getPathCellString(int index)
    {
        return "(" + index + "," + this.path[index] + ")";
    }
    
    public String getPathString()
    {
        StringBuilder pathString = new StringBuilder();
        for (int i = 0; i < this.path.length; i++)
        {
            pathString.append(getPathCellString(i));
        }
        return pathString.toString();
    }
    
    public int getMaxScore()
    {
        int[] lastLine = this.M[M.length - 1];
        int maxScore = lastLine[0];
        for (int i = 0; i < lastLine.length; i++){
            if(lastLine[i] > maxScore){
                maxScore = lastLine[i];
            }
        }
        return maxScore;
    }
}
