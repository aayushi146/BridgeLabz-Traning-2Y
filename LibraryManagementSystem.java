import java.util.Locale;

/**
 * DSA Practical Assignment: Smart Library Management System
 *
 * Task 1 - removeDuplicates    : two-pointer, in-place, O(N) time, O(1) space
 * Task 2 - searchByTitle       : case-insensitive substring search, O(N * L)
 * Task 3 - sortByPrice         : selection sort (counts swaps), O(N^2) time, O(1) space
 * Task 4 - searchByPrice       : binary search, O(log N)
 * Task 5 - minBooksForTargetCost : sliding window, O(N) time, O(1) auxiliary space
 */

/** Simple Book data model (as specified in the assignment). */
class Book {
    int bookId;
    String title;
    String author;
    double price;

    public Book(int bookId, String title, String author, double price) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
    }

    @Override
    public String toString() {
        return "[" + bookId + "] " + title + " - Rs. " + price;
    }
}

public class LibraryManagementSystem {

    /** Tolerance for comparing double prices (half a paisa). */
    private static final double EPS = 0.005;

    /** Converts a rupee price to whole paise so sums are exact (no floating-point drift). */
    private static long toPaise(double rupees) {
        return Math.round(rupees * 100.0);
    }

    // ---------------------------------------------------------------
    // Task 1: Remove duplicates in-place (input sorted by bookId)
    // ---------------------------------------------------------------
    public static int removeDuplicates(Book[] books, int n) {
        if (books == null || n <= 0) {
            return 0;
        }
        n = Math.min(n, books.length);

        int unique = 1; // books[0..unique-1] holds the unique books so far
        for (int i = 1; i < n; i++) {
            if (books[i].bookId != books[unique - 1].bookId) {
                books[unique++] = books[i];
            }
        }
        // Clear the leftover tail so stale duplicates are not accidentally used later.
        for (int i = unique; i < n; i++) {
            books[i] = null;
        }
        return unique;
    }

