import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Foxes can display themselves
 */
public class Fox extends FieldOccupant
{
   private Field          _theField;
   private int            _x;
   private int            _y;
   private int            _order;
   private AtomicBoolean  _drawFlag;
   private boolean        _dead;
   private CountDownLatch _running;

   public Fox(Field theField, AtomicBoolean drawFlag, CountDownLatch running,
         int x, int y, int order)
   {
      // super(theField, x, y, order);
      _theField = theField;
      _x = x;
      _y = y;
      _order = order;
      _drawFlag = drawFlag;
      _running = running;
   }

   /**
    * @return the color to use for a cell occupied by a Fox
    */
   @Override
   public Color getDisplayColor()
   {
      return Color.green;
   } // getDisplayColor

   /**
    * @return the text representing a Fox
    */
   @Override
   public String toString()
   {
      return "F";
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

      FieldOccupant theEmpty = null;

      // ArrayLists that hold my neighbors by type
      ArrayList<FieldOccupant> neighboringFoxes;
      ArrayList<FieldOccupant> neighboringHounds;
      ArrayList<FieldOccupant> neighboringEmpty;

      TreeSet<FieldOccupant> theEmptyNeighbors;
      boolean canReproduce;

      // Treeset sorted by order of the neighbors.
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
            // Sleep
            Thread.sleep((int) (Math.random() * 500 + 750));

            /*
             * CHECK AROUND
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

            // Based on our neighbors, we need to see if the conditions are
            // right to reproduce
            canReproduce = false;
            // If one of our neighbors was an empty slot
            if (!neighboringEmpty.isEmpty())
            {
               // Pick a random empty slot
               theEmpty = neighboringEmpty.get(
                     (int) (Math.random() * (neighboringEmpty.size() - 1)));

               // Now we need to check its neighbors for a fox
               theEmptyNeighbors = _theField.getNeighborsOf(theEmpty);

               // Look for a Fox in the empty Neighbors
               for (FieldOccupant theNeighbors : theEmptyNeighbors)
               {
                  // Don't want to reproduce with ourself...sicko
                  if ((theNeighbors instanceof Fox)
                        && !theNeighbors.equals(this))
                  {
                     canReproduce = true;
                  }
               }

               /*
                * If we can take an action, lock the appropriate cells
                */
               if (canReproduce)
               {
                  lockedNeighbors.add(this);
                  lockedNeighbors.add(theEmpty);

                  for (FieldOccupant neighbor : lockedNeighbors)
                  {
                     _theField.getLock(neighbor.getOrder()).lock();
                  }
               }

               /*
                * Check that we locked what was expected
                */
               if (!theEmpty.equals(
                     _theField.getOccupantAt(theEmpty.getX(), theEmpty.getY())))
               {
                  canReproduce = false;
               }

            }

            // If the condition still holds on second check, we can reproduce!
            if (canReproduce)
            {
               // Make New Fox
               _theField.setOccupantAt(theEmpty,
                     new Fox(_theField, _drawFlag, _running, theEmpty.getX(),
                           theEmpty.getY(), theEmpty.getOrder()));

               // Fire up new thread
               _theField.getOccupantAt(theEmpty.getX(), theEmpty.getY())
                     .start();

               // Let the Simulation know it needs to update its drawing
               _drawFlag.set(true);
            }

         }
         catch (InterruptedException e)
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

      } // while
   }
}
