package mpjexample1;
import mpi.*;
public class MPJExample1
{
    public static void main(String[] args)
    {
        MPI.Init(args);
        int TAG = 0;
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        //System.out.println(" size = " + size);
        if(rank%2==0)
        {
            if((rank+1) != size)
            {
                int to = rank + 1;
                int[] message = new int[]{2016};
                System.out.println("Send from : " + rank + " to " + to);
                MPI.COMM_WORLD.Send(message,0,message.length,MPI.INT,to,TAG);
                /*void *buf;int  count, datatype,dest, tag;
                buf - адрес начала буфера посылаемых данных
                count - число пересылаемых объектов типа, соответствующего datatype
                datatype - MPI-тип принимаемых данных
                dest - номер процесса-приемника. Ранг процесса-получателя сообщения ( целое число от 0 до n–1, где n число процессов в области взаимодействия);
                tag – уникальное число от 0 до 32767, идентифицирующий сообщение. Позволяет различать сообщения, приходящие от одного процесса. Теги могут использоваться и для соблюдения определенного порядка приема сообщений.*/

            }
        }else
        {
            if(rank!=0)
            {
                int from = rank - 1;
                int[] message = new int[1];
                mpi.Status st = MPI.COMM_WORLD.Recv(message,0,message.length,MPI.INT,from,TAG);
                /*void *buf;int  count, datatype,source, tag; */
                System.out.println("received from rank = " + from + " for " + rank + " with tag = " + TAG + " message = " + message[0]);
                System.out.println(st.countInBytes);

            }
        }

        MPI.Finalize();
    }

}
