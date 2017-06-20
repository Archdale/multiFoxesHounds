import java.awt.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Simulation class is a program that runs and animates a simulation of
 * Foxes and Hounds.
 */

public class Simulation
{

   // The constant CELL_SIZE determines the size of each cell on the
   // screen during animation. (You may change this if you wish.)
   private static final int      CELL_SIZE     = 20;
   private static final String   USAGE_MESSAGE = "Usage: java Simulation "
         + "[--graphics] [--width int] [--height int] [--starvetime int] "
         + "[--fox float] [--hound float]";
   private static AtomicBoolean  _updateField;
   private static CountDownLatch _running;
   private static Field          _theField     = null;

   /**
    * Draws the current state of the field
    *
    * @param graphicsContext
    *           is an optional GUI window to draw to
    * @param theField
    *           is the object to display
    */
   private static void drawField(Graphics graphicsContext, Field theField)
   {
      // If we have a graphics context then update the GUI, otherwise
      // output text-based display
      if (graphicsContext != null)
      {
         // Iterate over the cells and draw the thing in that cell
         for (int i = 0; i < theField.getHeight(); i++)
         {
            for (int j = 0; j < theField.getWidth(); j++)
            {

               graphicsContext
                     .setColor(theField.getOccupantAt(j, i).getDisplayColor());

               graphicsContext.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE,
                     CELL_SIZE);
            } // for
         } // for
      }
      else // No graphics, just text
      {
         // Draw a line above the field
         for (int i = 0; i < theField.getWidth() * 2 + 1; i++)
         {
            System.out.print("-");
         }
         System.out.println();
         // For each cell, display the thing in that cell
         for (int i = 0; i < theField.getHeight(); i++)
         {
            System.out.print("|"); // separate cells with '|'
            for (int j = 0; j < theField.getWidth(); j++)
            {
               System.out.print(theField.getOccupantAt(j, i) + "|");
            }
            System.out.println();
         } // for

         // Draw a line below the field
         for (int i = 0; i < theField.getWidth() * 2 + 1; i++)
         {
            System.out.print("-");
         }
         System.out.println();

      } // else
   } // drawField

   /**
    * Main reads the parameters and performs the simulation and animation.
    */
   public static void main(String[] args) throws InterruptedException
   {
      /**
       * Default parameters. (You may change these if you wish.)
       */
      int width = 50; // Default width
      int height = 50; // Default height
      int starveTime = Hound.DEFAULT_STARVE_TIME; // Default starvation time
      double probabilityFox = 0.1; // Default probability of fox
      double probabilityHound = 0.1; // Default probability of hound
      boolean graphicsMode = false;
      Random randomGenerator = new Random();
      int order;

      // If we attach a GUI to this program, these objects will hold
      // references to the GUI elements
      Frame windowFrame = null;
      Graphics graphicsContext = null;
      Canvas drawingCanvas = null;

      _updateField = new AtomicBoolean();
      _running = new CountDownLatch(1);

      /*
       * Process the input parameters. Switches we understand include:
       * --graphics for "graphics" mode --width 999 to set the "width" --height
       * 999 to set the height --starvetime 999 to set the "starve time" --fox
       * 0.999 to set the "fox probability" --hound 0.999 to set the
       * "hound probability"
       */
      for (int argNum = 0; argNum < args.length; argNum++)
      {
         try
         {
            switch (args[argNum])
            {
               case "--graphics": // Graphics mode
                  graphicsMode = true;
                  break;

               case "--width": // Set width
                  width = Integer.parseInt(args[++argNum]);
                  break;

               case "--height": // set height
                  height = Integer.parseInt(args[++argNum]);
                  break;

               case "--starvetime": // set 'starve time'
                  starveTime = Integer.parseInt(args[++argNum]);
                  break;

               case "--fox": // set the probability for adding a fox
                  probabilityFox = Double.parseDouble(args[++argNum]);
                  break;

               case "--hound": // set the probability for adding a hound
                  probabilityHound = Double.parseDouble(args[++argNum]);
                  break;

               default: // Anything else is an error and we'll quit
                  System.err.println("Unrecognized switch.");
                  System.err.println(USAGE_MESSAGE);
                  System.exit(1);
            } // switch
         }
         catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
         {
            System.err.println("Illegal or missing argument.");
            System.err.println(USAGE_MESSAGE);
            System.exit(1);
         }
      } // for

      // Create the initial Field.
      _theField = new Field(width, height);

      // Set the starve time for hounds
      Hound.setStarveTime(starveTime);

      // Setup Total Ordering
      order = 0;

      // Visit each cell; randomly placing a Fox, Hound, or nothing in each.
      for (int i = 0; i < _theField.getWidth(); i++)
      {
         for (int j = 0; j < _theField.getHeight(); j++)
         {
            // If a random number is greater than or equal to the probability
            // of adding a fox, then place a fox
            if (randomGenerator.nextGaussian() <= probabilityFox)
            {
               _theField.setOccupantAt(i, j,
                     new Fox(_theField, _updateField, _running, i, j, order));
            }
            // If a random number is less than or equal to the probability of
            // adding a hound, then place a hound. Note that if a fox
            // has already been placed, it remains and the hound is
            // ignored.
            if (randomGenerator.nextFloat() <= probabilityHound)
            {
               _theField.setOccupantAt(i, j,
                     new Hound(_theField, _updateField, _running, i, j, order));
            }

            // Fill the nulls with Empty Slots
            if (_theField.getOccupantAt(i, j) == null)
            {
               _theField.setOccupantAt(i, j,
                     new Empty(_theField, _updateField, _running, i, j, order));
            }

            // Build the matrix of locks
            _theField.setLock(new ReentrantLock(), order);

            // Start the new thread; it'll wait until they're all created.
            _theField.getOccupantAt(i, j).start();

            // increment the order
            order++;

         } // for
      } // for

      // If we're in graphics mode, then create the frame, canvas,
      // and window. If not in graphics mode, these will remain null
      if (graphicsMode)
      {
         windowFrame = new Frame("Foxes and Hounds");
         windowFrame.setSize(_theField.getWidth() * CELL_SIZE + 20,
               _theField.getHeight() * CELL_SIZE + 40);
         windowFrame.setVisible(true);

         // Create a "Canvas" we can draw upon; attach it to the window.
         drawingCanvas = new Canvas();
         drawingCanvas.setBackground(Color.gray);
         drawingCanvas.setSize(_theField.getWidth() * CELL_SIZE,
               _theField.getHeight() * CELL_SIZE);
         windowFrame.add(drawingCanvas);
         graphicsContext = drawingCanvas.getGraphics();
      } // if

      // Fire up the threads!
      _running.countDown();

      // Tell it to display the first time.
      _updateField.set(true);
      while (true)
      {
         if (_updateField.get())
         {
            drawField(graphicsContext, _theField); // Draw the current state
            _updateField.set(false);
         }

      }

   } // main
}
