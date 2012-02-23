package com.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Concurrent4ThreadPool { // 用于管理线程和提供线程服务的类
    private ExecutorService pool = null;// 线程池
    private static final int POOL_SIZE = 4;// 线程池的容量
    public Concurrent4ThreadPool() {
        //pool = Executors.newFixedThreadPool(POOL_SIZE);// 创建线程池
        pool = Executors.newCachedThreadPool();
        System.out.println("the server is ready...");
    }
    public void server() {
        int i = 0;
        while (i < 100) {
            pool.execute(new Worker(i));// 运行线程池
            //pool.submit(new Worker(i));
            i++;
        }
        pool.shutdown();
        
        int largestPoolSize = ((ThreadPoolExecutor) pool).getLargestPoolSize();
        System.out.println("Largest pool size = " + largestPoolSize);
    }
    public static void main(String[] args) {
        new Concurrent4ThreadPool().server();
    }
    class Worker implements Runnable // 工作线程，线程要完成的工作在此类中实现
    {
        int id;
        Worker(int id) {
            this.id = id;
        }
        public void run() {
            System.out.println("task " + id + ":start");// 具体要做的事
        }
    }
}
