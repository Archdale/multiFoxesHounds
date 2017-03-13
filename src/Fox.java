import java.awt.Color;
import java.util.Random;
import java.util.Set;
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

   public Fox(Field theField, AtomicBoolean drawFlag, int x, int y, int order)
   {
      // super(theField, x, y, order);
      _theField = theField;
      _x = x;
      _y = y;
      _order = order;
      _drawFlag = drawFlag;
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
            
            

            // Unlock Neighbors/Self
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y)
                  .descendingSet())
            {
               _theField.getLock(neighbor.getOrder()).release();
            }
         }
         catch (InterruptedException e)
         {
            
            // Unlock Neighbors/Self
            for (FieldOccupant neighbor : _theField.getNeighborsAndSelf(_x, _y)
                  .descendingSet())
            {
               _theField.getLock(neighbor.getOrder()).release();
            }
         }

      } // while
   }
}
