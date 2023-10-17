import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents a collection
interface ICollection<T> {

  //adds the given vertex to the given collection
  void add(Vertex vertex);

  // determines if there are any elements in the collection
  boolean isEmpty();

  // removes an element from the collection
  Vertex remove();

}

// to represent a player
class Player {
  Vertex currentPos;
  IList<Vertex> visited; // the vertices that the player has already visited

  Player(Vertex currentPos) {
    this.currentPos = currentPos;
    this.visited = new Empty<Vertex>();
  }

  // draws this player
  WorldImage drawPlayer() {
    return new RectangleImage(Maze.cellSize, Maze.cellSize,
        OutlineMode.SOLID, new Color(115, 171, 133));
  }
}

// represents a vertex
class Vertex {
  Edge right;
  Edge down;
  Edge left;
  Edge up;
  boolean rightFlag = true; // false means dont change
  boolean downFlag = true; // false means dont change
  int x;
  int y;

  // represents the neighboring vertices of this vertex
  ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
  boolean visited = false; // true if this vertex has been visited

  Vertex() {

  }

  Vertex(int x, int y) {
    this.x = x;
    this.y = y;
  }

  Vertex(Edge right, Edge down) {
    this.right = right;
    this.down = down;
  }

  Vertex(Edge right, Edge left, Edge up, Edge down) {
    this.right = right;
    this.left = left;
    this.up = up;
    this.down = down;
  }

  // determines if the vertex is equal to the given object
  public boolean equals(Object other) {
    if (!(other instanceof Vertex)) {
      return false;
    }
    else {
      Vertex that = (Vertex) other;
      return this.x == that.x &&
          this.y == that.y;
    }
  }

  // gives the hashcode for the vertex
  public int hashCode() {
    return this.x * this.y * 10000;
  }

  // draws vertex's horizontal right edge
  WorldImage drawHorizontal() {
    return new RectangleImage(Maze.cellSize, 1, OutlineMode.SOLID,
        Color.BLUE);
  }

  // draws this vertex's down edge
  WorldImage drawVertical() {
    return new RectangleImage(1, Maze.cellSize, OutlineMode.SOLID,
        Color.BLUE);
  }


}

// represents an edge
class Edge implements Comparable<Edge> {
  Vertex from;
  Vertex to;
  int weight;

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  Edge(int weight) {
    this.weight = weight;
  }

  Edge(Vertex from, Vertex to) {
    this.from = from;
    this.to = to;
  }

  // determines if the edge is equal to the given object
  public boolean equals(Object other) {
    if (!(other instanceof Edge)) {
      return false;
    }
    else {
      Edge that = (Edge) other;
      return (this.from.equals(that.from) && this.to.equals(that.to))
          || (this.from.equals(that.to) && this.to.equals(that.from));
    }
  }

  // gives the hashcode for the edge
  public int hashCode() {
    return this.from.hashCode() * this.to.hashCode() * 10000;
  }

  // compares this edge to the given edge
  public int compareTo(Edge edge) {
    return this.weight - edge.weight;
  }
}

// represents a maze
class Maze extends World {
  ArrayList<ArrayList<Vertex>> arrVert;
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> worklist;
  HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
  IList<Vertex> searchPath = new Empty<Vertex>();
  Vertex key;
  IList<Vertex> vertices;
  Player player;
  Vertex endPoint; // represents the vertex that must be reached to win
  static int mazeHeight = 50;
  static int mazeWidth = 80;
  static int cellSize = 10;

  Maze() {
    this.setMaze();
  }

  // created a 2d arraylist of vertices
  public ArrayList<ArrayList<Vertex>> make2Dvertices() {
    ArrayList<ArrayList<Vertex>> ans = new ArrayList<ArrayList<Vertex>>();
    for (int i = 0; i < mazeWidth; i++) {
      ArrayList<Vertex> arrVertex = new ArrayList<Vertex>();
      for (int j = 0; j < mazeHeight; j++) {
        arrVertex.add(new Vertex(i, j));
      }
      ans.add(arrVertex);
    }
    return ans;
  }

