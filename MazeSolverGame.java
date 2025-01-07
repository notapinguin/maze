import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;

public class MazeSolverGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

class GameFrame extends JFrame {
    private int rows;
    private int cols;
    private MazePanel mazePanel;

    public GameFrame() {
        setTitle("Maze Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        resetGame(); // Initialize the first maze
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void randomizeDimensions() {
        Random rand = new Random();
        boolean feelingLucky = rand.nextBoolean();
        int value = 5 + rand.nextInt(151);
        if (feelingLucky) value = 5 + rand.nextInt(31);
        rows = value%2==0?value+1:value; // Random between 21 and 101
        
        cols = rows; // Ensure square maze
        System.out.println("New Maze Size: " + rows + "x" + cols);
    }

    public void resetGame() {
        randomizeDimensions(); // Always randomize dimensions on reset
    
        if (mazePanel != null) {
            remove(mazePanel); // Remove the old panel if it exists
        }
    
        mazePanel = new MazePanel(rows, cols);
        add(mazePanel);
        revalidate(); // Ensure layout manager updates properly
        repaint();    // Ensure the frame repaints fully
        pack();       // Adjust the window size based on new panel dimensions
        mazePanel.requestFocusInWindow();
    }
    



class MazePanel extends JPanel {
    private int rows;
    private int cols;
    private final int cellSize;
    private final int[][] maze;
    private final MazePoint player;
    private final MazePoint exit;
    private List<MazePoint> shortestPath;
    private boolean firstMaze = true;
    private boolean showShortestPath = false; // Controls whether to show the shortest path
    private StringBuilder typedInput = new StringBuilder();

    public MazePanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    
        int fixedWindowSize = 600;
        this.cellSize = Math.min(fixedWindowSize / rows, fixedWindowSize / cols);
    
        this.maze = new int[rows][cols];
        this.player = new MazePoint(1, 1, 0);
        this.exit = new MazePoint(rows - 2, cols - 2, 0);
    
        generateMaze();
        if (shortestPath == null) {
            findShortestPath();
        }
        
    
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setBackground(Color.WHITE);
        addKeyListener(new PlayerControl());
        setFocusable(true);
    }

    private boolean isPathValid() {
        boolean[][] visited = new boolean[rows][cols];
        return dfsPathCheck(1, 1, visited);
    }
    
    private boolean dfsPathCheck(int x, int y, boolean[][] visited) {
        // Boundary check and wall check
        if (x < 0 || y < 0 || x >= rows || y >= cols || maze[x][y] == 1 || visited[x][y]) {
            return false;
        }
    
        // If we reach the exit, the path is valid
        if (x == rows - 2 && y == cols - 2) {
            return true;
        }
    
        // Mark the current cell as visited
        visited[x][y] = true;
    
        // Explore in all four directions
        return dfsPathCheck(x + 1, y, visited) || // Down
               dfsPathCheck(x - 1, y, visited) || // Up
               dfsPathCheck(x, y + 1, visited) || // Right
               dfsPathCheck(x, y - 1, visited);   // Left
    }
    
    
    
    public void resetMaze() {
        
        // Reset the maze grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                
                maze[i][j] = 1; // Set all cells to walls
            }
        }
        
    
        // Regenerate the maze
        generateMaze();
    
        // Reset player position
        player.setLocation(1, 1);
    
        // Reset exit position
        exit.setLocation(rows - 2, cols - 2);
    
        // Reset shortest path
        shortestPath = null;
    
        // Repaint the panel to reflect changes
        repaint();
    }
    

