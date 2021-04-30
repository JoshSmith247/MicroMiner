import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MicroMiner extends PApplet {

/*
* --- MicroMiner ---
* A tiny game where a miner collects as many resources as possible!
* 
* Processing Documentation used
* 
* @author Josh Smith
*/

public static Player player;
public static World world;

public static PImage miner;

public static PImage minerswing2;
public static PImage minerswing3;
public static PImage minerwalk2;
public static PImage minerwalk3;

String saveName = "save.json";
public boolean saveExists = false;

int saveTimer = 0;

public void setup() {
  
  
  text("Delete '/data/saves/"+saveName+"' to generate new world.", 15, height-20);
  text("LOADING", width/2-15, height/2);
  
  miner = loadImage("Miner.png"); // Load player asset
  
  minerwalk2 = loadImage("Miner-walk2.png"); // Load anim frames
  minerwalk3 = loadImage("Miner-walk3.png");
  minerswing2 = loadImage("Miner-swing2.png");
  minerswing3 = loadImage("Miner-swing3.png");
  
  player = new Player();
  
  if (dataFile("saves/"+saveName).exists()) {
    saveExists = true;
    world = new World(false);
    
    loadGame(saveName);
  } else {
    world = new World(true);
  }
  
  saveGame(saveName);
}

public void draw() {
  if (player.isAlive == false) {
    delay(1000);
    player.x = width/2;
    player.y = 80;
    player.score = 0;
    player.isAlive = true;
    
    return;
  }
  
  clear();
  
  // User Input
  if (mousePressed == true && millis() >= player.mineTimer + 100) {
    player.mineTimer = millis();
    player.mine();
  }
  
  if (keyPressed) { // Player Movement
    if (key == 'a') {
      player.xvelocity--;
      
      if (millis() >= player.walkTimer + 50) { // Animation frame
        player.swingAnim++;
        player.walkTimer = millis();
      }
    } else if (key == 'd') {
      player.xvelocity++;
      
      if (millis() >= player.walkTimer + 50) {
        player.swingAnim++;
        player.walkTimer = millis();
      }
    }
    
    if (player.swingAnim > 3) player.swingAnim = 1;
    
    if (key == 32 && world.blocks[player.y / blockSize + 8][player.x / blockSize + 2].type.equals("Air") == false) { // Space
      player.yvelocity = -20;
    }
  }
  
  player.yvelocity += 1; // Gravity
  
  // Colliders
  if (world.blocks[player.y / blockSize + 8][player.x / blockSize + 2].type.equals("Air") == false || world.blocks[player.y / blockSize + 8][player.x / blockSize + 4].type.equals("Air") == false) {
    if (player.yvelocity > 0) player.yvelocity = 0;
    
    if (player.xvelocity > 0.1f) { // Friction
       player.xvelocity -= 0.1f;
     } else if (player.xvelocity < -0.1f) {
       player.xvelocity += 0.1f;
     }
     
     // Death by colliding with trap
     if (world.blocks[player.y / blockSize + 8][player.x / blockSize + 2].type.equals("Trap") == true || world.blocks[player.y / blockSize + 8][player.x / blockSize + 4].type.equals("Trap") == true) {
       player.isAlive = false;
     }
  }
  if (world.blocks[player.y / blockSize + 6][player.x / blockSize + 5].type.equals("Air") == false || world.blocks[player.y / blockSize][player.x / blockSize + 5].type.equals("Air") == false) {
    player.xvelocity = 0;
    player.x -= 1;
  }
  if (world.blocks[player.y / blockSize + 6][player.x / blockSize].type.equals("Air") == false || world.blocks[player.y / blockSize][player.x / blockSize].type.equals("Air") == false) {
    player.xvelocity = 0;
    player.x += 1;
  }
  
  player.x += player.xvelocity / 10;
  player.y += player.yvelocity / 10;
  
  // Player Display
  if (player.xvelocity < 0) {
    player.facingRight = false;
  } else {
    player.facingRight = true;
  }
  
  // World Boundaries
  if (player.x < width/2) player.x = width/2;
  if (player.y < 0) player.y = 0;
  if (player.x > size[0] * blockSize - width/2) player.x = size[0] * blockSize - width/2;
  if (player.y > size[1] * blockSize) player.y = size[1] * blockSize;
  
  int[] offset = { player.x - width/2, player.y - height/2 };
  if (offset[0] < 0) offset[0] = 0;
  if (offset[1] < 0) offset[1] = 0;
  
  // Render Blocks
  for (int i = offset[1] / blockSize; i < floor(width/blockSize)+1+offset[1] / blockSize; i++) {
    for (int j = offset[0] / blockSize; j < floor(height/blockSize)+1+offset[0] / blockSize; j++) {
      fill(world.blocks[i][j].rgb);
      noStroke();
      
      rect(j * blockSize - offset[0], i * blockSize - offset[1], blockSize, blockSize);
    }
  }
  
   // Render UI
   fill(color(0, 255, 0));
   text("Score: "+player.score, 20, 20);
   
   // Render Player
   int w, x2;
   w = miner.width;
   x2 = player.x - offset[0];
   
   PImage minerImage = miner; // Animation Frame (Player Image)
   if (player.swingAnim == 1) {
     minerImage = minerwalk2;
   } else if (player.swingAnim == 2) {
     minerImage = miner;
   } else if (player.swingAnim == 3) {
     minerImage = minerwalk3;
   }
   
   if (player.swingAnim != 1) {
     if (player.swingAnim == 2) {
       minerImage = minerswing2;
     } else if (player.swingAnim == 3) {
       minerImage = miner;
     } else if (player.swingAnim == 4) {
       minerImage = minerswing3;
     }
   }
   
   if (player.facingRight == true) {
     scale(-1, 1);
     w *= -1;
     x2 *= -1;
   }
   
   image(minerImage, x2, player.y - offset[1], w, miner.height);
   
   if (minute() >= saveTimer + 1) { // Save Game
     saveGame(saveName);
     saveTimer = minute();
     println("Saving Game...");
   }
}

 public void saveGame(String fileName) { // Create a JSON Object corresponding to world array and save to file
    JSONObject saveJSON = new JSONObject();
    
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        JSONObject locJSON = new JSONObject();
        locJSON.setString("type", world.blocks[i][j].type);
        
        JSONObject saveJSON2;
        if (saveJSON.getJSONObject(String.valueOf(i)) != null) {
          saveJSON2 = saveJSON.getJSONObject(String.valueOf(i));
        } else {
          saveJSON2 = new JSONObject();
        }
        
        saveJSON2.setJSONObject(String.valueOf(j), locJSON);
        saveJSON.setJSONObject(String.valueOf(i), saveJSON2);
      }
    }
    
    saveJSONObject(saveJSON, "data/saves/"+fileName);
  }
  
  public void loadGame(String fileName) { // Parse save file
    JSONObject saveJSON = loadJSONObject("saves/"+fileName);
    
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        JSONObject saveData = saveJSON.getJSONObject(String.valueOf(i)).getJSONObject(String.valueOf(j));
        
        world.blocks[i][j] = new Block(saveData.getString("type"));
      }
    }
  }