  // initializes the edges of each vertex in the list of vertices 
  public ArrayList<Edge> initVerticalEdges(ArrayList<ArrayList<Vertex>> vert) {
    ArrayList<Edge> arr = new ArrayList<Edge>();
    for (int j = 0; j < vert.size(); j++) {
      for (int i = 0; i < vert.get(j).size(); i++) {
        Vertex cur = vert.get(j).get(i);
        if (cur.x < vert.size() - 1) {
          cur.right = new Edge(cur, vert.get(j + 1).get(i));
          arr.add(cur.right);
        }
        if (cur.y < vert.get(j).size() - 1) {
          cur.down = new Edge(cur, vert.get(j).get(i + 1));
          arr.add(cur.down);
        }
        if (cur.x > 0) {
          cur.left = new Edge(cur, vert.get(j - 1).get(i));
        }
        if (cur.y > 0) {
          cur.up = new Edge(cur, vert.get(j).get(i - 1));
        }
      }
    }
    return arr;
  }

  // EFFECT: sets all the edge booleans of the vertices to false if they are
  // in the spanning tree
  public void setVerticalEdges(ArrayList<ArrayList<Vertex>> vert) {
    for (int j = 0; j < vert.size(); j++) {
      for (int i = 0; i < vert.get(j).size(); i++) {
        Vertex cur = vert.get(j).get(i);
        if (this.edgesInTree.contains(cur.right)) {
          cur.rightFlag = false;
        }
        if (this.edgesInTree.contains(cur.down)) {
          cur.downFlag = false;
        }
      }
    }
  }

  // EFFECT: sets the vertical neighbors of all the vertices
  public void setVerticalNeighbors(ArrayList<ArrayList<Vertex>> vert) {
    for (int j = 0; j < vert.size(); j++) {
      for (int i = 0; i < vert.get(j).size(); i++) {
        Vertex cur = vert.get(j).get(i);
        if (cur.x == 0 || this.worklist.contains(cur.left)) {
          // don't add anything
        }
        else {
          cur.neighbors.add(vert.get(cur.x - 1).get(cur.y));
        }
        if (cur.x == mazeWidth - 1 || this.worklist.contains(cur.right)) {
          // don't add anything
        }
        else {
          cur.neighbors.add(vert.get(cur.x + 1).get(cur.y));
        }
        if (cur.y == 0 || this.worklist.contains(cur.up)) {
          // don't add anything
        }
        else {
          cur.neighbors.add(vert.get(cur.x).get(cur.y - 1));
        }
        if (cur.y == mazeHeight - 1 || this.worklist.contains(cur.down)) {
          // don't add anything
        }
        else {
          cur.neighbors.add(vert.get(cur.x).get(cur.y + 1));
        }
      }
    }
  }

  // EFFECT: updates the edges of the player's current, right, and down vertices
  public void updatePlayerVertices() {
    if (this.player.currentPos.x == 0) {
      // dont adjust anything
    }
    if (this.player.currentPos.x == mazeWidth) {
      // dont adjust anything
    }
    if (this.player.currentPos.y == 0) {
      // dont adjust anything 
    }
    if (this.player.currentPos.y == mazeHeight) {
      // dont adjust anything 
    }
    else {
      this.player.currentPos.right = new Edge(this.player.currentPos,
          new Vertex(this.player.currentPos.x + 1, this.player.currentPos.y));
      this.player.currentPos.left =
          new Edge(new Vertex(this.player.currentPos.x - 1,
              this.player.currentPos.y), this.player.currentPos);
      this.player.currentPos.up =
          new Edge(new Vertex(this.player.currentPos.x,
              this.player.currentPos.y - 1), this.player.currentPos);
      this.player.currentPos.down = new Edge(this.player.currentPos,
          new Vertex(this.player.currentPos.x, this.player.currentPos.y + 1));
    }
  }

