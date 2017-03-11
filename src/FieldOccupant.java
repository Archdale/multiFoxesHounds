import java.awt.Color;


/**
 * Abstract parent class for objects that can occupy a cell in the Field
 */
public abstract class FieldOccupant implements Runnable
{ 
   private Field _theField;
   private int _x;
   private int _y;
   private int _order;
   
   
   public FieldOccupant(Field theField, int x, int y, int order)
   {
      _theField = theField;
      _x = x;
      _y = y;
      _order = order;
   }
   
   /**
    * @return the color to use for a cell containing a particular kind
    *         of occupant
    */
   abstract public Color getDisplayColor();
   
   public int getOrder()
   {
      return _order;
   }
}