/*
* MicroMiner
* Block Class
* 
* @author Josh Smith
*/

public static HashMap<String, JSONObject> assets = new HashMap<String, JSONObject>();
public static int blockSize = 8;

class Block { // Block Class
  String type;
  JSONObject data;
  int rgb;
  int value;
  
  Block(String type) {
    this.type = type;
    this.data = getAsset(type);
    this.value = data.getInt("value");
    
    JSONObject colorData = data.getJSONObject("color");
    this.rgb = color(colorData.getInt("r"), colorData.getInt("g"), colorData.getInt("b"));
  }
}

public JSONObject getAsset(String name) {
  if (assets.get(name) == null) {
    assets.put(name, loadJSONObject(name+".json"));
  }
  
  JSONObject asset = assets.get(name);
  return asset;
}
/*
* MicroMiner
* Player Class
* 
* @author Josh Smith
*/


class Player { // Player Class
  boolean isAlive; // Declare Vars
  int score;
  int x, y;
  double xvelocity, yvelocity;
  boolean xchange, ychange;
  boolean facingRight;
  int mineTimer;
  int walkAnim;
  int walkTimer;
  int swingAnim;
  
  
  Player() { // Set starting player values
    this.isAlive = false;
    this.score = 0;
    this.x = width/2;
    this.y = 0;
    this.xvelocity = 0;
    this.yvelocity = 0;
    this.xchange = true;
    this.ychange = true;
    this.facingRight = true;
    this.mineTimer = 0;
    this.walkAnim = 1;
    this.walkTimer = 0;
    this.swingAnim = 1;
  }
  
