/**
 * Created by Ugurcan AYTAR on 14/12/15.
 */
public class Searcher {

    public static void main(String[] args) throws InterruptedException
    {
        int numItems = 10000;
        Integer[] haystack = new Integer[numItems];
        int domainSize = 1000;
        for (int i=0; i<numItems; ++i)
            haystack[i] = (int)(Math.random() * domainSize);
        int needle = 10;
        int newIndex = SearcherThread.search(needle, haystack, 1);
        System.out.println("Index of the first needle found: " + newIndex);
    }
}
