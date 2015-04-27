package visualizer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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
import edu.uci.ics.jung.visualization.picking.PickedState;

public class VisualizerWindow extends JFrame {

    private Graph<Vertex, Edge> sequenceGraph;
    private VisualizationViewer<Vertex, Edge> networkCanvas;

    
    private final List<Vertex> pickedVertexList = new ArrayList<Vertex>();
    private PickedState<Vertex> pickedState;
    
    private JComboBox<String> persNameField;
    private JButton selectAllSimilarButton;
    private JButton disconnectButton;
    private JButton mergeButton;
    private JCheckBox hideNamedSequence;
    private JCheckBox plotNameColors;
    
    private SequenceImagesPane sequenceImagesPane;
    
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
        
        loadControls();

        JScrollPane sequenceImagesScrollPane = new JScrollPane(sequenceImagesPane, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setMaximumSize(new Dimension(230, Integer.MAX_VALUE));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        BoxLayout controlPanelLayout = new BoxLayout(controlPanel, BoxLayout.Y_AXIS);
        controlPanel.setLayout(controlPanelLayout);

        final int spacerSize = 8;
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Sequence(s) name: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(persNameField);
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Controls: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(selectAllSimilarButton);
        controlPanel.add(disconnectButton);
        controlPanel.add(mergeButton);
        controlPanel.add(hideNamedSequence);
        controlPanel.add(plotNameColors);
        //controlPanel.add(new JButton("Save"));
        
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(new JLabel("Sequence images: "));
        controlPanel.add(Box.createVerticalStrut(spacerSize));
        controlPanel.add(sequenceImagesScrollPane);
        
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

	private void loadControls()
	{
	    pickedState = networkCanvas.getPickedVertexState();
        // Attach the listener that will print when the vertices selection changes.
        pickedState.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object subject = e.getItem();
                // The graph uses Integers for vertices.
                if (subject instanceof Vertex)
                {
                    Vertex vertex = (Vertex) subject;
                    if (pickedState.isPicked(vertex))
                    {
                        // Update the model
                        pickedVertexList.add(vertex);
                        
                        // Update the icons
                        vertex.setSelected(true);
                        for(Vertex neighborVertex : sequenceGraph.getNeighbors(vertex))
                        {
                            neighborVertex.neighborSelected();
                        }
                        
                        // Update the sequence pane
                        sequenceImagesPane.setSequence(vertex.getSeqId());
                    }
                    else
                    {
                        // Update the model
                        pickedVertexList.remove(vertex);
                        
                        // Update the icons
                        vertex.setSelected(false);
                        for(Vertex neighborVertex : sequenceGraph.getNeighbors(vertex))
                        {
                            neighborVertex.neighborDeselected();
                        }
                        
                        // Update the sequence pane
                        if(pickedVertexList.isEmpty())
                        {
                            sequenceImagesPane.setSequence("");
                        }
                    }
                    // The parent function will repaint the graph
                }
            }
        });
        
        
        persNameField = new JComboBox<String>();
        persNameField.setEditable(true);
        persNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, persNameField.getPreferredSize().height));
        persNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        persNameField.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                // Rename the current selection
                String currentText = ((JTextField)persNameField.getEditor().getEditorComponent()).getText();
                for(Vertex currentVertex : pickedVertexList)
                {
                    currentVertex.setPersName(currentText);
                }
                
                // Add the name to the list (if not already present)
                ComboBoxModel<String> model = persNameField.getModel();
                boolean found = false;
                for(int i=0 ; i<model.getSize() ; i++) {
                    if(currentText.equals((String)model.getElementAt(i)))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    persNameField.addItem(currentText);
                }
                
                networkCanvas.repaint();
            }
        });
        
        
        selectAllSimilarButton = new JButton("Select all similar");
        selectAllSimilarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(pickedVertexList.size() > 0)
                {
                    Vertex first = pickedVertexList.get(0); // We pick the first selected person
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        if(currentVertex.getPersName().equals(first.getPersName()))
                        {
                            pickedState.pick(currentVertex, true); // Will repaint the graph
                        }
                    }
                }
            }
        });
        
        
        disconnectButton = new JButton("Disconnect selection");
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

                List<Edge> listRemove = new ArrayList<Edge>();
                // For all selected nodes
                for(Vertex pickedVertex : pickedVertexList)
                {
                    // We check if some neighbor are not selected
                    for(Vertex neighborVertex : sequenceGraph.getNeighbors(pickedVertex))
                    {
                        if(!pickedVertexList.contains(neighborVertex))
                        {
                            neighborVertex.neighborDeselected();
                            listRemove.add(sequenceGraph.findEdge(pickedVertex, neighborVertex));
                        }
                    }
                }
                for(Edge currentEdge : listRemove)
                {
                    sequenceGraph.removeEdge(currentEdge);
                }
                
                networkCanvas.repaint();
            }
        });
        
        
        mergeButton = new JButton("Merge together");
        mergeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final float maxWeightValue = 2.f; // TODO : Define max value
                for(Vertex vertexI : pickedVertexList)
                {
                    for(Vertex vertexJ : pickedVertexList)
                    {
                        if(vertexI != vertexJ)
                        {
                            if(sequenceGraph.findEdge(vertexI, vertexJ) != null)
                            {
                                sequenceGraph.findEdge(vertexI, vertexJ).setWeight(maxWeightValue); // Set max weight
                            }
                            else
                            {
                                sequenceGraph.addEdge(new Edge(maxWeightValue), vertexI, vertexJ);
                                vertexI.neighborSelected();
                                vertexJ.neighborSelected();
                            }
                        }
                    }
                }
                
                networkCanvas.repaint();
            }
        });
        
        
        hideNamedSequence = new JCheckBox("Hide all named sequences");
        hideNamedSequence.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(hideNamedSequence.isSelected())
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setIsHidden(true);
                    }
                }
                else
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setIsHidden(false);
                    }
                }
                networkCanvas.repaint();
            }
        });
        
        
        plotNameColors = new JCheckBox("Show colors of named sequences");
        plotNameColors.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(plotNameColors.isSelected())
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setPlotNameColor(true);
                    }
                }
                else
                {
                    for(Vertex currentVertex : sequenceGraph.getVertices())
                    {
                        currentVertex.setPlotNameColor(false);
                    }
                }
                networkCanvas.repaint();
            }
        });
        
        
        sequenceImagesPane = new SequenceImagesPane();
	}
}