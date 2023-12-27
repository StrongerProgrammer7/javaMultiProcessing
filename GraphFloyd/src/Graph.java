//Разработать алгоритм вычисления диаметра произвольного неориентированного графа.

import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Graph
{
    static int countVertex = 1000;
    static int[][] graph;

    static int ROOT = 0;

    public static void main(String[] args)
    {
        MPI.Init(args);

        int size = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        int countVertexByRank = countVertex % (size-1);
        int slice = (countVertex - countVertexByRank)/(size-1);
        Status status;
        if(rank == ROOT)
        {
            double t1,t2;
            int disable = 0;
            int[] result = new int[3];
            graph = new int[countVertex][countVertex];
            for(int i=0;i<graph.length;i++)
            {
                for(int j=0;j<graph.length;j++)
                {
                    if(i==j)
                        graph[i][j] =0;
                    else if(j>i)
                    {
                        int temp = ThreadLocalRandom.current().nextInt(1,20);
                        graph[i][j] = temp;
                        graph[j][i] = temp;
                    }
                }
            }
            //printMatr(graph);
            for(int i=1;i<size;i++)
            {
                for(int j=0;j<countVertex;j++)
                {
                    int[] temp = graph[j];
                    MPI.COMM_WORLD.Send(temp,0,countVertex,MPI.INT,i,1);
                }

            }
            Instant starts = Instant.now();

            int lastTag = 2;

            int diameter = -1;
            for(int i=0;i<graph.length;i++)
            {
                int max = findMaxVecotr(graph[i]);
                if(diameter < max)
                    diameter = max;
            }
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
            Instant end = Instant.now();
            //printMatr(graph);
            System.out.println("total time = " + Duration.between(end,starts));
            System.out.println("Diameter = " + diameter);
        }else
        {
            graph = new int[countVertex][countVertex];
            int t =3;
            int[] out = new int[t];
            for(int i=0;i<countVertex;i++)
            {
                int[] temp = new int[countVertex];
                MPI.COMM_WORLD.Recv(temp,0,countVertex,MPI.INT,ROOT,1);
                graph[i] =temp;
            }
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
