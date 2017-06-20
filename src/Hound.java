import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hounds can display themselves. They also get hungry
 */
public class Hound extends FieldOccupant
{

   // Default starve time for Hounds in MS
   public static final int DEFAULT_STARVE_TIME = 3000;
   // Default Time Hounds are alive in ms.
   private static int      _houndStarveTime    = DEFAULT_STARVE_TIME;
   // Instance attributes to keep track of how hungry we are
   private int             _fedStatus;
   // The field we live on
   private Field           _theField;
   // Our X location
   private int             _x;
   // Our Y location
   private int             _y;
   // Our total order in the field
   private int             _order;
   // Shared flag to alert simulation to update the field's rendering
   private AtomicBoolean   _drawFlag;
   // Am I alive?
   private boolean         _dead;
   // Starting gate for the simulation
   private CountDownLatch  _running;

   /**
    * Create a hound
    */
   public Hound(Field theField, AtomicBoolean drawFlag, CountDownLatch running,
         int x, int y, int order)
   {
      _theField = theField;
      _x = x;
      _y = y;
      _order = order;
      _drawFlag = drawFlag;
      _running = running;
      // Start him off well-fed
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

   /**
    * Hound resets his starvation timer
    */
   public void eats()
   {
      // Reset the fed status of this Hound
      _fedStatus = _houndStarveTime;
   }

   /**
    * @return the color to use for a cell occupied by a Hound; based on
    *         fedStatus
    */
   @Override
   public Color getDisplayColor()
   {
      if (_fedStatus < _houndStarveTime / 6)
      {
         return new Color(75, 0, 0);
      }
      else if (_fedStatus < _houndStarveTime / 3)
      {
         return new Color(100, 0, 0);
      }
      else if (_fedStatus < _houndStarveTime / 2)
      {
         return new Color(125, 0, 0);
      }
      else if (_fedStatus < _houndStarveTime * 2 / 3)
      {
         return new Color(175, 0, 0);
      }
      else if (_fedStatus < _houndStarveTime * 5 / 6)
      {
         return new Color(200, 0, 0);
      }
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

   /**
    * @return the total ordering of this field occupant
    */
   @Override
   public int getOrder()
   {
      return _order;
   }

   /**
    * @return the X co-ordinant of this field occupant
    */
   @Override
   public int getX()
   {
      return _x;
   }

   /**
    * @return the Y co-ordinant of this field occupant
    */
   @Override
   public int getY()
   {
      return _y;
   }

   /**
    * Kills the current occupant.
    */
   @Override
   public void kill()
   {
      _dead = true;
   }

   @Override
   public void run()
   {
      int durationAsleep;
      int foxToPick;
      int emptyToPick;
      int foxCount;
      int houndCount;
      boolean canReproduce;
      boolean canEat;

      // ArrayLists that hold my neighbors by type
      ArrayList<FieldOccupant> neighboringFoxes;
      ArrayList<FieldOccupant> neighboringHounds;
      ArrayList<FieldOccupant> neighboringEmpty;

      FieldOccupant theEmpty = null;
      FieldOccupant theFox = null;

      // Checking our neighbor's neighbors
      TreeSet<FieldOccupant> theFoxNeighbors;
      TreeSet<FieldOccupant> theEmptyNeighbors;

      // List of hounds that could reproduce.
      ArrayList<FieldOccupant> eligibleHounds;
      FieldOccupant luckyHound = null;

      // This is a TreeSet that sorts by the _order of each element
      TreeSet<FieldOccupant> lockedNeighbors = new TreeSet<FieldOccupant>(
            new Comparator<FieldOccupant>()
            {
               public int compare(FieldOccupant o1, FieldOccupant o2)
               {
                  return (o1.getOrder() - o2.getOrder());
               }
            });

      // The Starting Gate!
      try
      {
         _running.await();
      }
      catch (InterruptedException e)
      {
         ;
      }

      // While we're alive
      while (!_dead)
      {
         try
         {
            // Sleep a random amount from 0.75s to 1.25s; this is also how much
            // we will subtract from our hunger
            durationAsleep = (int) (Math.random() * 500 + 750);
            Thread.sleep(durationAsleep);

            /*
             * CHECK
             */

            // Gets a list of our 8 neighbors
            TreeSet<FieldOccupant> myNeighbors = _theField.getNeighborsOf(this);

            // Iterate over the neighbors and see how many foxes and
            // hounds are nearby
            neighboringFoxes = new ArrayList<FieldOccupant>();
            neighboringHounds = new ArrayList<FieldOccupant>();
            neighboringEmpty = new ArrayList<FieldOccupant>();
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
               else if (neighbor instanceof Empty)
               {
                  neighboringEmpty.add(neighbor);
               }
            } // for

            canReproduce = false;
            canEat = false;
            eligibleHounds = new ArrayList<FieldOccupant>();

            if (!neighboringFoxes.isEmpty())
            {
               // Pick a random fox
               foxToPick = (int) (Math.random()
                     * (neighboringFoxes.size() - 1));
               theFox = neighboringFoxes.get(foxToPick);
               // If we picked one that hasn't started, we fail!
               if (theFox.getState() != Thread.State.NEW)
               {
                  // Now we need to check its neighbors for a hound
                  theFoxNeighbors = _theField.getNeighborsOf(theFox);
                  canEat = true;

                  // Look for a hound in the Foxes Neighbors
                  for (FieldOccupant theNeighbors : theFoxNeighbors)
                  {
                     // Don't want to reproduce with ourself...
                     if ((theNeighbors instanceof Hound)
                           && !theNeighbors.equals(this))
                     {
                        eligibleHounds.add(theNeighbors);
                        luckyHound = eligibleHounds.get(0);
                        canReproduce = true;
                     }
                  }
               }

               /*
                * If we can take an action, lock the appropriate cells
                */
               if (canReproduce || canEat)
               {
                  // Lock Myself, the Fox and the other Hound in total order
                  lockedNeighbors.add(this);
                  lockedNeighbors.add(theFox);
                  if (canReproduce)
                  {
                     lockedNeighbors.add(luckyHound);
                  }

                  for (FieldOccupant neighbor : lockedNeighbors)
                  {

                     _theField.getLock(neighbor.getOrder()).lock();

                  }
               }

               /*
                * Check that we locked what was expected
                */
               if (canReproduce && !luckyHound.equals(_theField
                     .getOccupantAt(luckyHound.getX(), luckyHound.getY())))
               {
                  canReproduce = false;
               }

               if (!theFox.equals(
                     _theField.getOccupantAt(theFox.getX(), theFox.getY())))
               {
                  canEat = false;
                  canReproduce = false;
               }

            }

            else if (!neighboringEmpty.isEmpty())
            {
               // Pick a random empty slot
               emptyToPick = (int) (Math.random()
                     * (neighboringEmpty.size() - 1));
               theEmpty = neighboringEmpty.get(emptyToPick);

               // Now we need to check its neighbors for a hound
               theEmptyNeighbors = _theField.getNeighborsOf(theEmpty);

               foxCount = 0;
               houndCount = 0;
               // Look for a hound in the Foxes Neighbors
               for (FieldOccupant theNeighbors : theEmptyNeighbors)
               {
                  if (theNeighbors instanceof Fox)
                  {
                     foxCount++;
                  }
                  // Don't want to reproduce with ourself...
                  if ((theNeighbors instanceof Hound)
                        && !theNeighbors.equals(this))
                  {
                     houndCount++;
                  }
                  if (houndCount >= 1 && foxCount >= 2)
                  {
                     canReproduce = true;
                     canEat = true;
                  }
               }

               // Lock Neighbors/Self based on total ordering
               if (canReproduce || canEat)
               {
                  // Lock Myself, the Fox and the other Hound in total order
                  lockedNeighbors.add(this);
                  lockedNeighbors.add(theEmpty);

                  for (FieldOccupant neighbor : lockedNeighbors)
                  {

                     _theField.getLock(neighbor.getOrder()).lock();

                  }
               }

               // CHeck again for that empty
               if (!theEmpty.equals(
                     _theField.getOccupantAt(theEmpty.getX(), theEmpty.getY())))
               {
                  canEat = false;
                  canReproduce = false;
               }
            }

            if (canEat)
            {
               /*
                * If we can reproduce
                */
               if (canReproduce)
               {

                  // Replace Fox with new Hound
                  _theField.setOccupantAt(theFox,
                        new Hound(_theField, _drawFlag, _running, theFox.getX(),
                              theFox.getY(), theFox.getOrder()));
                  // Fire up new thread
                  // threads aren't eaten
                  _theField.getOccupantAt(theFox.getX(), theFox.getY()).start();

               }
               /*
                * If we can Eat
                */
               else// We'll just eat one
               {
                  _theField.setOccupantAt(theFox,
                        new Empty(_theField, _drawFlag, _running, theFox.getX(),
                              theFox.getY(), theFox.getOrder()));
               }
               // Tell the fox he's dead and interrupt him.
               theFox.kill();
               theFox.interrupt();
               // Tell the field to redraw
               _drawFlag.set(true);
               eats();
            }

            /*
             * Hound gets hungry; maybe dies
             */
            else if (_dead = getHungrier(durationAsleep))
            {
               _theField.setOccupantAt(_x, _y,
                     new Empty(_theField, _drawFlag, _running, _x, _y, _order));

               _drawFlag.set(true);
               interrupt();
            }

         }
         catch (InterruptedException e1)
         {
            ;
         }
         finally
         {
            // Regardless of if we were interrupted or not, we want to free any
            // locks we have.
            for (FieldOccupant neighbor : lockedNeighbors.descendingSet())
            {

               if (_theField.getLock(neighbor.getOrder())
                     .isHeldByCurrentThread())
               {
                  _theField.getLock(neighbor.getOrder()).unlock();
               }
            }
         }
      } // While

   }

}