  // EFFECT: sets the weights of edges in the worklist to a random integer
  public void randomWeights(ArrayList<Edge> edges) {
    for (int i = 0; i < edges.size(); i++) {
      edges.get(i).weight = new Random().nextInt(100);
    }
  }

  // finds the representative of the given key
  Vertex find(HashMap<Vertex, Vertex> map, Vertex key) {
    Vertex val = map.get(key);
    while (!map.get(val).equals(val)) {
      val = map.get(val);
    }
    return val;
  }

  // EFFECT: updates the to's to the from's 
  void union(HashMap<Vertex, Vertex> map, Vertex to, Vertex from) {
    map.put(this.find(map, to), this.find(map, from));
  }

  // creates a hash map where each vertex's representative is initialized to itself
  public HashMap<Vertex, Vertex> initReps() {
    HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
    for (int i = 0; i < this.worklist.size(); i++) {
      Vertex to = this.worklist.get(i).to;
      Vertex from = this.worklist.get(i).from;
      map.put(to, to);
      map.put(from, from);
    }
    return map;
  }

  // determines if there is a complete spanning tree
  public boolean isTreeComplete() {
    return (mazeHeight * mazeWidth) - 1 == this.edgesInTree.size();
  }

  // EFFECT: updates the edgesInTree with edges from the worklist 
  public void unionFind() {
    Collections.sort(worklist);
    HashMap<Vertex, Vertex> map = this.initReps();
    int i = 0;
    while (!this.isTreeComplete() && i < this.worklist.size() - 1) {
      Vertex to = this.worklist.get(i).to;
      Vertex from = this.worklist.get(i).from;
      if (this.find(map, to).equals(this.find(map, from))) {
        i = i + 1;
      }
      else {
        this.edgesInTree.add(this.worklist.remove(i));
        this.union(map, to, from);
      }
    }
  }

  // performs search or dfs on this maze depending on whether a stack or
  // a queue is given.
  public HashMap<Vertex, Vertex> search(Collection<Vertex> queue) {
    HashMap<Vertex, Vertex> cameFromEdge = new HashMap<Vertex, Vertex>();
    ICollection<Vertex> worklist = (ICollection<Vertex>) queue;
    worklist.add(this.arrVert.get(0).get(0));
    while (!worklist.isEmpty()) {
      Vertex next = worklist.remove();
      if (next.visited) {
        // don't make any changes
        // because it has already been visited 

      }
      else if (next.equals(this.endPoint)) {
        return cameFromEdge;
      }
      else {
        next.visited = true;
        for (Vertex v : next.neighbors) {
          worklist.add(v);
          cameFromEdge.put(v, next);
        }
      }
    }
    return cameFromEdge;
  }

  // EFFECT: sets this maze with the given values 
  void setMaze() {
    this.arrVert = this.make2Dvertices();
    edgesInTree = new ArrayList<Edge>();
    vertices = new Empty<Vertex>();
    this.worklist = this.initVerticalEdges(this.arrVert);
    this.randomWeights(worklist);
    this.unionFind();
    this.setVerticalEdges(arrVert);
    this.setVerticalNeighbors(this.arrVert);
    this.map = new HashMap<Vertex, Vertex>();
    this.searchPath = new Empty<Vertex>();
    this.player = new Player(this.arrVert.get(0).get(0));
    this.updatePlayerVertices();
    this.endPoint = this.arrVert.get(mazeWidth - 1).get(mazeHeight - 1);
  }

