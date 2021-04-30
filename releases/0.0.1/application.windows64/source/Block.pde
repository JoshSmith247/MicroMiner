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
  color rgb;
  int value;
  
  Block(String type) {
    this.type = type;
    this.data = getAsset(type);
    this.value = data.getInt("value");
    
    JSONObject colorData = data.getJSONObject("color");
    this.rgb = color(colorData.getInt("r"), colorData.getInt("g"), colorData.getInt("b"));
  }
}

JSONObject getAsset(String name) {
  if (assets.get(name) == null) {
    assets.put(name, loadJSONObject(name+".json"));
  }
  
  JSONObject asset = assets.get(name);
  return asset;
}
