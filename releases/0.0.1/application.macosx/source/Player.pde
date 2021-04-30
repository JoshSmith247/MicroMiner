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
  
  void mine() { // Locates and breaks blocks
    int[] offset = { player.x - width/2, player.y - height/2 };
    
    float difY = player.y - offset[1] - mouseY;
    float difX = player.x - offset[0] - mouseX;
    
    float slope = abs(difY / difX);
    
    Block thisBlock = new Block("Air");
    float[] loc = { player.x, player.y };
    
    while (thisBlock.type.equals("Air")) { // Get the closest block (not air)
      if (difX < 0) {
        loc[0] += 0.25;
      } else {
        loc[0] -= 0.25;
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