  // EFFECT: moves the player/changes maps/performs search or DFS according to
  // the given key
  public void onKeyEvent(String ke) {
    this.updatePlayerVertices();
    boolean rightWall = this.worklist.contains(this.player.currentPos.right) ||
        (this.player.currentPos.x == mazeWidth - 1);
    boolean leftWall = this.worklist.contains(this.player.currentPos.left) ||
        (this.player.currentPos.x == 0);
    boolean topWall = this.worklist.contains(this.player.currentPos.up) ||
        (this.player.currentPos.y == 0);
    boolean bottomWall = this.worklist.contains(this.player.currentPos.down) ||
        (this.player.currentPos.y == mazeHeight - 1);
    if (ke.equals("right") && !rightWall) {
      this.player.visited = this.player.visited.add(this.player.currentPos);
      this.player.currentPos = new Vertex(this.player.currentPos.x + 1,
          this.player.currentPos.y);
    }
    else if (ke.equals("left") && !leftWall) {
      this.player.visited = this.player.visited.add(this.player.currentPos);
      this.player.currentPos = new Vertex(this.player.currentPos.x - 1,
          this.player.currentPos.y);
    }
    else if (ke.equals("up") && !topWall) {
      this.player.visited = this.player.visited.add(this.player.currentPos);
      this.player.currentPos = new Vertex(this.player.currentPos.x,
          this.player.currentPos.y - 1);
    }
    else if (ke.equals("down") && !bottomWall) {
      this.player.visited = this.player.visited.add(this.player.currentPos);
      this.player.currentPos = new Vertex(this.player.currentPos.x,
          this.player.currentPos.y + 1);
    }
    // to restart the game
    else if (ke.equals("r")) {
      this.setMaze();
    }
    // to perform bfs
    else if (ke.equals("b")) {
      this.setMaze();
      this.searchPath = new Empty<Vertex>();
      this.map = this.search(new PriorityQueue<Vertex>());
      this.key = this.arrVert.get(0).get(0);
    }
    // to perform dfs
    else if (ke.equals("d")) {
      this.setMaze();
      this.searchPath = new Empty<Vertex>();
      this.map = this.search(new Stack<Vertex>());
      this.key = this.arrVert.get(0).get(0);
    }
  }

  // EFFECT: runs the game one per tick
  public void onTick() {
    if (this.map.isEmpty()) {
      // don't do anything
    }
    else {
      this.searchPath = this.searchPath.add(key);
      key = this.map.get(key);
    }
  }

  // draws this maze
  public WorldScene makeScene() {
    WorldScene bg = new WorldScene(Maze.mazeWidth * Maze.cellSize,
        Maze.mazeHeight * Maze.cellSize);
    Utils util = new Utils();
    IList<Vertex> vert = util.convertToList2D(arrVert);
    IListIterator<Vertex> iter = new IListIterator<Vertex>(vert);
    WorldImage player = this.player.drawPlayer();
    IListIterator<Vertex> playerIter =
        new IListIterator<Vertex>(this.player.visited);
    WorldImage endPoint = new RectangleImage(cellSize, cellSize,
        OutlineMode.SOLID, new Color(227, 161, 161));
    IListIterator<Vertex> pathIter = new IListIterator<Vertex>(this.searchPath);
    // draws the endpoint
    bg.placeImageXY(endPoint, (this.endPoint.x + 1) * cellSize -
        cellSize / 2, (this.endPoint.y + 1) * cellSize - cellSize / 2);
    // draws the player's path
    while (playerIter.hasNext()) {
      Vertex cur = playerIter.next();
      Color col = new Color(121, 171, 133);
      WorldImage path = new RectangleImage(cellSize, cellSize,
          OutlineMode.SOLID, col.brighter());
      bg.placeImageXY(path, (cur.x + 1) * cellSize - cellSize / 2,
          (cur.y + 1) * cellSize - cellSize / 2);
    }
    // draws the search
    if (this.map.isEmpty()) {
      // don't draw anything
    }
    else {
      WorldImage path = new RectangleImage(cellSize, cellSize,
          OutlineMode.SOLID, new Color(109, 171, 191));
      bg.placeImageXY(path, (key.x + 1) * cellSize - cellSize / 2,
          (key.y + 1) * cellSize - cellSize / 2);
    }
    // draws the search's path
    while (pathIter.hasNext()) {
      Vertex cur = pathIter.next();
      Color col = new Color(109, 171, 191);
      WorldImage searchPath = new RectangleImage(cellSize, cellSize,
          OutlineMode.SOLID, col.brighter());
      bg.placeImageXY(searchPath, (cur.x + 1) * cellSize - cellSize / 2,
          (cur.y + 1) * cellSize - cellSize / 2);
    }
    // draws the walls
    while (iter.hasNext()) {
      Vertex cur = iter.next();
      if (cur.rightFlag) {
        bg.placeImageXY(cur.drawVertical(), (cur.x + 1) * cellSize,
            (cur.y + 1) * cellSize - cellSize / 2);
      }
      if (cur.downFlag) {
        bg.placeImageXY(cur.drawHorizontal(), (cur.x + 1) * cellSize -
            cellSize / 2, (cur.y + 1) * cellSize );
      }
    }
    // draws the player at the starting currentPos
    bg.placeImageXY(player, (this.player.currentPos.x + 1) * cellSize -
        cellSize / 2, (this.player.currentPos.y + 1) * cellSize -
        cellSize / 2);
    return bg;
  }

