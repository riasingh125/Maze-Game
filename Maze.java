
import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


interface Nodes {

}

class EmptyVertex implements Nodes{
}


class Vertexx implements Nodes{
  Posn posn;
  Nodes top;
  Nodes bottom;
  Nodes left;
  Nodes right;
  int row;
  int col;

  Vertexx(Posn posn){
    this.posn = posn;
  }
  
  Vertexx(){
    
  }

  Vertexx(int row, int col){
    this.row = row;
    this.col = col;
  }

  Vertexx(Vertexx top, Vertexx bottom, Vertexx left, Vertexx right){
    this.top = top;
    this.bottom = bottom;
    this.left = left;
    this.right = right;
  }

  public int hashCode() {
    return (this.row + this.col) * 100;
  }
  public WorldImage drawVertix() {
    return new RectangleImage(100,100, OutlineMode.OUTLINE, Color.BLUE);
  }
  
  public boolean equals(Object anot) {
    if(anot instanceof Vertexx) {
    Vertexx anott = (Vertexx) anot;
     return this.row ==  anott.row 
          && this.col ==  anott.col;
    }
    else {
     return 1 == 2;
    }
  }
  
  public WorldImage person(Color color) {
    return new RectangleImage(10, 10, OutlineMode.SOLID, color);
  }
}


class Edges{
  Vertexx from;
  Vertexx to;
  int weight;
  Random rand;

  Edges(int weight){
    this.weight = new Random().nextInt(500);
  }

  Edges(Vertexx from, Vertexx to){
    this.from = from;
    this.to=to;
    this.weight = new Random().nextInt(500);
  }

  WorldImage drawUpDown() {
    return new RectangleImage(7, 9, OutlineMode.SOLID, Color.BLACK);
  }
  
  WorldImage drawSideSide() {
    return new RectangleImage(9,7, OutlineMode.SOLID, Color.BLACK);
    
  }



//  public int compareTo(Edges edge) {
//    return this.weight - edge.weight;
//  }



}

class Weights implements Comparator<Edges>{


  public int compare(Edges o1, Edges o2) {
    return o1.weight - o2.weight; //FIX!!!
  }
  
}

class MazeWorld extends World{
  ArrayList<ArrayList<Vertexx>> vertices;
  ArrayList<Edges> edges;
  ArrayList<Edges> newEdge = new ArrayList<Edges>();;
  int height;
  int width;
  HashMap<Vertexx, Vertexx> hash= new HashMap<Vertexx, Vertexx>();
 // boolean complete;

  MazeWorld(int height, int width){
    this.height = height;
    this.width = width;
    this.vertices = mazeVerticies();
    this.edges = mazeEdges(this.vertices);
  }

  //draws the maze

  public WorldScene makeScene() {
    WorldScene background = new WorldScene(height * 12 + 6, width * 12 + 6);

    for (int i = 0; i < this.height; i++) {
      for(int j = 0; j < this.width; j++) {
      background.placeImageXY(vertices.get(i).get(j).drawVertix(), i * 10 + 5, j * 10 + 5);
      if (vertices.get(i).get(j).right != new EmptyVertex()) {
          background.placeImageXY(edges.get(i).drawSideSide(), edges.get(i).from.col , edges.get(i).to.col);
      }
      if (vertices.get(i).get(j).bottom != new EmptyVertex()){
          background.placeImageXY(edges.get(i).drawUpDown(), edges.get(i).from.row, edges.get(i).to.row);
    }

    }
  }
    background.placeImageXY(vertices.get(0).get(0).person(Color.GREEN), 0, 0);
    background.placeImageXY(vertices.get(59).get(29).person(Color.PINK), this.height, this.width);
    return background;
  }



  //makes 2D array of vertices

  public ArrayList<ArrayList<Vertexx>> mazeVerticies(){
    ArrayList<ArrayList<Vertexx>> board = new ArrayList<ArrayList<Vertexx>>();
    for(int row = 0; row < this.height; row++) {
      ArrayList<Vertexx> vertexx = new ArrayList<Vertexx>();
      for(int col = 0; col < this.width; col++) {
        vertexx.add(new Vertexx(row,col));
      }
      board.add(vertexx);
    }
    return board;
  }

