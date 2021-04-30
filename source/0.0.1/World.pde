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
          float nsize = 0.05;
          int ndetail = 2;
          noiseDetail(ndetail);
          float noiseValue = noise(i*nsize, j*nsize);
          if (noiseValue > 0.5) blocks[i][j] = new Block("Air");
          
          // Form Air
          if (i < 50) blocks[i][j] = new Block("Air");
        }
      }
      
      distributeTraps(10);
    }
    MicroMiner.player.isAlive = true;
  }
  
  void distributeOre(int pointCount) {
    // Ore Distribution
    HashMap points = new HashMap<int[], Block>();
    float coal = 0.05;
    float copper = 0.02;
    float diamond = 0.002;
    
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
  
  void distributeTraps(int density) {
    for (int i = 0; i < size[0]; i++) {
      for (int j = 0; j < size[1]; j++) {
        if (blocks[i][j].type == "Stone" && blocks[i-1][j].type == "Air" && i > 100) {
          if (random(100) < density) blocks[i][j] = new Block("Trap");
        }
      }
    }
  }
}
