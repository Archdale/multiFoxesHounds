import java.awt.Color;

/**
 * Abstract parent class for objects that can occupy a cell in the Field
 */
public abstract class FieldOccupant extends Thread
{
   /*
   private Field         _theField;
   private int           _x;
   private int           _y;
   private int           _order;
   private AtomicBoolean _drawFlag;
   private boolean       _dead;
   */
   
   /*
    * public FieldOccupant(Field theField, int x, int y, int order) { _theField
    * = theField; _x = x; _y = y; _order = order; }
    */

   /**
    * @return the color to use for a cell containing a particular kind of
    *         occupant
    */
   abstract public Color getDisplayColor();

   abstract public int getOrder();

   abstract public int getX();

   abstract public int getY();

   abstract public void kill();
}