  //i guess it only draws it right and bottom 
  public ArrayList<Edges> mazeEdges(ArrayList<ArrayList<Vertexx>> vertix){
    ArrayList<Edges> egde = new ArrayList<Edges>();
    for(int row = 0; row < this.height; row++) {
      for(int col = 0; col < this.width; col++) {
        Vertexx ver = vertix.get(row).get(col);
        if(ver.row < this.height - 1) {
          Edges oopsie = new Edges(ver, vertix.get(row + 1).get(col));
          egde.add(oopsie);
        }
        if (ver.col < this.width - 1) {
          Edges poopsie = new Edges(ver, vertix.get(row).get(col  + 1));
          egde.add(poopsie);
        }

      }
    }
    egde.sort(new Weights());
    return egde;
  }


//  public void weightOfEdge() {
//    for (Edges e: edges) {
//      e.weight= new Random().nextInt(500);
//      System.out.println(e.weight);
//    }
//  }

  public void settingNeighs() {
    for (int row = 0; row < vertices.size(); row++) {
      for (int col = 0; col < vertices.size(); col++) {
        if (col == 0) {
          vertices.get(row).get(col).left = new EmptyVertex();
        }
          else  {
            vertices.get(row).get(col).left = vertices.get(row).get(col - 1);
          }

          if (col == vertices.size() - 1) {
            vertices.get(row).get(col).right = new EmptyVertex();
          }
          else {
            vertices.get(row).get(col).right = vertices.get(row).get(col + 1);
          }
          if (row == 0) {
            vertices.get(row).get(col).top = new EmptyVertex();
          } 
          else {
            vertices.get(row).get(col).top = vertices.get(row - 1).get(col);
          }
          if (row == vertices.size() - 1) {
            vertices.get(row).get(col).bottom = new EmptyVertex();
          } 
          else {
            vertices.get(row).get(col).bottom = vertices.get(row + 1).get(col);
          }
        }
      }

    }
  
  public HashMap<Vertexx, Vertexx> hashEquals(){
    for(int i = 0; i < this.edges.size(); i++) {
      Vertexx to = this.edges.get(i).to;
      Vertexx from = this.edges.get(i).from;
      hash.put(to, to);
      hash.put(from, from);
    }
//    for (int key = 0; key < vertices.size(); key++) {
//      for(int other = 0; other < vertices.size(); other++) {
//      Vertexx keyt = vertices.get(key).get(other);
//      hash.put(keyt, keyt);
//    }
//    }
    
    return hash;
  }
  
//  public void newEdges() {
//    for(int i = 0; i < edges.size(); i++) {
//      newEdge.add(edges.remove(i));
//    }
//  }
  
//  public ArrayList<Edges> findKey(Vertexx thit, HashMap<Vertexx, Vertexx> that) {
//   while(!complete) {
//   for(int i = 0; i < vertices.size(); i++) {
//    Vertexx from = edges.get(i).from;
//     Vertexx to = edges.get(i).to;
//     
//     if(hash.get(from) != hash.get(to)) {
//       Vertexx thank = hash.get(from);
//       newEdge.add(edges.get(i));
//       thank = hash.get(to);
//       
//     }
//     
//
//   }
//  }
//   return newEdge;
//  }
//  
  
  public Vertexx find(Vertexx key) {
    Vertexx value = hash.get(key);
    while(!hash.get(value).equals(value)) {
      value = hash.get(value);   
    }
    return value;
  }
  
  //if (!hash.get(value).equals(value)) {
  //value = hash.get(value);  
  //hash.find(key)
  
  public void union(Vertexx to, Vertexx from) {
    hash.put(this.find(to), this.find(from));
  }
  
