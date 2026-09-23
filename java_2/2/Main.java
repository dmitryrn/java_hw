import java.util.ArrayDeque;
import java.util.Deque;

// non recursive DFS
public class Main {
    public static int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }

        Deque<TreeNode> nodes = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();

        nodes.push(root);
        depths.push(1);

        int maxDepth = 0;

        while (!nodes.isEmpty()) {
            TreeNode node = nodes.pop();
            int depth = depths.pop();

            maxDepth = Math.max(maxDepth, depth);

            if (node.left != null) {
                nodes.push(node.left);
                depths.push(depth + 1);
            }

            if (node.right != null) {
                nodes.push(node.right);
                depths.push(depth + 1);
            }
        }

        return maxDepth;
    }

    public static void main(String[] args) {
        TreeNode tree = new TreeNode(
                1,
                new TreeNode(2, new TreeNode(4), null),
                new TreeNode(3)
        );

        System.out.println("Максимальная глубина: " + maxDepth(tree));
    }
}
