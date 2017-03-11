import java.awt.Color;

public class Empty extends FieldOccupant
{

   Field _theField;
   int _x;
   int _y;
   int _order;
   
   public Empty(Field theField, int x, int y, int order)
   {
      super(theField, x, y, order);
   }

   @Override
   public Color getDisplayColor()
   {
      // TODO Auto-generated method stub
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

   @Override
   public void run()
   {
      // Lock Neighbors/Self based on total ordering
      
      // Do stuff
      
      // Unlock Neighbors/Self
      
   }
}
