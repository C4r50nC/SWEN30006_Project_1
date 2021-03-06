package snakeladder.game;

import ch.aplu.jgamegrid.*;
import java.awt.Point;
import java.util.HashMap;

public class Puppet extends Actor
{
  private GamePane gamePane;
  private NavigationPane navigationPane;
  private int cellIndex = 0;
  private int nbSteps;
  private Connection currentCon = null;
  private int y;
  private int dy;
  private boolean isAuto;
  private String puppetName;
  private boolean isLowestStep;

  private int up;
  private int down;

  private HashMap<Integer, Integer> record = new HashMap<>();

  Puppet(GamePane gp, NavigationPane np, String puppetImage)
  {
    super(puppetImage);
    this.gamePane = gp;
    this.navigationPane = np;

    for (int i = np.getNumberOfDice(); i <= 6 * np.getNumberOfDice(); i++) {
      record.put(i, 0);
    }
  }

  public HashMap<Integer, Integer> getRecord() {
    return record;
  }

  public int getUp() {
    return up;
  }

  public int getDown() {
    return down;
  }

  public boolean isAuto() {
    return isAuto;
  }

  public void setAuto(boolean auto) {
    isAuto = auto;
  }

  public String getPuppetName() {
    return puppetName;
  }

  public void setPuppetName(String puppetName) {
    this.puppetName = puppetName;
  }

  void go(int nbSteps)
  {
    isLowestStep = false;
    if (nbSteps == navigationPane.getNumberOfDice()) {
      isLowestStep = true;
    }
    if (nbSteps == 0) {
      navigationPane.prepareRoll(cellIndex);
      return;
    }
    if (cellIndex == 100)  // after game over
    {
      cellIndex = 0;
      setLocation(gamePane.startLocation);
    }
    this.nbSteps = nbSteps;
    setActEnabled(true);
  }

  void resetToStartingPoint() {
    cellIndex = 0;
    setLocation(gamePane.startLocation);
    setActEnabled(true);
  }

  int getCellIndex() {
    return cellIndex;
  }

  private void moveToNextCell()
  {
    int tens = cellIndex / 10;
    int ones = cellIndex - tens * 10;
    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
    {
      if (ones == 0 && cellIndex > 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() + 1, getY()));
    }
    else     // Cells starting left 20, 40, .. 100
    {
      if (ones == 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() - 1, getY()));
    }
    cellIndex++;
  }

  private void moveToPrevCell()
  {
    if (cellIndex == 1) {
      this.resetToStartingPoint();
    }

    cellIndex--;
    int tens = cellIndex / 10;
    int ones = cellIndex - tens * 10;
    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
    {
      if (ones == 0) {
        setLocation(new Location(getX(), getY() + 1));
      } else {
        setLocation(new Location(getX() - 1, getY()));
      }
    }
    else     // Cells starting left 20, 40, .. 100
    {
      if (ones == 0) {
        setLocation(new Location(getX(), getY() + 1));
      } else {
        setLocation(new Location(getX() + 1, getY()));
      }
    }

    // Check Connections
    if ((currentCon = gamePane.getConnectionAt(getLocation())) != null)
    {
      gamePane.setSimulationPeriod(50);
      y = gamePane.toPoint(currentCon.locStart).y;
      if (currentCon.locEnd.y > currentCon.locStart.y)
        dy = gamePane.animationStep;
      else
        dy = -gamePane.animationStep;
      if (currentCon instanceof Snake)
      {
        navigationPane.showStatus("Digesting...");
        navigationPane.playSound(GGSound.MMM);

        if (navigationPane.getToggleCheck().isChecked()) {
          up += 1;
        } else {
          down += 1;
        }
      }
      else
      {
        navigationPane.showStatus("Climbing...");
        navigationPane.playSound(GGSound.BOING);

        if (navigationPane.getToggleCheck().isChecked()) {
          down += 1;
        } else {
          up += 1;
        }
      }
    }
    setActEnabled(true);
    // gamePane.switchToPrevPuppet();
  }

  public void act()
  {
    if ((cellIndex / 10) % 2 == 0)
    {
      if (isHorzMirror())
        setHorzMirror(false);
    }
    else
    {
      if (!isHorzMirror())
        setHorzMirror(true);
    }

    // Animation: Move on connection
    if (currentCon != null)
    {
      // Avoid traveling down after getting the lowest step
      if (isLowestStep && currentCon.cellEnd < currentCon.cellStart) {
        currentCon = null;
        navigationPane.prepareRoll(cellIndex);
        return;
      }

      int x = gamePane.x(y, currentCon);
      setPixelLocation(new Point(x, y));
      y += dy;

      // Check end of connection
      if ((dy > 0 && (y - gamePane.toPoint(currentCon.locEnd).y) > 0)
        || (dy < 0 && (y - gamePane.toPoint(currentCon.locEnd).y) < 0))
      {
        gamePane.setSimulationPeriod(100);
        setActEnabled(false);
        setLocation(currentCon.locEnd);
        cellIndex = currentCon.cellEnd;
        setLocationOffset(new Point(0, 0));
        currentCon = null;
        navigationPane.prepareRoll(cellIndex);
      }
      return;
    }

    // Normal movement
    if (nbSteps > 0)
    {
      moveToNextCell();

      if (cellIndex == 100)  // Game over
      {
        setActEnabled(false);
        navigationPane.prepareRoll(cellIndex);
        return;
      }

      nbSteps--;
      if (nbSteps == 0)
      {
        // Detect collision
        for (Puppet puppet: gamePane.getAllPuppets()) {
          if (puppet != this && puppet.getCellIndex() == cellIndex) {
            puppet.moveToPrevCell();
          }
        }

        // Check if on connection start
        if ((currentCon = gamePane.getConnectionAt(getLocation())) != null)
        {
          gamePane.setSimulationPeriod(50);
          y = gamePane.toPoint(currentCon.locStart).y;
          if (currentCon.locEnd.y > currentCon.locStart.y)
            dy = gamePane.animationStep;
          else
            dy = -gamePane.animationStep;
          if (currentCon instanceof Snake)
          {
            if (!isLowestStep) {
              navigationPane.showStatus("Digesting...");
              navigationPane.playSound(GGSound.MMM);

              if (navigationPane.getToggleCheck().isChecked()) {
                up += 1;
              } else {
                down += 1;
              }
            }
          }
          else
          {
            navigationPane.showStatus("Climbing...");
            navigationPane.playSound(GGSound.BOING);

            if (navigationPane.getToggleCheck().isChecked()) {
              down += 1;
            } else {
              up += 1;
            }
          }
        }
        else
        {
          setActEnabled(false);
          navigationPane.prepareRoll(cellIndex);
        }
      }
    }
  }

}
