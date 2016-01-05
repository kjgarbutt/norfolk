package sim.app.geo.norfolk;

import com.vividsolutions.jts.io.ParseException;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import sim.app.keepaway.Bot;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 * A simple visualization of Norfolk LSOA and Road Network with a
 * bunch of agents moving around randomly.
 * 
 * @author KJGarbutt
 */
public class NorfolkWithUI extends GUIState	{
    private Display2D display;
    private JFrame displayFrame;

    // Map visualization objects
    private GeomVectorFieldPortrayal lsoaPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal floodPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal roadsPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal ngoagentPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal elderlyagentPortrayal = new GeomVectorFieldPortrayal();
    private GeomVectorFieldPortrayal limactagentPortrayal = new GeomVectorFieldPortrayal();
    
    ///////////////////////////////////////////////////////////////////////////
    /////////////////////////// BEGIN functions ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////	
    
    /** Default constructor */
    public NorfolkWithUI(SimState state)	{
        super(state);
    }

    public NorfolkWithUI() throws ParseException	{
        super(new Norfolk(System.currentTimeMillis()));
    }

    /** Initializes the simulation visualization */
    public void init(Controller controller)	{
        super.init(controller);

        // the map visualization
        display = new Display2D(Norfolk.WIDTH, Norfolk.HEIGHT, this);

        display.attach(lsoaPortrayal, "LSOA", true);
        display.attach(floodPortrayal, "Flood Zone", true);
        display.attach(roadsPortrayal, "Roads", true);
        display.attach(agentPortrayal, "Standard Agents", true);
        display.attach(ngoagentPortrayal, "NGO Agents", true);
        display.attach(elderlyagentPortrayal, "Elderly Agents", true);
        display.attach(limactagentPortrayal, "Limited Actions Agents", true);

        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    /** Begins the simulation */
    public void start()	{
        super.start();
        setupPortrayals();
    }
    
    /**
	 * Sets up the portrayals of objects within the map visualization. This is called by start()
	 */
    private void setupPortrayals()	{
        Norfolk world = (Norfolk)state;

        lsoaPortrayal.setField(world.lsoa);
        lsoaPortrayal.setPortrayalForAll(new GeomPortrayal(Color.LIGHT_GRAY,true));   
        floodPortrayal.setField(world.flood);
        floodPortrayal.setPortrayalForAll(new GeomPortrayal(Color.CYAN,true));
        roadsPortrayal.setField(world.roads);
        roadsPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK,0.1,true));
        
        agentPortrayal.setField(world.agents);
        agentPortrayal.setPortrayalForAll(new MovablePortrayal2D(new OvalPortrayal2D(Color.GREEN, 1)));
        
        ngoagentPortrayal.setField(world.ngoagents);
        ngoagentPortrayal.setPortrayalForAll(new sim.portrayal.simple.RectanglePortrayal2D(Color.RED, 1));
        
        elderlyagentPortrayal.setField(world.elderlyagents);
        elderlyagentPortrayal.setPortrayalForAll(new sim.portrayal.simple.HexagonalPortrayal2D(Color.GRAY, 1));
        
        limactagentPortrayal.setField(world.limactagents);
        limactagentPortrayal.setPortrayalForAll(new GeomPortrayal(Color.ORANGE,50,true));
        // ava.awt.geom.RoundRectangle2D.Float, java.awt.geom.RoundRectangle2D.Double
        
        // reset stuff
     	// reschedule the displayer
        display.reset();
        display.setBackdrop(Color.WHITE);
        // redraw the display
        display.repaint();
    }

    /** Runs the simulation */
    public static void main(String[] args)	{
        NorfolkWithUI worldGUI = null;

        try	{
            worldGUI = new NorfolkWithUI();
        }
        catch (ParseException ex)	{
            Logger.getLogger(NorfolkWithUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        Console console = new Console(worldGUI);
        console.setVisible(true);
    }
}
