import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class GraphWithSetMatrix
{
    static int countVertex = 5;

    static int[][] graph = {
            {0,2,4},
            {2,0,10},
            {4,10,0}
    };
    static int[][] graph1 = {
            {0,10,7,8,2},
            {10,0,11,14,3},
            {7,11,0,2,1},
            {8,14,2,0,3},
            {2,3,1,3,0}
    };
    static int[][] graph4 = {
            {0,11,14,18,11},
            {11,0,16,16,13},
            {14,16,0,7,6},
            {18,16,7,0,8},
            {11,13,6,8,0}
    };
    static int ROOT = 0;
    public GraphWithSetMatrix(int countVertex)
    {
        this.countVertex = countVertex;
        this.graph = new int[countVertex][countVertex];
    }

    public void addEdge(int source,int destination,int weight)
    {
        graph[source][destination] = weight;
        graph[destination][source] = weight;
    }

    public static int getDiameter(int countVertex,int[][] graph)
    {
        int[][] distances = new int[countVertex][countVertex];

        for(int i=0;i<countVertex;i++)
        {
            for(int j=0;j<countVertex;j++)
            {
                distances[i][j] = graph[i][j];
            }
            distances[i][i] = 0;
        }
        printMatr(distances);
        for(int k=0;k<countVertex;k++)
        {
            for(int i =0;i<countVertex;i++)
            {
                for(int j =0;j<countVertex;j++)
                {
                   // System.out.println("A["+i+"]["+k+"]= " + distances[i][k] + "| A["+k+"]["+j+"]=" + distances[k][j] + " = " + (distances[i][k] + distances[k][j]) + " < " + distances[i][j]);
                    if(distances[i][k] + distances[k][j] < distances[i][j])
                        distances[i][j] =distances[i][k] + distances[k][j];
                }
            }
        }

        int diameter = 0;

        for(int i=0;i<countVertex;i++)
        {
            for(int j=0;j<countVertex;j++)
            {
                if(distances[i][j] > diameter)
                    diameter = distances[i][j];
            }
        }
        printMatr(distances);
        System.out.println(diameter);
        return diameter;
    }
    public static void main(String[] args)
    {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();
         countVertex = graph.length;
        int countVertexByRank = countVertex % (size-1);
        int slice = (countVertex - countVertexByRank)/(size-1);
        Status status;

        if(rank == ROOT)
        {
            //getDiameter(countVertex,graph);
            double t1,t2;
            int disable = 0;
            int[] result = new int[3];


            t1 = MPI.Wtime();

            int lastTag = 2;

            int diameter = -1;
            for(int i=0;i<graph.length;i++)
            {
                int max = findMaxVecotr(graph[i]);
                if(diameter < max)
                    diameter = max;
            }
            printMatr(graph);
            do
            {
                status = MPI.COMM_WORLD.Probe(MPI.ANY_SOURCE,MPI.ANY_TAG);
                while(status == null)
                    status = MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE,MPI.ANY_TAG);
                Request req = MPI.COMM_WORLD.Irecv(result,0,3,MPI.INT,MPI.ANY_SOURCE,MPI.ANY_TAG);
                req.Wait();

                // System.out.println("Tag = " + status.tag + " Source = " + status.source + " Result = " + result[1] + ":" + result[2] + " = " + result[0]);
                if(status.tag == lastTag)
                {
                    //System.out.println(Arrays.toString(result));
                    if(diameter > result[0])
                        diameter = result[0];
                    disable++;
                }else
                {
                    if(graph[result[1]][result[2]] > result[0])
                    {
                        graph[result[1]][result[2]] = result[0];
                        graph[result[2]][result[1]] = result[0];
                    }
                }
            }while(disable < size -1);
            t2 = MPI.Wtime();
            printMatr(graph);
            System.out.println("total time = " + (t2 - t1));
            System.out.println("Diameter = " + diameter);
        }else
        {
            int t =3;
            int[] out = new int[t];

            if(rank +1!=size)
                countVertexByRank = 0;
            //System.out.println("Rank = " + rank + " K = " + (slice*(rank-1)) +
            //      " < " + (slice*(rank-1)+slice+countVertexByRank));
            for(int k=slice*(rank-1);k<slice*(rank-1)+slice+countVertexByRank;k++)
            {
                for(int i=0;i<graph.length;i++)
                {
                    for(int j=k;j<graph.length;j++)
                    {
//                        if(rank == 2)
//                            System.out.println("A["+i+"]["+k+"]= " + graph2[i][k] + "| A["+k+"]["+j+"]=" + graph2[k][j] + " = " + (graph2[i][k] + graph2[k][j]) + " < " + graph2[i][j]);
                        if(j>i && (graph[i][k] * graph[k][j] != 0) && (i!=j))
                        {
                            if(graph[i][j] == 0 || (graph[i][k] + graph[k][j] < graph[i][j]))
                            {

                                graph[i][j] = graph[i][k] + graph[k][j];
                                graph[j][i] = graph[i][k] + graph[k][j];
                                out[0] = graph[i][j];
                                out[1] = i;
                                out[2] = j;
                                MPI.COMM_WORLD.Isend(out,0,3,MPI.INT,ROOT,0);
                            }
                        }
                    }
                }
            }
            int potentialDiameter = -1;
            for(int i=0;i<graph.length;i++)
            {
                int max = findMaxVecotr(graph[i]);
                if(max >potentialDiameter)
                    potentialDiameter = max;
            }
            MPI.COMM_WORLD.Isend(new int[]{potentialDiameter,0,0},0,3,MPI.INT,ROOT,2);
        }
        MPI.Finalize();

    }
    static int findMaxVecotr(int[] a)
    {
        int max = -1;
        for(int i=0;i<a.length;i++)
            if(a[i] >max)
                max = a[i];
        return max;
    }

    static void printMatr(int[][] matr)
    {
        for(int i=0;i<matr.length;i++)
            System.out.println(Arrays.toString(matr[i]));
    }
}