  // is the final world once the player has finished the game
  public WorldEnd finalWorld() {
    WorldScene bg = this.makeScene();
    WorldImage win = new TextImage("YOU WON!", mazeHeight * cellSize / 4,
        Color.BLACK);
    if (this.player.currentPos.equals(this.endPoint)) {
      bg.placeImageXY(win, mazeWidth * cellSize / 2,
          mazeHeight * cellSize / 2);
      return new WorldEnd(true, bg);
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

// to represent a list iterator
class IListIterator<T> implements Iterator<T> {
  IList<T> items;

  IListIterator(IList<T> items) {
    this.items = items;
  }

  // checks if the list has a next item
  public boolean hasNext() {
    return this.items.isCons();
  }

  // gets the next item in the list
  // EFFECT: increases the iterator
  public T next() {
    if (!this.hasNext()) {
      throw new IllegalArgumentException();
    }
    Cons<T> itemsAsCons = this.items.asCons();
    T answer = itemsAsCons.first;
    this.items = itemsAsCons.rest;
    return answer;
  }

  // EFFECT: removes the next item
  public void remove() {
    throw new IllegalArgumentException();
  }
}

// to represent utilities
class Utils {
  // converts the given array list into an IList
  <T> IList<T> convertToList(ArrayList<T> alist) {
    IList<T> ans = new Empty<T>();
    for (int i = 0; i < alist.size(); i++) {
      ans = ans.add(alist.get(i));
    }
    return ans;
  }

  // converts the given 2D array list into an IList
  <T> IList<T> convertToList2D(ArrayList<ArrayList<T>> alist) {
    Utils util = new Utils();
    IList<T> ans = new Empty<T>();
    for (int row = 0; row < alist.size(); row++) {
      ans = ans.append(util.convertToList(alist.get(row)));
    }
    return ans;
  }

  // converts a 2D array list into a 1D array list
  <T> ArrayList<T> convertFrom2dTo1d(ArrayList<ArrayList<T>> alist) {
    ArrayList<T> ans = new ArrayList<T>();
    for (int row = 0; row < alist.size(); row++) {
      for (int col = 0; col < alist.get(row).size(); col++) {
        ans.add(alist.get(row).get(col));
      }
    }
    return ans;
  }

  // determines if the given arrayList contains the given item
  <T> boolean arrContains(ArrayList<T> arr, T item) {
    for (int i = 0; i < arr.size(); i++) {
      T cur = arr.get(i);
      if (cur.equals(item)) {
        return true;
      }
    }
    return false;
  }
}

// to represent a list of T
interface IList<T> extends Iterable<T> {
  // calculates the size of this list
  int size();

  // adds the given item to this list
  IList<T> add(T given);

  // appends the given list onto this list
  IList<T> append(IList<T> given);

  // casts this list as a cons, assuming that it can be done
  Cons<T> asCons();

  // determines if this list is a cons 
  boolean isCons();

  // represents an iterator for this list 
  Iterator<T> iterator();
}

// to represent an empty list of T
class Empty<T> implements IList<T> {
  // calculates the size of this list
  public int size() {
    return 0;
  }

  // adds the given item to this list
  public IList<T> add(T given) {
    return new Cons<T>(given, this);
  }

  // appends the given list onto this list
  public IList<T> append(IList<T> given) {
    return given;
  }

  // throws exception because it cannot be represented as a cons
  public Cons<T> asCons() {
    throw new ClassCastException();
  }

  // determines if this empty list is a cons
  public boolean isCons() {
    return false;
  }

  // represents an iterator for this list
  public Iterator<T> iterator() {
    return new IListIterator<T>(this);
  }
}

// to represent a cons list of T
class Cons<T> implements IList<T> {
  T first;
  IList<T> rest;

  Cons(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  // returns the size of this list
  public int size() {
    return 1 + this.rest.size();
  }

  // adds the given item to this list
  public IList<T> add(T given) {
    return this.append(new Cons<T>(given, new Empty<T>()));
  }

  // appends the given list onto this list
  public IList<T> append(IList<T> given) {
    return new Cons<T>(this.first, this.rest.append(given));
  }

  // casts this list as a cons
  public Cons<T> asCons() {
    return this;
  }

  // determines if this list is a cons
  public boolean isCons() {
    return true;
  }

  // creates an iterator for this list
  public Iterator<T> iterator() {
    return new IListIterator<T>(this);
  }
}

// represents the examples 
class ExamplesMaze {
  Maze world;



  ArrayList<Edge> edges = new ArrayList<Edge>();
  Utils util = new Utils();
  HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
  Player p1 = new Player(new Vertex(0, 0));

  Vertex v = new Vertex(0, 0);
  Vertex v2 = new Vertex(1, 0);
  Vertex v3 = new Vertex(2, 0);
  Vertex v4 = new Vertex(0, 1);
  Vertex v5 = new Vertex(1, 1);
  Vertex v6 = new Vertex(2, 1);

  Edge vv2 = new Edge(this.v, this.v2, 5);
  Edge v2v3 = new Edge(this.v2, this.v3, 3);
  Edge v4v5 = new Edge(this.v4, this.v5, 10);
  Edge v5v6 = new Edge(this.v5, this.v6, 12);
  Edge vv4 = new Edge(this.v, this.v4, 15);
  Edge v2v5 = new Edge(this.v2, this.v5, 20);
  Edge v3v6 = new Edge(this.v3, this.v6, 4);

  ArrayList<Edge> arrEdge1 = new ArrayList<Edge>();

  ArrayList<ArrayList<Vertex>> arrVertex = new ArrayList<ArrayList<Vertex>>();

  IList<Edge> mtEdge = new Empty<Edge>();
  IList<Edge> loe1 = new Cons<Edge>(this.vv2, new Cons<Edge>(this.v2v3,
      this.mtEdge));

  // to initialize the data for tests
  void initData() {
    this.world = new Maze();

    this.v = new Vertex(0, 0);
    this.v2 = new Vertex(1, 0);
    this.v3 = new Vertex(2, 0);
    this.v4 = new Vertex(0, 1);
    this.v5 = new Vertex(1, 1);
    this.v6 = new Vertex(2, 1);

    this.map = new HashMap<Vertex, Vertex>();
    map.put(this.v, this.v);
    map.put(this.v2, this.v);
    map.put(this.v3, this.v2);
    map.put(this.v5, this.v6);
    map.put(this.v6, this.v6);

    this.arrVertex = new ArrayList<ArrayList<Vertex>>();
    arrVertex.add(0, new ArrayList<Vertex>());
    arrVertex.add(1, new ArrayList<Vertex>());
    arrVertex.add(2, new ArrayList<Vertex>());
    arrVertex.get(0).add(this.v);
    arrVertex.get(0).add(this.v4);
    arrVertex.get(1).add(this.v2);
    arrVertex.get(1).add(this.v5);
    arrVertex.get(2).add(this.v3);
    arrVertex.get(2).add(this.v6);

    this.vv2 = new Edge(this.v, this.v2, 5);
    this.v2v3 = new Edge(this.v2, this.v3, 3);
    this.v4v5 = new Edge(this.v4, this.v5, 10);
    this.v5v6 = new Edge(this.v5, this.v6, 12);
    this.vv4 = new Edge(this.v, this.v4, 15);
    this.v2v5 = new Edge(this.v2, this.v5, 20);
    this.v3v6 = new Edge(this.v3, this.v6, 4);

    this.arrEdge1 = new ArrayList<Edge>();
  }

  // to test the method drawPlayer
  void testDrawPlayer(Tester t) {
    initData();
    t.checkExpect(this.p1.drawPlayer(),
        new RectangleImage(Maze.cellSize, Maze.cellSize,
            OutlineMode.SOLID, new Color(115, 171, 133)));
  }

  // to test the method equals for vertex
  void testVertexEqual(Tester t) {
    initData();
    t.checkExpect(this.v.equals(this.v), true);
    t.checkExpect(this.v5.equals(this.v5), true);
    t.checkExpect(this.v.equals(this.v4), false);
    t.checkExpect(this.v5.equals(this.v3), false);
  }

  // to test the method hashCode for vertex
  void testhashCode(Tester t) {
    initData();
    t.checkExpect(this.v.hashCode(), 0);
    t.checkExpect(this.v5.hashCode(), 10000);
  }

  // to test the method drawVertical
  void testDrawVertical(Tester t) {
    initData();
    t.checkExpect(this.v.drawVertical(), new RectangleImage(1,
        Maze.cellSize, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(this.v2.drawVertical(), new RectangleImage(1,
        Maze.cellSize, OutlineMode.SOLID, Color.BLACK));
  }

  // to test the method drawHorizontal
  void testDrawHorizontal(Tester t) {
    initData();
    t.checkExpect(this.v.drawHorizontal(), new RectangleImage(Maze.cellSize,
        1, OutlineMode.SOLID, Color.BLACK));
    t.checkExpect(this.v2.drawHorizontal(), new RectangleImage(Maze.cellSize,
        1, OutlineMode.SOLID, Color.BLACK));
  }

  // to test the method equals for edge
  void testEdgeEqual(Tester t) {
    initData();
    t.checkExpect(this.vv4.equals(this.vv4), true);
    t.checkExpect(this.vv2.equals(this.vv4), false);
  }

  // to test the method hashCode for edge
  void testEdgeHashCode(Tester t) {
    initData();
    t.checkExpect(this.vv4.hashCode(), 0);
    t.checkExpect(this.v5v6.hashCode(), -1454759936);
  }

  // to test the method compareTo for edge
  void testEdgeCompareTo(Tester t) {
    initData();
    t.checkExpect(this.vv4.compareTo(this.v4v5), 5);
    t.checkExpect(this.vv2.compareTo(this.v3v6), 1);
  }

  // to test the method make2D vertices
  void testmake2Dvertices(Tester t) {
    initData();
    ArrayList<ArrayList<Vertex>> arr = this.world.make2Dvertices();
    t.checkExpect(arr.get(0).get(1), new Vertex(0, 1));
    t.checkExpect(arr.get(5).get(7), new Vertex(5, 7));
  }



  // to test the method updatePlayerVertices
  void testupdatePlayerVertices(Tester t) {
    initData();
    world.player = new Player(new Vertex(5, 5));
    world.updatePlayerVertices();
    t.checkExpect(world.player.currentPos.right, new Edge(world.player.currentPos,
        new Vertex(6, 5)));
    t.checkExpect(world.player.currentPos.left, new Edge(new Vertex(4, 5),
        world.player.currentPos));
    t.checkExpect(world.player.currentPos.up, new Edge(new Vertex(5, 4),
        world.player.currentPos));
    t.checkExpect(world.player.currentPos.down, new Edge(world.player.currentPos,
        new Vertex(5, 6)));
  }


  // to test the method initReps
  void testInitReps(Tester t) {
    initData();
    ArrayList<Edge> arr = new ArrayList<Edge>();
    arr.add(this.vv4);
    arr.add(this.v4v5);
    arr.add(this.v5v6);
    world.worklist = arr;
    HashMap<Vertex, Vertex> map = world.initReps();
    t.checkExpect(map.get(world.worklist.get(0).from), this.v);
    t.checkExpect(map.get(world.worklist.get(0).to), this.v4);
  }


  // to test the method find
  void testFind(Tester t) {
    initData();
    t.checkExpect(this.world.find(map, this.v3), this.v);
    t.checkExpect(this.world.find(map, this.v), this.v);
    t.checkExpect(this.world.find(map,  this.v5), this.v6);
  }

  // tests initvertical edges
  void testinitVerticalEdges(Tester t) {
    initData();
    world.initVerticalEdges(this.arrVertex);
    t.checkExpect(this.v.down, new Edge(this.v, this.v4));
    t.checkExpect(this.v.right, new Edge(this.v, this.v2));
    t.checkExpect(this.v3.right, null);
  }
  
  // tests union
  void testUnion(Tester t) {
    initData();
    this.world.union(map, this.v5, this.v3);
    t.checkExpect(map.get(this.v6), this.v);
  }


  // tests iterator
  void testIterator(Tester t) {
    initData();
    t.checkExpect(this.mtEdge.iterator(), new IListIterator<Edge>(this.mtEdge));
    t.checkExpect(this.loe1.iterator(), new IListIterator<Edge>(this.loe1));
  } 


  // test tree complete
  void testIsTreeComplete(Tester t) {
    initData();
    world.edgesInTree = new ArrayList<Edge>();
    t.checkExpect(world.isTreeComplete(), false);
    world.setMaze();
    t.checkExpect(world.isTreeComplete(), true);
  }

  // test setMaze method
  void testsetMaze(Tester t) {
    initData();
    world.setMaze();
    t.checkExpect(this.world.edgesInTree.size(), Maze.mazeHeight *
        Maze.mazeWidth - 1);
    t.checkExpect(this.world.worklist.size() < this.world.edgesInTree.size(),
        true);
    t.checkExpect(this.world.worklist.contains(this.world.edgesInTree.get(0)),
        false);
  }

  // test add method
  void testAdd(Tester t) {
    initData();
    t.checkExpect(this.mtEdge.add(this.vv4),
        new Cons<Edge>(this.vv4, this.mtEdge));
    t.checkExpect(this.loe1.add(this.v4v5), new Cons<Edge>(this.vv2,
        new Cons<Edge>(this.v2v3, new Cons<Edge>(this.v4v5,
            this.mtEdge))));
  }

  // tests size method
  void testSize(Tester t) {
    initData();
    t.checkExpect(this.mtEdge.size(), 0);
    t.checkExpect(this.loe1.size(), 2);
  }


  // tests append method
  void testAppend(Tester t) {
    initData();
    t.checkExpect(this.mtEdge.append(this.loe1), this.loe1);
    t.checkExpect(this.loe1.append(this.mtEdge), this.loe1);
    t.checkExpect(this.loe1.append(this.loe1), new Cons<Edge>(this.vv2,
        new Cons<Edge>(this.v2v3, new Cons<Edge>(this.vv2,
            new Cons<Edge>(this.v2v3, this.mtEdge)))));
  }

  // tests is cons method
  void testIsCons(Tester t) {
    initData();
    t.checkExpect(this.mtEdge.isCons(), false);
    t.checkExpect(this.loe1.isCons(), true);
  }

  // tests ascons method
  void testAsCons(Tester t) {
    initData();
    t.checkException(new IllegalArgumentException(), this.mtEdge, "asCons");
    t.checkExpect(this.loe1.asCons(), this.loe1);
  }

  //to test the game
  void testGame(Tester t) {
    world = new Maze();
    world.bigBang(Maze.mazeWidth * Maze.cellSize, Maze.mazeHeight
        * Maze.cellSize, .01);
  }


}
