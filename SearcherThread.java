/*
 Created by Ugurcan AYTAR on 14/12/15.
 */

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SearcherThread <T> extends Thread {
    private T needle;
    private T[] haystack;
    private int start, end;
    private static Lock lock;
    private Condition cond;
    private static int firstItemIndex;
    private static AtomicBoolean done;

    public SearcherThread(T needle, T[] haystack, int start, int end, Lock lock, Condition cond) {
        this.needle = needle;
        this.haystack = haystack;
        this.start = start;
        this.end = end;
        this.lock = lock;
        this.cond = cond;
        this.firstItemIndex = -1;
        done = new AtomicBoolean(false);
    }

    public int getResult() {
        return firstItemIndex;
    }

    public void terminate() {
        done.set(true);
    }

    // @override
    public void run() {
        for (int i = start; !done.get() && i < end; ++i){
            if (haystack[i].equals(needle)) {
                firstItemIndex = i;
                lock.lock();
                try {
                    cond.signal();
                } finally {
                    lock.unlock();
                }
                return;
            }
        }
    }

    public static <T> int search(T needle, T[] haystack, int numThreads) throws InterruptedException {
        List<SearcherThread<T>> searchers = new ArrayList<SearcherThread<T>>();
        int numItemsPerThread = haystack.length / numThreads;
        int extraItems = haystack.length - numItemsPerThread * numThreads;

        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        for (int i = 0, start = 0; i < numThreads; i++) {
            int numItems = (i < extraItems) ? (numItemsPerThread++) : numItemsPerThread;
            searchers.add(new SearcherThread<T>(needle, haystack, start, start + numItems, lock, cond));
            start += numItems;
        }

        for (SearcherThread<T> searcher : searchers) {
            searcher.start();
        }

        lock.lock();
        try {
            cond.await();
        } finally {
            lock.unlock();
        }

        for (SearcherThread<T> searcher : searchers)
            searcher.terminate();

        for (SearcherThread<T> searcher : searchers) {
            int result = searcher.getResult();
            if (result != -1)
                return result;
        }

        return -1;
    }
}
