/**
 * Author: <Ludi Han>
 * Student Id: <1581026>
 * Email: <ludih@student.unimelb.edu.au>
 */
package server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    //the size of the pool
    private int corePoolSize = 10;
    private int maxSize = 16;
    List<Thread> coreList = new ArrayList<>();
    List<Thread> supportList = new ArrayList<>();

    public MyThreadPool() {
        for (int i = 0; i < corePoolSize; i++) {
            Thread thread = new Thread(coreTask, "core-" + i);
            coreList.add(thread);
            thread.start();
        }
    }

    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1024);
    private final Runnable coreTask = () -> {
        while(true){
            try{
                Runnable command = blockingQueue.take();
                command.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Runnable supportTask = () -> {
        while(true){
            try{
                Runnable command = blockingQueue.poll(1, TimeUnit.SECONDS);
                //if can't get task within set time, end the thread, cuz the pool is not busy
                if(command == null){
                    break;
                }
                command.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("End support thread : " + Thread.currentThread().getName());
        }
    };


    void execute(Runnable command){
        //if the number of the thread is less than the pool size, create thread
        if(coreList.size() < corePoolSize){
            Thread thread = new Thread(coreTask);
            coreList.add(thread);
            thread.start();
        }

        if(blockingQueue.offer(command)){
            return;
        }

        //if the total size of both lists aren't full, we can create new thread to the support list
        if(coreList.size() + supportList.size() < maxSize){
            Thread thread = new Thread(supportTask);
            supportList.add(thread);
            thread.start();
        }

        if(!blockingQueue.offer(command)){
            throw new RuntimeException("Blocking queue is full!");
        }




    }


}
