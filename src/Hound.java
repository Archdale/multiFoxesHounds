import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hounds can display themsevles. They also get hungry
 */
public class Hound extends FieldOccupant
{

   // Default starve time for Hounds
   public static final int DEFAULT_STARVE_TIME = 3000;                // In
                                                                      // milliseconds
   private static int      _houndStarveTime    = DEFAULT_STARVE_TIME; // Class
                                                                      // variable
                                                                      // for all
                                                                      // hounds

   // Instance attributes to keep track of how hungry we are
   private int             _fedStatus;

   private Field           _theField;
   private int             _x;
   private int             _y;
   private int             _order;
   private AtomicBoolean   _drawFlag;
   private boolean         _dead;

   /**
    * Create a hound
    */
   public Hound(Field theField, AtomicBoolean drawFlag, int x, int y, int order)
   {
      _theField = theField;
      _x = x;
      _y = y;
      _order = order;
      _drawFlag = drawFlag;
      eats();
   }

   /**
    * @return true if this Hound has starved to death
    */
   public boolean hasStarved()
   {
      return _fedStatus < 0;
   }

   /**
    * Make this Hound hungrier
    *
    * @return true if the Hound has starved to death
    */
   public boolean getHungrier(int timeAsleep)
   {
      // Decrease the fed status of this Hound
      _fedStatus -= timeAsleep;
      return hasStarved();
   }

   public void eats()
   {
      // Reset the fed status of this Hound
      _fedStatus = _houndStarveTime;
   }

   /**
    * @return the color to use for a cell occupied by a Hound
    */
   @Override
   public Color getDisplayColor()
   {
      return Color.red;
   } // getDisplayColor

   /**
    * @return the text representing a Hound
    */
   @Override
   public String toString()
   {
      return "H";
   }

   /**
    * Sets the starve time for this class
    *
    * @param starveTime
    */
   public static void setStarveTime(int starveTime)
   {
      _houndStarveTime = starveTime;
   }

   /**
    * @return the starve time for Hounds
    */
   public static int getStarveTime()
   {
      return _houndStarveTime;
   }

   public int getOrder()
   {
      return _order;
   }

   public int getX()
   {
      return _x;
   }

   public int getY()
   {
      return _y;
   }

   public void kill()
   {
      _dead = true;
   }
   
   @Override
   public void run()
   {
      int durationAsleep;
      int foxToPick;
      ArrayList<FieldOccupant> neighboringFoxes;
      ArrayList<FieldOccupant> neighboringHounds;
      FieldOccupant theFox;
      TreeSet<FieldOccupant> theFoxNeighbors;
      boolean canReproduce;

      while (!_dead)
      {
         try
         {
            durationAsleep = (int) (Math.random() * 500 + 750);

            Thread.sleep(durationAsleep);

            // Lock Neighbors/Self based on total ordering
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y))
            {

               _theField.getLock(neighbor.getOrder()).acquire();

            }

            /*
             * Do stuff
             */

            TreeSet<FieldOccupant> myNeighbors = _theField.getNeighborsOf(_x,
                  _y);
            // Iterate over the neighbors and see how many foxes and
            // hounds are nearby
            neighboringFoxes = new ArrayList<FieldOccupant>();
            neighboringHounds = new ArrayList<FieldOccupant>();
            for (FieldOccupant neighbor : myNeighbors)
            {
               if (neighbor instanceof Fox)
               {
                  neighboringFoxes.add(neighbor);
               }
               else if (neighbor instanceof Hound)
               {
                  neighboringHounds.add(neighbor);
               }
            } // for

            if (!neighboringFoxes.isEmpty())
            {
               // Hound has fox who has neighbor hound
               foxToPick = (int) (Math.random()
                     * (neighboringFoxes.size() - 1));
               theFox = neighboringFoxes.get(foxToPick);
               theFoxNeighbors = _theField.getNeighborsOf(theFox.getX(),
                     theFox.getY());

               canReproduce = false;
               // Look for a hound in the Foxes Neighbors
               for (FieldOccupant theNeighbors : theFoxNeighbors)
               {
                  if (theNeighbors instanceof Hound)
                  {
                     canReproduce = true;
                  }
               }
               if (canReproduce)
               {
                  //Replace Fox with new Hound
                  _theField.setOccupantAt(theFox.getX(), theFox.getY(),
                        new Hound(_theField, _drawFlag, theFox.getX(),
                              theFox.getY(), theFox.getOrder()));
                  //Fire up new thread
                  _theField.getOccupantAt(theFox.getX(), theFox.getY()).start();;

               }
               else
               {
                  _theField.setOccupantAt(theFox.getX(), theFox.getY(),
                        new Empty(_theField, _drawFlag, theFox.getX(),
                              theFox.getY(), theFox.getOrder()));
               }
               theFox.kill();
               theFox.interrupt();
               _drawFlag.getAndSet(true);
               eats();
            }
            
            // Hound gets hungry
            else if (_dead = getHungrier(durationAsleep))
            {
               _theField.setOccupantAt(_x, _y,
                     new Empty(_theField, _drawFlag, _x, _y, _order));
               _drawFlag.getAndSet(true);
               interrupt();
            }

            /*
             * Clean Up
             */

            // Unlock Neighbors/Self; reversed from how we locked them.
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y)
                  .descendingSet())
            {
               _theField.getLock(neighbor.getOrder()).release();
            }

         }
         catch (InterruptedException e1)
         {
            // Unlock Neighbors/Self; reversed from how we locked them.
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y)
                  .descendingSet())
            {
               _theField.getLock(neighbor.getOrder()).release();
            }
         }
      } // While

   }

}
