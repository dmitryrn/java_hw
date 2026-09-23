public class MainTest {
    public static void main(String[] args) {
        check(1221, true);
        check(1234, false);
        check(123, false);
        check(121, true);
        check(-121, false);
        check(10, false);
        check(7, true);
        check(0, true);

        System.out.println("All tests passed");
    }

    private static void check(int number, boolean expected) {
        boolean actual = Main.isPalindrome(number);

        if (actual != expected) {
            throw new AssertionError("Test failed for " + number);
        }
    }
}
