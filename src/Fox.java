import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Foxes can display themselves
 */
public class Fox extends FieldOccupant
{
   private Field         _theField;
   private int           _x;
   private int           _y;
   private int           _order;
   private AtomicBoolean _drawFlag;
   private boolean       _dead;
   private AtomicBoolean _running;

   public Fox(Field theField, AtomicBoolean drawFlag, AtomicBoolean running, int x, int y, int order)
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
      
      if(this.getState() == Thread.State.RUNNABLE)
      {
         return new Color(0, 50, 0);
      }
      else if(this.getState() == Thread.State.BLOCKED)
      {
         return new Color(0, 25, 0);
      }
      else if(this.getState() == Thread.State.NEW)
      {
         return new Color(0, 100, 0);
      }
      else if(this.getState() == Thread.State.WAITING)
      {
         return new Color(0, 150, 0);
      }
      else if(this.getState() == Thread.State.TIMED_WAITING)
      {
         return new Color(0, 200, 0);
      }
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
      // boolean dead = false;
      ArrayList<FieldOccupant> neighboringFoxes;
      ArrayList<FieldOccupant> neighboringHounds;
      ArrayList<FieldOccupant> neighboringEmpty;
      boolean hasActed;
      FieldOccupant emptyCell;
      TreeSet<FieldOccupant>  emptyCellNeighbors;
      boolean canReproduce;
      
      while(!_running.get())
      {
         yield();
      }
      
      while (!_dead)
      {

         try
         {
            // Sleep
            durationAsleep = (int) (Math.random() * 500 + 750);

            Thread.sleep(durationAsleep);

            // Get and Lock Neighbors/Self based on total ordering
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y))
            {

               _theField.getLock(neighbor.getOrder()).acquire();

            }

            // Do stuff
            TreeSet<FieldOccupant> myNeighbors = _theField.getNeighborsOf(_x,
                  _y);
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


            hasActed = false;
            if (!neighboringEmpty.isEmpty())
            {
               // Iterate over empty cells checking for a fox as its neighbor
               // Making sure we don't see ourself
               while (neighboringEmpty.iterator().hasNext() && !hasActed)
               {
                  emptyCell = neighboringEmpty.iterator().next();
                  emptyCellNeighbors = _theField.getNeighborsOf(emptyCell);
                  
                  canReproduce = false;
                  for (FieldOccupant theNeighbors : emptyCellNeighbors)
                  {
                     if ((theNeighbors instanceof Fox) && !theNeighbors.equals(this))
                     {
                        canReproduce = true;
                     }
                  }
                  
                  if (canReproduce)
                  {
                     // Replace Fox with new Hound
                     _theField.setOccupantAt(emptyCell.getX(), emptyCell.getY(),
                           new Fox(_theField, _drawFlag,_running, emptyCell.getX(),
                                 emptyCell.getY(), emptyCell.getOrder()));
                     // Fire up new thread
                     _theField.getOccupantAt(emptyCell.getX(), emptyCell.getY()).start(); //Caused IllegalThreadStateException
                     hasActed = true;
                     
                     emptyCell.interrupt();
                     _drawFlag.getAndSet(true);
                  }
                  
               }
            }

            // Unlock Neighbors/Self
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y)
                  .descendingSet())
            {
               if(_theField.getLock(neighbor.getOrder()).availablePermits() == 0)
               {
                  _theField.getLock(neighbor.getOrder()).release();
               }
            }
         }
         catch (InterruptedException e)
         {
            // ISSUE - Foxes that die not releasing holds; or semaphore has more than one permit
            // Unlock Neighbors/Self
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y)
                  .descendingSet())
            {
               if(_theField.getLock(neighbor.getOrder()).availablePermits() == 0)
               {
                  _theField.getLock(neighbor.getOrder()).release();
               }
            }
            
         }

      } // while
   }
}