  public ArrayList<Edges> unionFind() {
    int totVert = 0;
    
    while(!isComplete() && !(totVert == this.edges.size() - 1)) {
      Vertexx to = this.edges.get(totVert).to;
      Vertexx from = this.edges.get(totVert).from;
      
      if(this.find(to).equals(this.find(from))) {
        totVert++;
      }
      else {
        this.newEdge.add(this.edges.remove(totVert));
        this.union(to, from);
      }
    }
    return newEdge;
  }
  public boolean isComplete() {
    return this.vertices.size() - 1 == this.newEdge.size();
  }

 
  
  
  

  

}



class ExamplesMazes{
  
  MazeWorld world1;
  MazeWorld world2;
  MazeWorld world3;
  MazeWorld world4;
  
  MazeWorld world1new;
  MazeWorld world2new;
  MazeWorld world3new;
  MazeWorld world4new;
  
  Vertexx v1;
  Vertexx v2;
  Vertexx v3;
  Vertexx v4;
  Vertexx v5;
  Vertexx v6;
  Vertexx v7;
  Vertexx v8;
  Vertexx vx1;
  Vertexx vx2;
  Vertexx vx3;
  Vertexx vx4;
  Vertexx vx5;
  Vertexx vx6;
  Vertexx vx7;
  Vertexx vx8;
  
  Edges e1;
  Edges e2;
  Edges e3;
  Edges e4;
  Edges e5;
  Edges e6;
  Edges e7;
  
  ArrayList<Vertexx> list1;
  ArrayList<Vertexx> list2;
  ArrayList<Vertexx> list3;
  
  ArrayList<ArrayList<Vertexx>> board1;
  ArrayList<ArrayList<Vertexx>> board2;
  ArrayList<ArrayList<Vertexx>> mtboard;
  
  Vertexx vnew1;
  Vertexx vnew2;
  Vertexx vnew3;
  Vertexx vnew4;
  Vertexx vnew5;
  Vertexx vnew6;
  Vertexx vnew7;
  Vertexx  vnew8;
  Vertexx vnew9;
  ArrayList<Vertexx>  listnew1;
  ArrayList<Vertexx>  listnew2;
  ArrayList<Vertexx>  listnew3;
  ArrayList<Vertexx>  anslist;
  ArrayList<Vertexx>  anslist2;
  
  
  void initMaze() {
    
    world1 = new MazeWorld(2,4);
    world2 = new MazeWorld(2,2);
    world3 = new MazeWorld(5,3);
    world4 = new MazeWorld(10,20);
    
    v1 = new Vertexx(0, 0);
    v2 = new Vertexx(1, 1);
    v3 = new Vertexx(2, 1);
    v4 = new Vertexx(5, 4);
    v5 = new Vertexx(3, 3);
    v6 = new Vertexx(2, 3);
    v7 = new Vertexx(9, 10);
    
    vx1 = new Vertexx(0, 0);
    vx2 = new Vertexx(0, 1);
    vx3 = new Vertexx(1, 0);
    vx4 = new Vertexx(1, 1);
    
    
    v1 = new Vertexx(0, 0);
    v2 = new Vertexx(1, 1);
    v3 = new Vertexx(2, 1);
    v4 = new Vertexx(5, 4);
    v5 = new Vertexx(3, 3);
    v6 = new Vertexx(2, 3);
    v7 = new Vertexx(9, 10);
    
    vnew1 = new Vertexx(0, 0);
    vnew2 = new Vertexx(0, 1);
    vnew3 = new Vertexx(0, 2);
    vnew4 = new Vertexx(1, 0);
    vnew5 = new Vertexx(1, 1);
    vnew6 = new Vertexx(1, 2);
    vnew7 = new Vertexx(2, 0);
    vnew8 = new Vertexx(2, 1);
    vnew9 = new Vertexx(2, 2);
    
    
    
    e1 = new Edges(v1, v2);
    e2 = new Edges(v2, v3);
    e3 = new Edges(v3, v4);
    e4 = new Edges(v1, v2);
    e5 = new Edges(v1, v2);
    e6 = new Edges(v1, v2);
    e7 = new Edges(v1, v2);
    
    
    world1new = new MazeWorld(1,1);
    world2new = new MazeWorld(0,0);
    world3new = new MazeWorld(2,2);

    anslist = new ArrayList<Vertexx>();
    list1 = new ArrayList<Vertexx>();
    list2 = new ArrayList<Vertexx>();
    list3 = new ArrayList<Vertexx>();
    
    listnew1 = new ArrayList<Vertexx>();
    listnew2 = new ArrayList<Vertexx>();
    listnew3 = new ArrayList<Vertexx>();
    
    anslist2 = new ArrayList<Vertexx>();
    
    
    listnew1.add(vnew1);
    listnew1.add(vnew2);
    listnew1.add(vnew3);
    
    listnew2.add(vnew4);
    listnew2.add(vnew5);
    listnew2.add(vnew6);
    listnew3.add(vnew7);
    listnew3.add(vnew8);
    listnew3.add(vnew9);
    
    
    list1.add(vx1);
    list1.add(vx2);
    list2.add(vx3);
    list2.add(vx4);
   
    board2 = new ArrayList<ArrayList<Vertexx>>();
    board1 = new ArrayList<ArrayList<Vertexx>>();
    mtboard = new ArrayList<ArrayList<Vertexx>>();
    
    board1.add(list1);
    board1.add(list2);
    
    board2.add(listnew1);
    board2.add(listnew2);
    board2.add(listnew3);
    

    world1.vertices = board1;
    world2.vertices = (mtboard);
    
    
    world3.vertices = board2;
    
    
  }
  
//  public WorldImage drawVertix() {
//    return new RectangleImage(100,100, OutlineMode.OUTLINE, Color.BLUE);
//  }
  
  

