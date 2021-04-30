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

void setup() {
  size(500, 500);
  
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

void draw() {
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
    
    if (player.xvelocity > 0.1) { // Friction
       player.xvelocity -= 0.1;
     } else if (player.xvelocity < -0.1) {
       player.xvelocity += 0.1;
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

 void saveGame(String fileName) { // Create a JSON Object corresponding to world array and save to file
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
  
  void loadGame(String fileName) { // Parse save file
    JSONObject saveJSON = loadJSONObject("saves/"+fileName);
    
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        JSONObject saveData = saveJSON.getJSONObject(String.valueOf(i)).getJSONObject(String.valueOf(j));
        
        world.blocks[i][j] = new Block(saveData.getString("type"));
      }
    }
  }
