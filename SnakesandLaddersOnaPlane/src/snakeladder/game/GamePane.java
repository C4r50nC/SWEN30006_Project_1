package snakeladder.game;

import ch.aplu.jgamegrid.*;
import snakeladder.utility.PropertiesLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("serial")
public class GamePane extends GameGrid
{
  private NavigationPane np;
  private int numberOfPlayers = 1;
  private int currentPuppetIndex = 0;
  private List<Puppet> puppets =  new ArrayList<>();
  private List<Boolean> playerManualMode;
  private ArrayList<Connection> connections = new ArrayList<Connection>();
  final Location startLocation = new Location(-1, 9);  // outside grid
  final int animationStep = 10;
  public static final int NUMBER_HORIZONTAL_CELLS = 10;
  public static final int NUMBER_VERTICAL_CELLS = 10;
  private final int MAX_PUPPET_SPRITES = 4;

  GamePane(Properties properties)
  {
    setSimulationPeriod(100);
    setBgImagePath("sprites/gamepane_blank.png");
    setCellSize(60);
    setNbHorzCells(NUMBER_HORIZONTAL_CELLS);
    setNbHorzCells(NUMBER_VERTICAL_CELLS);
    doRun();
    createSnakesLadders(properties);
    setupPlayers(properties);
    setBgImagePath("sprites/gamepane_snakeladder.png");
  }

  void setupPlayers(Properties properties) {
    numberOfPlayers = Integer.parseInt(properties.getProperty("players.count"));
    playerManualMode = new ArrayList<>();
    for (int i = 0; i < numberOfPlayers; i++) {
      playerManualMode.add(Boolean.parseBoolean(properties.getProperty("players." + i + ".isAuto")));
    }
    System.out.println("playerManualMode = " + playerManualMode);
  }

  void createSnakesLadders(Properties properties) {
    connections.addAll(PropertiesLoader.loadSnakes(properties));
    connections.addAll(PropertiesLoader.loadLadders(properties));
  }

  void setNavigationPane(NavigationPane np)
  {
    this.np = np;
  }

  void createGui()
  {
    for (int i = 0; i < numberOfPlayers; i++) {
      boolean isAuto = playerManualMode.get(i);
      int spriteImageIndex = i % MAX_PUPPET_SPRITES;
      String puppetImage = "sprites/cat_" + spriteImageIndex + ".gif";

      Puppet puppet = new Puppet(this, np, puppetImage);
      puppet.setAuto(isAuto);
      puppet.setPuppetName("Player " + (i + 1));
      addActor(puppet, startLocation);
      puppets.add(puppet);
    }
  }

  Puppet getPuppet()
  {
    return puppets.get(currentPuppetIndex);
  }

  Puppet getNextPuppet() {
    return puppets.get((currentPuppetIndex + 1) % numberOfPlayers);
  }

  void switchToNextPuppet() {
    currentPuppetIndex = (currentPuppetIndex + 1) % numberOfPlayers;
  }
  void switchToPrevPuppet() {
    if (currentPuppetIndex != 0) {
      currentPuppetIndex = (currentPuppetIndex - 1) % numberOfPlayers;
    } else {
      currentPuppetIndex = numberOfPlayers - 1;
    }
  }

  List<Puppet> getAllPuppets() {
    return puppets;
  }

  void resetAllPuppets() {
    for (Puppet puppet: puppets) {
      puppet.resetToStartingPoint();
    }
  }

  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  Connection getConnectionAt(Location loc)
  {
    for (Connection con : connections)
      if (con.locStart.equals(loc))
        return con;
    return null;
  }

  static Location cellToLocation(int cellIndex)
  {
    int index = cellIndex - 1;  // 0..99

    int tens = index / NUMBER_HORIZONTAL_CELLS;
    int ones = index - tens * NUMBER_HORIZONTAL_CELLS;

    int y = 9 - tens;
    int x;

    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
      x = ones;
    else     // Cells starting left 20, 40, .. 100
      x = 9 - ones;

    return new Location(x, y);
  }
  
  int x(int y, Connection con)
  {
    int x0 = toPoint(con.locStart).x;
    int y0 = toPoint(con.locStart).y;
    int x1 = toPoint(con.locEnd).x;
    int y1 = toPoint(con.locEnd).y;
    // Assumption y1 != y0
    double a = (double)(x1 - x0) / (y1 - y0);
    double b = (double)(y1 * x0 - y0 * x1) / (y1 - y0);
    return (int)(a * y + b);
  }

  void swapSnakeLadder() {
    for (Connection connection: connections) {
      connection.switchDirection();
    }
  }

  void reportRolls(int puppetIndex) {
    System.out.print("Player " + puppetIndex + " rolled: ");
    for (int key: puppets.get(puppetIndex).getRecord().keySet()) {
      System.out.print(key + "-" + puppets.get(puppetIndex).getRecord().get(key));
      if (key != np.getNumberOfDice() * 6) {
        System.out.print(", ");
      }
    }
    System.out.println();
  }

  void reportTraverses(int puppetIndex) {
    System.out.println("Player " + puppetIndex + " traversed: up-" + puppets.get(puppetIndex).getUp() + ", down-" + puppets.get(puppetIndex).getDown());
  }

  int getCurrentPuppetIndex() {
    return currentPuppetIndex;
  }
}