    private void generateMaze() {
        
        // Initialize grid with walls
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze[i][j] = 1; // Wall
            }
        }

        // Recursive backtracking algorithm
        Random rand = new Random();
        ArrayList<MazePoint> stack = new ArrayList<>();
        MazePoint start = new MazePoint(1, 1, 0);
        maze[start.x][start.y] = 0;
        stack.add(start);

        while (!stack.isEmpty()) {
            MazePoint current = stack.remove(stack.size() - 1);
            ArrayList<MazePoint> neighbors = new ArrayList<>();

            for (int[] dir : new int[][]{{0, 2}, {2, 0}, {0, -2}, {-2, 0}}) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                if (nx > 0 && ny > 0 && nx < rows - 1 && ny < cols - 1 && maze[nx][ny] == 1) {
                    neighbors.add(new MazePoint(nx, ny, 0));
                }
            }

            if (!neighbors.isEmpty()) {
                stack.add(current);
                Collections.shuffle(neighbors);
                MazePoint chosen = neighbors.get(0);
                maze[chosen.x][chosen.y] = 0;
                maze[(current.x + chosen.x) / 2][(current.y + chosen.y) / 2] = 0;
                stack.add(chosen);
            }
        }

        if(!isPathValid()&&!firstMaze) generateMaze();
        firstMaze = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        
        
        super.paintComponent(g);

        // Draw maze
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (maze[i][j] == 1) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }
        if(showShortestPath)paintShortestPath(g);
        // Draw player
        g.setColor(Color.BLUE);
        g.fillRect(player.y * cellSize, player.x * cellSize, cellSize, cellSize);

        // Draw exit
        g.setColor(Color.RED);
        g.fillRect(exit.y * cellSize, exit.x * cellSize, cellSize, cellSize);
        
        // Paint the shortest path
        
    }

    private void paintShortestPath(Graphics g) {
        
        g.setColor(Color.GREEN);
        for (MazePoint p : shortestPath) {
            g.fillRect(p.y * cellSize, p.x * cellSize, cellSize, cellSize);
        }
    }

    private void findShortestPath() {
        PriorityQueue<MazePoint> queue = new PriorityQueue<>(Comparator.comparingInt(p -> p.distance));
        Map<MazePoint, MazePoint> prev = new HashMap<>();
        Map<MazePoint, Integer> distances = new HashMap<>();
        shortestPath = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                MazePoint p = new MazePoint(i, j, Integer.MAX_VALUE);
                distances.put(p, Integer.MAX_VALUE);
            }
        }

        MazePoint start = new MazePoint(player.x, player.y, 0);
        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            MazePoint current = queue.poll();

            if (current.equals(exit)) break;

            for (int[] dir : new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}}) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                MazePoint neighbor = new MazePoint(nx, ny, 0);

                if (nx >= 0 && ny >= 0 && nx < rows && ny < cols && maze[nx][ny] == 0) {
                    int newDist = distances.get(current) + 1;
                    if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        distances.put(neighbor, newDist);
                        neighbor.distance = newDist;
                        prev.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        MazePoint current = exit;
        while (current != null) {
            shortestPath.add(current);
            current = prev.get(current);
        }
        Collections.reverse(shortestPath);
    }

    private class PlayerControl extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int dx = 0, dy = 0;
            char typedChar = e.getKeyChar();
            String key = "cc";
            if (Character.isLetterOrDigit(typedChar)) {
                typedInput.append(typedChar); // Append the typed character
            }
    
            // Check if the typed word matches "show"
            if (typedInput.toString().equalsIgnoreCase(key)) {
                showShortestPath = !showShortestPath; // Display the shortest path
                System.out.println("Shortest path is now visible!");
                typedInput.setLength(0); // Clear the typed input after triggering
            } else if (typedInput.length() > key.length()) {
                typedInput.setLength(0); // Reset if the word exceeds "show"
            }
            

            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> dx = -1;
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> dx = 1;
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> dy = -1;
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> dy = 1;
            }
            
            
            
            int newX = player.x + dx;
            int newY = player.y + dy;
            
            if (newX >= 0 && newY >= 0 && newX < rows && newY < cols && maze[newX][newY] == 0) {
                player.setLocation(newX, newY);
                
                // Debug message: Check if the player is on the shortest path
                if (shortestPath != null && shortestPath.contains(player)) {
                    System.out.println("Player is on the optimal path at: (" + player.x + ", " + player.y + ")");
                } else {
                    System.out.println("Player moved off the optimal path at: (" + player.x + ", " + player.y + ")");
                    GameFrame gameFrame = (GameFrame) SwingUtilities.getWindowAncestor(MazePanel.this);
                    gameFrame.resetGame();

                }
    
                repaint();
            }

    
            if (player.equals(exit)) {
                JOptionPane.showMessageDialog(MazePanel.this, "You solved the maze!");
                System.exit(0);
            }
        }
    }
    
}

class MazePoint {
    int x, y, distance;

    public MazePoint(int x, int y, int distance) {
        this.x = x;
        this.y = y;
        this.distance = distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MazePoint)) return false;
        MazePoint other = (MazePoint) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
}