import mpi.MPI;
import mpi.MPIException;
import mpi.Request;
import mpi.Status;

public class SimpleWaitAll
{
    public static void main(String[] args) throws MPIException
    {
        MPI.Init(args);
        int TAG = 0;

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int[] buf = new int[]{0,0};

        int to = rank == size - 1 ? size - 1 - rank : rank + 1;
        int from = rank == 0 ? size - 1 : rank - 1;
        System.out.println("RANK = " + rank + " From = " + from + " to " + to);
        Request recvRequest1 = MPI.COMM_WORLD.Irecv(buf,0,1,MPI.INT,from,5);
        Request recvRequest2 = MPI.COMM_WORLD.Irecv(buf, 0, 2, MPI.INT, to, 6);

        MPI.COMM_WORLD.Isend(new int[]{rank,2023},0,2,MPI.INT,from,6);
        MPI.COMM_WORLD.Isend(new int[]{rank},0,1,MPI.INT,to,5);
        //[порядок, в данном случае против час стрелки]
        Status[] st = Request.Waitall(new Request[]{recvRequest1,recvRequest2});
        System.out.println("Rank = " + rank + " prev = " + buf[0] + " next = " + buf[1]);
        MPI.Finalize();
    }
}
