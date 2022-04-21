package snakeladder.game;

import ch.aplu.jgamegrid.Actor;

public class Die extends Actor
{
  private NavigationPane np;
  private int nb;
  private static int totalNb = 0;

  Die(int nb, NavigationPane np)
  {
    super("sprites/pips" + nb + ".gif", 7);
    this.nb = nb;
    this.np = np;
  }

  public void act()
  {
    showNextSprite();
    if (getIdVisible() == 6)
    {
      setActEnabled(false);
      totalNb += nb;
      if ((np.getNbRolls() + 1) % np.getNumberOfDice() == 0) {
        np.startMoving(totalNb, nb);
        totalNb = 0;
      } else {
        np.startMoving(0, nb);
      }
    }
  }

}
