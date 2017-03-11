import java.awt.Color;
import java.util.Set;

/**
 * Foxes can display themselves
 */
public class Fox extends FieldOccupant 
{ 
   private Field _theField;
   private int _x;
   private int _y;
   private int _order;
   
   public Fox(Field theField, int x, int y, int order)
   {
      super(theField, x, y, order);
      // TODO Auto-generated constructor stub
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

   @Override
   public void run()
   {
      // Get neighbors
      Set<FieldOccupant> myNeighbors = _theField.getNeighborsOf(_x, _y);
      
      // Lock Neighbors/Self based on total ordering
      
      // Do stuff
      
      // Unlock Neighbors/Self
      
   }
}
