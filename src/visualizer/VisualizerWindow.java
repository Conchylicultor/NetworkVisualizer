package visualizer;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;

public class VisualizerWindow extends JFrame {

    private Graph<Vertex, Edge> sequenceGraph;
    private VisualizationViewer<Vertex, Edge> networkCanvas;
    
	public VisualizerWindow() {
		// ----- General informations -----
		
		super("Sequence network");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ----- The main graph area -----

        // Create the canvas
		loadGraph();
        networkCanvas = new VisualizationViewer<Vertex, Edge>(new KKLayout<Vertex, Edge>(sequenceGraph));
        
        // Modifier (color, image,...)
        networkCanvas.getRenderContext().setVertexIconTransformer(Vertex.getIconTransformer());
        networkCanvas.getRenderContext().setVertexLabelTransformer(Vertex.getLabelTransformer());
        networkCanvas.getRenderContext().setEdgeDrawPaintTransformer(Edge.getColorTransformer());

        // Mouse control
        PluggableGraphMouse pluggableMouse = new PluggableGraphMouse();
        pluggableMouse.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON2_MASK));
        pluggableMouse.add(new ScalingGraphMousePlugin(new ViewScalingControl(), 0, 1.1f, 0.9f));
        pluggableMouse.add(new PickingGraphMousePlugin<Vertex, Edge>());
        networkCanvas.setGraphMouse(pluggableMouse);
		
		// ----- The right control panel -----
		
        JPanel controlPanel = new JPanel();
        BoxLayout controlPanelLayout = new BoxLayout(controlPanel, BoxLayout.Y_AXIS);
        controlPanel.setLayout(controlPanelLayout);

        final int spacerSize = 8;
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Sequence(s) name: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        //controlPanel.add(sequenceLabelCombo);
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Controls: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        //controlPanel.add(selectAllSimilarButton);
        //controlPanel.add(mergeButton);
        //controlPanel.add(disconnectButton);
        //controlPanel.add(hideNamedSequence);
        //controlPanel.add(plotNameColors);
        //controlPanel.add(new JButton("Save"));
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Sequence images: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        //controlPanel.add(seqImagePane);
        //controlPanel.add(Box.createVerticalGlue());
        
        // ----- Assemble the global ui -----
        
        BoxLayout mainLayout = new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS);
        this.getContentPane().setLayout(mainLayout);
        this.getContentPane().add(networkCanvas);
        this.getContentPane().add(controlPanel);
        
        this.pack();
	}
	
	private void loadGraph()
	{
    	// Our graph
	    sequenceGraph = new UndirectedSparseGraph<Vertex, Edge>();
        
        // Will Create and load the vertex/edge created with the factory
        PajekNetReader<Graph<Vertex, Edge>, Vertex, Edge> fileReader = new PajekNetReader<Graph<Vertex, Edge>, Vertex, Edge>(Vertex.getFactory(), Edge.getFactory());
        
        fileReader.setVertexLabeller(new MapSettableTransformer<Vertex, String>(new HashMap<Vertex, String>()));
        fileReader.setEdgeWeightTransformer(new MapSettableTransformer<Edge, Number>(new HashMap<Edge, Number>()));
        
        try {
            fileReader.load("/home/etienne/__A__/Dev/Reidentification/Data/Debug/network.net", sequenceGraph);
        } catch (IOException e) {
            System.err.println("Cannot load network file");
            e.printStackTrace();
        }

		for (Vertex node : sequenceGraph.getVertices())
		{
			node.setLabel(fileReader.getVertexLabeller().transform(node));
		}
		
		for (Edge edge : sequenceGraph.getEdges())
		{
			edge.setWeight(fileReader.getEdgeWeightTransformer().transform(edge).floatValue());
		}
	}

}