  void testBigBang(Tester t) {
    MazeWorld flood = new MazeWorld(60, 30);

    flood.bigBang(20 * flood.height, 20 * flood.width, 1);

  }
  
//  WorldImage drawUpDown() {
//    return new RectangleImage(7, 9, OutlineMode.SOLID, Color.BLACK);
//  }
//  
//  WorldImage drawSideSide() {
//    return new RectangleImage(9,7, OutlineMode.SOLID, Color.BLACK);
//    
//  }
  
  void testDrawSideSide() {
    
    
    
    
  }
  
  void testMakeScene(Tester t) {
    initMaze();
 
    WorldScene scene1 = new WorldScene(0, 0);
    WorldScene scene2 = new WorldScene(2,2);
    scene2.placeImageXY(board2, 2, 2);
    t.checkExpect(world1.makeScene(), scene1);
    t.checkExpect(world2.makeScene(), scene2);
  }
  


  void testMazeEdges(Tester t) {
    initMaze();
    ArrayList<ArrayList<Vertexx>> correctAns = new ArrayList<ArrayList<Vertexx>>();
    ArrayList<ArrayList<Vertexx>> correctAns2 = new ArrayList<ArrayList<Vertexx>>();
    anslist2.add(vnew3);
    anslist2.add(vnew6);
    anslist2.add(vnew9);
    correctAns2.add(anslist2);
    
    t.checkExpect(world2.mazeEdges(board1), correctAns);
    t.checkExpect(world1.mazeEdges(board1), correctAns);
    t.checkExpect(world3.mazeEdges(board2), correctAns2);
    
    
  }


  void testmazeVerticies(Tester t) {
    initMaze();
    ArrayList<ArrayList<Vertexx>> correctAns = new ArrayList<ArrayList<Vertexx>>();
    ArrayList<ArrayList<Vertexx>> correctAns2 = new ArrayList<ArrayList<Vertexx>>();
    
    anslist.add(vnew6);
    correctAns2.add(anslist);
    
    t.checkExpect(world2.mazeVerticies(), correctAns);
    t.checkExpect(world1.mazeVerticies(), correctAns2);
    
    t.checkExpect(world3.mazeVerticies(), anslist);
   

  }

}
