package sim.app.geo.norfolk;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import sim.app.geo.campusworld_norfolk.CampusworldNorfolk;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileExporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;

/**
 * A simple model of Norfolk's political boundary (LSOA) and Road Network
 * with a bunch of agents moving around randomly.
 * 
 * @author KJGarbutt
 *
 */
public class Norfolk extends SimState	{
	/////////////// Model Parameters ///////////////////////////////////
    private static final long serialVersionUID = -4554882816749973618L;

    public static final int WIDTH = 700; 
    public static final int HEIGHT = 700; 
    
    /////////////// Objects //////////////////////////////////////////////
	public int numAgents = 500;
	public int numNGOAgents = 500;
	public int numElderlyAgents = 500;
	public int numLimActAgents = 500;

	/////////////// Containers ///////////////////////////////////////
    public GeomVectorField roads = new GeomVectorField(WIDTH, HEIGHT);
    //public GeomVectorField floodedroads = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField lsoa = new GeomVectorField(WIDTH,HEIGHT);
    public GeomVectorField flood = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField agents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField ngoagents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField elderlyagents = new GeomVectorField(WIDTH, HEIGHT);
    public GeomVectorField limactagents = new GeomVectorField(WIDTH, HEIGHT);

    // Stores the road network connections
    public GeomPlanarGraph network = new GeomPlanarGraph();
    public GeomVectorField junctions = new GeomVectorField(WIDTH, HEIGHT); // nodes for intersections

	///////////////////////////////////////////////////////////////////////////
	/////////////////////////// BEGIN functions ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////
    
    /**
	 * Default constructor function
	 * @param seed
	 */
    public Norfolk(long seed)	{
        super(seed);

		//////////////////////////////////////////////
		///////////// READING IN DATA ////////////////
		//////////////////////////////////////////////
        try	{
        	System.out.println("Reading LSOA layer...");
            URL lsoaGeometry = Norfolk.class.getResource("data/NorfolkLSOA.shp");
            ShapeFileImporter.read(lsoaGeometry, lsoa);
            Envelope MBR = lsoa.getMBR();
            MBR.expandToInclude(lsoa.getMBR());
            
            System.out.println("Reading Flood Zone layer");
            URL floodGeometry = CampusworldNorfolk.class.getResource("data/flood_zone_3_010k_NORFOLK_ONLY.shp");
            ShapeFileImporter.read(floodGeometry, flood);
            MBR.expandToInclude(flood.getMBR());
            
        	System.out.println("Reading Road Network layer...");
            URL roadGeometry = Norfolk.class.getResource("data/NorfolkITN.shp");
            //URL roadGeometry = Norfolk.class.getResource("/Users/KJGarbutt/Desktop/ITNLSOA.shp");
            ShapeFileImporter.read(roadGeometry, roads);
            MBR.expandToInclude(roads.getMBR());
            
            //System.out.println("Reading Flooded Road Network layer...");
            //URL floodedroadGeometry = Norfolk.class.getResource("data/norfolk_NO_FLOODED_ROAD.shp");
            //ShapeFileImporter.read(floodedroadGeometry, floodedroads);
            //MBR.expandToInclude(floodedroads.getMBR());
            
            System.out.println("Done reading data!");
            System.out.println();

            // Now synchronize the MBR for all GeomFields to ensure they cover the same area
            lsoa.setMBR(MBR);
            flood.setMBR(MBR);
            //floodedroads.setMBR(MBR);
            roads.setMBR(MBR);

            System.out.println("Creating road network...");
            network.createFromGeomField(roads);
            //network.createFromGeomField(floodedroads);

            addIntersectionNodes(network.nodeIterator(), junctions);

        } catch (FileNotFoundException ex)	{
            Logger.getLogger(Norfolk.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	//////////////////////////////////////////////
	////////////////// AGENTS ////////////////////
	//////////////////////////////////////////////
	
	// set up the agents in the simulation
    public int getNumAgents() { return numAgents; } 
    public void setNumAgents(int n) { if (n > 0) numAgents = n; } 

    void addAgents()	{
    	System.out.println("Adding agents...");
    	for (int i = 0; i < numAgents; i++)
        {
            MainAgent a = new MainAgent(this);
            agents.addGeometry(a.getGeometry());
            schedule.scheduleRepeating(a); 
        }
        for (int i = 0; i < numNGOAgents; i++)
        {
	        NGOAgent b = new NGOAgent(this);
	        ngoagents.addGeometry(b.getGeometry());
	        schedule.scheduleRepeating(b);
        }
        for (int i = 0; i < numElderlyAgents; i++)
        {
	        ElderlyAgent c = new ElderlyAgent(this);
	        elderlyagents.addGeometry(c.getGeometry());
	        schedule.scheduleRepeating(c);
        }
        for (int i = 0; i < numLimActAgents; i++)
        {
	        LimitedActionsAgent d = new LimitedActionsAgent(this);
	        limactagents.addGeometry(d.getGeometry());
	        schedule.scheduleRepeating(d);
        }
    }

    /**
	 * Finish the simulation and clean up
	 */
    public void finish()	{
    	super.finish();
    	System.out.println("Simulation ended by user.");
        System.out.println("Attempting to export agent data...");
        try	{
        	ShapeFileExporter.write("agents", agents);
        } catch (Exception e)	{
        	System.out.println("Export failed.");
        	e.printStackTrace();
        }
    }
    
    /**
	 * Set up the simulation
	 */
    @Override
    public void start()	{
        super.start();

		//////////////////////////////////////////////
		////////////////// CLEANUP ///////////////////
		//////////////////////////////////////////////
		
        agents.clear(); // clear any existing agents from previous runs
        addAgents();
        System.out.println("Starting simulation...");
        
        // standardize the MBRs for each of the agent classes so that the visualization lines up
        agents.setMBR(roads.getMBR());
        ngoagents.setMBR(roads.getMBR());
        elderlyagents.setMBR(roads.getMBR());
        limactagents.setMBR(roads.getMBR());

        // Ensure that the spatial index is made aware of the new agent
        // positions. Scheduled to guaranteed to run after all agents moved.
        schedule.scheduleRepeating( agents.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);
    }

    /** adds nodes corresponding to road intersections to GeomVectorField
     *
     * @param nodeIterator Points to first node
     * @param intersections GeomVectorField containing intersection geometry
     *
     * Nodes will belong to a planar graph populated from LineString network.
     */
    private void addIntersectionNodes(Iterator nodeIterator, GeomVectorField intersections)	{
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;
        int counter = 0;

        while (nodeIterator.hasNext())	{
                Node node = (Node) nodeIterator.next();
                coord = node.getCoordinate();
                point = fact.createPoint(coord);

                junctions.addGeometry(new MasonGeometry(point));
                counter++;
        }
    }
    
    /**
	 * To run the model without visualization
	 */
    public static void main(String[] args)	{
        doLoop(Norfolk.class, args);
        System.exit(0);
    }
}
