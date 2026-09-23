public class MainTest {
    public static void main(String[] args) {
        check(null, 0);
        check(new TreeNode(1), 1);
        check(
                new TreeNode(
                        1,
                        new TreeNode(2, new TreeNode(4), null),
                        new TreeNode(3)
                ),
                3
        );

        System.out.println("All tests passed");
    }

    private static void check(TreeNode tree, int expected) {
        int actual = Main.maxDepth(tree);

        if (actual != expected) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }
}