  public void mine() { // Locates and breaks blocks
    int[] offset = { player.x - width/2, player.y - height/2 };
    
    float difY = player.y - offset[1] - mouseY;
    float difX = player.x - offset[0] - mouseX;
    
    float slope = abs(difY / difX);
    
    Block thisBlock = new Block("Air");
    float[] loc = { player.x, player.y };
    
    while (thisBlock.type.equals("Air")) { // Get the closest block (not air)
      if (difX < 0) {
        loc[0] += 0.25f;
      } else {
        loc[0] -= 0.25f;
      }
      if (difY < 0) {
        loc[1] += slope / 4;
      } else {
        loc[1] -= slope / 4;
      }
      
      try {
        thisBlock = MicroMiner.world.blocks[floor(loc[1] / blockSize)][floor(loc[0] / blockSize)];
      } catch (ArrayIndexOutOfBoundsException e) {
        return;
      } catch (ArithmeticException e) {
        return;
      }
    }
    
    player.score += MicroMiner.world.blocks[floor((loc[1]) / blockSize)][floor((loc[0]) / blockSize)].value;
    MicroMiner.world.blocks[floor((loc[1]) / blockSize)][floor((loc[0]) / blockSize)] = new Block("Air");
    
    swingAnim++;
    if (swingAnim > 3) swingAnim = 1;
  }
}
/*
* MicroMiner
* World Class
* 
* @author Josh Smith
*/

public static int[] size = { 500, 500 };

class World { // World Class
  Block[][] blocks = new Block[size[0]][size[1]];
  
  World(boolean generate) {
    
    if (generate == true) {
      println("Generating new world...");
      for (int i = 0; i < size[0]; i++) {
        for (int j = 0; j < size[1]; j++) {
          blocks[i][j] = new Block("Stone");
        }
      }
      
      distributeOre(1000); // Distribute different sizes of ore veins
      distributeOre(5000);
      
      for (int i = 0; i < size[0]; i++) {
        for (int j = 0; j < size[1]; j++) {
          // Air pockets
          float nsize = 0.05f;
          int ndetail = 2;
          noiseDetail(ndetail);
          float noiseValue = noise(i*nsize, j*nsize);
          if (noiseValue > 0.5f) blocks[i][j] = new Block("Air");
          
          // Form Air
          if (i < 50) blocks[i][j] = new Block("Air");
        }
      }
      
      distributeTraps(10);
    }
    MicroMiner.player.isAlive = true;
  }
  
  public void distributeOre(int pointCount) {
    // Ore Distribution
    HashMap points = new HashMap<int[], Block>();
    float coal = 0.05f;
    float copper = 0.02f;
    float diamond = 0.002f;
    
    // Generate Points
    for (int k = 0; k < pointCount; k++) {
      int[] newint = { floor(random(size[0])), floor(random(size[1])) };
      // TODO: Add existing pair detector
      points.put(newint, new Block("Stone"));
    }
    
    // Assign Blocks to Points
    ArrayList<int[]> listOfKeys = new ArrayList<int[]>(points.keySet()); // https://www.geeksforgeeks.org/how-to-convert-hashmap-to-arraylist-in-java/#:~:text=1%20Method%201%3A%20One%20way%20to%20convert%20is,an%20ArrayList%20without%20retaining%20the%20direct%20key%2Fvalue%20relationship.
    
    // Coal
    for (int k = 0; k < floor(pointCount * coal); k++) {
      points.put(listOfKeys.get(k), new Block("Coal"));
    }
    
    // Copper
    for (int k = floor(pointCount * coal); k < floor(pointCount * coal) + floor(pointCount * copper); k++) {
      points.put(listOfKeys.get(k), new Block("Copper"));
    }
    
    // Diamond
    for (int k = floor(pointCount * copper); k < floor(pointCount * copper) + floor(pointCount * diamond); k++) {
      points.put(listOfKeys.get(k), new Block("Diamond"));
    }
    
    // Assign Points to Map
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        int[] closest = listOfKeys.get(0);
        
        for (int k = 1; k < pointCount; k++) { // Get Closest Point (Pythagorean Theorem)
          int[] thisPoint = listOfKeys.get(k);
          
          if (pow(thisPoint[0] - j, 2) + pow(thisPoint[1] - i, 2) < pow(closest[0] - j, 2) + pow(closest[1] - i, 2)) {
            closest = thisPoint;
          }
        }
        
        blocks[i][j] = (MicroMiner.Block) points.get(closest);
      }
    }
  }
  
  public void distributeTraps(int density) {
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        if (blocks[i][j].type == "Stone" && blocks[i-1][j].type == "Air" && i > 100) {
          if (random(100) < density) blocks[i][j] = new Block("Trap");
        }
      }
    }
  }
}
  public void settings() {  size(500, 500); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "MicroMiner" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
