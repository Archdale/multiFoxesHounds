import java.awt.Color;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Empty extends FieldOccupant
{

   private int _x;
   private int _y;
   private int _order;

   public Empty(Field theField, AtomicBoolean drawFlag, CountDownLatch running,
         int x, int y, int order)
   {
      _x = x;
      _y = y;
      _order = order;
   }

   @Override
   public Color getDisplayColor()
   {
      return Color.white;
   }

   /**
    * @return the text representing a Fox
    */
   @Override
   public String toString()
   {
      return " ";
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
   }

   @Override
   public void run()
   {
      // Empty Cells just exist, don't do nothing.

   }
}
