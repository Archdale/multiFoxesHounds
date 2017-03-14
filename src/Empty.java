import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

public class Empty extends FieldOccupant
{

   private Field         _theField;
   private int           _x;
   private int           _y;
   private int           _order;
   private AtomicBoolean _drawFlag;
   private boolean       _dead;
   private AtomicBoolean _running;

   public Empty(Field theField, AtomicBoolean drawFlag, AtomicBoolean running, int x, int y, int order)
   {
      _theField = theField;
      _x = x;
      _y = y;
      _order = order;
      _drawFlag = drawFlag;
      _running = running;
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
      // Lock Neighbors/Self based on total ordering

      // Do stuff

      // Unlock Neighbors/Self

   }
}