    // ---------------------------------------------------------------
    // Task 2: Partial, case-insensitive title search
    // ---------------------------------------------------------------
    public static void searchByTitle(Book[] books, int count, String query) {
        System.out.println("Search Results for '" + query + "':");
        if (books == null || query == null || query.trim().isEmpty()) {
            System.out.println("- Please enter a valid search word.");
            return;
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        boolean found = false;
        for (int i = 0; i < count; i++) {
            if (books[i] != null && books[i].title != null
                    && books[i].title.toLowerCase(Locale.ROOT).contains(q)) {
                System.out.println("- Found: [" + books[i].bookId + "] "
                        + books[i].title + " (Rs. " + books[i].price + ")");
                found = true;
            }
        }
        if (!found) {
            System.out.println("- No books found.");
        }
    }

    // ---------------------------------------------------------------
    // Task 3: Selection sort by price (ascending) + swap counter
    // ---------------------------------------------------------------
    public static void sortByPrice(Book[] books, int count) {
        int swaps = 0;
        for (int i = 0; i < count - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < count; j++) {
                if (books[j].price < books[minIdx].price) {
                    minIdx = j;
                }
            }
            if (minIdx != i) { // only count real swaps
                Book temp = books[i];
                books[i] = books[minIdx];
                books[minIdx] = temp;
                swaps++;
            }
        }
        System.out.println("Books Sorted by Price:");
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + ". " + books[i]);
        }
        System.out.println("Total Swaps: " + swaps);
    }

    // ---------------------------------------------------------------
    // Task 4: Binary search by exact price (array sorted by price)
    // Returns the FIRST index with that price (lower-bound style), or -1.
    // ---------------------------------------------------------------
    public static int searchByPrice(Book[] books, int count, double targetPrice) {
        long target = toPaise(targetPrice);
        int low = 0, high = count - 1, result = -1;
        while (low <= high) {
            int mid = low + (high - low) / 2; // overflow-safe
            long midPrice = toPaise(books[mid].price);
            if (midPrice == target) {
                result = mid;
                high = mid - 1; // keep looking left for the first occurrence
            } else if (midPrice < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Task 5: Sliding window - minimum consecutive books with sum >= S
    // Returns 0 if impossible (or if targetCost is not positive).
    // ---------------------------------------------------------------
    public static int minBooksForTargetCost(Book[] books, int count, double targetCost) {
        if (books == null || count <= 0 || targetCost <= 0) {
            return 0;
        }
        long target = toPaise(targetCost);
        long currentSum = 0;
        int left = 0;
        int minLen = Integer.MAX_VALUE;

        for (int right = 0; right < count; right++) {
            currentSum += toPaise(books[right].price); // expand from the right
            while (currentSum >= target) {             // contract from the left
                minLen = Math.min(minLen, right - left + 1);
                currentSum -= toPaise(books[left].price);
                left++;
            }
        }
        return (minLen == Integer.MAX_VALUE) ? 0 : minLen;
    }

    // ---------------------------------------------------------------
    // Helpers for the demo
    // ---------------------------------------------------------------
    private static void printBooks(Book[] books, int count) {
        for (int i = 0; i < count; i++) {
            System.out.println(books[i]);
        }
    }

    private static void explainWindow(Book[] books, int count, double target) {
        // Prints every minimum-length window (for the demo explanation only).
        int minLen = minBooksForTargetCost(books, count, target);
        if (minLen == 0) {
            return;
        }
        int w = 1;
        for (int start = 0; start + minLen <= count; start++) {
            long sum = 0;
            for (int k = start; k < start + minLen; k++) {
                sum += toPaise(books[k].price);
            }
            if (sum >= toPaise(target)) {
                System.out.println("- Window " + (w++) + " (Index " + start + " to "
                        + (start + minLen - 1) + "):");
                StringBuilder sb = new StringBuilder("  ");
                for (int k = start; k < start + minLen; k++) {
                    sb.append(books[k]);
                    if (k < start + minLen - 1) sb.append(" + ");
                }
                System.out.println(sb);
                System.out.println("  Total = Rs. " + (sum / 100.0)
                        + " (>= " + target + ") -> Length: " + minLen + " books");
            }
        }
    }

    public static void main(String[] args) {
        // ----- Sample test case from the assignment -----
        Book[] books = {
                new Book(101, "Data Structures",   "Mark",   400.0),
                new Book(101, "Data Structures",   "Mark",   400.0), // duplicate
                new Book(102, "Java Basics",       "James",  300.0),
                new Book(103, "Python Guide",      "Guido",  600.0),
                new Book(104, "Database Systems",  "Raghu",  500.0),
                new Book(105, "Computer Networks", "Andrew", 700.0)
        };

        System.out.println("=== Task 1: Remove Duplicates ===");
        int count = removeDuplicates(books, books.length);
        System.out.println("Unique Books Count: " + count);
        System.out.println("Book List:");
        printBooks(books, count);

        System.out.println("\n=== Task 2: Partial Title Search ===");
        searchByTitle(books, count, "data");

        System.out.println("\n=== Task 3: Sort by Price ===");
        sortByPrice(books, count);

        System.out.println("\n=== Task 4: Search by Price ===");
        double targetPrice = 500.0;
        System.out.println("Searching for Price Rs. " + targetPrice + "...");
        int idx = searchByPrice(books, count, targetPrice);
        if (idx != -1) {
            System.out.println("Result: Book found at index " + idx + ": " + books[idx]);
        } else {
            System.out.println("Result: Book not found (-1)");
        }

        System.out.println("\n=== Task 5: Sliding Window ===");
        double targetCost = 1000.0;
        System.out.println("Finding minimum consecutive books whose total price >= Rs. "
                + targetCost + "...");
        int minBooks = minBooksForTargetCost(books, count, targetCost);
        System.out.println("Minimum Consecutive Books Needed: " + minBooks);
        if (minBooks > 0) {
            System.out.println("Explanation:");
            explainWindow(books, count, targetCost);
        }

        // ----- Extra edge-case checks -----
        System.out.println("\n=== Edge Cases ===");
        System.out.println("Price 999.0 search        -> " + searchByPrice(books, count, 999.0) + " (expected -1)");
        System.out.println("Target 5000 (impossible)  -> " + minBooksForTargetCost(books, count, 5000.0) + " (expected 0)");
        System.out.println("Target 700 (single book)  -> " + minBooksForTargetCost(books, count, 700.0) + " (expected 1)");
        System.out.println("Empty array duplicates    -> " + removeDuplicates(new Book[0], 0) + " (expected 0)");
        searchByTitle(books, count, "JAVA");
        searchByTitle(books, count, "xyz");
    }
}